package systems.rishon.surf.handler;

import com.hypixel.hytale.protocol.Packet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import systems.rishon.surf.SurfProxy;
import systems.rishon.surf.session.ProxyPlayerSession;

public final class BackendChannelHandler extends SimpleChannelInboundHandler<Packet> {

    private final SurfProxy proxy;
    private final ProxyPlayerSession session;

    public BackendChannelHandler(SurfProxy proxy, ProxyPlayerSession session) {
        this.proxy = proxy;
        this.session = session;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        session.clientStream().writeAndFlush(packet);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        proxy.onPlayerDisconnect(session);
    }
}
