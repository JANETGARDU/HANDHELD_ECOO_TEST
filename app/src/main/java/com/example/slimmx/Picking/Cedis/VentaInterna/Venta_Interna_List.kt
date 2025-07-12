package com.example.slimmx.Picking.Cedis.VentaInterna

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalUser
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Picking.Cedis.Cedis_seleccion_menu
import com.example.slimmx.R
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.listDatosFolios
import com.example.slimmx.listFoliosVentaInterna
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Venta_Interna_List : AppCompatActivity() {

    private lateinit var cbSelectVentaInterna_PI: AutoCompleteTextView
    private var filaSeleccionada: TableRow? = null
    private var Tcodigo:String="";
    private var Tdescripcion:String="";
    private var Tcantidad:String="";
    private var Tubicacion:String="";
    private var Tbox:String="";
    private var item_producto:String="";
    private var codigo_referencia:String="";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_venta_interna_list)
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

        cbSelectVentaInterna_PI = findViewById(R.id.cbSelectVentaInterna_PI);

        var btn_recargar=findViewById<Button>(R.id.buttonRecargar_Ven_In);
        var btn_confirmar=findViewById<Button>(R.id.buttonOK_PICK_Ven_In);

        //consultaFolios(); //Consultar los items

        val opciones = intent.getStringArrayExtra("folios")

        if (opciones != null && opciones.isNotEmpty()) {
            actualizarAutoCompleteComboBox(opciones.toList()) // Llenar el ComboBox con los datos recibidos
        } else {
            // Si no hay datos, regresar al Submenu_acomodo
            val intent = Intent(this, Cedis_seleccion_menu::class.java)
            intent.putExtra("MESSAGE", "No hay tareas para este usuario")
            startActivity(intent)
            finish()
        }

        btn_recargar.setOnClickListener {
            try {
                obtenerDatosPorFolio(cbSelectVentaInterna_PI.text.toString());
            }catch (e: Exception){
                MensajesDialog.showMessage(this@Venta_Interna_List, "Ocurrió un error: ${e.message}");
            }
        }

        btn_confirmar.setOnClickListener {
            try {
                val idSeleccionado = cbSelectVentaInterna_PI.text.toString()
                val intent = Intent(this, Venta_Interna_Confirmacion::class.java)

                intent.putExtra("folio", idSeleccionado);
                intent.putExtra("codigo", this@Venta_Interna_List.Tcodigo);
                intent.putExtra("descripcion", this@Venta_Interna_List.Tdescripcion);
                intent.putExtra("cantidad", this@Venta_Interna_List.Tcantidad);
                intent.putExtra("ubicacion", this@Venta_Interna_List.Tubicacion);
                intent.putExtra("box", this@Venta_Interna_List.Tbox);
                intent.putExtra("item_producto", this@Venta_Interna_List.item_producto);
                intent.putExtra("codigo_referencia", this@Venta_Interna_List.codigo_referencia);

                startActivity(intent)

                this@Venta_Interna_List.Tcodigo="";
                this@Venta_Interna_List.Tdescripcion="";
                this@Venta_Interna_List.Tubicacion="";
                this@Venta_Interna_List.Tbox="";
                this@Venta_Interna_List.Tcantidad="";
                this@Venta_Interna_List.item_producto="";
                this@Venta_Interna_List.codigo_referencia="";
            }catch (e: Exception){
                MensajesDialog.showMessage(this@Venta_Interna_List, "Ocurrió un error: ${e.message}");
            }
        }

    }

    override fun onResume() {//Cuando regresa a primer plano el activity
        super.onResume()

        if (!cbSelectVentaInterna_PI.text.isNullOrEmpty()) {
            var btn_actualizar=findViewById<Button>(R.id.buttonRecargar_Ven_In)
            btn_actualizar.performClick()
            cbSelectVentaInterna_PI.requestFocus()
        }
    }

    private fun consultaFolios(){
        try {

            val headers = mapOf("Token" to GlobalUser.token.toString());

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/clientes/ventas/internas/list",
                        params =emptyMap<String, String>(),
                        dataClass = listFoliosVentaInterna::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val resultados = lista.map { it.FOLIO.toString() }
                                    actualizarAutoCompleteComboBox(resultados);
                                } else {
                                    MensajesDialog.showMessage(
                                        this@Venta_Interna_List,
                                        "Ya no hay folios para pickear"
                                    );
                                    finish();
                                }


                            }
                        }, onError = {error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Venta_Interna_List, "Error: $error");
                            }
                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Venta_Interna_List, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Venta_Interna_List, "Ocurrió un error: ${e.message}");
            }
        }
    }

    private fun actualizarAutoCompleteComboBox(opciones: List<String>){
        try {
            val adaptador=ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                opciones
            )
            cbSelectVentaInterna_PI.setAdapter(adaptador);

            cbSelectVentaInterna_PI.setOnItemClickListener { _, _, position, _ ->
                val seleccion=opciones[position];
                val tableLayout = findViewById<TableLayout>(R.id.tableLayout_VentaI)

                if (tableLayout.childCount > 1) {
                    tableLayout.removeViews(1, tableLayout.childCount - 1)
                }
                obtenerDatosPorFolio(seleccion);
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun obtenerDatosPorFolio(seleccion: String){
        if(seleccion.isNotEmpty()){
            try {
                val params= mapOf(
                    "folio" to seleccion
                )
                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/clientes/ventas/internas/items",
                            params=params,
                            dataClass = listDatosFolios::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val items = lista.map {
                                            listDatosFolios(
                                                CODIGO = it.CODIGO,
                                                DESCRIPCION = it.DESCRIPCION,
                                                CANTIDAD = it.CANTIDAD,
                                                UBICACION = it.UBICACION,
                                                BOX = it.BOX,
                                                ITEM = it.ITEM,
                                                CODIGO_REFERENCIA = it.CODIGO_REFERENCIA?:""
                                            )
                                        }
                                        actualizarTableLayout(items);
                                    } else {
                                        val tableLayout =
                                            findViewById<TableLayout>(R.id.tableLayout_VentaI)
                                        if (tableLayout.childCount > 1) {
                                            tableLayout.removeViews(1, tableLayout.childCount - 1)
                                        }
                                        val intent = Intent(this@Venta_Interna_List, Cedis_seleccion_menu::class.java);
                                        intent.putExtra("MESSAGE", "Tarea concluida");
                                        startActivity(intent);
                                        finish();
                                    }

                                }
                            },
                            onError = {error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Venta_Interna_List, "Errores: $error");
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Venta_Interna_List, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Venta_Interna_List, "Ocurrió un error: ${e.message}");
                }
            }
        }else{
            MensajesDialog.showMessage(this@Venta_Interna_List,"Se debe de seleccionar un folio");
        }

    }

    private fun actualizarTableLayout(items: List<listDatosFolios>) {
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout_VentaI)

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
                    actualizarFilaSeleccionada(item)
                }

                tableLayout.addView(tableRow)
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun agregarFila(tableRow: TableRow, item: listDatosFolios) {
        try {
            val codigoTextView = TextView(this).apply {
                text = item.CODIGO
                gravity = Gravity.CENTER
            }

            val descripcionTextView = TextView(this).apply {
                text = item.DESCRIPCION
                gravity = Gravity.CENTER
                visibility = View.GONE
            }

            val cantidadTextView = TextView(this).apply {
                text = item.CANTIDAD.toString()
                gravity = Gravity.CENTER
            }

            val ubicacionTextView = TextView(this).apply {
                text = item.UBICACION
                gravity = Gravity.CENTER
            }

            val boxTextView = TextView(this).apply {
                text = item.BOX
                gravity = Gravity.CENTER
            }

            val itemTextView = TextView(this).apply {
                text = item.ITEM.toString()
                gravity = Gravity.CENTER
                visibility = View.GONE
            }

            val item_codigoReferencia=TextView(this).apply {
                text=item.CODIGO_REFERENCIA.toString()
                gravity=Gravity.CENTER
            }

            tableRow.addView(descripcionTextView)
            tableRow.addView(codigoTextView)
            tableRow.addView(cantidadTextView)
            tableRow.addView(ubicacionTextView)
            tableRow.addView(boxTextView)
            tableRow.addView(itemTextView)
            tableRow.addView(item_codigoReferencia)
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun actualizarFilaSeleccionada(item: listDatosFolios) {
        try {
            val editDescripcion = findViewById<TextView>(R.id.txtDescripcion_Venta_In_Pi)
            editDescripcion.text = item.DESCRIPCION

            this@Venta_Interna_List.Tcodigo=item.CODIGO;
            this@Venta_Interna_List.Tdescripcion=item.DESCRIPCION;
            this@Venta_Interna_List.Tubicacion=item.UBICACION;
            this@Venta_Interna_List.Tbox=item.BOX;
            this@Venta_Interna_List.Tcantidad=item.CANTIDAD.toString();
            this@Venta_Interna_List.item_producto=item.ITEM.toString();
            this@Venta_Interna_List.codigo_referencia=item.CODIGO_REFERENCIA;
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    override fun onBackPressed() {
        startActivity(Intent(this, Cedis_seleccion_menu::class.java));
        //LogsEntradaSalida.logsPorModulo( this, lifecycleScope, "PICKING/CEDIS/VENTA/INTERNA", "SALIDA");
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