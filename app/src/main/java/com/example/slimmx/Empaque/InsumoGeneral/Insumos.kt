package com.example.slimmx.Empaque.InsumoGeneral

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.slimmx.ListConsumaInterno
import com.example.slimmx.ListProducto
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.R
import com.example.slimmx.Submenus.Submenu_packing
import com.example.slimmx.Validaciones.BoxValidator
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Insumos : AppCompatActivity() {
    private var token:String="";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_insumos)
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

        var txtCodigo=findViewById<EditText>(R.id.editTextCodigo_In);
        var txtUbicacion=findViewById<EditText>(R.id.editTextUbicacion_In);
        var txtBox=findViewById<TextView>(R.id.editTextbox_In);
        var txtCantidad=findViewById<EditText>(R.id.editTextCantidad_In);
        val txtDescripcion=findViewById<TextView>(R.id.editTextDescripcion_In);
        var txtUnidades=findViewById<TextView>(R.id.editTextUnidades_In);
        var btn_validar=findViewById<Button>(R.id.buttonValidar_In);
        var btnEliminarCodigo=findViewById<Button>(R.id.buttonEliminar_Codigo_In);
        var btnEliminarUbicacion=findViewById<Button>(R.id.buttonEliminar_Ubicacion_In);

        var btn_cancelar=findViewById<Button>(R.id.buttonCancelar_In);
        var btn_confirmar=findViewById<Button>(R.id.buttonConfirmar_In);

        token=GlobalUser.token.toString();

        txtCodigo.post { txtCodigo.requestFocus() }
        txtCodigo.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txtUbicacion.setOnTouchListener(MostrarTeclado.getMostrarTeclado());


        btn_confirmar.isEnabled=false;


        txtCodigo.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtCodigo.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        validadCodigo(txtCodigo.text.toString());
                        txtUbicacion.post { txtUbicacion.requestFocus() }
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }


        txtUbicacion.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtUbicacion.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        validarUnidades(txtCodigo.text.toString(),txtUbicacion.text.toString(),txtBox.text.toString());
                        txtCantidad.post { txtCantidad.requestFocus() }
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }

        txtBox.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtUbicacion.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        BoxValidator.validateBox(
                            input = txtBox.text.toString(),
                            successComponent = {
                                txtCodigo.isEnabled = true
                                txtCodigo.post { txtCodigo.requestFocus() }
                            },
                            failureComponent = { message ->
                                MensajesDialog.showMessage(this, message)
                                txtBox.setText("")
                                txtBox.post { txtBox.requestFocus() }
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
            validarUnidades(txtCodigo.text.toString(),txtUbicacion.text.toString(),txtBox.text.toString());
        }

        btnEliminarCodigo.setOnClickListener {
            txtCodigo.setText("");
            txtDescripcion.setText("");
            txtCodigo.post { txtCodigo.requestFocus() }
        }

        btnEliminarUbicacion.setOnClickListener {
            txtUbicacion.setText("");
            txtUbicacion.post { txtUbicacion.requestFocus() }
            txtUnidades.setText("")
        }

        btn_cancelar.setOnClickListener {
            startActivity(Intent(this@Insumos, Submenu_packing::class.java));
            finish();
        }

        btn_confirmar.setOnClickListener {
            try {
                confirmarInsumo(txtCodigo.text.toString(), txtDescripcion.text.toString(), txtUbicacion.text.toString(),txtBox.text.toString(), txtCantidad.text.toString())

            }catch (e: Exception){
                MensajesDialog.showMessage(this@Insumos, "Ocurrió un error: ${e.message}");
                btn_confirmar.isEnabled=true;
            }

        }
    }
    private fun validarUnidades(codigo:String, ubicacion:String, box:String){
        var txtUnidades=findViewById<TextView>(R.id.editTextUnidades_In);
        var btn_confirmar=findViewById<Button>(R.id.buttonConfirmar_In);
        if (codigo.isNotEmpty() && ubicacion.isNotEmpty() && box.isNotEmpty()){
            try {
                val params= mapOf(
                    "codigo" to codigo.uppercase(),
                    "ubicacion" to ubicacion.uppercase(),
                    "box" to box.uppercase()
                )

                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/productos/ubicacion/unidades",
                            params=params,
                            dataClass = ListConsumaInterno::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        var datos = lista[0];
                                        txtUnidades.text = datos.UNIDADES.toInt().toString();
                                        btn_confirmar.isEnabled = true;
                                    } else {
                                        MensajesDialog.showMessage(this@Insumos, "Ubicación incorrecta")
                                    }

                                }
                            }, onError = {error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Insumos, "Error: $error")
                                    btn_confirmar.isEnabled = false;
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Insumos, "Ocurrió un error: ${e.message}")
                        }
                    }
                }
            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Insumos, "Ocurrió un error: ${e.message}");
                }
            }

        }else{
            MensajesDialog.showMessage(this,"Datos incompletos")
            btn_confirmar.isEnabled=false;
        }
    }

    private fun validadCodigo(codigo: String){
        val txtDescripcion=findViewById<TextView>(R.id.editTextDescripcion_In);
        if(codigo.isNotEmpty()){
            try {
                val params= mapOf(
                    "search" to codigo.uppercase()
                )

                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/productos/list",
                            params=params,
                            dataClass = ListProducto::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { list ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (list.isNotEmpty()) {
                                        val valores = list[0];
                                        txtDescripcion.text = valores.DESCRIPCION;
                                    }

                                }
                            },
                            onError = { error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Insumos, "Error: $error")
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Insumos, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Insumos, "Ocurrió un error: ${e.message}");
                }
            }

        }else{
            MensajesDialog.showMessage(this,"Se debe de escanear un código")
        }
    }

    private fun confirmarInsumo(codigo: String, descripcion:String, ubicacion:String,box: String, cantidad:String){
        var txtCodigo=findViewById<EditText>(R.id.editTextCodigo_In);
        var txtUbicacion=findViewById<EditText>(R.id.editTextUbicacion_In);
        var txtBox=findViewById<TextView>(R.id.editTextbox_In);
        var txtCantidad=findViewById<EditText>(R.id.editTextCantidad_In);
        val txtDescripcion=findViewById<TextView>(R.id.editTextDescripcion_In);
        var txtUnidades=findViewById<TextView>(R.id.editTextUnidades_In);
        var btn_confirmar=findViewById<Button>(R.id.buttonConfirmar_In);
        if (codigo.isNotEmpty() && descripcion.isNotEmpty() && ubicacion.isNotEmpty() && box.isNotEmpty() && cantidad.isNotEmpty() && cantidad.toInt()>0){
            try {
                btn_confirmar.isEnabled=false;
                val body= mapOf(
                    "CODIGO" to codigo.uppercase(),
                    "DESCRIPCION" to descripcion,
                    "UBICACION" to ubicacion.uppercase(),
                    "BOX" to box.uppercase(),
                    "CANTIDAD" to cantidad
                )

                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch {
                    Pedir_datos_apis_post(
                        endpoint = "/insumo",
                        body=body,
                        dataClass =Any::class,
                        listaKey = "message",
                        headers = headers,
                        onSuccess = { response ->
                            try {
                                try {
                                    val message = response.toString()
                                    if(message.contains("INSUMO REGISTRADO CON EXITO")){
                                        MensajesDialogConfirmaciones.showMessage(this@Insumos, "OK") {
                                            txtCodigo.setText("");
                                            txtDescripcion.setText("");
                                            txtUbicacion.setText("");
                                            txtBox.setText("S/B");
                                            txtCantidad.setText("");
                                            txtUnidades.setText("");
                                            txtCodigo.post { txtCodigo.requestFocus() }
                                            btn_confirmar.isEnabled=true;
                                        }
                                    }else{
                                        MensajesDialog.showMessage(this@Insumos, "Respuesta: $message");
                                        btn_confirmar.isEnabled=true;
                                    }
                                } catch (e: Exception) {
                                    MensajesDialog.showMessage(this@Insumos, "Error al procesar la respuesta: ${e.message}");
                                    btn_confirmar.isEnabled=true;
                                }

                            } catch (e: Exception) {
                                MensajesDialog.showMessage(this@Insumos, "Error al procesar la respuesta: ${e.message}");
                                btn_confirmar.isEnabled=true;
                            }
                        },
                        onError = { error ->
                            MensajesDialog.showMessage(this@Insumos, "Error: $error");
                            btn_confirmar.isEnabled=true;
                        }
                    )
                }

            }catch (e: Exception){
                MensajesDialog.showMessage(this@Insumos, "Ocurrió un error: ${e.message}");
                btn_confirmar.isEnabled=true;
            }

        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this@Insumos, Submenu_packing::class.java));
        //LogsEntradaSalida.logsPorModulo( this@Insumos, lifecycleScope, "INSUMOS", "SALIDA");
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