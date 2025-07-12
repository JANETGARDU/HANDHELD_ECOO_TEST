package com.example.slimmx.Empaque.Envio

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.Empaque.Envio.Asignacion.Empaque_Envio_Asignacion
import com.example.slimmx.Empaque.Envio.Packing.SeleccionList_Envio
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListaFolioPickingEnvio
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.R
import com.example.slimmx.ResultadoJsonSlimFolios_Packing
import com.example.slimmx.Submenus.Submenu_packing
import com.example.slimmx.Validaciones.LogsEntradaSalida
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Submenu_Empaque_Envio : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_submenu_empaque_envio)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (GlobalUser.nombre.isNullOrEmpty()) {
            MensajesDialogConfirmaciones.showMessage(
                this,
                "Ocurrio un error se cerrara la aplicacion, lamento el inconveniente"
            ) {
                finishAffinity()
            }
        }

        val btn_empaque=findViewById<Button>(R.id.buttonEmpa_Sub);
        val btn_asig=findViewById<Button>(R.id.buttonAsig_Sub);

        btn_empaque.setOnClickListener {
            folios_lista_envios();
        }

        btn_asig.setOnClickListener {
            folios_lista_envios_asignacion();
        }

    }

    private fun folios_lista_envios(){
        try {
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/inventario/empaque",
                        params=emptyMap<String, String>(),
                        dataClass = ResultadoJsonSlimFolios_Packing::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.ID }.toTypedArray()
                                    val intent =
                                            Intent(this@Submenu_Empaque_Envio, SeleccionList_Envio::class.java)
                                    intent.putExtra("folios", opciones)
                                    startActivity(intent)
                                    LogsEntradaSalida.logsPorModulo( this@Submenu_Empaque_Envio, lifecycleScope, "EMPAQUE/ENVIO/TAREA", "ENTRADA")
                                    finish();
                                } else {
                                    MensajesDialog.showMessage(this@Submenu_Empaque_Envio, "No hay folios disponibles")
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Submenu_Empaque_Envio, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Submenu_Empaque_Envio, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun folios_lista_envios_asignacion(){
        try {
            val params= mapOf(
                "prefijo" to ""
            );

            val headers = mapOf("Token" to GlobalUser.token.toString());

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/inventario/picking/folios/asig/empaque",
                        params=params,
                        dataClass = ListaFolioPickingEnvio::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.ID }.toTypedArray()
                                    val intent =
                                        Intent(this@Submenu_Empaque_Envio, Empaque_Envio_Asignacion::class.java)
                                    intent.putExtra("folios", opciones)
                                    startActivity(intent)
                                    LogsEntradaSalida.logsPorModulo( this@Submenu_Empaque_Envio, lifecycleScope, "EMPAQUE/ENVIO/ASIGNACION", "ENTRADA")
                                    finish();
                                } else {
                                    MensajesDialog.showMessage(this@Submenu_Empaque_Envio, "No hay folios disponibles")
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Submenu_Empaque_Envio, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Submenu_Empaque_Envio, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_packing::class.java));
        //LogsEntradaSalida.logsPorModulo( this@Submenu_Empaque_Envio, lifecycleScope, "EMPAQUE/ENVIO", "SALIDA");
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