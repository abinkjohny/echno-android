package org.echno.core.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.IOException
import java.util.Base64
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import timber.log.Timber

/** What survives an app restart: the tokens and the organization the user last worked in. */
data class StoredSession(val tokens: AuthTokens?, val organizationId: Long?)

/** Where the session is kept between launches. An interface so tests can use an in-memory fake. */
interface SessionStore {
    suspend fun read(): StoredSession

    suspend fun saveTokens(tokens: AuthTokens)

    suspend fun saveOrganization(organizationId: Long?)

    /** Signs out on this device: removes the tokens and the selected organization. */
    suspend fun clear()
}

/**
 * Persists the session in a DataStore file (`files/datastore/session.preferences_pb`).
 *
 * The tokens are JSON encrypted with [TokenCipher], so they are never on disk in the clear. The file
 * is excluded from backups (`backup_rules.xml`, `data_extraction_rules.xml`): the Keystore key doesn't travel with a backup, so a
 * restored copy could never be decrypted anyway.
 */
class EncryptedSessionStore @Inject constructor(
    @param:SessionPreferences private val dataStore: DataStore<Preferences>,
    private val cipher: TokenCipher,
    private val json: Json
) : SessionStore {
    override suspend fun read(): StoredSession {
        val preferences =
            dataStore.data
                .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
                .first()
        return StoredSession(
            tokens = preferences[TOKENS]?.let(::decryptTokens),
            organizationId = preferences[ORGANIZATION_ID]
        )
    }

    override suspend fun saveTokens(tokens: AuthTokens) {
        val sealed = cipher.encrypt(json.encodeToString(AuthTokens.serializer(), tokens).toByteArray())
        dataStore.edit { it[TOKENS] = Base64.getEncoder().encodeToString(sealed) }
    }

    override suspend fun saveOrganization(organizationId: Long?) {
        dataStore.edit { preferences ->
            if (organizationId == null) preferences.remove(ORGANIZATION_ID) else preferences[ORGANIZATION_ID] = organizationId
        }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    // A stored value that can't be read (the key was lost after a restore, or the file is damaged)
    // counts as signed out rather than crashing the app on launch.
    private fun decryptTokens(stored: String): AuthTokens? = try {
        val plain = cipher.decrypt(Base64.getDecoder().decode(stored))
        json.decodeFromString(AuthTokens.serializer(), String(plain))
    } catch (e: Exception) {
        Timber.w(e, "Stored session couldn't be read; treating it as signed out")
        null
    }

    private companion object {
        val TOKENS = stringPreferencesKey("tokens")
        val ORGANIZATION_ID = longPreferencesKey("organization_id")
    }
}
