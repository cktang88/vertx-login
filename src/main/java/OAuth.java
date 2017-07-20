import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.AccessToken;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions;
import io.vertx.ext.auth.oauth2.providers.GoogleAuth;

public class OAuth {

    public static String verify(String idTokenString, Vertx vertx) {

        OAuth2ClientOptions credentials = new OAuth2ClientOptions()
                .setClientID("<client-id>")
                .setClientSecret("<client-secret>")
                .setSite("https://api.oauth.com");


        // Initialize the OAuth2 Library
        /*http://vertx.io/docs/apidocs/io/vertx/ext/auth/oauth2/providers/GoogleAuth.html*/
        String clientId = "";
        String clientSecret = "";
        OAuth2Auth oauth2 = GoogleAuth.create(vertx, clientId, clientSecret);

        // Authorization oauth2 URI
        String authorization_uri = oauth2.authorizeURL(new JsonObject()
                .put("redirect_uri", "http://localhost:8080/callback")
                .put("scope", "<scope>")
                .put("state", "<state>"));

        // Redirect example using Vert.x
        response.putHeader("Location", authorization_uri)
                .setStatusCode(302)
                .end();

        JsonObject tokenConfig = new JsonObject()
                .put("code", "<code>")
                .put("redirect_uri", "http://localhost:3000/callback");

        // Callbacks
        // Save the access token
        oauth2.getToken(tokenConfig, res -> {
            if (res.failed()) {
                System.err.println("Access Token Error: " + res.cause().getMessage());
            } else {
                // Get the access token object
                // (the authorization code is given from the previous step).
                AccessToken token = res.result();
            }
        });
    }
}