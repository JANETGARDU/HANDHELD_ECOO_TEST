package com.example.slimmx.Inventario.Muestreo

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.Empaque.Envio.Packing.SeleccionList_Envio
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListaFolioPickingEnvio
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Picking.Cedis.ConsumoInterno.Consumo_Interno_PI
import com.example.slimmx.Picking.Cedis.VentaInterna.Venta_Interna_List
import com.example.slimmx.R
import com.example.slimmx.ResultadoJsonSlimFolios_Packing
import com.example.slimmx.Submenus.Submenu_inventarios
import com.example.slimmx.Validaciones.LogsEntradaSalida
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Seleccion_muestreo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_seleccion_muestreo)
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

        val opcionesDefault = listOf("Mercado Libre", "Amazon", "Shein", "Punto de Venta", "Inventario por ubicación")

        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.cbSelectTipoMuestreo)

        val adaptador = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opcionesDefault)
        autoCompleteTextView.setAdapter(adaptador)

        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            try {
                when (opcionesDefault[position]) {
                    "Mercado Libre" -> {
                       /* val intent = Intent(this, Muestreo_list::class.java);
                        intent.putExtra("Prefijo", "EML");
                        startActivity(intent);*/
                        folios_lista_muestros();
                    }
                    "Amazon" -> {
                       /* val intent = Intent(this, Muestreo_list::class.java);
                        intent.putExtra("Prefijo", "EAMZ");
                        startActivity(intent);*/
                        folios_lista_muestros();
                    }
                    "Shein" -> {
                        /*val intent = Intent(this, Muestreo_list::class.java);
                        intent.putExtra("Prefijo", "MO");
                        startActivity(intent);*/
                        folios_lista_muestros();
                    }
                    "Punto de Venta" -> {
                        /*val intent = Intent(this, Muestreo_list::class.java);
                        intent.putExtra("Prefijo", "EPV");
                        startActivity(intent);*/
                        folios_lista_muestros();
                    }
                    "Inventario por ubicación" -> {
                        val intent = Intent(this, Inventario_ubicacion::class.java)
                        LogsEntradaSalida.logsPorModulo(this@Seleccion_muestreo, lifecycleScope, "INVENTARIOS/MUESTREO/INVENTARIO/UBICACION", "ENTRADA")
                        startActivity(intent)
                    }
                }
            }catch (e: Exception){
                MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
            }

        }

    }

    private fun folios_lista_muestros(){
        try {
            val headers= mapOf(
                "Token" to GlobalUser.token.toString()
            )

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/inventario/tareas/muestreo",
                        params= emptyMap<String,String>(),
                        dataClass = ListaFolioPickingEnvio::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.ID }.toTypedArray()
                                    val intent =
                                        Intent(this@Seleccion_muestreo, Muestreo_list::class.java)
                                    intent.putExtra("folios", opciones)
                                    startActivity(intent)
                                    LogsEntradaSalida.logsPorModulo(this@Seleccion_muestreo, lifecycleScope, "INVENTARIOS/MUESTREO/TAREA", "ENTRADA")
                                    finish();
                                } else {
                                    MensajesDialog.showMessage(this@Seleccion_muestreo, "No hay folios disponibles")
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Seleccion_muestreo, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Seleccion_muestreo, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_inventarios::class.java));
        //LogsEntradaSalida.logsPorModulo(this, lifecycleScope, "INVENTARIOS/MUESTREO", "SALIDA")
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