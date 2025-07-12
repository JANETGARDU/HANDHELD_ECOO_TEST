package com.example.slimmx.Recibo.Devolucion.Recibo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListReciboAbastecimientoItems
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.R
import com.example.slimmx.Recibo.Devolucion.Seleccion_devolucion
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.example.slimmx.Vista.FragmentPage3
import com.example.slimmx.Vista.FragmentPageEtiquetaBluetooth
import com.example.slimmx.Vista.FragmentPageImpresionEtiquetas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Recibo_devolucion : AppCompatActivity(), FragmentPageImpresionEtiquetas.OnBackgroundBlockerListener, FragmentPage3.OnBackgroundBlockerListener, FragmentPageEtiquetaBluetooth.OnBackgroundBlockerListener {

    private lateinit var cbSelect: AutoCompleteTextView;

    private var filaSeleccionada: TableRow? = null

    private var codigo: String = "";
    private var descripcion: String = "";
    private var id: Int = 0;
    private var item_confirm: String = "";
    private var cantidad: String = "";
    private var referencia: String = "";
    private var calidad: String = "";
    private var razon_calidad: String = "";

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
        setContentView(R.layout.activity_recibo_devolucion)
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
        val opciones = intent.getStringArrayExtra("folios")

        if (opciones != null && opciones.isNotEmpty()) {
            actualizarcbBox(opciones.toList()) // Llenar el ComboBox con los datos recibidos
        } else {
            // Si no hay datos, regresar al Submenu_acomodo
            val intent = Intent(this, Seleccion_devolucion::class.java)
            intent.putExtra("MESSAGE", "No hay tareas para este usuario")
            startActivity(intent)
            finish()
        }

        val texViewCodigo = findViewById<EditText>(R.id.editTextCodigo);
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout);
        val btn_eliminarCodigo = findViewById<Button>(R.id.buttonEliminar_codigo);
        val btn_confirmar = findViewById<Button>(R.id.buttonOK)


        texViewCodigo.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        val buttonMenu = findViewById<Button>(R.id.buttonMenu)
        buttonMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, buttonMenu)
            popupMenu.menuInflater.inflate(R.menu.menu_opciones, popupMenu.menu)
            val menuItemVerImagen = popupMenu.menu.findItem(R.id.item_ver_imagen)
            menuItemVerImagen.isVisible = false
            val menuItemCombosPack = popupMenu.menu.findItem(R.id.item_packs_combos);
            menuItemCombosPack.isVisible = false;
            val menuImpresionBluetooth=popupMenu.menu.findItem(R.id.item_etiquetas_bluetooth);
            menuImpresionBluetooth.isVisible=false;
            val menuRecomendacion=popupMenu.menu.findItem(R.id.item_recomendaciones);
            menuRecomendacion.isVisible=false;
            val menuVerificacion=popupMenu.menu.findItem(R.id.item_verificacion);
            menuVerificacion.isVisible=false;
            val menuInventory=popupMenu.menu.findItem(R.id.item_inventario);
            menuInventory.isVisible=false;
            popupMenu.setOnMenuItemClickListener { item ->
                try {
                    when (item.itemId) {
                        R.id.item_impresora -> {
                            val codigo = this@Recibo_devolucion.codigo.toString()
                            val descripcion = this@Recibo_devolucion.descripcion.toString()
                            val folio = cbSelect.text.toString()
                            Log.d(
                                "IMPRESION",
                                "CODIGO: ${codigo}, DESCRIPCION: ${descripcion}, FOLIO: ${folio}"
                            )

                            if (this@Recibo_devolucion.calidad=="false"){
                                if (codigo.isNotEmpty() && descripcion.isNotEmpty() && folio.isNotEmpty()) {

                                    val fragmentImpresion = FragmentPageImpresionEtiquetas.newInstance(
                                        codigo,
                                        descripcion,
                                        2,
                                        folio,
                                        ""
                                    )
                                    // MensajesDialog.showMessage(this, "$codigo, $descripcion, 2, $folio")

                                    val fragmentContainerView =
                                        findViewById<View>(R.id.fragmentContainerView_impresion)
                                    val backgroundBlocker = findViewById<View>(R.id.background_blocker)

                                    val transaction = supportFragmentManager.beginTransaction()
                                    val fragment =
                                        supportFragmentManager.findFragmentById(R.id.fragmentContainerView_impresion)

                                    if (fragment != null) {
                                        if (fragment.isVisible) {
                                            transaction.hide(fragment)
                                            backgroundBlocker.visibility = View.GONE
                                            backgroundBlocker.setOnTouchListener(null)
                                        } else {
                                            transaction.show(fragment)
                                            backgroundBlocker.bringToFront()
                                            fragmentContainerView.bringToFront()
                                            backgroundBlocker.visibility = View.VISIBLE
                                            backgroundBlocker.setOnTouchListener { _, _ -> true }
                                        }
                                    } else {
                                        transaction.add(
                                            R.id.fragmentContainerView_impresion,
                                            fragmentImpresion
                                        );
                                        backgroundBlocker.bringToFront();
                                        fragmentContainerView.bringToFront();
                                        backgroundBlocker.visibility = View.VISIBLE;
                                        backgroundBlocker.setOnTouchListener { _, _ -> true }
                                    }
                                    transaction.commitNow()

                                } else {
                                    MensajesDialog.showMessage(
                                        this,
                                        "Debes de seleccionar algún producto"
                                    )
                                }
                            }else{
                                MensajesDialog.showMessage(this, "NO SE PUEDEN IMPRIMIR ETIQUETAS SI EL PRODUCTO VA A CALIDAD");
                            }


                            true
                        }

                        R.id.item_f1 -> {
                            if (this@Recibo_devolucion.codigo.isNotEmpty()) {
                                val fragmentPage3 =
                                    FragmentPage3.newInstance(this@Recibo_devolucion.codigo)

                                val fragmentContainerView =
                                    findViewById<View>(R.id.fragmentContainerView_f1)
                                val backgroundBlocker = findViewById<View>(R.id.background_blocker)

                                val transaction = supportFragmentManager.beginTransaction()
                                val fragment =
                                    supportFragmentManager.findFragmentById(R.id.fragmentContainerView_f1)

                                if (fragment != null) {
                                    if (fragment.isVisible) {
                                        transaction.hide(fragment)
                                        backgroundBlocker.visibility = View.GONE
                                        backgroundBlocker.setOnTouchListener(null)
                                    } else {
                                        transaction.show(fragment)
                                        backgroundBlocker.bringToFront()
                                        fragmentContainerView.bringToFront()
                                        backgroundBlocker.visibility = View.VISIBLE
                                        backgroundBlocker.setOnTouchListener { _, _ -> true }
                                    }
                                } else {
                                    transaction.add(
                                        R.id.fragmentContainerView_f1,
                                        fragmentPage3
                                    );
                                    backgroundBlocker.bringToFront();
                                    fragmentContainerView.bringToFront();
                                    backgroundBlocker.visibility = View.VISIBLE;
                                    backgroundBlocker.setOnTouchListener { _, _ -> true }
                                }

                                transaction.commitNow()
                            } else {
                                MensajesDialog.showMessage(
                                    this,
                                    "Se debe de seleccionar un producto"
                                )
                            }

                            true
                        }

                        R.id.item_ver_imagen -> {
                            false
                        }

                        R.id.item_etiquetas_bluetooth -> {
                            if (this@Recibo_devolucion.codigo.isNullOrBlank() || this@Recibo_devolucion.descripcion.isNullOrBlank()) {
                                MensajesDialog.showMessage(
                                    this,
                                    "Se debe de seleccionar algún código"
                                );
                                return@setOnMenuItemClickListener true
                            }

                            if (this@Recibo_devolucion.calidad=="true") {
                                MensajesDialog.showMessage(
                                    this,
                                    "NO SE PUEDEN IMPRIMIR ETIQUETAS SI EL PRODUCTO VA A CALIDAD"
                                );
                                return@setOnMenuItemClickListener true
                            }

                            val fragmentEtiquetaBluetooth = FragmentPageEtiquetaBluetooth().apply {
                                arguments = Bundle().apply {
                                    putString(
                                        FragmentPageEtiquetaBluetooth.ARG_CODIGO,
                                        this@Recibo_devolucion.codigo
                                    )
                                    putString(
                                        FragmentPageEtiquetaBluetooth.ARG_DESCRIPCION,
                                        this@Recibo_devolucion.descripcion
                                    )
                                    putInt(FragmentPageEtiquetaBluetooth.ARG_TIPO, 2)
                                    putString(
                                        FragmentPageEtiquetaBluetooth.ARG_FOLIO,
                                        cbSelect.text.toString()
                                    )
                                }
                            }

                            val fragmentContainerView =
                                findViewById<View>(R.id.fragmentContainerView_impresion_bluetooth)
                            val backgroundBlocker = findViewById<View>(R.id.background_blocker)

                            val transaction = supportFragmentManager.beginTransaction()
                            val fragmentActual =
                                supportFragmentManager.findFragmentById(R.id.fragmentContainerView_impresion_bluetooth)

                            if (fragmentActual != null && fragmentActual is FragmentPageEtiquetaBluetooth) {
                                if (fragmentActual.isVisible) {
                                    transaction.hide(fragmentActual)
                                    backgroundBlocker.visibility = View.GONE
                                    backgroundBlocker.setOnTouchListener(null)
                                } else {
                                    transaction.show(fragmentActual)
                                    backgroundBlocker.bringToFront()
                                    fragmentContainerView.bringToFront()
                                    backgroundBlocker.visibility = View.VISIBLE
                                    backgroundBlocker.setOnTouchListener { _, _ -> true }
                                }
                            } else {
                                transaction.add(
                                    R.id.fragmentContainerView_impresion_bluetooth,
                                    fragmentEtiquetaBluetooth
                                )
                                backgroundBlocker.bringToFront()
                                fragmentContainerView.bringToFront()
                                backgroundBlocker.visibility = View.VISIBLE
                                backgroundBlocker.setOnTouchListener { _, _ -> true }
                            }

                            transaction.commitNow()
                            true
                        }

                        else -> false
                    }
                } catch (e: Exception) {
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}")
                    false
                }
            }
            popupMenu.show()
        }

        texViewCodigo.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = texViewCodigo.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        val codigoIngresado = inputText.toString().trim()
                        val encontrado = buscarCodigo(codigoIngresado, tableLayout)
                        if (!encontrado) {
                            MensajesDialog.showMessage(
                                this@Recibo_devolucion,
                                "No se encontro ese Código"
                            )
                        }
                    }
                } catch (e: Exception) {
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }

        btn_eliminarCodigo.setOnClickListener {
            texViewCodigo.setText("");
            texViewCodigo.post { texViewCodigo.requestFocus() }
        }

        btn_confirmar.setOnClickListener {
            try {
                val intent = Intent(this, Recibo_devolucion_confirmar::class.java)

                // Envía los valores correctos
                intent.putExtra("codigo", this@Recibo_devolucion.codigo)
                intent.putExtra("descripcion", this@Recibo_devolucion.descripcion)
                intent.putExtra("id_confir", this@Recibo_devolucion.id.toString())
                intent.putExtra("item_confirm", this@Recibo_devolucion.item_confirm)
                intent.putExtra("cantidad", this@Recibo_devolucion.cantidad)
                intent.putExtra("referencia", this@Recibo_devolucion.referencia)
                intent.putExtra("calidad", this@Recibo_devolucion.calidad)
                intent.putExtra("razon_calidad", this@Recibo_devolucion.razon_calidad)
                /*MensajesDialog.showMessage(this, "codigo "+this@Recibo_list.codigo + "descripcion "+ this@Recibo_list.descripcion+"id_confir "+ this@Recibo_list.id.toString()+ "item_confirm "+ this@Recibo_list.item_confirm+
                        "cantidad"+ this@Recibo_list.cantidad + "referencia "+ this@Recibo_list.referencia + "calidad "+ this@Recibo_list.calidad+ "razon_calidad"+ this@Recibo_list.razon_calidad);
                */
                startActivity(intent)


                this@Recibo_devolucion.codigo = "";
                this@Recibo_devolucion.descripcion = "";
                this@Recibo_devolucion.item_confirm = "";
                this@Recibo_devolucion.cantidad = "";
                this@Recibo_devolucion.referencia = "";
                this@Recibo_devolucion.razon_calidad = "";
                this@Recibo_devolucion.calidad = "";
                btn_eliminarCodigo.performClick();
            } catch (e: Exception) {
                MensajesDialog.showMessage(this@Recibo_devolucion, "Ocurrió un error: ${e.message}");
            }

        }

    }


    override fun onResume() {//Cuando regresa a primer plano el activity
        super.onResume()
        val tableLayout =
            findViewById<TableLayout>(R.id.tableLayout)
        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }

        if (!cbSelect.text.isNullOrEmpty()) {
            cbSelect.requestFocus()
            optenerDatositems(cbSelect.text.toString());
        }

    }


    private fun actualizarcbBox(opciones: List<String>) {
        try {
            val texViewCodigo=findViewById<EditText>(R.id.editTextCodigo)
            val adaptador = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                opciones
            )
            cbSelect.setAdapter(adaptador)

            cbSelect.setOnItemClickListener { _, _, position, _ ->
                val seleccion = opciones[position]
                val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

                if (tableLayout.childCount > 1) {
                    tableLayout.removeViews(1, tableLayout.childCount - 1)
                }
                    optenerDatositems(seleccion);

                texViewCodigo.post { texViewCodigo.requestFocus() }
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this@Recibo_devolucion, "Ocurrió un error: ${e.message}");
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
                            endpoint = "/devoluciones/recibo/items",
                            params=params,
                            dataClass = ListReciboAbastecimientoItems::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    Log.d("DEBUG", "Lista recibida: ${lista.size} elementos")
                                    if (lista.isNotEmpty()) {
                                        val items = lista.map {
                                            ListReciboAbastecimientoItems(
                                                ID = it.ID,
                                                ITEM = it.ITEM,
                                                CANTIDAD = it.CANTIDAD,
                                                CODIGO = it.CODIGO,
                                                DESCRIPCION = it.DESCRIPCION,
                                                CANTIDAD_RECIBIDA = it.CANTIDAD_RECIBIDA,
                                                POR_RECIBIR = it.POR_RECIBIR,
                                                SUBLINEA2_ID = it.SUBLINEA2_ID,
                                                CAJA = "",
                                                STATUS = "",
                                                REFERENCIA = it.REFERENCIA,
                                                CALIDAD = it.CALIDAD,
                                                RAZON_CALIDAD = ""
                                            )

                                        }

                                        actualizarTableLayout(items);
                                    } else {
                                        Log.d("DEBUG", "Lista vacía, redirigiendo...")
                                        val tableLayout =
                                            findViewById<TableLayout>(R.id.tableLayout)
                                        if (tableLayout.childCount > 1) {
                                            tableLayout.removeViews(1, tableLayout.childCount - 1)
                                        }
                                        val intent = Intent(this@Recibo_devolucion, Seleccion_devolucion::class.java);
                                        intent.putExtra("MESSAGE", "Tarea concluida");
                                        startActivity(intent);
                                        finish();
                                    }

                                }
                            },
                            onError = {error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Recibo_devolucion, "Errores: $error");
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Recibo_devolucion, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@Recibo_devolucion, "Ocurrió un error: ${e.message}");
                }
            }
        }else{
            MensajesDialog.showMessage(this@Recibo_devolucion,"Se debe de seleccionar un folio");
        }

    }

    private fun actualizarTableLayout(items: List<ListReciboAbastecimientoItems>) {
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

    private fun agregarFila(tableRow: TableRow, item: ListReciboAbastecimientoItems) {
        try {
            val unidadesTextView = TextView(this).apply {
                text = item.CANTIDAD.toString();
                gravity = Gravity.CENTER
                visibility = View.GONE
            }

            val descripcionTextView = TextView(this).apply {
                text = item.DESCRIPCION
            }

            val codigoTextView = TextView(this).apply {
                text = item.CODIGO
                gravity = Gravity.CENTER
            }

            val ItemTextView = TextView(this).apply {
                text = item.ITEM.toString();
                gravity = Gravity.CENTER
                visibility = View.GONE
            }
            val RecibidaTextView = TextView(this).apply {
                text = item.CANTIDAD_RECIBIDA.toString();
                gravity = Gravity.CENTER
            }
            val referenciaTextView = TextView(this).apply {
                text = item.REFERENCIA
                gravity = Gravity.CENTER
                visibility=View.GONE
            }
            val calidad = TextView(this).apply {
                text = item.CALIDAD
                gravity = Gravity.CENTER
                visibility=View.GONE
            }
            val razon_calidad = TextView(this).apply {
                text = item.RAZON_CALIDAD
                gravity = Gravity.CENTER
                visibility=View.GONE
            }

            tableRow.addView(unidadesTextView);
            tableRow.addView(descripcionTextView);
            tableRow.addView(codigoTextView);
            tableRow.addView(ItemTextView);
            tableRow.addView(RecibidaTextView);
            tableRow.addView(referenciaTextView);
            tableRow.addView(calidad);
            tableRow.addView(razon_calidad);

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun actualizarFilaSeleccionada(item: ListReciboAbastecimientoItems) {
        try {
            val editDescripcion = findViewById<TextView>(R.id.txtDescripcion)
            editDescripcion.text = item.DESCRIPCION
            this@Recibo_devolucion.codigo=item.CODIGO;
            this@Recibo_devolucion.descripcion=item.DESCRIPCION;
            this@Recibo_devolucion.item_confirm=item.ITEM.toString();
            this@Recibo_devolucion.cantidad=item.CANTIDAD.toString();
            this@Recibo_devolucion.referencia=item.REFERENCIA.toString();
            this@Recibo_devolucion.calidad=item.CALIDAD.toString();
            this@Recibo_devolucion.razon_calidad=item.RAZON_CALIDAD.toString();
            this@Recibo_devolucion.id=item.ID;
            MensajesDialog.showMessage(this,"PRODUCTO:  ${this@Recibo_devolucion.codigo}, ${this@Recibo_devolucion.descripcion}")
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }


    fun buscarCodigo(codigo: String, tableLayout: TableLayout): Boolean {
        try {
            for (i in 1 until tableLayout.childCount) {
                val fila = tableLayout.getChildAt(i) as? TableRow ?: continue

                val columna1 = fila.getChildAt(2) as? TextView ?: continue
                fila.setBackgroundColor(Color.TRANSPARENT);
                if (columna1.text.equals(codigo) ) {
                    println("Encontrado");

                    fila.setBackgroundColor(Color.parseColor("#639A67"))

                    val unidades=(fila.getChildAt(0) as? TextView)?.text.toString();
                    val descripcion=(fila.getChildAt(1) as? TextView)?.text.toString();
                    val codigo=(fila.getChildAt(2) as? TextView)?.text.toString();
                    val items=(fila.getChildAt(3) as? TextView)?.text.toString();
                    val recibida=(fila.getChildAt(4) as? TextView)?.text.toString();
                    val referencia=(fila.getChildAt(5 )as? TextView)?.text.toString();
                    val calidad=(fila.getChildAt(6 )as? TextView)?.text.toString();
                    val razon_calidad=(fila.getChildAt(7 )as? TextView)?.text.toString();
                    val status=(fila.getChildAt(8) as? TextView)?.text.toString();



                    val item = ListReciboAbastecimientoItems(this@Recibo_devolucion.id,items.toInt(),unidades.toInt(),codigo,descripcion,recibida.toInt(),0,0,"",status,referencia,calidad,razon_calidad )

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
        startActivity(Intent(this, Seleccion_devolucion::class.java));
        //LogsEntradaSalida.logsPorModulo( this@Recibo_devolucion, lifecycleScope, "DEVOLUCION/RECIBO", "SALIDA");
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