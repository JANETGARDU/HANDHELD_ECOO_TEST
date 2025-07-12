package com.example.slimmx.Reacomodo.Tareas

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.CheckBox
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
import com.example.slimmx.Acomodo.Acomodo_confirmacion
import com.example.slimmx.GlobalConfig
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListItemsAcomodo
import com.example.slimmx.ListaReacomodoList
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.R
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.example.slimmx.Vista.FragmentPage3
import com.example.slimmx.Vista.FragmentPageEtiquetaBluetooth
import com.example.slimmx.Vista.FragmentPageImpresionEtiquetas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Tarea_reacomodo_list : AppCompatActivity() , FragmentPageImpresionEtiquetas.OnBackgroundBlockerListener, FragmentPage3.OnBackgroundBlockerListener, FragmentPageEtiquetaBluetooth.OnBackgroundBlockerListener   {

    private var filaSeleccionada: TableRow? = null
    private var codigo:String="";
    private var descripcion:String="";
    private var ubicacion_origen:String="";
    private var box_origen:String="";
    private var ubicacion_destino:String="";
    private var box_destino:String="";
    private var unidades:String="";
    private var item_producto="";
    private var folio:String="";

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
        setContentView(R.layout.activity_tarea_reacomodo_list)
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

        val v_id=intent.getStringExtra("id");

        this@Tarea_reacomodo_list.folio=v_id.toString();

        val lbfolio=findViewById<TextView>(R.id.lb_folio_reacomodo_list);
        val txtCodigo=findViewById<EditText>(R.id.editTextCodigo_Reacomodo_list);
        val tableLayout=findViewById<TableLayout>(R.id.tableLayout_Reacomodo_List);
        val btn_okConfirmacion=findViewById<Button>(R.id.buttonOK_Reacomodo);

        txtCodigo.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        lbfolio.setText(v_id);

        obtencionItems(v_id.toString());

        val buttonMenu = findViewById<Button>(R.id.buttonMenu_Reacomodo)
        buttonMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, buttonMenu)
            popupMenu.menuInflater.inflate(R.menu.menu_opciones, popupMenu.menu)
            val menuItemVerImagen = popupMenu.menu.findItem(R.id.item_ver_imagen)
            menuItemVerImagen.isVisible = false
            val menuImpresionBluetooth=popupMenu.menu.findItem(R.id.item_etiquetas_bluetooth);
            menuImpresionBluetooth.isVisible=false;
            val menuRecomendacion=popupMenu.menu.findItem(R.id.item_recomendaciones);
            menuRecomendacion.isVisible=false;
            val menuItemCombosPacks=popupMenu.menu.findItem(R.id.item_packs_combos);
            menuItemCombosPacks.isVisible=false;
            val menuVerificacion=popupMenu.menu.findItem(R.id.item_verificacion);
            menuVerificacion.isVisible=false;
            val menuInventory=popupMenu.menu.findItem(R.id.item_inventario);
            menuInventory.isVisible=false;
            popupMenu.setOnMenuItemClickListener { item ->
                try{
                    when (item.itemId) {
                        R.id.item_impresora -> {
                            val codigo = this@Tarea_reacomodo_list.codigo
                            val descripcion = this@Tarea_reacomodo_list.descripcion
                            val folio = v_id.toString();
                            if(codigo.isNotEmpty() && descripcion.isNotEmpty() && folio.isNotEmpty()){

                                val fragmentImpresion = FragmentPageImpresionEtiquetas.newInstance(codigo, descripcion, 2, folio,"")

                                val fragmentContainerView = findViewById<View>(R.id.fragmentContainerView_impresion)
                                val backgroundBlocker = findViewById<View>(R.id.background_blocker)

                                val transaction = supportFragmentManager.beginTransaction()
                                val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView_impresion)

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
                                    transaction.add(R.id.fragmentContainerView_impresion, fragmentImpresion);
                                    backgroundBlocker.bringToFront();
                                    fragmentContainerView.bringToFront();
                                    backgroundBlocker.visibility = View.VISIBLE;
                                    backgroundBlocker.setOnTouchListener { _, _ -> true }
                                }
                                transaction.commitNow()

                            }else{
                                MensajesDialog.showMessage(this,"Debes de seleccionar algún producto")
                            }
                            true
                        }
                        R.id.item_f1 -> {
                            if (this@Tarea_reacomodo_list.codigo.isNotEmpty()){
                                val fragmentPage3 = FragmentPage3.newInstance(this@Tarea_reacomodo_list.codigo)

                                val fragmentContainerView = findViewById<View>(R.id.fragmentContainerView_Reacomodo_f1)
                                val backgroundBlocker = findViewById<View>(R.id.background_blocker)

                                val transaction = supportFragmentManager.beginTransaction()
                                val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView_Reacomodo_f1)

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
                                    transaction.add(R.id.fragmentContainerView_Reacomodo_f1, fragmentPage3);
                                    backgroundBlocker.bringToFront();
                                    fragmentContainerView.bringToFront();
                                    backgroundBlocker.visibility = View.VISIBLE;
                                    backgroundBlocker.setOnTouchListener { _, _ -> true }
                                }

                                transaction.commitNow()
                            }else{
                                MensajesDialog.showMessage(this, "Se debe de seleccionar un producto")
                            }

                            true
                        }
                        R.id.item_ver_imagen -> {
                            false
                        }
                        R.id.item_etiquetas_bluetooth -> {

                            if (this@Tarea_reacomodo_list.codigo.isNullOrBlank() || this@Tarea_reacomodo_list.descripcion.isNullOrBlank()) {
                                MensajesDialog.showMessage(this, "Se debe de seleccionar algún código");
                                return@setOnMenuItemClickListener true
                            }

                            val fragmentEtiquetaBluetooth = FragmentPageEtiquetaBluetooth().apply {
                                arguments = Bundle().apply {
                                    putString(FragmentPageEtiquetaBluetooth.ARG_CODIGO, this@Tarea_reacomodo_list.codigo)
                                    putString(FragmentPageEtiquetaBluetooth.ARG_DESCRIPCION, this@Tarea_reacomodo_list.descripcion)
                                    putInt(FragmentPageEtiquetaBluetooth.ARG_TIPO, 2)
                                    putString(FragmentPageEtiquetaBluetooth.ARG_FOLIO,v_id.toString())
                                }
                            }

                            val fragmentContainerView = findViewById<View>(R.id.fragmentContainerView_impresion_bluetooth)
                            val backgroundBlocker = findViewById<View>(R.id.background_blocker)

                            val transaction = supportFragmentManager.beginTransaction()
                            val fragmentActual = supportFragmentManager.findFragmentById(R.id.fragmentContainerView_impresion_bluetooth)

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
                                transaction.add(R.id.fragmentContainerView_impresion_bluetooth, fragmentEtiquetaBluetooth)
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

        txtCodigo.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtCodigo.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        val codigoIngresado = inputText.toString().trim()
                        val encontrado = buscarCodigo(codigoIngresado, tableLayout)
                        if (!encontrado) {
                            MensajesDialog.showMessage(this@Tarea_reacomodo_list, "No se encontro ese Código")
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

        btn_okConfirmacion.setOnClickListener {
            try {
                if (this@Tarea_reacomodo_list.codigo.isNotEmpty() && this@Tarea_reacomodo_list.descripcion.isNotEmpty() && this@Tarea_reacomodo_list.ubicacion_origen.isNotEmpty() && this@Tarea_reacomodo_list.box_origen.isNotEmpty()
                    && this@Tarea_reacomodo_list.ubicacion_destino.isNotEmpty() && this@Tarea_reacomodo_list.box_destino.isNotEmpty() && this@Tarea_reacomodo_list.unidades.isNotEmpty()
                    && this@Tarea_reacomodo_list.item_producto.isNotEmpty()){
                    val intent = Intent(this, Reacomodo_Items::class.java)

                    // Envía los valores correctos
                    intent.putExtra("id", v_id.toString())
                    intent.putExtra("codigo", this@Tarea_reacomodo_list.codigo)
                    intent.putExtra("descripcion", this@Tarea_reacomodo_list.descripcion)
                    intent.putExtra("ubicacion_origen", this@Tarea_reacomodo_list.ubicacion_origen)
                    intent.putExtra("box_origen", this@Tarea_reacomodo_list.box_origen)
                    intent.putExtra("ubicacion_destino", this@Tarea_reacomodo_list.ubicacion_destino)
                    intent.putExtra("box_destino", this@Tarea_reacomodo_list.box_destino)
                    intent.putExtra("unidades", this@Tarea_reacomodo_list.unidades)
                    intent.putExtra("item", this@Tarea_reacomodo_list.item_producto)


                    startActivity(intent)

                    this@Tarea_reacomodo_list.codigo = "";
                    this@Tarea_reacomodo_list.descripcion = "";
                    this@Tarea_reacomodo_list.ubicacion_origen = "";
                    this@Tarea_reacomodo_list.box_origen = "";
                    this@Tarea_reacomodo_list.ubicacion_destino = "";
                    this@Tarea_reacomodo_list.box_destino = "";
                    this@Tarea_reacomodo_list.unidades = "";
                    this@Tarea_reacomodo_list.item_producto = "";
                    txtCodigo.setText("");
                }else{
                    MensajesDialog.showMessage(this,"No se ha seleccionado ningún producto");
                }
            }catch (e: Exception){
                MensajesDialog.showMessage(this@Tarea_reacomodo_list, "Ocurrió un error: ${e.message}");
            }
        }

    }

    private fun obtencionItems(folio:String){
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout_Reacomodo_List)

            if (tableLayout.childCount > 1) {
                tableLayout.removeViews(1, tableLayout.childCount - 1)
            }
            val params= mapOf(
                "id" to folio
            )
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/inventario/tareas/reubicacion/items/list",
                        params=params,
                        dataClass = ListaReacomodoList::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val items = lista.map {
                                        ListaReacomodoList(
                                            UBICACION_ORIGEN = it.UBICACION_ORIGEN,
                                            BOX_ORIGEN = it.BOX_ORIGEN,
                                            UBICACION_DESTINO = it.UBICACION_DESTINO,
                                            BOX_DESTINO = it.BOX_DESTINO,
                                            CANTIDAD = it.CANTIDAD,
                                            CODIGO = it.CODIGO,
                                            DESCRIPCION = it.DESCRIPCION,
                                            ITEM = it.ITEM
                                        )

                                    }
                                    actualizarTableLayout(items);
                                } else {
                                    val tableLayout =
                                        findViewById<TableLayout>(R.id.tableLayout_Reacomodo_List)
                                    if (tableLayout.childCount > 1) {
                                        tableLayout.removeViews(1, tableLayout.childCount - 1)
                                    }
                                    val intent = Intent(this@Tarea_reacomodo_list, Reacomodo_Check::class.java);
                                    intent.putExtra("MESSAGE", "Tarea concluida");
                                    startActivity(intent);
                                    finish();

                                }

                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Tarea_reacomodo_list, "Error: $error");
                            }
                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Tarea_reacomodo_list, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Tarea_reacomodo_list, "Ocurrió un error: ${e.message}");
            }
        }
    }


    private fun actualizarTableLayout(items: List<ListaReacomodoList>) {
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout_Reacomodo_List)

            if (tableLayout.childCount > 1) {
                tableLayout.removeViews(1, tableLayout.childCount - 1)
            }

            for (item in items) {
                val tableRow = TableRow(this)
                agregarFila(tableRow, item)

                tableRow.setOnClickListener {
                    filaSeleccionada?.let {
                        it.setBackgroundColor(Color.TRANSPARENT)
                    }

                    tableRow.setBackgroundColor(Color.parseColor("#639A67"))
                    filaSeleccionada = tableRow
                    actualizarFilaSeleccionada(item)
                }

                // Añade la fila a la tabla
                tableLayout.addView(tableRow)
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun agregarFila(tableRow: TableRow, item: ListaReacomodoList) {
        try {
            val ubicacion_origen=TextView(this).apply {
                text=item.UBICACION_ORIGEN;
                gravity= Gravity.CENTER
            }
            val box_origen=TextView(this).apply {
                text=item.BOX_ORIGEN;
                gravity=Gravity.CENTER
            }
            val ubicacion_destino=TextView(this).apply {
                text=item.UBICACION_DESTINO;
                gravity=Gravity.CENTER
            }
            val box_destino=TextView(this).apply {
                text=item.BOX_DESTINO;
                gravity=Gravity.CENTER
            }

            val unidades=TextView(this).apply {
                text=item.CANTIDAD.toString();
                gravity=Gravity.CENTER
            }

            val codigo=TextView(this).apply {
                text=item.CODIGO;
                gravity=Gravity.CENTER
            }
            val Descripcion=TextView(this).apply {
                text=item.DESCRIPCION;
            }
            val items_producto=TextView(this).apply {
                text=item.ITEM.toString();
                gravity=Gravity.CENTER
                visibility=View.GONE
            }

            tableRow.addView(ubicacion_origen);
            tableRow.addView(box_origen);
            tableRow.addView(ubicacion_destino);
            tableRow.addView(box_destino);
            tableRow.addView(unidades);
            tableRow.addView(codigo);
            tableRow.addView(Descripcion);
            tableRow.addView(items_producto);
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun actualizarFilaSeleccionada(item: ListaReacomodoList) {
        try {
            val editDescripcion = findViewById<TextView>(R.id.txtDescripcion_Reacomodo_list)
            editDescripcion.text = item.DESCRIPCION

            this@Tarea_reacomodo_list.codigo=item.CODIGO;
            this@Tarea_reacomodo_list.descripcion=item.DESCRIPCION;
            this@Tarea_reacomodo_list.ubicacion_origen=item.UBICACION_ORIGEN;
            this@Tarea_reacomodo_list.box_origen=item.BOX_ORIGEN;
            this@Tarea_reacomodo_list.ubicacion_destino=item.UBICACION_DESTINO;
            this@Tarea_reacomodo_list.box_destino=item.BOX_DESTINO;
            this@Tarea_reacomodo_list.unidades=item.CANTIDAD.toString();
            this@Tarea_reacomodo_list.item_producto=item.ITEM.toString();

            MensajesDialog.showMessage(this,"PRODUCTO:  ${this@Tarea_reacomodo_list.codigo}, ${this@Tarea_reacomodo_list.descripcion}")
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    fun buscarCodigo(codigo: String, tableLayout: TableLayout): Boolean {
        try {
            for (i in 1 until tableLayout.childCount) {
                val fila = tableLayout.getChildAt(i) as? TableRow ?: continue

                val columna1 = fila.getChildAt(5) as? TextView ?: continue
                fila.setBackgroundColor(Color.TRANSPARENT);
                if (columna1.text.equals(codigo) ) {
                    println("Encontrado");

                    fila.setBackgroundColor(Color.parseColor("#639A67"))

                    val ubi_origen=(fila.getChildAt(0) as? TextView)?.text.toString();
                    val box_origen=(fila.getChildAt(1) as? TextView)?.text.toString();
                    val ubi_destino=(fila.getChildAt(2) as? TextView)?.text.toString();
                    val box_destino=(fila.getChildAt(3) as? TextView)?.text.toString();
                    val unidades=(fila.getChildAt(4) as? TextView)?.text.toString();
                    val codigo=(fila.getChildAt(5) as? TextView)?.text.toString();
                    val descripcion=(fila.getChildAt(6) as? TextView)?.text.toString();
                    val item_producto=(fila.getChildAt(7) as? TextView)?.text.toString();
                    val unidades_destino="";


                    val item = ListaReacomodoList(ubi_origen, box_origen,ubi_destino, box_destino, unidades.toInt(),codigo,descripcion,item_producto.toInt());

                    actualizarFilaSeleccionada(item)

                    return true // Código encontrado
                }
            }
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
        return false
    }

    override fun onResume() {//Cuando regresa a primer plano el activity
        super.onResume()
         obtencionItems(this@Tarea_reacomodo_list.folio.toString());
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