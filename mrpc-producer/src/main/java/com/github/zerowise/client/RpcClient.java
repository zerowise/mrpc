package com.github.zerowise.client;

import com.github.zerowise.codec.RpcMessageDecoder;
import com.github.zerowise.codec.RpcMessageEncoder;
import com.github.zerowise.message.RpcRespMessage;
import com.github.zerowise.netty.Service;
import com.github.zerowise.netty.ServiceListener;
import com.github.zerowise.rpc.RpcInvoker;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 ** @createtime : 2018/10/12下午3:24
 **/
public class RpcClient implements Service {

    private RpcInvoker invoker;
    private int connectCnt;
    private EventLoopGroup worker;
    private SocketAddress socketAddress;

    private BatchSender[] senders;

    private AtomicInteger idGen = new AtomicInteger(0);

    public RpcClient(int connectCnt, String host, int port) {
        this.connectCnt = connectCnt;
        this.worker = new NioEventLoopGroup();
        this.socketAddress = new InetSocketAddress(host, port);
    }

    protected ChannelHandler handler() {
        return new SimpleChannelInboundHandler<RpcRespMessage>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, RpcRespMessage msg) throws Exception {
                invoker.onMessageReceive(msg);
            }
        };
    }

    @Override
    public void start(ServiceListener listener) {

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(worker).channel(NioSocketChannel.class).handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new RpcMessageDecoder(), new RpcMessageEncoder(), handler());
            }
        }).option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);//

        senders = new BatchSender[connectCnt];
        for (int i = 0; i < connectCnt; i++) {
            senders[i] = new BatchSender(bootstrap.connect(socketAddress).addListener(future -> listener.onSuccess()).channel());
        }
    }

    @Override
    public void stop(ServiceListener listener) {
        worker.shutdownGracefully();
        Stream.of(senders).forEach(send -> {
            try {
                send.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void writeMessage(Object rpcReqMessage) {
        senders[idGen.incrementAndGet() % connectCnt].send(rpcReqMessage);
    }

    public void setInvoker(RpcInvoker invoker) {
        this.invoker = invoker;
    }
}
