package com.github.zerowise.netty;

import com.github.zerowise.codec.RpcMessageDecoder;
import com.github.zerowise.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 ** @createtime : 2018/10/12下午3:03
 **/
public abstract class TcpClient implements Service {

    private final EventLoopGroup worker;

    protected Channel channel;

    private AtomicBoolean isConnecting;
    private Bootstrap bootstrap;

    public TcpClient() {
        worker = new NioEventLoopGroup(1);
        isConnecting = new AtomicBoolean(false);
    }


    @Override
    public void start() {
        bootstrap = new Bootstrap();
        bootstrap.group(worker).channel(NioSocketChannel.class).handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new RpcMessageDecoder(), new RpcMessageEncoder(), handler());
            }
        }).option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);//
        channel = bootstrap.connect(remoteAddr()).channel();
    }

    /**
     * 尝试重连
     */
    public void tryReConnect() {
        Runnable runnable = () -> {
            if (isConnecting.compareAndSet(false, true)) {
                channel = bootstrap.connect(remoteAddr()).addListener(future -> {
                    if (!future.isSuccess()) {
                        isConnecting.set(false);
                    }
                }).channel();
            }
        };

        new Thread(runnable).start();
    }

    protected abstract InetSocketAddress remoteAddr();

    protected abstract ChannelHandler handler();

    @Override
    public void stop() {
        worker.shutdownGracefully();
    }
}
