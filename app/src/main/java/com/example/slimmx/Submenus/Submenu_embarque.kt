package com.example.slimmx.Submenus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.Embarque.Devolucion_Proveedor.Devolucion_proveedor
import com.example.slimmx.Embarque.Envio.Embarque_Envio
import com.example.slimmx.Embarque.Recolecta.Embarque_Paquetes
import com.example.slimmx.Embarque.Recolecta.Embarque_Paquetes_confir
import com.example.slimmx.Embarque.Venta_interna.Embarque_venta_interna
import com.example.slimmx.GlobalConfig
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListaTareasReacomodo
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.R
import com.example.slimmx.Reacomodo.Tareas.Reacomodo_Check
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.listEccomerce
import com.example.slimmx.listFoliosDevEmbarque
import com.example.slimmx.listFoliosVentaInterna
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Submenu_embarque : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_submenu_embarque)
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
        val btn_envio=findViewById<Button>(R.id.buttonEnvio_Menu_embarque);
        val btn_recolecta=findViewById<Button>(R.id.buttonRecolecta_Menu_embarque);
        val btn_venta_interna=findViewById<Button>(R.id.buttonVenta_Menu_embarque);
        val btn_dev_proveedor=findViewById<Button>(R.id.buttonDevP_Menu_embarque);

        btn_envio.setOnClickListener {
            startActivity(Intent(this@Submenu_embarque, Embarque_Envio::class.java));
            //LogsEntradaSalida.logsPorModulo( this@Submenu_embarque, lifecycleScope, "EMBARQUE/ENVIO", "ENTRADA")
            finish();
        }

        btn_recolecta.setOnClickListener {
            /*startActivity(Intent(this@Submenu_embarque, Embarque_Paquetes::class.java));
            finish();*/
            //verificarEccomerce();
            startActivity(Intent(this@Submenu_embarque, Embarque_Paquetes_confir::class.java));
            finish();
        }

        btn_venta_interna.setOnClickListener {
            startActivity(Intent(this@Submenu_embarque, Embarque_venta_interna::class.java));
            //LogsEntradaSalida.logsPorModulo( this@Submenu_embarque, lifecycleScope, "EMBARQUE/VENTA/INTERNA", "ENTRADA")
            finish();
        }

        btn_dev_proveedor.setOnClickListener {
            //startActivity(Intent(this@Submenu_embarque, Devolucion_proveedor::class.java));
            verificarFoliosDevolucion();
        }

    }

    private fun verificarFoliosDevolucion() {
        try {
            var headers= mapOf(
                "Token" to GlobalUser.token.toString()
            )
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/obtener/folios/devoluciones/embarque",
                        params = emptyMap(),
                        dataClass = listFoliosDevEmbarque::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.FOLIO }.toTypedArray()
                                    val intent =
                                        Intent(this@Submenu_embarque, Devolucion_proveedor::class.java)
                                    intent.putExtra("folios", opciones)
                                    startActivity(intent)
                                    //LogsEntradaSalida.logsPorModulo( this@Submenu_embarque, lifecycleScope, "EMBARQUE/DEVOLUCION/PROVEEDOR", "ENTRADA")
                                    finish()
                                } else {
                                    MensajesDialog.showMessage(
                                        this@Submenu_embarque,
                                        "No hay folios disponibles"
                                    );
                                }
                            }
                        }, onError = { error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Submenu_embarque, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        // Captura de excepciones y mostrar un mensaje de error
                        MensajesDialog.showMessage(this@Submenu_embarque, "Ocurrió un error: ${e.message}")
                    }
                }

            }
        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Submenu_embarque, "Ocurrió un error: ${e.message}");
            }
        }
    }

    private fun verificarEccomerce() {
        try {
            var headers= mapOf(
                "Token" to GlobalUser.token.toString()
            )
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/configuracion/ecommerce/list",
                        params = emptyMap(),
                        dataClass = listEccomerce::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.CANAL_ECCOMERCE}.distinct().toTypedArray()
                                    val intent =
                                        Intent(this@Submenu_embarque, Embarque_Paquetes::class.java)
                                    intent.putExtra("folios", opciones)
                                    startActivity(intent)
                                    //LogsEntradaSalida.logsPorModulo( this@Submenu_embarque, lifecycleScope, "EMBARQUE/RECOLECTA", "ENTRADA");
                                    finish()
                                } else {
                                    MensajesDialog.showMessage(
                                        this@Submenu_embarque,
                                        "No hay folios disponibles"
                                    );
                                }
                            }
                        }, onError = { error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Submenu_embarque, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        // Captura de excepciones y mostrar un mensaje de error
                        MensajesDialog.showMessage(this@Submenu_embarque, "Ocurrió un error: ${e.message}")
                    }
                }

            }
        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Submenu_embarque, "Ocurrió un error: ${e.message}");
            }
        }
    }

    override fun onBackPressed() {
        LogsEntradaSalida.logsPorModulo( this@Submenu_embarque, lifecycleScope, "EMBARQUE", "SALIDA")
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