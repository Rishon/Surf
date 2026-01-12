package systems.rishon.surf.bootstrap;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.quic.QuicChannel;
import io.netty.incubator.codec.quic.QuicServerCodecBuilder;
import io.netty.incubator.codec.quic.QuicSslContext;
import io.netty.incubator.codec.quic.QuicSslContextBuilder;
import systems.rishon.surf.SurfProxy;
import systems.rishon.surf.config.ProxyConfig;
import systems.rishon.surf.handler.ProxyInitialConnectionHandler;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class QuicProxyServer {

    private final ProxyConfig config;
    private final SurfProxy proxy;
    private EventLoopGroup group;

    public QuicProxyServer(ProxyConfig config, SurfProxy proxy) {
        this.config = config;
        this.proxy = proxy;
    }

    public void start() {
        group = new NioEventLoopGroup();

        DynamicCert.KeyPairAndCert keyCert;
        try {
            keyCert = DynamicCert.generate("Surf-Proxy");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        QuicSslContext sslContext = QuicSslContextBuilder.forServer(
                keyCert.key(),
                UUID.randomUUID().toString().replace("-", ""),
                keyCert.cert()
        ).applicationProtocols("Surf-Proxy").build();

        ChannelHandler codec = new QuicServerCodecBuilder()
                .sslContext(sslContext)
                .maxIdleTimeout(30, TimeUnit.SECONDS)
                .initialMaxData(10 * 1024 * 1024)
                .initialMaxStreamDataBidirectionalLocal(1024 * 1024)
                .initialMaxStreamsBidirectional(100)

                .handler(new SimpleChannelInboundHandler<QuicChannel>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, QuicChannel quicChannel) {
                        proxy.getLogger().info("New QUIC connection: " + quicChannel);

                        quicChannel.pipeline().addLast(
                                new ProxyInitialConnectionHandler(proxy)
                        );
                    }
                })
                .build();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .handler(codec);

        bootstrap.bind(
                new InetSocketAddress(
                        config.bindHost(),
                        config.bindPort().intValue()
                )
        ).syncUninterruptibly();

        proxy.getLogger().info(
                "QUIC proxy listening on "
                        + config.bindHost() + ":" + config.bindPort()
        );
    }

    public void shutdown() {
        if (group != null) {
            group.shutdownGracefully();
        }
    }
}