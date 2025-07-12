package com.example.slimmx.Embarque.Recolecta

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TableLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.Acomodo.Acomodo_confirmacion
import com.example.slimmx.GlobalUser
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Picking.Cedis.ConsumoInterno.Consumo_Interno_PI
import com.example.slimmx.Picking.Cedis.VentaInterna.Venta_Interna_List
import com.example.slimmx.R
import com.example.slimmx.Submenus.Submenu_embarque
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.defectos
import com.example.slimmx.listaPaqueterias
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Embarque_Paquetes : AppCompatActivity() {

    private var opcionesDefault_Paqueteria = listOf("");
    private lateinit var cbSelectPaqueteria: AutoCompleteTextView;
    private lateinit var cbSelectEccomerce: AutoCompleteTextView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_embarque_paquetes)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        cbSelectEccomerce = findViewById(R.id.cbSelectEcomerce);
        cbSelectPaqueteria =findViewById(R.id.cbSelectPaqueteria_E);

        if (GlobalUser.nombre.isNullOrEmpty()) {
            MensajesDialogConfirmaciones.showMessage(
                this,
                "Ocurrio un error se cerrara la aplicacion, lamento el inconveniente"
            ) {
                finishAffinity()
            }
        }

        val opciones = intent.getStringArrayExtra("folios");

        if (opciones != null && opciones.isNotEmpty()) {
            actualizarcbBoxEccomerce(opciones.toList())
        } else {
            val intent = Intent(this, Submenu_embarque::class.java)
            intent.putExtra("MESSAGE", "Error al cargar los Eccomerce");
            startActivity(intent)
            finish();
        }
        obtenerPaqueteria();


        val btn_ok=findViewById<Button>(R.id.buttonOK);

        btn_ok.setOnClickListener {
            if (cbSelectEccomerce.text.toString().isNotEmpty() && cbSelectPaqueteria.text.toString().isNotEmpty()){
                val intent = Intent(this, Embarque_Paquetes_confir::class.java)

                // Envía los valores correctos
                intent.putExtra("ecommerce", cbSelectEccomerce.text.toString())
                intent.putExtra("paqueteria", cbSelectPaqueteria.text.toString())
                startActivity(intent)
            }else{
                MensajesDialog.showMessage(this,"Debes de seleccionar los dos datos")
            }

        }

    }


    private fun actualizarcbBoxEccomerce(opciones: List<String>) {
        try {
            val adaptador = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                opciones
            )
            cbSelectEccomerce.setAdapter(adaptador)

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun obtenerPaqueteria(){
        try {
            val headers= mapOf(
                "Token" to GlobalUser.token.toString()
            )

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/paqueterias/list",
                        params= emptyMap(),
                        dataClass = listaPaqueterias::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.NOMBRE }

                                    actualizarcbBoxPaqueteria(opciones)
                                } else {
                                    MensajesDialog.showMessage(this@Embarque_Paquetes, "Lista de paqueterias vacia")
                                }
                            }
                        },
                        onError = { error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Embarque_Paquetes, "Error: $error");
                            }
                        } )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Embarque_Paquetes, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Embarque_Paquetes, "Ocurrió un error: ${e.message}");
            }
        }
    }

    private fun actualizarcbBoxPaqueteria(opciones: List<String>) {
        try {
            val adaptador = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                opciones
            )
            cbSelectPaqueteria.setAdapter(adaptador)

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_embarque::class.java));
        //LogsEntradaSalida.logsPorModulo( this, lifecycleScope, "EMBARQUE/RECOLECTA", "SALIDA");
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