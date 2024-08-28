package com.example.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

suspend fun getApi(num: Int): String? {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = okhttp3.Request.Builder()
            .url("https://api.isevenapi.xyz/api/iseven/$num")
            .build()
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

suspend fun getEvenOrOdd(num: Int): String? {
    return withContext(Dispatchers.IO) {
        val jsonString = getApi(num)
        if (jsonString != null) {
            val jsonObject = org.json.JSONObject(jsonString)
            val isEven = jsonObject.getBoolean("iseven")
            val ad = jsonObject.getString("ad")
            if (isEven) {
                "even\n$ad"
            } else {
                "odd\n$ad"
            }
        } else {
            "not work"
        }
    }
}
