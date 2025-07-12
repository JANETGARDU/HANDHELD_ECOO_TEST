package com.example.slimmx.Empaque.Recolecta.Asignacion

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.Empaque.Recolecta.Submenu_Empaque_Recolecta
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListaPaqueteria
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.R
import com.example.slimmx.Submenus.Submenu_picking
import com.example.slimmx.Validaciones.LogsEntradaSalida
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Empaque_Recolecta_Asiganacion : AppCompatActivity() {

    private var paqueteria:String="";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_empaque_recolecta_asiganacion)
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

        val txtGuia=findViewById<EditText>(R.id.txtGuia);
        val buttonConfirmar=findViewById<Button>(R.id.buttonConfirmar);
        val txtUbicacion=findViewById<EditText>(R.id.txtUbicacion);
        val btn_eliminar_ubicacion=findViewById<Button>(R.id.buttonEliminar_ubicacion);
        val btn_eliminar_guia=findViewById<Button>(R.id.buttonEliminar_guia);


        txtGuia.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    var guia=txtGuia.text.toString();
                    if (!guia.isNullOrEmpty()) {
                        obtenerEccomerce(guia);
                    }else{
                        MensajesDialog.showMessage(this@Empaque_Recolecta_Asiganacion, "Debes escanear el número de guía");
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this@Empaque_Recolecta_Asiganacion, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }

        buttonConfirmar.setOnClickListener {
            /*if (txtUbicacion.text.toString().toUpperCase().contains(paqueteria.toUpperCase())){
                //MensajesDialog.showMessage(this,"Si coincide");*/
                Confirmacion_datos(txtGuia.text.toString(), txtUbicacion.text.toString().toUpperCase());
           /* }else{
                MensajesDialog.showMessage(this,"NO SE ESTA COLOCANDO EN LA UBICACIÓN CORRECTA");
                buttonConfirmar.isEnabled=true;
            }*/
        }

        btn_eliminar_ubicacion.setOnClickListener {
            txtUbicacion.setText("");
            txtUbicacion.post { txtUbicacion.requestFocus() }
        }

        btn_eliminar_guia.setOnClickListener {
            txtGuia.setText("");
            txtGuia.post { txtGuia.requestFocus()}
        }

    }

    private fun obtenerEccomerce(guia:String){
        try {
            val params= mapOf(
                "guia" to guia
            );

            val headers = mapOf("Token" to GlobalUser.token.toString());

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/paquetes/obtener/eccomerce",
                        params=params,
                        dataClass = ListaPaqueteria::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.PAQUETERIA }
                                    paqueteria =
                                        opciones.toString().replace("[", "").replace("]", "");
                                    println("Eccomerce: " + paqueteria);

                                } else {
                                    //val intent = Intent(this@Empaque_Recolecta_Asiganacion, Submenu_picking::class.java);
                                    //intent.putExtra("MESSAGE", "No se encontro la paqueteria");
                                    // startActivity(intent);
                                   // finish();
                                }

                            }
                        },
                        onError = { error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Empaque_Recolecta_Asiganacion, "Error: $error");
                            }
                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Empaque_Recolecta_Asiganacion, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Empaque_Recolecta_Asiganacion, "Ocurrió un error: ${e.message}");
            }
        }
    }

    private fun Confirmacion_datos(id:String,ubicacion: String){
        val buttonConfirmar=findViewById<Button>(R.id.buttonConfirmar);
        if (id.isNotEmpty() && ubicacion.isNotEmpty() ) {
            try {
                buttonConfirmar.isEnabled=false;
                val txtGuia=findViewById<EditText>(R.id.txtGuia);
                val body = mapOf(
                    "SHIPPING_ID" to id,
                    "UBICACION_DESTINO" to ubicacion.uppercase()
                )

                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch {
                    Pedir_datos_apis_post(
                        endpoint = "/paquetes/check/ubi/destino/asig",
                        body = body,
                        dataClass =Any::class,
                        listaKey = "message",
                        headers=headers,
                        onSuccess = { response ->
                            try {
                                try {
                                    val message = response.toString()
                                    if(message.contains("ENTREGADO CORRECTAMENTE")){
                                        MensajesDialogConfirmaciones.showMessage(this@Empaque_Recolecta_Asiganacion, "OK") {
                                            txtGuia.setText("");
                                            buttonConfirmar.isEnabled=true;
                                        }
                                    }else{
                                        MensajesDialog.showMessage(this@Empaque_Recolecta_Asiganacion, "Respuesta: $message");
                                        buttonConfirmar.isEnabled=true;
                                    }
                                } catch (e: Exception) {
                                    MensajesDialog.showMessage(this@Empaque_Recolecta_Asiganacion, "Error al procesar la respuesta: ${e.message}");
                                    buttonConfirmar.isEnabled=true;
                                }

                            } catch (e: Exception) {
                                MensajesDialog.showMessage(this@Empaque_Recolecta_Asiganacion, "Error al procesar la respuesta: ${e.message}");
                                buttonConfirmar.isEnabled=true;
                            }
                        },
                        onError = { error ->
                            MensajesDialog.showMessage(this@Empaque_Recolecta_Asiganacion, "Error: $error");
                            buttonConfirmar.isEnabled=true;
                        }
                    )
                }

            }catch (e: Exception){
                MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                buttonConfirmar.isEnabled=true;
            }

        }else {
            MensajesDialog.showMessage(this, "Por favor, complete todos los campos.");
            buttonConfirmar.isEnabled=true;
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_Empaque_Recolecta::class.java));
        //LogsEntradaSalida.logsPorModulo( this@Empaque_Recolecta_Asiganacion, lifecycleScope, "EMPAQUE/RECOLECTA/ASIGNACION", "SALIDA");
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