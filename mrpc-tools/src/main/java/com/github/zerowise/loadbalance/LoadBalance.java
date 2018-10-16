package com.github.zerowise.loadbalance;

import java.util.List;

/**
 ** @createtime : 2018/10/16上午11:15
 **/
public interface LoadBalance<T extends Weightable> {

    /**
     * 可多次重复设置
     *
     * @param weightables
     */
    void updateWeightables(List<T> weightables);

    /**
     * 选出一个来
     *
     * @return
     */
    T select();
}
