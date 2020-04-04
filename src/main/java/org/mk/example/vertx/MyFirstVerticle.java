package org.mk.example.vertx;

import java.util.LinkedHashMap;
import java.util.Map;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class MyFirstVerticle extends AbstractVerticle {

	
	// Store our product
	private Map<Integer, Whisky> products = new LinkedHashMap<>();
	// Create some product
	private void createSomeData() {
	  Whisky bowmore = new Whisky("Bowmore 15 Years Laimrig", "Scotland, Islay");
	  products.put(bowmore.getId(), bowmore);
	  Whisky talisker = new Whisky("Talisker 57° North", "Scotland, Island");
	  products.put(talisker.getId(), talisker);
	}
	
	
	@Override
	public void start(Future<Void> fut) {
		/*
		 * vertx.createHttpServer() .requestHandler(requestHandler ->
		 * requestHandler.response().
		 * end("<h1>Hello from my first Vert.x application</h1>")) .listen(8080, result
		 * -> { if (result.succeeded()) { fut.complete(); } else {
		 * fut.fail(result.cause()); } });
		 */

		createSomeData();
		Router router  = Router.router(vertx);
		
		router.route("/hello").handler(requestHandler ->{
			HttpServerResponse response = requestHandler.response();
			response.putHeader("content-type", "text/html")
			.end("</pre>\r\n" + 
					"<h1>Hello from my first Vert.x 3 app</h1>\r\n" + 
					"<pre>");
		});
		
		//enables the reading of the request body for all routes under “/api/whiskies”
		router.route("/api/whiskies*").handler(BodyHandler.create());
		
		router.get("/api/whiskies").handler(this::getAll);
		
		router.post("/api/whiskies").handler(this::addOne);
		
		router.delete("/api/whiskies/:id").handler(this::deleteOne);
		
		router.get("/api/whiskies/:id").handler(this::getOne);
		
		vertx.createHttpServer()
		.requestHandler(reqHandler -> router.accept(reqHandler))
		.listen(
			config().getInteger("http.port", 8080),
			result -> {
				if (result.succeeded()) {
					fut.complete();
				} else {
					fut.fail(result.cause());
				}

		});
		
		
	}
	
	private void getAll(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.end(Json.encodePrettily(products.values()));
	}
	
	private void addOne(RoutingContext routingContext) {
		final Whisky whisky = Json.decodeValue(routingContext.getBodyAsString(), Whisky.class);
		products.put(whisky.getId(), whisky);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(Json.encodePrettily(whisky));
	}
	
	private void deleteOne(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		if (id == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			Integer idAsInteger = Integer.valueOf(id);
			products.remove(idAsInteger);
		}
		routingContext.response().setStatusCode(204).end();
	}
	
	private void getOne(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		if (id == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			Integer idAsInteger = Integer.valueOf(id);

			if (products.containsKey(idAsInteger)) {
				Whisky whisky = products.get(idAsInteger);
				routingContext.response().setStatusCode(200)
						.putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(whisky));
			}else {
				routingContext.response().setStatusCode(404).end();	
			}
		}
	}
}
