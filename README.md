# vertx-login
[Vert.x](http://vertx.io/) web server back-end with 2-path login (@vanderbilt.edu OAuth2 token login, or email/password). User data is encrypted securely with PBKDF2, then stored in MongoDB.

## Dev

Put your db config vars in `conf.json`:
```
{
    "db_connectionString" : "<db url>",
    "oauth2_clientSecret" : "<client secret>",
    "oauth2_clientId": "<client id>"
}
```

**Build**

`mvn clean compile`

**Package into jar**

`mvn clean package`

OR

(for Windows): `run.bat`

**Run**

[Run vertx in an ide](https://stackoverflow.com/questions/24277301/run-vertx-in-an-ide)


**Also:**
Setting a redirect URI in the console is not a way of telling Google where to go when a login attempt comes in, but rather it's a way of telling Google what the allowed redirect URIs are (so if someone else writes a web app with your client ID but a different redirect URI it will be disallowed); your web app should, when someone clicks the "login" button, send the browser to:

**Potential improvements**

1. Sign user out (auto sign out when browser exits) + User sessions
2. Registration page.
