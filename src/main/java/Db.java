import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import io.vertx.core.*;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.UpdateOptions;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

public class Db {
    // could have just used MongoAuth
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
            this.upsert("foo@foo.com", "foo!"),
            this.upsert("bar@bar.com", "bar!"))
        .setHandler((res)->{
            System.out.println("Db test accounts created.");
        });

    }

    // find user by email
    public Future<JsonObject> find(String email) {
        JsonObject query = new JsonObject()
                .put("email", email);
        Future<JsonObject> future = Future.future();
        this.client.find(COLLECTION, query, res -> {
            if (res.succeeded()) {
                if(res.result().size()==0) {
                    future.fail("No docs found matching email.");
                    return;
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

        // generate salt
        SecureRandom srand = new SecureRandom();
        byte[] randomSalt = new byte[64];
        srand.nextBytes(randomSalt);
        String salt = Base64.encode(randomSalt);
        byte[] hashedpw = pwHash(password, randomSalt);
        String pw = Base64.encode(hashedpw);
        JsonObject query = new JsonObject()
                .put("email", email); // just find matching emails
        JsonObject update = new JsonObject().put("$set",
                new JsonObject().put("email", email)
                        .put("pwsalt", salt)
                        .put("hashedpassword", pw));
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

    // hash password
    // returns hashed password
    public static byte[] pwHash(String pw, byte[] salt){
        return Db.hashPassword(pw.toCharArray(), salt, 100000, 256);
    }
    private static byte[] hashPassword(
            final char[] password, final byte[] salt, final int iterations, final int keyLength)
    {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKey key = skf.generateSecret(spec);
            byte[] res = key.getEncoded();
            return res;

        } catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}
