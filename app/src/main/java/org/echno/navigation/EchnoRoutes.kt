package org.echno.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Each destination is a type. Arguments become constructor properties
// (e.g. `data class ProjectDetail(val id: Long) : NavKey`), so they are checked at compile time.
// @Serializable lets the back stack survive process death and rotation.

@Serializable
data object Splash : NavKey

/**
 * The signed-out home. [registeredUsername] is set right after registering, to welcome the user
 * and pre-fill the login page.
 */
@Serializable
data class Welcome(val registeredUsername: String? = null) : NavKey

/** The registration form, opened from Welcome. */
@Serializable
data object Register : NavKey

/** Signed in, but no organization chosen yet. */
@Serializable
data object OrganizationPicker : NavKey

/** The first screen once signed in and working in an organization. A placeholder until the signed-in features arrive. */
@Serializable
data object Home : NavKey
