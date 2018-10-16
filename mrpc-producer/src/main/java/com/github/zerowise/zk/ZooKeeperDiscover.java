package com.github.zerowise.zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ZooKeeperDiscover implements Discover {
	private static final Logger logger = LoggerFactory.getLogger(ZooKeeperDiscover.class);

	private CuratorFramework client;
	private List<PathChildrenCache> watchers;

	@Override
	public void init(String hostPorts) {
		watchers = Collections.synchronizedList(new ArrayList<>());
		RetryPolicy retryPolicy = new ForeverRetryPolicy(1000, 60 * 1000);
		client = CuratorFrameworkFactory.newClient(hostPorts, 1000 * 10, 1000 * 3, retryPolicy);
		client.start();
	}

	@Override
	public void addListener(String group, String app, final DiscoverListener listener) {
		Objects.requireNonNull(listener, "listener is null");
		Objects.requireNonNull(client, "call init first");

		final String path = "/turbo/" + group + "/" + app;

		final PathChildrenCache watcher = new PathChildrenCache(client, path, true);

		PathChildrenCacheListener pathChildrenCacheListener = new PathChildrenCacheListener() {
			private final ConcurrentMap<String, Integer> serverWithWeight = new ConcurrentHashMap<>();
			private volatile boolean waitForInitializedEvent = true;

			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
				if (logger.isInfoEnabled()) {
					logger.info("zk监控列表发生变化, " + path + ", " + event.getType());
				}

				boolean isChanged = true;

				switch (event.getType()) {

				case INITIALIZED:
					waitForInitializedEvent = false;

					if (logger.isInfoEnabled()) {
						logger.info("完成初始化: " + path);
					}

					break;

				case CHILD_ADDED: {
					AddressWithWeight kv = new AddressWithWeight(event.getData().getData());
					serverWithWeight.put(kv.getServerAddr(), kv.getWeight());

					if (logger.isInfoEnabled()) {
						logger.info("新增节点: " + kv);
					}

					break;
				}

				case CHILD_REMOVED: {
					AddressWithWeight kv = new AddressWithWeight(event.getData().getData());
					serverWithWeight.remove(kv.getServerAddr());

					if (logger.isInfoEnabled()) {
						logger.info("删除节点: " + kv);
					}

					break;
				}

				case CHILD_UPDATED: {
					AddressWithWeight kv = new AddressWithWeight(event.getData().getData());
					serverWithWeight.put(kv.getServerAddr(), kv.getWeight());

					if (logger.isInfoEnabled()) {
						logger.info("更新节点: " + kv);
					}

					break;
				}

				default:
					isChanged = false;

					if (logger.isInfoEnabled()) {
						logger.info("忽略, " + path + ", " + event.getType());
					}
				}

				if (!waitForInitializedEvent && isChanged) {
					try {
						listener.onChange(serverWithWeight);
					} catch (Throwable t) {
						if (logger.isWarnEnabled()) {
							logger.warn("Discover监听处理失败", t);
						}
					}
				}
			}
		};

		watcher.getListenable().addListener(pathChildrenCacheListener);

		try {
			watcher.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
			watchers.add(watcher);
		} catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error("zk监听失败, " + path, e);
			}
		}
	}

	@Override
	public void close() throws IOException {
		for (int i = 0; i < watchers.size(); i++) {
			PathChildrenCache watcher = watchers.get(i);

			try {
				watcher.close();
			} catch (Exception e) {
				if (logger.isErrorEnabled()) {
					logger.error("watcher关闭失败 ", e);
				}
			}
		}

		watchers = null;

		client.close();
		client = null;
	}
}
