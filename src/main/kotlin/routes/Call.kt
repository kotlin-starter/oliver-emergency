package com.oliver.routes

import com.oliver.models.respondApi
import com.oliver.service.ElevenLabsService
import com.oliver.utils.makeEmergencyScript
import com.oliver.utils.parsedDate
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

@Serializable
data class TTSRouteRequest(
    val location: String,
    val detectionTime: String,
    val voiceId: String? = null,
    val saveLocation: String? = null
)

private val elevenLabsService = ElevenLabsService()

fun Route.callRoutes() {
    post("/call") {
        try {
            val request = call.receive<TTSRouteRequest>()
            
            if (request.location.isBlank() || request.detectionTime.isBlank()) {
                call.respondApi<Unit>(
                    success = false,
                    message = "필수 Field를 입력해주세요."
                )
                return@post
            }
            
            val audioData = elevenLabsService.ttsToFile(
                text = makeEmergencyScript(request.location, parsedDate(request.detectionTime)),
            )
        } catch (e: SerializationException) {
            call.respondApi<Unit>(
                success = false,
                message = "요청 형식 오류: 올바른 JSON 형식으로 요청해주세요. 오류: ${e.message}"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            call.respondApi<Unit>(
                success = false,
                message = "TTS 생성 중 오류가 발생했습니다: ${e.javaClass.simpleName} - ${e.message}"
            )
        }
    }
}