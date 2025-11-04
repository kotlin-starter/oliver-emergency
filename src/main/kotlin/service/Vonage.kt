package com.oliver.service

import com.oliver.utils.ENV
import com.vonage.client.VonageClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.slf4j.LoggerFactory

object VonageService {
    private val logger = LoggerFactory.getLogger(VonageService::class.java)
    private val API_KEY = ENV.VONAGE_KEY
    private val API_SECRET = ENV.VONAGE_SECRET
    private val APPLICATION_ID = ENV.VONAGE_APP_ID
    private val PHONE_NUMBER = ENV.PHONE_NUMBER

    private val vonageClient: VonageClient = VonageClient.builder()
        .apiKey(API_KEY)
        .apiSecret(API_SECRET)
        .applicationId(APPLICATION_ID)
        .privateKeyPath(".private.key")
        .build()

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun call(toNumber: String, audioUrl: String, answerUrl: String, fallbackUrl: String): String? {
        return try {
            val jwt = vonageClient.generateJwt()
            
            val requestBody = buildJsonObject {
                put("to", buildJsonArray {
                    add(buildJsonObject {
                        put("type", "phone")
                        put("number", toNumber)
                    })
                })
                put("from", buildJsonObject {
                    put("type", "phone")
                    put("number", PHONE_NUMBER)
                })
                put("answer_url", buildJsonArray {
                    add(JsonPrimitive(answerUrl))
                })
                put("fallback_answer_url", buildJsonArray {
                    add(JsonPrimitive(fallbackUrl))
                })
            }
            
            logger.info("통화 시작 시도 - 수신번호: $toNumber, 오디오 URL: $audioUrl, Answer URL: $answerUrl, Fallback URL: $fallbackUrl, Application ID: $APPLICATION_ID")
            
            val response = httpClient.post("https://api.nexmo.com/v1/calls") {
                headers {
                    append("Authorization", "Bearer $jwt")
                }
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            
            val responseText = response.bodyAsText()
            logger.info("Vonage API 응답: $responseText")
            
            if (response.status.value in 200..299) {
                val jsonResponse = Json.parseToJsonElement(responseText) as JsonObject
                val uuid = jsonResponse["uuid"]?.toString()?.replace("\"", "")
                logger.info("통화 생성 성공 - UUID: $uuid")
                uuid
            } else {
                logger.error("통화 생성 실패 - 상태 코드: ${response.status.value}, 응답: $responseText")
                null
            }
        } catch (e: Exception) {
            logger.error("통화 생성 실패: ${e.message}", e)
            null
        }
    }
}