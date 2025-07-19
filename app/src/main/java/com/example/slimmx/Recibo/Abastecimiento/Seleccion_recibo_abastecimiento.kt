package com.example.slimmx.Recibo.Abastecimiento

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
import com.example.slimmx.Recibo.Abastecimiento.ControlCalidad.Revision.ControlCalidad_List
import com.example.slimmx.Recibo.Abastecimiento.ControlCalidad.Submenu_control_calidad
import com.example.slimmx.Recibo.Abastecimiento.Recibo.Recibo_list
import com.example.slimmx.Recibo.Abastecimiento.Entrada.Entrada_orden_compra
import com.example.slimmx.Submenus.Submenu_recibo
import com.example.slimmx.Validaciones.LogsEntradaSalida
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Seleccion_recibo_abastecimiento : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_seleccion_recibo_abastecimiento)
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

        val btn_recibo=findViewById<Button>(R.id.buttonRecibo);
        val btn_calidad=findViewById<Button>(R.id.buttonControl_calidad);
        var btn_entrada=findViewById<Button>(R.id.buttonEntrada_Menu_recibo);

        btn_recibo.setOnClickListener {
            GlobalUser.RECIBO="A";
            GlobalUser.DEVOLUCION=0;
            /*startActivity(Intent(this@Seleccion_recibo_abastecimiento, Recibo_list::class.java));
            finish();*/
            FoliosRecibo();
        }

        btn_calidad.setOnClickListener {
            startActivity(Intent(this@Seleccion_recibo_abastecimiento, Submenu_control_calidad::class.java));
            LogsEntradaSalida.logsPorModulo( this@Seleccion_recibo_abastecimiento, lifecycleScope, "ABASTECIMIENTO/CALIDAD", "ENTRADA");
            finish();
            /*FoliosReciboCalidad();*/

        }

        btn_entrada.setOnClickListener {
            /* startActivity(Intent(this@Submenu_recibo,Entrada_orden_compra::class.java));
             finish();*/
            FoliosEntrada()
        }

    }


    private fun FoliosRecibo() {
        try {
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/proveedores/compras/recibo/folios/check",
                        params=emptyMap<String, String>(),
                        dataClass = ListReciboAbastecimiento::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.FOLIO   }.toTypedArray()
                                    val intent =
                                        Intent(this@Seleccion_recibo_abastecimiento, Recibo_list::class.java)
                                    intent.putExtra("folios", opciones)
                                    startActivity(intent)
                                    LogsEntradaSalida.logsPorModulo( this@Seleccion_recibo_abastecimiento, lifecycleScope, "ABASTECIMIENTO/RECIBO", "ENTRADA");
                                    finish()
                                } else {
                                    MensajesDialog.showMessage(this@Seleccion_recibo_abastecimiento, "No hay folios disponibles");
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Seleccion_recibo_abastecimiento, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Seleccion_recibo_abastecimiento, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_recibo::class.java));
        LogsEntradaSalida.logsPorModulo( this@Seleccion_recibo_abastecimiento, lifecycleScope, "ABASTECIMIENTO", "SALIDA");
        finish();
    }

    private fun FoliosEntrada() {
        try {
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/proveedores/compras/recibo",
                        params=emptyMap<String, String>(),
                        dataClass = ListReciboAbastecimiento::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.FOLIO }.toTypedArray()
                                    val intent =
                                        Intent(this@Seleccion_recibo_abastecimiento, Entrada_orden_compra::class.java)
                                    intent.putExtra("folios", opciones)
                                    startActivity(intent)
                                    LogsEntradaSalida.logsPorModulo( this@Seleccion_recibo_abastecimiento, lifecycleScope, "ABASTECIMIENTO/ENTRADA", "ENTRADA");
                                    finish()
                                } else {
                                    MensajesDialog.showMessage(this@Seleccion_recibo_abastecimiento, "No hay folios disponibles");
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Seleccion_recibo_abastecimiento, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Seleccion_recibo_abastecimiento, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun FoliosReciboCalidad() {
        try {
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/compras/calidad/recibo/folios",
                        params=emptyMap<String, String>(),
                        dataClass = ListReciboAbastecimiento::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.FOLIO   }.toTypedArray()
                                    val intent =
                                        Intent(this@Seleccion_recibo_abastecimiento, ControlCalidad_List::class.java)
                                    intent.putExtra("folios", opciones)
                                    startActivity(intent);
                                    LogsEntradaSalida.logsPorModulo( this@Seleccion_recibo_abastecimiento, lifecycleScope, "ABASTECIMIENTO/CALIDAD/RECIBO", "ENTRADA");
                                    finish()
                                } else {
                                    MensajesDialog.showMessage(this@Seleccion_recibo_abastecimiento, "No hay folios disponibles");
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Seleccion_recibo_abastecimiento, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Seleccion_recibo_abastecimiento, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
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