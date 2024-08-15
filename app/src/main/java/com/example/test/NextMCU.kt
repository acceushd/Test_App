package com.example.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

private suspend fun fetchMovieString(url: String): String? {
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

private suspend fun getMovieObject(): JSONObject? {
    return withContext(Dispatchers.IO) {
        val jsonString = fetchMovieString("https://whenisthenextmcufilm.com/api")
        if (jsonString != null) {
            try {
                val json = JSONObject(jsonString)
                val movieObject = json.getJSONObject("following_production")
                movieObject
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }
}

suspend fun returnMovieName():String? {
return getMovieObject()?.getString("title")
}
suspend fun returnDays():String? {
    return getMovieObject()?.getString("days_until")
}