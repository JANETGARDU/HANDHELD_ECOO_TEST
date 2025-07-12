package com.example.slimmx.Picking.Envio

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListaFolioPickingEnvio
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Picking.Envio.Asignacion.Asignacion_Envio_picking
import com.example.slimmx.Picking.Envio.Picking.Picking_envio_list
import com.example.slimmx.Picking.Recolecta.Submenu_Picking_Recolecta
import com.example.slimmx.R
import com.example.slimmx.Submenus.Submenu_picking
import com.example.slimmx.Validaciones.LogsEntradaSalida
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Submenu_Picking_Envio : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_submenu_picking_envio)
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
        val btn_picking=findViewById<Button>(R.id.buttonPick_Sub);
        val btn_asignar=findViewById<Button>(R.id.buttonAsig_Sub);

        btn_picking.setOnClickListener {
           /* startActivity(Intent(this@Submenu_Picking_Envio, Picking_envio_list ::class.java));
            finish();*/
            FoliosPickEnvio();
        }

        btn_asignar.setOnClickListener {
            /*startActivity(Intent(this@Submenu_Picking_Envio, Asignacion_Envio_picking::class.java));
            finish();*/
            FoliosPickEnvio_Asignacion();
        }
    }

    private fun FoliosPickEnvio() {
        try {
            val params= mapOf(
                "prefijo" to ""
            );

            val headers = mapOf("Token" to GlobalUser.token.toString());

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/inventario/tareas/picking",
                        params=params,
                        dataClass = ListaFolioPickingEnvio::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.ID }.toTypedArray()
                                    val intent =
                                        Intent(this@Submenu_Picking_Envio, Picking_envio_list::class.java);
                                    intent.putExtra("folios", opciones)
                                    startActivity(intent);
                                    LogsEntradaSalida.logsPorModulo( this@Submenu_Picking_Envio, lifecycleScope, "PICKING/ENVIO/TAREA", "ENTRADA");
                                    finish()
                                } else {
                                    MensajesDialog.showMessage(
                                        this@Submenu_Picking_Envio,
                                        "No hay tareas para este usuario"
                                    )
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Submenu_Picking_Envio, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Submenu_Picking_Envio, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun FoliosPickEnvio_Asignacion() {
        try {
            val params= mapOf(
                "prefijo" to ""
            );

            val headers = mapOf("Token" to GlobalUser.token.toString());

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/inventario/tareas/picking/asig",
                        params=params,
                        dataClass = ListaFolioPickingEnvio::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.ID }.toTypedArray()
                                    val intent =
                                        Intent(this@Submenu_Picking_Envio, Asignacion_Envio_picking::class.java)
                                    intent.putExtra("folios", opciones)
                                    startActivity(intent)
                                    LogsEntradaSalida.logsPorModulo( this@Submenu_Picking_Envio, lifecycleScope, "PICKING/ENVIO/ASIGNACION", "ENTRADA");
                                    finish()
                                } else {
                                    MensajesDialog.showMessage(
                                        this@Submenu_Picking_Envio,
                                        "No hay tareas para este usuario"
                                    );
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Submenu_Picking_Envio, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Submenu_Picking_Envio, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_picking::class.java));
        //LogsEntradaSalida.logsPorModulo( this@Submenu_Picking_Envio, lifecycleScope, "PICKING/ENVIO", "SALIDA");
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