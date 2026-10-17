package org.echno.core.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.echno.BuildConfig

/** The DataStore that holds the session, as opposed to any other preferences file added later. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SessionPreferences

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Provides
    @Singleton
    fun provideAuthEndpoints(): AuthEndpoints = AuthEndpoints.forIssuer(BuildConfig.AUTH_ISSUER)

    @Provides
    @Singleton
    @SessionPreferences
    fun provideSessionDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(produceFile = { context.preferencesDataStoreFile("session") })

    @Provides
    @Singleton
    fun provideTokenCipher(): TokenCipher = KeystoreTokenCipher()

    @Provides
    @Singleton
    fun provideSessionStore(store: EncryptedSessionStore): SessionStore = store

    @Provides
    fun provideClock(): Clock = Clock.systemUTC()

    // A plain client, without the API client's token interceptor and authenticator, so asking
    // Keycloak for a token can never trigger another token request.
    @Provides
    @Singleton
    fun provideTokenService(endpoints: AuthEndpoints, json: Json, clock: Clock): TokenService = KeycloakTokenService(
        client = OkHttpClient(),
        endpoints = endpoints,
        clientId = BuildConfig.AUTH_CLIENT_ID,
        json = json,
        clock = clock
    )
}
