package com.example.slimmx.Empaque.Envio.Asignacion

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.Empaque.Envio.Submenu_Empaque_Envio
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListaCajasAsig
import com.example.slimmx.ListaFolioPickingEnvio
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.Picking.Envio.Submenu_Picking_Envio
import com.example.slimmx.Picking.Recolecta.Submenu_Picking_Recolecta
import com.example.slimmx.R
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Empaque_Envio_Asignacion : AppCompatActivity() {

    private lateinit var cbSelect: AutoCompleteTextView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_empaque_envio_asiganacion)
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

        cbSelect = findViewById(R.id.cbSelect)
        val btn_Confirmar=findViewById<Button>(R.id.buttonOK_PICK);
        val txtUbicacion=findViewById<EditText>(R.id.txtUbicacion);
        val btn_eliminar_ubicacion=findViewById<Button>(R.id.buttonEliminar_ubicacion);

        //obtenerfolio();

        val opciones = intent.getStringArrayExtra("folios")

        if (opciones != null && opciones.isNotEmpty()) {
            actualizarcbBox(opciones.toList()) // Llenar el ComboBox con los datos recibidos
        } else {
            // Si no hay datos, regresar al Submenu_acomodo
            val intent = Intent(this, Submenu_Empaque_Envio::class.java)
            intent.putExtra("MESSAGE", "No hay tareas para este usuario")
            startActivity(intent)
            finish()
        }

        txtUbicacion.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        btn_Confirmar.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmación")
            builder.setMessage("¿Estás seguro de que deseas confirmar?")

            builder.setPositiveButton("Sí") { dialog, which ->
                if(txtUbicacion.text.isNotEmpty() && cbSelect.text.isNotEmpty()){
                    try {
                        Confirmacion_datos(cbSelect.text.toString(), txtUbicacion.text.toString())
                    }catch (e: Exception){
                        MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                    }
                }else{
                    MensajesDialog.showMessage(this, "CAMPOS VACIOS");
                }

            }

            builder.setNegativeButton("No") { dialog, which ->

            }
            builder.show()

        }

        btn_eliminar_ubicacion.setOnClickListener {
            txtUbicacion.setText("");
            txtUbicacion.post { txtUbicacion.requestFocus() }
        }

    }

    private fun obtenerfolio(){
        try {
            val params= mapOf(
                "prefijo" to ""
            );

            val headers = mapOf("Token" to GlobalUser.token.toString());

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/inventario/picking/folios/asig/empaque",
                        params=params,
                        dataClass = ListaFolioPickingEnvio::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.ID }
                                    actualizarcbBox(opciones);
                                } else {
                                    val intent = Intent(this@Empaque_Envio_Asignacion, Submenu_Picking_Envio::class.java);
                                    intent.putExtra("MESSAGE", "No hay folios disponibles");
                                    startActivity(intent);
                                    finish();
                                }

                            }
                        },
                        onError = { error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Empaque_Envio_Asignacion, "Error: $error")
                            }
                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Empaque_Envio_Asignacion, "Ocurrió un error: ${e.message}")
                    }
                }
            }


        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Empaque_Envio_Asignacion, "Ocurrió un error: ${e.message}");
            }
        }

    }

    private fun actualizarcbBox(opciones: List<String>) {
        try {
            val adaptador = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                opciones
            )
            cbSelect.setAdapter(adaptador)

            // Cuando se selecciona algun dato en el comboBox
            cbSelect.setOnItemClickListener { _, _, position, _ ->
                val seleccion = opciones[position]
                val textCodigo=findViewById<EditText>(R.id.txtUbicacion)
                val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

                if (tableLayout.childCount > 1) {
                    tableLayout.removeViews(1, tableLayout.childCount - 1)
                }
                optenerDatositemsTareas(seleccion)//Metodo optencion de items
                textCodigo.post { textCodigo.requestFocus() }
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun optenerDatositemsTareas(folios: String){
        if (folios.isNotEmpty()){
            try {

                val params_pickeados= mapOf(
                    "folio" to folios
                )

                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/empaque/items/cajas/asig",
                            params=params_pickeados,
                            dataClass = ListaCajasAsig::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val items = lista.map {
                                            ListaCajasAsig(
                                                CAJA = it.CAJA,
                                                CANTIDAD_EMPACADA = it.CANTIDAD_EMPACADA,
                                            )
                                        }
                                        actualizarDatosTablaHistorial(items)
                                    }else{
                                        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

                                        if (tableLayout.childCount > 1) {
                                            tableLayout.removeViews(1, tableLayout.childCount - 1)
                                        }
                                    }
                                }
                            },
                            onError = {error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Empaque_Envio_Asignacion, "Error: $error")
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Empaque_Envio_Asignacion, "Ocurrió un error: ${e.message}")
                        }
                    }
                }


            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Empaque_Envio_Asignacion, "Ocurrió un error: ${e.message}");
                }
            }

        }else{
            MensajesDialog.showMessage(this,"Debes de seleccionar un folio")
        }
    }


    private fun actualizarDatosTablaHistorial(items: List<ListaCajasAsig>){
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

            if (tableLayout.childCount > 1) {
                tableLayout.removeViews(1, tableLayout.childCount - 1)
            }

            for (item in items) {
                val tableRow = TableRow(this);
                agregarFilaHistorial(tableRow, item);


                tableLayout.addView(tableRow)
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun agregarFilaHistorial(tableRow: TableRow, item: ListaCajasAsig) { //Agregar los datos encontrados en la tabla del historial
        try {
            val cajas = TextView(this).apply {
                text = item.CAJA;
                gravity= Gravity.CENTER;
            }

            val cantidadText = TextView(this).apply {
                text = item.CANTIDAD_EMPACADA.toString();
                gravity = Gravity.CENTER;
            }


            tableRow.addView(cajas);
            tableRow.addView(cantidadText);

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun Confirmacion_datos(id:String,ubicacion: String){
        if (id.isNotEmpty() && ubicacion.isNotEmpty() ) {
            try {
                val body = mapOf(
                    "ID" to id,
                    "UBICACION_DESTINO" to ubicacion.uppercase()
                )

                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch {
                    Pedir_datos_apis_post(
                        endpoint = "/inventarios/caja/asig",
                        body = body,
                        dataClass =Any::class,
                        listaKey = "message",
                        headers=headers,
                        onSuccess = { response ->
                            try {
                                try {
                                    val message = response.toString()
                                    if(message.contains("ENTREGADO CORRECTAMENTE")){
                                        MensajesDialogConfirmaciones.showMessage(this@Empaque_Envio_Asignacion, "OK") {
                                            startActivity(Intent(this@Empaque_Envio_Asignacion,Submenu_Empaque_Envio::class.java));
                                            finish();}
                                    }else{
                                        MensajesDialog.showMessage(this@Empaque_Envio_Asignacion, "Respuesta: $message");
                                    }
                                } catch (e: Exception) {
                                    MensajesDialog.showMessage(this@Empaque_Envio_Asignacion, "Error al procesar la respuesta: ${e.message}")
                                }

                            } catch (e: Exception) {
                                MensajesDialog.showMessage(this@Empaque_Envio_Asignacion, "Error al procesar la respuesta: ${e.message}")
                            }
                        },
                        onError = { error ->
                            MensajesDialog.showMessage(this@Empaque_Envio_Asignacion, "Error: $error")
                        }
                    )
                }

            }catch (e: Exception){
                MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
            }

        }else {
            MensajesDialog.showMessage(this, "Por favor, complete todos los campos.")
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_Empaque_Envio::class.java));
        //LogsEntradaSalida.logsPorModulo( this@Empaque_Envio_Asignacion, lifecycleScope, "EMPAQUE/ENVIO/ASIGNACION", "SALIDA");
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