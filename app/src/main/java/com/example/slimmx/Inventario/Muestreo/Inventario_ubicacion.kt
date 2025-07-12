package com.example.slimmx.Inventario.Muestreo

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
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalConfig
import com.example.slimmx.GlobalUser
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.MuestreoUbicaciones_mostrar
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.R
import com.example.slimmx.Submenus.Submenu_inventarios
import com.example.slimmx.Validaciones.BoxValidator
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.example.slimmx.Vista.FragmentPage3
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Inventario_ubicacion : AppCompatActivity()  , FragmentPage3.OnBackgroundBlockerListener{

    override fun showBackgroundBlocker() {
        try {
            val backgroundBlockerView: View = findViewById(R.id.background_blocker)
            backgroundBlockerView.visibility = View.VISIBLE
            backgroundBlockerView.setOnTouchListener { _, _ -> true }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    override fun hideBackgroundBlocker() {
        try {
            val backgroundBlockerView: View = findViewById(R.id.background_blocker)
            backgroundBlockerView.visibility = View.GONE
            backgroundBlockerView.setOnTouchListener(null)
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private var filaSeleccionada: TableRow? = null;
    private lateinit var txtProductosTotales: TextView;
    private lateinit var txtStockTotales: TextView;
    private var encontrado:String="";
    private var no_deberia:String="";
    private var no_encontrado:String="";
    private var reabastecer:String="";
    private var vacio:String="";
    private var codigo:String="";
    private var descripcion:String="";
    private var stock_virtual:String="";
    private var box:String="";
    private var ubicacion:String="";
    private var mensaje_confirm:String="";
    private val opcionesDefault = listOf("Encontrado", "No deberia de estar ahi", "Reabastecer", "No encontrado", "Vacio");
    private lateinit var cb_opciones: AutoCompleteTextView;

    private lateinit var txtUbicacion:EditText;
    private lateinit var txtCantidad:EditText;
    private lateinit var txtBox:EditText;
    private lateinit var txtCodigo:EditText;
    private lateinit var lb_cantidad: TextView;
    private lateinit var lb_box: TextView;
    private lateinit var lb_codigo: TextView;
    private lateinit var btn_eliminar_Ubicacion:Button;
    private lateinit var btn_eliminar_Codigo:Button;
    private lateinit var btn_eliminar_Box:Button;
    private lateinit var btn_confirmar:Button;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inventario_ubicacion)
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

        txtProductosTotales=findViewById(R.id.txtProductosTotales);
        txtStockTotales=findViewById(R.id.txtStockTotales);
        cb_opciones=findViewById(R.id.cb_opciones);

        txtUbicacion=findViewById(R.id.txtUbicacion_Inventario);
        txtCantidad=findViewById(R.id.txtCantidad_Inventario);
        txtBox=findViewById(R.id.txtBox_Inventario);
        txtCodigo=findViewById(R.id.txtCodigo_Inventario);
        lb_cantidad=findViewById(R.id.lb_cantidad);
        lb_box=findViewById(R.id.lb_Box_Inventario);
        lb_codigo=findViewById(R.id.lb_codigo);
        val tableLayout=findViewById<TableLayout>(R.id.tableLayout_Inventario);

        btn_eliminar_Ubicacion=findViewById(R.id.buttonEliminar_Ubicacion_In);
        btn_eliminar_Codigo=findViewById(R.id.buttonEliminar_Codigo_In);
        btn_eliminar_Box=findViewById(R.id.buttonEliminar_Box_In);
        btn_confirmar=findViewById(R.id.buttonOK_Inventario);

        txtUbicacion.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txtCodigo.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txtBox.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        txtCantidad.isVisible=false;
        lb_cantidad.isVisible=false;
        txtBox.isVisible=false;
        lb_box.isVisible=false;
        btn_eliminar_Box.isVisible=false;

        btn_eliminar_Ubicacion.setOnClickListener {
            txtUbicacion.setText("");
            txtUbicacion.post { txtUbicacion.requestFocus() }
        }

        btn_eliminar_Box.setOnClickListener {
            txtBox.setText("");
            txtBox.post { txtBox.requestFocus() }
        }

        btn_eliminar_Codigo.setOnClickListener {
            txtCodigo.setText("");
            txtCodigo.post { txtCodigo.requestFocus() }
        }

        // val opcionesDefault = listOf("Encontrado", "No deberia de estar ahi", "Reabastecer", "No encontrado", "Vacio")

        val adaptador = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opcionesDefault)
        cb_opciones.setAdapter(adaptador)

        cb_opciones.setOnItemClickListener { _, _, position, _ ->
            try {
                when (opcionesDefault[position]) {
                    "Encontrado" -> {
                        encontrado_metodo();
                    }

                    "No deberia de estar ahi" -> {
                        no_deberia_metodo();
                        f1_ubicaciones();
                    }

                    "Reabastecer" -> {
                        reabastecer_metodo();
                    }

                    "No encontrado" -> {
                        no_encontrado_metodo();
                    }

                    "Vacio" -> {
                        vacio_metodo();
                    }
                }
            } catch (e: Exception) {
                MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
            }
        }


        txtUbicacion.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtUbicacion.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        BuscarUbicacion(txtUbicacion.text.toString());
                        txtCodigo.post { txtCodigo.requestFocus() }
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }

        txtBox.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtBox.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        BoxValidator.validateBox(
                            input = txtBox.text.toString(),
                            successComponent = {
                                this@Inventario_ubicacion.box=txtBox.text.toString();
                            },
                            failureComponent = { message ->
                                MensajesDialog.showMessage(this@Inventario_ubicacion, message)
                                txtBox.setText("")
                                txtBox.post { txtBox.requestFocus() }
                            }
                        )
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }


        txtCodigo.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtCodigo.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        val codigoIngresado = inputText.toString().trim()
                        println("Código ingresado: $codigoIngresado")

                        val encontrado = buscarCodigo(codigoIngresado, tableLayout)
                        if (!encontrado) {
                            println("Código no encontrado")
                            //Toast.makeText(this@Inventario_ubicacion, "No se encontró ese Código", Toast.LENGTH_SHORT).show()
                            MensajesDialog.showMessage(this,"No se encontró ese Código");
                            cb_opciones.setText(opcionesDefault[1], false)
                            no_deberia_metodo()
                            f1_ubicaciones()
                        } else {
                            println("Código encontrado")
                            if (this@Inventario_ubicacion.stock_virtual.toInt() > 0) {
                                cb_opciones.setText(opcionesDefault[0], false)
                                encontrado_metodo()
                            } else {
                                cb_opciones.setText(opcionesDefault[2], false)
                                reabastecer_metodo()
                            }
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

        btn_confirmar.setOnClickListener {
            if(cb_opciones.text.toString()=="Encontrado"){
                //MensajesDialog.showMessage(this, "Encontrado");
                MensajeConfirmacion();
            }
            if (cb_opciones.text.toString()=="No deberia de estar ahi"){
                //MensajesDialog.showMessage(this, "No deberia")
                this@Inventario_ubicacion.box="";
                this@Inventario_ubicacion.stock_virtual="";
                this@Inventario_ubicacion.descripcion="";
                MensajeConfirmacion();
            }
            if (cb_opciones.text.toString()=="Reabastecer"){
                // MensajesDialog.showMessage(this, "Reabastecer");
                MensajeConfirmacion();
            }
            if (cb_opciones.text.toString()=="No encontrado"){
                //MensajesDialog.showMessage(this, "No encontrado");
                MensajeConfirmacion();
            }
            if (cb_opciones.text.toString()=="Vacio"){
                //MensajesDialog.showMessage(this, "Vacio");
                this@Inventario_ubicacion.box="";
                this@Inventario_ubicacion.stock_virtual="";
                this@Inventario_ubicacion.descripcion="";
                MensajeConfirmacion();
            }

        }
    }

    private fun BuscarUbicacion(ubicacion:String){
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout_Inventario)

            if (tableLayout.childCount > 1) {
                tableLayout.removeViews(1, tableLayout.childCount - 1)
            }
            val params= mapOf(
                "ubicacion" to ubicacion.uppercase()
            )

            val header= mapOf("Token" to GlobalUser.token.toString());

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/inventarios/ubicaciones/check/list",
                        params=params,
                        dataClass = MuestreoUbicaciones_mostrar::class,
                        listaKey = "result",
                        headers = header,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val items = lista.map {
                                        MuestreoUbicaciones_mostrar(
                                            CODIGO = it.CODIGO,
                                            DESCRIPCION = it.DESCRIPCION,
                                            FISICO_DISPONIBLE = it.FISICO_DISPONIBLE,
                                            BOX = it.BOX,
                                            UBICACION = it.UBICACION
                                        )
                                    }
                                    actualizarDatosTablaTareas(items)

                                    val productosTotales = lista.size

                                    val cantidadTotal = lista.sumOf { it.FISICO_DISPONIBLE ?: 0 }

                                    txtProductosTotales.setText("Productos totales: " + productosTotales.toString());
                                    txtStockTotales.setText("Stock totales: " + cantidadTotal.toString());

                                    if (cantidadTotal == 0) {
                                        MensajesDialog.showMessage(this@Inventario_ubicacion, "Ubicación Vacia");
                                        vacio_metodo();
                                        cb_opciones.setText(opcionesDefault[4], false);
                                    }

                                } else {
                                    MensajesDialog.showMessage(this@Inventario_ubicacion, "Ubicación Vacia");
                                    vacio_metodo();
                                    val tableLayout =
                                        findViewById<TableLayout>(R.id.tableLayout_Inventario)
                                    if (tableLayout.childCount > 1) {
                                        tableLayout.removeViews(1, tableLayout.childCount - 1)
                                    }
                                }

                            }
                        }, onError = {error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Inventario_ubicacion, "${error}");
                            }
                        }

                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Inventario_ubicacion, "Ocurrió un error: ${e.message}")
                    }
                }
            }
        }catch (e:Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Inventario_ubicacion, "Ocurrio un error: ${e.message}");
            }
        }
    }

    private fun actualizarDatosTablaTareas(items: List<MuestreoUbicaciones_mostrar>){
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout_Inventario)

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

    private fun agregarFila(tableRow: TableRow, item: MuestreoUbicaciones_mostrar) { //Agregar los datos encontrados en la tabla
        // Crear TextViews y añadirlos a la fila
        try {
            val codigotext = TextView(this).apply {
                text = item.CODIGO;
                gravity = Gravity.CENTER;
            }
            val cantidadtext = TextView(this).apply {
                text = item.FISICO_DISPONIBLE.toString();
                gravity = Gravity.CENTER;
            }

            val descripciontext = TextView(this).apply {
                text = item.DESCRIPCION;
            }

            val boxtext = TextView(this).apply {
                text = item.BOX;
                gravity= Gravity.CENTER;
            }

            val ubicaciontext=TextView(this).apply {
                text=item.UBICACION;
                visibility= View.GONE;
            }

            tableRow.addView(codigotext);
            tableRow.addView(cantidadtext);
            tableRow.addView(descripciontext);
            tableRow.addView(boxtext);
            tableRow.addView(ubicaciontext);

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun actualizarInformacionSeleccionada(item: MuestreoUbicaciones_mostrar) { //De la fila seleccionada colocar los datos en las textview
        try {
            MensajesDialog.showMessage(this,"PRODUCTO: ${item.CODIGO} , ${item.DESCRIPCION} ");

            this@Inventario_ubicacion.codigo=item.CODIGO;
            this@Inventario_ubicacion.descripcion=item.DESCRIPCION;
            this@Inventario_ubicacion.ubicacion=item.UBICACION;
            this@Inventario_ubicacion.box=item.BOX;
            this@Inventario_ubicacion.stock_virtual=item.FISICO_DISPONIBLE.toString();
            txtCodigo.setText(item.CODIGO.toString());
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun ConfirmarUbicacion(){
        try {
            val body= mapOf(
                "UBICACION" to txtUbicacion.text.toString().uppercase(),
                "BOX" to this@Inventario_ubicacion.box.uppercase(),
                "DESCRIPCION" to this@Inventario_ubicacion.descripcion,
                "STOCK_VIRTUAL" to (this@Inventario_ubicacion.stock_virtual.takeIf { it.isNotEmpty() } ?: "0"),
                "STOCK_CONFIRMADO" to txtCantidad.text.toString(),
                "ENCONTRADO" to this@Inventario_ubicacion.encontrado,
                "NO_DEBERIA" to this@Inventario_ubicacion.no_deberia,
                "NO_ENCONTRADO" to this@Inventario_ubicacion.no_encontrado,
                "REABASTECER" to this@Inventario_ubicacion.reabastecer,
                "VACIO" to this@Inventario_ubicacion.vacio,
                "CODIGO" to  (this@Inventario_ubicacion.codigo.uppercase().takeIf { it.isNotEmpty() } ?: txtCodigo.text.toString().uppercase())
            )

            val headers= mapOf("Token" to GlobalUser.token.toString());

            lifecycleScope.launch {
                Pedir_datos_apis_post(
                    endpoint = "/inventario/tareas/muestreo/por/ubicaciones",
                    body=body,
                    dataClass = Any::class,
                    listaKey = "message",
                    headers = headers,
                    onSuccess = {response->
                        try {
                            try {
                                val message = response.toString()
                                if(message.contains("INGRESADO CON EXITO")){
                                    MensajesDialogConfirmaciones.showMessage(this@Inventario_ubicacion, "OK") {
                                        BuscarUbicacion(txtUbicacion.text.toString());
                                        txtCodigo.setText("");
                                        txtCodigo.post { txtCodigo.requestFocus() }
                                    }
                                }else{
                                    MensajesDialog.showMessage(this@Inventario_ubicacion, "Respuesta: $message");
                                }
                            } catch (e: Exception) {
                                MensajesDialog.showMessage(this@Inventario_ubicacion, "Error al procesar la respuesta: ${e.message}")
                            }

                        } catch (e: Exception) {
                            MensajesDialog.showMessage(this@Inventario_ubicacion, "Error al procesar la respuesta: ${e.message}")
                        }
                    }, onError = { error ->
                        if (error.equals("OK", ignoreCase = true)) {
                            MensajesDialogConfirmaciones.showMessage(
                                this@Inventario_ubicacion, "Ok"
                            ) {
                                BuscarUbicacion(txtUbicacion.text.toString());
                                txtCodigo.setText("");
                                txtCodigo.post { txtCodigo.requestFocus() }
                                opcionesDefault[-1];
                            }
                        } else {
                            MensajesDialog.showMessage(this@Inventario_ubicacion, "Error: $error")
                        }
                    }
                )
            }

        }catch (e:Exception){
            MensajesDialog.showMessage(this, "Ocurrio un error: ${e.message}");
        }
    }

    fun buscarCodigo(codigo: String, tableLayout: TableLayout): Boolean {
        try {
            for (i in 1 until tableLayout.childCount) {
                val fila = tableLayout.getChildAt(i) as? TableRow ?: continue
                val columna1 = fila.getChildAt(0) as? TextView ?: continue

                fila.setBackgroundColor(Color.TRANSPARENT) // Reiniciar color de fondo
                if (columna1.text.toString() == codigo) {
                    println("Código encontrado: $codigo")

                    // Destacar la fila encontrada
                    fila.setBackgroundColor(Color.parseColor("#639A67"))

                    // Extraer datos
                    val codigo=(fila.getChildAt(0) as? TextView)?.text.toString();
                    val cantidadText = (fila.getChildAt(1) as? TextView)?.text.toString()
                    val cantidad = if (cantidadText.isNotEmpty()) cantidadText.toInt() else 0
                    val descripcion=(fila.getChildAt(2) as? TextView)?.text.toString();
                    val box=(fila.getChildAt(3) as? TextView)?.text.toString();
                    val ubicacion=(fila.getChildAt(4) as? TextView)?.text.toString();

                    val item = MuestreoUbicaciones_mostrar(codigo,descripcion,cantidad,box,ubicacion);

                    actualizarInformacionSeleccionada(item);
                    return true // Código encontrado
                }
            }
        } catch (e: Exception) {
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}")
        }
        return false
    }


    private fun MensajeConfirmacion(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmación")
        builder.setMessage(mensaje_confirm);

        builder.setPositiveButton("Sí") { dialog, which ->
            if(txtUbicacion.text.toString().isNotEmpty()){
                try {
                    ConfirmarUbicacion();
                }catch (e: Exception){
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                }
            }else{
                MensajesDialog.showMessage(this, "No puede confirmar piezas de mas ni confirmar 0 piezas");
            }

        }

        builder.setNegativeButton("No") { dialog, which ->

        }
        builder.show()
    }


    private fun encontrado_metodo(){
        this@Inventario_ubicacion.encontrado = "true";
        this@Inventario_ubicacion.no_deberia = "false";
        this@Inventario_ubicacion.no_encontrado = "false";
        this@Inventario_ubicacion.reabastecer = "false";
        this@Inventario_ubicacion.vacio = "false";
        txtCantidad.isVisible=false;
        lb_cantidad.isVisible=false;
        txtBox.isVisible=false;
        lb_box.isVisible=false;
        btn_eliminar_Box.isVisible=false;
        txtCodigo.isEnabled=true;
        btn_eliminar_Codigo.isEnabled=true;
        txtCodigo.isVisible=true;
        lb_codigo.isVisible=true;
        btn_eliminar_Codigo.isVisible=true;
        txtBox.setText("");

        mensaje_confirm="¿Estas seguro de que este código " + (this@Inventario_ubicacion.codigo.takeIf { it.isNotEmpty() } ?: txtCodigo.text.toString()) + " con " + txtCantidad.text.toString() + " piezas, y esta descripción " + this@Inventario_ubicacion.descripcion + " SI se encontro?";
        txtCantidad.setText(this@Inventario_ubicacion.stock_virtual);
    }

    private fun no_deberia_metodo(){
        this@Inventario_ubicacion.encontrado = "false";
        this@Inventario_ubicacion.no_deberia = "true";
        this@Inventario_ubicacion.no_encontrado = "false";
        this@Inventario_ubicacion.reabastecer = "false";
        this@Inventario_ubicacion.vacio = "false";
        txtCantidad.isVisible=true;
        lb_cantidad.isVisible=true;
        txtBox.isVisible=false;
        lb_box.isVisible=false;
        btn_eliminar_Box.isVisible=false;
        txtCodigo.isEnabled=true;
        btn_eliminar_Codigo.isEnabled=true;
        txtCodigo.isVisible=true;
        lb_codigo.isVisible=true;
        btn_eliminar_Codigo.isVisible=true;
        txtBox.setText("");
        this@Inventario_ubicacion.codigo="";
        mensaje_confirm="¿Estas seguro de que este código " + (this@Inventario_ubicacion.codigo.takeIf { it.isNotEmpty() } ?: txtCodigo.text.toString()) + " no debería de encontrarse ahi?";
        txtCantidad.setText("0");
    }

    private fun reabastecer_metodo(){
        this@Inventario_ubicacion.encontrado = "false";
        this@Inventario_ubicacion.no_deberia = "false";
        this@Inventario_ubicacion.no_encontrado = "false";
        this@Inventario_ubicacion.reabastecer = "true";
        this@Inventario_ubicacion.vacio = "false";
        txtCantidad.isVisible=true;
        lb_cantidad.isVisible=true;
        txtBox.isVisible=true;
        lb_box.isVisible=true;
        txtBox.setText("S/B");
        btn_eliminar_Box.isVisible=true;
        txtCodigo.isEnabled=true;
        btn_eliminar_Codigo.isEnabled=true;
        txtCodigo.isVisible=true;
        lb_codigo.isVisible=true;
        btn_eliminar_Codigo.isVisible=true;
        mensaje_confirm="¿Estas seguro de Reabastecer estas " +txtCantidad.text.toString() + " unidades de este código " + (this@Inventario_ubicacion.codigo.takeIf { it.isNotEmpty() } ?: txtCodigo.text.toString()) + "?";
    }

    private fun no_encontrado_metodo(){
        this@Inventario_ubicacion.encontrado = "false";
        this@Inventario_ubicacion.no_deberia = "false";
        this@Inventario_ubicacion.no_encontrado = "true";
        this@Inventario_ubicacion.reabastecer = "false";
        this@Inventario_ubicacion.vacio = "false";
        txtCantidad.isVisible=false;
        lb_cantidad.isVisible=false;
        txtBox.isVisible=false;
        lb_box.isVisible=false;
        btn_eliminar_Box.isVisible=false;
        txtCodigo.isEnabled=true;
        btn_eliminar_Codigo.isEnabled=true;
        txtCodigo.isVisible=true;
        lb_codigo.isVisible=true;
        btn_eliminar_Codigo.isVisible=true;
        txtBox.setText("");
        mensaje_confirm= "¿Estas seguro de que este código " + (this@Inventario_ubicacion.codigo.takeIf { it.isNotEmpty() } ?: txtCodigo.text.toString()) + " con esta descripción " + this@Inventario_ubicacion.descripcion + " NO se encontro?";
        txtCantidad.setText("0");
    }

    private fun vacio_metodo(){
        this@Inventario_ubicacion.encontrado = "false";
        this@Inventario_ubicacion.no_deberia = "false";
        this@Inventario_ubicacion.no_encontrado = "false";
        this@Inventario_ubicacion.reabastecer = "false";
        this@Inventario_ubicacion.vacio = "true";
        txtCantidad.isVisible=false;
        lb_cantidad.isVisible=false;
        txtBox.isVisible=false;
        lb_box.isVisible=false;
        btn_eliminar_Box.isVisible=false;
        btn_eliminar_Codigo.isEnabled=false;
        txtCodigo.isVisible=false;
        lb_codigo.isVisible=false;
        btn_eliminar_Codigo.isVisible=false;
        txtBox.setText("");
        mensaje_confirm="¿Estas seguro de que esta ubicación " + txtUbicacion.text+ " esta vacia?";
        txtCantidad.setText("0");
        this@Inventario_ubicacion.stock_virtual="0";
    }

    private fun f1_ubicaciones(){
        try {
            if (txtCodigo.text.isNotEmpty()){
                val fragmentPage3 = FragmentPage3.newInstance(txtCodigo.text.toString())

                val fragmentContainerView = findViewById<View>(R.id.fragmentContainerView_f1)
                val backgroundBlocker = findViewById<View>(R.id.background_blocker)

                val transaction = supportFragmentManager.beginTransaction()
                val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView_f1)

                if (fragment != null) {
                    if (fragment.isVisible) {
                        transaction.hide(fragment)
                        backgroundBlocker.visibility = View.GONE
                        backgroundBlocker.setOnTouchListener(null)
                    } else {
                        transaction.show(fragment)
                        backgroundBlocker.bringToFront()
                        fragmentContainerView.bringToFront()
                        backgroundBlocker.visibility = View.VISIBLE
                        backgroundBlocker.setOnTouchListener { _, _ -> true }
                    }
                } else {
                    transaction.add(R.id.fragmentContainerView_f1, fragmentPage3);
                    backgroundBlocker.bringToFront();
                    fragmentContainerView.bringToFront();
                    backgroundBlocker.visibility = View.VISIBLE;
                    backgroundBlocker.setOnTouchListener { _, _ -> true }
                }

                transaction.commitNow()
            }else{
                MensajesDialog.showMessage(this, "Se debe de seleccionar un producto")
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
        true
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Seleccion_muestreo::class.java));
        //LogsEntradaSalida.logsPorModulo(this@Inventario_ubicacion, lifecycleScope, "INVENTARIOS/MUESTREO/INVENTARIO/UBICACION", "SALIDA")
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