package com.example.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

private suspend fun fetchQuotes(url: String): String? {
        return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

private suspend fun fetchRandomQuote(): JSONObject? {
    return withContext(Dispatchers.IO) {
        val jsonString = fetchQuotes("https://dummyjson.com/quotes/random")
        if (jsonString != null) {
            try {
                JSONObject(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }
}

suspend fun getQuote(): String {
    val json = fetchRandomQuote()
    return json!!.getString("quote") + " - " + json.getString("author")
}