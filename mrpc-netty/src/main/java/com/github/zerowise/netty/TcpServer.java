package com.github.zerowise.netty;

import com.github.zerowise.codec.RpcMessageDecoder;
import com.github.zerowise.codec.RpcMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 ** @createtime : 2018/10/12下午2:30
 **/
public abstract class TcpServer implements Service {
    protected EventLoopGroup boss;
    protected EventLoopGroup worker;

    protected int port;

    public TcpServer(int port) {
        this.port = port;
        createBossGroup();
        createWorkerGroup();
    }

    @Override
    public void start() {
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
        b.bind(port);
    }

    protected void createBossGroup() {
        boss = new NioEventLoopGroup(1);
    }

    protected void createWorkerGroup() {
        worker = new NioEventLoopGroup();
    }

    protected abstract ChannelHandler getChannelHandler();

    @Override
    public void stop() {
        if (boss != null) boss.shutdownGracefully().syncUninterruptibly();//要先关闭接收连接的main reactor
        if (worker != null) worker.shutdownGracefully().syncUninterruptibly();//再关闭处理业务的sub reactor
    }

}
