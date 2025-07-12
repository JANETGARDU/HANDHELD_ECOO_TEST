package com.example.slimmx

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response
import retrofit2.http.*


interface ApiService {

    @GET("{endpoint}")
    suspend fun getDatos(
        @Path(value = "endpoint", encoded = true) endpoint: String,
        @QueryMap params: Map<String, String>,
        @HeaderMap headers: Map<String, String>
    ): Response<Map<String, Any>>

}

// Servicio para las solicitudes POST
interface ApiPostService {
    @POST("{endpoint}")
    suspend fun getDatos_Post(
        @Path(value = "endpoint", encoded = true) endpoint: String,
        @QueryMap params: Map<String, String>,
        @HeaderMap headers: Map<String, String>
    ): Response<Map<String, Any>>

}


interface ApiPostService_json {
    @POST
    suspend fun getDatos_Post_json(
        @Url url: String,
        @Body body: RequestBody,
        @HeaderMap headers: Map<String, String>
    ): Response<ResponseBody>
}

interface ApiPostService_imagenes {
    @Multipart
    @POST
    suspend fun getDatos_Post_imagenes(
        @Url url: String,
        @Part file: MultipartBody.Part,
        @HeaderMap headers: Map<String, String>
    ): Response<ResponseBody>
}

