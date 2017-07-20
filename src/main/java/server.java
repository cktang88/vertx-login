import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class server extends AbstractVerticle {

    private HttpServer httpServer;

    @Override
    public void start(Future<Void> fut) {
        // setup routes
        Router router = Router.router(vertx);
        router.route("/*").handler(StaticHandler.create());
        router.route(HttpMethod.POST, "/").handler(BodyHandler.create());
        router.post("/").handler(this::login);

        // start server
        System.out.println("Server started.");
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }
    private void login(RoutingContext routingContext){
        System.out.println("incoming request!");
        HttpServerRequest request = routingContext.request();

        if(request.method() == HttpMethod.POST){
            System.out.println(request.getParam("email"));
            System.out.println(request.getParam("password"));
            //here i want to do: send to the html page some data
            //like "hi"

            // authenticate here
        }
    }
}