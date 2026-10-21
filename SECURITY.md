# Security policy

## Reporting a vulnerability

Please **don't open a public issue** for a security problem. Report it privately through GitHub:
open the repository's **Security** tab and choose **Report a vulnerability**. Include what is
affected, how to reproduce it, and what an attacker could do with it.

We aim to answer within a week. Once a fix is released, we'll credit you in the advisory unless
you prefer otherwise.

Problems in the server or in Keycloak's configuration belong to the
[backend repository](https://github.com/tornotron/echno-backend), reported the same private way.

## Supported versions

The app is in early development and has no releases yet. Fixes go into `development`.

## What the app does to protect users

* Signs in with Keycloak's authorization code flow and PKCE, in the system browser. The app never
  sees the user's password and has no client secret.
* Stores tokens encrypted with AES-256-GCM, using a key kept in the Android Keystore, and excludes
  them from backups and device transfers.
* Uses HTTPS only, except in the `dev` build, which may use plain HTTP to the local hosts it is
  configured for.
* Logs HTTP traffic only in debug builds, as headers without bodies, with `Authorization` and cookies
  redacted. Release builds log nothing.
