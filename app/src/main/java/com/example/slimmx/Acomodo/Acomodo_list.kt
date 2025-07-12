package com.example.slimmx.Acomodo

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
import android.widget.PopupMenu
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListItemsAcomodo
import com.example.slimmx.ListaFolioPickingEnvio
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.R
import com.example.slimmx.Submenus.Submenu_acomodo
import com.example.slimmx.Submenus.Submenu_recibo
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.example.slimmx.Vista.FragmentPage3
import com.example.slimmx.Vista.FragmentPageEtiquetaBluetooth
import com.example.slimmx.Vista.FragmentPageImpresionEtiquetas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Acomodo_list : AppCompatActivity() , FragmentPageImpresionEtiquetas.OnBackgroundBlockerListener, FragmentPage3.OnBackgroundBlockerListener, FragmentPageEtiquetaBluetooth.OnBackgroundBlockerListener  {

    private lateinit var cbSelectAcomodoList: AutoCompleteTextView;
    private var filaSeleccionada: TableRow? = null
    private var codigo:String="";
    private var descripcion:String="";
    private var ubicacion_origen:String="";
    private var box_origen:String="";
    private var ubicacion_destino:String="";
    private var box_destino:String="";
    private var unidades:String="";
    private var area:String="";
    private var item_producto="";

    override fun showBackgroundBlocker() {
        val backgroundBlockerView: View = findViewById(R.id.background_blocker)
        backgroundBlockerView.visibility = View.VISIBLE
        backgroundBlockerView.setOnTouchListener { _, _ -> true }

    }

    override fun hideBackgroundBlocker() {
        val backgroundBlockerView: View = findViewById(R.id.background_blocker)
        backgroundBlockerView.visibility = View.GONE
        backgroundBlockerView.setOnTouchListener(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_acomodo_list)
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

        cbSelectAcomodoList = findViewById(R.id.cbSelectAcomodoList);

        val texViewCodigo=findViewById<EditText>(R.id.editTextCodigo_Acomodo_list);
        val tableLayout=findViewById<TableLayout>(R.id.tableLayout_Acomodo);
        val check=findViewById<CheckBox>(R.id.checkBox_Acomodo_List);
        val btn_eliminar_codigo=findViewById<Button>(R.id.buttonEliminar_codigo_Acomo);
        val btn_confirmacion=findViewById<Button>(R.id.buttonOK_Acomodo);


        texViewCodigo.setOnTouchListener(MostrarTeclado.getMostrarTeclado());


        val opciones = intent.getStringArrayExtra("folios")

        if (opciones != null && opciones.isNotEmpty()) {
            actualizarcbBox(opciones.toList()) // Llenar el ComboBox con los datos recibidos
        } else {
            // Si no hay datos, regresar al Submenu_acomodo
            val intent = Intent(this, Submenu_acomodo::class.java)
            intent.putExtra("MESSAGE", "No hay tareas para este usuario")
            startActivity(intent)
            finish()
        }


        btn_eliminar_codigo.setOnClickListener {
            texViewCodigo.setText("");
            check.isChecked=false;
        }

        val buttonMenu = findViewById<Button>(R.id.buttonMenu_Acomodo)
        buttonMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, buttonMenu)
            popupMenu.menuInflater.inflate(R.menu.menu_opciones, popupMenu.menu)
            val menuItemVerImagen = popupMenu.menu.findItem(R.id.item_ver_imagen)
            menuItemVerImagen.isVisible = false;
            val menuItemCombosPacks=popupMenu.menu.findItem(R.id.item_packs_combos);
            menuItemCombosPacks.isVisible=false;
            val menuImpresionBluetooth=popupMenu.menu.findItem(R.id.item_etiquetas_bluetooth);
            menuImpresionBluetooth.isVisible=false;
            val menuRecomendacion=popupMenu.menu.findItem(R.id.item_recomendaciones);
            menuRecomendacion.isVisible=false;
            val menuVerificacion=popupMenu.menu.findItem(R.id.item_verificacion);
            menuVerificacion.isVisible=false;
            val menuInventory=popupMenu.menu.findItem(R.id.item_inventario);
            menuInventory.isVisible=false;
            popupMenu.setOnMenuItemClickListener { item ->
                try {
                    when (item.itemId) {
                        R.id.item_impresora -> {
                            val codigo = this@Acomodo_list.codigo
                            val descripcion = this@Acomodo_list.descripcion
                            val folio = cbSelectAcomodoList.text.toString()
                            if(codigo.isNotEmpty() && descripcion.isNotEmpty() && folio.isNotEmpty()){

                                val fragmentImpresion = FragmentPageImpresionEtiquetas.newInstance(codigo, descripcion, 2, folio, "")

                                val fragmentContainerView = findViewById<View>(R.id.fragmentContainerView_impresion)
                                val backgroundBlocker = findViewById<View>(R.id.background_blocker)

                                val transaction = supportFragmentManager.beginTransaction()
                                val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView_impresion)

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
                                    transaction.add(R.id.fragmentContainerView_impresion, fragmentImpresion);
                                    backgroundBlocker.bringToFront();
                                    fragmentContainerView.bringToFront();
                                    backgroundBlocker.visibility = View.VISIBLE;
                                    backgroundBlocker.setOnTouchListener { _, _ -> true }
                                }
                                transaction.commitNow()

                            }else{
                                MensajesDialog.showMessage(this,"Debes de seleccionar algún producto")
                            }
                            true
                        }
                        R.id.item_f1 -> {
                            if (this@Acomodo_list.codigo.isNotEmpty()){
                                val fragmentPage3 = FragmentPage3.newInstance(this@Acomodo_list.codigo)

                                val fragmentContainerView = findViewById<View>(R.id.fragmentContainerView_Acomodo_f1)
                                val backgroundBlocker = findViewById<View>(R.id.background_blocker)

                                val transaction = supportFragmentManager.beginTransaction()
                                val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView_Acomodo_f1)

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
                                    transaction.add(R.id.fragmentContainerView_Acomodo_f1, fragmentPage3);
                                    backgroundBlocker.bringToFront();
                                    fragmentContainerView.bringToFront();
                                    backgroundBlocker.visibility = View.VISIBLE;
                                    backgroundBlocker.setOnTouchListener { _, _ -> true }
                                }

                                transaction.commitNow()
                            }else{
                                MensajesDialog.showMessage(this, "Se debe de seleccionar un producto")
                            }

                            true
                        }
                        R.id.item_ver_imagen -> {
                            false
                        }
                        R.id.item_etiquetas_bluetooth -> {

                            if (this@Acomodo_list.codigo.isNullOrBlank() || this@Acomodo_list.descripcion.isNullOrBlank()) {
                                MensajesDialog.showMessage(this, "Se debe de seleccionar algún código");
                                return@setOnMenuItemClickListener true
                            }

                            val fragmentEtiquetaBluetooth = FragmentPageEtiquetaBluetooth().apply {
                                arguments = Bundle().apply {
                                    putString(FragmentPageEtiquetaBluetooth.ARG_CODIGO, this@Acomodo_list.codigo)
                                    putString(FragmentPageEtiquetaBluetooth.ARG_DESCRIPCION, this@Acomodo_list.descripcion)
                                    putInt(FragmentPageEtiquetaBluetooth.ARG_TIPO, 2)
                                    putString(FragmentPageEtiquetaBluetooth.ARG_FOLIO, cbSelectAcomodoList.text.toString())
                                }
                            }

                            val fragmentContainerView = findViewById<View>(R.id.fragmentContainerView_impresion_bluetooth)
                            val backgroundBlocker = findViewById<View>(R.id.background_blocker)

                            val transaction = supportFragmentManager.beginTransaction()
                            val fragmentActual = supportFragmentManager.findFragmentById(R.id.fragmentContainerView_impresion_bluetooth)

                            if (fragmentActual != null && fragmentActual is FragmentPageEtiquetaBluetooth) {
                                if (fragmentActual.isVisible) {
                                    transaction.hide(fragmentActual)
                                    backgroundBlocker.visibility = View.GONE
                                    backgroundBlocker.setOnTouchListener(null)
                                } else {
                                    transaction.show(fragmentActual)
                                    backgroundBlocker.bringToFront()
                                    fragmentContainerView.bringToFront()
                                    backgroundBlocker.visibility = View.VISIBLE
                                    backgroundBlocker.setOnTouchListener { _, _ -> true }
                                }
                            } else {
                                transaction.add(R.id.fragmentContainerView_impresion_bluetooth, fragmentEtiquetaBluetooth)
                                backgroundBlocker.bringToFront()
                                fragmentContainerView.bringToFront()
                                backgroundBlocker.visibility = View.VISIBLE
                                backgroundBlocker.setOnTouchListener { _, _ -> true }
                            }

                            transaction.commitNow()
                            true
                        }
                        else -> false
                    }

                } catch (e: Exception) {
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}")
                    false
                }

            }
            popupMenu.show()
        }


        texViewCodigo.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = texViewCodigo.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        val codigoIngresado = inputText.toString().trim()
                        val encontrado = buscarCodigo(codigoIngresado, tableLayout)
                        if (!encontrado) {
                            MensajesDialog.showMessage(this@Acomodo_list, "No se encontro ese Código")
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

        btn_confirmacion.setOnClickListener {
            try {
                if (this@Acomodo_list.codigo.isNotEmpty() && this@Acomodo_list.descripcion.isNotEmpty() && this@Acomodo_list.ubicacion_origen.isNotEmpty() && this@Acomodo_list.box_origen.isNotEmpty()
                    && this@Acomodo_list.ubicacion_destino.isNotEmpty() && this@Acomodo_list.box_destino.isNotEmpty() && this@Acomodo_list.unidades.isNotEmpty() && this@Acomodo_list.area.isNotEmpty()
                    && this@Acomodo_list.item_producto.isNotEmpty()){
                    val intent = Intent(this, Acomodo_confirmacion::class.java)

                    // Envía los valores correctos
                    intent.putExtra("id", cbSelectAcomodoList.text.toString())
                    intent.putExtra("codigo", this@Acomodo_list.codigo)
                    intent.putExtra("descripcion", this@Acomodo_list.descripcion)
                    intent.putExtra("ubicacion_origen", this@Acomodo_list.ubicacion_origen)
                    intent.putExtra("box_origen", this@Acomodo_list.box_origen)
                    intent.putExtra("ubicacion_destino", this@Acomodo_list.ubicacion_destino)
                    intent.putExtra("box_destino", this@Acomodo_list.box_destino)
                    intent.putExtra("unidades", this@Acomodo_list.unidades)
                    intent.putExtra("area", this@Acomodo_list.area)
                    intent.putExtra("item", this@Acomodo_list.item_producto)


                    startActivity(intent)

                    this@Acomodo_list.codigo = "";
                    this@Acomodo_list.descripcion = "";
                    this@Acomodo_list.ubicacion_origen = "";
                    this@Acomodo_list.box_origen = "";
                    this@Acomodo_list.ubicacion_destino = "";
                    this@Acomodo_list.box_destino = "";
                    this@Acomodo_list.unidades = "";
                    this@Acomodo_list.area = "";
                    this@Acomodo_list.item_producto = "";
                    texViewCodigo.setText("");
                    check.isChecked=false;
                }else{
                    MensajesDialog.showMessage(this,"No se ha seleccionado ningún producto");
                }

            }catch (e: Exception){
                MensajesDialog.showMessage(this@Acomodo_list, "Ocurrió un error: ${e.message}");
            }


        }



    }
    override fun onResume() {//Cuando regresa a primer plano el activity
        super.onResume()

        if (!cbSelectAcomodoList.text.isNullOrEmpty()) {
            cbSelectAcomodoList.requestFocus();
            optenerDatositemsAcomodo(cbSelectAcomodoList.text.toString());
        }
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout_Acomodo)
        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }
    }

    private fun obtenerFoliosAcomodo(){
        try {
            val headers = mapOf("Token" to GlobalUser.token.toString());

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/inventario/tareas/acomodo",
                        params=emptyMap<String, String>(),
                        dataClass = ListaFolioPickingEnvio::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.ID }
                                    actualizarcbBox(opciones);
                                } else {
                                    val intent = Intent(this@Acomodo_list, Submenu_acomodo::class.java);
                                    intent.putExtra("MESSAGE", "No hay tareas para este usuario");
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Acomodo_list, "${error}");
                            }
                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Acomodo_list, "Ocurrió un error: ${e.message}")
                    }
                }
            }
        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Acomodo_list, "Ocurrió un error: ${e.message}");
            }
        }

    }

    private fun actualizarcbBox(opciones: List<String>) {
        try {
            val texViewCodigo=findViewById<EditText>(R.id.editTextCodigo_Acomodo_list)
            val adaptador = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                opciones
            )
            cbSelectAcomodoList.setAdapter(adaptador)

            cbSelectAcomodoList.setOnItemClickListener { _, _, position, _ ->
                val seleccion = opciones[position]

                val tableLayout = findViewById<TableLayout>(R.id.tableLayout_Acomodo)

                if (tableLayout.childCount > 1) {
                    tableLayout.removeViews(1, tableLayout.childCount - 1)
                }
                optenerDatositemsAcomodo(seleccion);
                texViewCodigo.post { texViewCodigo.requestFocus() }
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun optenerDatositemsAcomodo(seleccion: String){
        if(seleccion.isNotEmpty()){
            try {
                val params= mapOf(
                    "id" to seleccion
                )

                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/inventario/tareas/acomodo/items",
                            params = params,
                            dataClass = ListItemsAcomodo::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) { // Cambiado a Dispatchers.Main
                                    if (lista.isNotEmpty()) {
                                        val items = lista.map {
                                            ListItemsAcomodo(
                                                UBICACION_ORIGEN = it.UBICACION_ORIGEN,
                                                BOX_ORIGEN = it.BOX_ORIGEN,
                                                UBICACION_DESTINO = it.UBICACION_DESTINO,
                                                BOX_DESTINO = it.BOX_DESTINO,
                                                CANTIDAD = it.CANTIDAD,
                                                CODIGO = it.CODIGO,
                                                DESCRIPCION = it.DESCRIPCION,
                                                ITEM = it.ITEM,
                                                AREA_DESTINO = it.AREA_DESTINO,
                                                CANTIDAD_CONFIRMADA = it.CANTIDAD_CONFIRMADA,
                                                AREA_DESTINO_NOMBRE = it.AREA_DESTINO_NOMBRE
                                            )
                                        }
                                        actualizarTableLayout(items) // UI en Main
                                    } else {
                                        //lifecycleScope.launch(Dispatchers.Main) { // UI en Main
                                            val tableLayout = findViewById<TableLayout>(R.id.tableLayout_Acomodo)
                                            if (tableLayout.childCount > 1) {
                                                tableLayout.removeViews(1, tableLayout.childCount - 1)
                                            }
                                            val intent = Intent(this@Acomodo_list, Submenu_acomodo::class.java)
                                            intent.putExtra("MESSAGE", "Tarea concluida")
                                            startActivity(intent)
                                            finish()
                                       // }
                                    }
                                }
                            },
                            onError = { error ->
                                lifecycleScope.launch(Dispatchers.Main) { // Cambiado a Dispatchers.Main
                                    MensajesDialog.showMessage(this@Acomodo_list, "Errores: $error")
                                }
                            }
                        )
                    } catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) { // Cambiado a Dispatchers.Main
                            MensajesDialog.showMessage(this@Acomodo_list, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Acomodo_list, "Ocurrió un error: ${e.message}");
                }
            }


        }else{
            MensajesDialog.showMessage(this@Acomodo_list,"Se debe de seleccionar un folio");
        }

    }

    private fun actualizarTableLayout(items: List<ListItemsAcomodo>) {
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout_Acomodo)

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
                    actualizarFilaSeleccionada(item)
                }

                // Añade la fila a la tabla
                tableLayout.addView(tableRow)
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun agregarFila(tableRow: TableRow, item: ListItemsAcomodo) {
        try {
            val ubicacion_origen=TextView(this).apply {
                text=item.UBICACION_ORIGEN;
                gravity=Gravity.CENTER
            }
            val box_origen=TextView(this).apply {
                text=item.BOX_ORIGEN;
                gravity=Gravity.CENTER
            }
            val ubicacion_destino=TextView(this).apply {
                text=item.UBICACION_DESTINO;
                gravity=Gravity.CENTER
            }
            val box_destino=TextView(this).apply {
                text=item.BOX_DESTINO;
                gravity=Gravity.CENTER
            }

            val unidades=TextView(this).apply {
                text=item.CANTIDAD.toString();
                gravity=Gravity.CENTER
            }

            val codigo=TextView(this).apply {
                text=item.CODIGO;
                gravity=Gravity.CENTER
            }
            val Descripcion=TextView(this).apply {
                text=item.DESCRIPCION;
            }
            val items_producto=TextView(this).apply {
                text=item.ITEM.toString();
                gravity=Gravity.CENTER
                visibility=View.GONE
            }
            val area=TextView(this).apply {
                text=item.AREA_DESTINO.toString();
                gravity=Gravity.CENTER
                visibility=View.GONE
            }
            val area_name=TextView(this).apply {
                text=item.AREA_DESTINO_NOMBRE;
                gravity=Gravity.CENTER
            }


            tableRow.addView(ubicacion_origen);
            tableRow.addView(box_origen);
            tableRow.addView(ubicacion_destino);
            tableRow.addView(box_destino);
            tableRow.addView(unidades);
            tableRow.addView(codigo);
            tableRow.addView(Descripcion);
            tableRow.addView(items_producto);
            tableRow.addView(area);
            tableRow.addView(area_name);
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun actualizarFilaSeleccionada(item: ListItemsAcomodo) {
        try {
            val editDescripcion = findViewById<TextView>(R.id.txtDescripcion_Acomodo)
            editDescripcion.text = item.DESCRIPCION

            this@Acomodo_list.codigo=item.CODIGO;
            this@Acomodo_list.descripcion=item.DESCRIPCION;
            this@Acomodo_list.ubicacion_origen=item.UBICACION_ORIGEN;
            this@Acomodo_list.box_origen=item.BOX_ORIGEN;
            this@Acomodo_list.ubicacion_destino=item.UBICACION_DESTINO;
            this@Acomodo_list.box_destino=item.BOX_DESTINO;
            this@Acomodo_list.unidades=item.CANTIDAD.toString();
            this@Acomodo_list.area=item.AREA_DESTINO.toString();
            this@Acomodo_list.item_producto=item.ITEM.toString();

            MensajesDialog.showMessage(this,"PRODUCTO:  ${this@Acomodo_list.codigo}, ${this@Acomodo_list.descripcion}")
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    fun buscarCodigo(codigo: String, tableLayout: TableLayout): Boolean {
        try {
            for (i in 1 until tableLayout.childCount) {
                val fila = tableLayout.getChildAt(i) as? TableRow ?: continue

                val columna1 = fila.getChildAt(5) as? TextView ?: continue
                fila.setBackgroundColor(Color.TRANSPARENT);
                if (columna1.text.equals(codigo) ) {
                    println("Encontrado");

                    val checkBox = findViewById<CheckBox>(R.id.checkBox_Acomodo_List)
                    checkBox?.isChecked = true
                    checkBox.isEnabled=false
                    fila.setBackgroundColor(Color.parseColor("#639A67"))

                    val ubi_origen=(fila.getChildAt(0) as? TextView)?.text.toString();
                    val box_origen=(fila.getChildAt(1) as? TextView)?.text.toString();
                    val ubi_destino=(fila.getChildAt(2) as? TextView)?.text.toString();
                    val box_destino=(fila.getChildAt(3) as? TextView)?.text.toString();
                    val unidades=(fila.getChildAt(4) as? TextView)?.text.toString();
                    val codigo=(fila.getChildAt(5) as? TextView)?.text.toString();
                    val descripcion=(fila.getChildAt(6) as? TextView)?.text.toString();
                    val item_producto=(fila.getChildAt(7) as? TextView)?.text.toString();
                    val area=(fila.getChildAt(8) as? TextView)?.text.toString();
                    val area_nombre=(fila.getChildAt(9) as? TextView)?.text.toString();


                    val item = ListItemsAcomodo(ubi_origen, box_origen,ubi_destino, box_destino, unidades.toInt(),codigo,descripcion,item_producto.toInt(),area.toInt(),0,area_nombre)

                    actualizarFilaSeleccionada(item)

                    return true // Código encontrado
                }
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this@Acomodo_list, "Ocurrió un error: ${e.message}");
        }

        return false
    }

    override fun onBackPressed() {
        startActivity(Intent(this@Acomodo_list, Submenu_acomodo::class.java));
        //LogsEntradaSalida.logsPorModulo(this@Acomodo_list, lifecycleScope, "ACOMODO/ACOMODO", "SALIDA")
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