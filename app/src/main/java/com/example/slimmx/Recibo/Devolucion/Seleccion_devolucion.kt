package com.example.slimmx.Recibo.Devolucion

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListReciboAbastecimiento
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.R
import com.example.slimmx.Recibo.Abastecimiento.Entrada.Entrada_orden_compra
import com.example.slimmx.Recibo.Abastecimiento.Recibo.Recibo_list
import com.example.slimmx.Recibo.Devolucion.Calidad.Submenu_alta_calidad_devolucion
import com.example.slimmx.Recibo.Devolucion.Entrada.Entrada_Devoluciones
import com.example.slimmx.Recibo.Devolucion.Match.Devolucion
import com.example.slimmx.Recibo.Devolucion.Recibo.Recibo_devolucion
import com.example.slimmx.Validaciones.LogsEntradaSalida
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Seleccion_devolucion : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_seleccion_devolucion)
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
        val buttonMatch=findViewById<Button>(R.id.buttonMatch);
        val buttonEntrada=findViewById<Button>(R.id.buttonEntrada);
        val buttonRecibo=findViewById<Button>(R.id.buttonRecibo);
        val buttonCalidad=findViewById<Button>(R.id.buttonCalidad);

        buttonMatch.setOnClickListener {
            startActivity(Intent(this@Seleccion_devolucion, Devolucion::class.java));
            //LogsEntradaSalida.logsPorModulo( this, lifecycleScope, "DEVOLUCION/MATCH", "ENTRADA");
            finish();
        }

        buttonEntrada.setOnClickListener {
            FoliosEntrada_Devolucion();
        }

        buttonRecibo.setOnClickListener {
            FoliosRecibo_Devolucion();
        }

        buttonCalidad.setOnClickListener {
            startActivity(Intent(this@Seleccion_devolucion, Submenu_alta_calidad_devolucion::class.java));
            //LogsEntradaSalida.logsPorModulo( this@Seleccion_devolucion, lifecycleScope, "DEVOLUCION/CALIDAD", "ENTRADA");
            finish();
        }

    }

    private fun FoliosEntrada_Devolucion() {
        try {
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/devoluciones/recibo/folios/check",
                        params=emptyMap<String, String>(),
                        dataClass = ListReciboAbastecimiento::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.FOLIO }.toTypedArray()
                                    val intent =
                                        Intent(this@Seleccion_devolucion, Entrada_Devoluciones::class.java)
                                    intent.putExtra("folios", opciones)
                                    startActivity(intent)
                                    //LogsEntradaSalida.logsPorModulo( this@Seleccion_devolucion, lifecycleScope, "DEVOLUCION/ENTRADA", "ENTRADA");
                                    finish()
                                } else {
                                    MensajesDialog.showMessage(this@Seleccion_devolucion, "No hay folios disponibles");
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Seleccion_devolucion, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Seleccion_devolucion, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun FoliosRecibo_Devolucion() {
        try {
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/devoluciones/recibo",
                        params=emptyMap<String, String>(),
                        dataClass = ListReciboAbastecimiento::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.FOLIO   }.toTypedArray()
                                    val intent =
                                        Intent(this@Seleccion_devolucion, Recibo_devolucion::class.java)
                                    intent.putExtra("folios", opciones)
                                    startActivity(intent)
                                    //LogsEntradaSalida.logsPorModulo( this@Seleccion_devolucion, lifecycleScope, "DEVOLUCION/RECIBO", "ENTRADA");
                                    finish()
                                } else {
                                    MensajesDialog.showMessage(this@Seleccion_devolucion, "No hay folios disponibles");
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Seleccion_devolucion, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Seleccion_devolucion, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    override fun onBackPressed() {
        LogsEntradaSalida.logsPorModulo( this, lifecycleScope, "DEVOLUCION", "SALIDA");
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