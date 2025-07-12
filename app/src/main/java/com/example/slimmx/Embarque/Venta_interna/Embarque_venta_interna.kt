package com.example.slimmx.Embarque.Venta_interna

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalUser
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.Picking.Cedis.Cedis_seleccion_menu
import com.example.slimmx.R
import com.example.slimmx.Submenus.Submenu_embarque
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.listDatosFolios
import com.example.slimmx.listaEnbarquePuntoVenta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.w3c.dom.Text

class Embarque_venta_interna : AppCompatActivity() {
    private var filaSeleccionada: TableRow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_embarque_venta_interna)
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

        val txtEditText=findViewById<EditText>(R.id.txtEditFolio);
        val btn_confirmar=findViewById<Button>(R.id.buttonConfirmar);
        val buttonEliminar_folio=findViewById<Button>(R.id.buttonEliminar_folio)
        val txtFolio=findViewById<TextView>(R.id.txt_folio);
        val txtCliente=findViewById<TextView>(R.id.txt_cliente);
        val txtNumeroPiezas=findViewById<TextView>(R.id.txt_piezas);

        txtEditText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtEditText.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        obtenerDatosPorFolio(txtEditText.text.toString());
                        txtEditText.setText("");
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }

        buttonEliminar_folio.setOnClickListener {
            txtEditText.setText("");
            txtEditText.post { txtEditText.requestFocus() }
        }

        btn_confirmar.setOnClickListener {
            if(txtFolio.text.isNotEmpty()){
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Confirmación")
                    builder.setMessage("¿Estás seguro de confirmar los datos?")

                    builder.setPositiveButton("Confirmar") { dialog, _ ->
                        btn_confirmar.isEnabled=false;
                        try {
                            val body = mapOf(
                                "FOLIO" to txtFolio.text.toString(),
                                "ALMACEN_ID" to "1"
                            )
                            val headers = mapOf("Token" to GlobalUser.token.toString());

                            lifecycleScope.launch {
                                Pedir_datos_apis_post(
                                    endpoint = "/clientes/venta/embarque",
                                    body = body,
                                    dataClass = Any::class,
                                    listaKey = "message",
                                    headers = headers,
                                    onSuccess = { response ->
                                        try {
                                            try {
                                                val message = response.toString()
                                                if(message.contains("VENTA EMBARCADA CORRECTAMENTE")){
                                                    MensajesDialogConfirmaciones.showMessage(this@Embarque_venta_interna, "Confirmado correctamente") {

                                                        val items = List(5) {
                                                            listaEnbarquePuntoVenta(
                                                                CANTIDAD_CONFIRMADA = 0,
                                                                CODIGO = "",
                                                                DESCRIPCION = "",
                                                                FOLIO = 0,
                                                                NAME_CLIENT = ""
                                                            )
                                                        }
                                                        actualizarTableLayout(items);
                                                        txtFolio.setText("");
                                                        txtCliente.setText("");
                                                        txtNumeroPiezas.setText("");
                                                        btn_confirmar.isEnabled=true;
                                                    }
                                                }else{
                                                    MensajesDialog.showMessage(this@Embarque_venta_interna, "Respuesta: $message");
                                                    btn_confirmar.isEnabled=true;
                                                }
                                            } catch (e: Exception) {
                                                MensajesDialog.showMessage(this@Embarque_venta_interna, "Error al procesar la respuesta: ${e.message}")
                                                btn_confirmar.isEnabled=true;
                                            }

                                        } catch (e: Exception) {
                                            MensajesDialog.showMessage(this@Embarque_venta_interna, "Error al procesar la respuesta: ${e.message}")
                                            btn_confirmar.isEnabled=true;
                                        }
                                    },
                                    onError = { error ->
                                        val errorMessage = error.split("\n").first()
                                        MensajesDialog.showMessage(this@Embarque_venta_interna, "Error: $errorMessage");
                                        btn_confirmar.isEnabled=true;
                                    }
                                )
                            }

                        } catch (e: Exception) {
                            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}")
                            btn_confirmar.isEnabled=true;
                        }
                        dialog.dismiss()
                    }

                    builder.setNegativeButton("Cancelar") { dialog, _ ->
                        btn_confirmar.isEnabled=true;
                        dialog.dismiss()
                    }

                    builder.create().show()

            } else {
                MensajesDialog.showMessage(this, "El folio esta vacio");
                btn_confirmar.isEnabled=true;
            }
        }


    }

    private fun obtenerDatosPorFolio(seleccion: String){
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }
        val txtFolio=findViewById<TextView>(R.id.txt_folio);
        val txtCliente=findViewById<TextView>(R.id.txt_cliente);
        val txtNumeroPiezas=findViewById<TextView>(R.id.txt_piezas);
        if(seleccion.isNotEmpty()){
            try {
                val params= mapOf(
                    "folio" to seleccion
                )
                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/clientes/ventas/items/embarque",
                            params=params,
                            dataClass = listaEnbarquePuntoVenta::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val items = lista.map {
                                            listaEnbarquePuntoVenta(
                                                CANTIDAD_CONFIRMADA = it.CANTIDAD_CONFIRMADA,
                                                CODIGO = it.CODIGO,
                                                DESCRIPCION = it.DESCRIPCION,
                                                FOLIO = it.FOLIO,
                                                NAME_CLIENT = it.NAME_CLIENT
                                            )
                                        }
                                        actualizarTableLayout(items);
                                        txtFolio.setText(
                                            lista.firstOrNull()?.FOLIO?.toString() ?: ""
                                        )
                                        txtCliente.setText(lista.firstOrNull()?.NAME_CLIENT ?: "")
                                        txtNumeroPiezas.setText(lista.sumOf { it.CANTIDAD_CONFIRMADA }
                                            .toString());
                                    } else {
                                        val tableLayout =
                                            findViewById<TableLayout>(R.id.tableLayout)
                                        if (tableLayout.childCount > 1) {
                                            tableLayout.removeViews(1, tableLayout.childCount - 1)
                                        }
                                        MensajesDialog.showMessage(this@Embarque_venta_interna, "No se encontro ese folio de venta");
                                    }

                                }
                            } ,
                            onError = {error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Embarque_venta_interna, "Errores: $error");
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Embarque_venta_interna, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Embarque_venta_interna, "Ocurrió un error: ${e.message}");
                }
            }
        }else{
            MensajesDialog.showMessage(this,"Se debe de seleccionar un folio");
        }

    }

    private fun actualizarTableLayout(items: List<listaEnbarquePuntoVenta>) {
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

            if (tableLayout.childCount > 1) {
                tableLayout.removeViews(1, tableLayout.childCount - 1)
            }

            for (item in items) {
                val tableRow = TableRow(this)
                agregarFila(tableRow, item)

                tableRow.setOnClickListener {
                    filaSeleccionada?.setBackgroundColor(Color.TRANSPARENT)
                    tableRow.setBackgroundColor(Color.parseColor("#639A67"))
                    filaSeleccionada = tableRow
                }

                tableLayout.addView(tableRow)
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun agregarFila(tableRow: TableRow, item: listaEnbarquePuntoVenta) {
        try {
            val codigoTextView = TextView(this).apply {
                text = item.CODIGO
                gravity = Gravity.CENTER
            }

            val descripcionTextView = TextView(this).apply {
                text = item.DESCRIPCION
            }

            val cantidadTextView = TextView(this).apply {
                text = item.CANTIDAD_CONFIRMADA.toString()
                gravity = Gravity.CENTER
            }


            tableRow.addView(codigoTextView);
            tableRow.addView(descripcionTextView);
            tableRow.addView(cantidadTextView);

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_embarque::class.java));
        //LogsEntradaSalida.logsPorModulo( this@Embarque_venta_interna, lifecycleScope, "EMBARQUE/VENTA/INTERNA", "SALIDA")
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