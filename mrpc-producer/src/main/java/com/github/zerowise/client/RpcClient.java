package com.github.zerowise.client;

import com.github.zerowise.netty.TcpClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetSocketAddress;

/**
 ** @createtime : 2018/10/12下午3:24
 **/
public abstract class RpcClient extends TcpClient {
    @Override
    protected SimpleChannelInboundHandler handler() {
        return new SimpleChannelInboundHandler() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                System.out.println(msg);
            }
        };
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
