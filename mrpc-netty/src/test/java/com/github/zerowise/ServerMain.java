package com.github.zerowise;

import com.github.zerowise.codec.RpcMessageDecoder;
import com.github.zerowise.codec.RpcMessageEncoder;
import com.github.zerowise.netty.Service;
import com.github.zerowise.netty.ServiceListener;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

/**
 ** @createtime : 2018/10/12下午1:04
 **/
public class ServerMain implements Service {

    public static final int port = 7788;

    private final EventLoopGroup boss;
    private final EventLoopGroup worker;

    public ServerMain() {
        boss = new NioEventLoopGroup();
        worker = new NioEventLoopGroup();
    }


    @Override
    public void start(ServiceListener listener) {
        ServerBootstrap b = new ServerBootstrap();
        b.group(boss, worker).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new RpcMessageDecoder(),
                        new RpcMessageEncoder(), getChannelHandler());
            }
        });

        /**
         * 在Netty 4中实现了一个新的ByteBuf内存池，它是一个纯Java版本的 jemalloc （Facebook也在用）。
         * 现在，Netty不会再因为用零填充缓冲区而浪费内存带宽了。不过，由于它不依赖于GC，开发人员需要小心内存泄漏。
         * 如果忘记在处理程序中释放缓冲区，那么内存使用率会无限地增长。
         * Netty默认不使用内存池，需要在创建客户端或者服务端的时候进行指定
         */
        b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        b.bind(port).addListener(future ->
            System.out.println("server start success on:" + port)
        );
    }

    protected ChannelHandler getChannelHandler() {
        return new SimpleChannelInboundHandler<Object>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                System.out.println(msg);
            }
        };
    }

    @Override
    public void stop(ServiceListener listener) {
        if (boss != null) boss.shutdownGracefully().syncUninterruptibly();//要先关闭接收连接的main reactor
        if (worker != null) worker.shutdownGracefully().syncUninterruptibly();//再关闭处理业务的sub reactor
    }

    public static void main(String[] args) {
        new ServerMain().start(ServiceListener.NONE);
    }
}
