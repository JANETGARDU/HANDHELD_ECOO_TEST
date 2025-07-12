package com.example.slimmx.Empaque.Envio.Packing

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalConfig
import com.example.slimmx.GlobalUser
import com.example.slimmx.ImpresionEtiqueta.EtiquetaPrinter
import com.example.slimmx.ListaCaja
import com.example.slimmx.ListaCajaItems
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.R
import com.example.slimmx.Validaciones.LogsEntradaSalida
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CajasGeneradas : AppCompatActivity() {

    private lateinit var cbSelectCajas: AutoCompleteTextView
    private var filaSeleccionada: TableRow? = null
    private var var_cantidad: Int=0
    private var fhora:String=""
    private var token:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cajas_generadas)
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

        token= GlobalUser.token.toString()
        cbSelectCajas = findViewById(R.id.cbSelectCajas)

        val idSeleccionado = intent.getStringExtra("idSeleccionado")

        cbSelectCajas.requestFocus()
        // Encontrar el campo txtFolio
        val txtFolio = findViewById<TextView>(R.id.txtFolio)

        // Asignar el valor al EditText txtFolio
        txtFolio.setText(idSeleccionado)

        obtenerDatosCaja()

        val btnImpresion=findViewById<Button>(R.id.buttonImpresionCajas)

        btnImpresion.setOnClickListener {
            val caja=cbSelectCajas.text.toString()
            if(caja.toString()!="0"){
                try {
                    val token=this@CajasGeneradas.token
                    val ip = "192.168.10.22"

                   val codigoZPL = "\"\"\"\n" +
                            "            ^XA\n" +
                            "            ^CI28\n" +
                            "            ^LH0,0\n" +
                            "            ^FX Grafico de Logo.\n" +
                            "            ^FO455,30^GFA,1300,1300,13,,::::::::::::::R0F8,R0FC,Q018C,:Q03FE,Q07FF,Q04018,:,R07,P07JF,O03FI07E,N01FK07C,N0FM0F8,M03CM01C,M07O07,L01CO01C,L03Q0E,L06Q03,K01CQ018,K038R08,,::::::::N07EK07E,0078I01E78I01E78J0F,00C1I03818I0381CI0C3,0181I0300CI0300EI0C18,0181I06006I06006I0418,0783I06006I06006I041E,0F83I04006I06002I061F,0983I04006I06002I06198,:0983I06006I06006I06198,0983I0600CI03006I06198,0983I0301CI0380CI06198,0D83I01C38I01C38I06198,0F83J0FFK0FFJ041F,0383J018K018J041C,0181W0418,00818V0C18,00FD8V0DF,00788V09E,J0CU018,J0CJ03KFCJ01,J06J038I01CJ03,J06J01K08J02,J03J018I018J06,J01J018I018J0C,J018J0CI03K0C,K0CJ06I06J018,K06J03I0CJ03,K03J01C038J06,K018J0IFK0C,L0CJ01F8J018,L06Q07,L038P0E,M0EO038,M07O0F,M01EM03C,N078L0F,N01F8J0FC,O03FE03FE,P01IFC,,::::::::::::::^FS\n" +
                            "            ^FX Texto debajo del grafico.\n" +
                            "            ^FB500,2,2\n" +
                            "            ^FO435,130^A0N,20,20^FDSLIM-COMPANY^FS\n" +
                            "            ^FX Cliente del pedido.\n" +
                            "            ^FB500,2,2\n" +
                            "            ^FO10,60^A0N,25,25^FDCLIENTE: MERCADO LIBRE^FS\n" +
                            "            ^FX Numero de pedido.\n" +
                            "            ^FB500,2,2\n" +
                            "            ^FO10,100^A0N,35,35^FDPEDIDO $idSeleccionado-$caja^FS\n" +
                            "            ^FX Linea horizontal.\n" +
                            "            ^FO10,180^GB700,3,3^FS\n" +
                            "            ^FX Codigo de barras.\n" +
                            "            ^FO30,200^BY2\n" +
                            "            ^BCN,90,Y,N,N\n" +
                            "            ^FD$idSeleccionado-$caja^FS\n" +
                            "            ^FX Informacion.\n" +
                            "            ^FB280,2,2\n" +
                            "            ^FO10,330^A0N,25,25^FDINFORMACION^FS\n" +
                            "            ^FB240,2,2\n" +
                            "            ^FO10,355^A0N,25,25^FDContenido: ${this@CajasGeneradas.var_cantidad} Unidad(es)^FS\n" +
                            "            ^FB280,2,2\n" +
                            "            ^FO400,340^A0N,25,25^FD${this@CajasGeneradas.fhora}^FS\n" +
                            "            ^PQ1,0,1,Y^XZ\n" +
                            "        \"\"\""

                    val etiquetaPrinter = EtiquetaPrinter()
                    lifecycleScope.launch {
                        val exito = etiquetaPrinter.imprimirEtiqueta(
                            this@CajasGeneradas,
                            ip,
                            codigoZPL
                        );
                        if (exito) {
                            android.app.AlertDialog.Builder(this@CajasGeneradas)
                                .setMessage("Impresión completada correctamente")
                                .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                                .setCancelable(false)
                                .create()
                                .show()
                        } else {
                            android.app.AlertDialog.Builder(this@CajasGeneradas)
                                .setMessage("Hubo un error al imprimir.")
                                .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                                .setCancelable(false)
                                .create()
                                .show()
                        }
                    }

                }catch (e: Exception){
                    MensajesDialog.showMessage(this@CajasGeneradas, "Ocurrió un error: ${e.message}");
                }

            }else{
                MensajesDialog.showMessage(this, "No se puede imprimir la caja Número 0")
            }
        }
    }

   private fun obtenerDatosCaja() {
       val folio = findViewById<TextView>(R.id.txtFolio)
       val token = GlobalUser.token.toString()
       try {
           val params = mapOf(
               "id" to folio.text.toString(),
               "token" to token
           )

           val headers = mapOf("Token" to GlobalUser.token.toString());

           lifecycleScope.launch(Dispatchers.IO) {
               try {
                   Pedir_datos_apis(
                       endpoint = "/inventario/empaque/cajas",
                       params = params,
                       dataClass = ListaCaja::class,
                       listaKey = "result",
                       headers = headers,
                       onSuccess = { list ->
                           lifecycleScope.launch(Dispatchers.Main) {
                               // Filtrar elementos que no tengan valor 0
                               val opciones = list.mapNotNull { it.CAJA }

                               if (opciones.isNotEmpty()) {
                                   actualizarAutoCompleteTextView(opciones)
                               } else {
                                   MensajesDialog.showMessage(this@CajasGeneradas, "No hay cajas válidas")
                                   finish()
                               }
                           }
                       },
                       onError = { error ->
                           lifecycleScope.launch(Dispatchers.Main) {
                               MensajesDialog.showMessage(this@CajasGeneradas, "Error: $error")
                           }
                       }
                   )
               }catch (e: Exception) {
                   lifecycleScope.launch(Dispatchers.Main) {
                       MensajesDialog.showMessage(this@CajasGeneradas, "Ocurrió un error: ${e.message}")
                   }
               }
           }

       }catch (e: Exception){
           lifecycleScope.launch(Dispatchers.Main) {
               MensajesDialog.showMessage(this@CajasGeneradas, "Ocurrió un error: ${e.message}");
           }
       }

   }


    private fun actualizarAutoCompleteTextView(opciones: List<Int>) {
        try {
            val adaptador = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                opciones // Solo pasamos los valores de CAJA
            )
            cbSelectCajas.setAdapter(adaptador)

            // Cuando se selecciona un ítem
            cbSelectCajas.setOnItemClickListener { _, _, position, _ ->
                val seleccion = opciones[position]
                //mostrarMensaje("Seleccionaste: $seleccion")
                val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

                if (tableLayout.childCount > 1) {
                    tableLayout.removeViews(1, tableLayout.childCount - 1)
                }
                this@CajasGeneradas.var_cantidad=0
                obtenerItemsCajasPorId(seleccion.toString())
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun obtenerItemsCajasPorId(idSeleccionado: String) {
        val folio=findViewById<TextView>(R.id.txtFolio)
        if (idSeleccionado.isNotEmpty()){
            try {
                val params= mapOf(
                    "id" to folio.text.toString(),
                    "caja" to idSeleccionado,
                    "token" to this@CajasGeneradas.token.toString()
                );
                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/inventario/empaque/caja/items",
                            params = params,
                            dataClass = ListaCajaItems::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val cajas_items = lista.map {
                                            ListaCajaItems(
                                                ID = it.ID,
                                                EMPAQUE = it.EMPAQUE,
                                                CODIGO = it.CODIGO,
                                                DESCRIPCION = it.DESCRIPCION,
                                                CANTIDAD = it.CANTIDAD,
                                                FECHA_HORA = it.FECHA_HORA,
                                                CAJA = it.CAJA
                                            )
                                        }
                                        actualizarTableLayout(cajas_items)

                                    } else {
                                        MensajesDialog.showMessage(
                                            this@CajasGeneradas,
                                            "Carga de datos fallida"
                                        )
                                        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

                                        if (tableLayout.childCount > 1) {
                                            tableLayout.removeViews(1, tableLayout.childCount - 1)
                                        }
                                    }
                                }
                            },
                            onError = { error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    // Si hubo un error en la petición, mostramos un Toast
                                    MensajesDialog.showMessage(
                                        this@CajasGeneradas,
                                        "Error: $error"
                                    );
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@CajasGeneradas, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(
                        this@CajasGeneradas,
                        "Ocurrió un error: ${e.message}"
                    );
                }
            }


        }else{
            MensajesDialog.showMessage(this, "No se selecciono ninguna caja")
        }

    }

    private fun actualizarTableLayout(items: List<ListaCajaItems>) {
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

            if (tableLayout.childCount > 1) {
                tableLayout.removeViews(1, tableLayout.childCount - 1)
            }

            var primeraFila: TableRow? = null

            for ((index, item) in items.withIndex()) {
                val tableRow = TableRow(this)
                // Agrega los TextViews con los datos
                agregarFila(tableRow, item)

                tableRow.setOnClickListener {
                    // Restablece el color de la fila anteriormente seleccionada
                    filaSeleccionada?.setBackgroundColor(Color.TRANSPARENT)

                    // Cambia el color de la nueva fila seleccionada
                    tableRow.setBackgroundColor(Color.parseColor("#639A67"))

                    // Actualiza la fila seleccionada
                    filaSeleccionada = tableRow

                    // Opcional: Puedes actualizar algún contenido con los datos de la fila seleccionada
                    actualizarInformacionSeleccionada(item)
                }

                tableLayout.addView(tableRow)


                if (index == 0) {
                    primeraFila = tableRow
                }
            }

            // Selecciona automáticamente la primera fila después de agregar todas
            primeraFila?.let { fila ->
                fila.setBackgroundColor(Color.parseColor("#639A67"))
                filaSeleccionada = fila
                actualizarInformacionSeleccionada(items.first())
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun agregarFila(tableRow: TableRow, item: ListaCajaItems) {
        try {
            // Crear TextViews y añadirlos a la fila
            val codigoTextView = TextView(this).apply {
                text = item.CODIGO
                gravity = Gravity.CENTER // Centrar el texto
            }
            val descripcionTextView = TextView(this).apply {
                text = item.DESCRIPCION
            }
            val packingTextView = TextView(this).apply {
                text = item.CANTIDAD.toString()
                gravity = Gravity.CENTER // Centrar el texto
            }
            val cantidadTextView = TextView(this).apply {
                text = item.CANTIDAD.toString()
                gravity = Gravity.CENTER // Centrar el texto
                visibility= View.GONE
            }

            var lbUnidades = findViewById<TextView>(R.id.txtCantidadItemsCajas)
            this@CajasGeneradas.var_cantidad+=item.CANTIDAD.toInt()
            lbUnidades.text = "Unidades del Envio: ${this@CajasGeneradas.var_cantidad}"
            // Añadir los TextViews a la TableRow
            tableRow.addView(codigoTextView)
            tableRow.addView(descripcionTextView)
            tableRow.addView(packingTextView)
            tableRow.addView(cantidadTextView)
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }


    }

    private fun actualizarInformacionSeleccionada(item: ListaCajaItems) {
        try {
            val lbDescipcion=findViewById<TextView>(R.id.txtDescripcionE_C)
            lbDescipcion.text="${item.DESCRIPCION}"
            this@CajasGeneradas.fhora="${item.FECHA_HORA}"
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
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