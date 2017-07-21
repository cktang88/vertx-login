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

        router.route(HttpMethod.POST, "/").handler(BodyHandler.create());
        router.post("/").handler(this::logout);

        // start server
        System.out.println("Server started.");
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    // handle logins
    private void login(RoutingContext routingContext){
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();

        // only handle POST requests
        if(request.method() != HttpMethod.POST)
            return;

        // authentication for email/password
        String email = request.getParam("email").trim();
        String password = request.getParam("password").trim();
        db.find(email).setHandler(res-> {
            if(res.succeeded()){
                // email found
                if(res.result().getString("password").equals(password)){
                    // login success
                    response.sendFile("webroot/home.html").end();
                }
                else{
                    // password did not match
                    response.end("Incorrect password for " + email);
                }
            }
            else{
                // email not found
                response.end("No account found for " + email);
            }
        });
    }

    // logout
    private void logout(RoutingContext routingContext){
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();

        // only handle POST requests
        if(request.method() != HttpMethod.POST)
            return;

        // redirect to home page
        response.sendFile("webroot/index.html").end();

        // TODO: handle logout
    }
}