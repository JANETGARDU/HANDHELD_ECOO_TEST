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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListaFolioPickingEnvio
import com.example.slimmx.ListaMuestroItems
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.R
import com.example.slimmx.Submenus.Submenu_inventarios
import com.example.slimmx.Validaciones.LogsEntradaSalida
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Muestreo_list : AppCompatActivity() {

    private lateinit var cbSelectMuestroList: AutoCompleteTextView;
    private lateinit var textCodigo:EditText;
    private var filaSeleccionada: TableRow? = null;
    private var codigo:String="";
    private var descripcion:String="";
    private var item:String="";
    private var ubicacion:String="";
    private var box:String="";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_muestreo_list)
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

        cbSelectMuestroList = findViewById(R.id.cbSelectMuestroList);

        textCodigo=findViewById(R.id.editTextCodigo_Muestreo_list);
        val textUbicacion=findViewById<EditText>(R.id.editTextUbicacion_Muestreo_list);
        val btn_ok=findViewById<Button>(R.id.buttonOK_Muestreo);
        val btn_eliminar_codigo=findViewById<Button>(R.id.buttonEliminar_codigo_Muestro);
        val bt_eliminar_ubicacion=findViewById<Button>(R.id.buttonEliminar_ubicacion_Muestro);
        val tableLayout=findViewById<TableLayout>(R.id.tableLayout_Muestreo_List);


        btn_eliminar_codigo.setOnClickListener {
            textCodigo.setText("");
            textCodigo.post { textCodigo.requestFocus() }
        }

        bt_eliminar_ubicacion.setOnClickListener {
            textUbicacion.setText("");
            textUbicacion.post { textUbicacion.requestFocus() }
        }

        //obtenerfolio();

        val opciones = intent.getStringArrayExtra("folios")

        if (opciones != null && opciones.isNotEmpty()) {
            actualizarcbBox(opciones.toList()) // Llenar el ComboBox con los datos recibidos
        } else {
            // Si no hay datos, regresar al Submenu_acomodo
            val intent = Intent(this, Submenu_inventarios::class.java)
            intent.putExtra("MESSAGE", "No hay tareas para este usuario")
            startActivity(intent)
            finish()
        }

        btn_ok.setOnClickListener {
            try {
                val idSeleccionado = cbSelectMuestroList.text.toString()
                val intent = Intent(this, Muestro_confirmacion::class.java)

                intent.putExtra("folio", idSeleccionado);
                intent.putExtra("codigo", this@Muestreo_list.codigo);
                intent.putExtra("descripcion", this@Muestreo_list.descripcion);
                intent.putExtra("item_prodctos",this@Muestreo_list.item );
                intent.putExtra("ubicacion",this@Muestreo_list.ubicacion );
                intent.putExtra("box",this@Muestreo_list.box );

                startActivity(intent)

                this@Muestreo_list.codigo="";
                this@Muestreo_list.descripcion="";
                this@Muestreo_list.item="";
                this@Muestreo_list.ubicacion="";
                this@Muestreo_list.box="";

            }catch (e: Exception){
                MensajesDialog.showMessage(this@Muestreo_list, "Ocurrió un error: ${e.message}");
            }

        }

        textCodigo.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = textCodigo.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        textUbicacion.post { textUbicacion.requestFocus() }
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }


        textUbicacion.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = textUbicacion.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        val UbicacionIngresado = inputText.toString().trim()
                        val encontrado = buscarCodigo(textCodigo.text.toString(),UbicacionIngresado, tableLayout)
                        if (!encontrado) {
                            MensajesDialog.showMessage(this@Muestreo_list, "No se encontro ese Código")
                        }
                        textCodigo.setText("");
                        textUbicacion.setText("");
                        textCodigo.post { textCodigo.requestFocus() }
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }


    }

    override fun onResume() {//Cuando regresa a primer plano el activity
        try {
            super.onResume()
            if (!cbSelectMuestroList.text.isNullOrEmpty()) {
                optenerDatositemsTareas(cbSelectMuestroList.text.toString());
                cbSelectMuestroList.requestFocus();
                textCodigo.post { textCodigo.requestFocus() }
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrio un error ${e.message}");
        }
    }

    private fun obtenerfolio(){
        try {
            val headers= mapOf(
                "Token" to GlobalUser.token.toString()
            )

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/inventario/tareas/muestreo",
                        params= emptyMap<String,String>(),
                        dataClass = ListaFolioPickingEnvio::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.ID }
                                    actualizarcbBox(opciones);
                                } else {
                                    val intent = Intent(this@Muestreo_list, Submenu_inventarios::class.java);
                                    intent.putExtra("MESSAGE", "No hay tareas para este usuario");
                                    startActivity(intent);
                                    finish();
                                }

                            }
                        },
                        onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Muestreo_list, "${error}");
                            }
                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Muestreo_list, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Muestreo_list, "Ocurrió un error: ${e.message}");
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
            cbSelectMuestroList.setAdapter(adaptador)

            // Cuando se selecciona algun dato en el comboBox
            cbSelectMuestroList.setOnItemClickListener { _, _, position, _ ->
                val seleccion = opciones[position]
                val textCodigo=findViewById<EditText>(R.id.editTextCodigo_Muestreo_list);
                val tableLayout = findViewById<TableLayout>(R.id.tableLayout_Muestreo_List)

                if (tableLayout.childCount > 1) {
                    tableLayout.removeViews(1, tableLayout.childCount - 1)
                }
                optenerDatositemsTareas(seleccion)//Metodo optencion de items
                textCodigo.post { textCodigo.requestFocus() }
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun optenerDatositemsTareas(folios: String) {
        val txtTotal=findViewById<TextView>(R.id.txtTotal_MuestroList);
        if (folios.isNotEmpty()) {
            try {
                val params = mapOf(
                    "id" to folios
                );

                val header= mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/inventario/tareas/muestreo/items",
                            params = params,
                            dataClass = ListaMuestroItems::class,
                            listaKey = "result",
                            headers = header,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val items = lista.map {
                                            ListaMuestroItems(
                                                ID = it.ID,
                                                ITEM = it.ITEM,
                                                UBICACION = it.UBICACION,
                                                BOX = it.BOX,
                                                CODIGO = it.CODIGO,
                                                DESCRIPCION = it.DESCRIPCION,
                                                STOCK_VIRTUAL = it.STOCK_VIRTUAL,
                                                STOCK_CONFIRMADO = it.STOCK_CONFIRMADO
                                            )
                                        }
                                        val contar = lista.count();
                                        txtTotal.setText(contar.toString());
                                        actualizarDatosTablaTareas(items)
                                    } else {
                                        val tableLayout =
                                            findViewById<TableLayout>(R.id.tableLayout_Muestreo_List)
                                        if (tableLayout.childCount > 1) {
                                            tableLayout.removeViews(1, tableLayout.childCount - 1)
                                        }
                                        val intent = Intent(this@Muestreo_list, Submenu_inventarios::class.java);
                                        intent.putExtra("MESSAGE", "Tarea concluida");
                                        startActivity(intent);
                                        finish();
                                    }

                                }
                            },
                            onError = { error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Muestreo_list, "Error: $error");
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Muestreo_list, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Muestreo_list, "Ocurrió un error: ${e.message}");
                }
            }
        }else{
            MensajesDialog.showMessage(this, "No se ha seleccionado ningun Folio");
        }
    }

    private fun actualizarDatosTablaTareas(items: List<ListaMuestroItems>){
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout_Muestreo_List)

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

    private fun agregarFila(tableRow: TableRow, item: ListaMuestroItems) { //Agregar los datos encontrados en la tabla
        // Crear TextViews y añadirlos a la fila
        try {
            val idText = TextView(this).apply {
                text = item.ID;
                visibility= View.GONE;
            }

            val itemText = TextView(this).apply {
                text = item.ITEM.toString();
                gravity = Gravity.CENTER;
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

            val codigoText = TextView(this).apply {
                text = item.CODIGO;
                gravity= Gravity.CENTER;
            }

            val descripcionText = TextView(this).apply {
                text = item.DESCRIPCION;
            }

            tableRow.addView(idText);
            tableRow.addView(itemText);
            tableRow.addView(ubicacionText);
            tableRow.addView(boxText);
            tableRow.addView(codigoText);
            tableRow.addView(descripcionText);
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun actualizarInformacionSeleccionada(item: ListaMuestroItems) { //De la fila seleccionada colocar los datos en las textview
        try {
            val lbdescripcion = findViewById<TextView>(R.id.txtDescripcion_MuestroList);
            lbdescripcion.text = "${item.DESCRIPCION}";

            this@Muestreo_list.codigo="${item.CODIGO}";
            this@Muestreo_list.descripcion="${item.DESCRIPCION}";
            this@Muestreo_list.item="${item.ITEM}";
            this@Muestreo_list.ubicacion="${item.UBICACION}";
            this@Muestreo_list.box="${item.BOX}";

            MensajesDialog.showMessage(this,"PRODUCTO:  ${this@Muestreo_list.codigo}, ${this@Muestreo_list.descripcion}")
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    fun buscarCodigo(codigo: String,ubicacion:String, tableLayout: TableLayout): Boolean {
        try {
            for (i in 1 until tableLayout.childCount) {
                val fila = tableLayout.getChildAt(i) as? TableRow ?: continue

                val columnaCodigo = fila.getChildAt(4) as? TextView ?: continue
                val columnaUbicacion = fila.getChildAt(2) as? TextView ?: continue
                fila.setBackgroundColor(Color.TRANSPARENT);
                if (columnaCodigo.text.equals(codigo) && columnaUbicacion.text.equals(ubicacion) ) {
                    println("Encontrado");

                    fila.setBackgroundColor(Color.parseColor("#639A67"))

                    val envio=(fila.getChildAt(0) as? TextView)?.text.toString();
                    val item_producto=(fila.getChildAt(1) as? TextView)?.text.toString();
                    val ubicacion=(fila.getChildAt(2) as? TextView)?.text.toString();
                    val box=(fila.getChildAt(3) as? TextView)?.text.toString();
                    val codigo=(fila.getChildAt(4) as? TextView)?.text.toString();
                    val descripcion=(fila.getChildAt(5) as? TextView)?.text.toString();

                    val item = ListaMuestroItems(envio,item_producto.toInt(),ubicacion,box,codigo,descripcion,0,0);

                    actualizarInformacionSeleccionada(item)

                    return true // Código encontrado
                }
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

        return false
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_inventarios::class.java));
        //LogsEntradaSalida.logsPorModulo(this@Muestreo_list, lifecycleScope, "INVENTARIOS/MUESTREO/TAREA", "SALIDA");
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