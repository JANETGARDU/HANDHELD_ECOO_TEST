package com.example.slimmx.Picking.Cedis.ConsumoInterno

import android.content.ClipDescription
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalConfig
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListConsumaInterno
import com.example.slimmx.ListProducto
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.Picking.Cedis.Cedis_seleccion_menu
import com.example.slimmx.Picking.Cedis.VentaInterna.Venta_Interna_List
import com.example.slimmx.R
import com.example.slimmx.Validaciones.BoxValidator
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.example.slimmx.listaUnidades
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Consumo_Interno_PI : AppCompatActivity() {

    private var token:String="";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_consumo_interno_pi)
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

        var txtCodigo=findViewById<EditText>(R.id.txtEditCodigo_Pi_CoIn);
        var txtUbicacion=findViewById<EditText>(R.id.txtEditUbicacionOri_Pi_CoIn);
        var txtBox=findViewById<EditText>(R.id.txtEditBoxOri_Pi_CoIn);
        var txtdescripcion=findViewById<TextView>(R.id.txtEditDescripcion_Pi_CoIn);
        var txtUnidades=findViewById<TextView>(R.id.txtEditUnidades_Pi_CoIn);
        var txtCantidad=findViewById<EditText>(R.id.txtEditCantidad_Pi_CoIn);
        var btn_eliminarCodigo=findViewById<Button>(R.id.buttonEliminar_Codigo_ConsumoIn);
        var btn_eliminarUbicacion=findViewById<Button>(R.id.buttonEliminar_Ubicacion_ConsumoIn);
        var btn_eliminarBox=findViewById<Button>(R.id.buttonEliminar_Box_ConsumoIn);
        var btn_salir=findViewById<Button>(R.id.buttonCancelar_ConsumoIn_Pi);
        var btn_confirmar=findViewById<Button>(R.id.buttonConfirmar_ConsumoIn_Pi);


        //Supervisor
        val opcionesDefaultSupervisor = listOf("Ayde", "Erik Eduardo", "Jose Eduardo", "Juan Carlos", "Tania Merab", "Veronica Yazmin", "Victor","Jair", "Christina")

        val autoCompleteSupervisor = findViewById<AutoCompleteTextView>(R.id.cbSelectSupervisor_PI_Ci)

        val adaptadorSupervisor = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opcionesDefaultSupervisor)
        autoCompleteSupervisor.setAdapter(adaptadorSupervisor)


        //Razon
        val opcionesDefaultRazon = listOf("Reemplazo", "Asignación");

        val autoCompleteRazon = findViewById<AutoCompleteTextView>(R.id.cbSelectRazon_PI_Ci);

        val adaptadorRazon = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opcionesDefaultRazon);
        autoCompleteRazon.setAdapter(adaptadorRazon);


        txtCodigo.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txtUbicacion.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txtBox.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        txtCodigo.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtCodigo.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        consultarCodigo(txtCodigo.text.toString());
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
                        consultarCodigoUbicaciones(
                            txtCodigo.text.toString(),
                            txtUbicacion.text.toString(),
                            "S/B"
                        );
                        txtBox.setText("S/B");
                        btn_confirmar.isEnabled=true;
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                    btn_confirmar.isEnabled=false;
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
                                consultarCodigoUbicaciones(txtCodigo.text.toString(), txtUbicacion.text.toString(), txtBox.text.toString());
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

        btn_eliminarCodigo.setOnClickListener {
            txtCodigo.setText("");
            txtdescripcion.setText("");
        }

        btn_eliminarUbicacion.setOnClickListener {
            txtUbicacion.setText("");
            txtUnidades.setText("0");
        }

        btn_eliminarBox.setOnClickListener {
            txtBox.setText("");
            txtUnidades.setText("0");
        }

        btn_salir.setOnClickListener {
            finish();
        }

        btn_confirmar.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmación")
            builder.setMessage("¿Está seguro de que desea confirmar el elemento?")
            builder.setPositiveButton("Confirmar") { dialog, _ ->
                try {
                    confirmar(txtCodigo.text.toString(), txtdescripcion.text.toString(), txtUbicacion.text.toString(), txtBox.text.toString(), txtCantidad.text.toString(), autoCompleteRazon.text.toString(),autoCompleteSupervisor.text.toString());
                    dialog.dismiss()
                }catch (e: Exception){
                    MensajesDialog.showMessage(this@Consumo_Interno_PI, "Ocurrió un error: ${e.message}");
                    btn_confirmar.isEnabled=true;
                }
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
                btn_confirmar.isEnabled=true;
            }

            val dialog = builder.create()
            dialog.show()
        }

    }

    private fun consultarCodigo(codigo:String){
        val txtDescripcion=findViewById<TextView>(R.id.txtEditDescripcion_Pi_CoIn);
        val txtUbiOrigen=findViewById<EditText>(R.id.txtEditUbicacionOri_Pi_CoIn);
        if (codigo.isNotEmpty()){
            try {
                val params= mapOf(
                    "limit" to "1",
                    "search" to codigo.uppercase()
                )

                val headers= mapOf(
                    "Token" to  GlobalUser.token.toString()
                )

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
                                        val datos = lista[0];
                                        txtDescripcion.setText(datos.DESCRIPCION);
                                        txtUbiOrigen.post { txtUbiOrigen.requestFocus() }
                                    } else {
                                        MensajesDialog.showMessage(
                                            this@Consumo_Interno_PI,
                                            "No se encontro ese código"
                                        );
                                    }

                                }
                            }, onError = {error->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Consumo_Interno_PI, "Error $error");
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Consumo_Interno_PI, "Ocurrió un error: ${e.message}")
                        }
                    }
                }
            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Consumo_Interno_PI, "Ocurrió un error: ${e.message}");
                }
            }
        }else{
            MensajesDialog.showMessage(this, "Se debe escanear algún código")
        }
    }

    private fun consultarCodigoUbicaciones(codigo:String, ubicacion:String, box:String){
        val txtUnidades=findViewById<TextView>(R.id.txtEditUnidades_Pi_CoIn);
        val txtCantidad=findViewById<EditText>(R.id.txtEditCantidad_Pi_CoIn);
        val txtBox=findViewById<EditText>(R.id.txtEditBoxOri_Pi_CoIn);
        if (codigo.isNotEmpty()){
            try {
                val params= mapOf(
                    "codigo" to codigo.uppercase(),
                    "ubicacion" to ubicacion.uppercase(),
                    "box" to box.uppercase()
                )

                val headers= mapOf(
                    "Token" to GlobalUser.token.toString()
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/productos/ubicacion/unidades",
                            params=params,
                            dataClass = listaUnidades::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val datos = lista[0];
                                        txtUnidades.setText(datos.UNIDADES.toString());
                                        txtCantidad.post { txtCantidad.requestFocus() }

                                    } else {
                                        //MensajesDialog.showMessage(this, "No se encontro ese código");
                                        txtBox.post { txtBox.requestFocus() }
                                    }

                                }
                            }, onError = {error->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Consumo_Interno_PI, "Error $error");
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Consumo_Interno_PI, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Consumo_Interno_PI, "Ocurrió un error: ${e.message}");
                }
            }

        }else{
            MensajesDialog.showMessage(this, "Se debe escanear algún código")
        }
    }

    private fun confirmar(codigo: String, descripcion:String, ubicacion: String, box: String, cantidad:String, razon: String, supervisor:String){
        var txtCodigo=findViewById<EditText>(R.id.txtEditCodigo_Pi_CoIn);
        var txtUbicacion=findViewById<EditText>(R.id.txtEditUbicacionOri_Pi_CoIn);
        var txtBox=findViewById<EditText>(R.id.txtEditBoxOri_Pi_CoIn);
        var txtdescripcion=findViewById<TextView>(R.id.txtEditDescripcion_Pi_CoIn);
        var txtUnidades=findViewById<TextView>(R.id.txtEditUnidades_Pi_CoIn);
        var txtCantidad=findViewById<EditText>(R.id.txtEditCantidad_Pi_CoIn);
        var btn_confirmar=findViewById<Button>(R.id.buttonConfirmar_ConsumoIn_Pi);
        if (codigo.isNotEmpty() && descripcion.isNotEmpty() && ubicacion.isNotEmpty() && box.isNotEmpty() && cantidad.isNotEmpty() && razon.isNotEmpty() && supervisor.isNotEmpty()){
            try {
                btn_confirmar.isEnabled=false;
                var body= mapOf(
                    "CODIGO" to codigo.uppercase(),
                    "DESCRIPCION" to descripcion,
                    "UBICACION" to ubicacion.uppercase(),
                    "BOX" to box,
                    "CANTIDAD" to cantidad,
                    "RAZON" to razon,
                    "SUPERVISOR" to supervisor
                )

                var  headers= mapOf(
                    "Token" to GlobalUser.token.toString()
                )

                lifecycleScope.launch {
                    Pedir_datos_apis_post(
                        endpoint = "/consumo_interno",
                        body =body,
                        dataClass = Any::class,
                        listaKey = "message",
                        headers = headers,
                        onSuccess = { lista ->
                            if (lista.toString().contains("CONSUMO INTERNO REGISTRADO CON EXITO")) {
                                MensajesDialog.showMessage(this@Consumo_Interno_PI, "Ok")
                                txtCodigo.setText("");
                                txtdescripcion.setText("");
                                txtUbicacion.setText("");
                                txtBox.setText("");
                                txtUnidades.setText("");
                                txtCantidad.setText("");
                                txtCodigo.post { txtCodigo.requestFocus() }
                            }
                            btn_confirmar.isEnabled=true;
                        },
                        onError = { error ->
                            if (error.equals("OK", ignoreCase = true)) {
                                MensajesDialog.showMessage(this@Consumo_Interno_PI, "Ok")
                                txtCodigo.setText("");
                                txtdescripcion.setText("");
                                txtUbicacion.setText("");
                                txtBox.setText("");
                                txtCantidad.setText("");
                                txtUnidades.setText("");
                                txtCodigo.post { txtCodigo.requestFocus() }
                                btn_confirmar.isEnabled=true;
                            } else {
                                MensajesDialog.showMessage(this@Consumo_Interno_PI, "Error: $error")
                                btn_confirmar.isEnabled=true;
                            }
                        }
                    )
                }

            }catch (e: Exception){
                MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                btn_confirmar.isEnabled=true;
            }

        }else{
            btn_confirmar.isEnabled=true;
            MensajesDialog.showMessage(this,"Hay algun dato faltante");
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Cedis_seleccion_menu::class.java));
        //LogsEntradaSalida.logsPorModulo( this@Consumo_Interno_PI, lifecycleScope, "PICKING/CEDIS/CONSUMO/INTERNO", "SALIDA");
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
