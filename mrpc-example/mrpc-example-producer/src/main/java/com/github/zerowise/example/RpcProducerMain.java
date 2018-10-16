package com.github.zerowise.example;

import com.github.zerowise.client.RpcClient;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.List;

public class RpcProducerMain {
    public static void main(String[] args) {

        Config config = ConfigFactory.load("mrpc-producer-sample.conf");

        List<? extends Config> configs =config.getConfigList("apps");

        //consumerCnf.parse();

        //RpcClient rpcClient = new RpcClient();
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