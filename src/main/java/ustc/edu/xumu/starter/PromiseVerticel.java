/**
 * @(#)PromiseVerticel.java, 5月 17, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package ustc.edu.xumu.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.*;

import java.util.ArrayList;

/**
 * @author xumu-bak
 */
public class PromiseVerticel extends AbstractVerticle {
  //配置连接参数
  PgConnectOptions connectOptions = new PgConnectOptions()
    .setPort(5432)
    .setHost("xx.xx.xx.xx")
    .setDatabase("xxxx")
    .setUser("xxxx")
    .setPassword("xxxx")
    .addProperty("search_path", "music");

  //配置连接池
  PoolOptions poolOptions = new PoolOptions()
    .setMaxSize(5);

  PgPool client;
  Router router;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    client = PgPool.pool(vertx,connectOptions,poolOptions);
    router = Router.router(vertx);

    router.route("/").handler(req->{
      req.response()
        .putHeader("content-type","text/plain")
        .putHeader("charset", "utf-8")
        .end("你好，谢谢，我是主页。");
    });

    router.route("/test/list").handler(req->{
      Integer page;
      String temp = req.request().getParam("page");
      if (temp==null){
        page = 1;
      }else{
        page = Integer.valueOf(temp);
      }

      Integer offset = (page-1)*3;

      this.getCon()
        .compose(con->this.getRows(con, offset))
        .onSuccess(rows -> {
          ArrayList list = new ArrayList<JsonObject>();
          rows.forEach(item->{
            JsonObject json = new JsonObject();
            json.put("id", item.getValue("id"));
            json.put("name", item.getValue("name"));
            list.add(json);
          });
          req.response()
            .putHeader("content-type","application/json")
            .putHeader("charset", "utf-8")
            .end("你好，谢谢，我是列表。"+ list.toString());
        });
    });

    //创建http连接
    vertx.createHttpServer().requestHandler(router).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }


  //获取数据库连接
  private Future<SqlConnection> getCon(){
    Promise<SqlConnection> promise = Promise.promise();
    // Get a connection from the pool
    client.getConnection(ar1 -> {
      if (ar1.succeeded()) {
        System.out.println("Connected");
        // Obtain our connection
        SqlConnection conn = ar1.result();
        promise.complete(conn);
      }else {
        promise.fail(ar1.cause());
        System.out.println("Could not connect: " + ar1.cause().getMessage());
      }});
    return promise.future();
  }


  //用获取到的连接查询数据库
  private Future<RowSet<Row>> getRows(SqlConnection conn, Integer offset){
    Promise<RowSet<Row>> promise = Promise.promise();
    conn
      .preparedQuery("SELECT id,name FROM zc_test limit 3 offset $1")
      .execute(Tuple.of(offset), ar2 -> {
        // Release the connection to the pool
        conn.close();
        if (ar2.succeeded()) {
          promise.complete(ar2.result());
        } else {
          promise.fail(ar2.cause());
        }
      });
    return promise.future();
  }
}
