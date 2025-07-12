package com.example.slimmx

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KClass

private val service: ApiService by lazy { RetrofitInstance.api }
private val service2: ApiPostService by lazy { RetrofitPostInstance.api }
private val service3: ApiPostService_json by lazy { RetrofitPostInstance_json.api }
private val service4: ApiPostService_imagenes by lazy { RetrofitPostInstance_imagenes.api }

suspend fun <T : Any> Pedir_datos_apis(
    endpoint: String,
    params: Map<String, String>,
    listaKey: String,
    dataClass: KClass<T>,
    headers: Map<String, String> = emptyMap(),
    onSuccess: (List<T>) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val response = service.getDatos(endpoint, params, headers)

        if (response.isSuccessful) {
            val body = response.body()
            Log.d("API_RESPONSE", "Cuerpo completo: $body")

            if (body != null) {
                val jsonObject = Gson().toJsonTree(body).asJsonObject
                val status = jsonObject["status"]?.asInt ?: -1
                val message = jsonObject["message"]?.asString ?: "Sin mensaje"

                val dataArray = jsonObject[listaKey]?.asJsonArray

                if (dataArray != null && dataArray.size() > 0) {
                    val listaConvertida = dataArray.mapNotNull { jsonElement ->
                        try {
                            Gson().fromJson(jsonElement, dataClass.java)
                        } catch (e: Exception) {
                            Log.e("API_RESPONSE", "Error al convertir: ${e.message}")
                            null
                        }
                    }
                    onSuccess(listaConvertida)
                } else {
                    Log.d("API_RESPONSE", "Lista vacía en la API")
                    onSuccess(emptyList()) // Llamamos a onSuccess con lista vacía
                }
            } else {
                onError("El cuerpo de la respuesta está vacío.")
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val errorMessage = try {
                val errorJson = Gson().fromJson(errorBody, JsonObject::class.java)
                errorJson["message"]?.asString ?: "Error desconocido"
            } catch (e: Exception) {
                "Error desconocido al procesar la respuesta."
            }
            onError("Error en la petición: $errorMessage")
        }
    } catch (e: Exception) {
        when (e) {
            is SocketTimeoutException -> onError("Confirmación enviada. Respuesta no recibida.")
            else -> onError("Excepción al realizar la petición: ${e.localizedMessage}")
        }
    }
}

suspend fun <T : Any> Pedir_datos_apis_post(
    endpoint: String,
    body: Map<String, String>,
    listaKey: String,
    dataClass: KClass<T>,
    headers: Map<String, String> = emptyMap(), // Headers opcionales
    onSuccess: (T) -> Unit,
    onError: (String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = service2.getDatos_Post(endpoint, body, headers)

            if (response.isSuccessful) {
                val body = response.body()
                Log.d("API_RESPONSE", "Cuerpo completo: $body")

                if (body != null) {
                    val jsonObject = Gson().toJsonTree(body).asJsonObject
                    val status = jsonObject["status"]?.asInt ?: -1
                    val message = jsonObject["message"]?.asString ?: "Sin mensaje"  // Aquí manejamos el mensaje como String

                    val result = jsonObject[listaKey]

                    if (result != null) {
                        try {
                            // Si el resultado está disponible, deserialízalo
                            val parsedObject = Gson().fromJson(result, dataClass.java)
                            withContext(Dispatchers.Main) { onSuccess(parsedObject) }
                        } catch (e: Exception) {
                            Log.e("API_RESPONSE", "Error al deserializar: ${e.message}")
                            withContext(Dispatchers.Main) { onError("Error al procesar el resultado.") }
                        }
                    } else {
                        withContext(Dispatchers.Main) { onError("No se encontró la clave '$listaKey' en la respuesta.") }
                    }
                } else {
                    withContext(Dispatchers.Main) { onError("El cuerpo de la respuesta está vacío.") }
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    val errorJson = Gson().fromJson(errorBody, JsonObject::class.java)
                    errorJson["message"]?.asString ?: "Error desconocido"
                } catch (e: Exception) {
                    "Error desconocido al procesar la respuesta."
                }

                withContext(Dispatchers.Main) { onError("Error en la petición: $errorMessage") }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                when (e) {
                    is SocketTimeoutException -> onError("La respuesta tardó demasiado. Intenta nuevamente.")
                    else -> onError("Excepción al realizar la petición: ${e.localizedMessage}")
                }
            }
        }
    }
}



fun <T : Any> Pedir_datos_apis_post_json(
    endpoint: String,
    body: RequestBody,
    listaKey: String,
    dataClass: KClass<T>,
    headers: Map<String, String> = emptyMap(),
    onSuccess: (T) -> Unit,
    onError: (String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = service3.getDatos_Post_json(endpoint, body, headers)

            if (response.isSuccessful) {
                val responseBodyString = response.body()?.string()
                Log.d("API_RESPONSE", "Cuerpo completo: $responseBodyString")

                if (responseBodyString != null) {
                    val jsonObject = Gson().fromJson(responseBodyString, JsonObject::class.java)
                    val status = jsonObject["status"]?.asInt ?: -1
                    val message = jsonObject["message"]?.asString ?: "Sin mensaje"

                    val result = jsonObject[listaKey]
                    if (result != null) {
                        try {
                            val parsedObject = Gson().fromJson(result, dataClass.java)
                            withContext(Dispatchers.Main) { onSuccess(parsedObject) }
                        } catch (e: Exception) {
                            Log.e("API_RESPONSE", "Error al deserializar: ${e.message}")
                            withContext(Dispatchers.Main) { onError("Error al procesar el resultado.") }
                        }
                    } else {
                        withContext(Dispatchers.Main) { onError("No se encontró la clave '$listaKey' en la respuesta.") }
                    }
                } else {
                    withContext(Dispatchers.Main) { onError("El cuerpo de la respuesta está vacío.") }
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    val errorJson = Gson().fromJson(errorBody, JsonObject::class.java)
                    errorJson["message"]?.asString ?: "Error desconocido"
                } catch (e: Exception) {
                    "Error desconocido al procesar la respuesta."
                }

                withContext(Dispatchers.Main) { onError("Error en la petición: $errorMessage") }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                when (e) {
                    is SocketTimeoutException -> onError("La respuesta tardó demasiado. Intenta nuevamente.")
                    else -> onError("Excepción al realizar la petición: ${e.localizedMessage}")
                }
            }
        }
    }
}

/*fun <T : Any> Pedir_datos_apis_post_imagenes(
    endpoint: String,
    imageParts: List<MultipartBody.Part>, // Soporte para múltiples imágenes
    listaKey: String,
    dataClass: KClass<T>,
    headers: Map<String, String> = emptyMap(),
    onSuccess: (T) -> Unit,
    onError: (String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = service4.getDatos_Post_imagenes(endpoint, imageParts, headers)

            if (response.isSuccessful) {
                val responseBodyString = response.body()?.string()
                Log.d("API_RESPONSE", "Cuerpo completo: $responseBodyString")

                if (responseBodyString != null) {
                    val jsonObject = Gson().fromJson(responseBodyString, JsonObject::class.java)
                    val status = jsonObject["status"]?.asInt ?: -1
                    val message = jsonObject["message"]?.asString ?: "Sin mensaje"

                    val result = jsonObject[listaKey]
                    if (result != null) {
                        try {
                            val parsedObject = Gson().fromJson(result, dataClass.java)
                            withContext(Dispatchers.Main) { onSuccess(parsedObject) }
                        } catch (e: Exception) {
                            Log.e("API_RESPONSE", "Error al deserializar: ${e.message}")
                            withContext(Dispatchers.Main) { onError("Error al procesar el resultado.") }
                        }
                    } else {
                        withContext(Dispatchers.Main) { onError("No se encontró la clave '$listaKey' en la respuesta.") }
                    }
                } else {
                    withContext(Dispatchers.Main) { onError("El cuerpo de la respuesta está vacío.") }
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    val errorJson = Gson().fromJson(errorBody, JsonObject::class.java)
                    errorJson["message"]?.asString ?: "Error desconocido"
                } catch (e: Exception) {
                    "Error desconocido al procesar la respuesta."
                }

                withContext(Dispatchers.Main) { onError("Error en la petición: $errorMessage") }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                when (e) {
                    is SocketTimeoutException -> onError("La respuesta tardó demasiado. Intenta nuevamente.")
                    else -> onError("Excepción al realizar la petición: ${e.localizedMessage}")
                }
            }
        }
    }
}*/