package com.example.slimmx.Reacomodo.Manual

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
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
import com.example.slimmx.Validaciones.BoxValidator
import com.example.slimmx.R
import com.example.slimmx.ReubicacionConfir
import com.example.slimmx.Submenus.Submenu_reacomodo
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.example.slimmx.Vista.FragmentPage3
import com.example.slimmx.Vista.FragmentPageEtiquetaBluetooth
import com.example.slimmx.Vista.FragmentPageImpresionEtiquetas
import com.example.slimmx.sugerenciaUbicacion
import com.example.slimmx.validarUnidades
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private lateinit var btn_eliminarUbicacionOriginal: Button
private lateinit var txtCodigo: EditText
private lateinit var txtUbicacionOrigen: EditText
private lateinit var txtBoxOrigen: EditText
private lateinit var txtUbicacionDestino: EditText
private lateinit var txtBoxDestino: EditText
private lateinit var txtUbicacionDestinoConfir: EditText
private lateinit var txtBoxDestinoConfir: EditText
private lateinit var txtUnidadesMover: EditText
private lateinit var txtlbUnidades: TextView
private lateinit var lb_ubicacion_destinoConfirm: TextView
private lateinit var lb_box_destinoConfir: TextView
private lateinit var lb_unidades_destino: TextView
private lateinit var btn_eliminarCodigo: Button
private lateinit var btn_eliminarBoxOriginal: Button
private lateinit var btn_eliminarUbicacionDestino: Button
private lateinit var btn_eliminarBoxDestino: Button
private lateinit var btn_eliminarUbicacionDestinoConfir: Button
private lateinit var btn_eliminarBoxDestinoConfir: Button
private lateinit var btn_cancelar: Button
private lateinit var btn_confirmar: Button
private lateinit var btn_validar: Button
private lateinit var btn_segunda_val: Button
private lateinit var cbMotivo_Manual: AutoCompleteTextView;


class Reacomodo_manual : AppCompatActivity(), FragmentPageImpresionEtiquetas.OnBackgroundBlockerListener, FragmentPage3.OnBackgroundBlockerListener{

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
        setContentView(R.layout.activity_reacomodo_manual)
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

        txtCodigo = findViewById(R.id.txtCodigoManual)
        txtUbicacionOrigen = findViewById(R.id.txtUbicacionOrigen_Manual)
        txtBoxOrigen = findViewById(R.id.txtBoxOrigen_Manual)
        txtUbicacionDestino = findViewById(R.id.txtUbicacion_Destino_Manual)
        txtBoxDestino = findViewById(R.id.txtBox_Destino_Manual)
        txtUbicacionDestinoConfir = findViewById(R.id.txtUbicacion_Destino_Manual_Confirmacion)
        txtBoxDestinoConfir = findViewById(R.id.txtBox_Destino_Manual_Confirmacion)
        txtUnidadesMover = findViewById(R.id.txtUnidadesMover)
        txtlbUnidades = findViewById(R.id.txtLbUnidades)
        lb_ubicacion_destinoConfirm = findViewById(R.id.LbUbicacion_Destino_Manual_Confirmacion)
        lb_box_destinoConfir = findViewById(R.id.LbBox_Destino_Manual_Confirmacion)
        lb_unidades_destino = findViewById(R.id.txtUnidadesDestino)
        btn_eliminarCodigo = findViewById(R.id.buttonEliminar_Codigo_Manual)
        btn_eliminarUbicacionOriginal = findViewById(R.id.buttonEliminar_Ubicacion_Original_Manual)
        btn_eliminarBoxOriginal = findViewById(R.id.buttonEliminar_Box_Origen_Manual)
        btn_eliminarUbicacionDestino = findViewById(R.id.buttonEliminar_Ubicaciones_destino_Manual)
        btn_eliminarBoxDestino = findViewById(R.id.buttonEliminar_Box_destino_Manual)
        btn_eliminarUbicacionDestinoConfir = findViewById(R.id.buttonEliminar_Ubicaciones_destino_Manual_Confirmacion)
        btn_eliminarBoxDestinoConfir = findViewById(R.id.buttonEliminar_Box_destino_Manual_Confirmacion)
        btn_cancelar = findViewById(R.id.buttonCancelar_Reacomodo_Manual)
        btn_confirmar = findViewById(R.id.buttonConfirmar_Reacomodo_manual)
        btn_validar = findViewById(R.id.buttonValidar)
        btn_segunda_val = findViewById(R.id.button_SegundaConfir)
        cbMotivo_Manual=findViewById(R.id.cbMotivo_Manual);

        lb_ubicacion_destinoConfirm.isVisible = false;
        txtUbicacionDestinoConfir.isVisible = false;
        btn_eliminarUbicacionDestinoConfir.isVisible = false;

        lb_box_destinoConfir.isVisible = false;
        txtBoxDestinoConfir.isVisible = false;
        btn_eliminarBoxDestinoConfir.isVisible = false;

        btn_confirmar.isVisible = false;


        txtCodigo.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txtUbicacionOrigen.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txtBoxOrigen.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txtUbicacionDestino.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txtBoxDestino.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txtUbicacionDestinoConfir.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txtBoxDestinoConfir.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        btn_eliminarCodigo.setOnClickListener {
            txtCodigo.setText("");
            txtCodigo.post { txtCodigo.requestFocus() }
        }

        btn_eliminarUbicacionOriginal.setOnClickListener {
            txtUbicacionOrigen.setText("");
            txtUbicacionOrigen.post { txtUbicacionOrigen.requestFocus() }
        }

        btn_eliminarBoxOriginal.setOnClickListener {
            txtBoxOrigen.setText("");
            txtBoxOrigen.post { txtBoxOrigen.requestFocus() }
        }

        btn_eliminarUbicacionDestino.setOnClickListener {
            txtUbicacionDestino.setText("");
            txtUbicacionDestino.post { txtUbicacionDestino.requestFocus() }
        }

        btn_eliminarBoxDestino.setOnClickListener {
            txtBoxDestino.setText("");
            txtBoxDestino.post { txtBoxDestino.requestFocus() }
        }

        btn_eliminarUbicacionDestinoConfir.setOnClickListener {
            txtUbicacionDestinoConfir.setText("");
            txtUbicacionDestinoConfir.post { txtUbicacionDestinoConfir.requestFocus() }
        }

        btn_eliminarBoxDestinoConfir.setOnClickListener {
            txtBoxDestinoConfir.setText("");
            txtBoxDestinoConfir.post { txtBoxDestinoConfir.requestFocus() }
        }

        btn_cancelar.setOnClickListener {
            finish();
        }

        //Combo
        val opcionesDefault = listOf("AGRUPACION", "CAMBIO DE AREA", "DISEÑO", "CONTROL CALIDAD")


        val adaptador = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opcionesDefault)
        cbMotivo_Manual.setAdapter(adaptador)

        cbMotivo_Manual.setOnItemClickListener { _, _, position, _ ->
            try {
                when (opcionesDefault[position]) {
                    "AGRUPACION" -> {
                        btn_confirmar.isVisible = true;
                    }
                    "CAMBIO DE AREA" -> {
                        btn_confirmar.isVisible = true;
                    }
                    "DISEÑO" -> {
                        btn_confirmar.isVisible = true;
                    }
                    "CONTROL CALIDAD" -> {
                        btn_confirmar.isVisible = true;
                    }

                }
            }catch (e: Exception){
                MensajesDialog.showMessage(this@Reacomodo_manual, "Ocurrió un error: ${e.message}");
            }

        }


        txtCodigo.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtCodigo.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        txtUbicacionOrigen.post { txtUbicacionOrigen.requestFocus() }
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }

        txtUbicacionOrigen.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtUbicacionOrigen.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        if (txtBoxOrigen.text.toString().isNullOrEmpty()) {
                            txtBoxOrigen.setText("S/B");
                        }
                        validar(
                            txtCodigo.text.toString(),
                            txtUbicacionOrigen.text.toString(),
                            txtBoxOrigen.text.toString(),
                            1
                        );
                        sugerencia(txtCodigo.text.toString(), txtUbicacionOrigen.text.toString());
                        txtUnidadesMover.post { txtUnidadesMover.requestFocus() }
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }

        txtBoxOrigen.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtBoxOrigen.text.toString()
                    if (!inputText.isNullOrEmpty()) {

                        BoxValidator.validateBox(
                            input = txtBoxOrigen.text.toString(),
                            successComponent = {
                                validar(
                                    txtCodigo.text.toString(),
                                    txtUbicacionOrigen.text.toString(),
                                    txtBoxOrigen.text.toString(),
                                    2
                                );
                                sugerencia(txtCodigo.text.toString(), txtUbicacionOrigen.text.toString());
                            },
                            failureComponent = { message ->
                                MensajesDialog.showMessage(this, message)
                                txtBoxOrigen.setText("")
                                txtBoxOrigen.post { txtBoxOrigen.requestFocus() }
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

        btn_validar.setOnClickListener {
            try {
                if (txtBoxOrigen.text.toString().isNullOrEmpty()) {
                    txtBoxOrigen.setText("S/B");
                }
                validar(
                    txtCodigo.text.toString(),
                    txtUbicacionOrigen.text.toString(),
                    txtBoxOrigen.text.toString(),
                    1
                );
            }catch (e: Exception){
                MensajesDialog.showMessage(this@Reacomodo_manual, "Ocurrió un error: ${e.message}");
            }

        }

        btn_segunda_val.setOnClickListener {
            try {
                if (txtBoxOrigen.text.toString().isNotEmpty() && txtUbicacionOrigen.text.toString().isNotEmpty() && txtCodigo.text.toString().isNotEmpty()&&
                    txtUbicacionDestino.text.toString().isNotEmpty() && txtBoxDestino.text.toString().isNotEmpty() && txtUnidadesMover.text.toString().isNotEmpty() &&
                    txtUnidadesMover.text.toString().toInt()>0){

                    txtUbicacionOrigen.isEnabled=false;
                    txtBoxOrigen.isEnabled=false;
                    txtCodigo.isEnabled=false;
                    btn_eliminarCodigo.isEnabled=false;
                    btn_eliminarUbicacionOriginal.isEnabled=false;
                    btn_eliminarBoxOriginal.isEnabled=false;


                    lb_ubicacion_destinoConfirm.isVisible = true;
                    txtUbicacionDestinoConfir.isVisible = true;
                    btn_eliminarUbicacionDestinoConfir.isVisible = true;

                    lb_box_destinoConfir.isVisible = true;
                    txtBoxDestinoConfir.isVisible = true;
                    btn_eliminarBoxDestinoConfir.isVisible = true;

                    txtUbicacionDestinoConfir.post { txtUbicacionDestinoConfir.requestFocus() }

                }else{
                    MensajesDialog.showMessage(this, "Hay datos vacios");
                }
            }catch (e: Exception){
                MensajesDialog.showMessage(this@Reacomodo_manual, "Ocurrió un error: ${e.message}");
            }

        }


        txtUbicacionDestino.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtUbicacionDestino.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        txtBoxDestino.post { txtBoxDestino.requestFocus() }
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }


        txtUbicacionDestinoConfir.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtUbicacionDestinoConfir.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        txtBoxDestinoConfir.post { txtBoxDestinoConfir.requestFocus() }
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }

        txtBoxDestinoConfir.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtBoxDestinoConfir.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        BoxValidator.validateBox(
                            input = txtBoxDestinoConfir.text.toString(),
                            successComponent = {

                            },
                            failureComponent = { message ->
                                MensajesDialog.showMessage(this, message)
                                txtBoxDestinoConfir.setText("")
                                txtBoxDestinoConfir.post { txtBoxDestinoConfir.requestFocus() }
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

        btn_confirmar.setOnClickListener {
            try {
                if (txtUbicacionDestino.text.toString().equals(txtUbicacionDestinoConfir.text.toString()) && txtBoxDestino.text.toString().equals(txtBoxDestinoConfir.text.toString())){
                    if (txtUnidadesMover.text.toString().toInt()<=txtlbUnidades.text.toString().toInt()){
                        if (txtUnidadesMover.text.toString().toInt()>0){

                            confirmarReacomodo(txtCodigo.text.toString(), txtUnidadesMover.text.toString().toInt(),txtUbicacionOrigen.text.toString(), txtBoxOrigen.text.toString(),txtUbicacionDestinoConfir.text.toString(),txtBoxDestinoConfir.text.toString());

                        }else{
                            MensajesDialog.showMessage(this, "No puedes mover 0 unidades");
                            btn_confirmar.isEnabled=true;
                        }

                    }else{
                        MensajesDialog.showMessage(this, "No tienes suficientes unidades para mover");
                        btn_confirmar.isEnabled=true;
                    }

                }else{
                    MensajesDialog.showMessage(this,"Las ubicaciones no coinciden");
                    btn_confirmar.isEnabled=true;
                }
            }catch (e: Exception){
                MensajesDialog.showMessage(this@Reacomodo_manual, "Ocurrió un error: ${e.message}");
                btn_confirmar.isEnabled=true;
            }
        }

        val buttonMenu = findViewById<Button>(R.id.buttonMenu_Reacomodo)
        buttonMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, buttonMenu)
            popupMenu.menuInflater.inflate(R.menu.menu_opciones, popupMenu.menu)
            val menuItemVerImagen = popupMenu.menu.findItem(R.id.item_ver_imagen)
            menuItemVerImagen.isVisible = false
            val menuImpresionBluetooth=popupMenu.menu.findItem(R.id.item_etiquetas_bluetooth);
            menuImpresionBluetooth.isVisible=false;
            val menuRecomendacion=popupMenu.menu.findItem(R.id.item_recomendaciones);
            menuRecomendacion.isVisible=false;
            val menuItemCombosPacks=popupMenu.menu.findItem(R.id.item_packs_combos);
            menuItemCombosPacks.isVisible=false;
            val menuVerificacion=popupMenu.menu.findItem(R.id.item_verificacion);
            menuVerificacion.isVisible=false;
            val menuInventory=popupMenu.menu.findItem(R.id.item_inventario);
            menuInventory.isVisible=false;
            val menuImpresionEtiquetas=popupMenu.menu.findItem(R.id.item_impresora);
            menuImpresionEtiquetas.isVisible=false;
            popupMenu.setOnMenuItemClickListener { item ->
                try {
                    when (item.itemId) {
                        R.id.item_impresora -> {
                            true
                        }
                        R.id.item_f1 -> {
                            if (txtCodigo.text.toString().isNotEmpty()){
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
                                MensajesDialog.showMessage(this, "No se ha escaneado ningún código")
                            }

                            true
                        }
                        R.id.item_ver_imagen -> {
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

    private fun validar(codigo:String, ubicacion:String, box:String, tipo:Int){
        if (codigo.isNotEmpty() && ubicacion.isNotEmpty() && box.isNotEmpty()){
            try {
                val params= mapOf(
                    "codigo" to codigo.uppercase(),
                    "ubicacion" to ubicacion.uppercase(),
                    "box" to box.uppercase()
                );

                val headers = mapOf(
                    "Token" to GlobalUser.token.toString()
                );

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/productos/ubicacion/unidades",
                            params=params,
                            dataClass = validarUnidades::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val unidades =
                                            lista.map { it.UNIDADES }.toString().replace("[", "")
                                                .replace("]", "")
                                        txtlbUnidades.setText(unidades);
                                    } else {
                                        if (tipo == 2) {
                                            MensajesDialog.showMessage(
                                                this@Reacomodo_manual,
                                                "No hay unidades en esta ubicación"
                                            );
                                        }
                                    }
                                }
                            },
                            onError = {error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Reacomodo_manual, "${error}");
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Reacomodo_manual, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Reacomodo_manual, "Ocurrió un error: ${e.message}");
                }
            }
        }else{
            MensajesDialog.showMessage(this,"Ya sea el código, ubicación origen o el box origen estan vacios");
        }

    }

    private fun sugerencia(codigo:String, ubicacion:String){

        if (codigo.isNotEmpty() && ubicacion.isNotEmpty() ){
            try {
                val params = mapOf(
                    "codigo" to codigo.uppercase(),
                    "ubicacion" to ubicacion.uppercase()
                );
                val headers = mapOf(
                    "Token" to GlobalUser.token.toString()
                );
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/productos/ubicacion/recomendada",
                            params = params,
                            dataClass = sugerenciaUbicacion::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val ubicacion =
                                            lista.map { it.UBICACION }.toString().replace("[", "")
                                                .replace("]", "");
                                        val box = lista.map { it.BOX }.toString().replace("[", "")
                                            .replace("]", "");
                                        val unidades = lista.map { it.FISICO_DISPONIBLE }.toString()
                                            .replace("[", "").replace("]", "");


                                        if (ubicacion.equals("null")) {
                                            txtUbicacionDestino.setText("");
                                        } else {
                                            txtUbicacionDestino.setText(ubicacion);
                                        }
                                        if (ubicacion.equals("null")) {
                                            txtBoxDestino.setText("S/B");
                                        } else {
                                            txtBoxDestino.setText(box);
                                        }
                                        if (ubicacion.equals("null")) {
                                            lb_unidades_destino.setText("0");
                                        } else {
                                            lb_unidades_destino.setText(unidades);
                                        }

                                    } else {

                                        MensajesDialog.showMessage(this@Reacomodo_manual, "No hay sugerencia");
                                    }
                                }
                            },
                            onError = {error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Reacomodo_manual, "${error}");
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Reacomodo_manual, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Reacomodo_manual, "Ocurrió un error: ${e.message}");
                }
            }
        }else{
            MensajesDialog.showMessage(this,"Ya sea el código, ubicación origen o el box origen estan vacios");
        }

    }

    private fun confirmarReacomodo(codigo: String, unidades_confir:Int, ubicacion_origen:String,box_origen:String, ubicacion_destino:String,box_destino:String){
        if (codigo.isNotEmpty() && unidades_confir.toString().isNotEmpty() && ubicacion_origen.isNotEmpty() && box_origen.isNotEmpty() && ubicacion_destino.isNotEmpty() && box_destino.isNotEmpty()){
            try {
                btn_confirmar.isEnabled=false;
                val body= mapOf(
                    "ID" to  "*",
                    "ITEM" to "0",
                    "CODIGO" to codigo.uppercase(),
                    "UNIDADES_CONFIRMADAS" to unidades_confir.toString(),
                    "UBICACION_ORIGEN" to ubicacion_origen.uppercase(),
                    "BOX_ORIGEN" to box_origen.uppercase(),
                    "UBICACION_DESTINO" to ubicacion_destino.uppercase(),
                    "BOX_DESTINO" to box_destino.uppercase(),
                    "AREA_DESTINO" to "0",
                    "PROVEEDOR" to "",
                    "MOTIVO" to cbMotivo_Manual.text.toString()
                );
                val headers = mapOf(
                    "Token" to GlobalUser.token.toString()
                );

                Log.d("DEBUG", "boxOrigen recibido: $box_origen")
                Log.d("DEBUG", "boxDestino recibido: $box_destino")

                lifecycleScope.launch {
                    Pedir_datos_apis_post(
                        endpoint = "/inventario/tareas/reubicacion/item/set",
                        body=body,
                        dataClass = Any::class,
                        listaKey =  "message",
                        headers = headers,
                        onSuccess = {response ->
                            try {
                                try {
                                    val message = response.toString()
                                    if(message.contains("CONFIRMADO CON EXITO")){
                                        MensajesDialog.showMessage(this@Reacomodo_manual, "OK");
                                        lb_ubicacion_destinoConfirm.isVisible = false;
                                        txtUbicacionDestinoConfir.isVisible = false;
                                        btn_eliminarUbicacionDestinoConfir.isVisible = false;

                                        lb_box_destinoConfir.isVisible = false;
                                        txtBoxDestinoConfir.isVisible = false;
                                        btn_eliminarBoxDestinoConfir.isVisible = false;

                                        btn_confirmar.isVisible = false;

                                        btn_eliminarUbicacionDestinoConfir.performClick();
                                        btn_eliminarBoxDestinoConfir.performClick();

                                        txtUbicacionOrigen.isEnabled=true;
                                        txtBoxOrigen.isEnabled=true;
                                        txtUbicacionDestino.isEnabled=true;
                                        txtBoxDestino.isEnabled=true;
                                        txtCodigo.isEnabled=true;
                                        btn_eliminarCodigo.isEnabled=true;
                                        btn_eliminarUbicacionOriginal.isEnabled=true;
                                        btn_eliminarBoxOriginal.isEnabled=true;
                                        btn_eliminarUbicacionDestino.isEnabled=true;
                                        btn_eliminarBoxDestino.isEnabled=true;

                                        btn_eliminarCodigo.performClick();
                                        btn_eliminarUbicacionOriginal.performClick();
                                        btn_eliminarBoxOriginal.performClick();
                                        btn_eliminarUbicacionDestino.performClick();
                                        btn_eliminarBoxDestinoConfir.performClick();

                                        txtUnidadesMover.setText("");
                                        txtlbUnidades.setText("0");
                                        lb_unidades_destino.setText("0");
                                        txtBoxDestino.setText("");
                                        txtCodigo.post { txtCodigo.requestFocus() }
                                        btn_confirmar.isEnabled=true;
                                        cbMotivo_Manual.setText("");
                                    }else{
                                        MensajesDialog.showMessage(this@Reacomodo_manual, "Respuesta: $message");
                                        btn_confirmar.isEnabled=true;
                                    }
                                } catch (e: Exception) {
                                    MensajesDialog.showMessage(this@Reacomodo_manual, "Error al procesar la respuesta: ${e.message}")
                                }

                            } catch (e: Exception) {
                                MensajesDialog.showMessage(this@Reacomodo_manual, "Error al procesar la respuesta: ${e.message}")
                            }
                          /* if (lista.status.isNotEmpty()){
                                if (lista.status.equals("success")){
                                    MensajesDialog.showMessage(this@Reacomodo_manual, "OK");
                                    lb_ubicacion_destinoConfirm.isVisible = false;
                                    txtUbicacionDestinoConfir.isVisible = false;
                                    btn_eliminarUbicacionDestinoConfir.isVisible = false;

                                    lb_box_destinoConfir.isVisible = false;
                                    txtBoxDestinoConfir.isVisible = false;
                                    btn_eliminarBoxDestinoConfir.isVisible = false;

                                    btn_confirmar.isVisible = false;

                                    btn_eliminarUbicacionDestinoConfir.performClick();
                                    btn_eliminarBoxDestinoConfir.performClick();

                                    txtUbicacionOrigen.isEnabled=true;
                                    txtBoxOrigen.isEnabled=true;
                                    txtUbicacionDestino.isEnabled=true;
                                    txtBoxDestino.isEnabled=true;
                                    txtCodigo.isEnabled=true;
                                    btn_eliminarCodigo.isEnabled=true;
                                    btn_eliminarUbicacionOriginal.isEnabled=true;
                                    btn_eliminarBoxOriginal.isEnabled=true;
                                    btn_eliminarUbicacionDestino.isEnabled=true;
                                    btn_eliminarBoxDestino.isEnabled=true;

                                    btn_eliminarCodigo.performClick();
                                    btn_eliminarUbicacionOriginal.performClick();
                                    btn_eliminarBoxOriginal.performClick();
                                    btn_eliminarUbicacionDestino.performClick();
                                    btn_eliminarBoxDestinoConfir.performClick();

                                    txtUnidadesMover.setText("");
                                    txtlbUnidades.setText("0");
                                    lb_unidades_destino.setText("0");
                                    txtBoxDestino.setText("");
                                    txtCodigo.post { txtCodigo.requestFocus() }
                                    btn_confirmar.isEnabled=true;
                                    cbMotivo_Manual.setText("");
                                }
                            }else{
                                MensajesDialog.showMessage(this@Reacomodo_manual,"${lista.message}");
                                btn_confirmar.isEnabled=true;
                            }*/
                        }, onError = {error->
                            MensajesDialog.showMessage(this@Reacomodo_manual,"${error}");
                            btn_confirmar.isEnabled=true;
                        }
                    );
                }

            }catch (e: Exception){
                MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                btn_confirmar.isEnabled=true;
            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_reacomodo::class.java));
        //LogsEntradaSalida.logsPorModulo(this, lifecycleScope, "ACOMODO/REACOMODO/MANUAL", "SALIDA")
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