package com.example.slimmx.Recibo.Abastecimiento.Recibo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
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
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListReciboAbastecimiento
import com.example.slimmx.ListReciboAbastecimientoItems
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.R
import com.example.slimmx.Recibo.Abastecimiento.ControlCalidad.Submenu_control_calidad
import com.example.slimmx.Recibo.Abastecimiento.Seleccion_recibo_abastecimiento
import com.example.slimmx.Submenus.Submenu_recibo
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.example.slimmx.Vista.FragmentPage3
import com.example.slimmx.Vista.FragmentPageImpresionEtiquetas
import com.example.slimmx.Vista.FragmentPageEtiquetaBluetooth
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Recibo_list : AppCompatActivity() , FragmentPageImpresionEtiquetas.OnBackgroundBlockerListener, FragmentPage3.OnBackgroundBlockerListener, FragmentPageEtiquetaBluetooth.OnBackgroundBlockerListener {

    private var token:String="";
    private lateinit var cbSelectAbastecimiento_list: AutoCompleteTextView;
    private lateinit var cbSelectAbastecimiento_Cajas: AutoCompleteTextView;
    private lateinit var cbSelectAbastecimiento_Status: AutoCompleteTextView;

    private var filaSeleccionada: TableRow? = null

    private var codigo:String="";
    private var descripcion:String="";
    private var id:Int=0;
    private var item_confirm:String="";
    private var cantidad:String="";
    private var referencia:String="";
    private var calidad:String="";
    private var razon_calidad:String="";

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
        setContentView(R.layout.activity_recibo_abastecimiento_list)

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

        token=GlobalUser.token.toString();

        cbSelectAbastecimiento_list = findViewById(R.id.cbSelectAbastecimiento_list);
        cbSelectAbastecimiento_Cajas=findViewById(R.id.cbSelectAbastecimiento_Cajas);
        cbSelectAbastecimiento_Status=findViewById(R.id.cbSelectAbastecimiento_Status);
        val cbSelect_Cajas_Principal=findViewById<TextInputLayout>(R.id.cbSelectAbastecimiento_Cajas_Principal)
        val cbSelect_Status_Principal=findViewById<TextInputLayout>(R.id.cbSelectAbastecimiento_Status_Principal)

        if (GlobalUser.DEVOLUCION==0){
            //ObtenerDatosAbastecimiento();
            val opciones = intent.getStringArrayExtra("folios")

            if (opciones != null && opciones.isNotEmpty()) {
                actualizarcbBox(opciones.toList()) // Llenar el ComboBox con los datos recibidos
            } else {
                // Si no hay datos, regresar al Submenu_acomodo
                val intent = Intent(this, Seleccion_recibo_abastecimiento::class.java)
                intent.putExtra("MESSAGE", "No hay tareas para este usuario")
                startActivity(intent)
                finish()
            }
            cbSelectAbastecimiento_Cajas.isVisible=false;
            cbSelect_Cajas_Principal.isVisible=false;
        }else if (GlobalUser.DEVOLUCION==1){
            ObtenerDatosRecibo();
            cbSelectAbastecimiento_Status.isVisible=true;
            cbSelect_Status_Principal.isVisible=true;
        }

        val texViewCodigo=findViewById<EditText>(R.id.editTextCodigo_Rec_list);
        val tableLayout=findViewById<TableLayout>(R.id.tableLayout_Recibo);
        val btn_eliminarCodigo=findViewById<Button>(R.id.buttonEliminar_codigo_Reci);
        val checkCodigo=findViewById<CheckBox>(R.id.checkBox_Re_List);
        val btn_confirmar=findViewById<Button>(R.id.buttonOK_Recibo)


        texViewCodigo.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        val buttonMenu = findViewById<Button>(R.id.buttonMenu_Recibo)
        buttonMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, buttonMenu)
            popupMenu.menuInflater.inflate(R.menu.menu_opciones, popupMenu.menu)
            val menuItemVerImagen = popupMenu.menu.findItem(R.id.item_ver_imagen)
            menuItemVerImagen.isVisible = false
            val menuItemCombosPack=popupMenu.menu.findItem(R.id.item_packs_combos);
            menuItemCombosPack.isVisible=false;
            val menuImpresionBluetooth=popupMenu.menu.findItem(R.id.item_etiquetas_bluetooth);
            menuImpresionBluetooth.isVisible=false;
            val menuRecomendacion=popupMenu.menu.findItem(R.id.item_recomendaciones);
            menuRecomendacion.isVisible=false;
            val menuVerificacion=popupMenu.menu.findItem(R.id.item_verificacion);
            menuVerificacion.isVisible=false;
            val menuInventory=popupMenu.menu.findItem(R.id.item_inventario);
            menuInventory.isVisible=false;
            popupMenu.setOnMenuItemClickListener { item ->
                try{
                    when (item.itemId) {
                        R.id.item_impresora -> {
                            val codigo = this@Recibo_list.codigo.toString()
                            val descripcion = this@Recibo_list.descripcion.toString()
                            val folio = cbSelectAbastecimiento_list.text.toString()
                            Log.d("IMPRESION","CODIGO: ${codigo}, DESCRIPCION: ${descripcion}, FOLIO: ${folio}")

                                if(codigo.isNotEmpty() && descripcion.isNotEmpty() && folio.isNotEmpty()){

                                    val fragmentImpresion = FragmentPageImpresionEtiquetas.newInstance(codigo, descripcion, 2, folio,this@Recibo_list.calidad)
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

                                }else{
                                    MensajesDialog.showMessage(this,"Debes de seleccionar algún producto")
                                }

                            true
                        }
                        R.id.item_f1 -> {
                            if (this@Recibo_list.codigo.isNotEmpty()){
                                val fragmentPage3 = FragmentPage3.newInstance(this@Recibo_list.codigo)

                                val fragmentContainerView = findViewById<View>(R.id.fragmentContainerView_Recibo_f1)
                                val backgroundBlocker = findViewById<View>(R.id.background_blocker)

                                val transaction = supportFragmentManager.beginTransaction()
                                val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView_Recibo_f1)

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
                                    transaction.add(R.id.fragmentContainerView_Recibo_f1, fragmentPage3);
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
                        R.id.item_etiquetas_bluetooth->{
                            if (this@Recibo_list.codigo.isNullOrBlank() || this@Recibo_list.descripcion.isNullOrBlank()) {
                                MensajesDialog.showMessage(this, "Se debe de seleccionar algún código");
                                return@setOnMenuItemClickListener true
                            }

                            if (this@Recibo_list.calidad=="true") {
                                MensajesDialog.showMessage(this, "NO SE PUEDEN IMPRIMIR ETIQUETAS SI EL PRODUCTO VA A CALIDAD");
                                return@setOnMenuItemClickListener true
                            }

                            val fragmentEtiquetaBluetooth = FragmentPageEtiquetaBluetooth().apply {
                                arguments = Bundle().apply {
                                    putString(FragmentPageEtiquetaBluetooth.ARG_CODIGO, this@Recibo_list.codigo)
                                    putString(FragmentPageEtiquetaBluetooth.ARG_DESCRIPCION, this@Recibo_list.descripcion)
                                    putInt(FragmentPageEtiquetaBluetooth.ARG_TIPO, 2)
                                    putString(FragmentPageEtiquetaBluetooth.ARG_FOLIO, cbSelectAbastecimiento_list.text.toString())
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
                            MensajesDialog.showMessage(this@Recibo_list, "No se encontro ese Código")
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

        btn_eliminarCodigo.setOnClickListener {
            texViewCodigo.setText("");
            checkCodigo.isChecked=false;
            texViewCodigo.post { texViewCodigo.requestFocus() }
        }

        btn_confirmar.setOnClickListener {
            try {
                val intent = Intent(this, Recibo_confirmacion::class.java)

                // Envía los valores correctos
                intent.putExtra("codigo", this@Recibo_list.codigo)
                intent.putExtra("descripcion", this@Recibo_list.descripcion)
                intent.putExtra("id_confir", this@Recibo_list.id.toString())
                intent.putExtra("item_confirm", this@Recibo_list.item_confirm)
                intent.putExtra("cantidad", this@Recibo_list.cantidad)
                intent.putExtra("referencia", this@Recibo_list.referencia)
                intent.putExtra("calidad", this@Recibo_list.calidad)
                intent.putExtra("razon_calidad", this@Recibo_list.razon_calidad)
                /*MensajesDialog.showMessage(this, "codigo "+this@Recibo_list.codigo + "descripcion "+ this@Recibo_list.descripcion+"id_confir "+ this@Recibo_list.id.toString()+ "item_confirm "+ this@Recibo_list.item_confirm+
                        "cantidad"+ this@Recibo_list.cantidad + "referencia "+ this@Recibo_list.referencia + "calidad "+ this@Recibo_list.calidad+ "razon_calidad"+ this@Recibo_list.razon_calidad);
                */
                startActivity(intent)


                this@Recibo_list.codigo = "";
                this@Recibo_list.descripcion = "";
                this@Recibo_list.item_confirm = "";
                this@Recibo_list.cantidad = "";
                this@Recibo_list.referencia="";
                this@Recibo_list.razon_calidad="";
                this@Recibo_list.calidad="";
                btn_eliminarCodigo.performClick();
            }catch (e: Exception){
                MensajesDialog.showMessage(this@Recibo_list, "Ocurrió un error: ${e.message}");
            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_opciones, menu);
        Log.d("MENU", "Menu creado")
        return true
        //super.onCreateOptionsMenu(menu)
    }

    override fun onResume() {//Cuando regresa a primer plano el activity
        super.onResume()
        val tableLayout =
            findViewById<TableLayout>(R.id.tableLayout_Recibo)
        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }
        if (!cbSelectAbastecimiento_list.text.isNullOrEmpty()) {
            cbSelectAbastecimiento_list.requestFocus()
            if (GlobalUser.DEVOLUCION==0){
                optenerDatositemsAbastecimiento(cbSelectAbastecimiento_list.text.toString(),"0");
            }else if (GlobalUser.DEVOLUCION==1){
                optenerDatositemsRetorno(cbSelectAbastecimiento_list.text.toString());
            }
        }
    }

    private fun ObtenerDatosAbastecimiento(){
        try {
            val headers = mapOf("Token" to GlobalUser.token.toString());

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/proveedores/compras/recibo/folios/check",
                        params=emptyMap<String, String>(),
                        dataClass = ListReciboAbastecimiento::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                Log.d("DEBUG", "Lista recibida: ${lista.size} elementos")
                                if (lista.isEmpty()) {
                                    Log.d("DEBUG", "Lista vacía, redirigiendo...")
                                    val intent = Intent(this@Recibo_list, Seleccion_recibo_abastecimiento::class.java)
                                    intent.putExtra("MESSAGE", "No hay tareas para este usuario")
                                    startActivity(intent)
                                    finish()
                                } else {
                                    val opciones = lista.map { it.FOLIO }
                                    actualizarcbBox(opciones)
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                Log.e("API_ERROR", "Error recibido: $error")
                                MensajesDialog.showMessage(this@Recibo_list, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Recibo_list, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Recibo_list, "Ocurrió un error: ${e.message}");
            }
        }

    }


    private fun ObtenerDatosRecibo() {
        try {
            val headers = mapOf("Token" to GlobalUser.token.toString())

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/canales/devoluciones/recibo",
                        params = emptyMap(),
                        dataClass = ListReciboAbastecimiento::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                Log.d("DEBUG", "Lista recibida: ${lista.size} elementos")
                                if (lista.isEmpty()) {
                                    Log.d("DEBUG", "Lista vacía, redirigiendo...")
                                    val intent = Intent(this@Recibo_list, Seleccion_recibo_abastecimiento::class.java)
                                    intent.putExtra("MESSAGE", "No hay tareas para este usuario")
                                    startActivity(intent)
                                    finish()
                                } else {
                                    val opciones = lista.map { it.FOLIO }
                                    actualizarcbBox(opciones)
                                }
                            }
                        },
                        onError = { error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                Log.e("API_ERROR", "Error recibido: $error")
                                MensajesDialog.showMessage(this@Recibo_list, error)
                            }
                        }
                    )
                } catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Log.e("ERROR", "Excepción: ${e.message}")
                        MensajesDialog.showMessage(this@Recibo_list, "Ocurrió un error: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            lifecycleScope.launch(Dispatchers.Main) {
                Log.e("ERROR", "Excepción general: ${e.message}")
                MensajesDialog.showMessage(this@Recibo_list, "Ocurrió un error: ${e.message}")
            }
        }
    }


    private fun actualizarcbBox(opciones: List<String>) {
        try {
            val texViewCodigo=findViewById<EditText>(R.id.editTextCodigo_Rec_list)
            val adaptador = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                opciones
            )
            cbSelectAbastecimiento_list.setAdapter(adaptador)

            cbSelectAbastecimiento_list.setOnItemClickListener { _, _, position, _ ->
                val seleccion = opciones[position]
                val tableLayout = findViewById<TableLayout>(R.id.tableLayout_Recibo)

                if (tableLayout.childCount > 1) {
                    tableLayout.removeViews(1, tableLayout.childCount - 1)
                }
                if (GlobalUser.DEVOLUCION==1){
                    optenerDatositemsRetorno(seleccion);
                }else{
                    optenerDatositemsAbastecimiento(seleccion,"0");
                }

                texViewCodigo.post { texViewCodigo.requestFocus() }
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this@Recibo_list, "Ocurrió un error: ${e.message}");
        }

    }

    private fun actualizarcbBoxCajas(opciones: List<String>) {
        try {
            val texViewCodigo=findViewById<EditText>(R.id.editTextCodigo_Rec_list)
            val opcionesUnicas = opciones.distinct()

            val adaptador = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                opcionesUnicas
            )
            cbSelectAbastecimiento_Cajas.setAdapter(adaptador)

            cbSelectAbastecimiento_Cajas.setOnItemClickListener { _, _, position, _ ->
                val seleccion = opciones[position]
                val tableLayout = findViewById<TableLayout>(R.id.tableLayout_Recibo)

                if (tableLayout.childCount > 1) {
                    tableLayout.removeViews(1, tableLayout.childCount - 1)
                }
                optenerDatositemsAbastecimiento(cbSelectAbastecimiento_list.text.toString(),seleccion);
                texViewCodigo.post { texViewCodigo.requestFocus() }
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this@Recibo_list, "Ocurrió un error: ${e.message}");
        }
    }

    private fun actualizarcbBoxStatus(opciones: List<String>) {
        try {
            val texViewCodigo=findViewById<EditText>(R.id.editTextCodigo_Rec_list)
            val opcionesUnicas = opciones.distinct()

            val adaptador = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                opcionesUnicas
            )
            cbSelectAbastecimiento_Status.setAdapter(adaptador)

            cbSelectAbastecimiento_Status.setOnItemClickListener { _, _, position, _ ->
                val seleccion = opciones[position]
                val tableLayout = findViewById<TableLayout>(R.id.tableLayout_Recibo)

                if (tableLayout.childCount > 1) {
                    tableLayout.removeViews(1, tableLayout.childCount - 1)
                }

                optenerDatositemsRetornoStatus(cbSelectAbastecimiento_list.text.toString(),seleccion);
                texViewCodigo.post { texViewCodigo.requestFocus() }
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this@Recibo_list, "Ocurrió un error: ${e.message}");
        }

    }



    private fun optenerDatositemsAbastecimiento(seleccion: String, caja:String){
        if(seleccion.isNotEmpty()){
            try {
                val params= mapOf(
                    "folio" to seleccion,
                    "sublinea2" to "0",
                    "caja_box" to "0"
                );

                val headers = mapOf(
                    "Token" to GlobalUser.token.toString()
                );

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/proveedores/compras/recibo/items",
                            params=params,
                            dataClass = ListReciboAbastecimientoItems::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    Log.d("DEBUG", "Lista recibida: ${lista.size} elementos")
                                    if (lista.isNotEmpty()) {
                                        val items = lista.map {
                                            ListReciboAbastecimientoItems(
                                                ID = it.ID,
                                                ITEM = it.ITEM,
                                                CANTIDAD = it.CANTIDAD,
                                                CODIGO = it.CODIGO,
                                                DESCRIPCION = it.DESCRIPCION,
                                                CANTIDAD_RECIBIDA = it.CANTIDAD_RECIBIDA,
                                                POR_RECIBIR = it.POR_RECIBIR,
                                                SUBLINEA2_ID = it.SUBLINEA2_ID,
                                                CAJA = it.CAJA,
                                                STATUS = "",
                                                REFERENCIA = "",
                                                CALIDAD = it.CALIDAD,
                                                RAZON_CALIDAD = it.RAZON_CALIDAD
                                            )

                                        }
                                        actualizarTableLayout(items);
                                        actualizarcbBoxCajas(lista.map { it.CAJA });
                                        this@Recibo_list.id = lista[0].ID;
                                    } else {
                                        Log.d("DEBUG", "Lista vacía, redirigiendo...")
                                        val tableLayout =
                                            findViewById<TableLayout>(R.id.tableLayout_Recibo)
                                        if (tableLayout.childCount > 1) {
                                            tableLayout.removeViews(1, tableLayout.childCount - 1)
                                        }
                                        val intent = Intent(this@Recibo_list, Seleccion_recibo_abastecimiento::class.java);
                                        intent.putExtra("MESSAGE", "Tarea concluida");
                                        startActivity(intent);
                                        finish();
                                    }

                                }
                            },
                            onError = {error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Recibo_list, "Errores: $error");
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Recibo_list, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Recibo_list, "Ocurrió un error: ${e.message}");
                }
            }
        }else{
            MensajesDialog.showMessage(this@Recibo_list,"Se debe de seleccionar un folio");
        }

    }

    private fun optenerDatositemsRetorno(seleccion: String) {
        if (seleccion.isNotEmpty()) {
            try {
                val params= mapOf(
                    "folio" to seleccion,
                    "sublinea2" to "0",
                    "status" to "*"
                );

                val headers = mapOf(
                    "Token" to GlobalUser.token.toString()
                );

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/canales/devoluciones/recibo/items",
                            params=params,
                            dataClass = ListReciboAbastecimientoItems::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val items = lista.map {
                                            ListReciboAbastecimientoItems(
                                                ID = it.ID,
                                                ITEM = it.ITEM,
                                                CANTIDAD = it.CANTIDAD,
                                                CODIGO = it.CODIGO,
                                                DESCRIPCION = it.DESCRIPCION,
                                                CANTIDAD_RECIBIDA = it.CANTIDAD_RECIBIDA,
                                                POR_RECIBIR = it.POR_RECIBIR,
                                                SUBLINEA2_ID = it.SUBLINEA2_ID,
                                                CAJA = "",
                                                STATUS = it.STATUS,
                                                REFERENCIA = it.REFERENCIA,
                                                CALIDAD = it.CALIDAD,
                                                RAZON_CALIDAD = it.RAZON_CALIDAD
                                            )

                                        }
                                        actualizarTableLayout(items);
                                        actualizarcbBoxStatus(lista.map { it.STATUS });
                                        this@Recibo_list.id = lista[0].ID;
                                    } else {
                                        val tableLayout =
                                            findViewById<TableLayout>(R.id.tableLayout_Recibo)
                                        if (tableLayout.childCount > 1) {
                                            tableLayout.removeViews(1, tableLayout.childCount - 1)
                                        }
                                        val intent = Intent(this@Recibo_list, Seleccion_recibo_abastecimiento::class.java);
                                        intent.putExtra("MESSAGE", "Tarea concluida");
                                        startActivity(intent);
                                        finish();
                                    }

                                }
                            },
                            onError = {error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Recibo_list, "Errores: $error");
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Recibo_list, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Recibo_list, "Ocurrió un error: ${e.message}");
                }
            }
        } else {
            MensajesDialog.showMessage(
                this@Recibo_list,
                "Se debe de seleccionar un folio"
            );
        }
    }

    private fun optenerDatositemsRetornoStatus(seleccion: String, status:String) {
        if (seleccion.isNotEmpty()) {
            try {
                val params= mapOf(
                    "folio" to seleccion,
                    "sublinea2" to "0",
                    "status" to status
                );

                val headers = mapOf(
                    "Token" to GlobalUser.token.toString()
                );

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/canales/devoluciones/recibo/items",
                            params=params,
                            dataClass = ListReciboAbastecimientoItems::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val items = lista.map {
                                            ListReciboAbastecimientoItems(
                                                ID = it.ID,
                                                ITEM = it.ITEM,
                                                CANTIDAD = it.CANTIDAD,
                                                CODIGO = it.CODIGO,
                                                DESCRIPCION = it.DESCRIPCION,
                                                CANTIDAD_RECIBIDA = it.CANTIDAD_RECIBIDA,
                                                POR_RECIBIR = it.POR_RECIBIR,
                                                SUBLINEA2_ID = it.SUBLINEA2_ID,
                                                CAJA = "",
                                                STATUS = it.STATUS,
                                                REFERENCIA = it.REFERENCIA,
                                                CALIDAD = it.CALIDAD,
                                                RAZON_CALIDAD = it.RAZON_CALIDAD
                                            )

                                        }
                                        actualizarTableLayout(items);
                                        actualizarcbBoxStatus(lista.map { it.STATUS });
                                        this@Recibo_list.id = lista[0].ID;
                                    } else {
                                        MensajesDialog.showMessage(this@Recibo_list, "No hay datos");
                                    }

                                }
                            },
                            onError = {error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Recibo_list, "Errores: $error");
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Recibo_list, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Recibo_list, "Ocurrió un error: ${e.message}");
                }
            }
        } else {
            MensajesDialog.showMessage(
                this@Recibo_list,
                "Se debe de seleccionar un folio"
            );
        }
    }


    private fun actualizarTableLayout(items: List<ListReciboAbastecimientoItems>) {
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout_Recibo)

            // Elimina todas las filas excepto la primera
            if (tableLayout.childCount > 1) {
                tableLayout.removeViews(1, tableLayout.childCount - 1)
            }

            // Agrega las nuevas filas a la tabla
            for (item in items) {
                val tableRow = TableRow(this)
                agregarFila(tableRow, item)  // Agrega celdas a la fila según el elemento

                // Agrega un listener para cambiar el color al seleccionar una fila
                tableRow.setOnClickListener {
                    filaSeleccionada?.let {
                        it.setBackgroundColor(Color.TRANSPARENT)  // Resalta solo la fila seleccionada
                    }

                    tableRow.setBackgroundColor(Color.parseColor("#639A67"))  // Color de la fila seleccionada
                    filaSeleccionada = tableRow
                    actualizarFilaSeleccionada(item)  // Actualiza la información de la fila seleccionada
                }

                // Añade la fila a la tabla
                tableLayout.addView(tableRow)
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun agregarFila(tableRow: TableRow, item: ListReciboAbastecimientoItems) {
        try {
            val unidadesTextView = TextView(this).apply {
                text = item.CANTIDAD.toString();
                gravity = Gravity.CENTER
                visibility = View.GONE
            }

            val descripcionTextView = TextView(this).apply {
                text = item.DESCRIPCION
            }

            val codigoTextView = TextView(this).apply {
                text = item.CODIGO
                gravity = Gravity.CENTER
            }

            val ItemTextView = TextView(this).apply {
                text = item.ITEM.toString();
                gravity = Gravity.CENTER
                visibility = View.GONE
            }
            val RecibidaTextView = TextView(this).apply {
                text = item.CANTIDAD_RECIBIDA.toString();
                gravity = Gravity.CENTER
            }
            val referenciaTextView = TextView(this).apply {
                text = item.REFERENCIA
                gravity = Gravity.CENTER
                visibility=View.GONE
            }
            val calidad = TextView(this).apply {
                text = item.CALIDAD
                gravity = Gravity.CENTER
                visibility=View.GONE
            }
            val razon_calidad = TextView(this).apply {
                text = item.RAZON_CALIDAD
                gravity = Gravity.CENTER
                visibility=View.GONE
            }
            val statusTextView: TextView? = if (GlobalUser.DEVOLUCION == 1) {
                TextView(this).apply {
                    text = item.STATUS
                    gravity = Gravity.CENTER
                }
            } else {
                null
            }

            tableRow.addView(unidadesTextView);
            tableRow.addView(descripcionTextView);
            tableRow.addView(codigoTextView);
            tableRow.addView(ItemTextView);
            tableRow.addView(RecibidaTextView);
            tableRow.addView(referenciaTextView);
            tableRow.addView(calidad);
            tableRow.addView(razon_calidad);
            if (GlobalUser.DEVOLUCION == 1 && statusTextView != null) {
                tableRow.addView(statusTextView);
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun actualizarFilaSeleccionada(item: ListReciboAbastecimientoItems) {
        try {
            val editDescripcion = findViewById<TextView>(R.id.txtDescripcion_Recibo)
            editDescripcion.text = item.DESCRIPCION
            this@Recibo_list.codigo=item.CODIGO;
            this@Recibo_list.descripcion=item.DESCRIPCION;
            this@Recibo_list.item_confirm=item.ITEM.toString();
            this@Recibo_list.cantidad=item.CANTIDAD.toString();
            this@Recibo_list.referencia=item.REFERENCIA.toString();
            this@Recibo_list.calidad=item.CALIDAD.toString();
            this@Recibo_list.razon_calidad=item.RAZON_CALIDAD.toString();
            MensajesDialog.showMessage(this,"PRODUCTO:  ${this@Recibo_list.codigo}, ${this@Recibo_list.descripcion}")
            //MensajesDialog.showMessage(this, "codigo ${item.CODIGO}, calidad: ${item.CALIDAD.toString()} " )
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }


    fun buscarCodigo(codigo: String, tableLayout: TableLayout): Boolean {
        try {
            for (i in 1 until tableLayout.childCount) {
                val fila = tableLayout.getChildAt(i) as? TableRow ?: continue

                val columna1 = fila.getChildAt(2) as? TextView ?: continue
                fila.setBackgroundColor(Color.TRANSPARENT);
                if (columna1.text.equals(codigo) ) {
                    println("Encontrado");

                    val checkBox = findViewById<CheckBox>(R.id.checkBox_Re_List)
                    checkBox?.isChecked = true
                    checkBox.isEnabled=false
                    fila.setBackgroundColor(Color.parseColor("#639A67"))

                    val unidades=(fila.getChildAt(0) as? TextView)?.text.toString();
                    val descripcion=(fila.getChildAt(1) as? TextView)?.text.toString();
                    val codigo=(fila.getChildAt(2) as? TextView)?.text.toString();
                    val items=(fila.getChildAt(3) as? TextView)?.text.toString();
                    val recibida=(fila.getChildAt(4) as? TextView)?.text.toString();
                    val referencia=(fila.getChildAt(5 )as? TextView)?.text.toString();
                    val calidad=(fila.getChildAt(6 )as? TextView)?.text.toString();
                    val razon_calidad=(fila.getChildAt(7 )as? TextView)?.text.toString();
                    val status=(fila.getChildAt(8) as? TextView)?.text.toString();



                    val item = ListReciboAbastecimientoItems(this@Recibo_list.id,items.toInt(),unidades.toInt(),codigo,descripcion,recibida.toInt(),0,0,"",status,referencia,calidad,razon_calidad )

                    actualizarFilaSeleccionada(item)

                    return true // Código encontrado
                }
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
        return false
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Seleccion_recibo_abastecimiento::class.java));
        //LogsEntradaSalida.logsPorModulo( this, lifecycleScope, "ABASTECIMIENTO/RECIBO", "SALIDA");
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