package org.echno.core.network

import kotlinx.serialization.json.Json

/**
 * JSON settings shared by every API call.
 *
 * - `ignoreUnknownKeys`: the backend may add fields at any time; older app versions must not crash.
 * - `coerceInputValues`: a `null` sent for a non-null field with a default falls back to that default.
 * - `explicitNulls = false`: request bodies omit null fields instead of sending `"field": null`.
 */
val NetworkJson: Json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
}
