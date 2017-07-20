import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class server extends AbstractVerticle {

    private HttpServer httpServer;
    private Db db;
    private OAuth auth;

    @Override
    public void start(Future<Void> fut) {
        // initialize db
        db = new Db(this.vertx, config());
        // initialize Oauth
        auth = new OAuth(this.vertx, config());

        // setup routes
        Router router = Router.router(vertx);
        router.route("/*").handler(StaticHandler.create());
        router.route(HttpMethod.POST, "/").handler(BodyHandler.create());
        router.post("/").handler(this::login);

        // start server
        System.out.println("Server started.");
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    // handle logins
    private void login(RoutingContext routingContext){
        HttpServerRequest request = routingContext.request();

        if(request.method() == HttpMethod.POST){
            String email = request.getParam("email");
            String password = request.getParam("password");
            //here i want to do: send to the html page some data
            //like "hi"

            // authenticate here
        }
    }
}