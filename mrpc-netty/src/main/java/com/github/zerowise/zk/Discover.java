package com.github.zerowise.zk;

import java.io.Closeable;

/**
 ** @createtime : 2018/10/15下午4:54
 **/
public interface Discover extends Closeable {

    void init(String serverList);

    /**
     * 添加一个服务变化回调
     *
     * @param group
     * @param app
     * @param listener
     */
    void addListener(String group, String app, DiscoverListener listener);
}
