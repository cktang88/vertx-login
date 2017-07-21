import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.AccessToken;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions;
import io.vertx.ext.auth.oauth2.providers.GoogleAuth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

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

    public String process(String idTokenString, HttpServerResponse response) {

        // Redirect example using Vert.x
        response.putHeader("Location", this.authorization_uri)
                .setStatusCode(302)
                .end();

        JsonObject tokenConfig = new JsonObject()
                .put("code", "<code>")
                .put("redirect_uri", "/webroot/home.html");

        // Callbacks
        // Save the access token
        this.oauth2.getToken(tokenConfig, res -> {
            if (res.failed()) {
                System.err.println("Access Token Error: " + res.cause().getMessage());
            } else {

                // Get the access token object
                // (the authorization code is given from the previous step).
                AccessToken token = res.result();
            }
        });
        return "";
    }

    private void verify(String idTokenString){
        /*
        To validate token, check the following:
        1. The ID token is properly signed by Google. Use Google's public keys
        (available in JWK or PEM format) to verify the token's signature.
        2. The value of "aud" in the ID token is equal to one of your app's client IDs.
        This check is necessary to prevent ID tokens issued to a malicious app being used to
        access data about the same user on your app's backend server.
        3. The value of "iss" in the ID token is equal to accounts.google.com
        or https://accounts.google.com.
        4. The expiry time ("exp") of the ID token has not passed.
        5. If you want to restrict access to only members of your G Suite domain,
        verify that the ID token has an hd claim that matches your G Suite domain name.

        src: https://developers.google.com/identity/sign-in/web/backend-auth
        NOTE: this is all conveniently handled using GoogleIdTokenVerifier
        */

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(CLIENT_ID))
                // Or, if multiple clients access the backend:
                //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                .build();

// (Receive idTokenString by HTTPS POST)

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            Payload payload = idToken.getPayload();

            // Print user identifier
            String userId = payload.getSubject();
            System.out.println("User ID: " + userId);

            // Get profile information from payload
            String email = payload.getEmail();
            boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String locale = (String) payload.get("locale");
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");

            // Use or store profile information
            // ...

            /* allow only @vanderbilt.edu emails
               The hd (hosted domain) parameter streamlines the login process for G Suite hosted
               accounts. By including the domain of the G Suite user (for example, mycollege.edu)
             */

            // payload.getHostedDomain()

        } else {
            System.out.println("Invalid ID token.");
        }
    }
}