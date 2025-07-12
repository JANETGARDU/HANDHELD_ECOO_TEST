package com.example.slimmx.Reacomodo.Tareas

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.Acomodo.Acomodo_confirmacion
import com.example.slimmx.GlobalConfig
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListExistenciaReacomodo
import com.example.slimmx.ListItemsAcomodo
import com.example.slimmx.ListProducto
import com.example.slimmx.ListaCheckReacomodo
import com.example.slimmx.ListaDataCheckReacomodo
import com.example.slimmx.ListaFolioPickingEnvio
import com.example.slimmx.ListaTareasReacomodo
import com.example.slimmx.ListaTareasReacomodo_buscarfolios
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.R
import com.example.slimmx.ResultadoLista
import com.example.slimmx.Submenus.Submenu_acomodo
import com.example.slimmx.Submenus.Submenu_reacomodo
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.example.slimmx.listaDatosRespuesta
import com.example.slimmx.listaDatosRespuesta_faltantes
import com.example.slimmx.listaRespuestaDataReacomodo
import com.example.slimmx.listaRespuestaData_faltantes
import com.example.slimmx.lista_productos_valid
import com.example.slimmx.productosPacks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Reacomodo_Check : AppCompatActivity() {

    private lateinit var cbSelectReacomodoCheckList: AutoCompleteTextView;
    private lateinit var txtCodigoSearch:EditText;
    private var filaSeleccionada: TableRow? = null
    private lateinit var Frame_faltantes:FrameLayout;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reacomodo_check)
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

        cbSelectReacomodoCheckList = findViewById(R.id.cbSelectReacomodoCheckList);
        txtCodigoSearch=findViewById(R.id.editTextCodigo_ReacomodoCheck_list);
        Frame_faltantes=findViewById(R.id.Frame_faltantes);
        val btn_eliminarCodigo=findViewById<Button>(R.id.buttonEliminar_codigo_ReacomodoCheck);
        val btn_ver_faltantes=findViewById<Button>(R.id.buttonVer_faltantes);
        val btn_cerrar_faltantes=findViewById<Button>(R.id.buttonCancelar_check_faltantes);
        val btn_verificar=findViewById<Button>(R.id.buttonVerificar_piezas);
        val frameBlocker = findViewById<FrameLayout>(R.id.background_blocker);
        Frame_faltantes.isVisible=false;

        // buscarFolios();

        val opciones = intent.getStringArrayExtra("folios")

        if (opciones != null && opciones.isNotEmpty()) {
            actualizarcbBox(opciones.toList()) // Llenar el ComboBox con los datos recibidos
        } else {
            // Si no hay datos, regresar al Submenu_acomodo
            val intent = Intent(this, Submenu_reacomodo::class.java)
            intent.putExtra("MESSAGE", "No hay tareas para este usuario")
            startActivity(intent)
            finish()
        }

        txtCodigoSearch.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        txtCodigoSearch.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtCodigoSearch.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        verificarCodigo(txtCodigoSearch.text.toString(), cbSelectReacomodoCheckList.text.toString());
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }

        btn_eliminarCodigo.setOnClickListener {
            txtCodigoSearch.setText("");
        }

        btn_ver_faltantes.setOnClickListener {
            try {
                frameBlocker.isVisible = true;
                Frame_faltantes.isVisible = true;

                frameBlocker.bringToFront();
                Frame_faltantes.bringToFront();
                buscarFaltantes(cbSelectReacomodoCheckList.text.toString());
            }catch (e: Exception){
                MensajesDialog.showMessage(this@Reacomodo_Check, "Ocurrió un error: ${e.message}");
            }

        }

        btn_cerrar_faltantes.setOnClickListener {
            frameBlocker.isVisible = false;
            Frame_faltantes.isVisible = false;
        }

        btn_verificar.setOnClickListener {
            verificar(cbSelectReacomodoCheckList.text.toString());
        }

    }

    private fun buscarFolios(){
        try {
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/inventario/tareas/reubicacion/check/list",
                        params=emptyMap<String, String>(),
                        dataClass = ListaTareasReacomodo_buscarfolios::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.ID }
                                    actualizarcbBox(opciones);
                                } else {
                                    val intent = Intent(this@Reacomodo_Check, Submenu_reacomodo::class.java);
                                    intent.putExtra("MESSAGE", "No hay tareas para este usuario");
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Reacomodo_Check, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Reacomodo_Check, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun actualizarcbBox(opciones: List<String>) {
        try {
            val adaptador = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                opciones
            )
            cbSelectReacomodoCheckList.setAdapter(adaptador)

            cbSelectReacomodoCheckList.setOnItemClickListener { _, _, position, _ ->
                val seleccion = opciones[position]
                txtCodigoSearch.post { txtCodigoSearch.requestFocus() }
                val tableLayout = findViewById<TableLayout>(R.id.tableLayout_ReacomodoCheck)

                if (tableLayout.childCount > 1) {
                    tableLayout.removeViews(1, tableLayout.childCount - 1)
                }

                val tableLayout_faltante = findViewById<TableLayout>(R.id.tableLayout_ReacomodoCheck_faltantes)

                if (tableLayout_faltante.childCount > 1) {
                    tableLayout_faltante.removeViews(1, tableLayout_faltante.childCount - 1)
                }
                verificar(seleccion);
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun verificarCodigo(codigo:String, folio:String){
        try {
            val params= mapOf(
                "codigo" to codigo.uppercase()
            )

            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/productos/codigo/valid",
                        params=params,
                        dataClass = lista_productos_valid::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    verificarExistencia(codigo, folio);
                                } else {
                                    MensajesDialog.showMessage(this@Reacomodo_Check, "Producto inexistente");
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Reacomodo_Check, "${error}");
                            }
                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Reacomodo_Check, "Ocurrió un error: ${e.message}")
                    }
                }
            }
        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Reacomodo_Check, "Ocurrió un error: ${e.message}");
            }
        }
    }

    private fun verificarExistencia(codigo:String, folio:String){
        try {
            val params= mapOf(
                "folio" to folio,
                "codigo" to codigo.uppercase()
            )

            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/inventario/tareas/reubicacion/checklist/codigo/existente",
                        params=params,
                        dataClass = listaRespuestaDataReacomodo::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    if (lista.map { it.message }.contains("EXISTENTE")) {
                                        verificarItems(codigo, folio);
                                    }
                                } else {
                                    MensajesDialog.showMessage(
                                        this@Reacomodo_Check,
                                        "Ese código no pertenece a el reacomodo (${codigo})"
                                    );
                                    txtCodigoSearch.setText("");
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Reacomodo_Check, "${error}");
                            }
                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Reacomodo_Check, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Reacomodo_Check, "Ocurrió un error: ${e.message}");
            }
        }
    }

    private fun verificarItems(codigo:String, folio:String){
        try {
            val body= mapOf(
                "folio" to folio,
                "codigo" to codigo.uppercase()
            )
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );

            lifecycleScope.launch {
                Pedir_datos_apis_post(
                    endpoint = "/inventario/tareas/reubicacion/checklist",
                    body=body,
                    dataClass = ResultadoLista::class,
                    listaKey = "result",
                    headers = headers,
                    onSuccess = {resultado: ResultadoLista ->
                        val items = resultado.data
                        if (items.isNotEmpty()) {
                            actualizarTableLayout(items)
                        } else {
                            MensajesDialog.showMessage(
                                    this@Reacomodo_Check,
                                    "LA CANTIDAD ES MAYOR A LA PROPORCIONADA\nREVISE LAS PIEZAS (${codigo})"
                            )
                        }

                    }, onError = {error->
                        MensajesDialog.showMessage(this@Reacomodo_Check,"${error}");
                    }
                )
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun actualizarTableLayout(items: List<ListaCheckReacomodo>) {
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout_ReacomodoCheck)

            if (tableLayout.childCount > 1) {
                tableLayout.removeViews(1, tableLayout.childCount - 1)
            }

            for (item in items) {
                val tableRow = TableRow(this)
                agregarFila(tableRow, item)

                tableRow.setOnClickListener {
                    filaSeleccionada?.let {
                        it.setBackgroundColor(Color.TRANSPARENT)
                    }

                    tableRow.setBackgroundColor(Color.parseColor("#639A67"))
                    filaSeleccionada = tableRow

                }

                // Añade la fila a la tabla
                tableLayout.addView(tableRow)
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun agregarFila(tableRow: TableRow, item: ListaCheckReacomodo) {
        try {
            val codigo= TextView(this).apply {
                text=item.CODIGO;
                gravity= Gravity.CENTER
            }
            val existencia= TextView(this).apply {
                text=item.CANTIDAD_CHECK.toString();
                gravity= Gravity.CENTER
            }

            tableRow.addView(codigo);
            tableRow.addView(existencia);
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun buscarFaltantes(folio: String){
        try {
            val params= mapOf(
                "folio" to folio
            );

            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/inventario/tareas/reubicacion/checklist/faltantes",
                        params=params,
                        dataClass = listaRespuestaData_faltantes::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val items = lista.map {
                                        productosPacks(
                                            CODIGO = it.CODIGO,
                                            CANTIDAD = it.CANTIDAD,
                                            DESCRIPCION = it.DESCRIPCION
                                        )

                                    }
                                    actualizarTableLayout_Faltantes(items);
                                    txtCodigoSearch.setText("");
                                } else {
                                    MensajesDialog.showMessage(
                                        this@Reacomodo_Check,
                                        "TODAS LAS UNIDADES HAN SIDO VERIFICADAS :D"
                                    );
                                    txtCodigoSearch.setText("");
                                }

                            }
                        }, onError = { error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Reacomodo_Check, "Error: $error");
                            }
                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Reacomodo_Check, "Ocurrió un error: ${e.message}")
                    }
                }
            }
        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Reacomodo_Check, "Ocurrió un error: ${e.message}");
            }
        }
    }


    private fun actualizarTableLayout_Faltantes(items: List<productosPacks>) {
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout_ReacomodoCheck_faltantes)

            if (tableLayout.childCount > 1) {
                tableLayout.removeViews(1, tableLayout.childCount - 1)
            }

            for (item in items) {
                val tableRow = TableRow(this)
                agregarFila_faltantes(tableRow, item)

                tableRow.setOnClickListener {
                    filaSeleccionada?.let {
                        it.setBackgroundColor(Color.TRANSPARENT)
                    }

                    tableRow.setBackgroundColor(Color.parseColor("#639A67"))
                    filaSeleccionada = tableRow
                    actualizarFilaSeleccionada(item)

                }

                // Añade la fila a la tabla
                tableLayout.addView(tableRow)
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun agregarFila_faltantes(tableRow: TableRow, item: productosPacks) {
        try {
            val codigo= TextView(this).apply {
                text=item.CODIGO;
                gravity= Gravity.CENTER
            }
            val existencia= TextView(this).apply {
                text=item.CANTIDAD.toString();
                gravity= Gravity.CENTER
            }
            val descripcion= TextView(this).apply {
                text=item.CANTIDAD.toString();
                visibility=View.GONE;
            }

            tableRow.addView(codigo);
            tableRow.addView(existencia);
            tableRow.addView(descripcion);
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun actualizarFilaSeleccionada(item: productosPacks) {
        try {
            val editDescripcion = findViewById<TextView>(R.id.textDescripcionCheck_Faltantes)
            editDescripcion.text = item.DESCRIPCION
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun verificar(folio: String){
        try {
            val params= mapOf(
                "folio" to folio
            )
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/inventario/tareas/reubicacion/list/check",
                        params=params,
                        dataClass = ListaTareasReacomodo::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    if (cbSelectReacomodoCheckList.text.toString().isNotEmpty()) {
                                        val intent = Intent(this@Reacomodo_Check, Tarea_reacomodo_list::class.java)

                                        // Envía los valores correctos
                                        intent.putExtra(
                                            "id",
                                            cbSelectReacomodoCheckList.text.toString()
                                        );

                                        startActivity(intent)

                                        txtCodigoSearch.setText("");
                                    } else {
                                        MensajesDialog.showMessage(
                                            this@Reacomodo_Check,
                                            "No se ha seleccionado ningún folio"
                                        );
                                    }

                                } else {
                                    MensajesDialog.showMessage(
                                        this@Reacomodo_Check,
                                        "REACOMODO SELECCIONADO:\n ${cbSelectReacomodoCheckList.text.toString()}"
                                    );
                                }
                            }
                        }, onError = {error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Reacomodo_Check, "Error: $error");
                            }
                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Reacomodo_Check, "Ocurrió un error: ${e.message}")
                    }
                }
            }
        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Reacomodo_Check, "Ocurrió un error: ${e.message}");
            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_reacomodo::class.java));
        //LogsEntradaSalida.logsPorModulo(this, lifecycleScope, "ACOMODO/REACOMODO/TAREA", "SALIDA")
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