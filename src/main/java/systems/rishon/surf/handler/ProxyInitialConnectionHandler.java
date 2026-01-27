package systems.rishon.surf.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.incubator.codec.quic.QuicChannel;
import systems.rishon.surf.SurfProxy;
import systems.rishon.surf.session.ProxyPlayerSession;

import java.util.UUID;

public final class ProxyInitialConnectionHandler
        extends SimpleChannelInboundHandler<QuicChannel> {

    private final SurfProxy proxy;

    public ProxyInitialConnectionHandler(SurfProxy proxy) {
        this.proxy = proxy;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, QuicChannel quicChannel) {
        String sessionId = UUID.randomUUID().toString();

        ProxyPlayerSession session =
                new ProxyPlayerSession(sessionId);

        proxy.getLogger().info(
                "Accepted client session " + sessionId
        );

        proxy.onPlayerConnect(session, quicChannel);
    }
}