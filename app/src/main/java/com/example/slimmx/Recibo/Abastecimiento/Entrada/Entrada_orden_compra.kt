package com.example.slimmx.Recibo.Abastecimiento.Entrada

import android.content.Intent
import android.os.Bundle
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
import com.example.slimmx.Recibo.Abastecimiento.Seleccion_recibo_abastecimiento
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.base.Ascii.toUpperCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Entrada_orden_compra : AppCompatActivity() {
    private lateinit var cbSelect: AutoCompleteTextView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_entrada_orden_compra)
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

        val btn_confirmar=findViewById<Button>(R.id.buttonConfirmar);
        val txtUbicacion=findViewById<EditText>(R.id.txtUbicacion);
        val btn_eliminar_ubicacion=findViewById<Button>(R.id.buttonEliminar);

        txtUbicacion.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        cbSelect = findViewById(R.id.cbSelect);

        //Obtenerfolios();

        val opciones = intent.getStringArrayExtra("folios")

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
            txtUbicacion.requestFocus();
        }

        btn_confirmar.setOnClickListener {
            if (cbSelect.text.toString().isNotEmpty() && txtUbicacion.text.toString().isNotEmpty()){
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Confirmación")
                builder.setMessage("¿Está seguro de que desea confirmar el siguiente elemento?")

                builder.setPositiveButton("Confirmar") { dialog, _ ->
                    ConfirmacioneEntrada(cbSelect.text.toString(), txtUbicacion.text.toString());
                    dialog.dismiss()
                }

                builder.setNegativeButton("Cancelar") { dialog, _ ->
                    btn_confirmar.isEnabled=true;
                    dialog.dismiss()
                }

                val dialog = builder.create()
                dialog.show()
            }else{
                MensajesDialog.showMessage(this, "Se deben de ingresar todos los datos");
                btn_confirmar.isEnabled=true;
            }
        }
    }

    private fun Obtenerfolios(){
        try {
            val headers = mapOf("Token" to GlobalUser.token.toString());

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/proveedores/compras/recibo",
                        params=emptyMap<String, String>(),
                        dataClass = ListReciboAbastecimiento::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isEmpty()) {
                                    val intent = Intent(this@Entrada_orden_compra, Seleccion_recibo_abastecimiento::class.java)
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
                                MensajesDialog.showMessage(this@Entrada_orden_compra, "${error}");
                            }
                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Entrada_orden_compra, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(
                    this@Entrada_orden_compra,
                    "Ocurrió un error: ${e.message}"
                );
            }
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
            MensajesDialog.showMessage(this@Entrada_orden_compra, "Ocurrió un error: ${e.message}");
        }

    }

    private fun ConfirmacioneEntrada(folio: String, ubicacion:String){
        val btn_confirmar=findViewById<Button>(R.id.buttonConfirmar);
        try {
            btn_confirmar.isEnabled=false;
            val body= mapOf(
                "FOLIO" to folio,
                "UBICACION" to toUpperCase(ubicacion)
            )
            val txtUbicacion=findViewById<EditText>(R.id.txtUbicacion);
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );

            lifecycleScope.launch {
                Pedir_datos_apis_post(
                    endpoint = "/compras/proveedores/entrada",
                    body=body,
                    dataClass = Any::class,
                    listaKey ="message",
                    headers = headers,
                    onSuccess = { response ->
                        val mensaje=response.toString();
                        if (mensaje.contains("CONFIRMADO EXITOSAMENTE")) {
                            MensajesDialogConfirmaciones.showMessage(
                                this@Entrada_orden_compra,
                                "Confirmada correctamente"
                            ) {
                                cbSelect.setText("");
                                txtUbicacion.setText("");
                                Obtenerfolios();
                                btn_confirmar.isEnabled=true;
                            }
                        }
                    },
                    onError = { error ->
                        MensajesDialog.showMessage(this@Entrada_orden_compra, "Error: $error");
                        btn_confirmar.isEnabled=true;
                    }
                )
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
            btn_confirmar.isEnabled=true;
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Seleccion_recibo_abastecimiento::class.java));
        //LogsEntradaSalida.logsPorModulo( this, lifecycleScope, "ABASTECIMIENTO/ENTRADA", "SALIDA");
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