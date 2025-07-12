package com.example.slimmx.Almacen2.DevolucionProveedor.EntregaProveedor

import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
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
import com.example.slimmx.GlobalUser
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.R
import com.example.slimmx.listaDevolucionItems
import com.example.slimmx.listaDevolucionProveedor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Confirmacion_EntregaProveedor : AppCompatActivity() {

    private var id_devolucion:Int=0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_confirmacion_entrega_proveedor)
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

        val txtBuscarFolio=findViewById<EditText>(R.id.txtFolio);
        val btn_confirmar=findViewById<Button>(R.id.buttonConfirmar);
        var txtFolioDevolucion=findViewById<TextView>(R.id.txtFolioDevolucion);

        txtBuscarFolio.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtBuscarFolio.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        buscarguia(txtBuscarFolio.text.toString());
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
            AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Estás seguro de que deseas continuar?")
                .setPositiveButton("Sí") { _, _ ->
                    try {
                        confirmacion(this@Confirmacion_EntregaProveedor.id_devolucion,txtFolioDevolucion.text.toString())
                    }catch (e: Exception){
                        MensajesDialog.showMessage(this@Confirmacion_EntregaProveedor, "Ocurrió un error: ${e.message}");
                    }

                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

    }

    private fun buscarguia(id: String){
        var txtFolioCompra=findViewById<TextView>(R.id.txtFolioCompra);
        var txtFolioDevolucion=findViewById<TextView>(R.id.txtFolioDevolucion);
        var txtProveedor=findViewById<TextView>(R.id.txtProveedor);

        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }

        if (id.isNotEmpty()){
            try {
                val params= mapOf(
                    "folio" to id
                )
                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch(Dispatchers.IO) {
                    try{
                        Pedir_datos_apis(
                            endpoint = "/devolucion/proveedor/detalles/por/folio",
                            params=params,
                            dataClass = listaDevolucionProveedor::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val datos = lista[0]
                                        txtFolioCompra.setText(datos.FOLIO_COMPRA);
                                        txtFolioDevolucion.setText(datos.FOLIO_DEVOLUCION ?: " ");
                                        txtProveedor.setText(datos.PROVEEDOR);
                                        this@Confirmacion_EntregaProveedor.id_devolucion = datos.ID

                                        val items = datos.ITEMS
                                        actualizarTableLayout(items)
                                    }
                                }
                            },
                            onError = { error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Confirmacion_EntregaProveedor, "Error: $error")
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Confirmacion_EntregaProveedor, "Ocurrió un error: ${e.message}")
                        }
                    }
                }


            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(
                        this@Confirmacion_EntregaProveedor,
                        "Ocurrió un error: ${e.message}"
                    );
                }
            }
        }
    }

    private fun actualizarTableLayout(items: List<listaDevolucionItems>) { //Hacer la selecion por fila
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

            if (tableLayout.childCount > 1) {
                tableLayout.removeViews(1, tableLayout.childCount - 1)
            }

            for (item in items) {
                val tableRow = TableRow(this)
                agregarFila(tableRow, item)


                tableLayout.addView(tableRow)
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun agregarFila(tableRow: TableRow, item: listaDevolucionItems) { //Agregar los datos encontrados en la tabla
        try {
            val cantidadTextView = TextView(this).apply {
                text = item.CANTIDAD_DEVUELTA.toString()
                gravity = Gravity.CENTER // Centrar el texto
            }
            val CodigoTextView = TextView(this).apply {
                text = item.CODIGO
                gravity = Gravity.CENTER // Centrar el texto
            }
            val DescripcionTextView = TextView(this).apply {
                text = item.DESCRIPCION

            }
            val itemsText=TextView(this).apply {
                text=item.ITEM.toString()
                visibility= View.GONE
            }

            tableRow.addView(cantidadTextView)
            tableRow.addView(CodigoTextView)
            tableRow.addView(DescripcionTextView)
            tableRow.addView(itemsText)
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun confirmacion(id: Int, folio_devolucion:String){
        var txtFolioCompra=findViewById<TextView>(R.id.txtFolioCompra);
        var txtFolioDevolucion=findViewById<TextView>(R.id.txtFolioDevolucion);
        var txtProveedor=findViewById<TextView>(R.id.txtProveedor);
        var txtFolio=findViewById<EditText>(R.id.txtFolio);

        if (id.toString().isNotEmpty()){
            try {
                var body= mapOf(
                    "ID" to id.toString(),
                    "FOLIO_DEVOLUCION" to folio_devolucion
                )

                val headers = mapOf("Token" to GlobalUser.token.toString());
                lifecycleScope.launch {
                    Pedir_datos_apis_post(
                        endpoint = "/confirmar/entrega/a/proveedor",
                        body = body,
                        listaKey = "message",
                        dataClass = Any::class,
                        headers=headers,
                        onSuccess = { lista ->
                            if (lista.toString().contains("SE CONFIRMO LA ENTREGA AL PROVEEDOR")) {
                                MensajesDialogConfirmaciones.showMessage(this@Confirmacion_EntregaProveedor, "Confirmado correctamente") {
                                    txtFolioCompra.setText("");
                                    txtFolioDevolucion.setText(" ");
                                    txtProveedor.setText(" ");

                                    val items: List<listaDevolucionItems> = listOf(
                                        listaDevolucionItems(
                                            CANTIDAD_DEVUELTA = 0,
                                            CODIGO = "",
                                            DESCRIPCION = "",
                                            ITEM = 0
                                        )
                                    )

                                    actualizarTableLayout(items);
                                    txtFolio.setText("");
                                    this@Confirmacion_EntregaProveedor.id_devolucion=0;
                                }

                            }
                        },
                        onError = { error ->
                            val mensajeError = error.toString().substringBefore("\n").replace("ERROR:","");
                            MensajesDialog.showMessage(this@Confirmacion_EntregaProveedor, " $mensajeError");
                        }

                    )
                }

            }catch (e: Exception){
                MensajesDialog.showMessage(this@Confirmacion_EntregaProveedor, "Ocurrió un error: ${e.message}");
            }

        }
    }



}