package com.github.zerowise.example;

import com.github.zerowise.netty.ServiceListener;
import com.github.zerowise.server.RpcDispatcher;
import com.github.zerowise.server.RpcServer;

/**
 ** @createtime : 2018/10/12下午4:52
 **/
public class RpcComsumerMain {

    public static void main(String[] args) {
        RpcDispatcher rpcDispatcher = RpcDispatcher.type();
        rpcDispatcher.register(new RpcCalServiceImpl());
        RpcServer rpcServer = new RpcServer(6666, rpcDispatcher);
        rpcServer.start(ServiceListener.NONE);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> rpcServer.stop(ServiceListener.NONE)));
    }
}
