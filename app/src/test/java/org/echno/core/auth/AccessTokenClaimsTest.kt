package org.echno.core.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class AccessTokenClaimsTest {
    @Test
    fun `membership groups give organization ids, role groups are skipped`() {
        val token = accessTokenWithGroups("/org-5", "/org-5/system-admin", "/org-12", "/staff", "/org-x")

        assertEquals(listOf(5L, 12L), AccessTokenClaims.organizationIds(token))
    }

    @Test
    fun `a token without groups or that isn't a JWT gives none`() {
        assertEquals(emptyList<Long>(), AccessTokenClaims.organizationIds(accessTokenWithGroups()))
        assertEquals(emptyList<Long>(), AccessTokenClaims.organizationIds("opaque-token"))
        assertEquals(emptyList<Long>(), AccessTokenClaims.organizationIds("a.%%%.c"))
    }
}
