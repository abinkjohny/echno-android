package org.echno.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Each destination is a type. Arguments become constructor properties
// (e.g. `data class ProjectDetail(val id: Long) : NavKey`), so they are checked at compile time.
// @Serializable lets the back stack survive process death and rotation.

@Serializable
data object Splash : NavKey

@Serializable
data object Home : NavKey
