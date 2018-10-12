package com.github.zerowise;

import com.github.zerowise.codec.RpcMessageDecoder;
import com.github.zerowise.codec.RpcMessageEncoder;
import com.github.zerowise.netty.Service;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

/**
 ** @createtime : 2018/10/12下午1:07
 **/
public class ClientMain implements Service {

    private final EventLoopGroup worker;

    private Channel channel;

    public ClientMain() {
        worker = new NioEventLoopGroup();
    }


    @Override
    public void start() {

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(worker).channel(NioSocketChannel.class).handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new RpcMessageDecoder(), new RpcMessageEncoder(), new SimpleChannelInboundHandler<Object>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

                    }
                });
            }
        }).option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);//
        CountDownLatch countDownLatch = new CountDownLatch(1);
        channel = bootstrap.connect("localhost", ServerMain.port).addListener(future -> {
            countDownLatch.countDown();
        }).channel();

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            Method m1 = SmileJava.class.getMethod("add", int.class, int.class);
            Smile s1 = new Smile(m1, new Object[]{10000, 200000});

            channel.writeAndFlush(s1);
            System.out.println(s1);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        worker.shutdownGracefully();
    }

    public static void main(String[] args) {
        new ClientMain().start();
    }
}
