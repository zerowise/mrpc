package com.github.zerowise.loadbalance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalance<T extends Weightable> implements LoadBalance<T> {

	private final AtomicInteger sequencer = new AtomicInteger(0);

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

		int seed = sequencer.incrementAndGet();
		return weightableGroup.get(seed);
	}

}
