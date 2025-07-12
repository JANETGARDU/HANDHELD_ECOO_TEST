package com.example.slimmx.Recibo.Devolucion.Recibo

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
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
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.R
import com.example.slimmx.Validaciones.LogsEntradaSalida
import kotlinx.coroutines.launch

class Recibo_devolucion_confirmar : AppCompatActivity() {

    private var tipo: String="";
    private lateinit var btn_confirmacion:Button;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recibo_devolucion_confirmar)
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
        val v_codigo = intent.getStringExtra("codigo");
        val v_descripcion = intent.getStringExtra("descripcion");
        val v_id = intent.getStringExtra("id_confir");
        val v_item_confirm = intent.getStringExtra("item_confirm");
        val v_cantidad = intent.getStringExtra("cantidad");
        val v_referencia=intent.getStringExtra("referencia");
        val v_calidad=intent.getStringExtra("calidad");
        val v_razon_calidad=intent.getStringExtra("razon_calidad");

        val txtCodigo = findViewById<TextView>(R.id.txtCodigo);
        val txtDescripcion = findViewById<TextView>(R.id.txtDescripcion);
        val txtOrden = findViewById<TextView>(R.id.txtOrden);
        val txtcantidad=findViewById<TextView>(R.id.txtCantidad);

        txtCodigo.setText(v_codigo);
        txtDescripcion.setText(v_descripcion);
        txtOrden.setText(v_id);

        val ckGeneral=findViewById<RadioButton>(R.id.ckGeneral);
        val ckControlCalidad=findViewById<RadioButton>(R.id.ckcontrol_calidad);
        val txtReferencia=findViewById<EditText>(R.id.txtReferencia_Recibo);

        txtReferencia.setText(v_referencia);

        ckGeneral.isChecked=true;

        txtcantidad.post { txtcantidad.requestFocus() }

        if (v_calidad.toString()=="true"){
            ckControlCalidad.isChecked=true;
            ckGeneral.isChecked=false;
            ckGeneral.isVisible=false;
        }else{
            ckControlCalidad.isChecked=false;
            ckGeneral.isChecked=true;
            ckGeneral.isVisible=true;
        }


        val btn_cancelar=findViewById<Button>(R.id.buttonCancelar)
        btn_confirmacion=findViewById(R.id.buttonConfirmar)

        btn_cancelar.setOnClickListener {
            finish();
        }


        btn_confirmacion.setOnClickListener {
            try {
                val radioGroupRecolecta = findViewById<RadioGroup>(R.id.radioGroup_Recolecta)
                tipo = when (radioGroupRecolecta.checkedRadioButtonId) {
                    R.id.ckGeneral -> "GENERAL"
                    R.id.ckcontrol_calidad -> "CONTROL CALIDAD"
                    else -> "No seleccionado"
                }

                if (txtcantidad.text.toString().toInt()<=v_cantidad.toString().toInt()){
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Confirmación")
                    builder.setMessage("¿Está seguro de que desea confirmar el siguiente elemento?")

                    builder.setPositiveButton("Confirmar") { dialog, _ ->
                        Confirmacion(v_id.toString(),v_item_confirm.toString(),txtcantidad.text.toString(),v_referencia.toString(),tipo.toString());
                        dialog.dismiss()
                    }

                    builder.setNegativeButton("Cancelar") { dialog, _ ->
                        dialog.dismiss()
                    }

                    val dialog = builder.create()
                    dialog.show()
                }else{
                    MensajesDialog.showMessage(this, "¡NO PUEDES INGRESAR PRODUCTOS DE MAS! ");
                    btn_confirmacion.isEnabled=true;
                }

            }catch (e: Exception){
                MensajesDialog.showMessage(this@Recibo_devolucion_confirmar, "Ocurrió un error: ${e.message}");
                btn_confirmacion.isEnabled=true;
            }

        }
    }

    private fun Confirmacion(id: String, item_producto:String, unidades:String,referencia:String, tipo:String){
        try {
            btn_confirmacion.isEnabled=false;
            val body= mapOf(
                "ID" to id,
                "ITEM" to item_producto,
                "CANTIDAD" to unidades,
                "REFERENCIA" to referencia,
                "TIPO" to tipo
            )

            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );

            lifecycleScope.launch {
                Pedir_datos_apis_post(
                    endpoint = "/devoluciones/recibo/items/add",
                    body=body,
                    listaKey ="message",
                    dataClass = Any::class,
                    headers = headers,
                    onSuccess = { response ->
                        val mensaje=response.toString();
                        if (mensaje.contains("CONFIRMADO CORRECTAMENTE")) {
                            MensajesDialogConfirmaciones.showMessage(
                                this@Recibo_devolucion_confirmar,
                                "Confirmada correctamente"
                            ) {
                                finish();
                                btn_confirmacion.isEnabled=true;
                            }
                        }
                    },
                    onError = { error ->
                        MensajesDialog.showMessage(this@Recibo_devolucion_confirmar, "Error: $error");
                        btn_confirmacion.isEnabled=true;
                    }
                )
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
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