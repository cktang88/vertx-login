import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.AccessToken;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions;
import io.vertx.ext.auth.oauth2.providers.GoogleAuth;

public class OAuth {
    private OAuth2Auth oauth2;
    private String authorization_uri;
    public OAuth(Vertx vertx, JsonObject config){
        // Initialize the OAuth2 Library
        /* http://vertx.io/docs/apidocs/io/vertx/ext/auth/oauth2/providers/GoogleAuth.html */
        String clientId = config.getString("oauth2_clientId");
        String clientSecret = config.getString("oauth2_clientSecret");
        this.oauth2 = GoogleAuth.create(vertx, clientId, clientSecret);

        // Authorization oauth2 URI
        this.authorization_uri = oauth2.authorizeURL(new JsonObject()
                .put("redirect_uri", "http://localhost:8080/callback")
                .put("scope", "<scope>")
                .put("state", "<state>"));

        System.out.println("OAuth2 initialized.");
    }

    public String verify(String idTokenString, HttpServerResponse response) {

        // Redirect example using Vert.x
        response.putHeader("Location", this.authorization_uri)
                .setStatusCode(302)
                .end();

        JsonObject tokenConfig = new JsonObject()
                .put("code", "<code>")
                .put("redirect_uri", "http://localhost:3000/callback");

        // Callbacks
        // Save the access token
        this.oauth2.getToken(tokenConfig, res -> {
            if (res.failed()) {
                System.err.println("Access Token Error: " + res.cause().getMessage());
            } else {

                /* check only @vanderbilt.edu emails
                The hd (hosted domain) parameter streamlines the login process for G Suite hosted
                   accounts. By including the domain of the G Suite user (for example, mycollege.edu)
                   */


                // Get the access token object
                // (the authorization code is given from the previous step).
                AccessToken token = res.result();
            }
        });
        return "";
    }
}