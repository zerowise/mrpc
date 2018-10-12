package com.github.zerowise.client;

import com.github.zerowise.message.RpcRespMessage;
import com.github.zerowise.netty.TcpClient;
import com.github.zerowise.rpc.RpcInvoker;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetSocketAddress;

/**
 ** @createtime : 2018/10/12下午3:24
 **/
public abstract class RpcClient extends TcpClient {

    private RpcInvoker invoker;

    @Override
    protected ChannelHandler handler() {
        return new SimpleChannelInboundHandler<RpcRespMessage>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, RpcRespMessage msg) throws Exception {
                invoker.onMessageReceive(msg);
            }
        };
    }

    public void writeMessage(Object rpcReqMessage) {
        channel.writeAndFlush(rpcReqMessage);
    }

    public void setInvoker(RpcInvoker invoker) {
        this.invoker = invoker;
    }

    static class FixedAddrRpcClient extends RpcClient {

        private final InetSocketAddress remoteAddr;

        FixedAddrRpcClient(String host, int port) {
            remoteAddr = new InetSocketAddress(host, port);
        }

        @Override
        protected InetSocketAddress remoteAddr() {
            return remoteAddr;
        }
    }


    public static RpcClient createFixedAddrClient(String host, int port) {
        return new FixedAddrRpcClient(host, port);
    }
}
