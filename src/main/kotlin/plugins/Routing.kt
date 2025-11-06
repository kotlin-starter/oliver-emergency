package com.oliver.plugins

import com.oliver.routes.callRoutes
import com.oliver.routes.healthRoutes
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        
        staticFiles("/audio", File("audio")) {
            default("")
        }
        
        healthRoutes()
        callRoutes()
    }
}
