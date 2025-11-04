package com.example.postsapp.utils

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


class TimeoutInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            chain
                .withConnectTimeout(30, TimeUnit.SECONDS)
                .withReadTimeout(30, TimeUnit.SECONDS)
                .withWriteTimeout(30, TimeUnit.SECONDS)
                .proceed(chain.request())
        } catch (e: TimeoutException) {
            okhttp3.Response.Builder()
                .request(chain.request())
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(504)
                .message("Network timeout after 10 seconds")
                .body(
                    okhttp3.ResponseBody.create(
                        "application/json".toMediaTypeOrNull(),
                        """{"error":"timeout"}"""
                    )
                )
                .build()
        }
    }
}