package com.example.slimmx.Embarque.Envio

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListaItemsPi_Envio
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.R
import com.example.slimmx.Submenus.Submenu_embarque
import com.example.slimmx.Submenus.Submenu_picking
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.example.slimmx.listaResultadoEmbarqueCajas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Embarque_Envio : AppCompatActivity() {
    private var filaSeleccionada: TableRow? = null
    private lateinit var txtEtiqueta: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_embarque_envio)
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

        txtEtiqueta=findViewById(R.id.txtEtiqueta);
        val btn_etiqueta=findViewById<Button>(R.id.buttonEliminar_etiqueta);
        val txtAnden=findViewById<EditText>(R.id.txtAnden);
        val btn_eliminar_anden=findViewById<Button>(R.id.buttonEliminar_anden);


        txtEtiqueta.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        txtAnden.post { txtAnden.requestFocus() }
        btn_etiqueta.setOnClickListener {
            txtEtiqueta.setText("");
        }

        txtEtiqueta.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtEtiqueta.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        if(txtAnden.text.isNotEmpty()){
                            ConfirmarFolio(txtAnden.text.toString());
                        }else{
                            MensajesDialog.showMessage(this@Embarque_Envio, "Se debe escanear el Andén de salida");
                        }
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }

        btn_eliminar_anden.setOnClickListener {
            txtAnden.setText("");
            txtAnden.post { txtAnden.requestFocus() }
        }

    }

    private fun ConfirmarFolio(anden:String){
        try {
            val body = mapOf(
                "ID" to txtEtiqueta.text.toString(),
                "ANDEN" to anden.uppercase()
            )

            val headers = mapOf("Token" to GlobalUser.token.toString());

            lifecycleScope.launch {
                Pedir_datos_apis_post(
                    endpoint = "/inventario/embarque/confirmar",
                    body = body,
                    dataClass = Any::class,
                    listaKey = "message",
                    headers = headers,
                    onSuccess = { response ->
                        try {
                            try {
                                val message = response.toString()
                                if(message.contains("CAJA VALIDADA")){
                                    MensajesDialog.showMessage(this@Embarque_Envio,"CAJA VALIDADA");
                                    optenerDatositems(txtEtiqueta.text.toString());
                                    txtEtiqueta.setText("");
                                }else{
                                    MensajesDialog.showMessage(this@Embarque_Envio, "Respuesta: $message");
                                }
                            } catch (e: Exception) {
                                MensajesDialog.showMessage(this@Embarque_Envio, "Error al procesar la respuesta: ${e.message}")
                            }

                        } catch (e: Exception) {
                            MensajesDialog.showMessage(this@Embarque_Envio, "Error al procesar la respuesta: ${e.message}")
                        }
                    },
                    onError = { error ->
                        MensajesDialog.showMessage(this@Embarque_Envio, "Error: ${error}")
                        optenerDatositems(txtEtiqueta.text.toString());
                        txtEtiqueta.setText("");
                    }
                )
            }

        } catch (e: Exception) {
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}")
        }
    }


    private fun optenerDatositems(seleccion: String){
        if (seleccion.isNotEmpty()){
            try {
                val params= mapOf(
                    "id" to seleccion
                );

                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch(Dispatchers.IO) {
                    try{
                        Pedir_datos_apis(
                            endpoint = "/inventario/embarque",
                            params=params,
                            dataClass = listaResultadoEmbarqueCajas::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val items = lista.map {
                                            listaResultadoEmbarqueCajas(
                                                ID = it.ID,
                                                CANTIDAD_EMPACADA = it.CANTIDAD_EMPACADA
                                            )
                                        }
                                        actualizarDatosTabla(items)
                                    }
                                }
                            },
                            onError = {error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Embarque_Envio, "Error: $error")
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Embarque_Envio, "Ocurrió un error: ${e.message}")
                        }
                    }
                }


            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Embarque_Envio, "Ocurrió un error: ${e.message}");
                }
            }
        }else{
            MensajesDialog.showMessage(this,"Debes de seleccionar un folio")
        }

    }

    private fun actualizarDatosTabla(items: List<listaResultadoEmbarqueCajas>){
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

            if (tableLayout.childCount > 1) {
                tableLayout.removeViews(1, tableLayout.childCount - 1)
            }

            for (item in items) {
                val tableRow = TableRow(this);
                agregarFila(tableRow, item);

                tableRow.setOnClickListener {
                    filaSeleccionada?.setBackgroundColor(Color.TRANSPARENT)

                    //Solo cambia de color la fila que se selecciono
                    tableRow.setBackgroundColor(Color.parseColor("#639A67"))

                    // Actualiza las filas para el control de los colores
                    filaSeleccionada = tableRow

                }

                tableLayout.addView(tableRow)
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun agregarFila(tableRow: TableRow, item: listaResultadoEmbarqueCajas) { //Agregar los datos encontrados en la tabla
        try {
            val envioText = TextView(this).apply {
                text = item.ID;
                gravity= Gravity.CENTER;
                textSize = 20f ;
                setTextColor(Color.BLACK);
            }
            val cantidadText = TextView(this).apply {
                text = item.CANTIDAD_EMPACADA.toString();
                gravity= Gravity.CENTER;
                textSize = 20f ;
                setTextColor(Color.BLACK);
            }

            tableRow.addView(envioText)
            tableRow.addView(cantidadText)

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_embarque::class.java));
        //LogsEntradaSalida.logsPorModulo( this, lifecycleScope, "EMBARQUE/ENVIO", "SALIDA")
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