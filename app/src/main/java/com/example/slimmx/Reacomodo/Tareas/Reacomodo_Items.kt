package com.example.slimmx.Reacomodo.Tareas

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalConfig
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListReacomodoTareasConfirmacion
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.R
import com.example.slimmx.Validaciones.BoxValidator
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.example.slimmx.sugerenciaUbicacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Reacomodo_Items : AppCompatActivity() {

    private lateinit var btn_confirmar:Button;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reacomodo_items)
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

        val v_id = intent.getStringExtra("id");
        val v_codigo = intent.getStringExtra("codigo");
        val v_descripcion = intent.getStringExtra("descripcion");
        val v_ubicacion_origen = intent.getStringExtra("ubicacion_origen");
        val v_box_origen = intent.getStringExtra("box_origen");
        val v_ubicacion_destino = intent.getStringExtra("ubicacion_destino");
        val v_box_destino = intent.getStringExtra("box_destino");
        val v_unidades = intent.getStringExtra("unidades");
        val v_item_producto = intent.getStringExtra("item");

        val lbCodigo = findViewById<TextView>(R.id.txtCodigo_Items_Reacomodo);
        val lbUbi_origen = findViewById<TextView>(R.id.txtUbicacionOrigen_Items_Reacomodo);
        val lbbox_origen = findViewById<TextView>(R.id.txtBoxOrigen_Items_Reacomodo);
        val lbDescripcion = findViewById<TextView>(R.id.txt_descripcion_Items_Reacomodo);
        val txtCantidad = findViewById<TextView>(R.id.txtCantidadReacomodoItem);
        val lb_ubicacion_des_con =
            findViewById<TextView>(R.id.LbUbicacion_Destino_ubi_destino_Confirmacion);
        val lb_box_des_con = findViewById<TextView>(R.id.LbBox_Destino_items_Confirmacion);
        val txtUbicacion_destino =
            findViewById<EditText>(R.id.txtUbicacion_Destino_Items_Reacomodo);
        val txtBox_destino = findViewById<EditText>(R.id.txtBox_Destino_Items_Reacomodo);
        val txtUbicacion_destino_confi =
            findViewById<EditText>(R.id.txtUbicacion_Destino_Items_Reacomodo_Confirmacion);
        val txtBox_destino_confi =
            findViewById<EditText>(R.id.txtBox_Destino_Items_Reacomodo_Confirmacion);

        val btn_segunda_confir = findViewById<Button>(R.id.button_SegundaConfir);
        val btn_eliminar_ubicacion =
            findViewById<Button>(R.id.buttonEliminar_Ubicaciones_destino_Items_Reacomodo);
        val btn_eliminar_box =
            findViewById<Button>(R.id.buttonEliminar_Box_destino_Items_Reacomodo);
        val btn_eliminar_ubi_conf =
            findViewById<Button>(R.id.buttonEliminar_Ubicaciones_destino_Items_Reacomodo_Confirmacion);
        val btn_eliminar_box_conf =
            findViewById<Button>(R.id.buttonEliminar_Box_destino_Items_Reacomodo_Confirmacion);
        btn_confirmar = findViewById<Button>(R.id.buttonConfirmar_Items_Reacomodo);

        val btn_salir=findViewById<Button>(R.id.buttonCancelar_Items_Reacomodo);

        txtUbicacion_destino.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txtBox_destino.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txtUbicacion_destino_confi.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txtBox_destino_confi.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        lbCodigo.setText(v_codigo);
        lbUbi_origen.setText(v_ubicacion_origen);
        lbbox_origen.setText(v_box_origen);
        lbDescripcion.setText(v_descripcion);
        txtCantidad.setText(v_unidades);
        txtUbicacion_destino.setText(v_ubicacion_destino);
        txtBox_destino.setText(v_box_destino);

        lb_ubicacion_des_con.isVisible = false;
        txtUbicacion_destino_confi.isVisible = false;
        txtBox_destino_confi.isVisible = false;
        lb_box_des_con.isVisible = false;
        btn_eliminar_box_conf.isVisible = false;
        btn_eliminar_ubi_conf.isVisible = false;
        btn_confirmar.isVisible = false;

        btn_eliminar_ubicacion.setOnClickListener {
            txtUbicacion_destino.setText("");
            txtUbicacion_destino.post { txtUbicacion_destino.requestFocus() }
        }

        btn_eliminar_box.setOnClickListener {
            txtBox_destino.setText("");
            txtBox_destino.post { txtBox_destino.requestFocus() }
        }

        btn_eliminar_ubi_conf.setOnClickListener {
            txtUbicacion_destino_confi.setText("");
            txtUbicacion_destino_confi.post { txtUbicacion_destino_confi.requestFocus() }
        }

        btn_eliminar_box_conf.setOnClickListener {
            txtBox_destino_confi.setText("");
            txtBox_destino_confi.post { txtBox_destino_confi.requestFocus() }
        }

        btn_segunda_confir.setOnClickListener {
            try {
                if (txtCantidad.text.toString().toInt()>0) {
                    lb_ubicacion_des_con.isVisible = true;
                    txtUbicacion_destino_confi.isVisible = true;
                    txtBox_destino_confi.isVisible = true;
                    lb_box_des_con.isVisible = true;
                    btn_eliminar_box_conf.isVisible = true;
                    btn_eliminar_ubi_conf.isVisible = true;
                    btn_confirmar.isVisible = true;

                    txtUbicacion_destino_confi.post { txtUbicacion_destino_confi.requestFocus() }
                    txtCantidad.isEnabled = false;



                } else {
                    MensajesDialog.showMessage(this, "No puedes reacomodar 0 piezas");
                }
            }catch (e: Exception){
                MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
            }
        }

        if (txtUbicacion_destino.text.toString()=="(null)" || txtUbicacion_destino.text.toString()=="S/U" || txtUbicacion_destino.text.toString()==""){
            try {
                sugerencia(lbCodigo.text.toString(), lbUbi_origen.text.toString());
                txtCantidad.post { txtCantidad.requestFocus() }
            }catch (e: Exception){
                MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
            }
        }

        btn_confirmar.setOnClickListener {
            if (txtUbicacion_destino.text.toString().equals(txtUbicacion_destino_confi.text.toString()) && txtBox_destino.text.toString().equals(txtBox_destino_confi.text.toString())){
                try {
                    confirmarReacomodo(v_id.toString(),v_item_producto.toString(), v_codigo.toString(), txtCantidad.text.toString().toInt(),v_ubicacion_origen.toString(),v_box_origen.toString(), txtUbicacion_destino_confi.text.toString(),txtBox_destino_confi.text.toString());
                }catch (e: Exception){
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                    btn_confirmar.isEnabled=true;
                }
            }else{
                MensajesDialog.showMessage(this, "Las ubicaciones no coinciden");
                btn_confirmar.isEnabled=true;
            }
        }

        btn_salir.setOnClickListener {
            finish();
        }


        txtUbicacion_destino_confi.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtUbicacion_destino_confi.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        txtBox_destino_confi.post { txtBox_destino_confi.requestFocus() }
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }

        txtUbicacion_destino.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtUbicacion_destino.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        txtBox_destino.post { txtBox_destino.requestFocus() }
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

                            },
                            failureComponent = { message ->
                                MensajesDialog.showMessage(this, message)
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

    }

    private fun sugerencia(codigo: String, ubicacion: String) {
        val txtUbicacion_destino =
            findViewById<EditText>(R.id.txtUbicacion_Destino_Items_Reacomodo);
        val txtBox_destino = findViewById<EditText>(R.id.txtBox_Destino_Items_Reacomodo);
        val txtUnidades_stock = findViewById<EditText>(R.id.txtStock_Items_Reacomodo);

        if (codigo.isNotEmpty() && ubicacion.isNotEmpty()) {
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
                                        val unidades =
                                            lista.map { it.FISICO_DISPONIBLE }.toString()
                                                .replace("[", "")
                                                .replace("]", "");


                                        if (ubicacion.equals("null")) {
                                            txtUbicacion_destino.setText("");
                                        } else {
                                            txtUbicacion_destino.setText(ubicacion);
                                        }
                                        if (ubicacion.equals("null")) {
                                            txtBox_destino.setText("S/B");
                                        } else {
                                            txtBox_destino.setText(box);
                                        }
                                        if (ubicacion.equals("null")) {
                                            txtUnidades_stock.setText("0");
                                        } else {
                                            txtUnidades_stock.setText(unidades);
                                        }

                                    } else {

                                        MensajesDialog.showMessage(this@Reacomodo_Items, "No hay sugerencia");
                                    }
                                }
                            },
                            onError = { error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Reacomodo_Items, "${error}");
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Reacomodo_Items, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Reacomodo_Items, "Ocurrió un error: ${e.message}");
                }
            }
        } else {
            MensajesDialog.showMessage(
                this,
                "Ya sea el código, ubicación origen o el box origen estan vacios"
            );
        }

    }

    private fun confirmarReacomodo(folio:String, item_producto:String,codigo: String, unidades_confir:Int, ubicacion_origen:String,box_origen:String, ubicacion_destino:String,box_destino:String){
        if (codigo.isNotEmpty() && unidades_confir.toString().isNotEmpty() && ubicacion_origen.isNotEmpty() && box_origen.isNotEmpty() && ubicacion_destino.isNotEmpty() && box_destino.isNotEmpty()){
            try {
                btn_confirmar.isEnabled=false;
                val body= mapOf(
                    "FOLIO" to  folio,
                    "ITEM" to item_producto,
                    "CANTIDAD" to unidades_confir.toString(),
                    "UBICACION_DESTINO" to ubicacion_destino.uppercase(),
                    "BOX_DESTINO" to box_destino.uppercase(),
                    "CODIGO" to codigo.uppercase()
                );

                val headers = mapOf(
                    "Token" to GlobalUser.token.toString()
                );
                Log.d("DEBUG", "boxOrigen recibido: $box_origen")
                Log.d("DEBUG", "boxDestino recibido: $box_destino")

                lifecycleScope.launch {
                    Pedir_datos_apis_post(
                        endpoint = "/inventario/tareas/reubicacion/actualizacion/items/asignada",
                        body=body,
                        dataClass = Any::class,
                        listaKey =  "message",
                        headers = headers,
                        onSuccess = {lista ->
                            if (lista.equals("Ok")){
                                MensajesDialogConfirmaciones.showMessage(
                                    this@Reacomodo_Items,
                                    "Confirmada correctamente"
                                ) {
                                    finish();
                                    btn_confirmar.isEnabled=true;
                                }
                            }else{
                                MensajesDialog.showMessage(this@Reacomodo_Items,"${lista}");
                                btn_confirmar.isEnabled=true;
                            }
                        }, onError = {error->
                            MensajesDialog.showMessage(this@Reacomodo_Items,"${error}");
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