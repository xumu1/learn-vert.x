/**
 * @(#)MyVerticel.java, 5月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package ustc.edu.xumu.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;

/**
 * @author xumu-bak
 */
public class MyVerticel extends AbstractVerticle {
  private HttpServer server;

  @Override
  public void start(Promise<Void> startPromise) {
    server = vertx.createHttpServer().requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!");
    });

    // Now bind the server:
    server.listen(8080, res -> {
      if (res.succeeded()) {
        startPromise.complete();
      } else {
        startPromise.fail(res.cause());
      }
    });
  }
}
