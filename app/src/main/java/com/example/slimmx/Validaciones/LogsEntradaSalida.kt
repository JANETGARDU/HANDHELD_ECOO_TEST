package com.example.slimmx.Validaciones

import android.content.Context
import com.example.slimmx.GlobalUser
import com.example.slimmx.MensajesDialog
import com.example.slimmx.Pedir_datos_apis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object LogsEntradaSalida {

    fun logsPorModulo(
        context: Context,
        lifecycleScope: CoroutineScope,
        module: String,
        action: String
    ) {
        try {
            val headers = mapOf("Token" to GlobalUser.token.toString())

            val params = mapOf(
                "module" to module,
                "action" to action
            )

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/log/actions",
                        params = params,
                        dataClass = Any::class,
                        listaKey = "",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {

                            }
                        },
                        onError = { error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(context, "Error onError: $error")
                            }
                        }
                    )
                } catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(context, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        } catch (e: Exception) {
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(context, "Ocurrió un error: ${e.message}")
            }
        }
    }
}