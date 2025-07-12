package com.example.slimmx.Picking.Recolecta.Picking

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListaPickingRecolecta_valid
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.R
import com.example.slimmx.Validaciones.BoxValidator
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.example.slimmx.Vista.FragmentPage3
import com.example.slimmx.Vista.ImageFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class Picking_recolecta_confirmacion : AppCompatActivity() , FragmentPage3.OnBackgroundBlockerListener, ImageFragment.OnBackgroundBlockerListener{
    private var token :String="";
    private var job: Job? = null

    override fun showBackgroundBlocker() {
        val backgroundBlockerView: View = findViewById(R.id.background_blocker_P1_Recolecta)
        backgroundBlockerView.visibility = View.VISIBLE
        backgroundBlockerView.setOnTouchListener { _, _ -> true }

    }

    override fun hideBackgroundBlocker() {
        val backgroundBlockerView: View = findViewById(R.id.background_blocker_P1_Recolecta)
        backgroundBlockerView.visibility = View.GONE
        backgroundBlockerView.setOnTouchListener(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_picking_recolecta_confirmacion)
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

        this@Picking_recolecta_confirmacion.token=GlobalUser.token.toString();

        val v_packId = intent.getStringExtra("packId")
        val v_codigo = intent.getStringExtra("codigo")
        val v_descripcion = intent.getStringExtra("descripcion")
        val v_ubicacion = intent.getStringExtra("ubicacion")
        val v_box = intent.getStringExtra("box")
        val v_confirmado = intent.getStringExtra("confirmado")
        val v_almacenId = intent.getStringExtra("almacenId")
        val v_areaId = intent.getStringExtra("areaId")
        val v_numeroGuia = intent.getStringExtra("numeroGuia")
        val v_ubicacionOriginal = intent.getStringExtra("ubicacionOriginal")
        val v_boxOriginal = intent.getStringExtra("boxOriginal")
        val v_item = intent.getStringExtra("item");
        val v_tipo=intent.getStringExtra("tipo");
        val v_codigo_referencia=intent.getStringExtra("codigo_referencia");


        val txtPack = findViewById<TextView>(R.id.txtFolio_pi_re_confirm);
        val txtCodigo = findViewById<TextView>(R.id.txtSugeridoCodigo_pi_re_acc);
        val txtUbiSugerida = findViewById<TextView>(R.id.txtSugeridoUbi_pi_re_acc);
        val txtBoxSugerida = findViewById<TextView>(R.id.txtSugeridoBox_pi_re_acc);
        val txtDescSugerida = findViewById<TextView>(R.id.lbDescripcion_pi_re_confirm);
        val txtCantidadSugerida = findViewById<TextView>(R.id.txtSugeridoCantidad_pi_re_acc);

        val EditUbicacion=findViewById<EditText>(R.id.editTxtUbic_PI_RE_ACC);
        val btn_EliminarUbicacion=findViewById<Button>(R.id.buttonEliminar_Ubicacion_Recolecta_conAcc);

        val lbBox=findViewById<TextView>(R.id.lbbox_pi_re_confirm);
        val EditTextBox=findViewById<EditText>(R.id.editTxtBox_PI_RE_ACC);
        val btn_Eliminar_box=findViewById<Button>(R.id.buttonEliminar_Box_Recolecta_pi_ACC);

        val lbCodigo=findViewById<TextView>(R.id.lbCodigo_pi_re_confirm);
        val EditTextCodigo=findViewById<EditText>(R.id.editTxtCodigo_PI_RE_ACC);
        val btn_Eliminar_Codigo=findViewById<Button>(R.id.buttonEliminar_codigo_Recolecta_PI_ACC);

        val lbDescripcion=findViewById<TextView>(R.id.lbDescripcion_pi_re_confirm);

        val lbCantidad=findViewById<TextView>(R.id.lbCantidad_pi_re_confirm);
        val EditTextCantidad=findViewById<EditText>(R.id.editTxtCantidad_PI_RE_ACC);


        val btn_Confirmar=findViewById<Button>(R.id.buttonConfirmar_PI_Re_Acc);
        val btn_cancelar=findViewById<Button>(R.id.buttonCancelar_PI_Re_Acc);

        val txtTipo=findViewById<TextView>(R.id.txtTipo);
        val imagen_tipo=findViewById<ImageView>(R.id.imagen_tipo)

        val lbCodigoPack=findViewById<TextView>(R.id.lbPACKCOMBO);

        lbBox.isVisible=false;
        EditTextBox.isVisible=false;
        txtBoxSugerida.isVisible=false;
        btn_Eliminar_box.isVisible=false;

        lbCodigo.isVisible=false;
        EditTextCodigo.isVisible=false;
        txtCodigo.isVisible=false;
        btn_Eliminar_Codigo.isVisible=false;

        lbDescripcion.isVisible=false;

        lbCantidad.isVisible=false;
        EditTextCantidad.isVisible=false;
        txtCantidadSugerida.isVisible=false;
        btn_Confirmar.isEnabled=false;

        if (v_tipo.toString().equals("COMBO")){
            imagen_tipo.setImageResource(R.drawable.logo_combo)
            txtTipo.setText("COMBO");
            EditTextCodigo.setText(v_codigo);
            lbCodigoPack.setText(v_codigo_referencia);
        }

        if (v_tipo.toString().equals("PACK")){
            imagen_tipo.setImageResource(R.drawable.pack_combo)
            txtTipo.setText("PACK");
            EditTextCodigo.setText(v_codigo);
            lbCodigoPack.setText(v_codigo_referencia);
        }

        if (v_tipo.toString().equals("GENERAL") || v_tipo.toString().isNullOrEmpty()){
            imagen_tipo.isVisible=false;
            txtTipo.isVisible=false;
        }
        EditUbicacion.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        EditTextBox.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        //EditTextCodigo.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        txtPack.setText(v_packId);
        txtCodigo.setText(v_codigo);
        txtUbiSugerida.setText(v_ubicacion);
        if (v_box=="S/B"){
            EditTextBox.setText(v_box);

        }
        txtBoxSugerida.setText(v_box);
        txtDescSugerida.setText(v_descripcion);
        txtCantidadSugerida.setText(v_confirmado);

        //if (!GlobalUser.roles.toString().contains("DESARROLLADOR")){
            //Ocultar los componentes

        /*}else{
            EditUbicacion.setText(v_ubicacion);
            EditTextBox.setText(v_box);
            EditTextCodigo.setText(v_codigo);
            EditTextCantidad.setText(v_confirmado);
            btn_Confirmar.isEnabled=true;
            EditTextCantidad.post { EditTextCantidad.requestFocus() }
        }*/

        btn_EliminarUbicacion.setOnClickListener {
            EditUbicacion.setText("");
        }

        btn_Eliminar_box.setOnClickListener {
            EditTextBox.setText("");
        }

        btn_Eliminar_Codigo.setOnClickListener {
            EditTextCodigo.setText("");
        }

        btn_cancelar.setOnClickListener {
            finish();
        }


        EditUbicacion.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = EditUbicacion.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        ValidarCodigo(v_codigo.toString(),EditUbicacion.text.toString(),v_box.toString(), v_confirmado.toString(), "en_ubica");
                        if (v_tipo.toString().equals("PACK") || v_tipo.toString().equals("COMBO")){
                            lbCantidad.isVisible=true;
                            txtCantidadSugerida.isVisible=true;
                            EditTextCantidad.isVisible=true;
                            EditTextCantidad.post { EditTextCantidad.requestFocus() }
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


        EditTextBox.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = EditTextBox.text.toString()
                    if (!inputText.isNullOrEmpty()) {

                        BoxValidator.validateBox(
                            input = EditTextBox.text.toString(),
                            successComponent = {
                                ValidarCodigo(v_codigo.toString(),EditUbicacion.text.toString(),EditTextBox.text.toString(), v_confirmado.toString(), "en_box");
                                lbCodigo.isVisible=true;
                                EditTextCodigo.isVisible=true;
                                btn_Eliminar_Codigo.isVisible=true;
                                txtCodigo.isVisible=true;
                                btn_Eliminar_Codigo.isVisible=true;
                                EditTextCodigo.post { EditTextCodigo.requestFocus() }

                                if (v_tipo.toString().equals("PACK") || v_tipo.toString().equals("COMBO")){
                                    lbCantidad.isVisible=true;
                                    txtCantidadSugerida.isVisible=true;
                                    EditTextCantidad.isVisible=true;
                                    EditTextCantidad.post { EditTextCantidad.requestFocus() }
                                }

                            },
                            failureComponent = { message ->
                                MensajesDialog.showMessage(this, message)
                                EditTextBox.setText("")
                                EditTextBox.post { EditTextBox.requestFocus() }
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


        EditTextCodigo.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = EditTextCodigo.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        if (EditTextCodigo.text.toString().equals(txtCodigo.text.toString())){
                            lbCantidad.isVisible=true;
                            txtCantidadSugerida.isVisible=true;
                            EditTextCantidad.isVisible=true;
                            EditTextCantidad.post { EditTextCantidad.requestFocus() }

                        }else{
                            MensajesDialog.showMessage(this@Picking_recolecta_confirmacion,"El código es incorrecto");
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

        EditTextCantidad.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                try {
                    var cantidad=EditTextCantidad.text.toString();
                    if (!s.isNullOrEmpty()) {
                        //if (cantidad.toInt()>0 && cantidad.isNotEmpty() && cantidad.isNotBlank()){
                            btn_Confirmar.isEnabled=true;
                       // }else{
                            //MensajesDialog.showMessage(this@Picking_recolecta_confirmacion,"No puedes confirmar 0 unidades");
                           // btn_Confirmar.isEnabled=false;
                        //}
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this@Picking_recolecta_confirmacion, "Ocurrió un error: ${e.message}");
                }
            }
        })


        btn_Confirmar.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmación")
            builder.setMessage("¿Estás seguro de que deseas confirmar?")

            builder.setPositiveButton("Sí") { dialog, which ->
                //if( EditTextCantidad.text.toString().toInt()>0 && EditTextCantidad.text.toString().toInt()<=v_confirmado.toString().toInt()){
                    try {
                        Confirmacion_datos(v_packId.toString(), EditTextCodigo.text.toString(),v_descripcion.toString(),EditUbicacion.text.toString(),EditTextBox.text.toString(),
                            EditTextCantidad.text.toString(), v_almacenId.toString(),v_areaId.toString(), v_numeroGuia.toString(),v_ubicacionOriginal.toString(),v_boxOriginal.toString(),v_item.toString())
                        btn_Confirmar.isEnabled=false;
                    }catch (e: Exception){
                        MensajesDialog.showMessage(this@Picking_recolecta_confirmacion, "Ocurrió un error: ${e.message}");
                        btn_Confirmar.isEnabled=true;
                    }
                /*}else{
                    MensajesDialog.showMessage(this, "No puede confirmar piezas de mas ni confirmar 0 piezas");
                }*/

            }

            builder.setNegativeButton("No") { dialog, which ->

            }
            builder.show()

        }


        val buttonMenu = findViewById<Button>(R.id.buttonMenu_Pi_Recolecta)
        buttonMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, buttonMenu)
            popupMenu.menuInflater.inflate(R.menu.menu_opciones, popupMenu.menu)
            val menuImpresion = popupMenu.menu.findItem(R.id.item_impresora)
            menuImpresion.isVisible = false
            val menuImpresionBluetooth=popupMenu.menu.findItem(R.id.item_etiquetas_bluetooth);
            menuImpresionBluetooth.isVisible=false;
            val menuRecomendacion=popupMenu.menu.findItem(R.id.item_recomendaciones);
            menuRecomendacion.isVisible=false;
            val menuCombosPacks=popupMenu.menu.findItem(R.id.item_packs_combos);
            menuCombosPacks.isVisible=false;
            val menuVerificacion=popupMenu.menu.findItem(R.id.item_verificacion);
            menuVerificacion.isVisible=false;
            val menuInventory=popupMenu.menu.findItem(R.id.item_inventario);
            menuInventory.isVisible=false;
            popupMenu.setOnMenuItemClickListener { item ->
                try {
                    when (item.itemId) {
                        R.id.item_impresora -> {
                            true
                        }
                        R.id.item_f1 -> {
                            if (txtCodigo.text.toString().isNotEmpty()){
                                val fragmentPage3 = FragmentPage3.newInstance(txtCodigo.text.toString())

                                val fragmentContainerView = findViewById<View>(R.id.fragmentContainerView_f1_recolecta)
                                val backgroundBlocker = findViewById<View>(R.id.background_blocker_P1_Recolecta)

                                val transaction = supportFragmentManager.beginTransaction()
                                val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView_f1_recolecta)

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
                                    transaction.add(R.id.fragmentContainerView_f1_recolecta, fragmentPage3);
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
                            val codigo = v_codigo.toString()
                            val fragmentContainer = findViewById<View>(R.id.viewSwitcher_Pi_Recolecta)
                            val backgroundBlocker = findViewById<View>(R.id.background_blocker_P1_Recolecta)

                            if (codigo.isNotEmpty()) {
                                val transaction = supportFragmentManager.beginTransaction()
                                val existingFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerViewImagen)

                                if (existingFragment != null && existingFragment.isVisible) {
                                    transaction.hide(existingFragment)
                                    transaction.commitNow()
                                    backgroundBlocker.visibility = View.GONE
                                    backgroundBlocker.setOnTouchListener(null)
                                } else {
                                    transaction.replace(R.id.fragmentContainerViewImagen, ImageFragment.newInstance(codigo))
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

    private fun ValidarCodigo(codigo :String, ubicacion: String, box: String, unidades:String, enviado: String){
        if(codigo.isNotEmpty() && ubicacion.isNotEmpty() && box.isNotEmpty() && unidades.isNotEmpty()){
            try {
                val params= mapOf(
                    "codigo" to codigo.uppercase(),
                    "ubicacion" to ubicacion.uppercase(),
                    "box" to box.uppercase(),
                    "unidades" to unidades
                );

                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch(Dispatchers.IO) {
                    try{
                        Pedir_datos_apis(
                            endpoint = "/productos/ubicacion/unidades/valid/hand",
                            params = params,
                            listaKey = "result",
                            dataClass = ListaPickingRecolecta_valid::class,
                            headers =headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        if (lista.map { it.resultado }.toString()
                                                .contains("UNIDADES SUFICIENTES")
                                        ) {
                                            val lbBox =
                                                findViewById<TextView>(R.id.lbbox_pi_re_confirm);
                                            val EditTextBox =
                                                findViewById<EditText>(R.id.editTxtBox_PI_RE_ACC);
                                            val btn_Eliminar_box =
                                                findViewById<Button>(R.id.buttonEliminar_Box_Recolecta_pi_ACC);
                                            val txtBoxSugerida =
                                                findViewById<TextView>(R.id.txtSugeridoBox_pi_re_acc);

                                            val lbCodigo =
                                                findViewById<TextView>(R.id.lbCodigo_pi_re_confirm);
                                            val EditTextCodigo =
                                                findViewById<EditText>(R.id.editTxtCodigo_PI_RE_ACC);
                                            val btn_Eliminar_Codigo =
                                                findViewById<Button>(R.id.buttonEliminar_codigo_Recolecta_PI_ACC);
                                            val txtCodigo =
                                                findViewById<TextView>(R.id.txtSugeridoCodigo_pi_re_acc);
                                            val lbDescripcion =
                                                findViewById<TextView>(R.id.lbDescripcion_pi_re_confirm);

                                            //MensajesDialog.showMessage(this@Picking_recolecta_confirmacion_Acc, "Si hay unidades disponibles.")
                                            if (box == "S/B") {
                                                lbBox.isVisible = true;
                                                EditTextBox.isVisible = true;
                                                btn_Eliminar_box.isVisible = true;
                                                txtBoxSugerida.isVisible = true;

                                                lbDescripcion.isVisible = true;

                                                lbCodigo.isVisible = true;
                                                EditTextCodigo.isVisible = true;
                                                btn_Eliminar_Codigo.isVisible = true;
                                                txtCodigo.isVisible = true;
                                                EditTextCodigo.post { EditTextCodigo.requestFocus() }

                                            } else {
                                                lbBox.isVisible = true;
                                                EditTextBox.isVisible = true;
                                                btn_Eliminar_box.isVisible = true;
                                                lbDescripcion.isVisible = true;
                                                EditTextBox.post { EditTextBox.requestFocus() }
                                                txtBoxSugerida.isVisible = true;
                                                if (enviado == "en_box") {
                                                    EditTextCodigo.post { EditTextCodigo.requestFocus() }
                                                }
                                            }
                                            // MensajesDialog.showMessage(this, "Unidades Suficientes")
                                        } else {
                                            MensajesDialog.showMessage(
                                                this@Picking_recolecta_confirmacion,
                                                "Unidades insuficientes"
                                            )
                                        }

                                    } else {

                                    }
                                }
                            },
                            onError = { error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (error.equals("OK", ignoreCase = true)) {
                                        MensajesDialog.showMessage(
                                            this@Picking_recolecta_confirmacion,
                                            "Si hay unidades disponibles."
                                        )
                                    } else {
                                        MensajesDialog.showMessage(
                                            this@Picking_recolecta_confirmacion,
                                            "Error: $error"
                                        )
                                    }
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Picking_recolecta_confirmacion, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Picking_recolecta_confirmacion, "Ocurrió un error: ${e.message}");
                }
            }

        }else{
            MensajesDialog.showMessage(this,"Datos inconpletos");
        }
    }

    private fun Confirmacion_datos(id:String,codigo: String, descripcion:String,ubicacion: String, box:String, confirmado:String, almacen_id:String, area_id:String, paquete:String,
                                   ubicacion_original:String, box_original:String,item:String){
        if (id.isNotEmpty() && codigo.isNotEmpty() && descripcion.isNotEmpty() && ubicacion.isNotEmpty() && box.isNotEmpty() && confirmado.isNotEmpty() && almacen_id.isNotEmpty() &&
            area_id.isNotEmpty() && paquete.isNotEmpty() && ubicacion_original.isNotEmpty() && box_original.isNotEmpty() && item.isNotEmpty()) {
            try {
                val btn_Confirmar=findViewById<Button>(R.id.buttonConfirmar_PI_Re_Acc);
                val body = mapOf(
                    "ID" to id,
                    "ALMACEN_ID" to almacen_id,
                    "AREA_ID" to area_id,
                    "UBICACION" to ubicacion.uppercase(),
                    "BOX" to box.uppercase(),
                    "CODIGO" to codigo.uppercase(),
                    "PAQUETE" to paquete,
                    "CONFIRMADAS" to confirmado,
                    "OBSOLETAS" to "0",
                    "DANADAS" to "0",
                    "UBICACION_ORIGINAL" to ubicacion_original.uppercase(),
                    "BOX_ORIGINAL" to box_original.uppercase()
                )

                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch {
                    Pedir_datos_apis_post(
                        endpoint = "/fulfillment/picking/works/items/confirm/paquete",
                        body = body,
                        dataClass =Any::class,
                        listaKey = "message",
                        headers=headers,
                        onSuccess = { response ->
                            try {
                                try {
                                    val message = response.toString()
                                    if(message.contains("PAQUETE CONFIRMADO CON EXITO")){
                                        MensajesDialogConfirmaciones.showMessage(this@Picking_recolecta_confirmacion, "OK") {
                                            finish();}
                                    }else{
                                        MensajesDialog.showMessage(this@Picking_recolecta_confirmacion, "Respuesta: $message");
                                        btn_Confirmar.isEnabled=true;
                                    }
                                } catch (e: Exception) {
                                    MensajesDialog.showMessage(this@Picking_recolecta_confirmacion, "Error al procesar la respuesta: ${e.message}")
                                    btn_Confirmar.isEnabled=true;
                                }

                            } catch (e: Exception) {
                                MensajesDialog.showMessage(this@Picking_recolecta_confirmacion, "Error al procesar la respuesta: ${e.message}")
                                btn_Confirmar.isEnabled=true;
                            }
                        },
                        onError = { error ->
                            MensajesDialog.showMessage(this@Picking_recolecta_confirmacion, "Error: $error")
                            btn_Confirmar.isEnabled=true;
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
    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Cancelar operación")
            .setMessage("¿Estás seguro de que deseas cancelar la operación?")
            .setPositiveButton("Sí") { _, _ ->
               // job?.cancel()  // Cancela la petición
                finish()  // Cierra la actividad
            }
            .setNegativeButton("No", null)
            .show()
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