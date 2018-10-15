package com.github.zerowise.example;

import com.github.zerowise.conf.ConsumerCnf;
import com.github.zerowise.conf.RegisterCnf;
import com.github.zerowise.netty.Service;
import com.github.zerowise.netty.ServiceListener;
import com.github.zerowise.server.RpcDispatcher;
import com.github.zerowise.server.RpcServer;
import com.github.zerowise.tools.Pair;
import com.github.zerowise.zk.Register;
import com.github.zerowise.zk.ZkRegister;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 ** @createtime : 2018/10/12下午4:52
 **/
public class RpcComsumerMain {

    private static final Logger log = LoggerFactory.getLogger(RpcComsumerMain.class);

    private static Map<String, Pair<Service, Register>> consumerMap = new HashMap<>();

    public static void main(String[] args) {

        ConsumerCnf consumerCnf = new ConsumerCnf();
        consumerCnf.parse(ConfigFactory.load("mrpc-consumer-sample.conf"));

        RpcDispatcher rpcDispatcher = RpcDispatcher.type();
        rpcDispatcher.register(new RpcCalServiceImpl());

        consumerCnf.getRegisters().forEach(registerCnf -> {
            Register register = create(registerCnf);
            register.init(registerCnf.getRegisterAddr());
            RpcServer rpcServer = new RpcServer(Integer.parseInt(registerCnf.getServerAddr().split(":")[1]), rpcDispatcher);
            rpcServer.start(new ServiceListener() {
                @Override
                public void onSuccess() {
                    register.register(consumerCnf.getGroup(), consumerCnf.getApp(), registerCnf.getServerAddr(), registerCnf.getWeight());
                    consumerMap.putIfAbsent(registerCnf.getServerAddr(), new Pair<>(rpcServer, register));
                }

                @Override
                public void onFailure(Throwable t) {

                }
            });
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> consumerMap.forEach((addr, pair) ->
                pair.getM().stop(new ServiceListener() {
                    @Override
                    public void onSuccess() {
                        pair.getN().unregister(consumerCnf.getGroup(), consumerCnf.getApp(), addr);
                        try {
                            pair.getN().close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {

                    }
                })

        )));
    }

    private static Register create(RegisterCnf registerCnf) {
        try {
            return (Register) registerCnf.getRegisterClazz().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("", e);
        }
        return null;
    }
}
