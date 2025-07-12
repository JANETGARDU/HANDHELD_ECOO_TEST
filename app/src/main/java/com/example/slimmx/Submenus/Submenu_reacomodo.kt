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
import com.example.slimmx.ListaTareasReacomodo_buscarfolios
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.R
import com.example.slimmx.Reacomodo.Manual.Reacomodo_manual
import com.example.slimmx.Reacomodo.Tareas.Reacomodo_Check
import com.example.slimmx.Validaciones.LogsEntradaSalida
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Submenu_reacomodo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_submenu_reacomodo)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val message = intent.getStringExtra("MESSAGE")
        if (!message.isNullOrEmpty()) {
            MensajesDialog.showMessage(this, message)
        }

        if (GlobalUser.nombre.isNullOrEmpty()){
            MensajesDialogConfirmaciones.showMessage(this, "Ocurrio un error se cerrara la aplicacion, lamento el inconveniente"){
                finishAffinity();
            }
        }

        val btn_manual=findViewById<Button>(R.id.buttonReacomodo_Manual);
        val btn_tareas=findViewById<Button>(R.id.buttonReacomodo_Tareas);

        btn_manual.setOnClickListener {
            startActivity(Intent(this@Submenu_reacomodo, Reacomodo_manual::class.java));
            //LogsEntradaSalida.logsPorModulo(this, lifecycleScope, "ACOMODO/REACOMODO/MANUAL", "ENTRADA")
            finish();
        }

        btn_tareas.setOnClickListener {
            //startActivity(Intent(this@Submenu_reacomodo, Reacomodo_Check::class.java));
            //finish();
            verificarFoliosReacomodo();
        }

    }

    private fun verificarFoliosReacomodo() {
        try {
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/inventario/tareas/reubicacion/check/list",
                        params=emptyMap<String, String>(),
                        dataClass = ListaTareasReacomodo_buscarfolios::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.ID }.toTypedArray()
                                    val intent =
                                        Intent(this@Submenu_reacomodo, Reacomodo_Check::class.java)
                                    intent.putExtra("folios", opciones)
                                    startActivity(intent)
                                    //LogsEntradaSalida.logsPorModulo(this@Submenu_reacomodo, lifecycleScope, "ACOMODO/REACOMODO/TAREA", "ENTRADA")
                                    finish()
                                } else {
                                    MensajesDialog.showMessage(
                                        this@Submenu_reacomodo,
                                        "No hay tareas para este usuario"
                                    )
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Submenu_reacomodo, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Submenu_reacomodo, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }


    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_acomodo::class.java));
        LogsEntradaSalida.logsPorModulo(this, lifecycleScope, "ACOMODO/REACOMODO", "SALIDA")
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