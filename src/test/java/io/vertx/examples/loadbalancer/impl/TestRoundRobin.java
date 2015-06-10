package io.vertx.examples.loadbalancer.impl;

import static org.junit.Assert.assertEquals;
import io.vertx.core.http.HttpClient;
import io.vertx.examples.loadbalancer.TestBase;
import io.vertx.examples.loadbalancer.routing.RoutingPolicy;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class TestRoundRobin extends TestBase {

	@Test
	public void testRoundRobin(TestContext context) {
		Async async = context.async();
		HttpClient client = client();
		client.getNow("/test", response1 -> {
			response1.bodyHandler(buff1 -> {
				assertEquals("Handled by Server1", buff1.toString());
				client.getNow("/test", response2 -> {
					response2.bodyHandler(buff2 -> {
						assertEquals("Handled by Server2", buff2.toString());
						client.getNow("/test", response3 -> {
							response3.bodyHandler(buff3 -> {
								assertEquals("Handled by Server3", buff3.toString());
								client.getNow("/test", response4 -> {
									response4.bodyHandler(buff4 -> {
										assertEquals("Handled by Server1", buff4.toString());
										async.complete();
									});
								});
							});
						});
					});
				});
			});
		});
	}

	@Override
	protected RoutingPolicy getPolicy() {
		return RoutingPolicy.ROUND_ROBIN;
	}
}
