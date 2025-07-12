package com.example.slimmx.Embarque.Devolucion_Proveedor

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
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
import com.example.slimmx.GlobalConfig
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListaCheckReacomodo
import com.example.slimmx.ListaDevPr
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.R
import com.example.slimmx.Submenus.Submenu_acomodo
import com.example.slimmx.Submenus.Submenu_embarque
import com.example.slimmx.Submenus.Submenu_reacomodo
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.example.slimmx.lista_respuestas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Devolucion_proveedor : AppCompatActivity() {

    private lateinit var cbSelectFolios: AutoCompleteTextView;
    private var filaSeleccionada: TableRow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_devolucion_proveedor)
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

        cbSelectFolios = findViewById(R.id.cbSelectDevList);

        val opciones = intent.getStringArrayExtra("folios");

        val txtUbicacion=findViewById<EditText>(R.id.txtUbicacion);
        val txtAnden=findViewById<EditText>(R.id.txt_Anden);
        val btn_confirmar=findViewById<Button>(R.id.buttonConfirmar);
        val btn_eliminar_ubicacion=findViewById<Button>(R.id.buttonUbicacionEliminar);
        val btn_eliminar_almacen=findViewById<Button>(R.id.buttonAndenEliminar);

        txtUbicacion.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        txtAnden.setOnTouchListener(MostrarTeclado.getMostrarTeclado());
        btn_confirmar.isEnabled=false;

        if (opciones != null && opciones.isNotEmpty()) {
            actualizarcbBox(opciones.toList())
        } else {
            // Si no hay datos, regresar al Submenu_acomodo
            val intent = Intent(this, Submenu_embarque::class.java)
            intent.putExtra("MESSAGE", "No hay folios disponibles");
            startActivity(intent)
            finish()
        }

        btn_eliminar_ubicacion.setOnClickListener {
            txtUbicacion.post{txtUbicacion.requestFocus()};
            txtUbicacion.setText("");
        }

        btn_eliminar_almacen.setOnClickListener {
            txtAnden.post { txtAnden.requestFocus() };
            txtAnden.setText("");
        }

        txtUbicacion.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtUbicacion.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        if (inputText.equals("ANDEN DESPACHO")){
                            txtAnden.post{txtAnden.requestFocus()};
                        }else{
                            MensajesDialog.showMessage(this@Devolucion_proveedor, "No es una ubicación correcta");
                            btn_eliminar_ubicacion.performClick();
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

        txtAnden.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtAnden.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        if (inputText.trim().uppercase().startsWith("ANDEN")){
                            btn_confirmar.isEnabled=true;
                        }else{
                            MensajesDialog.showMessage(this@Devolucion_proveedor, "Ese no es un anden correcto para la salida");
                            btn_eliminar_almacen.performClick();
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
                Confirmacion(txtUbicacion.text.toString(), txtAnden.text.toString());
                dialog.dismiss()
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->

                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }

    }

    private fun actualizarcbBox(opciones: List<String>) {
        try {
            val adaptador = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                opciones
            )
            cbSelectFolios.setAdapter(adaptador)

            cbSelectFolios.setOnItemClickListener { _, _, position, _ ->
                val seleccion = opciones[position]
                val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

                if (tableLayout.childCount > 1) {
                    tableLayout.removeViews(1, tableLayout.childCount - 1)
                }
                ObtenerItems(seleccion);
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun ObtenerItems(folio: String){
        try {
            val params= mapOf(
                "folio" to folio
            )

            val headers= mapOf(
                "Token" to GlobalUser.token.toString()
            );

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/productos/devolucion/embarque",
                        params = params,
                        dataClass = ListaDevPr::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val items = lista.map {
                                        ListaDevPr(
                                            CANTIDAD=it.CANTIDAD,
                                            CODIGO=it.CODIGO,
                                            DESCRIPCION=it.DESCRIPCION
                                        )

                                    }
                                    actualizarTableLayout(items);

                                } else {
                                    MensajesDialog.showMessage(
                                        this@Devolucion_proveedor,
                                        "No hay datos para mostrar"
                                    );
                                }
                            }
                        }, onError = { error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Devolucion_proveedor, "${error}");
                            }
                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        // Captura de excepciones y mostrar un mensaje de error
                        MensajesDialog.showMessage(this@Devolucion_proveedor, "Ocurrió un error: ${e.message}")
                    }
                }

            }
        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Devolucion_proveedor, "Ocurrió un error: ${e.message}");
            }
        }
    }

    private fun actualizarTableLayout(items: List<ListaDevPr>) {
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout)
            val txtUbicacion=findViewById<EditText>(R.id.txtUbicacion);

            if (tableLayout.childCount > 1) {
                tableLayout.removeViews(1, tableLayout.childCount - 1)
            }

            for (item in items) {
                val tableRow = TableRow(this)
                agregarFila(tableRow, item)

                tableLayout.addView(tableRow)
            }
            txtUbicacion.post { txtUbicacion.requestFocus() }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun agregarFila(tableRow: TableRow, item: ListaDevPr) {
        try {
            val cantidad= TextView(this).apply {
                text=item.CANTIDAD.toString();
                gravity= Gravity.CENTER
            }
            val codigo= TextView(this).apply {
                text=item.CODIGO;
                gravity= Gravity.CENTER
            }

            val descripcion= TextView(this).apply {
                text=item.DESCRIPCION;
            }

            tableRow.addView(cantidad);
            tableRow.addView(codigo);
            tableRow.addView(descripcion);

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun Confirmacion(ubicacion:String, anden:String){
        try {
            val body= mapOf(
                "FOLIO_DEV" to cbSelectFolios.text.toString(),
                "ANDEN" to anden.uppercase(),
                "UBICACION" to ubicacion.uppercase(),
                "BOX" to "S/B"
            )
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );
            lifecycleScope.launch {
                Pedir_datos_apis_post(
                    endpoint = "/devolucion/proveedor/embarque/confirmar",
                    body=body,
                    dataClass = Any::class,
                    listaKey = "result",
                    headers = headers,
                    onSuccess = {response ->
                        val message = response.toString()
                        if(message.contains("PROCESO COMPLETADO")){
                            MensajesDialogConfirmaciones.showMessage(this@Devolucion_proveedor, "OK") {
                                finish()
                            }
                        }else{
                            MensajesDialog.showMessage(this@Devolucion_proveedor, "${message}");
                        }

                    }, onError = { error ->
                        MensajesDialog.showMessage(this@Devolucion_proveedor, "Error: ${error}")
                    }
                )
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this@Devolucion_proveedor, "Ocurrió un error: ${e.message}");
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_embarque::class.java));
        //LogsEntradaSalida.logsPorModulo( this@Devolucion_proveedor, lifecycleScope, "EMBARQUE/DEVOLUCION/PROVEEDOR", "SALIDA")
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