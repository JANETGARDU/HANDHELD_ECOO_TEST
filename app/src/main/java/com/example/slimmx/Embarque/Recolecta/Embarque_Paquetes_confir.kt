package com.example.slimmx.Embarque.Recolecta

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListaFolioPickingEnvio
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.R
import com.example.slimmx.Submenus.Submenu_acomodo
import com.example.slimmx.Validaciones.LogsEntradaSalida
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class Embarque_Paquetes_confir : AppCompatActivity() {

    private lateinit var txt_numero_paquete: EditText;
    private lateinit var txtEcomerce: TextView;
    private lateinit var txtPaqueteria: TextView;
    private lateinit var txtNumero_siguiente:TextView;
    private lateinit var txtNumeroEscaneado:TextView;
    private lateinit var txtBuscar:EditText;
    val guiasSet = mutableSetOf<String>();
    lateinit var layoutContenedor: LinearLayout;
    var v_ecommerce:String="";
    var v_paqueteria:String="";
    var v_numero:String="";
    private var contadorPaquete = 1
    private var contadorPaquete_2 = contadorPaquete

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_embarque_paquetes_confir)
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

        txtEcomerce=findViewById(R.id.txtEcomerce);
        txtPaqueteria=findViewById(R.id.txtPaqueteria);
        txtNumero_siguiente=findViewById(R.id.txtNumero_siguiente);
        txtNumeroEscaneado=findViewById(R.id.txtNumeroEscaneadoPack);
        txtBuscar=findViewById(R.id.txtBuscar);

        /*v_ecommerce=intent.getStringExtra("ecommerce").toString();
        v_paqueteria = intent.getStringExtra("paqueteria").toString();

        txtEcomerce.setText(v_ecommerce);
        txtPaqueteria.setText(v_paqueteria);*/

        layoutContenedor = findViewById(R.id.layoutContenedor);
        txt_numero_paquete=findViewById(R.id.txtGuia);

        txt_numero_paquete.post { txt_numero_paquete.requestFocus() }

        val buttonEliminar_Guia=findViewById<Button>(R.id.buttonEliminar_Guia);
        val buttonEliminar_buscar=findViewById<Button>(R.id.buttonEliminar_buscar);
        val txtAnden=findViewById<EditText>(R.id.txtAnden);
        val btn_eliminar_anden=findViewById<Button>(R.id.buttonEliminar_anden);

        NumeroSiguiente(v_ecommerce.toString(),v_paqueteria.toString());
        txtAnden.post { txtAnden.requestFocus() }

        txt_numero_paquete.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                confirmarGuia(txtAnden.text.toString());
                true
            } else {
                false
            }
        }

        txtBuscar.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                buscarGuia();
                true
                txtBuscar.post { txtBuscar.requestFocus() }
            } else {
                false
                txtBuscar.post { txtBuscar.requestFocus() }
            }
        }

        buttonEliminar_Guia.setOnClickListener {
            txt_numero_paquete.setText("");
            txt_numero_paquete.post { txt_numero_paquete.requestFocus() }
        }

        buttonEliminar_buscar.setOnClickListener {
            txtBuscar.setText("");
            txtBuscar.post { txtBuscar.requestFocus() }
        }

        btn_eliminar_anden.setOnClickListener {
            txtAnden.setText("");
            txtAnden.post { txtAnden.requestFocus() }
        }


    }

    private fun manejarNumeroPaquete(input: String) {
        try {
            val codigo = input.trim()

            if (codigo.isNotEmpty()) {

                if (!guiasSet.contains(codigo)) {
                    guiasSet.add(codigo)
                    agregarTarjeta(layoutContenedor, codigo)

                    txt_numero_paquete.text.clear();

                } else {
                    MensajesDialog.showMessage(this, "El número de guía ya existe");
                    txt_numero_paquete.text.clear();
                }
            }
        } catch (e: Exception) {
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}")
        }
    }

    private fun agregarTarjeta(parent: LinearLayout, numeroGuia: String) {
        val tarjeta = layoutInflater.inflate(R.layout.tarjeta_embarque_paquetes, parent, false)

        val txtNumeroGuia: TextView = tarjeta.findViewById(R.id.txtNumeroGuia)
        val txtNumero_paquete: TextView = tarjeta.findViewById(R.id.txtNumero_paquete);


        val numeroPaqueteActual = contadorPaquete++
        val numeroPaquetes=numeroPaqueteActual;
        txtNumero_paquete.setText("${numeroPaqueteActual}");
        txtNumeroEscaneado.setText("${numeroPaquetes}");

        txtNumeroGuia.text = numeroGuia

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(8, 8, 8, 8) // Margen de 8dp
        tarjeta.layoutParams = params
        tarjeta.setOnLongClickListener {
            mostrarDialogoEliminar(tarjeta, numeroGuia, parent);
            true
        }

        parent.addView(tarjeta)
    }

    private fun mostrarDialogoEliminar(tarjeta: View, numeroGuia: String, parent: LinearLayout) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar tarjeta")
            .setMessage("¿Deseas eliminar la guía $numeroGuia?")
            .setPositiveButton("Sí") { _, _ ->
                EliminarPaquete(numeroGuia,tarjeta,parent)

            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun confirmarGuia(anden:String){
        try {

            val body= mapOf(
                "id" to v_numero,
                "shippment_id" to txt_numero_paquete.text.toString(),
                "ecommerce" to v_ecommerce,
                "paqueteria" to v_paqueteria,
                "ANDEN" to anden.uppercase()
            )
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            )
            lifecycleScope.launch {
                Pedir_datos_apis_post(
                    endpoint = "/paquetes/check/embarque/id",
                    body = body,
                    dataClass = Any::class,
                    listaKey = "message",
                    headers = headers,
                    onSuccess = { response  ->
                        try {
                            try {
                                val message = response.toString()
                                if(message.contains("OK EMBARCADO")){
                                    /* val contenedor=findViewById<LinearLayout>(R.id.layoutContenedor);
                                     contenedor.removeAllViews();*/
                                    val input = txt_numero_paquete.text.toString().trim()
                                    if (input.isNotEmpty()) {
                                        manejarNumeroPaquete(input)
                                        txt_numero_paquete.text.clear()
                                    }
                                }else{
                                    MensajesDialog.showMessage(this@Embarque_Paquetes_confir, "Respuesta: $message");
                                }
                            } catch (e: Exception) {
                                MensajesDialog.showMessage(this@Embarque_Paquetes_confir, "Error al procesar la respuesta: ${e.message}")
                            }

                        } catch (e: Exception) {
                            MensajesDialog.showMessage(this@Embarque_Paquetes_confir, "Error al procesar la respuesta: ${e.message}")
                        }
                    },
                    onError = { error ->
                        MensajesDialog.showMessage(this@Embarque_Paquetes_confir, "Error:  $error");
                    }
                )
            }

        } catch (e: Exception) {
            MensajesDialog.showMessage(this, "Ocurrió un error ${e.message}");
        }
    }

    private fun NumeroSiguiente(ecommerce:String, paqueteria:String){
        try {
            val params= mapOf(
                "ecommerce" to ecommerce,
                "paqueteria" to paqueteria
            )
            val headers = mapOf("Token" to GlobalUser.token.toString());
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/paquetes/check/embarque/id/return",
                        params=params,
                        dataClass = ListaFolioPickingEnvio::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    txtNumero_siguiente.setText(
                                        lista.map { it.ID }.toString().replace("[", "")
                                            .replace("]", "")
                                    );
                                    v_numero = lista.map { it.ID }.toString().replace("[", "")
                                        .replace("]", "");
                                } else {
                                    /*val intent = Intent(this@Embarque_Paquetes_confir, Submenu_acomodo::class.java);
                                    intent.putExtra("MESSAGE", "No hay tareas para este usuario");
                                    startActivity(intent);
                                    finish();*/
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Embarque_Paquetes_confir, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Embarque_Paquetes_confir, "Ocurrió un error: ${e.message}")
                    }
                }
            }
        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Embarque_Paquetes_confir, "Ocurrió un error: ${e.message}");
            }
        }
    }

    private fun buscarGuia() {
        val numeroBuscado = txtBuscar.text.toString().trim()

        if (numeroBuscado.isEmpty()) {
            MensajesDialog.showMessage(this, "Por favor, ingresa un número de guía para buscar")
            return
        }

        val contenedor = findViewById<LinearLayout>(R.id.layoutContenedor)
        var encontrado = false

        for (i in 0 until contenedor.childCount) {
            val tarjeta = contenedor.getChildAt(i) // Obtén cada tarjeta
            val txtNumeroGuia = tarjeta.findViewById<TextView>(R.id.txtNumeroGuia)

            if (txtNumeroGuia != null && txtNumeroGuia.text.toString() == numeroBuscado) {

                tarjeta.setBackgroundColor(ContextCompat.getColor(this, R.color.white))

                MensajesDialog.showMessage(this, "Guía encontrada: $numeroBuscado");
                txtBuscar.post { txtBuscar.requestFocus() }
                encontrado = true
                break
            }
        }

        if (!encontrado) {
            MensajesDialog.showMessage(this, "No se encontró la guía: $numeroBuscado");
            txtBuscar.post { txtBuscar.requestFocus() }
        }
    }

    private fun EliminarPaquete(numeroGuia: String,tarjeta: View,  parent: LinearLayout){
        try {

            val body= mapOf(
                "id" to v_numero,
                "shippment_id" to numeroGuia.toString(),
                "ecommerce" to v_ecommerce,
                "paqueteria" to v_paqueteria
            )
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            )

            lifecycleScope.launch {
                Pedir_datos_apis_post(
                    endpoint = "/paquetes/check/embarque/delete",
                    body = body,
                    dataClass = Any::class,
                    listaKey = "message",
                    headers = headers,
                    onSuccess = { response  ->
                        try {
                            try {
                                val message = response.toString()
                                if(message.contains("OK ELIMINADO")){
                                    parent.removeView(tarjeta);
                                    contadorPaquete_2=contadorPaquete-1
                                    txtNumeroEscaneado.setText("${contadorPaquete_2-1}");
                                    MensajesDialog.showMessage(this@Embarque_Paquetes_confir, "Guía $numeroGuia eliminada.")
                                }else{
                                    MensajesDialog.showMessage(this@Embarque_Paquetes_confir, "Respuesta: $message");
                                }
                            } catch (e: Exception) {
                                MensajesDialog.showMessage(this@Embarque_Paquetes_confir, "Error al procesar la respuesta: ${e.message}")
                            }

                        } catch (e: Exception) {
                            MensajesDialog.showMessage(this@Embarque_Paquetes_confir, "Error al procesar la respuesta: ${e.message}")
                        }

                    },
                    onError = { error ->
                        MensajesDialog.showMessage(this@Embarque_Paquetes_confir, "Error:  $error");
                    }
                )
            }

        } catch (e: Exception) {
            MensajesDialog.showMessage(this, "Ocurrió un error ${e.message}");
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