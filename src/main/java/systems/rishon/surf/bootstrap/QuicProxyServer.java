package systems.rishon.surf.bootstrap;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.incubator.codec.quic.QuicServerCodecBuilder;
import io.netty.incubator.codec.quic.QuicSslContext;
import io.netty.incubator.codec.quic.QuicSslContextBuilder;
import systems.rishon.surf.SurfProxy;
import systems.rishon.surf.config.ProxyConfig;
import systems.rishon.surf.handler.ProxyInitialConnectionHandler;
import systems.rishon.surf.transport.TransportSelector;

import java.net.InetSocketAddress;
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
        group = TransportSelector.createEventLoopGroup();

        SelfSignedCertificate cert;
        try {
            cert = SelfSignedCertificate.builder().build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create self-signed certificate", e);
        }

        QuicSslContext sslContext =
                QuicSslContextBuilder.forServer(
                        cert.privateKey(),
                        null,
                        cert.certificate()
                ).applicationProtocols("hytale").build();

        ChannelHandler codec =
                new QuicServerCodecBuilder()
                        .sslContext(sslContext)
                        .maxIdleTimeout(30, TimeUnit.SECONDS)
                        .initialMaxData(10_000_000)
                        .initialMaxStreamDataBidirectionalLocal(1_000_000)
                        .initialMaxStreamsBidirectional(100)
                        .build();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .handler(codec);

        Channel channel = bootstrap.bind(
                new InetSocketAddress(
                        config.bindHost(),
                        config.bindPort().intValue()
                )
        ).syncUninterruptibly().channel();

        proxy.getLogger().info(
                "QUIC proxy listening on "
                        + config.bindHost() + ":" + config.bindPort()
        );

        channel.pipeline().addLast(new ProxyInitialConnectionHandler(proxy));
    }

    public void shutdown() {
        if (group != null) {
            group.shutdownGracefully();
        }
    }
}