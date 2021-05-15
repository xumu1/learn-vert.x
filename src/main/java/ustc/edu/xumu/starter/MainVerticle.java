package ustc.edu.xumu.starter;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.Router;


public class MainVerticle extends AbstractVerticle {
  @Override
  public void start() throws Exception {
    // Create a Router
    Router router = Router.router(vertx);

    // Mount the handler for all incoming requests at every path and HTTP method
    router.route().handler(context -> {
      // Get the address of the request
      String address = context.request().connection().remoteAddress().toString();
      // Get the query parameter "name"
      MultiMap queryParams = context.queryParams();
      String name = queryParams.contains("name") ? queryParams.get("name") : "unknown";
      // Write a json response
      context.json(
        new JsonObject()
          .put("name", name)
          .put("address", address)
          .put("message", "Hello " + name + " connected from " + address)
      );
    });

    // Create the HTTP server
    vertx.createHttpServer()
      // Handle every request using the router
      .requestHandler(router)
      // Start listening
      .listen(8888)
      // Print the port
      .onSuccess(server ->
        System.out.println(
          "HTTP server started on port " + server.actualPort()
        )
      );

    final Context context = vertx.getOrCreateContext();
    context.put("data", "hello");
    context.runOnContext((v) -> {
      String hello = context.get("data");
    });
    Buffer.buffer();

//    HttpClientOptions options = new HttpClientOptions().setLogActivity(true);
//    HttpClient client = vertx.createHttpClient(options);
//    client.request(HttpMethod.GET,8080, "myserver.mycompany.com", "/some-uri", ar1 -> {
//      if (ar1.succeeded()) {
//        // Connected to the server
//      }
//    });

//    HttpClient client = vertx.createHttpClient();
//
//    client.request(HttpMethod.POST, "some-uri")
//      .onSuccess(request -> {
//        request.response().onSuccess(response -> {
//          System.out.println("Received response with status code " + response.statusCode());
//        });
//
//        // Now do stuff with the request
//        request.putHeader("content-length", "1000");
//        request.putHeader("content-type", "text/plain");
//        request.write("body");
//
//        // Make sure the request is ended when you're done with it
//        request.end();
//      });

    HttpClient client = vertx.createHttpClient();
    Future<JsonObject> future = client
      .request(HttpMethod.GET, "some-uri")
      .compose(request -> request
        .send()
        .compose(response -> {
          // Process the response on the event-loop which guarantees no races
          if (response.statusCode() == 200 &&
            response.getHeader(HttpHeaders.CONTENT_TYPE).equals("application/json")) {
            return response
              .body()
              .map(buffer -> buffer.toJsonObject());
          } else {
            return Future.failedFuture("Incorrect HTTP response");
          }
        }));

// Listen to the composed final json result
    future.onSuccess(json -> {
      System.out.println("Received json result " + json);
    }).onFailure(err -> {
      System.out.println("Something went wrong " + err.getMessage());
    });


    SharedData sharedData = vertx.sharedData();

    LocalMap<String, String> map1 = sharedData.getLocalMap("mymap1");

    map1.put("foo", "bar"); // Strings are immutable so no need to copy


    vertx.executeBlocking(promise -> {
      // Call some blocking API that takes a significant amount of time to return
      String result = "hello";
      promise.complete(result);
    }, res -> {
      System.out.println("The result is: " + res.result());
    });
  }
}
