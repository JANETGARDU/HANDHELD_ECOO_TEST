package com.example.slimmx.Picking.Cedis.VentaInterna

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.Button
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
import com.example.slimmx.GlobalConfig
import com.example.slimmx.GlobalUser
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Packs_Combos.Busqueda_Packs
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.R
import com.example.slimmx.Validaciones.BoxValidator
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.example.slimmx.Vista.FragmentPage3
import com.example.slimmx.Vista.ImageFragment
import com.example.slimmx.listDatosFolios
import kotlinx.coroutines.launch

class Venta_Interna_Confirmacion : AppCompatActivity() , FragmentPage3.OnBackgroundBlockerListener, ImageFragment.OnBackgroundBlockerListener{

    private var token:String="";
    private lateinit var btn_confirmacion:Button;

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
        setContentView(R.layout.activity_venta_interna_confirmacion)
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


        val v_folio = intent.getStringExtra("folio");
        val v_codigo = intent.getStringExtra("codigo");
        val v_descripcion = intent.getStringExtra("descripcion");
        val v_cantidad = intent.getStringExtra("cantidad");
        val v_ubicacion = intent.getStringExtra("ubicacion");
        val v_box = intent.getStringExtra("box");
        val v_item= intent.getStringExtra("item_producto");
        val v_codigo_referencia=intent.getStringExtra("codigo_referencia")

        val txtFolio = findViewById<TextView>(R.id.txtFolio_VentaIn);
        val txtCodigo = findViewById<TextView>(R.id.txtCodigo_VentaIn);
        val txtDescripcion = findViewById<TextView>(R.id.txtDescripcion_VentaIn);
        val txtUbicacion = findViewById<TextView>(R.id.txtUbicacion_VentaIn);
        val txtbox = findViewById<TextView>(R.id.txtBox_VentaIn);
        val txtcantidad = findViewById<TextView>(R.id.txtCantidad_VentaIn);

        val btn_eliminar_ubicacion=findViewById<Button>(R.id.buttonEliminar_Ubicacion_VentaInterna_Pi);
        val btn_eliminar_box=findViewById<Button>(R.id.buttonEliminar_Box_VentaInterna_Pi);
        val btn_salir=findViewById<Button>(R.id.buttonCancelar_VentaInterna_Pi);
        btn_confirmacion=findViewById<Button>(R.id.buttonConfirmar_VentaInterna_Pi);
        val imagen_tipo=findViewById<ImageView>(R.id.imagen_tipo);
        val txtTipo=findViewById<TextView>(R.id.txtTipo);

        if(v_codigo_referencia.toString().startsWith("COM")){
            imagen_tipo.setImageResource(R.drawable.logo_combo)
            txtTipo.setText("COMBO");
        }

        if (v_codigo_referencia.toString().startsWith("PACK")){
            imagen_tipo.setImageResource(R.drawable.pack_combo)
            txtTipo.setText("PACK");
        }

        if (v_codigo_referencia.toString().isNullOrEmpty()){
            imagen_tipo.isVisible=false;
            txtTipo.isVisible=false;
        }

        txtUbicacion.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txtbox.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        txtFolio.setText(v_folio);
        txtCodigo.setText(v_codigo);
        txtDescripcion.setText(v_descripcion);
        txtUbicacion.setText(v_ubicacion);
        txtbox.setText(v_box);
        txtcantidad.setText(v_cantidad);

        txtUbicacion.post { txtUbicacion.requestFocus() }

        btn_eliminar_ubicacion.setOnClickListener {
            txtUbicacion.setText("");
            txtUbicacion.post { txtUbicacion.requestFocus() }
        }

        btn_eliminar_box.setOnClickListener {
            txtbox.setText("");
            txtbox.post { txtbox.requestFocus() }
        }

        btn_salir.setOnClickListener {
            finish();
        }

        val buttonMenu = findViewById<Button>(R.id.buttonMenu)
        buttonMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, buttonMenu)
            popupMenu.menuInflater.inflate(R.menu.menu_opciones, popupMenu.menu)
            val menuImpresion = popupMenu.menu.findItem(R.id.item_impresora)
            menuImpresion.isVisible = false
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
                            true
                        }
                        R.id.item_f1 -> {
                            if (txtCodigo.text.toString().isNotEmpty()){
                                val fragmentPage3 = FragmentPage3.newInstance(txtCodigo.text.toString())

                                val fragmentContainerView = findViewById<View>(R.id.fragmentContainerView)
                                val backgroundBlocker = findViewById<View>(R.id.background_blocker)

                                val transaction = supportFragmentManager.beginTransaction()
                                val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)

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
                                    transaction.add(R.id.fragmentContainerView, fragmentPage3);
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
                        R.id.item_packs_combos->{
                            if (v_codigo_referencia.toString().isNotEmpty() && (v_codigo_referencia.toString().startsWith("PACK")|| v_codigo_referencia.toString().startsWith("COM"))){
                                val intent = Intent(this, Busqueda_Packs::class.java)

                                intent.putExtra("codigo", v_codigo_referencia.toString());
                                startActivity(intent);
                            }else{
                                MensajesDialog.showMessage(this, "No es parte de un codigó COMBO o PACK")
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

        txtUbicacion.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtUbicacion.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        txtbox.post { txtbox.requestFocus() };
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }

        txtbox.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtUbicacion.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        BoxValidator.validateBox(
                            input = txtbox.text.toString(),
                            successComponent = {

                            },
                            failureComponent = { message ->
                                MensajesDialog.showMessage(this, message)
                                txtbox.setText("")
                                txtbox.post { txtbox.requestFocus() }
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

        btn_confirmacion.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmación")
            builder.setMessage("¿Está seguro de que desea confirmar el siguiente elemento?")

            builder.setPositiveButton("Confirmar") { dialog, _ ->
                try {
                    confirmarItems(v_codigo.toString(),v_descripcion.toString(),txtUbicacion.text.toString(),txtbox.text.toString(),v_cantidad.toString(),v_item.toString(),v_folio.toString());
                    dialog.dismiss()
                }catch (e: Exception){
                    MensajesDialog.showMessage(this@Venta_Interna_Confirmacion, "Ocurrió un error: ${e.message}");
                    btn_confirmacion.isEnabled=true;
                }
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->
                btn_confirmacion.isEnabled=true;
                dialog.dismiss();
            }

            val dialog = builder.create()
            dialog.show()

        }

    }

    private fun confirmarItems(codigo:String, descripcion:String, ubicacion:String, box:String, cantidad:String, item:String, folio:String){
        val txtFolio = findViewById<TextView>(R.id.txtFolio_VentaIn);
        val txtCodigo = findViewById<TextView>(R.id.txtCodigo_VentaIn);
        val txtDescripcion = findViewById<TextView>(R.id.txtDescripcion_VentaIn);
        val txtUbicacion = findViewById<TextView>(R.id.txtUbicacion_VentaIn);
        val txtbox = findViewById<TextView>(R.id.txtBox_VentaIn);
        val txtcantidad = findViewById<TextView>(R.id.txtCantidad_VentaIn);

        if(codigo.isNotEmpty() && descripcion.isNotEmpty() && ubicacion.isNotEmpty() && box.isNotEmpty() && cantidad.isNotEmpty() && item.isNotEmpty() && folio.isNotEmpty() && cantidad.toInt()>0 ){
            try {
                btn_confirmacion.isEnabled=false;
                val body= mapOf(
                    "CODIGO" to codigo.uppercase(),
                    "DESCRIPCION" to descripcion,
                    "UBICACION" to ubicacion.uppercase(),
                    "BOX" to box.uppercase(),
                    "CANTIDAD" to cantidad,
                    "ITEM" to item,
                    "FOLIO" to folio,
                    "ALMACEN_ID" to "1"
                )

                val headers= mapOf("Token" to GlobalUser.token.toString())

                lifecycleScope.launch {
                    Pedir_datos_apis_post(
                        endpoint = "/venta/interna",
                        body=body,
                        dataClass = Any::class,
                        listaKey ="message",
                        headers = headers,
                        onSuccess = { response ->
                            try {
                                try {
                                    val message = response.toString()
                                    if(message.contains("VENTA CONFIRMADA")){
                                        MensajesDialogConfirmaciones.showMessage(this@Venta_Interna_Confirmacion, "OK") {
                                            txtFolio.setText("");
                                            txtCodigo.setText("");
                                            txtDescripcion.setText("");
                                            txtUbicacion.setText("");
                                            txtbox.setText("");
                                            txtcantidad.setText("");
                                            finish();
                                            btn_confirmacion.isEnabled=true;
                                        }
                                    }else{
                                        MensajesDialog.showMessage(this@Venta_Interna_Confirmacion, "Respuesta: $message");
                                        btn_confirmacion.isEnabled=true;
                                    }
                                } catch (e: Exception) {
                                    MensajesDialog.showMessage(this@Venta_Interna_Confirmacion, "Error al procesar la respuesta: ${e.message}");
                                    btn_confirmacion.isEnabled=true;
                                }

                            } catch (e: Exception) {
                                MensajesDialog.showMessage(this@Venta_Interna_Confirmacion, "Error al procesar la respuesta: ${e.message}");
                                btn_confirmacion.isEnabled=true;
                            }
                        },
                        onError = { error ->
                            MensajesDialog.showMessage(this@Venta_Interna_Confirmacion, "Error: $error");
                            btn_confirmacion.isEnabled=true;
                        }
                    )
                }

            }catch (e: Exception){
                MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                btn_confirmacion.isEnabled=true;
            }

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