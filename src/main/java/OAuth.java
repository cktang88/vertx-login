// vertx
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.AccessToken;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.providers.GoogleAuth;
// google oauth
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
// exceptions
import java.io.IOException;
import java.security.GeneralSecurityException;
// collections
import java.util.Collections;


/*
NOTE: NOT doing this: https://developers.google.com/identity/sign-in/web/server-side-flow
We're doing this: https://developers.google.com/identity/protocols/OAuth2WebServer
 */




public class OAuth {
    private OAuth2Auth oauth2;
    private String authorization_uri;
    private GoogleIdTokenVerifier verifier;
    public OAuth(Vertx vertx, JsonObject config){
        // Initialize the OAuth2 Library
        /* http://vertx.io/docs/apidocs/io/vertx/ext/auth/oauth2/providers/GoogleAuth.html */
        String clientId = config.getString("oauth2_clientId");
        String clientSecret = config.getString("oauth2_clientSecret");
        this.oauth2 = GoogleAuth.create(vertx, clientId, clientSecret);

        // Authorization oauth2 URI
        this.authorization_uri = oauth2.authorizeURL(new JsonObject()
                .put("redirect_uri", "webroot/home.html")
                // scopes here: https://developers.google.com/identity/protocols/googlescopes#oauth2v2
                .put("scope", "https://www.googleapis.com/auth/plus.login")
                .put("state", "<state>"));

        // if application has only a single instance of GoogleIdTokenVerifier, use this builder
        verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList(clientId))
                // Or, if multiple clients access the backend:
                //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                .build();

        System.out.println("OAuth2 initialized.");
    }

    // process token string and send HTTP response
    public void process(String idTokenString, HttpServerResponse response) {

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

        // creating CSRF token???
        // https://developers.google.com/identity/protocols/OpenIDConnect#createxsrftoken
    }

    // verify OAuth token is legitimate using Google API
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


        A simple way to verify a token is to simply GET
        https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=XYZ123
        but this introduces network travel time, and is slower than verifying it here on server
        */

        GoogleIdToken idToken = null;
        try {
            idToken = verifier.verify(idTokenString);
        }
        catch(IOException | GeneralSecurityException err){
            System.out.println(err);
            return;
        }
        if (idToken != null) {
            Payload payload = idToken.getPayload();
            try {
                System.out.println(payload.toPrettyString());
            }
            catch(IOException err){
                System.out.println(err);
            }

            /* Example payload:

            {
             // These six fields are included in all Google ID Tokens.
             "iss": "https://accounts.google.com",
             "sub": "110169484474386276334",
             "azp": "1008719970978-hb24n2dstb40o45d4feuo2ukqmcc6381.apps.googleusercontent.com",
             "aud": "1008719970978-hb24n2dstb40o45d4feuo2ukqmcc6381.apps.googleusercontent.com",
             "iat": "1433978353",
             "exp": "1433981953",

             // These seven fields are only included when the user has granted the "profile" and
             // "email" OAuth scopes to the application.
             "email": "testuser@gmail.com",
             "email_verified": "true",
             "name" : "Test User",
             "picture": "https://lh4.googleusercontent.com/-kYgzyAWpZzJ/ABCDEFGHI/AAAJKLMNOP/tIXL9Ir44LE/s99-c/photo.jpg",
             "given_name": "Test",
             "family_name": "User",
             "locale": "en"
            }
             */

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
            String hostedDomain = payload.getHostedDomain();
            if(hostedDomain.equals("vanderbilt.edu")){
                //okay
            }
            else{
                // respond can't authenticate from non-Vandy people. Use email/password instead.
            }

        } else {
            System.out.println("Invalid ID token.");
        }
    }
}