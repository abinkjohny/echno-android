package org.echno.core.session

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The organization the user is currently working in, sent to the backend as `X-Organization-Id`.
 *
 * `null` means none chosen; the backend then infers it when the user belongs to exactly one.
 * Held in memory for now; Phase 3 persists it and sets it after login (org picker).
 */
@Singleton
class SelectedOrganization @Inject constructor() {
    private val _id = MutableStateFlow<Long?>(null)

    /** Observable so screens can react when the user switches organization. */
    val id: StateFlow<Long?> = _id.asStateFlow()

    fun select(organizationId: Long?) {
        _id.value = organizationId
    }
}
