package com.example.slimmx.Submenus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.Acomodo.Acomodo_list
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListaFolioPickingEnvio
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.R
import com.example.slimmx.Validaciones.LogsEntradaSalida
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Submenu_acomodo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_submenu_acomodo)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (GlobalUser.nombre.isNullOrEmpty()){
            MensajesDialogConfirmaciones.showMessage(this, "Ocurrio un error se cerrara la aplicacion, lamento el inconveniente"){
                finishAffinity();
            }
        }

        val message = intent.getStringExtra("MESSAGE")
        if (!message.isNullOrEmpty()) {
            MensajesDialog.showMessage(this, message)
        }

        var btn_acomodo=findViewById<Button>(R.id.buttonAcomodo_Sub);
        var btn_reacomodo=findViewById<Button>(R.id.buttonReacomodo_Sub);

       /* btn_acomodo.setOnClickListener {
            startActivity(Intent(this@Submenu_acomodo, Acomodo_list::class.java));
            finish();
        }*/

        btn_reacomodo.setOnClickListener {
            startActivity(Intent(this@Submenu_acomodo, Submenu_reacomodo::class.java));
            //LogsEntradaSalida.logsPorModulo(this, lifecycleScope, "ACOMODO/REACOMODO", "ENTRADA")
            finish();
        }

        btn_acomodo.setOnClickListener {
            verificarFoliosYAbrirActivity()
        }


    }

    private fun verificarFoliosYAbrirActivity() {
        try {
            val headers = mapOf("Token" to GlobalUser.token.toString())

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/inventario/tareas/acomodo",
                        params = emptyMap(),
                        dataClass = ListaFolioPickingEnvio::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    // Convertimos la lista de IDs en un array y la enviamos al intent
                                    val opciones = lista.map { it.ID }.toTypedArray()
                                    val intent = Intent(this@Submenu_acomodo, Acomodo_list::class.java)
                                    intent.putExtra("folios", opciones)
                                    startActivity(intent)
                                    //LogsEntradaSalida.logsPorModulo(this@Submenu_acomodo, lifecycleScope, "ACOMODO/ACOMODO", "ENTRADA")
                                    finish()
                                } else {
                                    MensajesDialog.showMessage(
                                        this@Submenu_acomodo,
                                        "No hay tareas para este usuario"
                                    )
                                }
                            }
                        },
                        onError = { error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Submenu_acomodo, "Error: $error");
                            }
                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Submenu_acomodo, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        } catch (e: Exception) {
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Submenu_acomodo, "Ocurrió un error: ${e.message}");
            }
        }
    }

    override fun onBackPressed() {
        LogsEntradaSalida.logsPorModulo( this@Submenu_acomodo, lifecycleScope, "ACOMODO", "SALIDA")
        finish();
    }

    /*override fun onStop() {
        super.onStop()
        LogsEntradaSalida.logsPorModulo(
            this,
            lifecycleScope,
            "APLICACION",
            "SALIDA"
        )
    }*/

}