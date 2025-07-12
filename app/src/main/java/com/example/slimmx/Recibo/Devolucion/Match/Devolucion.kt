package com.example.slimmx.Recibo.Devolucion.Match

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalUser
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis_post_json
import com.example.slimmx.R
import com.example.slimmx.Recibo.Devolucion.Seleccion_devolucion
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.listaResultadoDevolucion
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class Devolucion : AppCompatActivity() {

    private lateinit var cbSelectPaqueteria: AutoCompleteTextView;
    private lateinit var cbSelectCajas: AutoCompleteTextView;
    private lateinit var txt_numero_paquete: EditText;
    private lateinit var txt_folio_retiro: EditText;
    lateinit var layoutContenedor: LinearLayout
    private var jsonPaqueteriaArray = JSONArray()
    val guiasSet = mutableSetOf<String>();
    private var paqueteriaActual:String="";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_devolucion)
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
        val btn_confirmar=findViewById<Button>(R.id.buttonConfirmar);
        txt_numero_paquete=findViewById(R.id.txtNumero_guia);
        txt_folio_retiro=findViewById(R.id.txtFolio);
        layoutContenedor = findViewById(R.id.layoutContenedor);

        val opcionesDefaultPaqueteria = listOf(
            "DHL",
            "ESTAFETA",
            "FEDEX",
            "ML",
            "CORREOS",
            "PAQUETEEXPRESS",
            "CHICOH",
            "REDPACK",
            "99MINUTOS",
            "IMILE",
            "J&T",
            "TREGGO",
            "RETIRO",
            "RM",
            "AMAZON"
        )

        cbSelectPaqueteria =findViewById(R.id.cbSelectPaqueteria)

        txt_folio_retiro.isVisible=false;

        val adaptadorPaqueteria = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            opcionesDefaultPaqueteria
        )
        cbSelectPaqueteria.setAdapter(adaptadorPaqueteria)

        cbSelectPaqueteria.setOnItemClickListener { _, _, position, _ ->
            try {
                when (opcionesDefaultPaqueteria[position]) {
                    "RETIRO" -> {
                        txt_folio_retiro.isVisible=true;
                    }
                    else -> {
                    txt_folio_retiro.isVisible = false
                    }
                }
            } catch (e: Exception) {
                MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
            }

        }

        val opcionesDefaultCajas = listOf("1", "2", "3", "4", "5");

        cbSelectCajas = findViewById(R.id.cbSelectCajas);

        val adaptadorCajas =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opcionesDefaultCajas)
        cbSelectCajas.setAdapter(adaptadorCajas)

        cbSelectCajas.setOnItemClickListener { _, _, position, _ ->
            try {
                if (cbSelectPaqueteria.text.toString() == "RETIRO"){
                    txt_folio_retiro.post { txt_folio_retiro.requestFocus() }
                }else{
                    txt_numero_paquete.post { txt_numero_paquete.requestFocus() }
                }

                when (opcionesDefaultCajas[position]) {
                    "1" -> {

                    }

                }
            } catch (e: Exception) {
                MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
            }

        }

        btn_confirmar.setOnClickListener {
            try {
                if (cbSelectPaqueteria.text.isNotEmpty() && cbSelectCajas.text.isNotEmpty()){
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Confirmación")
                    builder.setMessage("¿Está seguro de que desea confirmar?")
                    builder.setPositiveButton("Confirmar") { dialog, _ ->
                        //ConfirmacionPaquete(cbSelectPaqueteria.text.toString(), txt_numero_paquete.text.toString(), cbSelectCajas.text.toString());
                        ConfirmacionPaquete();
                        dialog.dismiss()
                    }

                    builder.setNegativeButton("Cancelar") { dialog, _ ->

                        dialog.dismiss()
                    }

                    val dialog = builder.create()
                    dialog.show()

                }else{
                    MensajesDialog.showMessage(this, "Datos incompletos");
                }
            }catch (e:Exception){
                MensajesDialog.showMessage(this, "Ocurrio un error: ${e.message}")
            }
        }

        txt_numero_paquete.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val input = txt_numero_paquete.text.toString().trim()
                manejarNumeroPaquete(input)
                true
            } else {
                false
            }
        }


    }

    private fun manejarNumeroPaquete(input: String) {
        try {
            val codigo = input.trim()

            if (codigo.isNotEmpty()) {
                cbSelectPaqueteria.isEnabled = false
                // Validar si el número ya existe
                if (!guiasSet.contains(codigo)) {
                    guiasSet.add(codigo)
                    agregarTarjeta(layoutContenedor, codigo)

                    // Agregar al JSONArray
                    val jsonObject = JSONObject().apply {
                        put("MENSAJERIA", paqueteriaActual);
                        put("ID", codigo);
                        put("NUMERO", cbSelectCajas.text.toString());
                    }
                    jsonPaqueteriaArray.put(jsonObject);

                    // Log para depuración
                    Log.d("PaqueteriaJSON", jsonPaqueteriaArray.toString());

                    // Limpiar campos
                    txt_numero_paquete.text.clear();
                    txt_folio_retiro.text.clear();
                } else {
                    MensajesDialog.showMessage(this@Devolucion, "El número de guía ya existe");
                    txt_numero_paquete.text.clear();
                }
            }
        } catch (e: Exception) {
            MensajesDialog.showMessage(this@Devolucion, "Ocurrió un error: ${e.message}")
        }
    }

    private fun ConfirmacionPaquete() {
        try {
            Log.d("DEBUG", "Contenido inicial de jsonPaqueteriaArray: $jsonPaqueteriaArray")

            if (jsonPaqueteriaArray.length() == 0) {
                MensajesDialog.showMessage(this, "El arreglo de paquetería está vacío.")
                return
            }

            val jsonString = jsonPaqueteriaArray.toString()
            Log.d("DEBUG", "JSON String enviado: $jsonString")

            val requestBody = jsonString.toRequestBody("application/json".toMediaType())

            val headers = mapOf(
                "Token" to GlobalUser.token.toString(),
                "Content-Type" to "application/json"
            )

            Pedir_datos_apis_post_json(
                endpoint = "/shipment/get/async",
                body = requestBody,
                dataClass = Any::class,
                listaKey = "message",
                headers = headers,
                onSuccess = { response  ->
                    try {
                        try {
                            val message = response.toString()
                            if(message.contains("Registros procesados correctamente")) {
                                MensajesDialog.showMessage(this, "Agregada correctamente");
                                val contenedor=findViewById<LinearLayout>(R.id.layoutContenedor);
                                contenedor.removeAllViews();
                                jsonPaqueteriaArray = JSONArray();
                                cbSelectPaqueteria.isEnabled = true
                                limpiarFormulario();
                            } else {
                                MensajesDialog.showMessage(this, "Respuesta: $message");
                            }


                        } catch (e: Exception) {
                            MensajesDialog.showMessage(this@Devolucion, "Error al procesar la respuesta: ${e.message}")
                        }

                    } catch (e: Exception) {
                        MensajesDialog.showMessage(this@Devolucion, "Error al procesar la respuesta: ${e.message}")
                    }


/*
                    if (lista.mensaje.contains("Registros procesados correctamente")) {
                        MensajesDialog.showMessage(this, "Agregada correctamente");
                        val contenedor=findViewById<LinearLayout>(R.id.layoutContenedor);
                        contenedor.removeAllViews();
                        jsonPaqueteriaArray = JSONArray();
                        cbSelectPaqueteria.isEnabled = true
                        limpiarFormulario();
                    } else {
                        MensajesDialog.showMessage(this, "${lista.mensaje}");
                    }*/
                },
                onError = { error ->
                    MensajesDialog.showMessage(this, "Error:  $error");
                }
            )
        } catch (e: Exception) {
            MensajesDialog.showMessage(this, "Ocurrió un error ${e.message}");
        }
    }

    private fun limpiarFormulario() {
        cbSelectPaqueteria.clearListSelection();
        cbSelectCajas.clearListSelection();
        txt_numero_paquete.setText("");
    }

    private fun agregarTarjeta(parent: LinearLayout, numeroGuia: String) {
        val tarjeta = layoutInflater.inflate(R.layout.tarjeta_paqueteria, parent, false)

        val textFolio: TextView = tarjeta.findViewById(R.id.textView39)
        val textPaqueteria: TextView = tarjeta.findViewById(R.id.textView42)
        val textNumeroGuia: TextView = tarjeta.findViewById(R.id.textView43)
        val txtRonda: TextView = tarjeta.findViewById(R.id.txtRonda)
        val btnEliminar: ImageView = tarjeta.findViewById(R.id.btnEliminar) // Botón para eliminar la tarjeta

        if (txt_folio_retiro.text.isNotEmpty()) {
            textFolio.text = "${txt_folio_retiro.text}"
            textFolio.isVisible = true
        } else {
            textFolio.isVisible = false
        }

        txtRonda.text = "${cbSelectCajas.text}"
        textPaqueteria.text = "${cbSelectPaqueteria.text}"
        textNumeroGuia.text = numeroGuia

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(8, 8, 8, 8) // Margen de 8dp
        tarjeta.layoutParams = params

        btnEliminar.setOnClickListener {
            val builder = AlertDialog.Builder(this@Devolucion)
            builder.setTitle("Eliminar Guía")
                .setMessage("¿Estás seguro de que deseas eliminar esta guía?")
                .setPositiveButton("Sí") { dialog, which ->
                    guiasSet.remove(numeroGuia)

                    val updatedJsonArray = JSONArray()

                    for (i in 0 until jsonPaqueteriaArray.length()) {
                        val jsonObject = jsonPaqueteriaArray.getJSONObject(i)

                        if (jsonObject.getString("ID") != numeroGuia) {
                            updatedJsonArray.put(jsonObject)
                        }
                    }

                    jsonPaqueteriaArray = updatedJsonArray

                    Log.d("PaqueteriaJSON", jsonPaqueteriaArray.toString())

                    parent.removeView(tarjeta)

                    guardarJSON()
                }
                .setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
        Log.d("DEBUG", "jsonPaqueteriaArray después de agregar: $jsonPaqueteriaArray")
        parent.addView(tarjeta)
    }

    private fun guardarJSON() {
        try {
            val jsonString = jsonPaqueteriaArray.toString()

            val sharedPreferences = getSharedPreferences("PaqueteriaPrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("paqueteria_json", jsonString)
            editor.apply()

            Log.d("JSONGuardado", "JSON guardado correctamente: $jsonString")
        } catch (e: Exception) {
            Log.e("ErrorGuardarJSON", "Error al guardar el JSON: ${e.message}")
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Seleccion_devolucion::class.java));
        //LogsEntradaSalida.logsPorModulo( this, lifecycleScope, "DEVOLUCION/MATCH", "SALIDA");
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