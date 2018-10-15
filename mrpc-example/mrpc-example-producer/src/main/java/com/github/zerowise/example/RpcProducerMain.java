package com.github.zerowise.example;

import com.github.zerowise.client.RpcClient;
import com.github.zerowise.netty.ServiceListener;
import com.github.zerowise.rpc.RpcInvoker;
import com.github.zerowise.rpc.RpcProxy;

public class RpcProducerMain {
    public static void main(String[] args) {
//        RpcClient rpcClient = RpcClient.createFixedAddrClient("localhost", 6666);
//
//        rpcClient.start(ServiceListener.NONE);
//
//        RpcInvoker rpcInvoker = new RpcInvoker(rpcClient);
//
//        RpcCalService rpcCalService = new RpcProxy(rpcInvoker).newProxy(RpcCalService.class);
//
//        System.out.println(rpcCalService.add(100, 99223));
//
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> rpcClient.stop(ServiceListener.NONE)));
    }
}