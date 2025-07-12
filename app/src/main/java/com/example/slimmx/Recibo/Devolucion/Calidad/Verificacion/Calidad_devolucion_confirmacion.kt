package com.example.slimmx.Recibo.Devolucion.Calidad.Verificacion

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
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
import com.example.slimmx.Recibo.Devolucion.Calidad.Submenu_alta_calidad_devolucion
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Vista.FragmentPageEtiquetaBluetooth
import com.example.slimmx.Vista.FragmentPageImpresionEtiquetas
import com.example.slimmx.defectos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Calidad_devolucion_confirmacion : AppCompatActivity(), FragmentPageImpresionEtiquetas.OnBackgroundBlockerListener, FragmentPageEtiquetaBluetooth.OnBackgroundBlockerListener  {

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
        setContentView(R.layout.activity_calidad_devolucion_confirmacion)
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
        val txt_posibles_defectos=findViewById<TextView>(R.id.txt_posibles_defectos);

        val v_codigo = intent.getStringExtra("codigo");
        val v_descripcion = intent.getStringExtra("descripcion");
        val v_cantidad=intent.getStringExtra("cantidad");
        val v_item_producto=intent.getStringExtra("item_confirm");
        val v_folio=intent.getStringExtra("folio");
        val v_id=intent.getStringExtra("id");
        val v_posibles_defectos=intent.getStringExtra("posible_defecto");
        val v_referencia=intent.getStringExtra("referencia")

        val txtFolio=findViewById<TextView>(R.id.txtFolio);
        val txtCodigo=findViewById<TextView>(R.id.txtCodigo);
        val lb_Descripcion=findViewById<TextView>(R.id.lb_Descripcion);
        val txtCantidad=findViewById<TextView>(R.id.txtCantidad);
        val btn_cancelar=findViewById<Button>(R.id.buttonCancelar_Calidad);
        val btn_fotos=findViewById<Button>(R.id.buttonFotos)

        txt_posibles_defectos.setText(v_posibles_defectos);
        txtFolio.setText(v_folio);
        txtCodigo.setText(v_codigo);
        lb_Descripcion.setText(v_descripcion);

        obtenerItemsDefectos(v_codigo.toString());

        btn_cancelar.setOnClickListener {
            finish();
        }

        btn_confirmacion.setOnClickListener {
            confirmaciones(v_id.toString(),v_item_producto.toString(),v_cantidad.toString(),v_codigo.toString(), v_descripcion.toString(),txtCantidad.text.toString(),txtObservaciones.text.toString(),v_referencia.toString());
        }

        /*btn_fotos.setOnClickListener {
            /*val intent = Intent(this@Calidad_devolucion_confirmacion, CargaImagenes::class.java);
            startActivity(intent);*/

           // MensajesDialog.showMessage(this,"${v_referencia.toString()}")
            val intent = Intent(this, CargaImagenes::class.java)
            intent.putExtra("referencia",v_referencia);
            startActivity(intent)
        }*/

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
                                    MensajesDialog.showMessage(this@Calidad_devolucion_confirmacion, "Lista vacia")
                                }
                            }
                        },
                        onError = { error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Calidad_devolucion_confirmacion, "Error: $error")
                            }
                        } )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Calidad_devolucion_confirmacion, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Calidad_devolucion_confirmacion, "Ocurrió un error: ${e.message}");
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

    private fun confirmaciones(id:String,item:String,cantida_recib:String, codigo: String,descripcion:String,cantidad_confir:String, observaciones:String, referencia:String){
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
                    endpoint = "/devoluciones/recibo/calidad/producto",
                    body = body,
                    dataClass = Any::class,
                    listaKey = "message",
                    headers = headers,
                    onSuccess = { response ->
                        try {
                            try {
                                val message = response.toString()
                                if(message.contains("Confirmado con exito")){
                                    MensajesDialogConfirmaciones.showMessage(this@Calidad_devolucion_confirmacion, "Confirmado con exito") {
                                        finish();
                                        btn_confirmacion.isEnabled=true;
                                        val intent = Intent(this@Calidad_devolucion_confirmacion, CargaImagenes::class.java)
                                        intent.putExtra("referencia",referencia);
                                        startActivity(intent)
                                    }
                                }else{
                                    MensajesDialog.showMessage(this@Calidad_devolucion_confirmacion, "Respuesta: $message");
                                    btn_confirmacion.isEnabled=true;
                                }
                            } catch (e: Exception) {
                                MensajesDialog.showMessage(this@Calidad_devolucion_confirmacion, "Error al procesar la respuesta: ${e.message}")
                                btn_confirmacion.isEnabled=true;
                            }

                        } catch (e: Exception) {
                            MensajesDialog.showMessage(this@Calidad_devolucion_confirmacion, "Error al procesar la respuesta: ${e.message}")
                            btn_confirmacion.isEnabled=true;
                        }
                    },
                    onError = { error ->
                        MensajesDialog.showMessage(this@Calidad_devolucion_confirmacion, "Error: ${error}")
                        btn_confirmacion.isEnabled=true;
                    }
                )
            }


        }catch (e: Exception){
            MensajesDialog.showMessage(this@Calidad_devolucion_confirmacion, "Ocurrió un error: ${e.message}");
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