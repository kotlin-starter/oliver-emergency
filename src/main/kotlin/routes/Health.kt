package com.oliver.routes

import com.oliver.models.respondApi
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import java.time.LocalDateTime

fun Route.healthRoutes() {
    get("/health") {
        val now = LocalDateTime.now()
        call.respondApi<Unit>(true, "I'm healthy now! And the server time is $now")
    }
}