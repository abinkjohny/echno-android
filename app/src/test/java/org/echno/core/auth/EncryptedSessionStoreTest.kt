package org.echno.core.auth

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.echno.core.network.NetworkJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

// A real DataStore writing to a temp folder; only the Keystore cipher is faked.
class EncryptedSessionStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val tokens = AuthTokens("access", "refresh", "id", accessTokenExpiresAt = 1_800_000_000)

    @Test
    fun `tokens and organization survive a round trip`() = runTest {
        val store = EncryptedSessionStore(dataStore(), FakeTokenCipher(), NetworkJson)

        store.saveTokens(tokens)
        store.saveOrganization(5)

        assertEquals(StoredSession(tokens, 5), store.read())
    }

    @Test
    fun `tokens are not written in the clear`() = runTest {
        EncryptedSessionStore(dataStore(), FakeTokenCipher(), NetworkJson).saveTokens(tokens)

        assertFalse(String(file.readBytes()).contains("access"))
    }

    @Test
    fun `tokens that can't be decrypted count as signed out`() = runTest {
        val dataStore = dataStore()
        EncryptedSessionStore(dataStore, FakeTokenCipher(), NetworkJson).saveTokens(tokens)

        val afterKeyLoss = EncryptedSessionStore(dataStore, FakeTokenCipher(failDecrypt = true), NetworkJson)

        assertNull(afterKeyLoss.read().tokens)
    }

    @Test
    fun `clear removes everything`() = runTest {
        val store = EncryptedSessionStore(dataStore(), FakeTokenCipher(), NetworkJson)
        store.saveTokens(tokens)
        store.saveOrganization(5)

        store.clear()

        assertEquals(StoredSession(null, null), store.read())
    }

    private val file by lazy { File(folder.root, "session.preferences_pb") }

    // backgroundScope is cancelled when the test ends, which closes the DataStore.
    private fun TestScope.dataStore() = PreferenceDataStoreFactory.create(scope = backgroundScope, produceFile = { file })
}

/** Reversible stand-in for Android Keystore: XORs each byte. */
class FakeTokenCipher(private val failDecrypt: Boolean = false) : TokenCipher {
    override fun encrypt(plain: ByteArray): ByteArray = plain.map { (it.toInt() xor 0x5A).toByte() }.toByteArray()

    override fun decrypt(data: ByteArray): ByteArray {
        if (failDecrypt) throw javax.crypto.AEADBadTagException("key changed")
        return encrypt(data)
    }
}
