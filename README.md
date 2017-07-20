# vertx-login

2-path login (@vanderbilt.edu OAuth login, or email/password) on Vert.x backend

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

(batchfile for Windows): `run`

**Run**

[Run vertx in an ide](https://stackoverflow.com/questions/24277301/run-vertx-in-an-ide)


**Also:**
Setting a redirect URI in the console is not a way of telling Google where to go when a login attempt comes in, but rather it's a way of telling Google what the allowed redirect URIs are (so if someone else writes a web app with your client ID but a different redirect URI it will be disallowed); your web app should, when someone clicks the "login" button, send the browser to: