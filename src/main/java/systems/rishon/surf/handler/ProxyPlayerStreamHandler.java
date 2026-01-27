package systems.rishon.surf.handler;

import com.hypixel.hytale.protocol.Packet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.incubator.codec.quic.QuicStreamChannel;
import systems.rishon.surf.SurfProxy;
import systems.rishon.surf.session.ProxyPlayerSession;

public final class ProxyPlayerStreamHandler
        extends SimpleChannelInboundHandler<Packet> {

    private final SurfProxy proxy;
    private final ProxyPlayerSession session;

    public ProxyPlayerStreamHandler(
            SurfProxy proxy,
            ProxyPlayerSession session
    ) {
        this.proxy = proxy;
        this.session = session;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        QuicStreamChannel backend = session.backendStream();
        if (backend == null || !backend.isActive()) {
            proxy.getLogger().warning("Backend not connected yet, dropping packet: " + packet.getClass().getSimpleName());
            return;
        }

        proxy.getLogger().info("Forwarding packet to backend: " + packet.getClass().getSimpleName());
        backend.writeAndFlush(packet);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        QuicStreamChannel backendStream = session.backendStream();
        if (backendStream != null) {
            backendStream.close();
        }

        proxy.onPlayerDisconnect(session);
    }
}
