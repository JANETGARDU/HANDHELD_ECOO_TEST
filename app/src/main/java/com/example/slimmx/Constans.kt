package com.example.slimmx

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL = "https://diler.com.mx"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url.toString()

            //val newUrl = originalUrl.replace("https://diler.com.mx", "https://diler.com.mx:${GlobalConfig.puerto}")

            val newUrl = originalUrl.replace("https://diler.com.mx", "https://diler.com.mx:9092")

            val newRequest = originalRequest.newBuilder()
                .url(newUrl)
                .build()

            chain.proceed(newRequest)
        }
        .addInterceptor(loggingInterceptor)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // URL base sin puerto
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}

object RetrofitPostInstance {
    private const val BASE_URL = "https://diler.com.mx"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url.toString()

            //val newUrl = originalUrl.replace("https://diler.com.mx", "https://diler.com.mx:${GlobalConfig.puerto}")
            val newUrl = originalUrl.replace("https://diler.com.mx", "https://diler.com.mx:9092")
            val newRequest = originalRequest.newBuilder()
                .url(newUrl)
                .build()

            chain.proceed(newRequest)
        }
        .addInterceptor(loggingInterceptor)
        .build()

    val api: ApiPostService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // URL base sin puerto
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiPostService::class.java)
    }
}


object RetrofitPostInstance_json {
    private const val BASE_URL = "https://diler.com.mx"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url.toString()

            //val newUrl = originalUrl.replace("https://diler.com.mx", "https://diler.com.mx:${GlobalConfig.puerto}")
            val newUrl = originalUrl.replace("https://diler.com.mx", "https://diler.com.mx:9092")
            val newRequest = originalRequest.newBuilder()
                .url(newUrl)
                .build()

            chain.proceed(newRequest)
        }
        .addInterceptor(loggingInterceptor)
        .build()

    val api: ApiPostService_json by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // URL base sin puerto
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiPostService_json::class.java)
    }
}

object RetrofitPostInstance_imagenes {
    private const val BASE_URL = "https://diler.com.mx"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url.toString()

            //val newUrl = originalUrl.replace("https://diler.com.mx", "https://diler.com.mx:${GlobalConfig.puerto}")
            val newUrl = originalUrl.replace("https://diler.com.mx", "https://diler.com.mx:9092")
            val newRequest = originalRequest.newBuilder()
                .url(newUrl)
                .build()

            chain.proceed(newRequest)
        }
        .addInterceptor(loggingInterceptor)
        .build()

    val api: ApiPostService_imagenes by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // URL base sin puerto
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiPostService_imagenes::class.java)
    }
}

object GlobalConfig {
    var puerto: String = "9092"
}