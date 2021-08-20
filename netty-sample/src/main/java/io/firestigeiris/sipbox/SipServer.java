package io.firestigeiris.sipbox;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * @author firestige
 * @version [version], 2021-08-20
 * @since [version]
 */
public class SipServer {
    private final int port;

    public SipServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        final SipServerHandler handler = new SipServerHandler();
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(handler);
                        }
                    });
            bootstrap.bind().sync().channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}
