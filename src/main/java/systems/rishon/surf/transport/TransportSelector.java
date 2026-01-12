package systems.rishon.surf.transport;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueDatagramChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

public final class TransportSelector {

    private TransportSelector() {
    }

    public static EventLoopGroup createEventLoopGroup() {
        if (Epoll.isAvailable()) {
            return new EpollEventLoopGroup();
        }
        if (KQueue.isAvailable()) {
            return new KQueueEventLoopGroup();
        }
        return new NioEventLoopGroup();
    }

    public static Class<? extends DatagramChannel> datagramChannel() {
        if (Epoll.isAvailable()) {
            return EpollDatagramChannel.class;
        }
        if (KQueue.isAvailable()) {
            return KQueueDatagramChannel.class;
        }
        return NioDatagramChannel.class;
    }
}