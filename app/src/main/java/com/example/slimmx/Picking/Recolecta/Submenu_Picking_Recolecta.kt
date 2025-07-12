package com.example.slimmx.Picking.Recolecta

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListaPickingRecolecta
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Picking.Recolecta.Asignacion.Asignacion_Recolecta_picking
import com.example.slimmx.Picking.Recolecta.Picking.Picking_recolecta_list
import com.example.slimmx.R
import com.example.slimmx.Submenus.Submenu_picking
import com.example.slimmx.Validaciones.LogsEntradaSalida
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Submenu_Picking_Recolecta : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_submenu_picking_recolecta)
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
            /*startActivity(Intent(this@Submenu_Picking_Recolecta, Picking_recolecta_list ::class.java));
            finish();*/

            FoliosPickinRecolecta();
        }

        btn_asignar.setOnClickListener {
            /*startActivity(Intent(this@Submenu_Picking_Recolecta, Asignacion_Recolecta_picking::class.java));
            finish();*/
            FoliosAsignacionRecolecta();
        }
    }

    private fun FoliosPickinRecolecta() {
        try {
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/fulfillment/picking/works/paquetes",
                        params=emptyMap<String, String>(),
                        dataClass = ListaPickingRecolecta::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.WORK }.toTypedArray()
                                    val intent =
                                        Intent(this@Submenu_Picking_Recolecta, Picking_recolecta_list::class.java)
                                    intent.putExtra("folios", opciones)
                                    startActivity(intent)
                                    LogsEntradaSalida.logsPorModulo( this@Submenu_Picking_Recolecta, lifecycleScope, "PICKING/RECOLECTA/TAREA", "ENTRADA");
                                    finish()
                                } else {
                                    MensajesDialog.showMessage(
                                        this@Submenu_Picking_Recolecta,
                                        "No hay tareas para este usuario"
                                    )
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Submenu_Picking_Recolecta, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Submenu_Picking_Recolecta, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun FoliosAsignacionRecolecta() {
        try {
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/fulfillment/picking/works/paquetes/asignar",
                        params=emptyMap<String, String>(),
                        dataClass = ListaPickingRecolecta::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.WORK }.toTypedArray()
                                    val intent =
                                        Intent(this@Submenu_Picking_Recolecta, Asignacion_Recolecta_picking::class.java)
                                    intent.putExtra("folios", opciones)
                                    startActivity(intent)
                                    LogsEntradaSalida.logsPorModulo( this@Submenu_Picking_Recolecta, lifecycleScope, "PICKING/RECOLECTA/ASIGNACION", "ENTRADA");
                                    finish()
                                } else {
                                    MensajesDialog.showMessage(
                                        this@Submenu_Picking_Recolecta,
                                        "No hay tareas para este usuario"
                                    )
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Submenu_Picking_Recolecta, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Submenu_Picking_Recolecta, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_picking::class.java));
        //LogsEntradaSalida.logsPorModulo( this@Submenu_Picking_Recolecta, lifecycleScope, "PICKING/RECOLECTA", "SALIDA");
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