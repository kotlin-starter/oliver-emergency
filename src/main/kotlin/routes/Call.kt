package com.oliver.routes

import com.oliver.models.respondApi
import com.oliver.service.ElevenLabsService
import com.oliver.service.VonageService
import com.oliver.utils.ENV
import com.oliver.utils.formatPhoneNumberToE164
import com.oliver.utils.makeEmergencyScript
import com.oliver.utils.parsedDate
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.request.queryString
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.MissingFieldException
import org.slf4j.LoggerFactory

@Serializable
data class CallRequest(
    val phoneNumber: String,
    val location: String,
    val detectionTime: String
)

@Serializable
data class CallResponse(
    val callUuid: String?,
    val audioUrl: String
)

private val elevenLabsService = ElevenLabsService()
private val logger = LoggerFactory.getLogger("CallRoutes")

@OptIn(ExperimentalSerializationApi::class)
fun Route.callRoutes() {
    get("/voice/ncco") {
        val audioUrl = call.request.queryParameters["audioUrl"]

        logger.info("NCCO 요청 수신 - audioUrl: $audioUrl, 전체 쿼리: ${call.request.queryString()}")

        if (audioUrl.isNullOrBlank()) {
            logger.warn("NCCO 요청에 audioUrl 파라미터가 없습니다")
            call.respondText(
                text = """{"error": "audioUrl 파라미터가 필요합니다."}""",
                contentType = ContentType.Application.Json,
                status = HttpStatusCode.BadRequest
            )
            return@get
        }

        val ncco = """
            [
                {
                    "action": "stream",
                    "streamUrl": ["$audioUrl"],
                    "loop": 1,
                    "level": 1.0
                }
            ]
        """.trimIndent()

        logger.info("NCCO 응답 생성: $ncco")

        call.respondText(
            text = ncco,
            contentType = ContentType.Application.Json
        )
    }

    get("/voice/fallback-ncco") {
        val audioUrl = call.request.queryParameters["audioUrl"]

        logger.info("Fallback NCCO 요청 수신 - audioUrl: $audioUrl, 전체 쿼리: ${call.request.queryString()}")

        if (audioUrl.isNullOrBlank()) {
            logger.warn("Fallback NCCO 요청에 audioUrl 파라미터가 없습니다")
            call.respondText(
                text = """{"error": "audioUrl 파라미터가 필요합니다."}""",
                contentType = ContentType.Application.Json,
                status = HttpStatusCode.BadRequest
            )
            return@get
        }

        val ncco = """
            [
                {
                    "action": "stream",
                    "streamUrl": ["$audioUrl"],
                    "loop": 1,
                    "level": 1.0
                }
            ]
        """.trimIndent()

        logger.info("Fallback NCCO 응답 생성: $ncco")

        call.respondText(
            text = ncco,
            contentType = ContentType.Application.Json
        )
    }
    
    post("/voice/event") {
        val request = call.receiveText()
        logger.info(">>> Vonage 이벤트 수신: $request")
        call.respondApi<String>(
            success = true,
            message = "이벤트 수신 완료",
            data = request
        )
    }
    
    post("/call") {
        try {
            val request = call.receive<CallRequest>()
            
            if (request.phoneNumber.isBlank() || request.location.isBlank() || request.detectionTime.isBlank()) {
                call.respondApi<Unit>(
                    success = false,
                    message = "전화번호, 위치, 감지 시간을 입력해주세요."
                )
                return@post
            }

            val script = makeEmergencyScript(request.location, parsedDate(request.detectionTime))
            val audioFileName = elevenLabsService.ttsToFile(script)
            
            if (audioFileName == null) {
                call.respondApi<Unit>(
                    success = false,
                    message = "TTS 생성에 실패했습니다."
                )
                return@post
            }

            // 생성된 오디오 파일 확인
            val audioFile = java.io.File("audio", audioFileName)
            if (!audioFile.exists()) {
                logger.error("오디오 파일이 존재하지 않습니다: ${audioFile.absolutePath}")
                call.respondApi<Unit>(
                    success = false,
                    message = "오디오 파일 생성 확인에 실패했습니다."
                )
                return@post
            }

            val baseUrl = ENV.BASE_URL
            val audioFilePath = "/audio/$audioFileName"
            val audioUrl = "$baseUrl$audioFilePath"

            if (baseUrl.contains("localhost") || baseUrl.contains("127.0.0.1") || 
                baseUrl.startsWith("http://") || !baseUrl.startsWith("https://")) {
                logger.warn("⚠️ 경고: BASE_URL이 공개적으로 접근 가능하지 않을 수 있습니다. Vonage는 공개 HTTPS URL이 필요합니다. BASE_URL: $baseUrl")
            }

            logger.info("오디오 파일 URL 생성 - 파일명: $audioFileName, 파일 크기: ${audioFile.length()} bytes, BASE_URL: $baseUrl, 최종 오디오 URL: $audioUrl")

            val answerUrl = "$baseUrl/voice/ncco?audioUrl=${java.net.URLEncoder.encode(audioUrl, "UTF-8")}"
            val fallbackUrl = answerUrl.replace("/voice/ncco", "/voice/fallback-ncco")
            
            logger.info("URL 생성 완료 - Answer URL: $answerUrl, Fallback URL: $fallbackUrl")

            val formattedPhoneNumber = formatPhoneNumberToE164(request.phoneNumber)

            val callUuid = VonageService.call(formattedPhoneNumber, audioUrl, answerUrl, fallbackUrl)
            
            if (callUuid != null) {
                call.respondApi(
                    success = true,
                    message = "통화가 시작되었습니다.",
                    data = CallResponse(callUuid, audioUrl)
                )
            } else {
                call.respondApi<Unit>(
                    success = false,
                    message = "통화 시작에 실패했습니다."
                )
            }
        } catch (e: MissingFieldException) {
            call.respondApi<Unit>(
                success = false,
                message = "필수 필드가 누락되었습니다: ${e.message}. phoneNumber, location, detectionTime을 모두 포함해주세요."
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
                message = "통화 시작 중 오류가 발생했습니다: ${e.javaClass.simpleName} - ${e.message}"
            )
        }
    }
    
//    post("/generate-tts") {
//        try {
//            val request = call.receive<TTSRouteRequest>()
//
//            if (request.location.isBlank() || request.detectionTime.isBlank()) {
//                call.respondApi<Unit>(
//                    success = false,
//                    message = "필수 Field를 입력해주세요."
//                )
//                return@post
//            }
//
//            val audioData = elevenLabsService.ttsToFile(
//                text = makeEmergencyScript(request.location, parsedDate(request.detectionTime)),
//            )
//        } catch (e: MissingFieldException) {
//            call.respondApi<Unit>(
//                success = false,
//                message = "필수 필드가 누락되었습니다: ${e.message}. location, detectionTime을 모두 포함해주세요."
//            )
//        } catch (e: SerializationException) {
//            call.respondApi<Unit>(
//                success = false,
//                message = "요청 형식 오류: 올바른 JSON 형식으로 요청해주세요. 오류: ${e.message}"
//            )
//        } catch (e: Exception) {
//            e.printStackTrace()
//            call.respondApi<Unit>(
//                success = false,
//                message = "TTS 생성 중 오류가 발생했습니다: ${e.javaClass.simpleName} - ${e.message}"
//            )
//        }
//    }
}