package io.vertx.examples.loadbalancer;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.examples.loadbalancer.routing.RoutingPolicy;
import io.vertx.ext.unit.TestContext;

import org.junit.After;
import org.junit.Before;

abstract public class TestBase {

	protected final static String PROXY_HOST = "localhost";
	protected final static Integer PROXY_PORT = 9090;

	protected final static String SERVER_HOST_1 = "localhost";
	protected final static Integer SERVER_PORT_1 = 9191;

	protected final static String SERVER_HOST_2 = "localhost";
	protected final static Integer SERVER_PORT_2 = 9292;

	protected final static String SERVER_HOST_3 = "localhost";
	protected final static Integer SERVER_PORT_3 = 9393;

	protected Vertx vertx;

	@Before
	public void createLoadBalancer(TestContext context) {
		if (vertx == null) {
			vertx = Vertx.vertx();
		}
		DeploymentOptions options = new DeploymentOptions();
		JsonObject config = new JsonObject();
		config.put("policy", getPolicy().toString());
		JsonArray slaves = new JsonArray();
		JsonObject proxyOptions = new JsonObject();
		proxyOptions.put("host", PROXY_HOST);
		proxyOptions.put("port", PROXY_PORT);
		config.put("server", proxyOptions);
		JsonObject server1Options = new JsonObject();
		server1Options.put("host", SERVER_HOST_1);
		server1Options.put("port", SERVER_PORT_1);
		slaves.add(server1Options);
		JsonObject server2Options = new JsonObject();
		server2Options.put("host", SERVER_HOST_2);
		server2Options.put("port", SERVER_PORT_2);
		slaves.add(server2Options);
		JsonObject server3Options = new JsonObject();
		server3Options.put("host", SERVER_HOST_3);
		server3Options.put("port", SERVER_PORT_3);
		slaves.add(server3Options);
		config.put("slaves", slaves);
		options.setConfig(config);
		//options.setInstances(4);
		vertx.deployVerticle(new LoadBalancerVerticle(), options, context.asyncAssertSuccess());
	}

	@Before
	public void createSlave1(TestContext context) {
		createSlave(context, SERVER_HOST_1, SERVER_PORT_1, "Handled by Server1");
	}

	@Before
	public void createSlave2(TestContext context) {
		createSlave(context, SERVER_HOST_2, SERVER_PORT_2, "Handled by Server2");
	}

	@Before
	public void createSlave3(TestContext context) {
		createSlave(context, SERVER_HOST_3, SERVER_PORT_3, "Handled by Server3");
	}

	@After
	public void tearDown(TestContext context) {
		if (vertx != null) {
			vertx.close(context.asyncAssertSuccess());
		}
	}

	protected HttpClient client() {
		HttpClientOptions options = new HttpClientOptions();
		options.setDefaultHost(PROXY_HOST);
		options.setDefaultPort(PROXY_PORT);
		return vertx.createHttpClient(options);
	}

	private void createSlave(TestContext context, String host, int port, String response) {
		if (vertx == null) {
			vertx = Vertx.vertx();
		}
		HttpServerOptions options = new HttpServerOptions();
		options.setHost(host);
		options.setPort(port);
		vertx.createHttpServer(options).requestHandler(request -> {
			request.response().end(response);
		}).listen(context.asyncAssertSuccess());
	}

	abstract protected RoutingPolicy getPolicy();
}
