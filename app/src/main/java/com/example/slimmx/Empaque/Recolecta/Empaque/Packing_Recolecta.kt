package com.example.slimmx.Empaque.Recolecta.Empaque

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.Empaque.Recolecta.Submenu_Empaque_Recolecta
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListRecolectaPacking
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Packs_Combos.Busqueda_Packs
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.R
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.example.slimmx.Vista.ImageFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Packing_Recolecta : AppCompatActivity() , ImageFragment.OnBackgroundBlockerListener {

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

    private var token: String="";
    private var filaSeleccionada: TableRow? = null
    private var codigo:String="";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_packing_recolecta)
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

        this@Packing_Recolecta.token=GlobalUser.token.toString();
        val txtNumeroGuia=findViewById<EditText>(R.id.editTextNumeroGuia);
        val btn_Confirmacion=findViewById<Button>(R.id.buttonOK_PA_RE);
        var txtPack=findViewById<TextView>(R.id.txtPackId_PA_RE);

        txtNumeroGuia.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        txtNumeroGuia.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtNumeroGuia.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        buscarguia(txtNumeroGuia.text.toString());
                        txtNumeroGuia.setText("");
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }

        btn_Confirmacion.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Estás seguro de que deseas continuar?")
                .setPositiveButton("Sí") { _, _ ->
                    try {
                        confirmacion(txtPack.text.toString())
                    }catch (e: Exception){
                        MensajesDialog.showMessage(this@Packing_Recolecta, "Ocurrió un error: ${e.message}");
                    }
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        val buttonMenu = findViewById<Button>(R.id.buttonMenu)
        buttonMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, buttonMenu)
            popupMenu.menuInflater.inflate(R.menu.menu_opciones, popupMenu.menu)
            val menuImpresion = popupMenu.menu.findItem(R.id.item_impresora)
            menuImpresion.isVisible = false
            val menuUbicaciones = popupMenu.menu.findItem(R.id.item_f1)
            menuUbicaciones.isVisible = false
            val menuImpresionBluetooth=popupMenu.menu.findItem(R.id.item_etiquetas_bluetooth);
            menuImpresionBluetooth.isVisible=false;
            val menuRecomendacion=popupMenu.menu.findItem(R.id.item_recomendaciones);
            menuRecomendacion.isVisible=false;
            val menuCombosPacks=popupMenu.menu.findItem(R.id.item_packs_combos)
            menuCombosPacks.isVisible= false
            val menuVerificacion=popupMenu.menu.findItem(R.id.item_verificacion);
            menuVerificacion.isVisible=false;
            val menuInventory=popupMenu.menu.findItem(R.id.item_inventario);
            menuInventory.isVisible=false;
            popupMenu.setOnMenuItemClickListener { item ->
                try {
                    when (item.itemId) {
                        R.id.item_impresora -> {
                            true
                        }
                        R.id.item_f1 -> {
                            true
                        }
                        R.id.item_ver_imagen -> {
                            val codigo = this@Packing_Recolecta.codigo
                            val fragmentContainer = findViewById<View>(R.id.viewSwitcher)
                            val backgroundBlocker = findViewById<View>(R.id.background_blocker)

                            if (codigo.isNotEmpty()) {
                                val transaction = supportFragmentManager.beginTransaction()
                                val existingFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerViewImagen)

                                if (existingFragment != null && existingFragment.isVisible) {
                                    transaction.hide(existingFragment)
                                    transaction.commitNow()
                                    backgroundBlocker.visibility = View.GONE
                                    backgroundBlocker.setOnTouchListener(null)
                                } else {
                                    transaction.replace(R.id.fragmentContainerViewImagen, ImageFragment.newInstance(codigo))
                                    transaction.commitNow()
                                    backgroundBlocker.visibility = View.VISIBLE
                                    backgroundBlocker.bringToFront()

                                    backgroundBlocker.setOnTouchListener { _, _ -> true }
                                    fragmentContainer.bringToFront()
                                }
                            } else {
                                MensajesDialog.showMessage(this, "No se está recibiendo un código")
                            }
                            true
                        }

                        R.id.item_packs_combos->{
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

    }

    private fun buscarguia(id: String){
        var txtPack=findViewById<TextView>(R.id.txtPackId_PA_RE);
        var txtNoVenta=findViewById<TextView>(R.id.txtNoVenta_PA_RE);
        var txtDestino=findViewById<TextView>(R.id.txtDestinatario_PA_RE);
        var txtCantidad=findViewById<TextView>(R.id.txtCantidad_PA_RE);
        var txtSku=findViewById<TextView>(R.id.txtSku_PA_RE);
        var txtTitulo=findViewById<TextView>(R.id.txtTitulo_PA_RE);

        var txtCantidadTotal=findViewById<TextView>(R.id.txtCantidadTo_PA_RE);
        val tableLayout = findViewById<TableLayout>(R.id.tableLayoutPA_RE)

        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }
        if (id.isNotEmpty()){
            try {
                val params= mapOf(
                    "id" to id
                )

                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/paquetes/general/items",
                            params=params,
                            dataClass = ListRecolectaPacking::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val datos = lista[0];
                                        txtPack.setText(id);
                                        txtNoVenta.setText(datos.ID ?: " ");
                                        txtDestino.setText(datos.DESTINATARIO ?: " ");

                                        val items = lista.map {
                                            ListRecolectaPacking(
                                                ITEM_ID = it.ITEM_ID,
                                                TITLE = it.TITLE,
                                                SKU = it.SKU,
                                                QUANTITY = it.QUANTITY,
                                                ID = it.ID,
                                                SHIPPMENT_ID = it.SHIPPMENT_ID,
                                                DESTINATARIO = it.DESTINATARIO,
                                                PACK_ID = it.PACK_ID,
                                                ECOMMERCE = it.ECOMMERCE,
                                                CODIGO = it.CODIGO
                                            )
                                        }

                                        val totalQuantity =
                                            lista.sumOf { it.QUANTITY?.toInt() ?: 0 }
                                        txtCantidadTotal.text =
                                            "Cantidad total: \n ${totalQuantity.toString()}"

                                        actualizarTableLayout(items);
                                    } else {
                                        MensajesDialog.showMessage(this@Packing_Recolecta, "Paquete no encontrado")
                                        txtPack.setText("");
                                        txtNoVenta.setText(" ");
                                        txtDestino.setText(" ");
                                        val items = lista.map {
                                            ListRecolectaPacking(
                                                ITEM_ID = "",
                                                TITLE = "",
                                                SKU = "",
                                                QUANTITY = 0,
                                                ID = "",
                                                SHIPPMENT_ID = "",
                                                DESTINATARIO = "",
                                                PACK_ID = "",
                                                ECOMMERCE = "",
                                                CODIGO = ""
                                            )
                                        }

                                        val totalQuantity =
                                            lista.sumOf { it.QUANTITY?.toInt() ?: 0 }
                                        txtCantidadTotal.text =
                                            "Cantidad total: \n ${totalQuantity.toString()}"
                                        txtCantidad.text = "";
                                        txtSku.text = "";
                                        txtTitulo.text = "";
                                        actualizarTableLayout(items);
                                    }

                                }
                            },
                            onError = { error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Packing_Recolecta, "Error: $error")
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Packing_Recolecta, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(
                        this@Packing_Recolecta,
                        "Ocurrió un error: ${e.message}"
                    );
                }
            }

        }

    }

    private fun actualizarTableLayout(items: List<ListRecolectaPacking>) { //Hacer la selecion por fila
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayoutPA_RE)

            if (tableLayout.childCount > 1) {
                tableLayout.removeViews(1, tableLayout.childCount - 1)
            }

            for (item in items) {
                val tableRow = TableRow(this)
                agregarFila(tableRow, item)

                tableRow.setOnClickListener {
                    filaSeleccionada?.setBackgroundColor(Color.TRANSPARENT)

                    //Solo cambia de color la fila que se selecciono
                    tableRow.setBackgroundColor(Color.parseColor("#639A67"))

                    // Actualiza las filas para el control de los colores
                    filaSeleccionada = tableRow

                    actualizarInformacionSeleccionada(item)//Actualizar las label al seleccionar una nueva fila
                }

                tableLayout.addView(tableRow)
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun agregarFila(tableRow: TableRow, item: ListRecolectaPacking) { //Agregar los datos encontrados en la tabla
        // Crear TextViews y añadirlos a la fila
        try {
            val cantidadTextView = TextView(this).apply {
                text = item.QUANTITY.toString()
                gravity = Gravity.CENTER // Centrar el texto
            }
            val TituloTextView = TextView(this).apply {
                text = item.TITLE
            }
            val SkuTextView = TextView(this).apply {
                text = item.SKU
                gravity = Gravity.CENTER // Centrar el texto
            }
            val codigoTextView=TextView(this).apply {
                text=item.CODIGO
                visibility=View.GONE
            }


            // Añadir los TextViews a la TableRow
            tableRow.addView(cantidadTextView)
            tableRow.addView(TituloTextView)
            tableRow.addView(SkuTextView)
            tableRow.addView(codigoTextView)
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun actualizarInformacionSeleccionada(item: ListRecolectaPacking) { //De la fila seleccionada colocar los datos en las textview
        try {
            var txtCantidad=findViewById<TextView>(R.id.txtCantidad_PA_RE);
            var txtSku=findViewById<TextView>(R.id.txtSku_PA_RE);
            var txtTitulo=findViewById<TextView>(R.id.txtTitulo_PA_RE);

            txtCantidad.text = "${item.QUANTITY}"
            txtSku.text = "${item.SKU}"
            txtTitulo.text = "${item.TITLE}"
            this@Packing_Recolecta.codigo="${item.CODIGO}"
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun confirmacion(id: String){
        if (id.isNotEmpty()){
            try {
                var body= mapOf(
                    "SHIPPING_ID" to id
                )

                val headers = mapOf("Token" to GlobalUser.token.toString());

                lifecycleScope.launch {
                    Pedir_datos_apis_post(
                        endpoint = "/paquetes/general/check/set",
                        body = body,
                        listaKey = "message",
                        dataClass = Any::class,
                        headers = headers,
                        onSuccess = { lista ->
                            if (lista.toString().contains("OK")) {
                                var txtPack = findViewById<TextView>(R.id.txtPackId_PA_RE);
                                var txtNoVenta = findViewById<TextView>(R.id.txtNoVenta_PA_RE);
                                var txtDestino = findViewById<TextView>(R.id.txtDestinatario_PA_RE);
                                var txtCantidad = findViewById<TextView>(R.id.txtCantidad_PA_RE);
                                var txtSku = findViewById<TextView>(R.id.txtSku_PA_RE);
                                var txtTitulo = findViewById<TextView>(R.id.txtTitulo_PA_RE);

                                var txtCantidadTotal = findViewById<TextView>(R.id.txtCantidadTo_PA_RE);
                                MensajesDialogConfirmaciones.showMessage(this@Packing_Recolecta, "Confirmado correctamente") {
                                    txtPack.setText("");
                                    txtNoVenta.setText(" ");
                                    txtDestino.setText(" ");
                                    val items: List<ListRecolectaPacking> = listOf(
                                        ListRecolectaPacking(
                                            ITEM_ID = "",
                                            TITLE = "",
                                            SKU = "",
                                            QUANTITY = 0,
                                            ID = "",
                                            SHIPPMENT_ID = "",
                                            DESTINATARIO = "",
                                            PACK_ID = "",
                                            ECOMMERCE = "",
                                            CODIGO = ""
                                        )
                                    )

                                    txtCantidadTotal.text = "Cantidad total: \n 0"
                                    txtCantidad.text = "";
                                    txtSku.text = "";
                                    txtTitulo.text = "";
                                    actualizarTableLayout(items);
                                }

                            }
                        },
                        onError = { error ->
                            MensajesDialog.showMessage(this@Packing_Recolecta, "Error: $error")
                        }

                    )
                }

            }catch (e: Exception){
                MensajesDialog.showMessage(this@Packing_Recolecta, "Ocurrió un error: ${e.message}");
            }

        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_Empaque_Recolecta::class.java));
        //LogsEntradaSalida.logsPorModulo( this@Packing_Recolecta, lifecycleScope, "EMPAQUE/RECOLECTA/TAREA", "SALIDA");
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