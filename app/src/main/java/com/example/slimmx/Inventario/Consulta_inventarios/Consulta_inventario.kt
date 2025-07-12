package com.example.slimmx.Inventario.Consulta_inventarios

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalConfig
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListProducto
import com.example.slimmx.ListReciboAbastecimientoItems
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.R
import com.example.slimmx.Submenus.Submenu_inventarios
import com.example.slimmx.Submenus.Submenu_recibo
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Validaciones.MostrarTeclado
import com.example.slimmx.Vista.FragmentPage3
import com.example.slimmx.Vista.FragmentPageEtiquetaBluetooth
import com.example.slimmx.Vista.FragmentPageImpresionEtiquetas
import com.example.slimmx.Vista.FragmentPageImpresionEtiquetas_Verificacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Consulta_inventario : AppCompatActivity(), FragmentPageImpresionEtiquetas.OnBackgroundBlockerListener, FragmentPage3.OnBackgroundBlockerListener, FragmentPageEtiquetaBluetooth.OnBackgroundBlockerListener, FragmentPageImpresionEtiquetas_Verificacion.OnBackgroundBlockerListener
{
    private var codigo:String="";
    private var descripcion:String="";
    private var filaSeleccionada: TableRow? = null
    private lateinit var txtSearch_consulta: EditText;
    private lateinit var txtTotalStock:TextView;
    private lateinit var txtCodigosUnicos:TextView;
    private lateinit var lbTotalStock:TextView;
    private lateinit var lbCodigosUnicos:TextView;
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
        setContentView(R.layout.activity_consulta_inventario)
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

        var txtusuario=findViewById<TextView>(R.id.txtUsuario_consulta);
        txtSearch_consulta=findViewById<EditText>(R.id.txtSearch_consulta);
        var btn_eliminarSearch=findViewById<Button>(R.id.buttonEliminar_Search_Consulta);
        txtTotalStock=findViewById(R.id.txtExistencia);
        txtCodigosUnicos=findViewById(R.id.txtNCodigos);
        lbCodigosUnicos=findViewById(R.id.lbExistencia);
        lbTotalStock=findViewById(R.id.lbNCodigos);

        txtCodigosUnicos.isVisible=false;
        txtTotalStock.isVisible=false;
        lbCodigosUnicos.isVisible=false;
        lbTotalStock.isVisible=false;
        btn_eliminarSearch.setOnClickListener {
            txtSearch_consulta.setText("");
        }

        txtusuario.setText(GlobalUser.nombre.toString());

        val buttonMenu = findViewById<Button>(R.id.buttonMenu_Consulta)
        buttonMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, buttonMenu)
            popupMenu.menuInflater.inflate(R.menu.menu_opciones, popupMenu.menu)
            val menuItemVerImagen = popupMenu.menu.findItem(R.id.item_ver_imagen)
            menuItemVerImagen.isVisible = false
            val menuItemCombosPacks=popupMenu.menu.findItem(R.id.item_packs_combos);
            menuItemCombosPacks.isVisible=false;
            val menuImpresionBluetooth=popupMenu.menu.findItem(R.id.item_etiquetas_bluetooth);
            menuImpresionBluetooth.isVisible=false;
            val menuRecomendacion=popupMenu.menu.findItem(R.id.item_recomendaciones);
            menuRecomendacion.isVisible=false;
            val menuInventory=popupMenu.menu.findItem(R.id.item_inventario);
            menuInventory.isVisible=false;
            popupMenu.setOnMenuItemClickListener { item ->
                try {
                    when (item.itemId) {
                        R.id.item_impresora -> {
                            val codigo = this@Consulta_inventario.codigo
                            val descripcion = this@Consulta_inventario.descripcion
                            val folio = "-"
                            if (codigo.isNotEmpty() && descripcion.isNotEmpty() && folio.isNotEmpty()) {

                                val fragmentImpresion = FragmentPageImpresionEtiquetas.newInstance(
                                    codigo,
                                    descripcion,
                                    1,
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
                            true
                        }

                        R.id.item_f1 -> {
                            if (this@Consulta_inventario.codigo.isNotEmpty()) {
                                val fragmentPage3 =
                                    FragmentPage3.newInstance(this@Consulta_inventario.codigo)

                                val fragmentContainerView =
                                    findViewById<View>(R.id.fragmentContainerView_Consulta_f1)
                                val backgroundBlocker = findViewById<View>(R.id.background_blocker)

                                val transaction = supportFragmentManager.beginTransaction()
                                val fragment =
                                    supportFragmentManager.findFragmentById(R.id.fragmentContainerView_Consulta_f1)

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
                                        R.id.fragmentContainerView_Consulta_f1,
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

                            if (this@Consulta_inventario.codigo.isNullOrBlank() || this@Consulta_inventario.descripcion.isNullOrBlank()) {
                                MensajesDialog.showMessage(this, "Se debe de seleccionar algún código");
                                return@setOnMenuItemClickListener true
                            }

                            val fragmentEtiquetaBluetooth = FragmentPageEtiquetaBluetooth().apply {
                                arguments = Bundle().apply {
                                    putString(FragmentPageEtiquetaBluetooth.ARG_CODIGO, this@Consulta_inventario.codigo)
                                    putString(FragmentPageEtiquetaBluetooth.ARG_DESCRIPCION, this@Consulta_inventario.descripcion)
                                    putInt(FragmentPageEtiquetaBluetooth.ARG_TIPO, 1)
                                    putString(FragmentPageEtiquetaBluetooth.ARG_FOLIO, "")
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

                        R.id.item_verificacion -> {

                            val fragmentImpresion = FragmentPageImpresionEtiquetas_Verificacion.newInstance()

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

        txtSearch_consulta.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtSearch_consulta.text.toString()
                    txtCodigosUnicos.isVisible=false;
                    txtTotalStock.isVisible=false;
                    lbCodigosUnicos.isVisible=false;
                    lbTotalStock.isVisible=false;
                    if (!inputText.isNullOrEmpty()) {
                        /*if (txtSearch_consulta.text.toString().contains("-")){
                            buscarUbicacion(txtSearch_consulta.text.toString());
                        }else if (txtSearch_consulta.text.matches(Regex(".*[a-zA-Z].*")) && txtSearch_consulta.text.matches(Regex(".*[0-9].*"))) {
                            buscarCodigo(txtSearch_consulta.text.toString());
                        }else{*/
                            buscarArea(txtSearch_consulta.text.toString())
                        //}
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }

    }

    private fun buscarUbicacion(search:String){
        if(search.isNotEmpty()){
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout_Consulta_List)

            if (tableLayout.childCount > 1) {
                tableLayout.removeViews(1, tableLayout.childCount - 1)
            }
            try {
                val params= mapOf(
                    "search" to search.uppercase()
                )

                val headers= mapOf(
                    "Token" to GlobalUser.token.toString()
                )
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/productos/list/ubicacion",
                            params=params,
                            dataClass = ListProducto::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val items = lista.map {
                                            ListProducto(
                                                CODIGO = it.CODIGO,
                                                DESCRIPCION = it.DESCRIPCION,
                                                STOCK_CEDIS = it.STOCK_CEDIS,
                                                FISICO_DISPONIBLE = it.FISICO_DISPONIBLE,
                                                APARTADO_PICKING = it.APARTADO_PICKING,
                                                PISO_PICKING = it.PISO_PICKING,
                                                STOCK_CEDIS_UBICACIONES = it.STOCK_CEDIS_UBICACIONES,
                                                LINEA_ID = it.LINEA_ID,
                                                LINEA_NOMBRE = it.LINEA_NOMBRE,
                                                LINEA_CODIGO = it.LINEA_CODIGO,
                                                SUBLINEA2_ID = it.SUBLINEA2_ID,
                                                SUBLINEA2_NOMBRE = it.SUBLINEA2_NOMBRE,
                                                PRC_1 = it.PRC_1,
                                                PRC_2 = it.PRC_2,
                                                PRC_3 = it.PRC_3,
                                                PRECIO_1 = it.PRECIO_1,
                                                PRECIO_2 = it.PRECIO_2,
                                                PRECIO_3 = it.PRECIO_3,
                                                MAYOR_A2 = it.MAYOR_A2,
                                                MAYOR_A3 = it.MAYOR_A3,
                                                COSTO_ULTIMO = it.COSTO_ULTIMO,
                                                PRECIO_ML = it.PRECIO_ML,
                                                PRECIO_SHOPEE = it.PRECIO_SHOPEE,
                                                PRECIO_AMAZON = it.PRECIO_AMAZON,
                                                DESCRIPCION_CHINA = it.DESCRIPCION_CHINA,
                                                COLOR_MEDIDA_CHINA = it.COLOR_MEDIDA_CHINA,
                                                THUMBNAIL = it.THUMBNAIL,
                                                CODIGO_SAT = it.CODIGO_SAT,
                                                DESCRIPCION_SAT = it.DESCRIPCION_SAT,
                                                UNIDADES_POR_CAJA = it.UNIDADES_POR_CAJA,
                                                GRUPO = it.GRUPO,
                                                ID = "",
                                                VARIATION_ID = ""
                                            )

                                        }
                                        actualizarTableLayout(items);
                                        txtSearch_consulta.setText("");
                                    } else {
                                        MensajesDialog.showMessage(this@Consulta_inventario, "Ubicación no encontrada");
                                        txtSearch_consulta.setText("");
                                    }

                                }
                            },
                            onError = {error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Consulta_inventario, "Errores: $error");
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Consulta_inventario, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(
                        this@Consulta_inventario,
                        "Ocurrió un error: ${e.message}"
                    );
                }
            }

        }else{
            MensajesDialog.showMessage(this,"Se debe de escanear ya sea un código o ubicación ");
        }
    }

    private fun buscarCodigo(search:String){
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout_Consulta_List)

        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }
        if(search.isNotEmpty()){
            try {
                val params= mapOf(
                    "buscar" to search.uppercase(),
                    "limit" to "100"
                )

                val headers= mapOf(
                    "Token" to GlobalUser.token.toString()
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/productos/list/handheld",
                            params=params,
                            dataClass = ListProducto::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val items = lista.map {
                                            ListProducto(
                                                CODIGO = it.CODIGO,
                                                DESCRIPCION = it.DESCRIPCION,
                                                STOCK_CEDIS = it.STOCK_CEDIS,
                                                FISICO_DISPONIBLE = it.FISICO_DISPONIBLE,
                                                APARTADO_PICKING = it.APARTADO_PICKING,
                                                PISO_PICKING = it.PISO_PICKING,
                                                STOCK_CEDIS_UBICACIONES = it.STOCK_CEDIS_UBICACIONES,
                                                LINEA_ID = it.LINEA_ID,
                                                LINEA_NOMBRE = it.LINEA_NOMBRE,
                                                LINEA_CODIGO = it.LINEA_CODIGO,
                                                SUBLINEA2_ID = it.SUBLINEA2_ID,
                                                SUBLINEA2_NOMBRE = it.SUBLINEA2_NOMBRE,
                                                PRC_1 = it.PRC_1,
                                                PRC_2 = it.PRC_2,
                                                PRC_3 = it.PRC_3,
                                                PRECIO_1 = it.PRECIO_1,
                                                PRECIO_2 = it.PRECIO_2,
                                                PRECIO_3 = it.PRECIO_3,
                                                MAYOR_A2 = it.MAYOR_A2,
                                                MAYOR_A3 = it.MAYOR_A3,
                                                COSTO_ULTIMO = it.COSTO_ULTIMO,
                                                PRECIO_ML = it.PRECIO_ML,
                                                PRECIO_SHOPEE = it.PRECIO_SHOPEE,
                                                PRECIO_AMAZON = it.PRECIO_AMAZON,
                                                DESCRIPCION_CHINA = it.DESCRIPCION_CHINA,
                                                COLOR_MEDIDA_CHINA = it.COLOR_MEDIDA_CHINA,
                                                THUMBNAIL = it.THUMBNAIL,
                                                CODIGO_SAT = it.CODIGO_SAT,
                                                DESCRIPCION_SAT = it.DESCRIPCION_SAT,
                                                UNIDADES_POR_CAJA = it.UNIDADES_POR_CAJA,
                                                GRUPO = it.GRUPO,
                                                ID = "",
                                                VARIATION_ID = ""
                                            )

                                        }
                                        actualizarTableLayout(items);
                                        txtSearch_consulta.setText("");
                                    } else {
                                        MensajesDialog.showMessage(this@Consulta_inventario, "Producto no encontrado");
                                        txtSearch_consulta.setText("");
                                    }

                                }
                            },
                            onError = {error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Consulta_inventario, "Errores: $error");
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Consulta_inventario, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(
                        this@Consulta_inventario,
                        "Ocurrió un error: ${e.message}"
                    );
                }
            }

        }else{
            MensajesDialog.showMessage(this,"Se debe de escanear ya sea un código o ubicación ");
        }
    }


    private fun buscarArea(search:String){
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout_Consulta_List)

        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }
        if(search.isNotEmpty()){
            try {
                val params= mapOf(
                    "p_busqueda" to search.uppercase(),
                )

                val headers= mapOf(
                    "Token" to GlobalUser.token.toString()
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/consulta/inventario/consolidado",
                            params=params,
                            dataClass = ListProducto::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNotEmpty()) {
                                        val itemsFiltrados = lista
                                            .filter { it.STOCK_CEDIS > 0 } // Filtra solo los que tengan STOCK_CEDIS > 0
                                            .map {
                                                ListProducto(
                                                    CODIGO = it.CODIGO,
                                                    DESCRIPCION = it.DESCRIPCION,
                                                    STOCK_CEDIS = it.STOCK_CEDIS,
                                                    FISICO_DISPONIBLE = it.FISICO_DISPONIBLE,
                                                    APARTADO_PICKING = it.APARTADO_PICKING ?: 0,
                                                    PISO_PICKING = it.PISO_PICKING ?:0,
                                                    STOCK_CEDIS_UBICACIONES = it.STOCK_CEDIS_UBICACIONES,
                                                    LINEA_ID = it.LINEA_ID ?:0,
                                                    LINEA_NOMBRE = it.LINEA_NOMBRE ?: "",
                                                    LINEA_CODIGO = it.LINEA_CODIGO ?: "",
                                                    SUBLINEA2_ID = it.SUBLINEA2_ID ?: 0,
                                                    SUBLINEA2_NOMBRE = it.SUBLINEA2_NOMBRE ?: "",
                                                    PRC_1 = it.PRC_1 ?: 0,
                                                    PRC_2 = it.PRC_2 ?: 0,
                                                    PRC_3 = it.PRC_3 ?: 0,
                                                    PRECIO_1 = it.PRECIO_1 ?: 0.0,
                                                    PRECIO_2 = it.PRECIO_2 ?: 0.0,
                                                    PRECIO_3 = it.PRECIO_3 ?: 0.0,
                                                    MAYOR_A2 = it.MAYOR_A2 ?: 0,
                                                    MAYOR_A3 = it.MAYOR_A3 ?: 0,
                                                    COSTO_ULTIMO = it.COSTO_ULTIMO ?: 0.0,
                                                    PRECIO_ML = it.PRECIO_ML ?: 0.0,
                                                    PRECIO_SHOPEE = it.PRECIO_SHOPEE ?: 0.0,
                                                    PRECIO_AMAZON = it.PRECIO_AMAZON ?: 0.0,
                                                    DESCRIPCION_CHINA = it.DESCRIPCION_CHINA ?: "",
                                                    COLOR_MEDIDA_CHINA = it.COLOR_MEDIDA_CHINA ?: "",
                                                    THUMBNAIL = it.THUMBNAIL ?: "",
                                                    CODIGO_SAT = it.CODIGO_SAT ?: "",
                                                    DESCRIPCION_SAT = it.DESCRIPCION_SAT ?: "",
                                                    UNIDADES_POR_CAJA = it.UNIDADES_POR_CAJA  ?: 0,
                                                    GRUPO = it.GRUPO ?: "",
                                                    ID = "",
                                                    VARIATION_ID = ""
                                                )
                                            }
                                        txtCodigosUnicos.isVisible=true;
                                        txtTotalStock.isVisible=true;
                                        lbCodigosUnicos.isVisible=true;
                                        lbTotalStock.isVisible=true;
                                        val totalCodigosUnicos = itemsFiltrados.map { it.CODIGO }.toSet().size
                                        txtCodigosUnicos.setText(totalCodigosUnicos.toString());
                                        val totalStockCedis = itemsFiltrados.sumOf { it.STOCK_CEDIS }
                                        txtTotalStock.setText(totalStockCedis.toString());
                                        if (itemsFiltrados.isNotEmpty()) {
                                            actualizarTableLayout(itemsFiltrados)
                                            txtSearch_consulta.setText("")
                                        } else {
                                            MensajesDialog.showMessage(this@Consulta_inventario, "Producto no encontrado con stock mayor a 0")
                                            txtSearch_consulta.setText("")
                                        }
                                    } else {
                                        MensajesDialog.showMessage(this@Consulta_inventario, "Área vacia")
                                        txtSearch_consulta.setText("")
                                    }

                                }
                            },
                            onError = {error ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(this@Consulta_inventario, "Errores: $error");
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(this@Consulta_inventario, "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(
                        this@Consulta_inventario,
                        "Ocurrió un error: ${e.message}"
                    );
                }
            }

        }else{
            MensajesDialog.showMessage(this,"Se debe de escanear ya sea un código o ubicación ");
        }
    }

    private fun actualizarTableLayout(items: List<ListProducto>) {
        try {
            val tableLayout = findViewById<TableLayout>(R.id.tableLayout_Consulta_List)

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

    private fun agregarFila(tableRow: TableRow, item: ListProducto) {
        try {
            val codigotxt = TextView(this).apply {
                text = item.CODIGO;
                gravity = Gravity.CENTER
            }

            val descripcionTextView = TextView(this).apply {
                text = item.DESCRIPCION
            }

            val existenciaTextView = TextView(this).apply {
                text = item.STOCK_CEDIS.toString()
                gravity = Gravity.CENTER
            }

            val UbicacionTextView = TextView(this).apply {
                text = item.STOCK_CEDIS_UBICACIONES.toString();
            }

            tableRow.addView(codigotxt);
            tableRow.addView(descripcionTextView);
            tableRow.addView(existenciaTextView);
            tableRow.addView(UbicacionTextView);
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    private fun actualizarFilaSeleccionada(item: ListProducto) {
        try {
            this@Consulta_inventario.codigo=item.CODIGO;
            this@Consulta_inventario.descripcion=item.DESCRIPCION;

            MensajesDialog.showMessage(this,"PRODUCTO:  ${this@Consulta_inventario.codigo}, ${this@Consulta_inventario.descripcion}")
        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }

    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_inventarios::class.java));
        //LogsEntradaSalida.logsPorModulo(this, lifecycleScope, "INVENTARIOS/CONSULTA/INVENTARIO", "SALIDA")
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