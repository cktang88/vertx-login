import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class server extends AbstractVerticle {

    private HttpServer httpServer;

    @Override
    public void start(Future<Void> fut) {
        httpServer = vertx.createHttpServer();

        Router router = Router.router(vertx);
        router.route("/*").handler(StaticHandler.create());



        httpServer.requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest request) {
                System.out.println("incoming request!");

                if(request.method() == HttpMethod.POST){
                    //here i want to do: send to the html page some data
                    //like "hi"

                    // authenticate here
                }
            }
        });
        httpServer.requestHandler(router::accept).listen(8080);
    }
}