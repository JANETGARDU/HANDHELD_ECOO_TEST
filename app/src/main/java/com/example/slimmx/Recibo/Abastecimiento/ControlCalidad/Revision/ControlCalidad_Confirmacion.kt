package com.example.slimmx.Recibo.Abastecimiento.ControlCalidad.Revision

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
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
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalUser
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.R
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Vista.FragmentPage3
import com.example.slimmx.Vista.FragmentPageEtiquetaBluetooth
import com.example.slimmx.Vista.FragmentPageImpresionEtiquetas
import com.example.slimmx.Vista.ImageFragment
import com.example.slimmx.defectos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ControlCalidad_Confirmacion : AppCompatActivity(), FragmentPageImpresionEtiquetas.OnBackgroundBlockerListener, FragmentPageEtiquetaBluetooth.OnBackgroundBlockerListener , ImageFragment.OnBackgroundBlockerListener {

    private lateinit var cbSelect: AutoCompleteTextView

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
        setContentView(R.layout.activity_control_calidad_confirmacion)
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

        cbSelect = findViewById(R.id.cbSelect);
        val btn_confirmacion=findViewById<Button>(R.id.buttonConfirmar);
        val txtObservaciones=findViewById<EditText>(R.id.txtObservaciones);

        val v_codigo = intent.getStringExtra("codigo");
        val v_descripcion = intent.getStringExtra("descripcion");
        val v_cantidad=intent.getStringExtra("cantidad");
        val v_item_producto=intent.getStringExtra("item_confirm");
        val v_folio=intent.getStringExtra("folio");
        val v_id=intent.getStringExtra("id");

        val txtFolio=findViewById<TextView>(R.id.txtFolio);
        val txtCodigo=findViewById<TextView>(R.id.txtCodigo);
        val lb_Descripcion=findViewById<TextView>(R.id.lb_Descripcion);
        val txtCantidad=findViewById<TextView>(R.id.txtCantidad);
        val btn_cancelar=findViewById<Button>(R.id.buttonCancelar_Calidad);

        txtFolio.setText(v_folio);
        txtCodigo.setText(v_codigo);
        lb_Descripcion.setText(v_descripcion);
       // txtCantidad.setText(v_cantidad);

        btn_cancelar.setOnClickListener {
            finish();
        }

        obtenerItemsDefectos(v_codigo.toString());

        obtenerPosiblesDefectosCodigo(v_codigo.toString());

        btn_confirmacion.setOnClickListener {
           /* if (cbSelect.text.equals("PERFECTO")){
                confirmaciones(v_id.toString(),v_item_producto.toString(),v_cantidad.toString(),v_codigo.toString(), v_descripcion.toString(),txtCantidad.text.toString(),txtObservaciones.text.toString());
            }else{
                if (txtObservaciones.text.isNotEmpty()){*/
                    confirmaciones(v_id.toString(),v_item_producto.toString(),v_cantidad.toString(),v_codigo.toString(), v_descripcion.toString(),txtCantidad.text.toString(),txtObservaciones.text.toString());
               /* }else{
                    MensajesDialog.showMessage(this,"Se debe de colocar una observación")
                }
            }*/
        }


        val buttonMenu = findViewById<Button>(R.id.buttonMenu)
        buttonMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, buttonMenu)
            popupMenu.menuInflater.inflate(R.menu.menu_opciones, popupMenu.menu)
            /*val menuItemVerImagen = popupMenu.menu.findItem(R.id.item_ver_imagen)
            menuItemVerImagen.isVisible = false*/
            val menuImpresionBluetooth=popupMenu.menu.findItem(R.id.item_etiquetas_bluetooth);
            menuImpresionBluetooth.isVisible=false;
            val menuRecomendacion=popupMenu.menu.findItem(R.id.item_recomendaciones);
            menuRecomendacion.isVisible=false;
            val menuItemCombosPack=popupMenu.menu.findItem(R.id.item_packs_combos);
            menuItemCombosPack.isVisible=false;
            val menuItemUbicaciones=popupMenu.menu.findItem(R.id.item_f1);
            menuItemUbicaciones.isVisible=false;
            val menuInventory=popupMenu.menu.findItem(R.id.item_inventario);
            menuInventory.isVisible=false;
            popupMenu.setOnMenuItemClickListener { item ->
                try{
                    when (item.itemId) {
                        R.id.item_impresora -> {
                            val codigo = v_codigo.toString()
                            val descripcion = v_descripcion.toString()
                            val folio = v_folio.toString()
                            Log.d("IMPRESION","CODIGO: ${codigo}, DESCRIPCION: ${descripcion}, FOLIO: ${folio}")
                            if(codigo.isNotEmpty() && descripcion.isNotEmpty() && folio.isNotEmpty()){

                                val fragmentImpresion = FragmentPageImpresionEtiquetas.newInstance(codigo, descripcion, 2, folio,"")
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
                            false
                        }
                        R.id.item_ver_imagen -> {
                            val codigo = v_codigo.toString()
                            val fragmentContainer = findViewById<View>(R.id.viewSwitcher)
                            val backgroundBlocker = findViewById<View>(R.id.background_blocker)

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
                        R.id.item_etiquetas_bluetooth->{
                            if (v_codigo.toString().isNullOrBlank() || v_descripcion.toString().isNullOrBlank()) {
                                MensajesDialog.showMessage(this, "Se debe de seleccionar algún código");
                                return@setOnMenuItemClickListener true
                            }

                            val fragmentEtiquetaBluetooth = FragmentPageEtiquetaBluetooth().apply {
                                arguments = Bundle().apply {
                                    putString(FragmentPageEtiquetaBluetooth.ARG_CODIGO, v_codigo.toString())
                                    putString(FragmentPageEtiquetaBluetooth.ARG_DESCRIPCION, v_descripcion.toString())
                                    putInt(FragmentPageEtiquetaBluetooth.ARG_TIPO, 2)
                                    putString(FragmentPageEtiquetaBluetooth.ARG_FOLIO, v_folio.toString())
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


    }

    private fun obtenerItemsDefectos(codigo:String){
        try {
            val params = mapOf(
                "codigo" to codigo.toString().uppercase()
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
                                    val opcionesConPerfecto = opciones +"MERMA"+ "PERFECTO"

                                    actualizarAutoCompleteTextView(opcionesConPerfecto)
                                } else {
                                    MensajesDialog.showMessage(this@ControlCalidad_Confirmacion, "Lista vacia")
                                }
                            }
                        },
                        onError = { error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@ControlCalidad_Confirmacion, "Error: $error")
                            }
                        } )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@ControlCalidad_Confirmacion, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@ControlCalidad_Confirmacion, "Ocurrió un error: ${e.message}");
            }
        }
    }

    private fun obtenerPosiblesDefectosCodigo(codigo:String){
        try {
            val params = mapOf(
                "codigo" to codigo.toString().uppercase()
            )

            val headers= mapOf(
                "Token" to GlobalUser.token.toString()
            )

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/posibles/defectos/productos",
                        params=params,
                        dataClass = defectos::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()){
                                    val opciones = lista.map { it.PREGUNTA }

                                    val txt_posibles_defectos=findViewById<TextView>(R.id.txt_posibles_defectos);
                                    txt_posibles_defectos.setText(opciones.toString().replace("[", "").replace("]",""));
                                }else{
                                    MensajesDialog.showMessage(this@ControlCalidad_Confirmacion, "No hay posibles defectos registrados")
                                }
                            }
                        },
                        onError = { error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@ControlCalidad_Confirmacion, "Error: $error")
                            }
                        } )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@ControlCalidad_Confirmacion, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@ControlCalidad_Confirmacion, "Ocurrió un error: ${e.message}");
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
            cbSelect.setAdapter(adaptador)

            val btn_confirmar=findViewById<Button>(R.id.buttonConfirmar);

            cbSelect.setOnItemClickListener { _, _, position, _ ->
                val seleccion = opciones[position]
                btn_confirmar.isEnabled=true;
                btn_confirmar.setBackgroundColor(Color.parseColor("#059212"));
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun confirmaciones(id:String,item:String,cantida_recib:String, codigo: String,descripcion:String,cantidad_confir:String, observaciones:String){
        val btn_confirmacion=findViewById<Button>(R.id.buttonConfirmar);
        try {
            btn_confirmacion.isEnabled=false;
            val body= mapOf(
                "ID" to id,
                "ITEM" to item,
                "CANTIDAD_RECIBIDA" to cantida_recib,
                "CODIGO" to codigo.uppercase(),
                "DESCRIPCION" to descripcion,
                "CANTIDAD_CONFIRMADA" to cantidad_confir,
                "VEREDICTO" to cbSelect.text.toString(),
                "OBSERVACIONES" to observaciones
            )
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );

            lifecycleScope.launch {
                Pedir_datos_apis_post(
                    endpoint = "/recibo/calidad/producto",
                    body = body,
                    dataClass = Any::class,
                    listaKey = "message",
                    headers = headers,
                    onSuccess = { response ->
                        try {
                            try {
                                val message = response.toString()
                                if(message.contains("Confirmado con exito")){
                                    MensajesDialogConfirmaciones.showMessage(this@ControlCalidad_Confirmacion, "Confirmado con exito") {
                                        finish();
                                        btn_confirmacion.isEnabled=true;
                                    }
                                }else{
                                    MensajesDialog.showMessage(this@ControlCalidad_Confirmacion, "Respuesta: $message");
                                    btn_confirmacion.isEnabled=true;
                                }
                            } catch (e: Exception) {
                                MensajesDialog.showMessage(this@ControlCalidad_Confirmacion, "Error al procesar la respuesta: ${e.message}")
                                btn_confirmacion.isEnabled=true;
                            }

                        } catch (e: Exception) {
                            MensajesDialog.showMessage(this@ControlCalidad_Confirmacion, "Error al procesar la respuesta: ${e.message}")
                            btn_confirmacion.isEnabled=true;
                        }
                    },
                    onError = { error ->
                        MensajesDialog.showMessage(this@ControlCalidad_Confirmacion, "Error: ${error}")
                        btn_confirmacion.isEnabled=true;
                    }
                )
            }


        }catch (e: Exception){
            MensajesDialog.showMessage(this@ControlCalidad_Confirmacion, "Ocurrió un error: ${e.message}");
            btn_confirmacion.isEnabled=true;
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