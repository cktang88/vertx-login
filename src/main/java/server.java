import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import io.vertx.core.http.*;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

public class server extends AbstractVerticle {

    private Db db;
    private OAuth auth;

    @Override
    public void start(Future<Void> fut) {
        // initialize db
        db = new Db(this.vertx, config());
        // initialize Oauth
        auth = new OAuth(this.vertx, config());

        // setup routes & middleware
        Router router = Router.router(vertx);
        router.route("/*").handler(StaticHandler.create()); // static page handler
        router.route("/*").handler(CookieHandler.create()); // cookie handler, required for sessions
        SessionStore sstore = LocalSessionStore.create(vertx);
        router.route("/*").handler(SessionHandler.create(sstore)); // session handler

        router.post("/").handler(BodyHandler.create());
        router.post("/").handler(this::login);


        router.get("/home.html").handler(this::restrictAccess);
        router.post("/home.html").handler(BodyHandler.create());
        router.post("/home.html").handler(this::logout);

        // start server
        System.out.println("Server started.");
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    // handle logins
    private void login(RoutingContext routingContext){
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();
        Session session = routingContext.session();

        // only handle POST requests
        if(request == null || request.method() != HttpMethod.POST)
            return;

        String idtoken = request.getParam("idtoken");
        if(idtoken!=null && idtoken.length()>0){
            // Option #1: OAuth verification

            this.auth.process(idtoken, response);
            return;

            // OAuth requires an "Authorization" on HTTP header
            // response.putHeader(HttpHeaders.AUTHORIZATION, "<token>");
        }
        else {
            // Option #2: authentication for email/password via MongoDB
            String email_orig = request.getParam("email");
            String password_orig = request.getParam("password");
            if(email_orig==null || password_orig==null){
                response.end("Email/password cannot be null");
                return;
            }
            // clean inputs
            String email = email_orig.trim();
            String password = password_orig.trim();
            db.find(email).setHandler(res -> {
                if (res.succeeded()) {
                    // email found
                    if (res.result().getString("password").equals(password)) {
                        // login success
                        // OAuth requires an "Authorization" on HTTP header
                        // see https://stackoverflow.com/questions/11318038/http-authorization-header-in-html
                        // but if you send Authorization headers, any 3rd party scripts can read it...bad
                        // work-around: redirect to special url that doesn't include response params
                        session.put("User", res.result()); // add to session
                        response.sendFile("webroot/home.html").end();
                    } else {
                        // password did not match
                        response.end("Incorrect password for " + email);
                    }
                } else {
                    // email not found
                    response.end("No account found for " + email);
                }
            });
        }
    }

    // restrict access to webpage to only allow authenticated users
    private void restrictAccess(RoutingContext routingContext){
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();
        Session session = routingContext.session();
        if(session.get("User")){
            response.sendFile("webroot/home.html");
        }
        else{
            response.setStatusCode(401).end("Access denied."); // Unauthorized
        }
    }

    // logout
    private void logout(RoutingContext routingContext){
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();


        // only handle POST requests
        if(request.method() != HttpMethod.POST)
            return;

        // destroy session
        // NOTE: session destroyed when browser exists anyway
        routingContext.session().destroy();
        System.out.println("Session destroyed.");

        // redirect to home page
        response.sendFile("webroot/index.html").end();

        // TODO: handle logout
    }
}