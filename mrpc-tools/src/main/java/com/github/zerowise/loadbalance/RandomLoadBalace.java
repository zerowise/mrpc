package com.github.zerowise.loadbalance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 ** @createtime : 2018/10/16上午11:14
 **/
public final class RandomLoadBalace<T extends Weightable> implements LoadBalance<T> {


    protected volatile WeightableGroup<T> weightableGroup = null;

    @Override
    public void updateWeightables(List<T> weightables) {
        weightableGroup = new WeightableGroup<>(weightables);
    }

    @Override
    public T select() {
        final WeightableGroup<T> weightableGroup = this.weightableGroup;

        if (weightableGroup == null) {
            return null;
        }

        int sum = weightableGroup.sum();

        if (sum < 2) {
            return weightableGroup.get(0);
        }

        int seed = ThreadLocalRandom.current().nextInt(sum + 1);
        return weightableGroup.get(seed);
    }
}
