package com.example.slimmx.Inventario.Ajustes

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.Empaque.Recolecta.Submenu_Empaque_Recolecta
import com.example.slimmx.GlobalConfig
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListProducto
import com.example.slimmx.ListaAjusteConfir
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.R
import com.example.slimmx.Submenus.Submenu_inventarios
import com.example.slimmx.Validaciones.BoxValidator
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.example.slimmx.listDatosFolios
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Ajustes : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ajustes)
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

        var txtUsuario=findViewById<TextView>(R.id.txtUsuario_ajustes);
        var txtCodigo=findViewById<EditText>(R.id.txtCodigo_Ajustes);
        var txtUbicacion=findViewById<EditText>(R.id.txtUbicacion_Ajustes);
        var txtBox=findViewById<EditText>(R.id.txtBox_Ajustes);
        var txtExistencia=findViewById<EditText>(R.id.txtExistencia_Ajustes);
        var txtDescripcion=findViewById<TextView>(R.id.txtDescripcion_ajustes);

        var btn_eliminar_codigo=findViewById<Button>(R.id.buttonEliminar_codigo_Ajuste);
        var btn_eliminar_ubicacion=findViewById<Button>(R.id.buttonEliminar_ubicacion_Ajuste);
        var btn_eliminar_box=findViewById<Button>(R.id.buttonEliminar_box_Ajuste);

        var btn_cancelar=findViewById<Button>(R.id.buttonCancelar_ajustes);
        var btn_confirmar=findViewById<Button>(R.id.buttonConfirmar_ajustes);

        txtCodigo.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txtUbicacion.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txtBox.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        txtUsuario.setText(GlobalUser.nombre);

        txtCodigo.post { txtCodigo.requestFocus() }

        btn_eliminar_codigo.setOnClickListener {
            txtCodigo.setText("");
            txtCodigo.post { txtCodigo.requestFocus() }
            txtDescripcion.setText("");
        }

        btn_eliminar_ubicacion.setOnClickListener {
            txtUbicacion.setText("");
            txtUbicacion.post { txtUbicacion.requestFocus() }
        }

        btn_eliminar_box.setOnClickListener {
            txtBox.setText("");
            txtBox.post { txtBox.requestFocus() }
        }

        btn_cancelar.setOnClickListener {
            finish();
        }

        txtCodigo.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtCodigo.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        buscarCodigo(txtCodigo.text.toString());
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
                        txtBox.post { txtBox.requestFocus() }
                        txtBox.setText("S/B");
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
                    val inputText = txtBox.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        BoxValidator.validateBox(
                            input = txtBox.text.toString(),
                            successComponent = {
                                txtExistencia.post { txtExistencia.requestFocus() };
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

        btn_confirmar.setOnClickListener {
            if (txtUbicacion.text.toString().isNotEmpty() && txtBox.text.toString().isNotEmpty() && txtCodigo.text.toString().isNotEmpty() && txtExistencia.text.toString().isNotEmpty()){
                try {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Confirmar ajuste")
                    builder.setMessage("¿Desea confirmar el ajuste?")

                    builder.setPositiveButton("Sí") { _, _ ->
                        confirmar_ajuste(txtUbicacion.text.toString(),txtBox.text.toString(),txtCodigo.text.toString(),txtExistencia.text.toString());
                    }

                    builder.setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss();
                    }

                    val alertDialog = builder.create();
                    alertDialog.show();
                }catch (e: Exception){
                    MensajesDialog.showMessage(this@Ajustes, "Ocurrió un error: ${e.message}");
                }
            }else{
                MensajesDialog.showMessage(this,"Hay datos vacios");
            }


        }


    }

    private fun buscarCodigo(codigo:String){
        var txtDescripcion=findViewById<TextView>(R.id.txtDescripcion_ajustes);
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
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val descripcion =
                                        lista.map { it.DESCRIPCION }.toString().replace("[", "")
                                            .replace("]", "");
                                    txtDescripcion.setText(descripcion.toString());
                                } else {
                                    MensajesDialog.showMessage(this@Ajustes, "No se encontro ese código");
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Ajustes, "${error}");
                            }
                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Ajustes, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Ajustes, "Ocurrió un error: ${e.message}");
            }
        }

    }

    private fun confirmar_ajuste(ubicacion:String, box:String, codigo: String,existencia:String){
        var btn_eliminar_codigo=findViewById<Button>(R.id.buttonEliminar_codigo_Ajuste);
        var btn_eliminar_ubicacion=findViewById<Button>(R.id.buttonEliminar_ubicacion_Ajuste);
        var btn_eliminar_box=findViewById<Button>(R.id.buttonEliminar_box_Ajuste);
        var txtExistencia=findViewById<EditText>(R.id.txtExistencia_Ajustes);
        if (ubicacion.isNotEmpty() && box.isNotEmpty() && codigo.isNotEmpty() && existencia.isNotEmpty()){
            try {
                val body= mapOf(
                    "UBICACION" to ubicacion.uppercase(),
                    "BOX" to box.uppercase(),
                    "CODIGO" to codigo.uppercase(),
                    "EXISTENCIA" to existencia,
                    "ALMACEN_ID" to "1"
                )

                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch {
                    Pedir_datos_apis_post(
                        endpoint = "/inventarios/ubicaciones/codigo/existencia",
                        body=body,
                        dataClass = ListaAjusteConfir::class,
                        listaKey ="result",
                        headers = headers,
                        onSuccess = { lista ->
                            if (lista.message.equals("Ajuste realizado correctamente")) {
                                MensajesDialogConfirmaciones.showMessage(
                                    this@Ajustes, "OK") {
                                    btn_eliminar_ubicacion.performClick();
                                    btn_eliminar_box.performClick();
                                    btn_eliminar_codigo.performClick();
                                    txtExistencia.setText("");

                                }
                            }else{
                                MensajesDialog.showMessage(this@Ajustes, lista.message);
                            }
                        },
                        onError = { error ->
                            if (error.equals("OK", ignoreCase = true)) {

                                MensajesDialogConfirmaciones.showMessage(
                                    this@Ajustes, "OK") {
                                    btn_eliminar_ubicacion.performClick();
                                    btn_eliminar_box.performClick();
                                    btn_eliminar_codigo.performClick();
                                    txtExistencia.setText("");
                                }
                            } else {
                                MensajesDialog.showMessage(this@Ajustes, "Error: $error")
                            }
                        }
                    )
                }

            }catch (e: Exception){
                MensajesDialog.showMessage(this@Ajustes, "Ocurrió un error: ${e.message}");
            }

        }else{
            MensajesDialog.showMessage(this,"Hay datos vacios");
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_inventarios::class.java));
        //LogsEntradaSalida.logsPorModulo(this, lifecycleScope, "INVENTARIOS/AJUSTES", "SALIDA")
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