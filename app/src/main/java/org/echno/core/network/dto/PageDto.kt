package org.echno.core.network.dto

import kotlinx.serialization.Serializable

/**
 * A page of results in Spring Data's `Page` JSON shape (`content`, `totalElements`, ...).
 * Spring's `pageable` and `sort` objects are ignored. [number] is zero-based.
 */
@Serializable
data class PageDto<T>(
    val content: List<T> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val number: Int = 0,
    val size: Int = 0,
    val first: Boolean = true,
    val last: Boolean = true
)
