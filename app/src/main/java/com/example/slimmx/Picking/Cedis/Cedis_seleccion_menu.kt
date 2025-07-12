package com.example.slimmx.Picking.Cedis

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalUser
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Picking.Cedis.ConsumoInterno.Consumo_Interno_PI
import com.example.slimmx.Picking.Cedis.VentaInterna.Venta_Interna_List
import com.example.slimmx.R
import com.example.slimmx.Submenus.Submenu_picking
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.listFoliosVentaInterna
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Cedis_seleccion_menu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cedis_seleccion_menu)
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

        val opcionesDefault = listOf("Venta Interna", "Consumo Interno"/*, "Venta"*/)

        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.cbSelectTipoSurtido_PI)

        val adaptador = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opcionesDefault)
        autoCompleteTextView.setAdapter(adaptador)

        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            try {
                when (opcionesDefault[position]) {
                    "Venta Interna" -> {
                        /*val intent = Intent(this, Venta_Interna_List::class.java)
                        startActivity(intent)*/
                        Log.d("AutoComplete", "Ejecutando verificarFoliosReacomodo()")
                        verificarFoliosVenta();
                    }
                    "Consumo Interno" -> {
                        val intent = Intent(this, Consumo_Interno_PI::class.java);
                        LogsEntradaSalida.logsPorModulo( this@Cedis_seleccion_menu, lifecycleScope, "PICKING/CEDIS/CONSUMO/INTERNO", "ENTRADA");
                        startActivity(intent)
                    }
                    /* "Venta" -> {
                         val intent = Intent(this, VentaActivity::class.java)
                         startActivity(intent)
                     }*/
                }
            }catch (e: Exception){
                MensajesDialog.showMessage(this@Cedis_seleccion_menu, "Ocurrió un error: ${e.message}");
            }

        }

        val message = intent.getStringExtra("MESSAGE")
        if (!message.isNullOrEmpty()) {
            MensajesDialog.showMessage(this, message)
        }

    }

    private fun verificarFoliosVenta() {
        try {
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/clientes/ventas/internas/list",
                        params =emptyMap<String, String>(),
                        dataClass = listFoliosVentaInterna::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.FOLIO.toString() }.toTypedArray();
                                    val intent = Intent(
                                        this@Cedis_seleccion_menu,
                                        Venta_Interna_List::class.java
                                    );
                                    intent.putExtra("folios", opciones);
                                    startActivity(intent);
                                    LogsEntradaSalida.logsPorModulo( this@Cedis_seleccion_menu, lifecycleScope, "PICKING/CEDIS/VENTA/INTERNA", "ENTRADA");
                                    finish();
                                } else {
                                    MensajesDialog.showMessage(
                                        this@Cedis_seleccion_menu,
                                        "No hay tareas para este usuario"
                                    );
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Cedis_seleccion_menu, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Cedis_seleccion_menu, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_picking::class.java));
        //LogsEntradaSalida.logsPorModulo( this, lifecycleScope, "PICKING/CEDIS", "SALIDA");
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