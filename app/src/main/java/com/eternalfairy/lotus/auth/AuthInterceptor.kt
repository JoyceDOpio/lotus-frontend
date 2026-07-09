package com.eternalfairy.lotus.auth

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.markedForInjection()) {
            val token = tokenProvider.invoke()
            if (!token.isNullOrEmpty()) {
                val newRequest = request.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                return chain.proceed(newRequest)
            }
        }

        return chain.proceed(request)
    }

    /**
     * Check if request is annotated with `@InjectAuth` annotation,
     * If annotated, then it's marked for `Authorization` injection
     */
    private fun Request.markedForInjection(): Boolean = tag<Invocation>()?.method()
        ?.annotations?.toSet()?.find { it is InjectAuth } != null
}