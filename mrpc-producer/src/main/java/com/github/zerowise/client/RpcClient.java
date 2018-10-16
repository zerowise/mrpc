package com.github.zerowise.client;

import com.github.zerowise.conf.ProducerCnf;
import com.github.zerowise.loadbalance.LoadBalance;
import com.github.zerowise.loadbalance.Weightable;
import com.github.zerowise.message.RpcReqMessage;
import com.github.zerowise.netty.Service;
import com.github.zerowise.netty.ServiceListener;
import com.github.zerowise.rpc.RpcInvoker;
import com.github.zerowise.tools.ClazzUtil;
import com.github.zerowise.zk.Discover;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 ** @createtime : 2018/10/12下午3:24
 **/
public class RpcClient implements Service {

    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private Discover discover;
    private ProducerCnf producerCnf;

    private RpcInvoker invoker;

    private ConcurrentHashMap<String, ConnectContext> activeMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ConnectContext> zombieMap = new ConcurrentHashMap<>();

    private volatile boolean isCloseing = false;

    // 并发的做一些建立连接、心跳等后台工作，线程数量用配置的方式更合理一些，但需要用户深入理解这个逻辑，暂时先这样
    private static final ForkJoinPool appForkJoinPool = new ForkJoinPool(64);

    private LoadBalance<ConnectContext> loadBalance;

    static {
        // 自动资源清理
        Runtime.getRuntime()//
                .addShutdownHook(new Thread(() -> appForkJoinPool.shutdownNow(), "appForkJoinPool-shutdown-thread"));
    }

    public RpcClient(ProducerCnf producerCnf) {
        this.producerCnf = producerCnf;
        try {
            loadBalance = (LoadBalance<ConnectContext>) ClazzUtil.findClazz(producerCnf.getLoadBalaceClass()).newInstance();
            discover = (Discover) ClazzUtil.findClazz(producerCnf.getDiscover().getClassName()).newInstance();
            discover.init(producerCnf.getDiscover().getAddress());
            discover.addListener(producerCnf.getGroup(), producerCnf.getApp(), serverWithWeight -> {
                if (isCloseing) {
                    return;
                }

                if (logger.isInfoEnabled()) {
                    logger.info("Discover检测到服务变化: " + serverWithWeight);
                }

                try {
                    updateConnectors(serverWithWeight);
                } catch (Exception e) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Discover连接出错: " + serverWithWeight, e);
                    }
                }
            });
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void updateConnectors(Map<String, Integer> serverWithWeight) {
        if (serverWithWeight == null || serverWithWeight.size() == 0) {
            return;
        }

        if (isCloseing) {
            return;
        }

        try {
            AtomicBoolean changed = new AtomicBoolean(false);
            // 未建立连接的建立连接
            appForkJoinPool.submit(() -> {
                serverWithWeight//
                        .entrySet()//
                        .stream()//
                        .parallel()// 并发的建立连接
                        .filter(kv -> !activeMap.containsKey(kv.getKey()))// 过滤掉已连接上的
                        .forEach(kv -> {
                            if (isCloseing) {
                                return;
                            }
                            changed.set(true);
                            updateConnector(kv.getKey(), kv.getValue());
                        });
            }).get();

            // 删除多余的连接
            appForkJoinPool.submit(() -> {
                activeMap//
                        .entrySet()//
                        .stream()//
                        .parallel()//
                        .forEach(kv -> {
                            if (isCloseing) {
                                return;
                            }

                            String serverAddress = kv.getKey();

                            if (!serverWithWeight.containsKey(serverAddress)) {
                                changed.set(true);
                                ConnectContext context = activeMap.remove(serverAddress);
                                context.stop();
                            }
                        });
            }).get();

            // 删除多余的连接
            appForkJoinPool.submit(() -> {
                zombieMap//
                        .entrySet()//
                        .stream()//
                        .parallel()//
                        .forEach(kv -> {
                            if (isCloseing) {
                                return;
                            }
                            String serverAddress = kv.getKey();
                            if (!serverWithWeight.containsKey(serverAddress)) {
                                ConnectContext context = zombieMap.remove(serverAddress);
                                context.stop();
                            }
                        });
            }).get();

            if(changed.get()){
                loadBalance.updateWeightables(Lists.newArrayList(activeMap.values()));
            }

        } catch (Exception e) {
            logger.error("", e);
        }


    }

    private void updateConnector(String key, int weight) {

        String[] addr = key.split(":");
        ConnectContext connectContext = new ConnectContext(producerCnf.getConnectCnt(), new InetSocketAddress(addr[0], Integer.parseInt(addr[1])));
        connectContext.start();
        connectContext.setWeight(weight);
        connectContext.setInvoker(invoker);
        activeMap.put(key, connectContext);
        zombieMap.remove(key);
    }


    @Override
    public void start(ServiceListener listener) {

    }

    @Override
    public void stop(ServiceListener listener) {
        if (isCloseing) {
            return;
        }
        isCloseing = false;
        activeMap.values().forEach(connectContext -> connectContext.stop());
        activeMap.clear();
        zombieMap.clear();
        try {
            discover.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setInvoker(RpcInvoker invoker) {
        this.invoker = invoker;
        activeMap.values().forEach(connectContext -> connectContext.setInvoker(invoker));
    }

    public void writeMessage(Object rpcReqMessage) {
        loadBalance.select().writeMessage(rpcReqMessage);
    }
}
