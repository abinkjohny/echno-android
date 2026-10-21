# Architecture

How the app is organized, how a screen gets its data, and how to add a feature. The Profile feature
(`feature/profile`) is the smallest complete example; read it alongside this page.

## The big picture

```text
Screen (Compose) ──observes──▶ ViewModel ──calls──▶ Repository ──calls──▶ Retrofit API ──HTTP──▶ backend
       ▲                          │                     │
       └──── UiState (StateFlow) ─┘      ApiResult<DomainModel> (never throws)
```

* **One activity.** `MainActivity` hosts all of Compose. Screens are composables, not activities or
  fragments.
* **Unidirectional data flow.** A ViewModel exposes one `StateFlow<…UiState>`; the screen draws it and
  sends user actions back as function calls (`retry()`, `submit()`).
* **Features don't know about each other.** Each one lives in its own package and depends only on
  `core`. Navigation connects them.
* **Dependency injection with Hilt.** Classes ask for what they need in their `@Inject constructor`,
  which makes it easy to swap in fakes in tests.

## Packages

```text
org.echno
├── EchnoApplication, MainActivity, MainViewModel   app entry point; MainViewModel exposes the session state
├── navigation/          EchnoRoutes (every route) and EchnoNavDisplay (the navigation host)
├── core/
│   ├── auth/            sign-in, tokens, the encrypted session store, SessionManager
│   ├── network/         Retrofit and OkHttp setup, interceptors, ApiResult, AppError, safeApiCall
│   ├── session/         SelectedOrganization, the organization the user works in
│   ├── coroutines/      the application-wide CoroutineScope
│   ├── designsystem/    theme (colors, type) and shared components (loading, empty, error states)
│   └── ui/              shared UI pieces: the account menu, an error message for each AppError
└── feature/<name>/
    ├── data/            <Name>Api (Retrofit), dto/ (wire format), <Name>Repository, <Name>DataModule (Hilt)
    ├── domain/          the models the UI works with
    └── ui/              <Name>Screen, <Name>ViewModel and the UiState
```

## Layers

### Data: API, DTOs and repository

* **`…Api`** is a Retrofit interface of `suspend` functions, one per endpoint.
* **DTOs** (`data/dto`) mirror the backend's JSON exactly, are `@Serializable`, and make nullable
  every field the backend may leave out. They never leave the data layer.
* **The repository** wraps every call in `safeApiCall { … }` and maps DTOs to domain models (a
  `toDomain()` extension). It returns `ApiResult<T>` (`Success(data)` or `Failure(AppError)`) and
  never throws, except for coroutine cancellation.
* Each feature has an interface (`ProfileRepository`) and an implementation
  (`DefaultProfileRepository`), bound in its `…DataModule`. Tests replace the interface with a fake.

### Domain models

Plain Kotlin data classes named after what they mean to the user (`Profile`, `Organization`), with
proper types: enums instead of strings, `LocalDate` instead of ISO text, `null` for "not given"
instead of blank strings.

### UI: ViewModel and screen

* The **ViewModel** (`@HiltViewModel`) holds a `MutableStateFlow` of a sealed `…UiState`
  (`Loading`, `Error(AppError)`, `Loaded(…)`), launches work in `viewModelScope`, and survives
  rotation.
* Each screen has two composables:
  * `ProfileScreen(viewModel = hiltViewModel())` collects the state and passes callbacks;
  * `ProfileContent(uiState, onRetry, …)` is **stateless**: it only draws what it's given. Previews
    and Compose UI tests use it directly, without Hilt or a network.
* Errors are shown with `AppError.messageRes()` (`core/ui/AppErrorMessage.kt`), so every screen
  words the same failure the same way. `LoadingState`, `EmptyState` and `ErrorState` come from the
  design system.

## Networking

`NetworkModule` builds one OkHttp client and one Retrofit instance for the whole app.

| Piece | What it does |
|---|---|
| `AuthInterceptor` | Adds `Authorization: Bearer <access token>`, refreshing it first when it's about to expire |
| `OrganizationInterceptor` | Adds `X-Organization-Id` from `SelectedOrganization`, unless the call sets it itself |
| `TokenAuthenticator` | On a 401, refreshes the token once and retries. If Keycloak refuses the refresh token, the session ends as expired; a network or server failure leaves it signed in |
| `safeApiCall` | Turns every result into `ApiResult`: HTTP status to `AppError`, using the backend's `ProblemDetail` body |

`AppError` covers what a screen needs to decide: `Network`, `Timeout`, `Unauthorized`, `Forbidden`,
`NotFound`, `Validation` (with per-field errors), `Conflict`, `RateLimited`, `Server` and `Unknown`.

## Sign-in and the session

* **Keycloak, authorization code flow with PKCE.** AppAuth opens Keycloak's login page in a Custom
  Tab and receives the redirect (`org.echno[.dev|.staging]:/oauth2redirect`). The code exchange,
  refresh and revoke are form posts over OkHttp (`KeycloakTokenService`), because AppAuth's own HTTP
  refuses the plain `http://` a local Keycloak uses. Client setup: [keycloak-client.md](keycloak-client.md).
* **Storage.** `SessionStore` keeps the tokens and the selected organization in DataStore, encrypted
  with AES-256-GCM by a key in the Android Keystore (`TokenCipher`). The file is excluded from backups.
  A session that can't be decrypted (the key is gone after a restore) counts as signed out.
* **`SessionManager`** is the single owner of the session. It exposes `SessionState`
  (`Loading`, `SignedOut`, `SignedIn(organizationId)`) as a `StateFlow`, refreshes one request at a
  time when several fail together, and serializes writes to the store so a late write from an old
  session can't overwrite a newer one.
* **Organizations.** The access token's `groups` claim lists the user's organizations
  (`/org-5/…`). The picker asks the backend for their details; a single active organization is chosen
  automatically.

## Navigation

Navigation 3 with a back stack the app owns (`EchnoNavDisplay`). Routes are `@Serializable` objects
or data classes in `EchnoRoutes.kt`.

The **session decides the root**, the screen at the bottom of the stack:

| Session | Root |
|---|---|
| Being read at launch | Splash |
| Signed out | Welcome |
| Signed in, no organization chosen | Organization picker |
| Signed in with an organization | Profile |

When the session changes (sign-in, sign-out, an expired session), the whole stack is replaced, so
Back can never return to a screen from the previous session. Other screens are pushed on top with
`backStack.add(route)`. Each entry gets its own ViewModels, cleared when it is popped.

## Build variants and configuration

The `environment` flavor dimension has `dev`, `staging` and `prod`, each with its own application id,
backend URL and Keycloak issuer, all exposed through `BuildConfig`. See the [README](../README.md#build-variants)
for the values and the `local.properties` overrides. Only `dev` may use plain HTTP, and its network
security config is generated from the configured hosts.

## Testing

| Layer | How | Example |
|---|---|---|
| Repository | MockWebServer serves recorded JSON; assert the domain result and the request sent | `DefaultProfileRepositoryTest` |
| ViewModel | A fake repository and `MainDispatcherRule`; assert the `UiState` sequence | `ProfileViewModelTest` |
| Screen | `createComposeRule()` on the stateless `…Content`; find nodes by text and click | `ProfileContentTest` |
| Auth and network core | Fakes for the token service and the store | `SessionManagerTest`, `TokenAuthenticatorTest` |

## Adding a feature

1. **Data:** add `feature/<name>/data/<Name>Api.kt`, the DTOs in `data/dto`, the repository interface
   and its `Default…` implementation using `safeApiCall`, and a `<Name>DataModule` that binds the
   repository and creates the API from `Retrofit`.
2. **Domain:** add the models the screen needs, and a `toDomain()` mapping in the data layer.
3. **UI:** add the `UiState`, a `@HiltViewModel`, and the `…Screen` and stateless `…Content`
   composables. Put strings in `res/values/strings.xml`.
4. **Navigation:** add a route to `EchnoRoutes.kt` and an `entry<…>` in `EchnoNavDisplay`, and push it
   from wherever the user starts it.
5. **Tests:** a repository test, a ViewModel test and a Compose UI test, as in the table above.
