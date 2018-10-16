package com.github.zerowise.zk;

import java.util.Map;

@FunctionalInterface
public interface DiscoverListener {
    /**
     * 当服务发生变化时调用
     *
     * @param serverWithWeight
     */
    void onChange(Map<String, Integer> serverWithWeight);
}