package org.echno.feature.organization.domain

/** A company (tenant) the user works for. Everything else in the app is scoped to one of these. */
data class Organization(val id: Long, val name: String, val address: String?, val isActive: Boolean)
