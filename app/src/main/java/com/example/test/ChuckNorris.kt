package com.example.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

suspend fun getChuckNorrisQuote(): String? {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder().url("https://api.chucknorris.io/jokes/random").build()
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string().let {
                    JSONObject(it).getString("value")
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

