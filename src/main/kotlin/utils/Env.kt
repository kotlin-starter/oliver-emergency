package com.oliver.utils

import io.ktor.server.application.ApplicationEnvironment

data object ENV {
    lateinit var ELEVENLABS_KEY: String
    lateinit var VONAGE_KEY: String
    lateinit var VONAGE_SECRET: String
    lateinit var VONAGE_APP_ID: String

    private fun getEnv(environment: ApplicationEnvironment, path: String, envName: String): String {
        return environment.config.propertyOrNull(path)?.getString()
            ?: System.getenv(envName)
            ?: throw IllegalStateException("${envName}를 설정 해주세요")
    }

    fun init(environment: ApplicationEnvironment) {
        ELEVENLABS_KEY = getEnv(environment, "ktor.elevenlabs.key", "ELEVENLABS_KEY")
        VONAGE_KEY = getEnv(environment, "ktor.vonage.key", "VONAGE_KEY")
        VONAGE_SECRET = getEnv(environment, "ktor.vonage.secret", "VONAGE_SECRET")
        VONAGE_APP_ID = getEnv(environment, "ktor.vonage.appId", "VONAGE_APP_ID")
    }
}