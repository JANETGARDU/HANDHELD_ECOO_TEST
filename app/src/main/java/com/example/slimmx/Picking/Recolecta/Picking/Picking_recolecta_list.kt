package com.example.slimmx.Picking.Recolecta.Picking

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListaItemsPickingRecolecta
import com.example.slimmx.ListaPickingRecolecta
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Picking.Recolecta.Submenu_Picking_Recolecta
import com.example.slimmx.R
import com.example.slimmx.Submenus.Submenu_picking
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.example.slimmx.Vista.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Picking_recolecta_list : AppCompatActivity() {
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var cbSelectPiRecolecta: AutoCompleteTextView;
    private var filaSeleccionada: TableRow? = null

    private var token : String="";
    private var packId: String = "";
    private var codigo: String = "";
    private var descripcion: String = "";
    private var ubicacion: String = "";
    private var box: String = "";
    private var confirmado: String = "";
    private var almacenId: String = "";
    private var areaId: String = "";
    private var numeroGuia: String = "";
    private var ubicacionOriginal: String = "";
    private var boxOriginal: String = "";
    private var item: String = "";
    private var tipo:String="";
    private var codigo_Referencia:String="";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picking_recolecta_list)

        if (GlobalUser.nombre.isNullOrEmpty()) {
            MensajesDialogConfirmaciones.showMessage(
                this,
                "Ocurrio un error se cerrara la aplicacion, lamento el inconveniente"
            ) {
                finishAffinity()
            }
        }

        viewPager = findViewById(R.id.pager)
        tabLayout = findViewById(R.id.tab_layout)

        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPager.adapter = viewPagerAdapter

        tabLayout.setupWithViewPager(viewPager)


        token= GlobalUser.token.toString()

        cbSelectPiRecolecta = findViewById(R.id.cbSelectPiRecolecta)
        val txtCodigo=findViewById<EditText>(R.id.textCodigoPi_recolecta)
        val btnLimpiarCodigo=findViewById<Button>(R.id.buttonEliminar_codigo_Recolecta)
        val btnActualizar=findViewById<Button>(R.id.buttonRecargar_PICK_Recolecta)
        val btn_confirmar=findViewById<Button>(R.id.buttonOK_PICK_Envio)

        txtCodigo.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        //obtenerfolio();//Obtencion de folios

        val opciones = intent.getStringArrayExtra("folios")

        if (opciones != null && opciones.isNotEmpty()) {
            actualizarcbBox(opciones.toList()) // Llenar el ComboBox con los datos recibidos
        } else {
            // Si no hay datos, regresar al Submenu_acomodo
            val intent = Intent(this, Submenu_Picking_Recolecta::class.java)
            intent.putExtra("MESSAGE", "No hay tareas para este usuario")
            startActivity(intent)
            finish()
        }

        txtCodigo.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtCodigo.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        val codigoIngresado = inputText.toString().trim()
                        val encontrado = buscarCodigo(codigoIngresado)
                        if (!encontrado) {
                            MensajesDialog.showMessage(this@Picking_recolecta_list, "No se encontro ese Código")
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

        btnLimpiarCodigo.setOnClickListener {
            txtCodigo.text.clear();
            txtCodigo.post { txtCodigo.requestFocus() }
        }

        btnActualizar.setOnClickListener {
            try {
                val idSeleccionado = cbSelectPiRecolecta.text.toString()

                if (idSeleccionado.isNullOrEmpty()) {
                    MensajesDialog.showMessage(this,"No se ha seleccionado un ID.")
                    return@setOnClickListener
                }
                val tableLayout =
                    findViewById<TableLayout>(R.id.tableLayout_tareas_pick)
                if (tableLayout.childCount > 1) {
                    tableLayout.removeViews(1, tableLayout.childCount - 1)
                }
                optenerDatositemsTareas(idSeleccionado)
            }catch (e: Exception){
                MensajesDialog.showMessage(this@Picking_recolecta_list, "Ocurrió un error: ${e.message}");
            }
        }

        btn_confirmar.setOnClickListener {//Nos envia al activity CajasGeneradas
            try {
                val packId = cbSelectPiRecolecta.text.toString();
                Log.d("VALIDACIÓN", """
                    packId: '${packId}' -> ${packId.isNotEmpty()}
                    codigo: '${this@Picking_recolecta_list.codigo}' -> ${this@Picking_recolecta_list.codigo.isNotEmpty()}
                    descripcion: '${this@Picking_recolecta_list.descripcion}' -> ${this@Picking_recolecta_list.descripcion.isNotEmpty()}
                    ubicacion: '${this@Picking_recolecta_list.ubicacion}' -> ${this@Picking_recolecta_list.ubicacion.isNotEmpty()}
                    box: '${this@Picking_recolecta_list.box}' -> ${this@Picking_recolecta_list.box.isNotEmpty()}
                    confirmado: '${this@Picking_recolecta_list.confirmado}' -> ${this@Picking_recolecta_list.confirmado.isNotEmpty()}
                    almacenId: '${this@Picking_recolecta_list.almacenId}' -> ${this@Picking_recolecta_list.almacenId.isNotEmpty()}
                    areaId: '${this@Picking_recolecta_list.areaId}' -> ${this@Picking_recolecta_list.areaId.isNotEmpty()}
                    numeroGuia: '${this@Picking_recolecta_list.numeroGuia}' -> ${this@Picking_recolecta_list.numeroGuia.isNotEmpty()}
                    ubicacionOriginal: '${this@Picking_recolecta_list.ubicacionOriginal}' -> ${this@Picking_recolecta_list.ubicacionOriginal.isNotEmpty()}
                    boxOriginal: '${this@Picking_recolecta_list.boxOriginal}' -> ${this@Picking_recolecta_list.boxOriginal.isNotEmpty()}
                    item: '${this@Picking_recolecta_list.item}' -> ${this@Picking_recolecta_list.item.isNotEmpty()}
                """.trimIndent())
                if(packId.isNotEmpty() && this@Picking_recolecta_list.codigo.isNotEmpty() && this@Picking_recolecta_list.descripcion.isNotEmpty() && this@Picking_recolecta_list.ubicacion.isNotEmpty() &&
                    this@Picking_recolecta_list.box.isNotEmpty() && this@Picking_recolecta_list.confirmado.isNotEmpty() && this@Picking_recolecta_list.almacenId.isNotEmpty() && this@Picking_recolecta_list.areaId.isNotEmpty() &&
                    this@Picking_recolecta_list.numeroGuia.isNotEmpty() && this@Picking_recolecta_list.ubicacionOriginal.isNotEmpty() && this@Picking_recolecta_list.boxOriginal.isNotEmpty() && this@Picking_recolecta_list.item.isNotEmpty()
                ) {

                    val intent = Intent(this, Picking_recolecta_confirmacion::class.java);

                    intent.putExtra("packId", packId);
                    intent.putExtra("codigo", this@Picking_recolecta_list.codigo);
                    intent.putExtra("descripcion", this@Picking_recolecta_list.descripcion);
                    intent.putExtra("ubicacion", this@Picking_recolecta_list.ubicacion);
                    intent.putExtra("box", this@Picking_recolecta_list.box);
                    intent.putExtra("confirmado", this@Picking_recolecta_list.confirmado);
                    intent.putExtra("almacenId", this@Picking_recolecta_list.almacenId);
                    intent.putExtra("areaId", this@Picking_recolecta_list.areaId);
                    intent.putExtra("numeroGuia", this@Picking_recolecta_list.numeroGuia);
                    intent.putExtra("ubicacionOriginal", this@Picking_recolecta_list.ubicacionOriginal);
                    intent.putExtra("boxOriginal", this@Picking_recolecta_list.boxOriginal);
                    intent.putExtra("item", this@Picking_recolecta_list.item);
                    intent.putExtra("tipo",this@Picking_recolecta_list.tipo);
                    intent.putExtra("codigo_referencia", this@Picking_recolecta_list.codigo_Referencia);

                    startActivity(intent)
                }else{
                    MensajesDialog.showMessage(this, "Debes de seleccionar un producto")
                }


                this@Picking_recolecta_list.codigo = "";
                this@Picking_recolecta_list.descripcion = "";
                this@Picking_recolecta_list.ubicacion = "";
                this@Picking_recolecta_list.box = "";
                this@Picking_recolecta_list.confirmado = "";
                this@Picking_recolecta_list.almacenId = "";
                this@Picking_recolecta_list.areaId = "";
                this@Picking_recolecta_list.numeroGuia = "";
                this@Picking_recolecta_list.ubicacionOriginal = "";
                this@Picking_recolecta_list.boxOriginal = "";
                this@Picking_recolecta_list.item = "";
                this@Picking_recolecta_list.tipo="";
                this@Picking_recolecta_list.codigo_Referencia="";
            }catch (e: Exception){
                MensajesDialog.showMessage(this@Picking_recolecta_list, "Ocurrió un error: ${e.message}");
            }

        }

    }
    override fun onResume() {//Cuando regresa a primer plano el activity
        super.onResume()

        if (!cbSelectPiRecolecta.text.isNullOrEmpty()) {
            var btn_actualizar=findViewById<Button>(R.id.buttonRecargar_PICK_Recolecta)
            btn_actualizar.performClick()
            cbSelectPiRecolecta.requestFocus()
        }
        val txtCodigo=findViewById<EditText>(R.id.textCodigoPi_recolecta)
        txtCodigo.setText("");
        txtCodigo.post { txtCodigo.requestFocus() }
    }

    private fun obtenerfolio(){
        try {
            val headers = mapOf("Token" to GlobalUser.token.toString());
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/fulfillment/picking/works/paquetes",
                        params=emptyMap<String, String>(),
                        dataClass = ListaPickingRecolecta::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.WORK }
                                    actualizarcbBox(opciones);
                                } else {
                                    val intent = Intent(this@Picking_recolecta_list, Submenu_picking::class.java);
                                    intent.putExtra("MESSAGE", "No hay tareas para este usuario");
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        },
                        onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Picking_recolecta_list, "${error}");
                            }
                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Picking_recolecta_list, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Picking_recolecta_list, "Ocurrió un error: ${e.message}");
            }
        }

    }

    private fun actualizarcbBox(opciones: List<String>) {
        try {
            val adaptador = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                opciones
            )
            cbSelectPiRecolecta.setAdapter(adaptador)

            // Cuando se selecciona algun dato en el comboBox
            cbSelectPiRecolecta.setOnItemClickListener { _, _, position, _ ->
                val seleccion = opciones[position]
                val textCodigo=findViewById<EditText>(R.id.textCodigoPi_recolecta)
                val tableLayout = findViewById<TableLayout>(R.id.tableLayout_tareas_pick)

                if (tableLayout.childCount > 1) {
                    tableLayout.removeViews(1, tableLayout.childCount - 1)
                }
                val tableLayout_his = findViewById<TableLayout>(R.id.tableLayout_hist_pick)

                if (tableLayout_his.childCount > 1) {
                    tableLayout_his.removeViews(1, tableLayout_his.childCount - 1)
                }
                optenerDatositemsTareas(seleccion)//Metodo optencion de items
                textCodigo.post { textCodigo.requestFocus() }
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun optenerDatositemsTareas(folios: String){
        if (folios.isNotEmpty()){
            try {
                val params= mapOf(
                    "id" to folios
                );

                val params_pickeados= mapOf(
                    "id_paquete" to folios
                )

                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/fulfillment/picking/works/items/paquete",
                            params=params,
                            dataClass = ListaItemsPickingRecolecta::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val items = lista.map {
                                            ListaItemsPickingRecolecta(
                                                CODIGO = it.CODIGO,
                                                DESCRIPCION = it.DESCRIPCION,
                                                ALMACEN_ID = it.ALMACEN_ID,
                                                AREA_ID = it.AREA_ID,
                                                UBICACION = it.UBICACION,
                                                BOX = it.BOX,
                                                UNIDADES = it.UNIDADES,
                                                UNIDADES_CONFIRMADAS = it.UNIDADES_CONFIRMADAS,
                                                PAQUETE = it.PAQUETE,
                                                UBICACION_ORIGINAL = it.UBICACION_ORIGINAL,
                                                BOX_ORIGINAL = it.BOX_ORIGINAL,
                                                ITEM = it.ITEM,
                                                ECOMMERCE = it.ECOMMERCE,
                                                SHIPPING_STATUS = it.SHIPPING_STATUS,
                                                FECHA = it.FECHA,
                                                INDEX = it.INDEX,
                                                TIPO = it.TIPO,
                                                CODIGO_REFERENCIA=it.CODIGO_REFERENCIA
                                            )
                                        }

                                        actualizarDatosTablaTareas(items)

                                        val btnLimpiarCodigo=findViewById<Button>(R.id.buttonEliminar_codigo_Recolecta)
                                        btnLimpiarCodigo.performClick();

                                    } else {
                                        val tableLayout =
                                            findViewById<TableLayout>(R.id.tableLayout_tareas_pick)
                                        if (tableLayout.childCount > 1) {
                                            tableLayout.removeViews(1, tableLayout.childCount - 1)
                                        }
                                        val intent =
                                            Intent(this@Picking_recolecta_list, Submenu_Picking_Recolecta::class.java);
                                        intent.putExtra("MESSAGE", "Tarea concluida");
                                        startActivity(intent);
                                        finish();
                                    }

                                }
                            },
                            onError = {error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Picking_recolecta_list, "Error: $error");
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Picking_recolecta_list, "Ocurrió un error: ${e.message}")
                        }
                    }
                }
                lifecycleScope.launch(Dispatchers.Main) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/fulfillment/picking/works/items/paquete/pickeados",
                            params=params_pickeados,
                            dataClass = ListaItemsPickingRecolecta::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val items = lista.map {
                                            ListaItemsPickingRecolecta(
                                                CODIGO = it.CODIGO,
                                                DESCRIPCION = it.DESCRIPCION,
                                                ALMACEN_ID = it.ALMACEN_ID,
                                                AREA_ID = it.AREA_ID,
                                                UBICACION = it.UBICACION,
                                                BOX = it.BOX,
                                                UNIDADES = it.UNIDADES,
                                                UNIDADES_CONFIRMADAS = it.UNIDADES_CONFIRMADAS,
                                                PAQUETE = it.PAQUETE,
                                                UBICACION_ORIGINAL = it.UBICACION_ORIGINAL,
                                                BOX_ORIGINAL = it.BOX_ORIGINAL,
                                                ITEM = it.ITEM,
                                                ECOMMERCE = it.ECOMMERCE,
                                                SHIPPING_STATUS = it.SHIPPING_STATUS,
                                                FECHA = it.FECHA,
                                                INDEX = it.INDEX,
                                                TIPO = "",
                                                CODIGO_REFERENCIA = ""
                                            )
                                        }
                                        actualizarDatosTablaHistorial(items)
                                    }
                                }
                            },
                            onError = {error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Picking_recolecta_list, "Error: $error")
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Picking_recolecta_list, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Picking_recolecta_list, "Ocurrió un error: ${e.message}");
                }
            }

        }else{
            MensajesDialog.showMessage(this,"Debes de seleccionar un folio")
        }
    }

    private fun actualizarDatosTablaTareas(items: List<ListaItemsPickingRecolecta>){
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout_tareas_pick)

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

                    actualizarInformacionSeleccionadaTareas(item)//Actualizar las label al seleccionar una nueva fila
                }

                tableLayout.addView(tableRow)
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun agregarFila(tableRow: TableRow, item: ListaItemsPickingRecolecta) { //Agregar los datos encontrados en la tabla
        // Crear TextViews y añadirlos a la fila
        try {
            val descripcionText = TextView(this).apply {
                text = item.DESCRIPCION;
                visibility= View.GONE;
            }
            val Itemtext = TextView(this).apply {
                text = item.ITEM.toString();
            }
            val codigoText = TextView(this).apply {
                text = item.CODIGO;
                gravity= Gravity.CENTER;
            }
            val paqueteText = TextView(this).apply {
                text = item.PAQUETE;
                visibility= View.GONE;
            }
            val ubicacionText = TextView(this).apply {
                text = item.UBICACION;
                gravity = Gravity.CENTER;
            }
            val boxText = TextView(this).apply {
                text = item.BOX;
                gravity = Gravity.CENTER;
            }
            val cantidadText = TextView(this).apply {
                text = item.UNIDADES.toString();
                gravity = Gravity.CENTER;
            }

            val almacenText = TextView(this).apply {
                text = item.ALMACEN_ID.toString();
                gravity = Gravity.CENTER;
                visibility= View.GONE;
            }
            val areaText = TextView(this).apply {
                text = item.AREA_ID.toString();
                gravity = Gravity.CENTER;
                visibility= View.GONE;
            }
            val ubicacionOrigenText = TextView(this).apply {
                text = item.UBICACION_ORIGINAL;
                gravity = Gravity.CENTER;
                visibility= View.GONE;
            }
            val boxOrigenText = TextView(this).apply {
                text = item.BOX_ORIGINAL;
                gravity = Gravity.CENTER;
                visibility= View.GONE;
            }
            val itemText = TextView(this).apply {
                text = item.ITEM.toString();
                gravity = Gravity.CENTER;
            }
            val fechaText = TextView(this).apply {
                text = item.FECHA;
                gravity = Gravity.CENTER;
            }
            val tipo=TextView(this).apply {
                text=item.TIPO;
                visibility=View.GONE;
            }

            val codigoReferencia=TextView(this).apply {
                text=item.CODIGO_REFERENCIA;
                gravity = Gravity.CENTER;
            }


            // Añadir los TextViews a la TableRow
            tableRow.addView(descripcionText)
            tableRow.addView(Itemtext)
            tableRow.addView(codigoText)
            tableRow.addView(paqueteText)
            tableRow.addView(ubicacionText)
            tableRow.addView(boxText)
            tableRow.addView(cantidadText)
            tableRow.addView(almacenText)
            tableRow.addView(areaText)
            tableRow.addView(ubicacionOrigenText)
            tableRow.addView(boxOrigenText)
            tableRow.addView(itemText)
            tableRow.addView(fechaText)
            tableRow.addView(tipo)
            tableRow.addView(codigoReferencia)
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun actualizarInformacionSeleccionadaTareas(item: ListaItemsPickingRecolecta) { //De la fila seleccionada colocar los datos en las textview
        try {
            val lbdescripcion = findViewById<TextView>(R.id.textDescripcionPi_recolecta)
            lbdescripcion.text = "${item.DESCRIPCION}";
            this@Picking_recolecta_list.codigo="${item.CODIGO}"
            this@Picking_recolecta_list.descripcion="${item.DESCRIPCION}"
            this@Picking_recolecta_list.ubicacion="${item.UBICACION}"
            this@Picking_recolecta_list.box="${item.BOX}"
            this@Picking_recolecta_list.confirmado="${item.UNIDADES}"
            this@Picking_recolecta_list.almacenId="${item.ALMACEN_ID}"
            this@Picking_recolecta_list.areaId="${item.AREA_ID}"
            this@Picking_recolecta_list.numeroGuia="${item.PAQUETE}"
            this@Picking_recolecta_list.ubicacionOriginal="${item.UBICACION_ORIGINAL}"
            this@Picking_recolecta_list.boxOriginal="${item.BOX_ORIGINAL}"
            this@Picking_recolecta_list.item="${item.ITEM}";
            this@Picking_recolecta_list.tipo="${item.TIPO}";
            this@Picking_recolecta_list.codigo_Referencia="${item.CODIGO_REFERENCIA}";

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun actualizarDatosTablaHistorial(items: List<ListaItemsPickingRecolecta>){
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout_hist_pick)

            if (tableLayout.childCount > 1) {
                tableLayout.removeViews(1, tableLayout.childCount - 1)
            }

            for (item in items) {
                val tableRow = TableRow(this);
                agregarFilaHistorial(tableRow, item);

                tableRow.setOnClickListener {
                    filaSeleccionada?.setBackgroundColor(Color.TRANSPARENT)

                    //Solo cambia de color la fila que se selecciono
                    tableRow.setBackgroundColor(Color.parseColor("#639A67"))

                    // Actualiza las filas para el control de los colores
                    filaSeleccionada = tableRow

                    actualizarInformacionSeleccionadaHistorial(item)//Actualizar las label al seleccionar una nueva fila
                }

                tableLayout.addView(tableRow)
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun agregarFilaHistorial(tableRow: TableRow, item: ListaItemsPickingRecolecta) { //Agregar los datos encontrados en la tabla del historial
        try {
            val codigoText = TextView(this).apply {
                text = item.CODIGO;
                gravity= Gravity.CENTER;
            }

            val cantidadText = TextView(this).apply {
                text = item.UNIDADES.toString();
                gravity = Gravity.CENTER;
            }

            val descripcionText = TextView(this).apply {
                text = item.DESCRIPCION;
                visibility= View.GONE;
            }

            tableRow.addView(codigoText)
            tableRow.addView(cantidadText)
            tableRow.addView(descripcionText)
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }
    private fun actualizarInformacionSeleccionadaHistorial(item: ListaItemsPickingRecolecta) { //De la fila seleccionada colocar los datos en las textview
        try {
            val lbdescripcion = findViewById<TextView>(R.id.textDescripcionPi_recolecta)
            lbdescripcion.text = "${item.DESCRIPCION}";
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }


    fun buscarCodigo(codigoOubicacion: String): Boolean {
        val otroLayout = findViewById<LinearLayout>(R.id.linear_pickingtareas)
        val tableLayout = otroLayout.findViewById<TableLayout>(R.id.tableLayout_tareas_pick)
            ?: return false // Si no encuentra la tabla, salir.

        val rowCount = tableLayout.childCount
        try {
            for (i in 0 until rowCount) {
                val fila = tableLayout.getChildAt(i) as? TableRow ?: continue

                val columnaCodigo = fila.getChildAt(2) as? TextView
                val columnaUbiOrigen = fila.getChildAt(4) as? TextView
                val columnaCodigoReferencia=fila.getChildAt(14)as? TextView

                val textoCodigo = columnaCodigo?.text?.toString()?.trim()
                val textoUbiOrigen = columnaUbiOrigen?.text?.toString()?.trim()
                val textoCodigoRefe=columnaCodigoReferencia?.text?.toString()?.trim();

                Log.d("DEBUG", "Fila $i: Código = '$textoCodigo', Ubicación Origen = '$textoUbiOrigen', Buscado = '$codigoOubicacion'")


                if (textoCodigo.equals(codigoOubicacion, ignoreCase = true) ||
                    textoUbiOrigen.equals(codigoOubicacion, ignoreCase = true) ||
                    textoCodigoRefe.equals(codigoOubicacion, ignoreCase = true)) {

                    fila.setBackgroundColor(Color.parseColor("#639A67"))
                    Log.d("DEBUG", "Coincidencia encontrada en la fila $i")

                    val descripcion=(fila.getChildAt(0) as? TextView)?.text.toString();
                    val index=(fila.getChildAt(1) as? TextView)?.text.toString();
                    val codigo2=(fila.getChildAt(2) as? TextView)?.text.toString();
                    val paquete=(fila.getChildAt(3) as? TextView)?.text.toString();
                    val ubicacion=(fila.getChildAt(4) as? TextView)?.text.toString();
                    val box=(fila.getChildAt(5) as? TextView)?.text.toString();
                    val cantidad_unidades=(fila.getChildAt(6) as? TextView)?.text.toString();
                    val almacen=(fila.getChildAt(7) as? TextView)?.text.toString();
                    val area=(fila.getChildAt(8) as? TextView)?.text.toString();
                    val ubi_origen=(fila.getChildAt(9) as? TextView)?.text.toString();
                    val box_origen=(fila.getChildAt(10) as? TextView)?.text.toString();
                    val item_fila=(fila.getChildAt(11) as? TextView)?.text.toString();
                    val fecha=(fila.getChildAt(12) as? TextView)?.text.toString();
                    val tipo=(fila.getChildAt(13)as? TextView)?.text.toString();
                    val codigoReferencia=(fila.getChildAt(14)as? TextView)?.text.toString();

                    val item =  ListaItemsPickingRecolecta(
                        codigo2,
                        descripcion,
                        almacen.toInt(),
                        area.toInt(),
                        ubicacion,
                        box,
                        cantidad_unidades.toInt(),
                        0,
                        paquete,
                        ubi_origen,
                        box_origen,
                        item_fila.toInt(),
                        "",
                        "",
                        fecha.toString(),
                        index.toInt(),
                        tipo,
                        codigoReferencia
                    )
                    Log.d("DEBUG", "Paquete recuperado: '$paquete'");
                    actualizarInformacionSeleccionadaTareas(item)

                    return true
                }
            }
        } catch (e: Exception) {
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}")
        }

        return false
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_Picking_Recolecta::class.java));
        //LogsEntradaSalida.logsPorModulo( this@Picking_recolecta_list, lifecycleScope, "PICKING/RECOLECTA/TAREA", "SALIDA");
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