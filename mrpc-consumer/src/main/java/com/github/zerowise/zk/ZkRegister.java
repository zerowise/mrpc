package com.github.zerowise.zk;

import com.github.zerowise.tools.Hex;
import org.apache.curator.RetryPolicy;
import org.apache.curator.RetrySleeper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

/**
 ** @createtime : 2018/10/15下午5:01
 **/
public class ZkRegister implements Register {

    private static final Logger logger = LoggerFactory.getLogger(ZkRegister.class);

    private CuratorFramework client;
    private ConcurrentMap<String, PathChildrenCache> watcherMap;

    @Override
    public void init(String serverList) {
        watcherMap = new ConcurrentHashMap<>();
        RetryPolicy retryPolicy = new ForeverRetryPolicy(1000, 60 * 1000);
        client = CuratorFrameworkFactory.newClient(serverList, 1000 * 10, 1000 * 3, retryPolicy);
        client.start();
    }

    @Override
    public void register(String group, String app, String serverAddress, int serverWeight) {
        Objects.requireNonNull(client, "call init first");

        final String path = "/turbo/" + group + "/" + app  + "/"
                + Hex.byte2HexStr(serverAddress.getBytes(StandardCharsets.UTF_8));

        byte[] data = new AddressWithWeight(serverAddress, serverWeight).toBytes();

        try {
            if (client.checkExists().forPath(path) != null) {
                if (logger.isInfoEnabled()) {
                    logger.info("删除zk已存在节点: " + path + ", " + serverAddress);
                }

                client.delete().forPath(path);
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("zk已存在节点删除失败, " + path + ", " + serverAddress, e);
            }
        }

        try {
            client//
                    .create()//
                    .creatingParentsIfNeeded()//
                    .withMode(CreateMode.EPHEMERAL)//
                    .forPath(path, data);

            if (logger.isInfoEnabled()) {
                logger.info("zk注册成功, " + path + ", " + serverAddress + "@" + serverWeight);
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("zk注册失败, " + path + ", " + serverAddress + "@" + serverWeight, e);
            }
        }

        if (!watcherMap.containsKey(path)) {
            addRegisterWatcher(group, app, serverAddress, serverWeight);
        }
    }

    private void addRegisterWatcher(String group, String app, String serverAddress, int serverWeight) {
        final String path = "/turbo/" + group + "/" + app + "/"
                + Hex.byte2HexStr(serverAddress.getBytes(StandardCharsets.UTF_8));

        if (watcherMap.containsKey(path)) {
            return;
        }

        PathChildrenCache watcher = new PathChildrenCache(client, path, false);


        watcher.getListenable().addListener(new PathChildrenCacheListener() {
            private volatile boolean waitForInitializedEvent = true;

            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {

                    case INITIALIZED:
                        waitForInitializedEvent = false;
                        break;

                    case CONNECTION_RECONNECTED:
                        if (waitForInitializedEvent) {
                            return;
                        }

                        if (logger.isInfoEnabled()) {
                            logger.info("获得zk连接尝试重新注册, " + path + ", " + serverAddress + "@" + serverWeight);
                        }

                        ZkRegister.this.register(group, app, serverAddress, serverWeight);

                        break;

                    default:
                        break;
                }
            }
        });

        try {
            watcher.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
            watcherMap.put(path, watcher);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("zk监听失败, " + path, e);
            }
        }
    }

    @Override
    public void unregister(String group, String app, String serverAddress) {
        String path = "/turbo/" + group + "/" + app + "/"
                + Hex.byte2HexStr(serverAddress.getBytes(StandardCharsets.UTF_8));

        try {
            PathChildrenCache watcher = watcherMap.remove(path);
            if (watcher != null) {
                watcher.close();
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("warcher关闭失败, " + path + ", " + serverAddress, e);
            }
        }

        try {
            if (client.checkExists().forPath(path) != null) {
                client.delete().forPath(path);
            }

            if (logger.isInfoEnabled()) {
                logger.info("zk注销成功, " + path + ", " + serverAddress);
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("zk注销失败, " + path + ", " + serverAddress, e);
            }
        }
    }

    @Override
    public void close() throws IOException {
        watcherMap.forEach((path, watcher) -> {
            try {
                watcher.close();
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("warcher关闭失败, " + path);
                }
            }
        });

        watcherMap = null;

        client.close();
        client = null;
    }


    class ForeverRetryPolicy implements RetryPolicy {
        private final int baseSleepTimeMs;
        private final int maxSleepMs;

        public ForeverRetryPolicy(int baseSleepTimeMs, int maxSleepMs) {
            checkArgument(baseSleepTimeMs > 0);
            checkArgument(maxSleepMs > 0);
            checkArgument(maxSleepMs >= baseSleepTimeMs);

            this.baseSleepTimeMs = baseSleepTimeMs;
            this.maxSleepMs = maxSleepMs;
        }

        @Override
        public boolean allowRetry(int retryCount, long elapsedTimeMs, RetrySleeper sleeper) {
            try {
                sleeper.sleepFor(getSleepTimeMs(retryCount, elapsedTimeMs), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }

            return true;
        }

        private long getSleepTimeMs(int retryCount, long elapsedTimeMs) {
            if (retryCount < 0) {
                return maxSleepMs;
            }

            long sleepMs = baseSleepTimeMs * (retryCount + 1);

            if (sleepMs > maxSleepMs || sleepMs <= 0) {
                sleepMs = maxSleepMs;
            }

            return sleepMs;
        }

    }

}
