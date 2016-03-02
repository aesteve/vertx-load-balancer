package io.vertx.examples.loadbalancer.routing;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.examples.proxy.handler.ProxyHandler;

import java.util.List;

public abstract class Router implements Handler<HttpServerRequest> {

	private final static Logger log = LoggerFactory.getLogger(Router.class);
	protected List<ProxyHandler> slaves;

	public static Router create(RoutingPolicy policy, List<ProxyHandler> slaves) {
		Class<? extends Router> clazz = policy.implementation;
		Router router;
		try {
			router = clazz.newInstance();
			router.init(slaves);
			return router;
		} catch (InstantiationException | IllegalAccessException e) {
			log.error("Could not instantiate router for " + policy, e);
			return null;
		}
	}

	@Override
	public void handle(HttpServerRequest request) {
		chooseProxyFor(request).forward(request);
	}

	protected void init(List<ProxyHandler> slaves) {
		this.slaves = slaves;
	}

	abstract protected ProxyHandler chooseProxyFor(HttpServerRequest request);
}
