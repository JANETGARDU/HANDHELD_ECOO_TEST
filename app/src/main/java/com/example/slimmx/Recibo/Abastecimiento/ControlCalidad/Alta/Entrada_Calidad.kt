package com.example.slimmx.Recibo.Abastecimiento.ControlCalidad.Alta

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListReciboAbastecimiento
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.R
import com.example.slimmx.Recibo.Abastecimiento.ControlCalidad.Submenu_control_calidad
import com.example.slimmx.Recibo.Abastecimiento.Seleccion_recibo_abastecimiento
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Entrada_Calidad : AppCompatActivity() {

    private lateinit var txtUbicacion:EditText;
    private lateinit var cbSelect: AutoCompleteTextView;
    private lateinit var btn_confirmar: Button;
    private lateinit var txtCodigo:EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_entrada_calidad)
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

        txtUbicacion = findViewById(R.id.txtUbicacion);
        cbSelect = findViewById(R.id.cbSelect);
        btn_confirmar = findViewById(R.id.buttonConfirmar);
        txtCodigo = findViewById(R.id.txtCodigo)
        btn_confirmar.isEnabled = false;

        val btn_eliminar_ubicacion = findViewById<Button>(R.id.buttonEliminar_Ubicacion);
        val btn_eliminar_codigo = findViewById<Button>(R.id.buttonEliminar_CODIGO)

        val opciones = intent.getStringArrayExtra("folios")

        txtUbicacion.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txtCodigo.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        if (opciones != null && opciones.isNotEmpty()) {
            actualizarcbBox(opciones.toList()) // Llenar el ComboBox con los datos recibidos
        } else {
            val intent = Intent(this, Seleccion_recibo_abastecimiento::class.java)
            intent.putExtra("MESSAGE", "No hay folios disponibles")
            startActivity(intent)
            finish()
        }

        btn_eliminar_ubicacion.setOnClickListener {
            txtUbicacion.setText("");
            txtUbicacion.post { txtUbicacion.requestFocus() }
        }

        btn_eliminar_codigo.setOnClickListener {
            txtCodigo.setText("");
            txtCodigo.post { txtCodigo.requestFocus() }
        }

        txtUbicacion.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtUbicacion.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        if (inputText.equals("CALIDAD ACCESORIOS") || inputText.equals("CALIDAD REFACCIONES")){
                            btn_confirmar.isEnabled=true;
                        }else{
                            MensajesDialog.showMessage(this, "ESA UBICACIÓN NO PERTENECE A CALIDAD");
                            txtUbicacion.setText("");
                            txtUbicacion.post{txtUbicacion.requestFocus()}
                            btn_confirmar.isEnabled=false;
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
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmación")
            builder.setMessage("¿Está seguro de que desea confirmar el siguiente elemento?")

            builder.setPositiveButton("Confirmar") { dialog, _ ->
                Confirmacion(cbSelect.text.toString(), txtCodigo.text.toString());
                dialog.dismiss()
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->
                btn_confirmar.isEnabled=false;
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }

    }

    private fun actualizarcbBox(opciones: List<String>) {
        try {
            val txtUbicacion=findViewById<EditText>(R.id.txtUbicacion)
            val adaptador = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                opciones
            )
            cbSelect.setAdapter(adaptador)

            cbSelect.setOnItemClickListener { _, _, position, _ ->


                txtUbicacion.post { txtUbicacion.requestFocus() }
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this@Entrada_Calidad, "Ocurrió un error: ${e.message}");
        }

    }

    private fun Confirmacion(folio:String, codigo:String){
        val btn_confirmar=findViewById<Button>(R.id.buttonConfirmar);
        try {
            val body= mapOf(
                "FOLIO" to folio,
                "CODIGO" to codigo
            )
            val txtUbicacion=findViewById<EditText>(R.id.txtUbicacion);
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );

            lifecycleScope.launch {
                Pedir_datos_apis_post(
                    endpoint = "/compras/entrada/calidad",
                    body=body,
                    dataClass = Any::class,
                    listaKey ="message",
                    headers = headers,
                    onSuccess = { response ->
                        val mensaje=response.toString();
                        if (mensaje.contains("CONFIRMADO EXITOSAMENTE")) {
                            MensajesDialogConfirmaciones.showMessage(
                                this@Entrada_Calidad,
                                "Confirmada correctamente"
                            ) {
                                //cbSelect.setText("");
                                //txtUbicacion.setText("");
                                txtCodigo.setText("");
                                Obtenerfolios();
                                btn_confirmar.isEnabled=true;
                            }
                        }
                    },
                    onError = { error ->
                        MensajesDialog.showMessage(this@Entrada_Calidad, "Error: $error");
                        btn_confirmar.isEnabled=true;
                    }
                )
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
            btn_confirmar.isEnabled=true;
        }
    }

    private fun Obtenerfolios(){
        try {
            btn_confirmar.isEnabled=false;
            val headers = mapOf("Token" to GlobalUser.token.toString());

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/compras/recibo/calidad/folio/entrada",
                        params=emptyMap<String, String>(),
                        dataClass = ListReciboAbastecimiento::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isEmpty()) {
                                    val intent = Intent(this@Entrada_Calidad, Submenu_control_calidad::class.java)
                                    intent.putExtra("MESSAGE", "No hay tareas para este usuario")
                                    startActivity(intent)
                                    finish()
                                } else {
                                    val opciones = lista.map { it.FOLIO }
                                    actualizarcbBox(opciones)
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Entrada_Calidad, "${error}");
                            }
                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Entrada_Calidad, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(
                    this@Entrada_Calidad,
                    "Ocurrió un error: ${e.message}"
                );
            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_control_calidad::class.java));
        //LogsEntradaSalida.logsPorModulo( this@Entrada_Calidad, lifecycleScope, "ABASTECIMIENTO/CALIDAD/ENTRADA", "SALIDA");
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