package com.example.slimmx.Packs_Combos

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
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
import com.example.slimmx.GlobalConfig
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListItemsAcomodo
import com.example.slimmx.ListaItemsEnvioAsig
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.R
import com.example.slimmx.Submenus.Submenu_recibo
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.example.slimmx.Vista.FragmentPage3
import com.example.slimmx.Vista.FragmentPageEtiquetaBluetooth
import com.example.slimmx.Vista.FragmentPageImpresionEtiquetas
import com.example.slimmx.productosPacks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Busqueda_Packs : AppCompatActivity() ,  FragmentPage3.OnBackgroundBlockerListener, FragmentPageEtiquetaBluetooth.OnBackgroundBlockerListener{

    private var filaSeleccionada: TableRow? = null
    private var codigo:String="";

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
        setContentView(R.layout.activity_busqueda_packs)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val txtCodigoPacks=findViewById<EditText>(R.id.txtPacks_Codigo);

        if (GlobalUser.nombre.isNullOrEmpty()) {
            MensajesDialogConfirmaciones.showMessage(
                this,
                "Ocurrio un error se cerrara la aplicacion, lamento el inconveniente"
            ) {
                finishAffinity()
            }
        }

        val v_codigo = intent.getStringExtra("codigo")
        //MensajesDialog.showMessage(this, "CODIGO ${v_codigo}")

        if (!v_codigo.isNullOrEmpty()) {
            txtCodigoPacks.setText(v_codigo)

            txtCodigoPacks.post {
                txtCodigoPacks.requestFocus()

                Handler(Looper.getMainLooper()).postDelayed({
                    txtCodigoPacks.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                    txtCodigoPacks.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                }, 0)
            }
        }
        val tableLayout=findViewById<TableLayout>(R.id.tableLayout_Packs);


        val lb_CombosPacketes=findViewById<TextView>(R.id.lb_CombosPacketes);
        lb_CombosPacketes.requestFocus();



        val btn_eliminar_codigo=findViewById<Button>(R.id.buttonEliminar_Busqueda);

        txtCodigoPacks.setOnTouchListener(MostrarTeclado.getMostrarTeclado());

        val buttonMenu = findViewById<Button>(R.id.buttonMenu_Packs)
        buttonMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, buttonMenu)
            popupMenu.menuInflater.inflate(R.menu.menu_opciones, popupMenu.menu)
            val menuItemVerImagen = popupMenu.menu.findItem(R.id.item_ver_imagen)
            menuItemVerImagen.isVisible = false
            val menuItemImprimir=popupMenu.menu.findItem(R.id.item_impresora);
            menuItemImprimir.isVisible=false;
            val menuPacks=popupMenu.menu.findItem(R.id.item_packs_combos);
            menuPacks.isVisible=false;
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
                            true
                        }
                        R.id.item_f1 -> {
                            if (this@Busqueda_Packs.codigo.toString().isNotEmpty()){
                                val fragmentPage3 = FragmentPage3.newInstance(this@Busqueda_Packs.codigo.toString(), "2")

                                val fragmentContainerView = findViewById<View>(R.id.fragmentContainerView_Packs_f1)
                                val backgroundBlocker = findViewById<View>(R.id.background_blocker)

                                val transaction = supportFragmentManager.beginTransaction()
                                val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView_Packs_f1)

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
                                    transaction.add(R.id.fragmentContainerView_Packs_f1, fragmentPage3);
                                    backgroundBlocker.bringToFront();
                                    fragmentContainerView.bringToFront();
                                    backgroundBlocker.visibility = View.VISIBLE;
                                    backgroundBlocker.setOnTouchListener { _, _ -> true }
                                }

                                transaction.commitNow()
                            }else{
                                MensajesDialog.showMessage(this, "Se debe de escanear un código")
                            }

                            true
                        }
                        R.id.item_ver_imagen -> {
                            false
                        }
                        R.id.item_etiquetas_bluetooth->{
                            val fragmentEtiquetaBluetooth = FragmentPageEtiquetaBluetooth()

                            // Aquí creas el Bundle con tus variables
                            val bundle = Bundle()
                            bundle.putString("ARG_CODIGO", "tu_codigo_aqui")
                            bundle.putString("ARG_DESCRIPCION", "tu_descripcion_aqui")
                            bundle.putInt("ARG_TIPO", 1) // o el tipo que quieras
                            bundle.putString("ARG_FOLIO", "tu_folio_aqui")

                            fragmentEtiquetaBluetooth.arguments = bundle

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


        txtCodigoPacks.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtCodigoPacks.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        BuscarProductos(txtCodigoPacks.text.toString());
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }

        btn_eliminar_codigo.setOnClickListener {
            txtCodigoPacks.setText("");
            txtCodigoPacks.post { txtCodigoPacks.requestFocus() }
        }

    }

    private fun BuscarProductos(codigo:String){
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout_Packs)

            if (tableLayout.childCount > 1) {
                tableLayout.removeViews(1, tableLayout.childCount - 1)
            }
            val params= mapOf(
                "codigo" to codigo.uppercase()
            );
            val headers = mapOf("Token" to GlobalUser.token.toString());

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/productos/com_pack/contenido",
                        params=params,
                        dataClass = productosPacks::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val items = lista.map {
                                        productosPacks(
                                            CODIGO = it.CODIGO,
                                            DESCRIPCION = it.DESCRIPCION,
                                            CANTIDAD = it.CANTIDAD
                                        )

                                    }
                                    actualizarTableLayout(items);

                                } else {
                                    val tableLayout =
                                        findViewById<TableLayout>(R.id.tableLayout_Packs)
                                    if (tableLayout.childCount > 1) {
                                        tableLayout.removeViews(1, tableLayout.childCount - 1)
                                    }
                                    MensajesDialogConfirmaciones.showMessage(
                                        this@Busqueda_Packs,
                                        "Ese código no existe"
                                    ) {
                                        val txtCodigoPacks=findViewById<EditText>(R.id.txtPacks_Codigo);
                                        txtCodigoPacks.setText("");
                                    }
                                }
                            }
                        },
                        onError = {error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Busqueda_Packs, "Errores: $error");
                            }
                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Busqueda_Packs, "Ocurrió un error: ${e.message}")
                    }
                }
            }
        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@Busqueda_Packs, "Ocurrió un error: ${e.message}");
            }
        }

    }

    private fun actualizarTableLayout(items: List<productosPacks>) {
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout_Packs)

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
    private fun actualizarInformacionSeleccionada(item: productosPacks) { //De la fila seleccionada colocar los datos en las textview
        try {
            this@Busqueda_Packs.codigo=item.CODIGO;

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun agregarFila(tableRow: TableRow, item: productosPacks) {
        try {
            val codigo=TextView(this).apply {
                text=item.CODIGO;
                gravity= Gravity.CENTER
            }
            val Descripcion=TextView(this).apply {
                text=item.DESCRIPCION;
            }
            val unidades=TextView(this).apply {
                text=item.CANTIDAD.toString();
                gravity= Gravity.CENTER
            }

            tableRow.addView(codigo);
            tableRow.addView(Descripcion);
            tableRow.addView(unidades);

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    override fun onBackPressed() {
        LogsEntradaSalida.logsPorModulo( this@Busqueda_Packs, lifecycleScope, "PACK_COMBO", "SALIDA")
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