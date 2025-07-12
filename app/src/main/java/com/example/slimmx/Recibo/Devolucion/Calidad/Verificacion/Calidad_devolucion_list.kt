package com.example.slimmx.Recibo.Devolucion.Calidad.Verificacion

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListaItemsCalidadDevolucion
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.R
import com.example.slimmx.Recibo.Abastecimiento.ControlCalidad.Revision.ControlCalidad_Confirmacion
import com.example.slimmx.Recibo.Abastecimiento.ControlCalidad.Submenu_control_calidad
import com.example.slimmx.Recibo.Abastecimiento.Seleccion_recibo_abastecimiento
import com.example.slimmx.Recibo.Devolucion.Calidad.Submenu_alta_calidad_devolucion
import com.example.slimmx.Validaciones.LogsEntradaSalida
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Calidad_devolucion_list : AppCompatActivity() {

    private lateinit var cbSelectCalidad_list: AutoCompleteTextView;
    private var filaSeleccionada: TableRow? = null

    private var codigo:String="";
    private var descripcion:String="";
    private var id:Int=0;
    private var item_confirm:String="";
    private var cantidad:String="";
    private var observacion:String="";
    private var referencia:String="";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calidad_devolucion_list)
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
        cbSelectCalidad_list = findViewById(R.id.cbSelectCalidad_list);
        val txtCodigo=findViewById<EditText>(R.id.editTextCodigo_Calidad_list);
        val tableLayout=findViewById<TableLayout>(R.id.tableLayout);
        val btn_eliminar_codigo=findViewById<Button>(R.id.buttonEliminar_codigo);
        val btn_ok_calidad=findViewById<Button>(R.id.buttonOK_Calidad);


        val opciones = intent.getStringArrayExtra("folios")

        if (opciones != null && opciones.isNotEmpty()) {
            actualizarcbBox(opciones.toList()) // Llenar el ComboBox con los datos recibidos
        } else {
            val intent = Intent(this, Submenu_alta_calidad_devolucion::class.java)
            intent.putExtra("MESSAGE", "No hay tareas para este usuario")
            startActivity(intent)
            finish()
        }

        txtCodigo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                try {
                    if (!s.isNullOrEmpty()) {
                        val codigoIngresado = s.toString().trim()
                        val encontrado = buscarCodigo(codigoIngresado, tableLayout)
                        if (!encontrado) {
                            MensajesDialog.showMessage(this@Calidad_devolucion_list, "No se encontro ese Código")
                        }

                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this@Calidad_devolucion_list, "Ocurrió un error: ${e.message}");
                }

            }
        });

        btn_eliminar_codigo.setOnClickListener {
            txtCodigo.setText("");
            txtCodigo.post { txtCodigo.requestFocus() }
        }

        btn_ok_calidad.setOnClickListener {
            try {
                val intent = Intent(this, Calidad_devolucion_confirmacion::class.java)

                // Envía los valores correctos
                intent.putExtra("codigo", this@Calidad_devolucion_list.codigo.toString())
                intent.putExtra("descripcion", this@Calidad_devolucion_list.descripcion.toString())
                intent.putExtra("folio", cbSelectCalidad_list.text.toString())
                intent.putExtra("item_confirm", this@Calidad_devolucion_list.item_confirm.toString())
                intent.putExtra("cantidad", this@Calidad_devolucion_list.cantidad.toString())
                intent.putExtra("id", this@Calidad_devolucion_list.id.toString())
                intent.putExtra("posible_defecto", this@Calidad_devolucion_list.observacion)
                intent.putExtra("referencia",this@Calidad_devolucion_list.referencia);

                startActivity(intent)

                this@Calidad_devolucion_list.codigo = "";
                this@Calidad_devolucion_list.descripcion = "";
                this@Calidad_devolucion_list.item_confirm = "";
                this@Calidad_devolucion_list.cantidad = "";
                this@Calidad_devolucion_list.id=0;
                this@Calidad_devolucion_list.observacion="";
                this@Calidad_devolucion_list.referencia="";
            }catch (e: Exception){
                MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
            }
        }

    }

    override fun onResume() {//Cuando regresa a primer plano el activity
        super.onResume()

        if (!cbSelectCalidad_list.text.isNullOrEmpty()) {
            cbSelectCalidad_list.requestFocus();
            optenerDatositems(cbSelectCalidad_list.text.toString());
        }
    }

    private fun actualizarcbBox(opciones: List<String>) {
        try {
            val texViewCodigo=findViewById<EditText>(R.id.editTextCodigo_Calidad_list)
            val adaptador = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                opciones
            )
            cbSelectCalidad_list.setAdapter(adaptador)

            cbSelectCalidad_list.setOnItemClickListener { _, _, position, _ ->
                val seleccion = opciones[position]

                val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

                if (tableLayout.childCount > 1) {
                    tableLayout.removeViews(1, tableLayout.childCount - 1)
                }

                optenerDatositems(seleccion);

                texViewCodigo.post { texViewCodigo.requestFocus() }
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun optenerDatositems(seleccion: String){
        if(seleccion.isNotEmpty()){
            try {
                val params= mapOf(
                    "folio" to seleccion
                );

                val headers = mapOf(
                    "Token" to GlobalUser.token.toString()
                );

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/devoluciones/control/calidad/items",
                            params=params,
                            dataClass = ListaItemsCalidadDevolucion::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val items = lista.map {
                                            ListaItemsCalidadDevolucion(
                                                CODIGO = it.CODIGO,
                                                DESCRIPCION = it.DESCRIPCION,
                                                CANTIDAD_RECIBIDA = it.CANTIDAD_RECIBIDA,
                                                POR_CONFIRMAR = it.POR_CONFIRMAR,
                                                ITEM = it.ITEM,
                                                ID = it.ID,
                                                OBSERVACION = it.OBSERVACION,
                                                REFERENCIA = it.REFERENCIA
                                            )

                                        }
                                        actualizarTableLayout(items);
                                    } else {
                                        val tableLayout =
                                            findViewById<TableLayout>(R.id.tableLayout)
                                        if (tableLayout.childCount > 1) {
                                            tableLayout.removeViews(1, tableLayout.childCount - 1)
                                        }
                                        val intent = Intent(this@Calidad_devolucion_list, Submenu_alta_calidad_devolucion::class.java);
                                        intent.putExtra("MESSAGE", "Tarea concluida");
                                        startActivity(intent);
                                        finish();
                                    }

                                }
                            },
                            onError = {error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Calidad_devolucion_list, "Errores: $error");
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Calidad_devolucion_list, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Calidad_devolucion_list, "Ocurrió un error: ${e.message}");
                }
            }
        }else{
            MensajesDialog.showMessage(this,"Se debe de seleccionar un folio");
        }

    }

    private fun actualizarTableLayout(items: List<ListaItemsCalidadDevolucion>) {
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

            // Elimina todas las filas excepto la primera
            if (tableLayout.childCount > 1) {
                tableLayout.removeViews(1, tableLayout.childCount - 1)
            }

            // Agrega las nuevas filas a la tabla
            for (item in items) {
                val tableRow = TableRow(this)
                agregarFila(tableRow, item)  // Agrega celdas a la fila según el elemento

                // Agrega un listener para cambiar el color al seleccionar una fila
                tableRow.setOnClickListener {
                    filaSeleccionada?.let {
                        it.setBackgroundColor(Color.TRANSPARENT)  // Resalta solo la fila seleccionada
                    }

                    tableRow.setBackgroundColor(Color.parseColor("#639A67"))  // Color de la fila seleccionada
                    filaSeleccionada = tableRow
                    actualizarFilaSeleccionada(item)  // Actualiza la información de la fila seleccionada
                }

                // Añade la fila a la tabla
                tableLayout.addView(tableRow)
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun agregarFila(tableRow: TableRow, item: ListaItemsCalidadDevolucion) {
        try {
            val codigoTextView = TextView(this).apply {
                text = item.CODIGO
                gravity = Gravity.CENTER
            }
            val descripcionTextView = TextView(this).apply {
                text = item.DESCRIPCION
            }

            val RecibidaTextView = TextView(this).apply {
                text = item.CANTIDAD_RECIBIDA.toString();
                gravity = Gravity.CENTER
            }

            val Por_recibirTextView = TextView(this).apply {
                text = item.POR_CONFIRMAR.toString();
                gravity = Gravity.CENTER
            }

            val ItemTextView = TextView(this).apply {
                text = item.ITEM.toString();
                gravity = Gravity.CENTER
                visibility = View.GONE
            }

            val IdTextView = TextView(this).apply {
                text = item.ID.toString();
                gravity = Gravity.CENTER
                visibility = View.GONE
            }

            val observacionesText=TextView(this).apply {
                text=item.OBSERVACION
                visibility=View.GONE
            }

            val referenciaText=TextView(this).apply {
                text=item.REFERENCIA
                visibility=View.GONE
            }

            tableRow.addView(codigoTextView);
            tableRow.addView(descripcionTextView);
            tableRow.addView(RecibidaTextView);
            tableRow.addView(Por_recibirTextView);
            tableRow.addView(ItemTextView);
            tableRow.addView(IdTextView);
            tableRow.addView(observacionesText);
            tableRow.addView(referenciaText);

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun actualizarFilaSeleccionada(item: ListaItemsCalidadDevolucion) {
        try {
            val editDescripcion = findViewById<TextView>(R.id.txtDescripcion)
            editDescripcion.text = item.DESCRIPCION

            this@Calidad_devolucion_list.codigo=item.CODIGO;
            this@Calidad_devolucion_list.descripcion=item.DESCRIPCION;
            this@Calidad_devolucion_list.cantidad=item.CANTIDAD_RECIBIDA.toString();
            this@Calidad_devolucion_list.item_confirm=item.ITEM.toString();
            this@Calidad_devolucion_list.id=item.ID;
            this@Calidad_devolucion_list.observacion=item.OBSERVACION;
            this@Calidad_devolucion_list.referencia=item.REFERENCIA;

            MensajesDialog.showMessage(this,"PRODUCTO:  ${item.CODIGO}, ${item.DESCRIPCION}")
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    fun buscarCodigo(codigo: String, tableLayout: TableLayout): Boolean {
        try {
            for (i in 1 until tableLayout.childCount) {
                val fila = tableLayout.getChildAt(i) as? TableRow ?: continue

                val columna1 = fila.getChildAt(0) as? TextView ?: continue
                fila.setBackgroundColor(Color.TRANSPARENT);
                if (columna1.text.equals(codigo) ) {
                    println("Encontrado");

                    fila.setBackgroundColor(Color.parseColor("#639A67"))

                    val codigo=(fila.getChildAt(0) as? TextView)?.text.toString();
                    val descripcion=(fila.getChildAt(1) as? TextView)?.text.toString();
                    val cantidad_recibida=(fila.getChildAt(2) as? TextView)?.text.toString();
                    val por_recibir=(fila.getChildAt(3)as? TextView)?.text.toString();
                    val no_item=(fila.getChildAt(4) as? TextView)?.text.toString();
                    val id=(fila.getChildAt(5)as? TextView)?.text.toString();
                    val observacion=(fila.getChildAt(6)as? TextView)?.text.toString();
                    val referencia=(fila.getChildAt(7)as? TextView)?.text.toString();

                    val item = ListaItemsCalidadDevolucion(codigo,descripcion,cantidad_recibida.toInt(),por_recibir.toInt(), no_item.toInt(), id.toInt(),observacion.toString(), referencia);

                    actualizarFilaSeleccionada(item)

                    return true // Código encontrado
                }
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
        return false
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_alta_calidad_devolucion::class.java));
        //LogsEntradaSalida.logsPorModulo( this, lifecycleScope, "DEVOLUCION/CALIDAD/RECIBO", "SALIDA");
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