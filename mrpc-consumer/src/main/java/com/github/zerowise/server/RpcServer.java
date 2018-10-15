package com.github.zerowise.server;

import com.github.zerowise.message.RpcReqMessage;
import com.github.zerowise.netty.ServiceListener;
import com.github.zerowise.netty.TcpServer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


/**
 ** @createtime : 2018/10/12下午4:20
 **/
public class RpcServer extends TcpServer {

    private RpcDispatcher rpcDispatcher;

    public RpcServer(int port, RpcDispatcher rpcDispatcher) {
        super(port);
        this.rpcDispatcher = rpcDispatcher;
    }

    @Override
    protected ChannelHandler channelHandler() {
        return new SimpleChannelInboundHandler<RpcReqMessage>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, RpcReqMessage msg) throws Exception {
                rpcDispatcher.execute(ctx, msg);
            }
        };
    }

    @Override
    public void stop(ServiceListener listener) {
        super.stop(listener);
        rpcDispatcher.shutdown();
    }
}
