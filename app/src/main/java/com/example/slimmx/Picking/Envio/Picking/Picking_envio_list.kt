package com.example.slimmx.Picking.Envio.Picking

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
import android.widget.CheckBox
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
import com.example.slimmx.ListaFolioPickingEnvio
import com.example.slimmx.ListaItemsPi_Envio
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Picking.Envio.Submenu_Picking_Envio
import com.example.slimmx.R
import com.example.slimmx.Submenus.Submenu_picking
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Picking_envio_list : AppCompatActivity() {

    private lateinit var cbSelectPickEnvios: AutoCompleteTextView;
    private var token : String=""
    private var filaSeleccionada: TableRow? = null

    private var item_producto:String="";
    private var Tcodigo: String="";
    private var Tdescripcion: String="";
    private var Tubicacion:String="";
    private var Tbox:String="";
    private var Tcantidad:String="";
    private var p_tipo:String="";
    private var codigo_referencia:String="";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_picking_envio_list)
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

        token= GlobalUser.token.toString()
        var lb_usuario=findViewById<TextView>(R.id.envio_picking)
        var txtCodigo=findViewById<EditText>(R.id.editTextTextCodigo_pi_en)
        cbSelectPickEnvios = findViewById(R.id.cbSelectPickEnvios)
        var btn_actualizar=findViewById<Button>(R.id.buttonRecargar_PICK_Envio)
        var btnLimpiarCodigo=findViewById<Button>(R.id.buttonEliminar_PICK_Envio)
        val tableLayout=findViewById<TableLayout>(R.id.tableLayout)
        val check_codigo=findViewById<CheckBox>(R.id.checkBox_Pi_Envio)
        val btn_confirmar=findViewById<Button>(R.id.buttonOK_PICK_Envio)
        lb_usuario.setText("Usuario: "+GlobalUser.nombre)

        txtCodigo.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        //obtenerFolio();

        val opciones = intent.getStringArrayExtra("folios")

        if (opciones != null && opciones.isNotEmpty()) {
            actualizarcbBox(opciones.toList()) // Llenar el ComboBox con los datos recibidos
        } else {
            // Si no hay datos, regresar al Submenu_acomodo
            val intent = Intent(this, Submenu_Picking_Envio::class.java)
            intent.putExtra("MESSAGE", "No hay tareas para este usuario")
            startActivity(intent)
            finish()
        }

        cbSelectPickEnvios.requestFocus();
        val idSeleccionado = cbSelectPickEnvios.text.toString()
        if (idSeleccionado.isNotEmpty()) {
            btn_actualizar.performClick()
        }



        btn_actualizar.setOnClickListener {
            try {
                val idSeleccionado = cbSelectPickEnvios.text.toString()

                if (idSeleccionado.isNullOrEmpty()) {
                    MensajesDialog.showMessage(this,"No se ha seleccionado un ID.")
                    return@setOnClickListener
                }
                optenerDatositems(idSeleccionado)
            }catch (e: Exception){
                MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
            }
        }

        btnLimpiarCodigo.setOnClickListener {
            txtCodigo.text.clear();
            txtCodigo.post { txtCodigo.requestFocus() }
            check_codigo.isChecked=false;
        }

        txtCodigo.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtCodigo.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        val codigoIngresado = inputText.toString().trim()
                        val encontrado = buscarCodigo(codigoIngresado, tableLayout)
                        if (!encontrado) {
                            MensajesDialog.showMessage(this@Picking_envio_list, "No se encontro ese Código o esa Ubicación")
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

        btn_confirmar.setOnClickListener {//Nos envia al activity CajasGeneradas
            try {
                val idSeleccionado = cbSelectPickEnvios.text.toString()
                val intent = Intent(this, Picking_envio_confirmacion::class.java)

                if (this@Picking_envio_list.Tcodigo.isNotEmpty()){
                    intent.putExtra("folio", idSeleccionado);
                    intent.putExtra("codigo", this@Picking_envio_list.Tcodigo);
                    intent.putExtra("descripcion", this@Picking_envio_list.Tdescripcion);
                    intent.putExtra("ubicacion", this@Picking_envio_list.Tubicacion);
                    intent.putExtra("box", this@Picking_envio_list.Tbox);
                    intent.putExtra("cantidad", this@Picking_envio_list.Tcantidad);
                    intent.putExtra("item_producto", this@Picking_envio_list.item_producto);
                    intent.putExtra("tipo",this@Picking_envio_list.p_tipo);
                    intent.putExtra("codigo_referencia", this@Picking_envio_list.codigo_referencia);

                    startActivity(intent)

                    this@Picking_envio_list.Tcodigo="";
                    this@Picking_envio_list.Tdescripcion="";
                    this@Picking_envio_list.Tubicacion="";
                    this@Picking_envio_list.Tbox="";
                    this@Picking_envio_list.Tcantidad="";
                    this@Picking_envio_list.item_producto="";
                    this@Picking_envio_list.p_tipo="";
                    this@Picking_envio_list.codigo_referencia="";
                }else{
                    MensajesDialog.showMessage(this, "No se seleccionaron correctamente los datos");
                }


            }catch (e: Exception){
                MensajesDialog.showMessage(this@Picking_envio_list, "Ocurrió un error: ${e.message}");
            }
        }

    }
    override fun onResume() {//Cuando regresa a primer plano el activity
        super.onResume()

        if (!cbSelectPickEnvios.text.isNullOrEmpty()) {
            var btn_actualizar=findViewById<Button>(R.id.buttonRecargar_PICK_Envio)
            btn_actualizar.performClick()
            cbSelectPickEnvios.requestFocus()
        }
    }

    private fun obtenerFolio(){
        try {
            val params= mapOf(
                "prefijo" to ""
            );

            val headers = mapOf("Token" to GlobalUser.token.toString());

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/inventario/tareas/picking",
                        params=params,
                        dataClass = ListaFolioPickingEnvio::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.ID }
                                    actualizarcbBox(opciones);
                                } else {
                                    val intent = Intent(this@Picking_envio_list, Submenu_picking::class.java);
                                    intent.putExtra("MESSAGE", "No hay tareas para este usuario");
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        },
                        onError = { error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Picking_envio_list, "Error: $error");
                            }
                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Picking_envio_list, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Picking_envio_list, "Ocurrió un error: ${e.message}");
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
            cbSelectPickEnvios.setAdapter(adaptador)

            // Cuando se selecciona algun dato en el comboBox
            cbSelectPickEnvios.setOnItemClickListener { _, _, position, _ ->
                val seleccion = opciones[position]
                val textCodigo=findViewById<EditText>(R.id.editTextTextCodigo_pi_en)
                val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

                if (tableLayout.childCount > 1) {
                    tableLayout.removeViews(1, tableLayout.childCount - 1)
                }
                optenerDatositems(seleccion)//Metodo optencion de item
                textCodigo.post { textCodigo.requestFocus() }
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
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
                    try {
                        Pedir_datos_apis(
                            endpoint = "/inventario/tareas/picking/items",
                            params=params,
                            dataClass = ListaItemsPi_Envio::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val items = lista.map {
                                            ListaItemsPi_Envio(
                                                ALMACEN_ID = it.ALMACEN_ID,
                                                AREA_ID = it.AREA_ID,
                                                UBICACION_ORIGEN = it.UBICACION_ORIGEN,
                                                BOX_ORIGEN = it.BOX_ORIGEN,
                                                UBICACION_DESTINO = it.UBICACION_DESTINO,
                                                BOX_DESTINO = it.BOX_DESTINO,
                                                CANTIDAD = it.CANTIDAD,
                                                CODIGO = it.CODIGO,
                                                DESCRIPCION = it.DESCRIPCION,
                                                ITEM = it.ITEM,
                                                CANTIDAD_CONFIRMADA = it.CANTIDAD_CONFIRMADA,
                                                TIPO=it.TIPO,
                                                CODIGO_REFERENCIA=it.CODIGO_REFERENCIA
                                            )
                                        }

                                        actualizarDatosTabla(items);
                                        var btnLimpiarCodigo=findViewById<Button>(R.id.buttonEliminar_PICK_Envio)

                                        btnLimpiarCodigo.performClick();

                                    } else {
                                        val tableLayout =
                                            findViewById<TableLayout>(R.id.tableLayout)
                                        if (tableLayout.childCount > 1) {
                                            tableLayout.removeViews(1, tableLayout.childCount - 1)
                                        }
                                        val intent =
                                            Intent(this@Picking_envio_list, Submenu_Picking_Envio::class.java);
                                        intent.putExtra("MESSAGE", "Tarea concluida");
                                        startActivity(intent);
                                        finish();
                                    }

                                }
                            },
                            onError = {error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Picking_envio_list, "Error: $error");
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Picking_envio_list, "Ocurrió un error: ${e.message}")
                        }
                    }
                }
            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Picking_envio_list, "Ocurrió un error: ${e.message}");
                }
            }
        }else{
            MensajesDialog.showMessage(this,"Debes de seleccionar un folio")
        }

    }

    private fun actualizarDatosTabla(items: List<ListaItemsPi_Envio>){
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

                    actualizarInformacionSeleccionada(item)//Actualizar las label al seleccionar una nueva fila
                }

                tableLayout.addView(tableRow)
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun agregarFila(tableRow: TableRow, item: ListaItemsPi_Envio) { //Agregar los datos encontrados en la tabla
        // Crear TextViews y añadirlos a la fila
        try {
            val descripcionText = TextView(this).apply {
                text = item.DESCRIPCION;
                visibility= View.GONE;
            }
            val codigoText = TextView(this).apply {
                text = item.CODIGO;
                gravity=Gravity.CENTER;
            }
            val cantidadText = TextView(this).apply {
                text = item.CANTIDAD.toString();
                gravity = Gravity.CENTER;
            }
            val ubicacionOrigenText = TextView(this).apply {
                text = item.UBICACION_ORIGEN;
                gravity = Gravity.CENTER;
            }
            val boxOrigenText = TextView(this).apply {
                text = item.BOX_ORIGEN;
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
            val cantidadConfirmText = TextView(this).apply {
                text = item.CANTIDAD_CONFIRMADA.toString();
                gravity = Gravity.CENTER;
            }
            val itemText = TextView(this).apply {
                text = item.ITEM.toString();
                gravity = Gravity.CENTER;
                visibility= View.GONE;
            }
            val ubicacionDestinoText = TextView(this).apply {
                text = item.UBICACION_DESTINO;
                gravity = Gravity.CENTER;
                visibility= View.GONE;
            }
            val boxDestinoText = TextView(this).apply {
                text = item.BOX_DESTINO;
                gravity = Gravity.CENTER;
                visibility= View.GONE;
            }
            val tipo = TextView(this).apply {
                text = item.TIPO;
                gravity = Gravity.CENTER;
                visibility= View.GONE;
            }
            val codigo_referencia=TextView(this).apply {
                text=item.CODIGO_REFERENCIA;
                gravity = Gravity.CENTER;
            }

            // Añadir los TextViews a la TableRow
            tableRow.addView(descripcionText)
            tableRow.addView(codigoText)
            tableRow.addView(cantidadText)
            tableRow.addView(ubicacionOrigenText)
            tableRow.addView(boxOrigenText)
            tableRow.addView(almacenText)
            tableRow.addView(areaText)
            tableRow.addView(cantidadConfirmText)
            tableRow.addView(itemText)
            tableRow.addView(ubicacionDestinoText)
            tableRow.addView(boxDestinoText)
            tableRow.addView(tipo)
            tableRow.addView(codigo_referencia)
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }


    }

    private fun actualizarInformacionSeleccionada(item: ListaItemsPi_Envio) { //De la fila seleccionada colocar los datos en las textview
        try {
            val lbdescripcion = findViewById<TextView>(R.id.lbDescripcionPi_Envio)
            lbdescripcion.text = "${item.DESCRIPCION}";
            this@Picking_envio_list.Tcodigo="${item.CODIGO}";
            this@Picking_envio_list.Tdescripcion="${item.DESCRIPCION}";
            this@Picking_envio_list.Tubicacion="${item.UBICACION_ORIGEN}";
            this@Picking_envio_list.Tbox="${item.BOX_ORIGEN}";
            this@Picking_envio_list.Tcantidad="${item.CANTIDAD}";
            this@Picking_envio_list.item_producto="${item.ITEM}";
            this@Picking_envio_list.p_tipo="${item.TIPO}";
            this@Picking_envio_list.codigo_referencia="${item.CODIGO_REFERENCIA}";

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    fun buscarCodigo(codigoOubicacion: String, tableLayout: TableLayout): Boolean { //Busqueda de codigo en la tabla
        try {
            for (i in 1 until tableLayout.childCount) {
                val fila = tableLayout.getChildAt(i) as? TableRow ?: continue

                val columnaCodigo = fila.getChildAt(1) as? TextView ?: continue
                val columnaUbiOrigen = fila.getChildAt(3) as? TextView ?: continue
                val columnaCodigo_Referecia=fila.getChildAt(12) as? TextView ?: continue

                val textoCodigo = columnaCodigo.text.toString()
                val textoUbiOrigen = columnaUbiOrigen.text.toString()
                val txtCodigoReferencia=columnaCodigo_Referecia.text.toString()

                if (textoCodigo.equals(codigoOubicacion, ignoreCase = true) ||
                    textoUbiOrigen.equals(codigoOubicacion, ignoreCase = true) ||
                        txtCodigoReferencia.equals(codigoOubicacion, ignoreCase = true)){

                    println("Encontrado")

                    val checkBox = findViewById<CheckBox>(R.id.checkBox_Pi_Envio)
                    checkBox?.isChecked = true
                    checkBox?.isEnabled = false
                    fila.setBackgroundColor(Color.parseColor("#639A67"))

                    val almacen_id = (fila.getChildAt(5) as? TextView)?.text.toString()
                    val area_id = (fila.getChildAt(6) as? TextView)?.text.toString()
                    val ubi_origen = textoUbiOrigen
                    val box_origen = (fila.getChildAt(4) as? TextView)?.text.toString()
                    val ubi_des = (fila.getChildAt(9) as? TextView)?.text.toString()
                    val box_des = (fila.getChildAt(10) as? TextView)?.text.toString()
                    val cantidad = (fila.getChildAt(2) as? TextView)?.text.toString()
                    val codigo = textoCodigo
                    val descripcion = (fila.getChildAt(0) as? TextView)?.text.toString()
                    val item_fila = (fila.getChildAt(8) as? TextView)?.text.toString()
                    val cantidad_confir = (fila.getChildAt(7) as? TextView)?.text.toString()
                    val p_tipo = (fila.getChildAt(11) as? TextView)?.text.toString()
                    val codigo_referencia=(fila.getChildAt(12)as? TextView)?.text.toString()

                    val item = ListaItemsPi_Envio(
                        almacen_id.toInt(),
                        area_id.toInt(),
                        ubi_origen,
                        box_origen,
                        ubi_des,
                        box_des,
                        cantidad.toInt(),
                        codigo,
                        descripcion,
                        item_fila.toInt(),
                        cantidad_confir.toInt(),
                        p_tipo,
                        codigo_referencia
                    )

                    actualizarInformacionSeleccionada(item)

                    return true
                }
            }
        } catch (e: Exception) {
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}")
        }
        return false
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_Picking_Envio::class.java));
        //LogsEntradaSalida.logsPorModulo( this@Picking_envio_list, lifecycleScope, "PICKING/ENVIO/TAREA", "SALIDA");
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