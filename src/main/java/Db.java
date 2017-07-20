import io.vertx.core.*;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.UpdateOptions;

import java.util.List;

public class Db {
    public static final String COLLECTION = "vlogin";
    private MongoClient client;
    public Db(Vertx vertx, JsonObject config){
        JsonObject params = new JsonObject()
                .put("connection_string", config.getString("db_connectionString"));
        this.client = MongoClient.createShared(vertx, params);
        System.out.println("Db initialized.");

        // setup test accounts
        CompositeFuture
        .all(
            this.upsert("foo@foo.com", "foo"),
            this.upsert("bar@bar.com", "bar"))
        .setHandler((res)->{
            System.out.println("Db test accounts created.");
        });

    }

    // find doc
    /*
    public Future<String> find(String email, String password){
        JsonObject query = new JsonObject()
                .put("email", email)
                .put("password", password);
        return this.client.find(COLLECTION, query, docs -> {
            if(!docs.succeeded()) {
                return Future.failedFuture(docs.cause());
            }
            if(docs.result().size()==0){
                return Future.failedFuture("No docs found matching.");
            }
            // default
            return Future.succeededFuture(docs.result().get(0).toString());
        });
    }
    */
    // find doc
    public Future<JsonObject> find(String email, String password) {
        JsonObject query = new JsonObject()
                .put("email", email)
                .put("password", password);
        Future<JsonObject> future = Future.future();
        this.client.find(COLLECTION, query, res -> {
            if (res.succeeded()) {
                if(res.result().size()==0) {
                    future.fail("No docs found matching.");
                }
                future.complete(res.result().get(0)); //JsonObject
            } else {
                res.cause().printStackTrace();
                future.fail("find query failed.");
            }
        });
        return future;
    }

    // performs mongo upsert operation
    public Future<JsonObject> upsert(String email, String password){
        JsonObject query = new JsonObject()
                .put("email", email)
                .put("password", password);
        JsonObject update = new JsonObject().put("$set",
                new JsonObject().put("email", email)
                .put("password", password));
        Future<JsonObject> future = Future.future();
        UpdateOptions options = new UpdateOptions().setMulti(false).setUpsert(true);

        this.client.updateWithOptions(COLLECTION, query, update, options, res -> {
            if (res.succeeded()) {
                future.complete(update);
            } else {
                res.cause().printStackTrace();
                future.fail("Upsert failed.");
            }

        });
        return future;
    }
}
