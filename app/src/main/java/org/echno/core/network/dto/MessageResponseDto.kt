package org.echno.core.network.dto

import kotlinx.serialization.Serializable

/** Body of mutations that return only a confirmation (the backend's `ApiResponse { message }`). */
@Serializable
data class MessageResponseDto(val message: String? = null)
