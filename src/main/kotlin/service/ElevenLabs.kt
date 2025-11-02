package com.oliver.service

import com.oliver.utils.ENV
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

@Serializable
data class TTSRequest(
    val text: String,
    val model_id: String = "eleven_multilingual_v2",
    val output_format: String = "mp3_44100_128",
    val voice_settings: VoiceSettings = VoiceSettings()
)

@Serializable
data class VoiceSettings(
    val stability: Double = 1.0,
    val similarity_boost: Double = 0.75
)

class ElevenLabsService {
    private val logger = LoggerFactory.getLogger(ElevenLabsService::class.java)
    private val baseUrl = "https://api.elevenlabs.io/v1"
    
    suspend fun ttsToFile(
        text: String,
    ) {
        val voiceId = "KlstlYt9VVf3zgie2Oht"
        val url = "$baseUrl/text-to-speech/$voiceId"
        
        logger.info("TTS 요청 시작 - voiceId: $voiceId, text 내용: ${text}")
        
        return try {
            val response = client.post(url) {
                headers {
                    append("xi-api-key", ENV.ELEVENLABS_KEY)
                }
                contentType(ContentType.Application.Json)
                setBody(
                    TTSRequest(text = text)
                )
            }

            val outputFileNAme = "tts_${System.currentTimeMillis()}.mp3"

            val dirFile = java.io.File("audio")
            if(!dirFile.exists()) {
                dirFile.mkdirs()
            }

            val outputFile = java.io.File(dirFile, outputFileNAme)
            outputFile.writeBytes(response.bodyAsBytes())
        } catch (e: Exception) {
            logger.error(e.message)
        }
    }
}