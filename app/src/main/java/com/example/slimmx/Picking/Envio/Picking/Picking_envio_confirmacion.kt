package com.example.slimmx.Picking.Envio.Picking

import android.content.Intent
import android.os.Bundle
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
import com.example.slimmx.GlobalUser
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Packs_Combos.Busqueda_Packs
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.R
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Vista.FragmentPage3
import com.example.slimmx.Vista.ImageFragment
import kotlinx.coroutines.launch

class Picking_envio_confirmacion : AppCompatActivity() , FragmentPage3.OnBackgroundBlockerListener, ImageFragment.OnBackgroundBlockerListener{

    override fun showBackgroundBlocker() {
        val backgroundBlockerView: View = findViewById(R.id.background_blocker_P1_Envio)
        backgroundBlockerView.visibility = View.VISIBLE
        backgroundBlockerView.setOnTouchListener { _, _ -> true }

    }

    override fun hideBackgroundBlocker() {
        val backgroundBlockerView: View = findViewById(R.id.background_blocker_P1_Envio)
        backgroundBlockerView.visibility = View.GONE
        backgroundBlockerView.setOnTouchListener(null)
    }

    private lateinit var btn_confirmacion:Button;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_picking_envio_confirmacion)
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

        val v_folio = intent.getStringExtra("folio");
        val v_codigo = intent.getStringExtra("codigo");
        val v_descripcion = intent.getStringExtra("descripcion");
        val v_ubicacion = intent.getStringExtra("ubicacion");
        val v_box = intent.getStringExtra("box");
        val v_cantidad = intent.getStringExtra("cantidad");
        val v_item= intent.getStringExtra("item_producto");
        val v_tipo=intent.getStringExtra("tipo");
        val v_codigo_referencia=intent.getStringExtra("codigo_referencia");

        val txtFolio = findViewById<TextView>(R.id.txtFolioPi_En_Con);
        val txtCodigo = findViewById<TextView>(R.id.txtCodigoPi_En_Con);
        val txtDescripcion = findViewById<TextView>(R.id.txtDescripcionPi_En_Con);
        val txtUbicacion = findViewById<TextView>(R.id.txtUbicacionPi_En_Con);
        val txtbox = findViewById<TextView>(R.id.txtBoxPi_En_Con);
        val txtcantidad = findViewById<TextView>(R.id.txtCantidadPi_En_Con);
        val imagen_tipo=findViewById<ImageView>(R.id.imagen_tipo);
        val txtTipo=findViewById<TextView>(R.id.txtTipo);
        val lb_codigo_referncia=findViewById<TextView>(R.id.lb_codigo_referencia);

        if(v_tipo.toString().equals("COMBO")){
            imagen_tipo.setImageResource(R.drawable.logo_combo)
            txtTipo.setText("COMBO");
            lb_codigo_referncia.setText(v_codigo_referencia);
        }

        if (v_tipo.toString().equals("PACK")){
            imagen_tipo.setImageResource(R.drawable.pack_combo)
            txtTipo.setText("PACK");
            lb_codigo_referncia.setText(v_codigo_referencia);
        }

        if (v_tipo.toString().equals("GENERAL")){
            imagen_tipo.isVisible=false;
            txtTipo.isVisible=false;
            lb_codigo_referncia.setText("");
        }

        val btn_cancelar=findViewById<Button>(R.id.buttonCancelar_PI_En)
        btn_confirmacion=findViewById<Button>(R.id.buttonConfirmar_PI_En)

        txtFolio.setText(v_folio);
        txtFolio.isFocusable =false;
        txtFolio.isClickable  =false;

        txtCodigo.setText(v_codigo);
        txtCodigo.isFocusable =false;
        txtCodigo.isClickable =false;

        txtDescripcion.setText(v_descripcion);
        txtDescripcion.isFocusable=false;
        txtDescripcion.isClickable=false;

        txtUbicacion.setText(v_ubicacion);
        txtUbicacion.isFocusable=false;
        txtUbicacion.isClickable=false;

        txtbox.setText(v_box);
        txtbox.isFocusable=false;
        txtbox.isClickable=false;

        txtcantidad.setText(v_cantidad);
        txtcantidad.post { txtcantidad.requestFocus() }

        btn_cancelar.setOnClickListener {
            finish();
        }

        var v_cantidad_confir=txtcantidad.text.toString();

        btn_confirmacion.setOnClickListener {
            if(v_folio.toString().isNotEmpty()&& v_codigo.toString().isNotEmpty() && v_descripcion.toString().isNotEmpty() && v_ubicacion.toString().isNotEmpty() && v_box.toString().isNotEmpty() && v_cantidad_confir.toInt()>0  ){
                if (v_cantidad.toString().toInt()>=txtcantidad.text.toString().toInt()){
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Confirmación")
                    builder.setMessage("¿Estás seguro de confirmar los datos?")

                    builder.setPositiveButton("Confirmar") { dialog, _ ->
                        btn_confirmacion.isEnabled=false;
                        try {
                            val body = mapOf(
                                "ID" to v_folio.toString(),
                                "ITEM" to v_item.toString(),
                                "UBICACION_ORIGEN" to v_ubicacion.toString().uppercase(),
                                "BOX_ORIGEN" to v_box.toString().uppercase(),
                                "CODIGO" to v_codigo.toString().uppercase(),
                                "DESCRIPCION" to v_descripcion.toString(),
                                "UNIDADES_CONFIRMADAS" to txtcantidad.text.toString(),
                                "FALTANTES" to "0",
                                "REVISION" to "0"
                            )
                            val headers = mapOf("Token" to GlobalUser.token.toString());
                            lifecycleScope.launch {
                                Pedir_datos_apis_post(
                                    endpoint = "/inventario/tareas/picking/item/set",
                                    body = body,
                                    dataClass = Any::class,
                                    listaKey = "message",
                                    headers = headers,
                                    onSuccess = { response ->
                                        try {
                                            try {
                                                val message = response.toString()
                                                if(message.contains("Se actualizo la tarea con")){
                                                    MensajesDialogConfirmaciones.showMessage(this@Picking_envio_confirmacion, "OK") {
                                                        finish();
                                                        btn_confirmacion.isEnabled=true;
                                                    }
                                                }else{
                                                    MensajesDialog.showMessage(this@Picking_envio_confirmacion, "Respuesta: $message");
                                                    btn_confirmacion.isEnabled=true;
                                                }
                                            } catch (e: Exception) {
                                                MensajesDialog.showMessage(this@Picking_envio_confirmacion, "Error al procesar la respuesta: ${e.message}");
                                                btn_confirmacion.isEnabled=true;
                                            }

                                        } catch (e: Exception) {
                                            MensajesDialog.showMessage(this@Picking_envio_confirmacion, "Error al procesar la respuesta: ${e.message}");
                                            btn_confirmacion.isEnabled=true;
                                        }
                                    },
                                    onError = { error ->
                                        MensajesDialog.showMessage(this@Picking_envio_confirmacion, "Error: ${error}");
                                        btn_confirmacion.isEnabled=true;
                                    }
                                )
                            }

                        } catch (e: Exception) {
                            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                            btn_confirmacion.isEnabled=true;
                        }
                        dialog.dismiss()
                    }

                    builder.setNegativeButton("Cancelar") { dialog, _ ->
                        dialog.dismiss();
                        btn_confirmacion.isEnabled=true;
                    }

                    builder.create().show()
                }else{
                    MensajesDialog.showMessage(this,"No puedes confirmar piezas de mas");
                    btn_confirmacion.isEnabled=true;
                }

            } else {
                MensajesDialog.showMessage(this, "Revisa los datos y la cantidad que estás colocando");
                btn_confirmacion.isEnabled=true;
            }
        }

        val buttonMenu = findViewById<Button>(R.id.buttonMenu_Pi_Envio)
        buttonMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, buttonMenu)
            popupMenu.menuInflater.inflate(R.menu.menu_opciones, popupMenu.menu)
            val menuImpresion = popupMenu.menu.findItem(R.id.item_impresora)
            menuImpresion.isVisible = false
            val menuImpresionBluetooth=popupMenu.menu.findItem(R.id.item_etiquetas_bluetooth);
            menuImpresionBluetooth.isVisible=false;
            val menuRecomendacion=popupMenu.menu.findItem(R.id.item_recomendaciones);
            menuRecomendacion.isVisible=false;
            val menuUbicacion=popupMenu.menu.findItem(R.id.item_f1);
            menuUbicacion.isVisible=false;
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

                                val fragmentContainerView = findViewById<View>(R.id.fragmentContainerView_f1_Envio)
                                val backgroundBlocker = findViewById<View>(R.id.background_blocker_P1_Envio)

                                val transaction = supportFragmentManager.beginTransaction()
                                val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView_f1_Envio)

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
                                    transaction.add(R.id.fragmentContainerView_f1_Envio, fragmentPage3);
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
                            val fragmentContainer = findViewById<View>(R.id.viewSwitcher_Pi_Envio)
                            val backgroundBlocker = findViewById<View>(R.id.background_blocker_P1_Envio)

                            if (codigo.isNotEmpty()) {
                                val transaction = supportFragmentManager.beginTransaction()
                                val existingFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerViewImagenPi_Envio)

                                if (existingFragment != null && existingFragment.isVisible) {
                                    transaction.hide(existingFragment)
                                    transaction.commitNow()
                                    backgroundBlocker.visibility = View.GONE
                                    backgroundBlocker.setOnTouchListener(null)
                                } else {
                                    transaction.replace(R.id.fragmentContainerViewImagenPi_Envio, ImageFragment.newInstance(codigo))
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
                            if (v_tipo.toString().isNotEmpty() && (v_tipo.toString().startsWith("PACK")|| v_tipo.toString().startsWith("COM"))){
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
                }catch (e: Exception) {
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}")
                    false
                }
            }
            popupMenu.show()
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