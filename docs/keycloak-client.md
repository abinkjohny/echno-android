# Keycloak client: `echno-android-client`

The app signs users in through the `echno-android-client` client in Keycloak's `echno-realm`. Staging
already has it. This page describes how to create it, for example on a local Keycloak started by the
backend. Field names follow the Keycloak 26 admin console.

## What kind of client

A **public OpenID Connect client** using the **authorization code flow with PKCE (S256)**, the
standard for native apps ([RFC 8252](https://datatracker.ietf.org/doc/html/rfc8252)). The app has no
client secret, because anything inside an APK can be extracted.

The app sends its access token as `Authorization: Bearer …`. The backend exchanges it for its own
token (an RPT) exactly as it does for the web app, so this client needs no audience mapper and no
authorization settings.

## 1. Create the client (Clients → Create client)

| Field | Value |
|---|---|
| Client type | `OpenID Connect` |
| Client ID | `echno-android-client` |
| Name | `Echno Android` |

## 2. Capability config

| Field | Value | Why |
|---|---|---|
| Client authentication | Off | A public client: an app can't keep a secret |
| Authorization | Off | Permissions live on the backend's client |
| Standard flow | **On** | The authorization code flow, used with PKCE |
| Direct access grants | Off | No password grant; the app never sees the password |
| Implicit flow | Off | Deprecated and unsafe for apps |
| Device authorization grant, CIBA | Off | |

## 3. Login settings

| Field | Value |
|---|---|
| Root URL, Home URL | *(empty)* |
| Valid redirect URIs | The one for your build, below |
| Valid post logout redirect URIs | `+` (the same as the redirect URIs) |
| Web origins | *(empty)*: apps don't make CORS requests |

Each build variant has its own application id and its own redirect, so Android never asks which
installed app should finish a sign-in:

| Keycloak | App build | Valid redirect URI |
|---|---|---|
| Local (`http://localhost:8180/realms/echno-realm` on your computer) | `dev` (`org.echno.dev`) | `org.echno.dev:/oauth2redirect` |
| Staging (`https://auth.echno.in/realms/echno-realm`) | `staging` (`org.echno.staging`) | `org.echno.staging:/oauth2redirect` |
| Production (not deployed yet) | `prod` (`org.echno`) | `org.echno:/oauth2redirect` |

The URI must match **exactly**: one slash after the colon, no trailing slash. Never use a `*`
wildcard on a public client.

`localhost` on a phone or emulator is the device itself, not your computer. The `dev` build reaches
a local Keycloak at `http://10.0.2.2:8180/realms/echno-realm`, the emulator's alias for your
computer. On a phone, point `echno.dev.authIssuer` in `local.properties` at your computer's LAN
address ([README](../README.md#local-overrides)).

## 4. Logout settings

| Field | Value |
|---|---|
| Front channel logout | Off: an app has no logout URL Keycloak can call |
| Backchannel logout URL | *(empty)* |

## 5. Advanced tab

| Field | Value |
|---|---|
| Proof Key for Code Exchange Code Challenge Method | **`S256`** (makes PKCE mandatory) |

Leave token lifespans and session limits inherited from the realm.

## 6. Client scopes

Keep the realm's default scopes. The app requests `openid profile email offline_access`; the
offline token can keep a phone signed in beyond the browser session, for as long as the realm's
offline session limits allow.

Then open **Client scopes → `echno-android-client-dedicated` → Add mapper → By configuration →
Group Membership**:

| Field | Value |
|---|---|
| Name, Token Claim Name | `groups` |
| Full group path | **On** (the backend reads paths like `/org-5/system-admin`) |
| Add to ID token, access token, userinfo, token introspection | On |
| Add to lightweight access token | Off |

**This mapper is required.** Without it the app can sign in, but its token, and the backend's token
made from it, carry no organizations, so every organization-scoped request is refused with 403.

## 7. Keep it across realm rebuilds

The backend recreates the realm's clients from its `init-keycloak.json.template` at startup, so a
client added only in the admin console is lost when the realm is rebuilt. To keep it, add it to that
template next to the web client:

```json
{
    "clientId": "echno-android-client",
    "name": "Echno Android",
    "publicClient": true,
    "protocol": "openid-connect",
    "redirectUris": [${KEYCLOAK_ANDROID_REDIRECT_URI}],
    "webOrigins": [],
    "standardFlowEnabled": true,
    "implicitFlowEnabled": false,
    "directAccessGrantsEnabled": false,
    "serviceAccountsEnabled": false,
    "frontchannelLogout": false,
    "attributes": {
      "pkce.code.challenge.method": "S256",
      "post.logout.redirect.uris": "+"
    },
    "protocolMappers": [
      {
        "name": "groups",
        "protocol": "openid-connect",
        "protocolMapper": "oidc-group-membership-mapper",
        "consentRequired": false,
        "config": {
          "full.path": "true",
          "id.token.claim": "true",
          "access.token.claim": "true",
          "claim.name": "groups",
          "userinfo.token.claim": "true"
        }
      }
    ]
}
```

with `KEYCLOAK_ANDROID_REDIRECT_URI` set per environment, for example
`"org.echno.dev:/oauth2redirect"` locally.

## Check that it works

1. Discovery answers: `<issuer>/.well-known/openid-configuration`.
2. Signing in from the app opens the realm's login page and comes back to the app, with no
   "Invalid parameter: redirect_uri" or "code_challenge_method" error.
3. The access token has `azp` = `echno-android-client` and a `groups` claim with the user's
   `/org-…` paths.
4. After signing in, the Profile screen loads.

## How the app uses it

`AUTH_CLIENT_ID` is set in `app/build.gradle.kts`. The redirect URI and AppAuth's
`appAuthRedirectScheme` come from each variant's application id, and the issuer from the flavor (or a
`local.properties` override; see the [README](../README.md#local-overrides)).
