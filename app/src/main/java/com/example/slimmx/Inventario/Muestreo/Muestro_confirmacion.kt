package com.example.slimmx.Inventario.Muestreo

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
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
import com.example.slimmx.ListaMuestroItemsStock
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.R
import com.example.slimmx.Validaciones.BoxValidator
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Muestro_confirmacion : AppCompatActivity() {

    private lateinit var txt_stockVirtual:TextView;
    private lateinit var txt_apartado:TextView;
    private lateinit var txt_reserva:TextView;
    private lateinit var btn_confirmar:Button;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_muestro_confirmacion)
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
        val v_item = intent.getStringExtra("item_prodctos");

        val lb_ubicacion = findViewById<TextView>(R.id.LbUbicacion_Confirmacion);
        val lb_box = findViewById<TextView>(R.id.LbBox_Confirmacion);


        val txt_ubicacion_confi = findViewById<TextView>(R.id.txtUbicacion_Confirmacion);
        val txt_box_confi = findViewById<TextView>(R.id.txtBox_Confirmacion);
        val txtExistencia = findViewById<EditText>(R.id.txt_Existencia);
        val txtUsuario = findViewById<TextView>(R.id.txt_usuario);
        val txtcodigo_1 = findViewById<TextView>(R.id.txt_codigo_1);
        val txtcodigo_2 = findViewById<TextView>(R.id.txt_codigo_2);
        val txtDescripcion = findViewById<TextView>(R.id.txt_descripcion);
        val txtUbicacion = findViewById<TextView>(R.id.txt_ubicacion);
        val txtBox = findViewById<TextView>(R.id.txt_box);


        txt_ubicacion_confi.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txt_box_confi.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        txtDescripcion.setText(v_descripcion);
        txtUbicacion.setText(v_ubicacion);
        txtBox.setText(v_box);
        txt_box_confi.setText(v_box);
        val ultimos4 = v_codigo.toString().takeLast(4)
        val resto = v_codigo.toString().dropLast(4)

        txtcodigo_1.text = resto
        txtcodigo_2.text = ultimos4


        val btn_eliminar_ubicaciones =
            findViewById<Button>(R.id.buttonEliminar_Ubicaciones_Confirmacion);
        val btn_eliminar_box = findViewById<Button>(R.id.buttonEliminar_Box_Confirmacion);
        val btn_segunda_validacion = findViewById<Button>(R.id.button_SegundaConfir);
        val btn_cancelar = findViewById<Button>(R.id.buttonCancelar);
         btn_confirmar = findViewById<Button>(R.id.buttonConfirmar);
        val btn_actualizar = findViewById<Button>(R.id.button_Actualizar);

        txt_stockVirtual = findViewById(R.id.txt_stockVirtual);
        txt_apartado = findViewById(R.id.txt_apartado);
        txt_reserva=findViewById(R.id.txt_reserva);
        txtUsuario.setText(GlobalUser.nombre);

        lb_ubicacion.isVisible = false;
        lb_box.isVisible = false;
        txt_ubicacion_confi.isVisible = false;
        txt_box_confi.isVisible = false;
        btn_eliminar_ubicaciones.isVisible = false;
        btn_eliminar_box.isVisible = false;
        btn_confirmar.isVisible = false;

        btn_eliminar_ubicaciones.setOnClickListener {
            txt_ubicacion_confi.setText("");
            txt_ubicacion_confi.post{txt_ubicacion_confi.requestFocus()}
        }

        btn_eliminar_box.setOnClickListener {
            txt_box_confi.setText("S/B");
            txt_box_confi.post { txt_box_confi.requestFocus() };
        }

        txtBox.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtBox.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        BoxValidator.validateBox(
                            input = txtBox.text.toString(),
                            successComponent = {
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

        txt_box_confi.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txt_box_confi.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        BoxValidator.validateBox(
                            input = txt_box_confi.text.toString(),
                            successComponent = {
                                txtExistencia.post { txtExistencia.requestFocus() };
                            },
                            failureComponent = { message ->
                                MensajesDialog.showMessage(this, message)
                                txt_box_confi.setText("")
                                txt_box_confi.post { txt_box_confi.requestFocus() }
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

        btn_actualizar.setOnClickListener {
            try {
                actualizar(
                    v_codigo.toString(),
                    v_folio.toString(),
                    v_ubicacion.toString(),
                    v_box.toString()
                );
            } catch (e: Exception) {
                MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
            }
        }

        btn_segunda_validacion.setOnClickListener {
            try {
                if (txtExistencia.text.toString().toInt() >= 0 && txtExistencia.text.isNotEmpty()) {
                    lb_ubicacion.isVisible = true;
                    lb_box.isVisible = true;
                    txt_ubicacion_confi.isVisible = true;
                    txt_box_confi.isVisible = true;
                    btn_eliminar_ubicaciones.isVisible = true;
                    btn_eliminar_box.isVisible = true;
                    btn_confirmar.isVisible = true;
                    txt_ubicacion_confi.post { txt_ubicacion_confi.requestFocus() };
                } else {
                    MensajesDialog.showMessage(this, "NO HAY UNIDADES INGRESADAS");
                }
            } catch (e: Exception) {
                MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
            }

        }

        btn_cancelar.setOnClickListener {
            finish();
        }

        btn_confirmar.setOnClickListener {
            if (v_ubicacion.equals(txt_ubicacion_confi.text.toString()) && v_box.equals(
                    txt_box_confi.text.toString()
                )
            ) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Confirmación")
                builder.setMessage("¿Está seguro de que desea confirmar ${txtExistencia.text} piezas de existencia?");

                builder.setPositiveButton("Confirmar") { dialog, _ ->
                    try {
                        confirmarMuestreo(
                            v_folio.toString(),
                            v_item.toString(),
                            txtExistencia.text.toString()
                        );
                        dialog.dismiss()

                    } catch (e: Exception) {
                        MensajesDialog.showMessage(
                            this@Muestro_confirmacion,
                            "Ocurrió un error: ${e.message}"
                        );
                    }

                }
                builder.setNegativeButton("Cancelar") { dialog, _ ->

                    dialog.dismiss()
                }

                val dialog = builder.create()
                dialog.show()
            } else {
                MensajesDialog.showMessage(this, "Las ubicaciones no coinciden");
            }
        }
    }

    private fun actualizar(codigo:String, folio:String, ubicacion:String,box:String){
        val params= mapOf(
            "codigo" to codigo.uppercase(),
            "folio" to folio,
            "ubicacion" to ubicacion.uppercase(),
            "box" to box.uppercase()
        )

        val headers= mapOf("Token" to GlobalUser.token.toString());

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Pedir_datos_apis(
                    endpoint = "/paquetes/actualizar",
                    params=params,
                    dataClass = ListaMuestroItemsStock::class,
                    listaKey = "result",
                    headers = headers,
                    onSuccess = { lista ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            if (lista.isNotEmpty()) {
                                txt_stockVirtual.setText(
                                    lista.map { it.STOCK_VIRTUAL }.toString().replace("[", "")
                                        .replace("]", "")
                                );
                                txt_apartado.setText(
                                    lista.map { it.STOCK_APARTADO }.toString().replace("[", "")
                                        .replace("]", "")
                                );
                                txt_reserva.setText(
                                    lista.map { it.RESERVA_COMBOS_PACKS }.toString().replace("[", "").replace("]","")
                                );

                            } else {
                                MensajesDialog.showMessage(this@Muestro_confirmacion, "La lista se devolvio vacia")
                            }
                        }
                    }, onError = {error->
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Muestro_confirmacion, "${error}");
                        }
                    }
                )
            }catch (e: Exception) {
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Muestro_confirmacion, "Ocurrió un error: ${e.message}")
                }
            }
        }
    }

    private fun confirmarMuestreo(id:String, item:String, stock:String){
        try {
            btn_confirmar.isEnabled = false;
            var body= mapOf(
                "id" to id,
                "item" to item,
                "stock_confirmado" to stock
            )

            val headers= mapOf("Token" to GlobalUser.token.toString());

            lifecycleScope.launch {
                Pedir_datos_apis_post(
                    endpoint = "/inventario/tareas/muestreo/item/set",
                    body=body,
                    dataClass = Any::class,
                    listaKey = "message",
                    headers = headers,
                    onSuccess = {response ->
                        try {
                            try {
                                val message = response.toString()
                                if(message.contains("CONFIRMADO CON EXITO")){
                                    MensajesDialogConfirmaciones.showMessage(this@Muestro_confirmacion, "OK") {
                                        finish();
                                        btn_confirmar.isEnabled = true;
                                    }
                                }else{
                                    MensajesDialog.showMessage(this@Muestro_confirmacion, "Respuesta: $message");
                                    btn_confirmar.isEnabled = true;
                                }
                            } catch (e: Exception) {
                                MensajesDialog.showMessage(this@Muestro_confirmacion, "Error al procesar la respuesta: ${e.message}");
                                btn_confirmar.isEnabled = true;
                            }

                        } catch (e: Exception) {
                            MensajesDialog.showMessage(this@Muestro_confirmacion, "Error al procesar la respuesta: ${e.message}");
                            btn_confirmar.isEnabled = true;
                        }
                    },
                    onError = { error ->
                        MensajesDialog.showMessage(this@Muestro_confirmacion, "Error: $error");
                        btn_confirmar.isEnabled = true;
                    }
                )
            }

        }catch(e: Exception){
            MensajesDialog.showMessage(this@Muestro_confirmacion, "Ocurrió un error: ${e.message}");
            btn_confirmar.isEnabled = true;
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