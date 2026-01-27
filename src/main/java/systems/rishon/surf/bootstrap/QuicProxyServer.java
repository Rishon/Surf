package systems.rishon.surf.bootstrap;

import com.hypixel.hytale.protocol.io.netty.PacketDecoder;
import com.hypixel.hytale.protocol.io.netty.PacketEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.incubator.codec.quic.*;
import io.netty.util.concurrent.Future;
import systems.rishon.surf.SurfProxy;
import systems.rishon.surf.config.BackendServer;
import systems.rishon.surf.config.ProxyConfig;
import systems.rishon.surf.handler.BackendChannelHandler;
import systems.rishon.surf.handler.ProxyPlayerStreamHandler;
import systems.rishon.surf.session.ProxyPlayerSession;
import systems.rishon.surf.transport.QUICTransport;

import javax.net.ssl.SSLEngine;
import java.net.InetSocketAddress;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class QuicProxyServer {

    private final ProxyConfig config;
    private final SurfProxy proxy;
    private EventLoopGroup group;
    private QuicSslContext serverSslContext;
    private QuicSslContext clientSslContext;

    public QuicProxyServer(ProxyConfig config, SurfProxy proxy) {
        this.config = config;
        this.proxy = proxy;
    }

    public void start() {
        group = new NioEventLoopGroup();

        try {
            DynamicCert.KeyPairAndCert keyCert = DynamicCert.generate("Surf-Proxy");

            serverSslContext = QuicSslContextBuilder.forServer(
                            keyCert.key(),
                            "",
                            keyCert.cert()
                    )
                    .applicationProtocols("hytale/2", "hytale/1")
                    .earlyData(false)
                    .clientAuth(ClientAuth.REQUIRE)
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

            clientSslContext = QuicSslContextBuilder.forClient()
                    .keyManager(keyCert.key(), "", keyCert.cert())
                    .applicationProtocols("hytale/2", "hytale/1")
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        protected void initChannel(DatagramChannel ch) {
                            ChannelHandler codec = new QuicServerCodecBuilder()
                                    .sslContext(serverSslContext)
                                    .congestionControlAlgorithm(QuicCongestionControlAlgorithm.BBR)
                                    .tokenHandler(InsecureQuicTokenHandler.INSTANCE)
                                    .maxIdleTimeout(30, TimeUnit.SECONDS)
                                    .initialMaxData(524288)
                                    .initialMaxStreamDataUnidirectional(0)
                                    .initialMaxStreamsUnidirectional(0)
                                    .initialMaxStreamDataBidirectionalLocal(2000000)
                                    .initialMaxStreamDataBidirectionalRemote(2000000)
                                    .initialMaxStreamsBidirectional(100)
                                    .handler(new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) {
                                            QuicChannel ch = (QuicChannel) ctx.channel();
                                            String negotiatedAlpn = ch.sslEngine().getApplicationProtocol();
                                            int version = parseAlpnVersion(negotiatedAlpn);
                                            if (version < 2) {
                                                ch.attr(QUICTransport.ALPN_REJECT_ERROR_CODE_ATTR).set(5);
                                            }
                                            X509Certificate cert = extractClientCert(ch);
                                            if (cert == null) {
                                                ch.close();
                                                return;
                                            }
                                            ch.attr(QUICTransport.CLIENT_CERTIFICATE_ATTR).set(cert);
                                        }

                                        private int parseAlpnVersion(String alpn) {
                                            if (alpn != null && alpn.startsWith("hytale/")) {
                                                try {
                                                    return Integer.parseInt(alpn.substring(7));
                                                } catch (NumberFormatException ignored) {
                                                }
                                            }
                                            return 0;
                                        }

                                        private X509Certificate extractClientCert(QuicChannel channel) {
                                            try {
                                                SSLEngine engine = channel.sslEngine();
                                                Certificate[] certs = engine.getSession().getPeerCertificates();
                                                if (certs != null && certs.length > 0 && certs[0] instanceof X509Certificate) {
                                                    return (X509Certificate) certs[0];
                                                }
                                            } catch (Exception ignored) {
                                            }
                                            return null;
                                        }
                                    })
                                    .streamHandler(new ChannelInitializer<QuicStreamChannel>() {
                                        @Override
                                        protected void initChannel(QuicStreamChannel stream) {

                                            Integer rejectErrorCode = stream.parent()
                                                    .attr(QUICTransport.ALPN_REJECT_ERROR_CODE_ATTR)
                                                    .get();

                                            if (rejectErrorCode != null) {
                                                stream.close();
                                                return;
                                            }

                                            String sessionId = UUID.randomUUID().toString();
                                            ProxyPlayerSession session = new ProxyPlayerSession(sessionId);
                                            session.attachClientStream(stream);

                                            stream.pipeline().addLast(
                                                    new PacketDecoder(),
                                                    new PacketEncoder(),
                                                    new ProxyPlayerStreamHandler(proxy, session)
                                            );

                                            BackendServer backend = proxy.getJoinRouter().selectInitialServer();
                                            proxy.getServer().connectToBackend(session, backend);

                                        }
                                    })
                                    .build();

                            ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                            ch.pipeline().addLast(codec);
                        }
                    });

            bootstrap.bind(config.bindHost(), config.bindPort().intValue()).syncUninterruptibly();
            proxy.getLogger().info("Proxy listening on " + config.bindHost() + ":" + config.bindPort());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void connectToBackend(ProxyPlayerSession session, BackendServer backend) {
        Bootstrap udpBootstrap = new Bootstrap();
        udpBootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    protected void initChannel(DatagramChannel ch) {
                        QuicClientCodecBuilder codecBuilder = new QuicClientCodecBuilder()
                                .sslContext(clientSslContext)
                                .maxIdleTimeout(30, TimeUnit.SECONDS)
                                .initialMaxData(524288)
                                .initialMaxStreamDataBidirectionalLocal(2000000)
                                .initialMaxStreamDataBidirectionalRemote(2000000)
                                .initialMaxStreamsBidirectional(100);

                        ch.pipeline().addLast(codecBuilder.build());
                    }
                });

        udpBootstrap.bind(0).addListener((ChannelFuture bindFuture) -> {
            if (!bindFuture.isSuccess()) {
                session.clientStream().close();
                return;
            }

            DatagramChannel udpChannel = (DatagramChannel) bindFuture.channel();

            QuicChannelBootstrap quicBootstrap = new QuicChannelBootstrap(udpChannel)
                    .remoteAddress(new InetSocketAddress(backend.host(), backend.port().intValue()))
                    .handler(new ChannelInboundHandlerAdapter() {
                    })
                    .streamHandler(new ChannelInitializer<QuicStreamChannel>() {
                        @Override
                        protected void initChannel(QuicStreamChannel stream) {
                            session.setBackendStream(stream);
                            stream.pipeline().addLast(
                                    new PacketDecoder(),
                                    new PacketEncoder(),
                                    new BackendChannelHandler(proxy, session)
                            );
                        }
                    });

            quicBootstrap.connect().addListener((Future<QuicChannel> quicFuture) -> {
                if (!quicFuture.isSuccess()) {
                    session.clientStream().close();
                    return;
                }

                QuicChannel backendQuicChannel = quicFuture.getNow();

                backendQuicChannel.createStream(
                        QuicStreamType.BIDIRECTIONAL,
                        new ChannelInitializer<QuicStreamChannel>() {
                            @Override
                            protected void initChannel(QuicStreamChannel stream) {
                                session.setBackendStream(stream);
                                stream.pipeline().addLast(
                                        new PacketDecoder(),
                                        new PacketEncoder(),
                                        new BackendChannelHandler(proxy, session)
                                );
                            }
                        }
                ).addListener((Future<QuicStreamChannel> streamFuture) -> {
                    if (streamFuture.isSuccess()) {
                        session.connectTo(backend);
                        proxy.getLogger().info("Connected to backend " + backend.name());
                    } else {
                        session.clientStream().close();
                    }
                });
            });
        });
    }

    public void shutdown() {
        if (group != null) {
            group.shutdownGracefully();
        }
    }
}
