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
        
        // 오디오 파일 정적 파일 서빙 (프로젝트 루트의 audio 폴더)
        staticFiles("/audio", File("audio")) {
            default("")
        }
        
        healthRoutes()
        callRoutes()
    }
}
