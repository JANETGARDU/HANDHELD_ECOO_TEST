package com.example.slimmx.ImpresionEtiqueta

import android.content.Context
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.Socket


class EtiquetaPrinter {

    suspend fun imprimirEtiqueta(context: Context, ip: String, codigoZPL: String): Boolean {
        return withContext(Dispatchers.IO) {
            var socket: Socket? = null
            try {
                socket = Socket(ip, 9100)
                val outputStream: OutputStream = socket.getOutputStream()

                outputStream.write(codigoZPL.toByteArray())
                outputStream.flush()
                true // éxito
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(context)
                        .setMessage("Error al imprimir: ${e.message}")
                        .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                        .setCancelable(false)
                        .create()
                        .show()
                }
                false // hubo error
            } finally {
                socket?.close()
            }
        }
    }
}


