package com.example.slimmx.Acomodo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalConfig
import com.example.slimmx.GlobalUser
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.R
import com.example.slimmx.ResultadoJsonSlimFolios_Packing
import com.example.slimmx.Validaciones.BoxValidator
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.example.slimmx.Vista.FragmentPage3
import com.example.slimmx.Vista.FragmentPageEtiquetaBluetooth
import com.example.slimmx.Vista.FragmentPageImpresionEtiquetas
import com.example.slimmx.acomodos_con
import com.example.slimmx.defectos
import com.example.slimmx.lista_respuestas
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Acomodo_confirmacion : AppCompatActivity(), FragmentPageImpresionEtiquetas.OnBackgroundBlockerListener, FragmentPage3.OnBackgroundBlockerListener, FragmentPageEtiquetaBluetooth.OnBackgroundBlockerListener  {

    private lateinit var cbSelectAcomodo: AutoCompleteTextView
    private lateinit var txtCantidad:EditText

    override fun showBackgroundBlocker() {
        val backgroundBlockerView: View = findViewById(R.id.background_blocker_Acomodo_confir)
        backgroundBlockerView.visibility = View.VISIBLE
        backgroundBlockerView.setOnTouchListener { _, _ -> true }

    }

    override fun hideBackgroundBlocker() {
        val backgroundBlockerView: View = findViewById(R.id.background_blocker_Acomodo_confir)
        backgroundBlockerView.visibility = View.GONE
        backgroundBlockerView.setOnTouchListener(null)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_acomodo_confirmacion)
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

        cbSelectAcomodo = findViewById(R.id.cbSelectAcomodo);

        val v_id=intent.getStringExtra("id");
        val v_codigo = intent.getStringExtra("codigo");
        val v_descripcion = intent.getStringExtra("descripcion");
        val v_ubicacion_origen=intent.getStringExtra("ubicacion_origen");
        val v_box_origen=intent.getStringExtra("box_origen");
        val v_ubicacion_destino=intent.getStringExtra("ubicacion_destino");
        val v_box_destino=intent.getStringExtra("box_destino");
        val v_unidades=intent.getStringExtra("unidades");
        val v_area=intent.getStringExtra("area");
        val v_item_producto=intent.getStringExtra("item");

        val lbCodigo = findViewById<TextView>(R.id.txtSugeridoCodigo_Acomodo);
        val lbfolio=findViewById<TextView>(R.id.txtFolio_Acomodo_confirm);
        val lbUbi_origen=findViewById<TextView>(R.id.txtUbicacion_Origen_Acomodo_confirm);
        val lbUbi_destino=findViewById<TextView>(R.id.lb_SugeridoUbi_Des_acomodo);
        val lbBox_destino=findViewById<TextView>(R.id.lb_SugeridoBox_Des_acomodo);
        val lbDescripcion=findViewById<TextView>(R.id.lb_Descripcion_acomodo);
        val lbCantidad=findViewById<TextView>(R.id.txtSugeridoCantidad_pi_re_acc);

        val txtUbicacion_destino=findViewById<EditText>(R.id.txtUbicacion_Destino_Acomodo_confirm);
        val txtBox_destino=findViewById<EditText>(R.id.txtBox_Destino_Acomodo_confirm);
        val txtCodigo=findViewById<EditText>(R.id.txtCodigo_Acomodo_confirm);

        val btn_eliminar_ubicacion=findViewById<Button>(R.id.buttonEliminar_Ubicacion_Acomodo);
        val btn_eliminar_box=findViewById<Button>(R.id.buttonEliminar_Box_Acomodo);
        val btn_eliminar_codigo=findViewById<Button>(R.id.buttonEliminar_codigo_Acomodo);
        val btn_salir=findViewById<Button>(R.id.buttonCancelar_Acomodo_confir);
        val btn_confirmar=findViewById<Button>(R.id.buttonConfirmar_Acomodo_confir);

        val lbTipo=findViewById<TextView>(R.id.lbtipodefecto);
        val cbDrown=findViewById<TextInputLayout>(R.id.cbSelectAcomodo_drown)
        txtCantidad=findViewById(R.id.txtCantidad_Acomodo_confirm);
        txtUbicacion_destino.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txtBox_destino.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txtCodigo.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        cbSelectAcomodo.inputType = InputType.TYPE_NULL
        cbSelectAcomodo.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(cbSelectAcomodo.windowToken, 0)
            }
        }

        lbfolio.setText(v_id);
        lbCodigo.setText(v_codigo);
        lbUbi_origen.setText(v_ubicacion_origen);
        lbUbi_destino.setText(v_ubicacion_destino);
        lbBox_destino.setText(v_box_destino);
        lbDescripcion.setText(v_descripcion);
        lbCantidad.setText(v_unidades);

        if ((v_area.toString().toInt() == 29 || v_area.toString().toInt() == 8 || v_area.toString().toInt() == 17) ){
            if(!v_ubicacion_origen.toString().startsWith("CALIDAD")){
                lbTipo.isVisible=true;
                cbSelectAcomodo.isVisible=true;
                cbDrown.isVisible=true;

                try {
                    val params = mapOf(
                        "codigo" to v_codigo.toString()
                    )

                    val headers= mapOf(
                        "Token" to GlobalUser.token.toString()
                    )

                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            Pedir_datos_apis(
                                endpoint = "/tipos/defecto/producto",
                                params=params,
                                dataClass = defectos::class,
                                listaKey = "result",
                                headers = headers,
                                onSuccess = { lista ->
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        if (lista.isNotEmpty()) {
                                            val opciones = lista.map { it.DEFECTO_GENERAL }

                                            actualizarAutoCompleteTextView(opciones)
                                        } else {
                                            MensajesDialog.showMessage(this@Acomodo_confirmacion, "Lista vacia")
                                        }
                                    }
                                },
                                onError = { error ->
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        MensajesDialog.showMessage(this@Acomodo_confirmacion, "Error: $error");
                                    }
                                } )
                        }catch (e: Exception) {
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Acomodo_confirmacion, "Ocurrió un error: ${e.message}")
                            }
                        }
                    }

                }catch (e: Exception){
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Acomodo_confirmacion, "Ocurrió un error: ${e.message}");
                    }
                }
            }else{
                lbTipo.isVisible=false;
                cbSelectAcomodo.isVisible=false;
                cbDrown.isVisible=false;
            }

        }else{
            lbTipo.isVisible=false;
            cbSelectAcomodo.isVisible=false;
            cbDrown.isVisible=false;
        }



        if(v_ubicacion_destino=="CROSSDOCK"){
            txtUbicacion_destino.setText(v_ubicacion_destino);
            if (v_box_destino=="S/B"){
                txtBox_destino.setText(v_box_destino);
                txtBox_destino.isEnabled=true;
                txtCodigo.isEnabled=true;
                txtCodigo.post { txtCodigo.requestFocus() }
            }else{
                txtBox_destino.isEnabled=true;
                txtBox_destino.post { txtBox_destino.requestFocus() }
            }
        }else{
            txtUbicacion_destino.post { txtUbicacion_destino.requestFocus() }
        }

        if (v_box_destino=="S/B"){
            txtBox_destino.setText(v_box_destino);
        }



        val buttonMenu = findViewById<Button>(R.id.buttonMenu_Acomodo_confir)
        buttonMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, buttonMenu)
            popupMenu.menuInflater.inflate(R.menu.menu_opciones, popupMenu.menu)
            val menuItemVerImagen = popupMenu.menu.findItem(R.id.item_ver_imagen)
            menuItemVerImagen.isVisible = false
            val menuRecomendacion=popupMenu.menu.findItem(R.id.item_recomendaciones);
            menuRecomendacion.isVisible=false;
            val menuVerificacion=popupMenu.menu.findItem(R.id.item_verificacion);
            menuVerificacion.isVisible=false;
            val menuImpresionBluetooth=popupMenu.menu.findItem(R.id.item_etiquetas_bluetooth);
            menuImpresionBluetooth.isVisible=false;
            val menuComboPack=popupMenu.menu.findItem(R.id.item_packs_combos);
            menuComboPack.isVisible=false;
            val menuInventory=popupMenu.menu.findItem(R.id.item_inventario);
            menuInventory.isVisible=false;
                popupMenu.setOnMenuItemClickListener { item ->
                    try {
                        when (item.itemId) {
                            R.id.item_impresora -> {

                                if(v_codigo.toString().isNotEmpty() && v_descripcion.toString().isNotEmpty() && v_id.toString().isNotEmpty()){

                                    val fragmentImpresion = FragmentPageImpresionEtiquetas.newInstance(v_codigo.toString(), v_descripcion.toString(), 2, v_id.toString(), "")

                                    val fragmentContainerView = findViewById<View>(R.id.fragmentContainerView_impresion_Acomodo)
                                    val backgroundBlocker = findViewById<View>(R.id.background_blocker_Acomodo_confir)

                                    val transaction = supportFragmentManager.beginTransaction()
                                    val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView_impresion_Acomodo)

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
                                        transaction.add(R.id.fragmentContainerView_impresion_Acomodo, fragmentImpresion);
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
                                if (v_codigo.toString().isNotEmpty()){
                                    val fragmentPage3 = FragmentPage3.newInstance(v_codigo.toString())

                                    val fragmentContainerView = findViewById<View>(R.id.fragmentContainerView_f1_Acomodo_confir)
                                    val backgroundBlocker = findViewById<View>(R.id.background_blocker_Acomodo_confir)

                                    val transaction = supportFragmentManager.beginTransaction()
                                    val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView_f1_Acomodo_confir)

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
                                        transaction.add(R.id.fragmentContainerView_f1_Acomodo_confir, fragmentPage3);
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
                                if (v_codigo.toString().isNullOrBlank() || v_descripcion.toString().isNullOrBlank()) {
                                    MensajesDialog.showMessage(this, "Se debe de seleccionar algún código");
                                    return@setOnMenuItemClickListener true
                                }

                                val fragmentEtiquetaBluetooth = FragmentPageEtiquetaBluetooth().apply {
                                    arguments = Bundle().apply {
                                        putString(FragmentPageEtiquetaBluetooth.ARG_CODIGO, v_codigo.toString())
                                        putString(FragmentPageEtiquetaBluetooth.ARG_DESCRIPCION, v_descripcion.toString())
                                        putInt(FragmentPageEtiquetaBluetooth.ARG_TIPO, 2)
                                        putString(FragmentPageEtiquetaBluetooth.ARG_FOLIO, v_id.toString())
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

        btn_eliminar_ubicacion.setOnClickListener {
            txtUbicacion_destino.setText("");
            txtUbicacion_destino.post { txtUbicacion_destino.requestFocus() }
        }

        btn_eliminar_box.setOnClickListener {
            txtBox_destino.setText("");
            txtBox_destino.post { txtBox_destino.requestFocus() }
        }

        btn_eliminar_codigo.setOnClickListener {
            txtCodigo.setText("");
            txtCodigo.post { txtCodigo.requestFocus() }
        }

        btn_salir.setOnClickListener {
            finish();
        }

        txtUbicacion_destino.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtUbicacion_destino.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        if (v_ubicacion_destino.toString()!=txtUbicacion_destino.text.toString()){
                            MensajesDialog.showMessage(this@Acomodo_confirmacion, "Se cambio la ubicación recomendada");
                        }
                        if (v_box_destino.equals("S/B")){
                            txtCodigo.isEnabled=true;
                            txtCodigo.post { txtCodigo.requestFocus() }
                            txtBox_destino.isEnabled=true;
                        }else{
                            txtBox_destino.isEnabled=true;

                            txtBox_destino.post { txtBox_destino.requestFocus() }
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

        txtCantidad.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtCantidad.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        if (txtCantidad.text.toString().toInt()>v_unidades.toString().toInt()){
                            MensajesDialog.showMessage(this@Acomodo_confirmacion, "No puedes confirmar piezas de más");
                            txtCantidad.setText("");
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

        txtBox_destino.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtBox_destino.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        BoxValidator.validateBox(
                            input = txtBox_destino.text.toString(),
                            successComponent = {
                                txtCodigo.isEnabled = true
                                txtCodigo.post { txtCodigo.requestFocus() }
                            },
                            failureComponent = { message ->
                                MensajesDialog.showMessage(this@Acomodo_confirmacion, message)
                                txtBox_destino.setText("")
                                txtBox_destino.post { txtBox_destino.requestFocus() }
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
                        if (txtCodigo.text.toString().equals(v_codigo)){
                            if (v_area.toString().toInt()==29 || v_area.toString().toInt()==8 || v_area.toString().toInt()==17){
                                if(!v_ubicacion_origen.toString().startsWith("CALIDAD")){
                                    cbSelectAcomodo.requestFocus();
                                }else{
                                    btn_confirmar.setBackgroundColor(Color.parseColor("#059212"));
                                    btn_confirmar.isEnabled=true;
                                    txtCantidad.isEnabled=true;
                                }
                            }else{
                                btn_confirmar.setBackgroundColor(Color.parseColor("#059212"));
                                btn_confirmar.isEnabled=true;
                            }


                        }else{
                            MensajesDialog.showMessage(this@Acomodo_confirmacion,"El código ingresado es erroneo");
                            txtCodigo.setText("");
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

            if (v_area.toString().toInt()==29 || v_area.toString().toInt()==8 || v_area.toString().toInt()==17 ){
                if(!v_ubicacion_origen.toString().startsWith("CALIDAD")){
                    try {
                        if (txtCodigo.text.toString().equals(v_codigo.toString())){
                            confirmarAcomodoDefecto(v_id.toString(),v_ubicacion_origen.toString(),v_box_origen.toString(),txtUbicacion_destino.text.toString(),txtBox_destino.text.toString(),txtCantidad.text.toString().toInt(),txtCodigo.text.toString(), v_item_producto.toString().toInt(),v_area.toString(),cbSelectAcomodo.text.toString());
                        }else{
                            MensajesDialog.showMessage(this, "Código incorrecto");
                            btn_confirmar.isEnabled=true;
                        }
                    }catch (e: Exception){
                        MensajesDialog.showMessage(this@Acomodo_confirmacion, "Ocurrió un error: ${e.message}");
                    }
                }else{
                    try {
                        confirmarAcomodoDefecto(v_id.toString(),v_ubicacion_origen.toString(),v_box_origen.toString(),txtUbicacion_destino.text.toString(),txtBox_destino.text.toString(),txtCantidad.text.toString().toInt(),txtCodigo.text.toString(), v_item_producto.toString().toInt(),v_area.toString(),"DEFECTO");
                    }catch (e: Exception){
                        MensajesDialog.showMessage(this@Acomodo_confirmacion, "Ocurrió un error: ${e.message}");
                    }
                }
            }else{
                try {
                    confirmarAcomodo(v_id.toString(),v_ubicacion_origen.toString(),v_box_origen.toString(),txtUbicacion_destino.text.toString(),txtBox_destino.text.toString(),txtCantidad.text.toString().toInt(),txtCodigo.text.toString(), v_item_producto.toString().toInt(),v_area.toString());
                }catch (e: Exception){
                    MensajesDialog.showMessage(this@Acomodo_confirmacion, "Ocurrió un error: ${e.message}");
                }

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
            cbSelectAcomodo.setAdapter(adaptador)

            val btn_confirmar=findViewById<Button>(R.id.buttonConfirmar_Acomodo_confir);

            cbSelectAcomodo.setOnItemClickListener { _, _, position, _ ->
                val seleccion = opciones[position]
                btn_confirmar.isEnabled=true;
                btn_confirmar.setBackgroundColor(Color.parseColor("#059212"));
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun confirmarAcomodo(id:String, ubicacion_origen:String,box_origen:String, ubicacion_destino:String,box_destino:String, unidades_conf:Int,
                                 codigo: String, item:Int, area_destino:String){
        if (id.isNotEmpty() && ubicacion_origen.isNotEmpty() && box_origen.isNotEmpty() && ubicacion_destino.isNotEmpty() && box_destino.isNotEmpty() && unidades_conf.toString().isNotEmpty() &&
            codigo.isNotEmpty() && item.toString().isNotEmpty() && area_destino.isNotEmpty()){

            try {
                val body= mapOf(
                    "ID" to id,
                    "UBICACION_ORIGEN" to ubicacion_origen.uppercase(),
                    "BOX_ORIGEN" to box_origen.uppercase(),
                    "UBICACION_DESTINO" to ubicacion_destino.uppercase(),
                    "BOX_DESTINO" to box_destino.uppercase(),
                    "UNIDADES_CONFIRMADAS" to unidades_conf.toString(),
                    "CODIGO" to codigo.uppercase(),
                    "ITEM" to item.toString(),
                    "AREA_DESTINO" to area_destino
                )
                val headers = mapOf(
                    "Token" to GlobalUser.token.toString()
                );
                lifecycleScope.launch {
                    Pedir_datos_apis_post(
                        endpoint = "/inventario/tareas/acomodo/item/set",
                        body=body,
                        dataClass = lista_respuestas::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = {lista ->
                            if (lista.status.equals("OK")){
                                MensajesDialogConfirmaciones.showMessage(this@Acomodo_confirmacion, "OK") {
                                    finish()
                                }
                            }else{
                                MensajesDialog.showMessage(this@Acomodo_confirmacion, "${lista.status}");
                            }

                        }, onError = { error ->
                            MensajesDialog.showMessage(this@Acomodo_confirmacion, "Error: ${error}")
                        }
                    )
                }


            }catch (e: Exception){
                MensajesDialog.showMessage(this@Acomodo_confirmacion, "Ocurrió un error: ${e.message}");
            }


        }else{
            MensajesDialog.showMessage(this,"Se deben de llenar todos los datos seleccionados");
        }

    }

    private fun confirmarAcomodoDefecto(id:String, ubicacion_origen:String,box_origen:String, ubicacion_destino:String,box_destino:String, unidades_conf:Int,
                                        codigo: String, item:Int, area_destino:String, tipo_defecto:String){
        if (id.isNotEmpty() && ubicacion_origen.isNotEmpty() && box_origen.isNotEmpty() && ubicacion_destino.isNotEmpty() && box_destino.isNotEmpty() && unidades_conf.toString().isNotEmpty() &&
            codigo.isNotEmpty() && item.toString().isNotEmpty() && area_destino.isNotEmpty() && tipo_defecto.isNotEmpty()){
            try {
                val body= mapOf(
                    "ID" to id,
                    "UBICACION_ORIGEN" to ubicacion_origen.uppercase(),
                    "BOX_ORIGEN" to box_origen.uppercase(),
                    "UBICACION_DESTINO" to ubicacion_destino.uppercase(),
                    "BOX_DESTINO" to box_destino.uppercase(),
                    "UNIDADES_CONFIRMADAS" to unidades_conf.toString(),
                    "CODIGO" to codigo.uppercase(),
                    "ITEM" to item.toString(),
                    "AREA_DESTINO" to area_destino,
                    "TIPO_DEFECTO" to tipo_defecto
                )
                val headers = mapOf(
                    "Token" to GlobalUser.token.toString()
                );
                lifecycleScope.launch {
                    Pedir_datos_apis_post(
                        endpoint = "/inventario/tareas/acomodo/item/set/defecto",
                        body=body,
                        dataClass = Any::class,
                        listaKey = "message",
                        headers = headers,
                        onSuccess = { response ->
                            try {
                                try {
                                    val message = response.toString()
                                    if(message.contains("ACOMODO REALIZADO")){
                                        MensajesDialogConfirmaciones.showMessage(this@Acomodo_confirmacion, "Confirmado con exito") {
                                            finish();}
                                    }else{
                                        MensajesDialog.showMessage(this@Acomodo_confirmacion, "Respuesta: $message");
                                    }
                                } catch (e: Exception) {
                                    MensajesDialog.showMessage(this@Acomodo_confirmacion, "Error al procesar la respuesta: ${e.message}")
                                }

                            } catch (e: Exception) {
                                MensajesDialog.showMessage(this@Acomodo_confirmacion, "Error al procesar la respuesta: ${e.message}")
                            }
                        },
                        onError = { error ->
                            MensajesDialog.showMessage(this@Acomodo_confirmacion, "Error: ${error}")
                        }
                    )
                }


            }catch (e: Exception){
                MensajesDialog.showMessage(this@Acomodo_confirmacion, "Ocurrió un error: ${e.message}");
            }

        }else{
            MensajesDialog.showMessage(this,"Se deben de llenar todos los datos seleccionados");
        }

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