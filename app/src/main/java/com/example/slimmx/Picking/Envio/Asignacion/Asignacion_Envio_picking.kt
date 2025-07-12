package com.example.slimmx.Picking.Envio.Asignacion

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
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
import com.example.slimmx.ImpresionEtiqueta.EtiquetaPrinter
import com.example.slimmx.ListaFolioPickingEnvio
import com.example.slimmx.ListaItemsEnvioAsig
import com.example.slimmx.ListaItemsPi_Envio
import com.example.slimmx.ListaItemsPickingRecolecta
import com.example.slimmx.ListaPickingRecolecta
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Packs_Combos.Busqueda_Packs
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.Picking.Cedis.Cedis_seleccion_menu
import com.example.slimmx.Picking.Envio.Picking.Picking_envio_confirmacion
import com.example.slimmx.Picking.Envio.Submenu_Picking_Envio
import com.example.slimmx.R
import com.example.slimmx.Submenus.Submenu_picking
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.example.slimmx.Vista.FragmentPage3
import com.example.slimmx.Vista.FragmentPageEtiquetaBluetooth
import com.example.slimmx.Vista.FragmentPageImpresionEtiquetas
import com.example.slimmx.Vista.ImageFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Asignacion_Envio_picking : AppCompatActivity() , FragmentPageImpresionEtiquetas.OnBackgroundBlockerListener, FragmentPage3.OnBackgroundBlockerListener, FragmentPageEtiquetaBluetooth.OnBackgroundBlockerListener {

    private lateinit var cbSelect: AutoCompleteTextView;
    private lateinit var buttonImpresionLocal:Button;
    private lateinit var buttonImpresionPortatil:Button;

    private var itemsAImprimir: List<ListaItemsEnvioAsig> = emptyList()

    private var contadorBotonEtiquetas: Int=0;
    private var filaSeleccionada: TableRow? = null
    private var unidades_confirmadas: Int=0;
    private var codigo_referencia:String="";

    private var codigo: String="";
    private var descripcion:String="";
    private var inventory_id:String="";
    private var title:String="";
    private var sku:String="";
    private var envio_id:String="";

    var ipImpresoraSeleccionada: String? = null

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
        setContentView(R.layout.activity_asignacion_envio_picking)
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

        cbSelect = findViewById(R.id.cbSelect)
        val btn_Confirmar=findViewById<Button>(R.id.buttonOK_PICK);
        val txtUbicacion=findViewById<EditText>(R.id.txtUbicacion);
        val btn_eliminar_ubicacion=findViewById<Button>(R.id.buttonEliminar_ubicacion);
        buttonImpresionLocal=findViewById(R.id.ButtonEtiquetaLocal);
        buttonImpresionPortatil=findViewById(R.id.ButtonEtiquetaPortatil);


        btn_eliminar_ubicacion.setOnClickListener {
            txtUbicacion.setText("");
            txtUbicacion.post { txtUbicacion.requestFocus() }
        }

        txtUbicacion.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        //obtenerfolio();

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

        btn_Confirmar.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmación")
            builder.setMessage("¿Estás seguro de que deseas confirmar?")

            builder.setPositiveButton("Sí") { dialog, which ->
                if(txtUbicacion.text.isNotEmpty() && cbSelect.text.isNotEmpty()){
                    try {
                        if(contadorBotonEtiquetas==1){
                            Confirmacion_datos(cbSelect.text.toString(), txtUbicacion.text.toString());
                        }else{
                            MensajesDialog.showMessage(this@Asignacion_Envio_picking, "DEBES IMPRIMIR PRIMERO LAS ETIQUETAS");
                        }

                    }catch (e: Exception){
                        MensajesDialog.showMessage(this@Asignacion_Envio_picking, "Ocurrió un error: ${e.message}");
                    }
                }else{
                    MensajesDialog.showMessage(this, "No puede confirmar piezas de mas ni confirmar 0 piezas");
                }

            }

            builder.setNegativeButton("No") { dialog, which ->

            }
            builder.show()

        }

        val buttonMenu = findViewById<Button>(R.id.buttonMenu)
        buttonMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, buttonMenu)
            popupMenu.menuInflater.inflate(R.menu.menu_opciones, popupMenu.menu)
            val menuImpresion = popupMenu.menu.findItem(R.id.item_impresora)
            menuImpresion.isVisible = false;
            val menuUbicacion=popupMenu.menu.findItem(R.id.item_f1);
            menuUbicacion.isVisible=false;
            val menuverImagen=popupMenu.menu.findItem(R.id.item_ver_imagen);
            menuverImagen.isVisible=false;
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
                            val codigo = this@Asignacion_Envio_picking.codigo.toString()
                            val descripcion = this@Asignacion_Envio_picking.descripcion.toString()
                            val folio = cbSelect.text.toString()
                            Log.d("IMPRESION","CODIGO: ${codigo}, DESCRIPCION: ${descripcion}, FOLIO: ${folio}")
                                val fragmentImpresion = FragmentPageImpresionEtiquetas.newInstance(codigo, descripcion, 3, folio, "")
                                // MensajesDialog.showMessage(this, "$codigo, $descripcion, 2, $folio")

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

                            true
                        }
                        R.id.item_f1 -> {
                            true
                        }
                        R.id.item_ver_imagen -> {

                            true
                        }
                        R.id.item_etiquetas_bluetooth->{
                            if (this@Asignacion_Envio_picking.codigo.isNullOrBlank() || this@Asignacion_Envio_picking.descripcion.isNullOrBlank()) {
                                MensajesDialog.showMessage(this, "Se debe de seleccionar algún código");
                                return@setOnMenuItemClickListener true
                            }

                            val fragmentEtiquetaBluetooth = FragmentPageEtiquetaBluetooth().apply {
                                arguments = Bundle().apply {
                                    putString(FragmentPageEtiquetaBluetooth.ARG_CODIGO, this@Asignacion_Envio_picking.codigo)
                                    putString(FragmentPageEtiquetaBluetooth.ARG_DESCRIPCION, this@Asignacion_Envio_picking.descripcion)
                                    putInt(FragmentPageEtiquetaBluetooth.ARG_TIPO, 3)
                                    putString(FragmentPageEtiquetaBluetooth.ARG_FOLIO, cbSelect.text.toString())
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
                        R.id.item_packs_combos->{

                            if (this@Asignacion_Envio_picking.codigo_referencia.isNotEmpty() && (this@Asignacion_Envio_picking.codigo_referencia.startsWith("PACK")|| this@Asignacion_Envio_picking.codigo_referencia.startsWith("COM"))){
                                val intent = Intent(this, Busqueda_Packs::class.java)

                                intent.putExtra("codigo", this@Asignacion_Envio_picking.codigo_referencia);
                                startActivity(intent);
                            }else{
                                MensajesDialog.showMessage(this, "No se ha seleccionado un código combo o pack")
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

        buttonImpresionLocal.setOnClickListener{
            if (txtUbicacion.text.isNotEmpty()){
                optenerDatositemsTareas_Local(cbSelect.text.toString(), txtUbicacion.text.toString())
                contadorBotonEtiquetas=1;
            }else{
                MensajesDialog.showMessage(this@Asignacion_Envio_picking, "Debes de escanear la ubicación destino");
            }

        }

        buttonImpresionPortatil.setOnClickListener {
            if (txtUbicacion.text.isNotEmpty()){
                optenerDatositemsTareas_Bluetooth(cbSelect.text.toString());
                contadorBotonEtiquetas=1;
            }else{
                MensajesDialog.showMessage(this@Asignacion_Envio_picking, "Debes de escanear la ubicación destino");
            }

        }

    }

    /*fun optenerDatositemsTareasYImprimir(folios: String) {
        if (folios.isNotEmpty()) {
            val paramsPickeados = mapOf("folio" to folios)
            val headers = mapOf("Token" to GlobalUser.token.toString())

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/tareas/picking/envios/items/asig",
                        params = paramsPickeados,
                        dataClass = ListaItemsEnvioAsig::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                val ip = ipImpresoraSeleccionada
                                if (ip.isNullOrEmpty()) {
                                    MensajesDialog.showMessage(this@Asignacion_Envio_picking, "IP de impresora no seleccionada")
                                    return@launch
                                }

                                lista.forEach { item ->
                                    val codigoZPL =
                                        "^XA" +
                                                "^CI28" +
                                                "^LH0,0" +
                                                "^FO16,155^A0N,14,14" +
                                                "^FB300,2,0^FDSKU:${item.SKU}^FS" +
                                                "^FO16,140^A0N,18,18^FD${item.COLOR}^FS" +
                                                "^FO16,115^A0N,14,14^FD${item.TITLE}^FS" +
                                                "^FO30,15^BY2^BCN,55,N,N^FD${item.INVENTORY_ID}^FS" +
                                                "^FT109,100^A0N,22,22^FD${item.INVENTORY_ID}^FS" +
                                                "^FO360,20^A0B,20,20^FDID:${item.ENVIO_ID}^FS" +
                                                "^PQ${item.UNIDADES_CONFIRMADAS},0,1,Y" +
                                                "^XZ"

                                    EtiquetaPrinter().imprimirEtiqueta(this@Asignacion_Envio_picking, ip, codigoZPL)
                                    MensajesDialog.showMessage(this@Asignacion_Envio_picking,codigoZPL);
                                }
                            }
                        },
                        onError = { error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Asignacion_Envio_picking, "Error: $error")
                            }
                        }
                    )
                } catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Asignacion_Envio_picking, "Error: ${e.message}")
                    }
                }
            }
        } else {
            MensajesDialog.showMessage(this, "Debes seleccionar un folio")
        }
    }*/
    private fun obtenerfolio(){
        try {
            val params= mapOf(
                "prefijo" to ""
            );

            val headers = mapOf("Token" to GlobalUser.token.toString());

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/inventario/tareas/picking/asig",
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
                                    val intent = Intent(this@Asignacion_Envio_picking, Submenu_Picking_Envio::class.java);
                                    intent.putExtra("MESSAGE", "No hay tareas para este usuario");
                                    startActivity(intent);
                                    finish();
                                }

                            }
                        },
                        onError = { error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Asignacion_Envio_picking, "Error: $error");
                            }
                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Asignacion_Envio_picking, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Asignacion_Envio_picking, "Ocurrió un error: ${e.message}");
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
            cbSelect.setAdapter(adaptador)

            // Cuando se selecciona algun dato en el comboBox
            cbSelect.setOnItemClickListener { _, _, position, _ ->
                val seleccion = opciones[position]
                val textCodigo=findViewById<EditText>(R.id.txtUbicacion)
                val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

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

    private fun optenerDatositemsTareas(folios: String){
        if (folios.isNotEmpty()){
            try {

                val params_pickeados= mapOf(
                    "folio" to folios
                )

                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/tareas/picking/envios/items/asig",
                            params=params_pickeados,
                            dataClass = ListaItemsEnvioAsig::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val items = lista.map {
                                            ListaItemsEnvioAsig(
                                                CODIGO = it.CODIGO,
                                                UNIDADES_CONFIRMADAS = it.UNIDADES_CONFIRMADAS,
                                                CODIGO_REFERENCIA=it.CODIGO_REFERENCIA,
                                                INVENTORY_ID = it.INVENTORY_ID?:"",
                                                TITLE = it.TITLE?:"",
                                                SKU = it.SKU?:"",
                                                ENVIO_ID = it.ENVIO_ID?:"",
                                                COLOR = it.COLOR?:"",
                                                DESCRIPCION=it.DESCRIPCION?:""
                                            )
                                        }
                                        actualizarDatosTablaHistorial(items)
                                    }
                                }
                            },
                            onError = {error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Asignacion_Envio_picking, "Error: $error");
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Asignacion_Envio_picking, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Asignacion_Envio_picking, "Ocurrió un error: ${e.message}");
                }
            }

        }else{
            MensajesDialog.showMessage(this,"Debes de seleccionar un folio")
        }
    }


    private fun actualizarDatosTablaHistorial(items: List<ListaItemsEnvioAsig>){
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

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

                    actualizarInformacionSeleccionada(item)//Actualizar las label al seleccionar una nueva fila
                }

                tableLayout.addView(tableRow)
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun agregarFilaHistorial(tableRow: TableRow, item: ListaItemsEnvioAsig) { //Agregar los datos encontrados en la tabla del historial
        try {
            val codigoText = TextView(this).apply {
                text = item.CODIGO;
                gravity= Gravity.CENTER;
            }

            val cantidadText = TextView(this).apply {
                text = item.UNIDADES_CONFIRMADAS.toString();
                gravity = Gravity.CENTER;
            }

            val codigo_Referencia=TextView(this).apply {
                text=item.CODIGO_REFERENCIA;
                gravity=Gravity.CENTER;
            }


            tableRow.addView(codigoText);
            tableRow.addView(cantidadText);
            tableRow.addView(codigo_Referencia);

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun actualizarInformacionSeleccionada(item: ListaItemsEnvioAsig) { //De la fila seleccionada colocar los datos en las textview
        try {
            this@Asignacion_Envio_picking.codigo=item.CODIGO;
            this@Asignacion_Envio_picking.unidades_confirmadas=item.UNIDADES_CONFIRMADAS;
            this@Asignacion_Envio_picking.codigo_referencia=item.CODIGO_REFERENCIA;
            //MensajesDialog.showMessage(this, "Asignacion codigo ${this@Asignacion_Envio_picking.codigo_referencia}")
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun optenerDatositemsTareas_Bluetooth(folios: String) {
        if (folios.isNotEmpty()) {
            try {
                val params_pickeados = mapOf("folio" to folios)
                val headers = mapOf("Token" to GlobalUser.token.toString())

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/tareas/picking/envios/items/asig/etiquetas",
                            params = params_pickeados,
                            dataClass = ListaItemsEnvioAsig::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        // Convertir ListaItemsEnvioAsig -> ItemEtiqueta
                                        val itemsParaImprimir = lista.map {
                                            FragmentPageEtiquetaBluetooth.ItemEtiqueta(
                                                codigo = it.CODIGO ?: "",
                                                descripcion = it.DESCRIPCION ?: "",
                                                cantidad = it.UNIDADES_CONFIRMADAS ?: 1,
                                                inventory = it.INVENTORY_ID ?: "",
                                                title = it.TITLE ?: "",
                                                sku = it.SKU ?: "",
                                                envio = it.ENVIO_ID ?: "",
                                                color = it.COLOR ?: ""
                                            )
                                        }

                                        abrirFragmentoImpresora(itemsParaImprimir)
                                    }
                                }
                            },
                            onError = { error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(
                                        this@Asignacion_Envio_picking,
                                        "Error: $error"
                                    )
                                }
                            }
                        )
                    } catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Asignacion_Envio_picking, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            } catch (e: Exception) {
                MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}")
            }
        } else {
            MensajesDialog.showMessage(this, "Debes de seleccionar un folio")
        }
    }

    private fun abrirFragmentoImpresora(itemsAImprimir: List<FragmentPageEtiquetaBluetooth.ItemEtiqueta>) {
        val fragmentEtiqueta = FragmentPageEtiquetaBluetooth().apply {
            arguments = Bundle().apply {
                putInt(FragmentPageEtiquetaBluetooth.ARG_TIPO, 3)
                putString(FragmentPageEtiquetaBluetooth.ARG_FOLIO, cbSelect.text.toString())
            }
        }

        val fragmentContainerView = findViewById<View>(R.id.fragmentContainerView_impresion_bluetooth)
        val backgroundBlocker = findViewById<View>(R.id.background_blocker)

        val transaction = supportFragmentManager.beginTransaction()
        val fragmentActual = supportFragmentManager.findFragmentById(R.id.fragmentContainerView_impresion_bluetooth)

        if (fragmentActual != null && fragmentActual is FragmentPageEtiquetaBluetooth) {
            transaction.remove(fragmentActual)
            transaction.add(R.id.fragmentContainerView_impresion_bluetooth, fragmentEtiqueta)
        } else {
            transaction.add(R.id.fragmentContainerView_impresion_bluetooth, fragmentEtiqueta)
        }

        transaction.commitNow()

        // Llevar al frente
        backgroundBlocker.bringToFront()
        fragmentContainerView.bringToFront()

        backgroundBlocker.visibility = View.VISIBLE
        backgroundBlocker.setOnTouchListener { _, _ -> true }

        // Enviar lista de ítems para imprimir
        fragmentEtiqueta.setListaParaImprimir(itemsAImprimir)
    }

    private fun Confirmacion_datos(id:String,ubicacion: String){
        if (id.isNotEmpty() && ubicacion.isNotEmpty() ) {
            try {
                val body = mapOf(
                    "ID" to id,
                    "UBICACION_DESTINO" to ubicacion.uppercase()
                )

                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch {
                    Pedir_datos_apis_post(
                        endpoint = "/picking/envios/asig",
                        body = body,
                        dataClass =Any::class,
                        listaKey = "message",
                        headers=headers,
                        onSuccess = { response ->
                            try {
                                try {
                                    val message = response.toString()
                                    if(message.contains("ENTREGADO CORRECTAMENTE")){
                                        MensajesDialogConfirmaciones.showMessage(this@Asignacion_Envio_picking, "OK") {
                                            val intent = Intent(this@Asignacion_Envio_picking, Submenu_Picking_Envio::class.java)
                                            startActivity(intent)
                                            finish();}
                                    }else{
                                        MensajesDialog.showMessage(this@Asignacion_Envio_picking, "Respuesta: $message");
                                    }
                                } catch (e: Exception) {
                                    MensajesDialog.showMessage(this@Asignacion_Envio_picking, "Error al procesar la respuesta: ${e.message}")
                                }

                            } catch (e: Exception) {
                                MensajesDialog.showMessage(this@Asignacion_Envio_picking, "Error al procesar la respuesta: ${e.message}")
                            }
                        },
                        onError = { error ->
                            MensajesDialog.showMessage(this@Asignacion_Envio_picking, "Error: $error")
                        }
                    )
                }

            }catch (e: Exception){
                MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
            }

        }else {
            MensajesDialog.showMessage(this, "Por favor, complete todos los campos.")
        }
    }

    private fun optenerDatositemsTareas_Local(folios: String, tarima:String) {
        if (folios.isNotEmpty()) {
            try {

                val params_pickeados = mapOf("folio" to folios)
                val headers = mapOf("Token" to GlobalUser.token.toString())

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/tareas/picking/envios/items/asig/etiquetas",
                            params = params_pickeados,
                            dataClass = ListaItemsEnvioAsig::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val itemsParaImprimir = lista.map {
                                            FragmentPageImpresionEtiquetas.ItemEtiqueta(
                                                codigo = it.CODIGO ?: "",
                                                descripcion = it.DESCRIPCION ?: "",
                                                cantidad = it.UNIDADES_CONFIRMADAS ?: 1,
                                                inventory = it.INVENTORY_ID ?: "",
                                                title = it.TITLE ?: "",
                                                sku = it.SKU ?: "",
                                                envio = it.ENVIO_ID ?: "",
                                                color = it.COLOR ?: "",
                                                tarima= tarima
                                            )
                                        }

                                        abrirFragmentoImpresora_Local(itemsParaImprimir)
                                    }
                                }
                            },
                            onError = { error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(
                                        this@Asignacion_Envio_picking,
                                        "Error: $error"
                                    )
                                }
                            }
                        )
                    } catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Asignacion_Envio_picking, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            } catch (e: Exception) {
                MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}")
            }
        } else {
            MensajesDialog.showMessage(this, "Debes de seleccionar un folio")
        }
    }

    private fun abrirFragmentoImpresora_Local(itemsAImprimir: List<FragmentPageImpresionEtiquetas.ItemEtiqueta>) {
        val fragmentEtiqueta = FragmentPageImpresionEtiquetas().apply {
            arguments = Bundle().apply {
                putInt(FragmentPageImpresionEtiquetas.ARG_TIPO, 3) // <<< TIPO 3
                putString(FragmentPageImpresionEtiquetas.ARG_FOLIO, cbSelect.text.toString())
            }
        }

        val fragmentContainerView = findViewById<View>(R.id.fragmentContainerView_impresion)
        val backgroundBlocker = findViewById<View>(R.id.background_blocker)

        val transaction = supportFragmentManager.beginTransaction()
        val fragmentActual = supportFragmentManager.findFragmentById(R.id.fragmentContainerView_impresion)

        if (fragmentActual != null && fragmentActual is FragmentPageImpresionEtiquetas) {
            transaction.remove(fragmentActual)
            transaction.add(R.id.fragmentContainerView_impresion, fragmentEtiqueta)
        } else {
            transaction.add(R.id.fragmentContainerView_impresion, fragmentEtiqueta)
        }

        transaction.commitNow()

        backgroundBlocker.bringToFront()
        fragmentContainerView.bringToFront()

        backgroundBlocker.visibility = View.VISIBLE
        backgroundBlocker.setOnTouchListener { _, _ -> true }

        fragmentEtiqueta.setListaParaImprimir(itemsAImprimir)
    }


    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_Picking_Envio::class.java));
        //LogsEntradaSalida.logsPorModulo( this@Asignacion_Envio_picking, lifecycleScope, "PICKING/ENVIO/ASIGNACION", "SALIDA");
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