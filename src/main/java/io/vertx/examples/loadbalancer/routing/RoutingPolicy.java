package io.vertx.examples.loadbalancer.routing;

import io.vertx.examples.loadbalancer.routing.impl.RoundRobinRouter;

public enum RoutingPolicy {

	ROUND_ROBIN(RoundRobinRouter.class);

	public Class<? extends Router> implementation;

	RoutingPolicy(Class<? extends Router> implementation) {
		this.implementation = implementation;
	}
}
