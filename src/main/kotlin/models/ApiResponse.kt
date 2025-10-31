package com.oliver.models

import io.ktor.server.application.ApplicationCall
import io.ktor.util.reflect.typeInfo
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
)

suspend inline fun <reified T> ApplicationCall.respondApi(
    success: Boolean,
    message: String,
    data: T? = null
) {
    respond(ApiResponse(success, message, data), typeInfo<ApiResponse<T>>())
}