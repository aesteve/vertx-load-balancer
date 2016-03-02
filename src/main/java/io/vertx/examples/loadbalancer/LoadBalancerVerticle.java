package io.vertx.examples.loadbalancer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.examples.loadbalancer.routing.Router;
import io.vertx.examples.loadbalancer.routing.RoutingPolicy;
import io.vertx.examples.proxy.handler.ProxyHandler;

import java.util.ArrayList;
import java.util.List;

public class LoadBalancerVerticle extends AbstractVerticle {
	private final static Logger log = LoggerFactory.getLogger(LoadBalancerVerticle.class);
	private final static String DEFAULT_HOST = "localhost";
	private final static Integer DEFAULT_PORT = 8080;

	private HttpServer server;
	private HttpServerOptions options;
	private List<ProxyHandler> slaves;

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
		slaves = new ArrayList<>();
	}

	@Override
	public void start(Future<Void> future) {
		log.info("Starting load balancer");
		createServerOptions();
		server = vertx.createHttpServer(options);
		JsonObject config = context.config();
		JsonArray slavesConfig = config.getJsonArray("slaves");
		slavesConfig.forEach(slaveConfig -> {
			try {
				ProxyHandler proxy = new ProxyHandler(vertx, (JsonObject) slaveConfig);
				slaves.add(proxy);
			} catch (Throwable t) {
				if (!future.failed()) {
					future.fail(t);
				}
				return;
			}
		});
		if (future.failed()) {
			return;
		}
		Router router = Router.create(RoutingPolicy.valueOf(config.getString("policy")), slaves);
		server.requestHandler(router);
		server.listen(res -> {
			if (res.failed()) {
				future.fail(res.cause());
			} else {
				log.info("...Proxy server started");
				future.complete();
			}
		});
	}

	@Override
	public void stop(Future<Void> future) {
		log.info("Closing load balancer");
		if (server == null) {
			future.complete();
			return;
		}
		server.close(res -> {
			if (res.failed()) {
				log.error("Could not close web server", res.cause());
			}
			log.info("Load balancer closed");
			future.complete();
		});
	}

	private void createServerOptions() {
		options = new HttpServerOptions();
		JsonObject config = context.config();
		JsonObject serverConfig = config.getJsonObject("server");
		if (serverConfig == null) {
			options.setHost(DEFAULT_HOST);
			options.setPort(DEFAULT_PORT);
			return;
		}
		options.setHost(serverConfig.getString("host", DEFAULT_HOST));
		options.setPort(serverConfig.getInteger("port", DEFAULT_PORT));
		/*
		 * README : add your options there. For example : clientAuthRequired,
		 * ssl configuration, ...
		 */
	}

}
