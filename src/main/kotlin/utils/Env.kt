package com.oliver.utils

import io.ktor.server.application.ApplicationEnvironment

data object ENV {
    lateinit var ELEVENLABS_KEY: String
        private set

    fun init(environment: ApplicationEnvironment) {
        ELEVENLABS_KEY = environment.config.propertyOrNull("ktor.elevenlabs.key")?.getString()
            ?: System.getenv("ELEVENLABS_KEY")
            ?: throw IllegalStateException("ELEVENLABS_KEY를 설정 해주세요")
    }
}