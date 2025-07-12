package com.example.slimmx

import android.content.Context
import androidx.appcompat.app.AlertDialog

class MensajesDialog {
    companion object {
        // Método para mostrar el mensaje
        fun showMessage(context: Context, message: String) {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(message)
                .setPositiveButton("ACEPTAR") { dialog, _ ->
                    dialog.dismiss()
                }
            val dialog = builder.create()
            dialog.show()
        }
    }

}

class MensajesDialogConfirmaciones {
    companion object {
        // Método para mostrar el mensaje con un callback
        fun showMessage(context: Context, message: String, onDismiss: () -> Unit) {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(message)
                .setPositiveButton("ACEPTAR") { dialog, _ ->
                    dialog.dismiss()
                    onDismiss()
                }

            val dialog = builder.create()
            dialog.setCancelable(false) // Evita que se cierre al tocar fuera
            dialog.setCanceledOnTouchOutside(false) // También evita que se cierre al tocar fuera
            dialog.show()
        }
    }
}