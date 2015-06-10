package io.vertx.examples.loadbalancer.routing.impl;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.examples.loadbalancer.routing.Router;
import io.vertx.examples.proxy.handler.ProxyHandler;

import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinRouter extends Router {

	private AtomicInteger counter;

	public RoundRobinRouter() {
		counter = new AtomicInteger(0);
	}

	@Override
	protected ProxyHandler chooseProxyFor(HttpServerRequest request) {
		int i = counter.getAndIncrement();
		counter.compareAndSet(slaves.size(), 0);
		return slaves.get(i);
	}
}
