package com.example.slimmx.Empaque.Envio.Packing

import android.annotation.SuppressLint
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
import android.widget.CheckBox
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.Empaque.Envio.Submenu_Empaque_Envio
import com.example.slimmx.GlobalConfig
import com.example.slimmx.GlobalUser
import com.example.slimmx.ImpresionEtiqueta.EtiquetaPrinter
import com.example.slimmx.ListaCerrarCaja
import com.example.slimmx.ListaItem
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Packs_Combos.Busqueda_Packs
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.R
import com.example.slimmx.ResultadoJsonSlimFolios_Packing
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.example.slimmx.Vista.FragmentPageEtiquetaMeli
import com.example.slimmx.Vista.FragmentPageEtiquetaRecomendacion
import com.example.slimmx.Vista.FragmentPageImpresionEtiquetas
import com.example.slimmx.Vista.ImageFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SeleccionList_Envio : AppCompatActivity(), ImageFragment.OnBackgroundBlockerListener, FragmentPageEtiquetaRecomendacion.OnBackgroundBlockerListener, FragmentPageEtiquetaMeli.OnBackgroundBlockerListener {

    override fun showBackgroundBlocker() {
        val backgroundBlockerView: View = findViewById(R.id.background_blocker_Pa_Envio)
        backgroundBlockerView.visibility = View.VISIBLE
        backgroundBlockerView.setOnTouchListener { _, _ -> true }

    }

    override fun hideBackgroundBlocker() {
        val backgroundBlockerView: View = findViewById(R.id.background_blocker_Pa_Envio)
        backgroundBlockerView.visibility = View.GONE
        backgroundBlockerView.setOnTouchListener(null)
    }


    private lateinit var cbSelectEnvios: AutoCompleteTextView // ComboBox para selección
    private lateinit var btn_OK:Button
    private var filaSeleccionada: TableRow? = null
    private var codigo: String = ""
    private var descripcion: String=""
    private var token : String=""
    private var porEmpacar:Int=0;

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empaque_list_envio)

        // Inicializa los componentes de la interfaz
        cbSelectEnvios = findViewById(R.id.cbSelectEnvios)
        cbSelectEnvios.requestFocus()

        // Configura el puerto
        token= GlobalUser.token.toString()

        //obtenerDatosDeApi();//Obtener lod folios

        val opciones = intent.getStringArrayExtra("folios")

        if (opciones != null && opciones.isNotEmpty()) {
            actualizarAutoCompleteTextView(opciones.toList()) // Llenar el ComboBox con los datos recibidos
        } else {
            // Si no hay datos, regresar al Submenu_acomodo
            val intent = Intent(this, Submenu_Empaque_Envio::class.java)
            intent.putExtra("MESSAGE", "No hay tareas para este usuario")
            startActivity(intent)
            finish()
        }

        if (GlobalUser.nombre.isNullOrEmpty()) {
            MensajesDialogConfirmaciones.showMessage(
                this,
                "Ocurrio un error se cerrara la aplicacion, lamento el inconveniente"
            ) {
                finishAffinity()
            }
        }

        val botones_Refrescar = findViewById<Button>(R.id.buttonRefrescar_envio)
        val btn_check=findViewById<Button>(R.id.buttonCK_envio)
        val texViewCodigo=findViewById<EditText>(R.id.txtCodigo)
        val tableLayout=findViewById<TableLayout>(R.id.tableLayout)
        val btnLimpiar=findViewById<Button>(R.id.buttonEliminar_codigo)
        val textViewCantidad=findViewById<EditText>(R.id.txtCantidad)
        btn_OK=findViewById<Button>(R.id.buttonOK_Enviar)
        val btn_CerrarCaja=findViewById<Button>(R.id.buttonCaja_envio)

        texViewCodigo.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        botones_Refrescar.setOnClickListener {//Refresca los datos de la tabla
            // Obtener el valor seleccionado
            val idSeleccionado = cbSelectEnvios.text.toString()

            if (idSeleccionado.isNullOrEmpty()) {
                mostrarError("No se ha seleccionado un ID.")
                return@setOnClickListener
            }
            try {
                obtenerItemsPorId(idSeleccionado)//Metodo optencion de items
            }catch (e: Exception){
                MensajesDialog.showMessage(this@SeleccionList_Envio, "Ocurrió un error: ${e.message}");
            }

        }

        btn_check.setOnClickListener {//Nos envia al activity CajasGeneradas
            try {
                val idSeleccionado = cbSelectEnvios.text.toString()
                val intent = Intent(this, CajasGeneradas::class.java)

                intent.putExtra("idSeleccionado", idSeleccionado)

                startActivity(intent)

            }catch (e: Exception){
                MensajesDialog.showMessage(this@SeleccionList_Envio, "Ocurrió un error: ${e.message}");
            }

        }

        texViewCodigo.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val idSeleccionado = cbSelectEnvios.text.toString()
                    val inputText = texViewCodigo.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        val codigoIngresado = inputText.toString().trim()
                        val encontrado = buscarCodigo(codigoIngresado, tableLayout)
                        val txtCodigo=findViewById<EditText>(R.id.txtCodigo)
                        if (!encontrado) {
                            obtenerItemPorCodigoYFolio(idSeleccionado,txtCodigo.text.toString())

                        }

                        textViewCantidad.post { textViewCantidad.requestFocus() }

                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this@SeleccionList_Envio, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }

        btnLimpiar.setOnClickListener {
            texViewCodigo.text.clear()
            texViewCodigo.post { texViewCodigo.requestFocus() }
        }


        textViewCantidad.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                try {
                    val idSeleccionado = cbSelectEnvios.text.toString()
                    val cantidad = textViewCantidad.text.toString().trim().toIntOrNull() ?: 0
                    if (texViewCodigo.text.isNotEmpty() && textViewCantidad.text.isNotEmpty() && cantidad > 0 || texViewCodigo.text.toString() == this@SeleccionList_Envio.codigo) {
                        val builder = AlertDialog.Builder(this@SeleccionList_Envio)
                        builder.setMessage("¿Está seguro de confirmar?")
                            .setPositiveButton("Sí") { dialog, _ ->
                                confirmacionItemCodigo(idSeleccionado)
                                dialog.dismiss()
                            }
                            .setNegativeButton("No") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .setCancelable(false)
                            .create()
                            .show()
                    } else {
                        val builder = AlertDialog.Builder(this@SeleccionList_Envio)
                        builder.setMessage("Revise bien los datos")
                            .setPositiveButton("Aceptar") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .setCancelable(false)
                            .create()
                            .show()
                    }
                    obtenerItemsPorId(idSeleccionado)
                }catch (e: Exception){
                    MensajesDialog.showMessage(this@SeleccionList_Envio, "Ocurrió un error: ${e.message}");
                }

                true // Indica que el evento fue manejado
            } else {
                false // Permite otras teclas
            }
        }

        btn_OK.setOnClickListener {
            try {
                val idSeleccionado = cbSelectEnvios.text.toString()
                val cantidad = textViewCantidad.text.toString().trim().toIntOrNull() ?: 0
                if(texViewCodigo.text.isNotEmpty() && textViewCantidad.text.isNotEmpty() && cantidad>0 || texViewCodigo.text.toString()==this@SeleccionList_Envio.codigo){
                    btn_OK.isEnabled=false;
                    confirmacionItemCodigo(idSeleccionado)

                }else{
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("Revise bien los datos")
                        .setPositiveButton("Aceptar") { dialog, _ ->//cerrar dialog
                            dialog.dismiss()
                        }
                        .setCancelable(false) //Permite validar que solo va a dejar salir de ese dialog al darle al botón de OK
                        .create()
                        .show()
                }
                obtenerItemsPorId(idSeleccionado)//Metodo optencion de items

            }catch (e: Exception){
                MensajesDialog.showMessage(this@SeleccionList_Envio, "Ocurrió un error: ${e.message}");
            }

        }

        btn_CerrarCaja.setOnClickListener {
            try {
                val idSeleccionado = cbSelectEnvios.text.toString()
                val builder = AlertDialog.Builder(this)
                builder.setMessage("¿Está seguro que desea CERRAR esta caja?")
                    .setPositiveButton("Sí") { dialog, _ ->
                        confirmacionCerrarCaja(idSeleccionado);
                        dialog.dismiss();
                        obtenerItemsPorId(idSeleccionado)//Metodo optencion de items
                        texViewCodigo.post { texViewCodigo.requestFocus() }
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss();
                        obtenerItemsPorId(idSeleccionado)//Metodo optencion de items
                    }
                    .setCancelable(false) // Evita que el usuario cierre el diálogo tocando fuera de él
                    .create()
                    .show()


            }catch (e: Exception){
                MensajesDialog.showMessage(this@SeleccionList_Envio, "Ocurrió un error: ${e.message}");
            }

        }


        val buttonMenu = findViewById<Button>(R.id.buttonMenu_Pa_Envio)
        buttonMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, buttonMenu)
            popupMenu.menuInflater.inflate(R.menu.menu_opciones, popupMenu.menu)
            val menuImpresion = popupMenu.menu.findItem(R.id.item_impresora)
            menuImpresion.isVisible = false
            val menuUbicaciones = popupMenu.menu.findItem(R.id.item_f1)
            menuUbicaciones.isVisible = false
            val menuImpresionBluetooth=popupMenu.menu.findItem(R.id.item_etiquetas_bluetooth);
            menuImpresionBluetooth.isVisible=false;
            val menuVerificacion=popupMenu.menu.findItem(R.id.item_verificacion);
            menuVerificacion.isVisible=false;
            popupMenu.setOnMenuItemClickListener { item ->
                try {
                    when (item.itemId) {
                        R.id.item_impresora -> {
                            true
                        }
                        R.id.item_f1 -> {
                            true
                        }
                        R.id.item_ver_imagen -> {
                            val codigo = this@SeleccionList_Envio.codigo
                            val fragmentContainer = findViewById<View>(R.id.viewSwitcher_Pa_Envio)
                            val backgroundBlocker = findViewById<View>(R.id.background_blocker_Pa_Envio)

                            if (codigo.isNotEmpty()) {
                                val transaction = supportFragmentManager.beginTransaction()
                                val existingFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerViewImagen_Pa_Envio)

                                if (existingFragment != null && existingFragment.isVisible) {
                                    transaction.hide(existingFragment)
                                    transaction.commitNow()
                                    backgroundBlocker.visibility = View.GONE
                                    backgroundBlocker.setOnTouchListener(null)
                                } else {
                                    transaction.replace(R.id.fragmentContainerViewImagen_Pa_Envio, ImageFragment.newInstance(codigo))
                                    transaction.commitNow()
                                    backgroundBlocker.visibility = View.VISIBLE
                                    backgroundBlocker.bringToFront()

                                    backgroundBlocker.setOnTouchListener { _, _ -> true }
                                    fragmentContainer.bringToFront()
                                }
                            } else {
                                MensajesDialog.showMessage(this, "No se está recibiendo un código")
                            }
                            true
                        }

                        R.id.item_packs_combos->{

                            if (this@SeleccionList_Envio.codigo.isNotEmpty() && (this@SeleccionList_Envio.codigo.startsWith("PACK")|| this@SeleccionList_Envio.codigo.startsWith("COM"))){
                                val intent = Intent(this, Busqueda_Packs::class.java)

                                intent.putExtra("codigo", this@SeleccionList_Envio.codigo);
                                startActivity(intent);
                            }else{
                                MensajesDialog.showMessage(this, "No se ha seleccionado un código combo o pack")
                            }

                            true
                        }

                        R.id.item_recomendaciones->{

                            val codigo = this@SeleccionList_Envio.codigo.toString()
                            val folio = cbSelectEnvios.text.toString()
                            Log.d("IMPRESION","CODIGO: ${codigo}, FOLIO: ${folio}, CANTIDAD: ${this@SeleccionList_Envio.porEmpacar}")

                            if(codigo.isNotEmpty() && folio.isNotEmpty()){

                                val fragmentImpresion = FragmentPageEtiquetaRecomendacion.newInstance(codigo, folio, this@SeleccionList_Envio.porEmpacar)

                                val fragmentContainerView = findViewById<View>(R.id.fragmentContainerView_recomendacion)
                                val backgroundBlocker = findViewById<View>(R.id.background_blocker_Pa_Envio)

                                val transaction = supportFragmentManager.beginTransaction()
                                val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView_recomendacion)

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
                                    transaction.add(R.id.fragmentContainerView_recomendacion, fragmentImpresion);
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
                        R.id.item_inventario->{

                            val codigo = this@SeleccionList_Envio.codigo.toString()
                            val folio = cbSelectEnvios.text.toString()
                            Log.d("IMPRESION","CODIGO: ${codigo}, FOLIO: ${folio}, CANTIDAD: ${this@SeleccionList_Envio.porEmpacar}")

                            if(codigo.isNotEmpty() && folio.isNotEmpty()){

                                val fragmentImpresion = FragmentPageEtiquetaMeli.newInstance(codigo, folio, this@SeleccionList_Envio.porEmpacar)

                                val fragmentContainerView = findViewById<View>(R.id.fragmentContainerView_inventory)
                                val backgroundBlocker = findViewById<View>(R.id.background_blocker_Pa_Envio)

                                val transaction = supportFragmentManager.beginTransaction()
                                val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView_inventory)

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
                                    transaction.add(R.id.fragmentContainerView_inventory, fragmentImpresion);
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

                        else -> false
                    }
                } catch (e: Exception) {
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}")
                    false
                }

            }
            popupMenu.show()
        }


    }

    override fun onResume() {//Cuando regresa a primer plano el activity
        super.onResume()

        if (!cbSelectEnvios.text.isNullOrEmpty()) {
            cbSelectEnvios.requestFocus();
            obtenerItemsPorId(cbSelectEnvios.text.toString());
        }
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)
        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }
    }

    private fun obtenerDatosDeApi(){
        try {
            /*val params= mapOf(
                "prefix" to "",
                "token" to  this@SeleccionList_Envio.token
            )*/
            val headers = mapOf("Token" to GlobalUser.token.toString());

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/inventario/empaque",
                        params=emptyMap<String, String>(),
                        dataClass = ResultadoJsonSlimFolios_Packing::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.ID }

                                    actualizarAutoCompleteTextView(opciones)
                                } else {
                                    MensajesDialog.showMessage(this@SeleccionList_Envio, "Lista vacia")
                                }
                            }
                        },
                        onError = { error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@SeleccionList_Envio, "Error: $error")
                            }
                        } )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@SeleccionList_Envio, "Ocurrió un error: ${e.message}")
                    }
                }
            }
        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(
                    this@SeleccionList_Envio,
                    "Ocurrió un error: ${e.message}"
                );
            }
        }

    }

    private fun actualizarAutoCompleteTextView(opciones: List<String>) {
        try {
            val adaptador = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                opciones
            )
            cbSelectEnvios.setAdapter(adaptador)

            // Cuando se selecciona algun dato en el comboBox
            cbSelectEnvios.setOnItemClickListener { _, _, position, _ ->
                val seleccion = opciones[position];
                val textCodigo=findViewById<EditText>(R.id.txtCodigo);
                try {
                    val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

                    if (tableLayout.childCount > 1) {
                        tableLayout.removeViews(1, tableLayout.childCount - 1)
                    }
                    obtenerItemsPorId(seleccion);//Metodo optencion de items
                    textCodigo.post { textCodigo.requestFocus() }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this@SeleccionList_Envio, "Ocurrió un error: ${e.message}");
                }

            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun obtenerItemsPorId(seleccion: String){
        try {
            val params= mapOf(
                "id" to seleccion
            )

            val headers = mapOf("Token" to GlobalUser.token.toString());

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/inventario/empaque/items",
                        params=params,
                        dataClass = ListaItem::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val items = lista.map {
                                        ListaItem(
                                            ID = it.ID,
                                            CODIGO = it.CODIGO,
                                            DESCRIPCION = it.DESCRIPCION,
                                            CONFIRMADA = it.CONFIRMADA.toInt(),
                                            EMPACADA = it.EMPACADA.toInt(),
                                            POR_EMPACAR = it.POR_EMPACAR.toInt(),
                                            REFERENCIA = it.REFERENCIA
                                        )
                                    }

                                    actualizarTableLayout(items);
                                } else {
                                    val tableLayout = findViewById<TableLayout>(R.id.tableLayout)
                                    if (tableLayout.childCount > 1) {
                                        tableLayout.removeViews(1, tableLayout.childCount - 1)
                                    }
                                    MensajesDialog.showMessage(this@SeleccionList_Envio, "Tarea concluida");

                                }

                            }
                        },
                        onError = {error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@SeleccionList_Envio, "Error: $error");
                            }
                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@SeleccionList_Envio, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(
                    this@SeleccionList_Envio,
                    "Ocurrió un error: ${e.message}"
                );
            }
        }

    }



    private fun actualizarTableLayout(items: List<ListaItem>) { //Hacer la selecion por fila
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

                    //Solo cambia de color la fila que se selecciono
                    tableRow.setBackgroundColor(Color.parseColor("#639A67"))

                    // Actualiza las filas para el control de los colores
                    filaSeleccionada = tableRow

                    actualizarInformacionSeleccionadaSelect(item)//Actualizar las label al seleccionar una nueva fila
                }

                tableLayout.addView(tableRow)
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun agregarFila(tableRow: TableRow, item: ListaItem) { //Agregar los datos encontrados en la tabla
        // Crear TextViews y añadirlos a la fila
        try {
            val codigoTextView = TextView(this).apply {
                text = item.CODIGO
                gravity = Gravity.CENTER // Centrar el texto
            }
            val descripcionTextView = TextView(this).apply {
                text = item.DESCRIPCION
            }
            val pickingTextView = TextView(this).apply {
                text = item.CONFIRMADA.toInt().toString()
                gravity = Gravity.CENTER // Centrar el texto
            }
            val packingTextView = TextView(this).apply {
                text = item.EMPACADA.toInt().toString()
                gravity = Gravity.CENTER // Centrar el texto
            }
            val porEmpacarTextView = TextView(this).apply {
                text = item.POR_EMPACAR.toInt().toString()
                gravity = Gravity.CENTER // Centrar el texto
            }

            val referencia=TextView(this).apply {
                text=item.REFERENCIA
                visibility=View.GONE
            }

            // Añadir los TextViews a la TableRow
            tableRow.addView(codigoTextView)
            tableRow.addView(descripcionTextView)
            tableRow.addView(pickingTextView)
            tableRow.addView(packingTextView)
            tableRow.addView(porEmpacarTextView)
            tableRow.addView(referencia)
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun actualizarInformacionSeleccionada(item: ListaItem) {
        try {
            val lbPickeadosEnvio = findViewById<TextView>(R.id.lb_pickeados_envio);
            val lbPorEmpacarEnvio = findViewById<TextView>(R.id.lb_porEmpacar_envio);
            val lbEmpaquetadasEnvio = findViewById<TextView>(R.id.lb_empaquetadas_envio);

            lbPickeadosEnvio.text = "Pickeados: ${item.CONFIRMADA}";
            lbPorEmpacarEnvio.text = "Por Empacar: ${item.POR_EMPACAR}";
            lbEmpaquetadasEnvio.text = "Empaquetadas: ${item.EMPACADA}";
            this@SeleccionList_Envio.descripcion="${item.DESCRIPCION}";
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun actualizarInformacionSeleccionadaSelect(item: ListaItem) {
        try {
            val lbPickeadosEnvio = findViewById<TextView>(R.id.lb_pickeados_envio);
            val lbPorEmpacarEnvio = findViewById<TextView>(R.id.lb_porEmpacar_envio);
            val lbEmpaquetadasEnvio = findViewById<TextView>(R.id.lb_empaquetadas_envio);
            val texViewCodigo=findViewById<EditText>(R.id.txtCodigo);

            lbPickeadosEnvio.text = "Pickeados: ${item.CONFIRMADA}";
            lbPorEmpacarEnvio.text = "Por Empacar: ${item.POR_EMPACAR}";
            lbEmpaquetadasEnvio.text = "Empaquetadas: ${item.EMPACADA}";
            this@SeleccionList_Envio.descripcion="${item.DESCRIPCION}";
            this@SeleccionList_Envio.codigo="${item.CODIGO}";
            this@SeleccionList_Envio.porEmpacar="${item.POR_EMPACAR}".toInt()
            texViewCodigo.setText(this@SeleccionList_Envio.codigo);
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }


    fun buscarCodigo(codigo: String, tableLayout: TableLayout): Boolean {
        try {
            for (i in 1 until tableLayout.childCount) {
                val fila = tableLayout.getChildAt(i) as? TableRow ?: continue

                val columna1 = fila.getChildAt(0) as? TextView ?: continue
                val columnnaReferencia=fila.getChildAt(5) as? TextView?:continue

                if (columna1.text.equals(codigo) || columnnaReferencia.text.contains(codigo)) {
                    println("Encontrado");


                    val checkBox = findViewById<CheckBox>(R.id.checkBox_envios)
                    checkBox?.isChecked = true
                    this@SeleccionList_Envio.codigo =codigo
                    checkBox.isEnabled=false
                    fila.setBackgroundColor(Color.parseColor("#639A67"))
                    val codigo2 = (fila.getChildAt(0) as? TextView)?.text?.toString()
                    val descripcion2 = (fila.getChildAt(1) as? TextView)?.text?.toString() ?: "Descripción no encontrada"
                    val confirmadas = (fila.getChildAt(2) as? TextView)?.text.toString()
                    val empacados = (fila.getChildAt(3) as? TextView)?.text.toString()
                    val porEmpacar = (fila.getChildAt(4) as? TextView)?.text.toString()
                    val referencia=(fila.getChildAt(5)as? TextView)?.text.toString()
                    this@SeleccionList_Envio.descripcion=descripcion2;
                    this@SeleccionList_Envio.codigo=codigo2.toString();
                    this@SeleccionList_Envio.porEmpacar=porEmpacar.toInt();
                    mostrarDialogo(codigo, descripcion, confirmadas, empacados, porEmpacar);
                    if (columnnaReferencia.text.contains(codigo)){
                        val texViewCodigo=findViewById<EditText>(R.id.txtCodigo)
                        texViewCodigo.setText(codigo2)
                    }
                    val item = ListaItem("",codigo.toString(),this@SeleccionList_Envio.descripcion,confirmadas.toInt(), empacados.toInt(), porEmpacar.toInt(), referencia)
                    actualizarInformacionSeleccionada(item)

                    return true
                }
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this@SeleccionList_Envio, "Ocurrió un error: ${e.message}");
        }

        return false
    }

    private fun obtenerItemPorCodigoYFolio(idSeleccionado: String, codigo: String){
        if(codigo.isNotEmpty() && idSeleccionado.isNotEmpty()){
            try {
                val params= mapOf(
                    "id" to idSeleccionado,
                    "codigo" to codigo.uppercase()
                )

                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/inventario/empaque/items/status",
                            params=params,
                            dataClass = ListaItem::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val item = lista[0];
                                        MensajesDialog.showMessage(
                                            this@SeleccionList_Envio,
                                            "Código: " + item.CODIGO + "\nDescripción: " + item.DESCRIPCION + "\nCantidad confirmada: " + item.CONFIRMADA.toInt()
                                                .toString() + "\nEmpacada: " + item.EMPACADA.toInt()
                                                .toString() + "\nPor empacar: " + item.POR_EMPACAR.toInt()
                                                .toString()
                                        );
                                        this@SeleccionList_Envio.codigo = item.CODIGO
                                        this@SeleccionList_Envio.descripcion = item.DESCRIPCION
                                        val texViewCodigo=findViewById<EditText>(R.id.txtCodigo)
                                        texViewCodigo.setText("");
                                        texViewCodigo.post { texViewCodigo.requestFocus() }
                                    } else {
                                        MensajesDialog.showMessage(
                                            this@SeleccionList_Envio,
                                            "El código no se encontro en esta Lista"
                                        )
                                    }
                                }
                            },
                            onError = { error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(
                                        this@SeleccionList_Envio,
                                        "Error: $error"
                                    )
                                }
                            }

                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@SeleccionList_Envio, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(
                        this@SeleccionList_Envio,
                        "Ocurrió un error: ${e.message}"
                    );
                }
            }
        }else{
            MensajesDialog.showMessage(this, "Se deben llenar todos los datos")
        }
    }

    private fun confirmacionItemCodigo(folio:String){
        var check=findViewById<CheckBox>(R.id.checkBox_envios)
        var codigo=findViewById<EditText>(R.id.txtCodigo)
        var cantidad=findViewById<EditText>(R.id.txtCantidad)

        if(folio.isNotEmpty() && codigo.toString().isNotEmpty() && cantidad.toString().isNotEmpty()){
            try {
                val body= mapOf(
                    "ID" to folio,
                    "DESCRIPCION" to this@SeleccionList_Envio.descripcion.toString(),
                    "CODIGO" to codigo.text.toString().uppercase(),
                    "CANTIDAD" to cantidad.text.toString()
                );

                val headers = mapOf("Token" to GlobalUser.token.toString());
                lifecycleScope.launch {
                    Pedir_datos_apis_post(
                        endpoint = "/inventario/empaque/item/set",
                        body = body,
                        dataClass = Any::class,
                        listaKey = "message",
                        headers = headers,
                        onSuccess = { response ->
                            try {
                                try {
                                    val message = response.toString()
                                    if(message.contains("Se actualizo la tarea con")){
                                        MensajesDialog.showMessage(this@SeleccionList_Envio, "OK");
                                        this@SeleccionList_Envio.codigo="";
                                        this@SeleccionList_Envio.descripcion="";

                                        check.isEnabled=true;
                                        check.isChecked=false;

                                        ResetForm();
                                    }else{
                                        MensajesDialog.showMessage(this@SeleccionList_Envio, "Respuesta: $message");
                                    }
                                } catch (e: Exception) {
                                    MensajesDialog.showMessage(this@SeleccionList_Envio, "Error al procesar la respuesta: ${e.message}")
                                }

                            } catch (e: Exception) {
                                MensajesDialog.showMessage(this@SeleccionList_Envio, "Error al procesar la respuesta: ${e.message}")
                            }
                        },
                        onError = { error ->
                            MensajesDialog.showMessage(this@SeleccionList_Envio, "Error: $error")
                        }
                    )
                }


            }catch (e: Exception){
                MensajesDialog.showMessage(this@SeleccionList_Envio, "Ocurrió un error: ${e.message}");
            }

        }else{
            MensajesDialog.showMessage(this, "Debes de llenar todos los datos")
        }
    }

    private fun confirmacionCerrarCaja(folio: String){
        if (folio.isNotEmpty()){
            try {
                val body= mapOf(
                    "ID" to folio
                );

                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch {
                    Pedir_datos_apis_post(
                        endpoint = "/inventario/empaque/caja/cerrar",
                        body=body,
                        dataClass = ListaCerrarCaja::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess ={ lista ->
                            if (lista.MESSAGE.equals("OK")){
                                val builder = AlertDialog.Builder(this@SeleccionList_Envio)
                                val caja=lista.BOX
                                val piezas=lista.CANTIDAD
                                val fhora=lista.FECHA_HORA
                                val idSeleccionado = cbSelectEnvios.text.toString()
                                builder.setMessage("Mensaje: OK")
                                    .setPositiveButton("Aceptar") { dialog, _ ->
                                        dialog.dismiss()
                                        val token=this@SeleccionList_Envio.token
                                       /* val ip = "192.168.10.22"
                                        val codigoZPL = "\"\"\"\n" +
                                                 "            ^XA\n" +
                                                 "            ^CI28\n" +
                                                 "            ^LH0,0\n" +
                                                 "            ^FX Grafico de Logo.\n" +
                                                 "            ^FO455,30^GFA,1300,1300,13,,::::::::::::::R0F8,R0FC,Q018C,:Q03FE,Q07FF,Q04018,:,R07,P07JF,O03FI07E,N01FK07C,N0FM0F8,M03CM01C,M07O07,L01CO01C,L03Q0E,L06Q03,K01CQ018,K038R08,,::::::::N07EK07E,0078I01E78I01E78J0F,00C1I03818I0381CI0C3,0181I0300CI0300EI0C18,0181I06006I06006I0418,0783I06006I06006I041E,0F83I04006I06002I061F,0983I04006I06002I06198,:0983I06006I06006I06198,0983I0600CI03006I06198,0983I0301CI0380CI06198,0D83I01C38I01C38I06198,0F83J0FFK0FFJ041F,0383J018K018J041C,0181W0418,00818V0C18,00FD8V0DF,00788V09E,J0CU018,J0CJ03KFCJ01,J06J038I01CJ03,J06J01K08J02,J03J018I018J06,J01J018I018J0C,J018J0CI03K0C,K0CJ06I06J018,K06J03I0CJ03,K03J01C038J06,K018J0IFK0C,L0CJ01F8J018,L06Q07,L038P0E,M0EO038,M07O0F,M01EM03C,N078L0F,N01F8J0FC,O03FE03FE,P01IFC,,::::::::::::::^FS\n" +
                                                 "            ^FX Texto debajo del grafico.\n" +
                                                 "            ^FB500,2,2\n" +
                                                 "            ^FO435,130^A0N,20,20^FDSLIM-COMPANY^FS\n" +
                                                 "            ^FX Cliente del pedido.\n" +
                                                 "            ^FB500,2,2\n" +
                                                 "            ^FO10,60^A0N,25,25^FDCLIENTE: MERCADO LIBRE^FS\n" +
                                                 "            ^FX Numero de pedido.\n" +
                                                 "            ^FB500,2,2\n" +
                                                 "            ^FO10,100^A0N,35,35^FDPEDIDO $idSeleccionado-$caja^FS\n" +
                                                 "            ^FX Linea horizontal.\n" +
                                                 "            ^FO10,180^GB700,3,3^FS\n" +
                                                 "            ^FX Codigo de barras.\n" +
                                                 "            ^FO30,200^BY2\n" +
                                                 "            ^BCN,90,Y,N,N\n" +
                                                 "            ^FD$idSeleccionado-$caja^FS\n" +
                                                 "            ^FX Informacion.\n" +
                                                 "            ^FB280,2,2\n" +
                                                 "            ^FO10,330^A0N,25,25^FDINFORMACION^FS\n" +
                                                 "            ^FB240,2,2\n" +
                                                 "            ^FO10,355^A0N,25,25^FDContenido: $piezas Unidad(es)^FS\n" +
                                                 "            ^FB280,2,2\n" +
                                                 "            ^FO400,340^A0N,25,25^FD$fhora^FS\n" +
                                                 "            ^PQ1,0,1,Y^XZ\n" +
                                                 "        \"\"\""
                                        val etiquetaPrinter = EtiquetaPrinter()
                                        lifecycleScope.launch {
                                            val exito = etiquetaPrinter.imprimirEtiqueta(
                                                this@SeleccionList_Envio,
                                                ip,
                                                codigoZPL
                                            );
                                            if (exito) {
                                                android.app.AlertDialog.Builder(this@SeleccionList_Envio)
                                                    .setMessage("Impresión completada correctamente")
                                                    .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                                                    .setCancelable(false)
                                                    .create()
                                                    .show()
                                            } else {
                                                android.app.AlertDialog.Builder(this@SeleccionList_Envio)
                                                    .setMessage("Hubo un error al imprimir.")
                                                    .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                                                    .setCancelable(false)
                                                    .create()
                                                    .show()
                                            }
                                        }*/
                                    }
                                    .setCancelable(false)
                                    .create()
                                    .show()

                                ResetForm()//Limpiar datos y refrescar

                            } else {
                                MensajesDialog.showMessage(this@SeleccionList_Envio, "${lista.MESSAGE}")
                            }

                        },
                        onError = { error ->
                            MensajesDialog.showMessage(this@SeleccionList_Envio, "Error: $error")
                        }
                    )
                }
            }catch (e: Exception){
                MensajesDialog.showMessage(this@SeleccionList_Envio, "Ocurrió un error: ${e.message}");
            }

        }else{
            MensajesDialog.showMessage(this, "No se ha seleccionado nada")
        }
    }


    //Mensajes de dailog
    fun mostrarDialogo(codigo: String, descripcion: String, confirmadas: String, empacados: String, porEmpacar: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("CODIGO : $codigo\n" +
                "DESCRIPCION: $descripcion\n\n" +
                "CONFIRMADAS: $confirmadas\n" +
                "EMPACADOS: $empacados\n" +
                "POR EMPACAR: $porEmpacar")
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
            .show()

    }


    private fun mostrarError(error: String) {
        Log.e("SeleccionList_Envio", error)
       // mostrarMensaje(error)
    }

    private fun ResetForm(){
        var texViewCodigo=findViewById<EditText>(R.id.txtCodigo)
        var textViewCantidad=findViewById<EditText>(R.id.txtCantidad)
        val idSeleccionado = cbSelectEnvios.text.toString()
        texViewCodigo.setText("");
        textViewCantidad.setText("");
        btn_OK.isEnabled=true;

        if (idSeleccionado.isNullOrEmpty()) {
            mostrarError("No se ha seleccionado un ID.")
        }
        obtenerItemsPorId(idSeleccionado);//Metodo optencion de items
        texViewCodigo.post { texViewCodigo.requestFocus() }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_Empaque_Envio::class.java));
        //LogsEntradaSalida.logsPorModulo( this@SeleccionList_Envio, lifecycleScope, "EMPAQUE/ENVIO/TAREA", "SALIDA");
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

