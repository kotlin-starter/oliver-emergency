package com.oliver.service

import com.vonage.client.VonageClient

import com.oliver.utils.ENV

object VonageService {
    private val API_KEY = ENV.VONAGE_KEY
    private val API_SECRET = ENV.VONAGE_SECRET
    private val APPLICATION_ID = ENV.VONAGE_APP_ID

    val client: VonageClient = VonageClient.builder()
        .apiKey(API_KEY)
        .apiSecret(API_SECRET)
        .applicationId(APPLICATION_ID)
        .privateKeyPath("/.private.key")
        .build()
}