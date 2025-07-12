package com.example.slimmx.Vista

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import coil.load
import com.example.slimmx.GlobalConfig
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListImagen
import com.example.slimmx.ListImpresoras
import com.example.slimmx.MensajesDialog
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.R
import java.text.SimpleDateFormat
import java.util.Locale
import android.util.Base64
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.ImpresionEtiqueta.EtiquetaPrinter
import com.example.slimmx.ListaProductoUbicacion
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Environment
import android.os.Message
import androidx.core.content.ContextCompat
import com.example.slimmx.ListaItemsEnvioAsig
import com.example.slimmx.Picking.Envio.Asignacion.Asignacion_Envio_picking
import com.example.slimmx.ResultadoJsonSlimFolios_Packing
import com.example.slimmx.listaEtiquetaPackingMeli
import com.example.slimmx.listaRecomendaciones
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.Normalizer
import java.util.Date
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.log


class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> FragmentPage1()
            1 -> FragmentPage2()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }

    override fun getCount(): Int = 2

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Tareas"
            1 -> "Historial Pickeados"
            else -> null
        }
    }
}


class FragmentPage1 : Fragment(R.layout.fragment_first) {
    // Puedes agregar lógica adicional si es necesario
}

class FragmentPage2 : Fragment(R.layout.fragment_second) {
    // Puedes agregar lógica adicional si es necesario
}

class FragmentPage3 : Fragment(R.layout.fragment_f1) {

    interface OnBackgroundBlockerListener {
        fun showBackgroundBlocker()
        fun hideBackgroundBlocker()
    }

    private var backgroundBlockerListener: OnBackgroundBlockerListener? = null

    companion object {
        private const val ARG_SEARCH_QUERY = "search_query"
        private const val ARG_TIPO = "tipo"

        fun newInstance(query: String, tipo: String = "1"): FragmentPage3 {
            val fragment = FragmentPage3()
            val bundle = Bundle()
            bundle.putString(ARG_SEARCH_QUERY, query)
            bundle.putString(ARG_TIPO, tipo) // se añade ARG_TIPO al Bundle
            fragment.arguments = bundle
            return fragment
        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnBackgroundBlockerListener) {
            backgroundBlockerListener = context
        } else {
            throw RuntimeException("$context must implement OnBackgroundBlockerListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        backgroundBlockerListener = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        val searchQuery = arguments?.getString(ARG_SEARCH_QUERY)
        val tipo = arguments?.getString(ARG_TIPO)?.takeIf { it.isNotBlank() } ?: "1"

        searchQuery?.let {
            performSearch(it,tipo)
        }
        val lbCodigo:TextView= view.findViewById(R.id.txtCodigoF1busqueda);
        lbCodigo.setText(searchQuery);

        val buttonOcultar: Button = view.findViewById(R.id.buttonOcultarf1)
        buttonOcultar.setOnClickListener {
            backgroundBlockerListener?.hideBackgroundBlocker()
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commitNow()
        }

        parentFragmentManager.beginTransaction()
            .show(this)
            .commit()
        backgroundBlockerListener?.showBackgroundBlocker()

    }

    private fun performSearch(query: String, tipo: String) {
        if (query.isNotEmpty()) {
            try {
                val params = mapOf("codigo" to query)
                val headers = mapOf("Token" to GlobalUser.token.toString())

                val endpoint = if (tipo == "2") {
                    "/productos/ubicaciones/reserva/combo/pack"
                } else {
                    "/productos/ubicaciones"
                }


                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = endpoint,
                            params = params,
                            dataClass = ListaProductoUbicacion::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { lista ->
                                if (!isAdded || view == null) return@Pedir_datos_apis
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (lista.isNullOrEmpty()) {
                                        MensajesDialog.showMessage(
                                            requireContext(),
                                            "No se encontraron ubicaciones"
                                        )
                                        //return@Pedir_datos_apis
                                    }

                                    // Procesa los datos correctamente como JSON
                                    val jsonData = Gson().toJson(lista)
                                    val tableLayout: TableLayout =
                                        requireView().findViewById(R.id.tableLayout_f1)
                                    populateTableWithData(tableLayout, jsonData)
                                }
                            },
                            onError = {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    MensajesDialog.showMessage(
                                        requireContext(),
                                        "Error al obtener datos de la API"
                                    );
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(requireContext(), "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            } catch (e: Exception) {
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(requireContext(), "Ocurrió un error: ${e.message}")
                }
            }
        } else {
            MensajesDialog.showMessage(requireContext(), "Se debe de seleccionar un Código")
        }
    }

    private fun populateTableWithData(tableLayout: TableLayout, jsonData: String) {
        try {
            val childCount = tableLayout.childCount
            if (childCount > 1) {
                tableLayout.removeViews(1, childCount - 1)
            }

            // Convierte el JSON a una lista de objetos
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            val dataList: List<Map<String, Any>> = Gson().fromJson(jsonData, type)

            for (item in dataList) {
                val tableRow = TableRow(context).apply {
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                }

                val ubicacion = item["UBICACION"]?.toString() ?: "N/A"
                //val codigo = item["CODIGO"]?.toString() ?: "N/A"
                val box = item["BOX"]?.toString() ?: "N/A"
                val fisicoDisponible = item["FISICO_DISPONIBLE"]?.let {
                    when (it) {
                        is Double -> if (it % 1 == 0.0) it.toInt().toString() else it.toString()
                        is Float -> if (it % 1 == 0f) it.toInt().toString() else it.toString()
                        else -> it.toString()
                    }
                } ?: "0"


                val values = listOf(ubicacion,box, fisicoDisponible )

                values.forEachIndexed { index, value ->
                    val textView = TextView(context).apply {
                        text = value
                        textSize = 16f
                        setPadding(8, 8, 8, 35)
                        gravity = if (index == 1 || index == 2) Gravity.CENTER else Gravity.START
                    }
                    tableRow.addView(textView)
                }

                // Agrega la fila al TableLayout
                tableLayout.addView(tableRow)
            }
        } catch (e: Exception) {
            MensajesDialog.showMessage(requireContext(), "Ocurrió un error: ${e.message}")
        }
    }
}

class ImageFragment : Fragment(R.layout.fragment_ver_imagen) {
    interface OnBackgroundBlockerListener {
        fun showBackgroundBlocker()
        fun hideBackgroundBlocker()
    }
    private var backgroundBlockerListener: OnBackgroundBlockerListener? = null

    private lateinit var imageUrls: List<String>
    private var currentImageIndex: Int = 0
    private var codigo: String? = null

    companion object {
        fun newInstance(codigo: String): ImageFragment {
            val fragment = ImageFragment()
            val args = Bundle()
            args.putString("codigo", codigo)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnBackgroundBlockerListener) {
            backgroundBlockerListener = context
        } else {
            throw RuntimeException("$context must implement OnBackgroundBlockerListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        backgroundBlockerListener = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageView: ImageView = view.findViewById(R.id.imageViewProduct)

        arguments?.let {
            codigo = it.getString("codigo")
        }

        codigo?.let {
            try {
                val params = mapOf("codigo" to it)
                val headers= mapOf("Token" to GlobalUser.token.toString())

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        Pedir_datos_apis(
                            endpoint = "/productos/img/list",
                            params = params,
                            dataClass = ListImagen::class,
                            listaKey = "result",
                            headers = headers,
                            onSuccess = { response ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    val urls = response.mapNotNull { it.URL }
                                    if (urls.isNotEmpty()) {
                                        imageUrls = urls
                                        currentImageIndex = 0
                                        cargarImagen(imageView, imageUrls[currentImageIndex])

                                        imageView.setOnClickListener {
                                            currentImageIndex =
                                                (currentImageIndex + 1) % imageUrls.size
                                            cargarImagen(imageView, imageUrls[currentImageIndex])
                                        }
                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            "SIN DATOS",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            onError = {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Error al obtener datos de la API",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }catch (e: Exception) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MensajesDialog.showMessage(requireContext(), "Ocurrió un error: ${e.message}")
                        }
                    }
                }

            }catch (e: Exception){
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(requireContext(), "Ocurrió un error: ${e.message}");
                }
            }

        }


        val buttonOcultar: Button = view.findViewById(R.id.buttonOcultarVer_Imagen)
        buttonOcultar.setOnClickListener {
            backgroundBlockerListener?.hideBackgroundBlocker()
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commitNow()
        }
    }

    private fun cargarImagen(imageView: ImageView, url: String) {
        imageView.load("$url") {
            crossfade(true)
            size(dpToPx(250), dpToPx(250))
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}


class FragmentPageImpresionEtiquetas : Fragment(R.layout.fragment_impresion_etiquetas) {

    interface OnBackgroundBlockerListener {
        fun showBackgroundBlocker()
        fun hideBackgroundBlocker()
    }

    private var backgroundBlockerListener: OnBackgroundBlockerListener? = null

    private lateinit var cbSelectImpresora: AutoCompleteTextView
    private lateinit var txtImpresorasCantidad: EditText
    private var listaItemsAImprimir: List<ItemEtiqueta> = emptyList()
    private var etiquetaZplRecomendacion: String = ""
    private val PERMISO_ESCRITURA = 100

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnBackgroundBlockerListener) {
            backgroundBlockerListener = context
        } else {
            throw RuntimeException("$context must implement OnBackgroundBlockerListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        backgroundBlockerListener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cbSelectImpresora = view.findViewById(R.id.cbSelectImpresora)
        txtImpresorasCantidad = view.findViewById(R.id.txtImpresorasCantidad)

        val lbCodigo: TextView = view.findViewById(R.id.lb_codigo_impresion)
        lbCodigo.text = arguments?.getString(ARG_CODIGO) ?: ""

        val tipo = arguments?.getInt(ARG_TIPO) ?: 0

        if (tipo == 3) {
            view.findViewById<View>(R.id.lb_numero_etiquetas).visibility = View.GONE
            view.findViewById<View>(R.id.lb_numero).visibility = View.GONE
            view.findViewById<View>(R.id.txtImpresorasCantidad).visibility = View.GONE
        }

        val params = mapOf("format" to "json")
        val headers = mapOf("Token" to GlobalUser.token.toString())

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Pedir_datos_apis(
                    endpoint = "/configuracion/list/impresoras",
                    params = params,
                    dataClass = ListImpresoras::class,
                    listaKey = "result",
                    headers = headers,
                    onSuccess = { response ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            val ips = response.map { it.IP }
                            if (ips.isNotEmpty()) {
                                actualizarAutoCompleteComboBox(ips)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "SIN DATOS",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    onError = { error ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "Error al obtener datos de la API: $error",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            } catch (e: Exception) {
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(requireContext(), "Ocurrió un error: ${e.message}")
                }
            }
        }

        val buttonConfirmarImpresion: Button = view.findViewById(R.id.buttonConfirmar_impresion)
        buttonConfirmarImpresion.setOnClickListener {
            val tipo = arguments?.getInt(ARG_TIPO) ?: 0
            val folio = arguments?.getString(ARG_FOLIO) ?: ""
            val cantidadText = txtImpresorasCantidad.text.toString()
            val cantidad = cantidadText.toIntOrNull() ?: 0
            val ipImpresora = cbSelectImpresora.text.toString()
            val area=arguments?.getString(ARG_AREA)?:""

            if (ipImpresora.isEmpty()) {
                MensajesDialog.showMessage(requireContext(), "Debes de seleccionar una impresora")
                return@setOnClickListener
            }

            if (cantidad <= 0 && tipo!=3 ) {
                MensajesDialog.showMessage(requireContext(), "No puedes mandar 0 Etiquetas")
                return@setOnClickListener
            }

            if ((cantidad <= 0 || cantidad > 2) && tipo!=3 && area=="true") {
                MensajesDialog.showMessage(requireContext(), "No puedes imprimir mas de dos etiquetas, si el producto va a calidad")
                return@setOnClickListener
            }

            val etiquetaPrinter = EtiquetaPrinter()

            when (tipo) {
                1, 2 -> {
                    val codigo = arguments?.getString(ARG_CODIGO) ?: ""
                    val descripcion = arguments?.getString(ARG_DESCRIPCION) ?: ""
                    val fechasa = obtenerFechaActualBase64()
                    val foliosEncrip = Base64.encodeToString(folio.toByteArray(), Base64.DEFAULT)

                    val codigoZPL = if (tipo == 1) {
                        "^XA" +
                                "^CI28" +
                                "^LH0,0" +
                                "^FO50,18^BY2^BCN,50,N,N" +
                                "^FD${codigo}^FS" +
                                "^FT115,95^A0N,25,25^FH\\^FD${codigo}^FS" +
                                "^FB280,2,2" +
                                "^FO40,110^A0N,14,14^FD${descripcion}^FS" +
                                "^FO20,142^A0N,16,16^FD${GlobalUser.nombre}^FS" +
                                "^FT250,185^A0N,17,17^FD${fechasa}^FS" +
                                "^PQ${cantidad},0,1,Y" +
                                "^XZ"
                    } else {
                        "^XA" +
                                "^CI28" +
                                "^LH0,0" +
                                "^FO50,18^BY2^BCN,50,N,N" +
                                "^FD${codigo}^FS" +
                                "^FT115,95^A0N,25,25^FH\\^FD${codigo}^FS" +
                                "^FB280,2,2" +
                                "^FO40,110^A0N,14,14^FD${descripcion}^FS" +
                                "^FO20,142^A0N,16,16^FD${GlobalUser.nombre}^FS" +
                                "^FO25,160^A0N,17,17^FD${foliosEncrip}^FS" +
                                "^FT250,185^A0N,17,17^FD${fechasa}^FS" +
                                "^PQ${cantidad},0,1,Y" +
                                "^XZ"
                    }
                    lifecycleScope.launch {
                        val exitoImpresion = etiquetaPrinter.imprimirEtiqueta(requireContext(), ipImpresora, codigoZPL)

                        if (exitoImpresion) {
                            AlertDialog.Builder(requireContext())
                                .setMessage("Impresión completada correctamente")
                                .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                                .setCancelable(false)
                                .create()
                                .show()
                        } else {
                            AlertDialog.Builder(requireContext())
                                .setMessage("Ocurrió un error al imprimir")
                                .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                                .setCancelable(false)
                                .create()
                                .show()
                        }
                    }
                }

                3 -> {
                    if (listaItemsAImprimir.isEmpty()) {
                        MensajesDialog.showMessage(requireContext(), "No hay ítems para imprimir")
                        return@setOnClickListener
                    }

                    // Contamos la cantidad de etiquetas: 2 por cada ítem (una normal y una slim)
                    val cantidadEtiquetas = listaItemsAImprimir.size * 3

                    AlertDialog.Builder(requireContext())
                        .setTitle("Confirmar impresión")
                        .setMessage("Se van a imprimir $cantidadEtiquetas etiquetas.\n¿Deseas continuar?")
                        .setPositiveButton("Sí") { _, _ ->

                            lifecycleScope.launch {
                                val zplCompleto = StringBuilder()

                                for (item in listaItemsAImprimir) {
                                    // 1. Generar etiquetas necesarias
                                    val etiquetaSlim = generarEtiquetaResumenSlim(
                                        codigoSlim = item.codigo,
                                        descripcionCorta = item.descripcion,
                                        unidadesStr = item.cantidad.toString(),
                                        ubicaciones = "",
                                        tarima = item.tarima
                                    )

                                    val etiquetaRecomendacion = if (
                                        item.codigo.startsWith("CT21") ||
                                        item.codigo.startsWith("CO21") ||
                                        item.codigo.startsWith("EA21")
                                    ) {
                                        generarEtiquetaRecomendacion(
                                            codigo = item.codigo,
                                            folio = folio
                                        )
                                    } else {
                                        ""
                                    }

                                    val etiquetaTipo3 = generarEtiquetaTipo3(
                                        codigo = item.codigo,
                                        descripcion = item.descripcion,
                                        cantidad = 1, // Etiqueta individual por repetición
                                        folio = folio,
                                        envio = item.envio,
                                        color = item.color,
                                        sku = item.sku,
                                        title = item.title,
                                        inventory = item.inventory,
                                        tarima = item.tarima
                                    )

                                    val codigoSeparacion = """
                                            ^XA
                                            ^PW203
                                            ^LL101
                                            ^FO5,5
                                            ^A0N,20,20
                                            ^FB193,1,0,C,0
                                            ^FD****SEPARACION*****^FS
                                            ^XZ
                                        """.trimIndent()

                                    // 1. Slim una vez
                                    zplCompleto.appendLine(etiquetaSlim)

                                    // 2. Tipo3 + Recomendación (si aplica) por cada unidad
                                    repeat(item.cantidad) {
                                        zplCompleto.appendLine(etiquetaTipo3)
                                        if (etiquetaRecomendacion.isNotBlank()) {
                                            zplCompleto.appendLine(etiquetaRecomendacion)
                                        }
                                    }

                                    // 3. Separación una vez
                                    zplCompleto.appendLine(codigoSeparacion)
                                }

                                val exito = etiquetaPrinter.imprimirEtiqueta(requireContext(), ipImpresora, zplCompleto.toString())
                                delay(1000L);
                                if (exito) {
                                    AlertDialog.Builder(requireContext())
                                        .setMessage("Impresión completada correctamente")
                                        .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                                        .setCancelable(false)
                                        .create()
                                        .show()
                                } else {
                                    AlertDialog.Builder(requireContext())
                                        .setMessage("Hubo un error al imprimir.")
                                        .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                                        .setCancelable(false)
                                        .create()
                                        .show()
                                }


                                Log.d("zpl", zplCompleto.toString())
                            }
                        }


                        .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
                        .show()
                }

                4->{
                    val codigoZPL_verificacion =
                        " ^XA~TA000~JSN^LT0^MNW^MTT^PON^PMN^LH0,0^JMA^PR5,5~SD15^JUS^LRN^CI0^XZ" +
                                "            ^XA" +
                                "            ^MMT" +
                                "            ^PW392" +
                                "            ^LL0168" +
                                "            ^LS0" +
                                "            ^FO32,0^GFA,03072,03072,00016,:Z64:" +
                                "            eJztVj9r20AUf3eOkBGENGCRpcbCU9Bc6Fh1yO6ADzrExB/BQ0sypPXRyfhTCE9Cn0JLdw8teIggH8FD2iyh6Xt3uj9Sm61LoRf75MtP7/3e793TOwH8jREIISbeOi3LsnBLJs6Pj194eN4xz9rrSLbXcYcu4ug/dzfFveVyuVg4HAh3AcQww7m2Bn3FkOYOD+8Awntnz8m49Phv8TKsdwaHlC5G4wBghBJGyxuLc3ReSmcfbIHdOn4KILIBIr4EuKocP3FbesTZTodg8Cj3kzhQ9ODhqb+EmNV1vcVv46Kv8uf2KO49PTzt6Wv59VXa+NQYyjZuWAYq/8BujJ3yr3Lg8cOhEdhvzKXTD2wPnzz9JM/PD7AtprCFuyoZEL4YLb38I546fcre06/wso3vwY6G3waq7XF619Jnd7DRD5a/0W8KoMnfsG7p5+Xa6WM/4OTnieVP0LaUkcV3h3V1C0OLF9HnvLT7MEDxvflyMbJ4nmJ+SmvPqpdsi5tg+DnhWL8Of4X6mdt/eUr6U5MfBhesAlWFTX4K3FwJpw0/hIujbHQNBwaPcHdS7/kZVgHMDH2jv7AbqPOzPzJrHXfO2/g2eA4fqHkRwjN4177/Rxzr/963b9cH9o/ae/5bY9BZd3Fr79ffb/5fPz08+vauvjXeqX9wz0fDv+zgnfxCUIHPr5ucZ88ejQOu57wdwNDYQ195dk0mzmi+seso8d0DOzvPPHoAKdFBtOo3gbApnR5XVj/fqPbpzhB1urj8oQAJ/gjEFOcws/xAAbgjKBYCb6pdAZJ5VLoGTfioOroz+jkneV4CRAYXugc0DKr4ncLLOVwDfZqBhwsa6+eDTkbsfVvTP1Zk7/AYuQGp9439mOoSceNfIDflvsW/liY+PHunEFzSETSa4zpR4aVG37EQTEB84fT3TQJUflD6gZjElMCw0tpx+8dOuhBvpxNBQWT+Eb6S2GE0LqYHhIfzN0p5giGUUZpEqkMzwlmNOzjUFbAi+UV/JVPVYxUefEf8verBfLPB7Jd8Q+d3oXERk/1O50/bI3+h8oc4RvcB54WqQJ6OMYAiSZPc4rH4hjNmcacDByofCcb/eSyUPuhR/9twyWlIkHTKor4p/lGO67qy8iX1b1U/ZKtw1rR/0p8miXnBIOeT9usR5n9tf9K7UxZffhHibKLzP96Q789c78EZ4sBmNV3cWNvaVTiEHw1FwhVvmehKgCmbfdU0JoQoh9bQr1/dEDGOpPWPSeeGpgv8H//y+AWvt1I7:47CA" +
                                "            ^FO160,0^GFA,03840,03840,00020,:Z64:" +
                                "            eJzt1Tu6gkAMBeDQYJklZGuuxvWG7lbEc8J3cUA7EyvGwcdPMTMxCSLX+NnQiBBXfEhop93TVvs3TMV0FY8uU2xhe5m+m35jNthGOHajzbQpptEQ4WBs20wyoM7dtBlzIw+t3mt2sM2dN6TLGFDHwPpeZObLcjCR8C0tUXXNZjLtps5sEexwL/Ny4y/cMbPMndGURVljmflYle2pyww2HY1VhgPn1WXMeVmw7PL6L/fteYkh57O+kPc/NclEYUDHOBcb1x6O32FYbdXHrDO/vIy9SjUbV5GZYg7GiXblQy2UWz7LhO/RardV/86GyyvN0HvPlt2q0fJ5iZ51inOpMTceNyThWw5lKXTZNa5xjY/jCSdesdY=:C83C" +
                                "            ^FT188,159^A0B,28,28^FH^FDACCESORIOS^FS" +
                                "            ^FT325,145^A0B,15,14^FH^FDVERIFICADO${GlobalUser.nombre}^FS\n" +
                                "            ^PQ${cantidad},0,1,Y^XZ";

                        lifecycleScope.launch {
                            etiquetaPrinter.imprimirEtiqueta(requireContext(), ipImpresora, codigoZPL_verificacion)
                        }

                }

                else -> {
                    MensajesDialog.showMessage(requireContext(), "Tipo de etiqueta no soportado")
                }
            }
        }


        val buttonOcultar: Button = view.findViewById(R.id.buttonOcultarImpresionEtiquetas)
        buttonOcultar.setOnClickListener {
            backgroundBlockerListener?.hideBackgroundBlocker()
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commitNow()

            val lbCodigo: TextView = view.findViewById(R.id.lb_codigo_impresion)
            lbCodigo.text = ""
        }

        parentFragmentManager.beginTransaction()
            .show(this)
            .commit()
        backgroundBlockerListener?.showBackgroundBlocker()
    }

    private fun actualizarAutoCompleteComboBox(opciones: List<String>) {
        val adaptador = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            opciones
        )
        cbSelectImpresora.setAdapter(adaptador)
    }

    private fun obtenerFechaActualBase64(): String {
        val fechaActual = Calendar.getInstance().time
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaFormateada = sdf.format(fechaActual)
        return Base64.encodeToString(fechaFormateada.toByteArray(), Base64.DEFAULT)
    }

    private fun generarEtiquetaTipo3(
        codigo: String,
        descripcion: String,
        cantidad: Int,
        folio: String,
        envio: String,
        color: String,
        sku: String,
        title: String,
        inventory: String,
        tarima:String
    ): String {

        val tarimaReducida = if (tarima.length >= 2) {
            "${tarima.first()}${tarima.last()}"
        } else {
            tarima // Si tiene menos de 2 caracteres, usa lo que haya
        }

        return """
            ^XA
            ^CI28
            ^LH5,0
            ^FO20,155^A0N,14,14
            ^FB300,2,0^FDSKU:${sku}^FS
            ^FB300,2,2
            ^FO355,170^A0N,18,18^FD${tarimaReducida} ^FS
            ^FB280,2,2
            ^FO20,115^A0N,14,14^FD ${title}/${color} ^FS
            ^FO30,15^BY2^BCN,55,N,N
            ^FD ${inventory} ^FS
            ^FT128,100^A0N,22,22^FH\^FD ${inventory}^FS
            ^LH0,0
            ^FO360,20^A0B,20,20^FDID: ${envio}^FS
            ^PQ1,0,1,Y
            ^XZ
        """.trimIndent()

    }

    private fun generarEtiquetaResumenSlim(
        codigoSlim: String,
        descripcionCorta: String,
        unidadesStr: String,
        ubicaciones: String,
        tarima:String
    ): String {

        val tarimaReducida = if (tarima.length >= 2) {
            "${tarima.first()}${tarima.last()}"
        } else {
            tarima // Si tiene menos de 2 caracteres, usa lo que haya
        }

        return """
                    ^XA
            ^PW406
            ^LL350
            ^LS0
            ^CI28
            ^LH5,0
            ^FO270,80^GFA,1300,1300,13,,::::::::::::::R0F8,R0FC,Q018C,:Q03FE,Q07FF,Q04018,:,R07,P07JF,O03FI07E,N01FK07C,N0FM0F8,M03CM01C,M07O07,L01CO01C,L03Q0E,L06Q03,K01CQ018,K038R08,
            ,::::::::N07EK07E,0078I01E78I01E78J0F,00C1I03818I0381CI0C3,0181I0300CI0300EI0C18,0181I06006I06006I0418,0783I06006I06006I041E,0F83I04006I06002I061F,0983I04006I06002I06198,
            :0983I06006I06006I06198,0983I0600CI03006I06198,0983I0301CI0380CI06198,0D83I01C38I01C38I06198,0F83J0FFK0FFJ041F,0383J018K018J041C,0181W0418,00818V0C18,00FD8V0DF,00788V09E,
            J0CU018,J0CJ03KFCJ01,J06J038I01CJ03,J06J01K08J02,J03J018I018J06,J01J018I018J0C,J018J0CI03K0C,K0CJ06I06J018,K06J03I0CJ03,K03J01C038J06,K018J0IFK0C,L0CJ01F8J018,L06Q07,L038P0E,
            M0EO038,M07O0F,M01EM03C,N078L0F,N01F8J0FC,O03FE03FE,P01IFC,,::::::::::::::^FS
            ^FO10,16^BY2
            ^BCN,65,Y,N,N
            ^FD${codigoSlim}^FS
            ^FB280,2,2
            ^FO10,105^A0N,15,15^FD${descripcionCorta}^FS
            ^FO150,170^A0N,15,15^FD${GlobalUser.nombre} ^FS
            ^FO355,170^A0N,18,18^FD${tarimaReducida} ^FS
            ^FB280,2,2
            ^FO10,150^A0N,15,15^FD${unidadesStr} PZA ${ubicaciones}^FS
            ^FO10,200^A0N,10,10^FD                             ^FS  ; espacio en blanco al final
            ^PQ1,0,1,Y
            ^XZ
    """.trimIndent()
    }

    suspend fun generarEtiquetaRecomendacion(folio: String, codigo: String): String {
        var etiqueta = ""
        var titulo = ""
        var marca = ""
        var modelo = ""
        var recomendaciones = ""
        var recomendacion1 = ""
        var recomendacion2 = ""
        var recomendacion3 = ""
        var titulo1 = ""
        var titulo2 = ""

        try {
            val params = mapOf("codigo" to codigo, "folio" to folio)
            val headers = mapOf("Token" to GlobalUser.token.toString())

            etiqueta = ""

            Pedir_datos_apis(
                endpoint = "/tareas/envios/items/etiquetas/recomendacion",
                params = params,
                dataClass = listaRecomendaciones::class,
                listaKey = "result",
                headers = headers,
                onSuccess = { lista ->
                    if (lista.isNotEmpty()) {
                        val item = lista[0]
                        titulo = item.TITLE ?: ""
                        marca = item.MARCA ?: ""
                        modelo = item.MODELO ?: ""
                        recomendaciones = item.RECOMENDACIONES
                            ?: "NO UTILIZAR PARA UN USO DIFERENTE AL FABRICADO, DUDAS SOBRE USO CONTACTE AL PROVEEDOR"

                        val texto = if (recomendaciones.isEmpty()) {
                            "NO UTILIZAR PARA UN USO DIFERENTE AL FABRICADO, DUDAS SOBRE USO CONTACTE AL PROVEEDOR"
                        } else {
                            recomendaciones
                        }

                        recomendacion1 = texto.substring(0, minOf(20, texto.length))
                        recomendacion2 = if (texto.length > 20) texto.substring(20, minOf(72, texto.length)) else ""
                        recomendacion3 = if (texto.length > 72) texto.substring(72) else ""

                        titulo1 = titulo.substring(0, minOf(45, titulo.length))
                        titulo2 = if (titulo.length > 45) titulo.substring(45) else ""
                    }
                },
                onError = { error ->
                    etiqueta = "Error: $error"
                }
            )
        } catch (e: Exception) {
            return "Ocurrió un error: ${e.message}"
        }

        fun quitarAcentos(texto: String): String {
            val normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD)
            return normalizado.replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
        }

        val marcaSinAcentos = quitarAcentos(marca)
        val modeloSinAcentos = quitarAcentos(modelo)

        return """
        ^XA
        ^PW406
        ^LL203
        ^LS0
        ^CI28
        ^LH0,0
        ^FO16,05^A0N,18,18^FD${quitarAcentos(titulo1)}^FS
        ^FO16,25^A0N,18,18^FD${quitarAcentos(titulo2)}^FS
        ^FO16,45^A0N,16,16^FDHECHO EN CHINA, CONTENIDO: 1 PIEZA^FS
        ^FO16,60^A0N,10,10^FDMARCA: $marcaSinAcentos MODELO: $modeloSinAcentos^FS
        ^FO16,72^A0N,10,10^FDIMPORTADOR Y EXPORTADOR CROWN SA DE CV^FS
        ^FO16,84^A0N,10,10^FDDIRECCION: PINA 264, NUEVA SANTA MARIA^FS
        ^FO16,96^A0N,10,10^FDAZCAPOTZALCO, CDMX, C.P. 02800^FS
        ^FO16,108^A0N,10,10^FDimportadorcrown@gmail.com^FS
        ^FO16,120^A0N,10,10^FDR.F.C. IEC141020Q36^FS
        ^FO16,132^A0N,13,13^FDINSTRUCCION DE USO: $recomendacion1^FS
        ^FO16,147^A0N,13,13^FD$recomendacion2^FS
        ^FO16,162^A0N,13,13^FD$recomendacion3^FS
        ^PQ1,0,1,Y
        ^XZ
    """.trimIndent()
    }

    fun setListaParaImprimir(lista: List<ItemEtiqueta>) {
        listaItemsAImprimir = lista
    }

    data class ItemEtiqueta(
        val codigo: String,
        val descripcion: String,
        val cantidad: Int,
        val inventory: String,
        val title: String,
        val sku: String,
        val envio: String,
        val color: String,
        val tarima:String
    )

    companion object {
        const val ARG_CODIGO = "codigo"
        const val ARG_DESCRIPCION = "descripcion"
        const val ARG_TIPO = "tipo"
        const val ARG_FOLIO = "folio"
        const val ARG_AREA="area"

        fun newInstance(codigo: String, descripcion: String, tipo: Int, folio: String, area:String): FragmentPageImpresionEtiquetas {
            val fragment = FragmentPageImpresionEtiquetas()
            val args = Bundle()
            args.putString(ARG_CODIGO, codigo)
            args.putString(ARG_DESCRIPCION, descripcion)
            args.putInt(ARG_TIPO, tipo)
            args.putString(ARG_FOLIO, folio)
            args.putString(ARG_AREA, area)
            fragment.arguments = args
            return fragment
        }
    }

}


class FragmentPageEtiquetaBluetooth : Fragment(R.layout.fragment_etiquetas_bluetooth) {

    companion object {
        const val ARG_CODIGO = "codigo"
        const val ARG_DESCRIPCION = "descripcion"
        const val ARG_TIPO = "tipo"
        const val ARG_FOLIO = "folio"
        const val ARG_INVENTORY = "inventory"
        const val ARG_TITLE = "title"
        const val ARG_SKU = "sku"
        const val ARG_ENVIO = "envio"
        const val ARG_COLOR = "color"
        const val ARG_CANTIDAD = "cantidad"
        private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    interface OnBackgroundBlockerListener {
        fun showBackgroundBlocker()
        fun hideBackgroundBlocker()
    }

    private var backgroundBlockerListener: OnBackgroundBlockerListener? = null
    private lateinit var txtImpresorasCantidad: EditText
    private lateinit var cbSelectImpresora: AutoCompleteTextView
    private lateinit var adapter: ArrayAdapter<String>
    private val impresorasBluetooth = mutableListOf<String>()
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var isReceiverRegistered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    val nombre = it.name
                    val direccion = it.address
                    if (nombre != null && esImpresora(nombre) && !impresorasBluetooth.contains("$nombre ($direccion)")) {
                        impresorasBluetooth.add("$nombre ($direccion)")
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnBackgroundBlockerListener) {
            backgroundBlockerListener = context
        } else {
            throw RuntimeException("$context must implement OnBackgroundBlockerListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtImpresorasCantidad = view.findViewById(R.id.txtImpresorasCantidad)
        cbSelectImpresora = view.findViewById(R.id.cbSelectImpresora)

        val lbCodigo: TextView = view.findViewById(R.id.lb_codigo_impresion)
        lbCodigo.text = arguments?.getString(ARG_CODIGO).orEmpty()

        val tipo = arguments?.getInt(ARG_TIPO) ?: 0

        if (tipo == 3) {
            view.findViewById<View>(R.id.lb_numero_etiquetas).visibility = View.GONE
            view.findViewById<View>(R.id.lb_numero).visibility = View.GONE
            view.findViewById<View>(R.id.txtImpresorasCantidad).visibility = View.GONE
        }

        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, impresorasBluetooth)
        cbSelectImpresora.setAdapter(adapter)
        cbSelectImpresora.threshold = 1

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            Toast.makeText(requireContext(), "Bluetooth no soportado", Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
        }

        backgroundBlockerListener?.showBackgroundBlocker()

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val nombre = device.name
            val direccion = device.address
            if (nombre != null && esImpresora(nombre) && !impresorasBluetooth.contains("$nombre ($direccion)")) {
                impresorasBluetooth.add("$nombre ($direccion)")
            }
        }

        adapter.notifyDataSetChanged()

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        requireContext().registerReceiver(receiver, filter)
        isReceiverRegistered = true
        bluetoothAdapter?.startDiscovery()

        view.findViewById<Button>(R.id.buttonConfirmar_impresion).setOnClickListener {
            mandarImprimir()
        }

        view.findViewById<Button>(R.id.buttonOcultarImpresionEtiquetas).setOnClickListener {
            backgroundBlockerListener?.hideBackgroundBlocker()
            parentFragmentManager.beginTransaction().remove(this).commitNow()
        }

        parentFragmentManager.beginTransaction().show(this).commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isReceiverRegistered) {
            requireContext().unregisterReceiver(receiver)
            isReceiverRegistered = false
        }
        bluetoothAdapter?.cancelDiscovery()
    }

    data class ItemEtiqueta(
        val codigo: String,
        val descripcion: String,
        val cantidad: Int,
        val inventory: String,
        val title: String,
        val sku: String,
        val envio: String,
        val color: String
    )

    private var listaItemsAImprimir: List<ItemEtiqueta> = emptyList()

    fun setListaParaImprimir(lista: List<ItemEtiqueta>) {
        listaItemsAImprimir = lista
    }

    private fun mandarImprimir() {
        val tipo = arguments?.getInt(ARG_TIPO) ?: 0
        val folio = arguments?.getString(ARG_FOLIO).orEmpty()
        val fecha = obtenerFechaActualBase64()
        val folioBase64 = Base64.encodeToString(folio.toByteArray(), Base64.NO_WRAP)
        val nombreUsuario = GlobalUser.nombre ?: "USUARIO"

        val impresoraSeleccionada = cbSelectImpresora.text.toString()
        if (impresoraSeleccionada.isEmpty()) {
            MensajesDialog.showMessage(requireContext(), "Seleccione una impresora")
            return
        }

        val totalEtiquetas = when (tipo) {
            3 -> listaItemsAImprimir.sumOf { it.cantidad }
            1, 2 -> 1
            else -> 0
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar impresión")
            .setMessage("Se enviarán $totalEtiquetas etiqueta(s) a imprimir. ¿Desea continuar?")
            .setPositiveButton("Sí") { _, _ ->
                val direccion = extraerDireccionBluetooth(impresoraSeleccionada)
                val device = bluetoothAdapter?.getRemoteDevice(direccion)

                if (device == null) {
                    MensajesDialog.showMessage(requireContext(), "Impresora no encontrada")
                    return@setPositiveButton
                }

                Thread {
                    try {
                        bluetoothAdapter?.cancelDiscovery()
                        val socket = device.createRfcommSocketToServiceRecord(MY_UUID)
                        socket.connect()
                        val outputStream = socket.outputStream

                        when (tipo) {
                            3 -> {
                                for (item in listaItemsAImprimir) {
                                    val etiquetaResumen = generarEtiquetaResumenSlim(
                                        codigoSlim = item.codigo,
                                        descripcionCorta = item.descripcion,
                                        unidadesStr = item.cantidad.toString(),
                                        ubicaciones = "" // O el campo que represente ubicaciones
                                    )
                                    outputStream.write(etiquetaResumen.toByteArray(Charsets.UTF_8))
                                    outputStream.flush()

                                    val zpl = generarEtiquetaTipo3(
                                        codigo = item.codigo,
                                        descripcion = item.descripcion,
                                        cantidad = item.cantidad,
                                        folio = folio,
                                        envio = item.envio,
                                        color = item.color,
                                        sku = item.sku,
                                        title = item.title,
                                        inventory = item.inventory
                                    )
                                    outputStream.write(zpl.toByteArray(Charsets.UTF_8))
                                    outputStream.flush()

                                    val zpl_separacion="^XA" +
                                            "^PW203" +
                                            "^LL101" +
                                            "^FO5,5" +
                                            "^A0N,20,20" +
                                            "^FB193,1,0,C,0" +
                                            "^FD****SEPARACION*****^FS" +
                                            "^XZ";
                                    outputStream.write(zpl_separacion.toByteArray(Charsets.UTF_8))
                                    outputStream.flush()
                                }
                            }
                            1, 2 -> {
                                val item = listaItemsAImprimir.firstOrNull() ?: ItemEtiqueta(
                                    codigo = arguments?.getString(ARG_CODIGO).orEmpty(),
                                    descripcion = arguments?.getString(ARG_DESCRIPCION).orEmpty(),
                                    cantidad = arguments?.getString(ARG_CANTIDAD)?.toIntOrNull() ?: 1,
                                    inventory = arguments?.getString(ARG_INVENTORY).orEmpty(),
                                    title = arguments?.getString(ARG_TITLE).orEmpty(),
                                    sku = arguments?.getString(ARG_SKU).orEmpty(),
                                    envio = arguments?.getString(ARG_ENVIO).orEmpty(),
                                    color = arguments?.getString(ARG_COLOR).orEmpty()
                                )

                                val zpl = generarEtiquetaTipo1y2(
                                    codigo = item.codigo,
                                    descripcion = item.descripcion,
                                    fecha = fecha,
                                    nombreUsuario = nombreUsuario,
                                    folioBase64 = folioBase64
                                )
                                outputStream.write(zpl.toByteArray(Charsets.UTF_8))
                                outputStream.flush()
                            }
                        }

                        outputStream.close()
                        socket.close()

                        requireActivity().runOnUiThread {
                            MensajesDialog.showMessage(requireContext(), "Impresión enviada.")
                        }

                    } catch (e: IOException) {
                        requireActivity().runOnUiThread {
                            MensajesDialog.showMessage(requireContext(), "Error al imprimir: ${e.message}")
                        }
                    }
                }.start()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun extraerDireccionBluetooth(nombreYDireccion: String): String {
        val regex = "\\((.*?)\\)".toRegex()
        return regex.find(nombreYDireccion)?.groupValues?.get(1) ?: ""
    }

    private fun esImpresora(nombre: String): Boolean {
        return true // Aquí puedes filtrar nombres si deseas
    }

    private fun obtenerFechaActualBase64(): String {
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fecha = formato.format(Date())
        return Base64.encodeToString(fecha.toByteArray(), Base64.NO_WRAP)
    }

    private fun generarEtiquetaTipo1y2(
        codigo: String,
        descripcion: String,
        fecha: String,
        nombreUsuario: String,
        folioBase64: String
    ): String {
        return """
        ^XA
        ^CI28
        ^LH0,0
        ^FO130,18^BY2^BCN,50,N,N
        ^FD${codigo}^FS
        ^FT210,95^A0N,25,25^FH\^FD${codigo}^FS
        ^FB280,2,2
        ^FO130,110^A0N,14,14^FD${descripcion}^FS
        ^FO130,142^A0N,16,16^FD${nombreUsuario}^FS
        ^FO105,159^A0N,17,17^FD${folioBase64}^FS
        ^FT280,187^A0N,17,17^FD${fecha}^FS
        ^PQ1,0,1,Y
        ^XZ
        """.trimIndent()


    }

    private fun generarEtiquetaTipo3(
        codigo: String,
        descripcion: String,
        cantidad: Int,
        folio: String,
        envio: String,
        color: String,
        sku: String,
        title: String,
        inventory: String
    ): String {
        return """
        ^XA
        ^CI28
        ^LH5,0
        ^FO20,155^A0N,14,14
        ^FB300,2,0^FDSKU:${sku}^FS
        ^FB300,2,2
        ^FO20,140^A0N,10,10^FD${color} ^FS
        ^FB280,2,2
        ^FO20,115^A0N,14,14^FD ${title} ^FS
        ^FO30,15^BY2^BCN,55,N,N
        ^FD ${inventory} ^FS
        ^FT128,100^A0N,22,22^FH\^FD ${inventory}^FS
        ^LH0,0
        ^FO360,20^A0B,20,20^FDID: ${envio}^FS
        ^PQ${cantidad},0,1,Y
        ^XZ
        """.trimIndent()
    }

    private fun generarEtiquetaResumenSlim(
        codigoSlim: String,
        descripcionCorta: String,
        unidadesStr: String,
        ubicaciones: String
    ): String {
        return """
                    ^XA
            ^PW406
            ^LL350
            ^LS0
            ^CI28
            ^LH5,0
            ^FO270,80^GFA,1300,1300,13,,::::::::::::::R0F8,R0FC,Q018C,:Q03FE,Q07FF,Q04018,:,R07,P07JF,O03FI07E,N01FK07C,N0FM0F8,M03CM01C,M07O07,L01CO01C,L03Q0E,L06Q03,K01CQ018,K038R08,
            ,::::::::N07EK07E,0078I01E78I01E78J0F,00C1I03818I0381CI0C3,0181I0300CI0300EI0C18,0181I06006I06006I0418,0783I06006I06006I041E,0F83I04006I06002I061F,0983I04006I06002I06198,
            :0983I06006I06006I06198,0983I0600CI03006I06198,0983I0301CI0380CI06198,0D83I01C38I01C38I06198,0F83J0FFK0FFJ041F,0383J018K018J041C,0181W0418,00818V0C18,00FD8V0DF,00788V09E,
            J0CU018,J0CJ03KFCJ01,J06J038I01CJ03,J06J01K08J02,J03J018I018J06,J01J018I018J0C,J018J0CI03K0C,K0CJ06I06J018,K06J03I0CJ03,K03J01C038J06,K018J0IFK0C,L0CJ01F8J018,L06Q07,L038P0E,
            M0EO038,M07O0F,M01EM03C,N078L0F,N01F8J0FC,O03FE03FE,P01IFC,,::::::::::::::^FS
            ^FO10,16^BY2
            ^BCN,65,Y,N,N
            ^FD${codigoSlim}^FS
            ^FB280,2,2
            ^FO10,105^A0N,15,15^FD${descripcionCorta}^FS
            ^FB280,2,2
            ^FO10,150^A0N,15,15^FD${unidadesStr} PZA^FS
            
            ^FO10,200^A0N,10,10^FD                             ^FS  ; espacio en blanco al final
            ^PQ1,0,1,Y
            ^XZ
    """.trimIndent()
    }


}

class FragmentPageEtiquetaRecomendacion : Fragment(R.layout.fragment_impresion_recomendaciones) {

    companion object{
        const val ARG_CODIGO="codigo";
        const val ARG_FOLIO="folio";
        const val ARG_CANTIDAD_MAX="cantidad_max"

        fun newInstance(codigo: String, folio: String, cantidad_max:Int): FragmentPageEtiquetaRecomendacion {
            val fragment = FragmentPageEtiquetaRecomendacion()
            val args = Bundle().apply {
                putString(ARG_CODIGO, codigo)
                putString(ARG_FOLIO, folio)
                putInt(ARG_CANTIDAD_MAX, cantidad_max)
            }
            fragment.arguments = args
            return fragment
        }
    }

    interface OnBackgroundBlockerListener {
        fun showBackgroundBlocker()
        fun hideBackgroundBlocker()
    }

    private var backgroundBlockerListener: OnBackgroundBlockerListener? = null
    private lateinit var txtImpresorasCantidad: EditText
    private lateinit var cbSelectImpresora: AutoCompleteTextView
    private var titulo: String = ""
    private var marca: String = ""
    private var modelo: String = ""
    private var recomendaciones: String = ""
    private var recomendacion1: String = ""
    private var recomendacion2: String = ""
    private var recomendacion3: String = ""
    private var titulo1: String=""
    private var titulo2:String=""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnBackgroundBlockerListener) {
            backgroundBlockerListener = context
        } else {
            throw RuntimeException("$context must implement OnBackgroundBlockerListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        backgroundBlockerListener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtImpresorasCantidad = view.findViewById(R.id.txtImpresorasCantidad)

        val lbCodigo: TextView = view.findViewById(R.id.lb_codigo_impresion)
        lbCodigo.text = arguments?.getString(ARG_CODIGO).orEmpty()

        val lbCantidadMaxima: TextView = view.findViewById(R.id.lb_max_impresiona)
        val cantidadMax = arguments?.getInt(ARG_CANTIDAD_MAX) ?: 0
        lbCantidadMaxima.text = cantidadMax.toString()

        obtenerDatos();

        cbSelectImpresora = view.findViewById(R.id.cbSelectImpresora)
        txtImpresorasCantidad = view.findViewById(R.id.txtImpresorasCantidad)


        val params = mapOf("format" to "json")
        val headers = mapOf("Token" to GlobalUser.token.toString())

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Pedir_datos_apis(
                    endpoint = "/configuracion/list/impresoras",
                    params = params,
                    dataClass = ListImpresoras::class,
                    listaKey = "result",
                    headers = headers,
                    onSuccess = { response ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            val ips = response.map { it.IP }
                            if (ips.isNotEmpty()) {
                                actualizarAutoCompleteComboBox(ips)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "SIN DATOS",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    onError = { error ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "Error al obtener datos de la API: $error",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            } catch (e: Exception) {
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(requireContext(), "Ocurrió un error: ${e.message}")
                }
            }
        }

        view.findViewById<Button>(R.id.buttonConfirmar_impresion).setOnClickListener {
            val cantidad = txtImpresorasCantidad.text.toString().toIntOrNull() ?: 1
            val cantidad_max =arguments?.getInt(ARG_CANTIDAD_MAX)

            if (cantidad<=cantidad_max.toString().toInt() && cantidad>0){

                val marcaSinAcentos = quitarAcentos(marca)
                val modeloSinAcentos = quitarAcentos(modelo)

                val zpl = """
                        ^XA
                        ^PW406
                        ^LL203
                        ^LS0
                        ^CI28
                        ^LH0,0
                        ^FO16,05^A0N,18,18^FD${quitarAcentos(titulo1)}^FS
                        ^FO16,25^A0N,18,18^FD${quitarAcentos(titulo2)}^FS
                        ^FO16,45^A0N,16,16^FDHECHO EN CHINA, CONTENIDO: 1 PIEZA^FS
                        ^FO16,60^A0N,10,10^FDMARCA: $marcaSinAcentos MODELO: $modeloSinAcentos^FS
                        ^FO16,72^A0N,10,10^FDIMPORTADOR Y EXPORTADOR CROWN SA DE CV^FS
                        ^FO16,84^A0N,10,10^FDDIRECCION: PINA 264, NUEVA SANTA MARIA^FS
                        ^FO16,96^A0N,10,10^FDAZCAPOTZALCO, CDMX, C.P. 02800^FS
                        ^FO16,108^A0N,10,10^FDimportadorcrown@gmail.com^FS
                        ^FO16,120^A0N,10,10^FDR.F.C. IEC141020Q36^FS
                        ^FO16,132^A0N,13,13^FDINSTRUCCION DE USO: $recomendacion1^FS
                        ^FO16,147^A0N,13,13^FD$recomendacion2^FS
                        ^FO16,162^A0N,13,13^FD$recomendacion3^FS
                        ^PQ$cantidad,0,1,Y
                        ^XZ
                    """.trimIndent();
                val etiquetaPrinter = EtiquetaPrinter()


                lifecycleScope.launch {

                    var exitoPrimario = false
                    try {
                        exitoPrimario = etiquetaPrinter.imprimirEtiqueta(requireContext(), cbSelectImpresora.text.toString(), zpl)
                    } catch (e: Exception) {
                        // No mostramos el error, simplemente fallamos silenciosamente
                    }

                    if (exitoPrimario) {
                        AlertDialog.Builder(requireContext())
                            .setMessage("Impresión completada correctamente en IP ${cbSelectImpresora.text.toString()}")
                            .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                            .setCancelable(false)
                            .create()
                            .show()
                    }else {
                            AlertDialog.Builder(requireContext())
                                .setMessage("Error al imprimir")
                                .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                                .setCancelable(false)
                                .create()
                                .show()
                        }
                    }

            }else{
                MensajesDialog.showMessage(requireContext(), "No puedes imprimir etiquetas de más o imprimir 0")
            }

        }

        val buttonOcultar: Button = view.findViewById(R.id.buttonOcultarImpresionEtiquetas)
        buttonOcultar.setOnClickListener {
            backgroundBlockerListener?.hideBackgroundBlocker()
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commitNow()

            val lbCodigo: TextView = view.findViewById(R.id.lb_codigo_impresion)
            lbCodigo.text = ""
        }

        parentFragmentManager.beginTransaction()
            .show(this)
            .commit()
        backgroundBlockerListener?.showBackgroundBlocker()

    }

    private fun actualizarAutoCompleteComboBox(opciones: List<String>) {
        val adaptador = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            opciones
        )
        cbSelectImpresora.setAdapter(adaptador)
    }

    private fun obtenerDatos(){
        try {
            val params= mapOf(
                "codigo" to arguments?.getString(ARG_CODIGO).orEmpty(),
                "folio" to  arguments?.getString(ARG_FOLIO).orEmpty()
            )
            val headers = mapOf("Token" to GlobalUser.token.toString());

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/tareas/envios/items/etiquetas/recomendacion",
                        params=params,
                        dataClass = listaRecomendaciones::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val item = lista[0]
                                    titulo = item.TITLE ?: ""
                                    marca = item.MARCA ?: ""
                                    modelo = item.MODELO ?: ""
                                    recomendaciones = item.RECOMENDACIONES ?: "NO UTILIZAR PARA UN USO DIFERENTE AL FABRICADO, DUDAS SOBRE USO CONTACTE AL PROVEEDOR"

                                    val texto = if (item.RECOMENDACIONES.isNullOrEmpty()) {
                                        "NO UTILIZAR PARA UN USO DIFERENTE AL FABRICADO, DUDAS SOBRE USO CONTACTE AL PROVEEDOR"
                                    } else {
                                        item.RECOMENDACIONES
                                    }
                                    //val texto = item.RECOMENDACIONES ?: "NO UTILIZAR PARA UN USO DIFERENTE AL FABRICADO, DUDAS SOBRE USO CONTACTE AL PROVEEDOR"
                                    recomendacion1 = texto.substring(0, minOf(20, texto.length))
                                    recomendacion2 = if (texto.length > 20) texto.substring(20, minOf(72, texto.length)) else ""
                                    recomendacion3 = if (texto.length > 72) texto.substring(72) else ""

                                    titulo1=titulo.substring(0, minOf(45, titulo.length));
                                    titulo2=if (titulo.length > 45) titulo.substring(45) else ""
                                } else {
                                    MensajesDialog.showMessage(requireContext(), "Lista vacía")
                                }
                            }
                        },
                        onError = { error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(requireContext(), "Error: $error")
                            }
                        } )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(requireContext(), "Ocurrió un error: ${e.message}")
                    }
                }
            }
        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(
                    requireContext(),
                    "Ocurrió un error: ${e.message}"
                );
            }
        }

    }

    fun quitarAcentos(texto: String): String {
        val normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD)
        return normalizado.replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
    }

}



class FragmentPageImpresionEtiquetas_Verificacion : Fragment(R.layout.fragment_impresion_etiquetas_verificacion) {

    interface OnBackgroundBlockerListener {
        fun showBackgroundBlocker()
        fun hideBackgroundBlocker()
    }

    private var backgroundBlockerListener: OnBackgroundBlockerListener? = null

    private lateinit var cbSelectImpresora: AutoCompleteTextView
    private lateinit var txtImpresorasCantidad: EditText

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnBackgroundBlockerListener) {
            backgroundBlockerListener = context
        } else {
            throw RuntimeException("$context must implement OnBackgroundBlockerListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        backgroundBlockerListener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cbSelectImpresora = view.findViewById(R.id.cbSelectImpresora)
        txtImpresorasCantidad = view.findViewById(R.id.txtImpresorasCantidad)


        val params = mapOf("format" to "json")
        val headers = mapOf("Token" to GlobalUser.token.toString())

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Pedir_datos_apis(
                    endpoint = "/configuracion/list/impresoras",
                    params = params,
                    dataClass = ListImpresoras::class,
                    listaKey = "result",
                    headers = headers,
                    onSuccess = { response ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            val ips = response.map { it.IP }
                            if (ips.isNotEmpty()) {
                                actualizarAutoCompleteComboBox(ips)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "SIN DATOS",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    onError = { error ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "Error al obtener datos de la API: $error",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            } catch (e: Exception) {
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(requireContext(), "Ocurrió un error: ${e.message}")
                }
            }
        }

        val buttonConfirmarImpresion: Button = view.findViewById(R.id.buttonConfirmar_impresion)
        buttonConfirmarImpresion.setOnClickListener {
            val cantidadText = txtImpresorasCantidad.text.toString()
            val ipImpresora = cbSelectImpresora.text.toString()

            if (ipImpresora.isEmpty()) {
                MensajesDialog.showMessage(requireContext(), "Debes de seleccionar una impresora")
                return@setOnClickListener
            }

            if (cantidadText.toString().toInt() <= 0 ) {
                MensajesDialog.showMessage(requireContext(), "No puedes mandar 0 Etiquetas")
                return@setOnClickListener
            }
            val calendar = Calendar.getInstance()
            val mesNumero = String.format("%02d", calendar.get(Calendar.MONTH) + 1)

            val etiquetaPrinter = EtiquetaPrinter()

            val codigoZPL_verificacion =
                " ^XA~TA000~JSN^LT0^MNW^MTT^PON^PMN^LH0,0^JMA^PR5,5~SD15^JUS^LRN^CI0^XZ" +
                        "^XA" +
                        "^MMT" +
                        "^PW392" +
                        "^LL0168" +
                        "^LS0" +
                        "^FO32,0^GFA,03072,03072,00016,:Z64:" +
                        "eJztVj9r20AUf3eOkBGENGCRpcbCU9Bc6Fh1yO6ADzrExB/BQ0sypPXRyfhTCE9Cn0JLdw8teIggH8FD2iyh6Xt3uj9Sm61LoRf75MtP7/3e793TOwH8jREIISbeOi3LsnBLJs6Pj194eN4xz9rrSLbXcYcu4ug/dzfFveVyuVg4HAh3AcQww7m2Bn3FkOYOD+8Awntnz8m49Phv8TKsdwaHlC5G4wBghBJGyxuLc3ReSmcfbIHdOn4KILIBIr4EuKocP3FbesTZTodg8Cj3kzhQ9ODhqb+EmNV1vcVv46Kv8uf2KO49PTzt6Wv59VXa+NQYyjZuWAYq/8BujJ3yr3Lg8cOhEdhvzKXTD2wPnzz9JM/PD7AtprCFuyoZEL4YLb38I546fcre06/wso3vwY6G3waq7XF619Jnd7DRD5a/0W8KoMnfsG7p5+Xa6WM/4OTnieVP0LaUkcV3h3V1C0OLF9HnvLT7MEDxvflyMbJ4nmJ+SmvPqpdsi5tg+DnhWL8Of4X6mdt/eUr6U5MfBhesAlWFTX4K3FwJpw0/hIujbHQNBwaPcHdS7/kZVgHMDH2jv7AbqPOzPzJrHXfO2/g2eA4fqHkRwjN4177/Rxzr/963b9cH9o/ae/5bY9BZd3Fr79ffb/5fPz08+vauvjXeqX9wz0fDv+zgnfxCUIHPr5ucZ88ejQOu57wdwNDYQ195dk0mzmi+seso8d0DOzvPPHoAKdFBtOo3gbApnR5XVj/fqPbpzhB1urj8oQAJ/gjEFOcws/xAAbgjKBYCb6pdAZJ5VLoGTfioOroz+jkneV4CRAYXugc0DKr4ncLLOVwDfZqBhwsa6+eDTkbsfVvTP1Zk7/AYuQGp9439mOoSceNfIDflvsW/liY+PHunEFzSETSa4zpR4aVG37EQTEB84fT3TQJUflD6gZjElMCw0tpx+8dOuhBvpxNBQWT+Eb6S2GE0LqYHhIfzN0p5giGUUZpEqkMzwlmNOzjUFbAi+UV/JVPVYxUefEf8verBfLPB7Jd8Q+d3oXERk/1O50/bI3+h8oc4RvcB54WqQJ6OMYAiSZPc4rH4hjNmcacDByofCcb/eSyUPuhR/9twyWlIkHTKor4p/lGO67qy8iX1b1U/ZKtw1rR/0p8miXnBIOeT9usR5n9tf9K7UxZffhHibKLzP96Q789c78EZ4sBmNV3cWNvaVTiEHw1FwhVvmehKgCmbfdU0JoQoh9bQr1/dEDGOpPWPSeeGpgv8H//y+AWvt1I7:47CA" +
                        "^FO160,0^GFA,03840,03840,00020,:Z64:" +
                        "eJzt1Tu6gkAMBeDQYJklZGuuxvWG7lbEc8J3cUA7EyvGwcdPMTMxCSLX+NnQiBBXfEhop93TVvs3TMV0FY8uU2xhe5m+m35jNthGOHajzbQpptEQ4WBs20wyoM7dtBlzIw+t3mt2sM2dN6TLGFDHwPpeZObLcjCR8C0tUXXNZjLtps5sEexwL/Ny4y/cMbPMndGURVljmflYle2pyww2HY1VhgPn1WXMeVmw7PL6L/fteYkh57O+kPc/NclEYUDHOBcb1x6O32FYbdXHrDO/vIy9SjUbV5GZYg7GiXblQy2UWz7LhO/RardV/86GyyvN0HvPlt2q0fJ5iZ51inOpMTceNyThWw5lKXTZNa5xjY/jCSdesdY=:C83C" +
                        "^FT188,159^A0B,28,28^FH^FDACCESORIOS^FS" +
                        "^FT30,90^A0B,18,18^FH^FD${GlobalUser.nombre}^FS"+
                        "^FT345,180^A0B,25,25^FH^FDVERIFICADO/QA${mesNumero}^FS\n" +
                        "^PQ${cantidadText},0,1,Y^XZ";

            /*lifecycleScope.launch {
                etiquetaPrinter.imprimirEtiqueta(requireContext(), ipImpresora, codigoZPL_verificacion)
            }*/

            lifecycleScope.launch {
                val exitoImpresion = etiquetaPrinter.imprimirEtiqueta(requireContext(), ipImpresora, codigoZPL_verificacion)

                if (exitoImpresion) {
                    AlertDialog.Builder(requireContext())
                        .setMessage("Impresión completada correctamente")
                        .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                        .setCancelable(false)
                        .create()
                        .show()
                } else {
                    AlertDialog.Builder(requireContext())
                        .setMessage("Ocurrió un error al imprimir")
                        .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                        .setCancelable(false)
                        .create()
                        .show()
                }
            }
        }


        val buttonOcultar: Button = view.findViewById(R.id.buttonOcultarImpresionEtiquetas)
        buttonOcultar.setOnClickListener {
            backgroundBlockerListener?.hideBackgroundBlocker()
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commitNow()
        }

        parentFragmentManager.beginTransaction()
            .show(this)
            .commit()
        backgroundBlockerListener?.showBackgroundBlocker()
    }

    private fun actualizarAutoCompleteComboBox(opciones: List<String>) {
        val adaptador = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            opciones
        )
        cbSelectImpresora.setAdapter(adaptador)
    }

    companion object {

        fun newInstance(): FragmentPageImpresionEtiquetas_Verificacion {
            val fragment = FragmentPageImpresionEtiquetas_Verificacion()
            return fragment
        }
    }

}



class FragmentPageEtiquetaMeli : Fragment(R.layout.fragment_impresion_meli) {

    companion object{
        const val ARG_CODIGO="codigo";
        const val ARG_FOLIO="folio";
        const val ARG_CANTIDAD_MAX="cantidad_max"

        fun newInstance(codigo: String, folio: String, cantidad_max:Int): FragmentPageEtiquetaMeli {
            val fragment = FragmentPageEtiquetaMeli()
            val args = Bundle().apply {
                putString(ARG_CODIGO, codigo)
                putString(ARG_FOLIO, folio)
                putInt(ARG_CANTIDAD_MAX, cantidad_max)
            }
            fragment.arguments = args
            return fragment
        }
    }

    interface OnBackgroundBlockerListener {
        fun showBackgroundBlocker()
        fun hideBackgroundBlocker()
    }

    private var backgroundBlockerListener: OnBackgroundBlockerListener? = null
    private lateinit var txtImpresorasCantidad: EditText
    private lateinit var cbSelectImpresora: AutoCompleteTextView
    private lateinit var buttonOcultar: Button
    private var title: String = ""
    private var envio: String = ""
    private var color: String = ""
    private var sku: String = ""
    private var inventory: String=""
    private var cantidad_datos:Int=0;

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnBackgroundBlockerListener) {
            backgroundBlockerListener = context
        } else {
            throw RuntimeException("$context must implement OnBackgroundBlockerListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        backgroundBlockerListener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtImpresorasCantidad = view.findViewById(R.id.txtImpresorasCantidad)

        val lbCodigo: TextView = view.findViewById(R.id.lb_codigo_impresion)
        lbCodigo.text = arguments?.getString(ARG_CODIGO).orEmpty()

        val lbCantidadMaxima: TextView = view.findViewById(R.id.lb_max_impresiona)
        val cantidadMax = arguments?.getInt(ARG_CANTIDAD_MAX) ?: 0
        lbCantidadMaxima.text = cantidadMax.toString()

        obtenerDatos();

        cbSelectImpresora = view.findViewById(R.id.cbSelectImpresora)
        txtImpresorasCantidad = view.findViewById(R.id.txtImpresorasCantidad)


        val params = mapOf("format" to "json")
        val headers = mapOf("Token" to GlobalUser.token.toString())

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Pedir_datos_apis(
                    endpoint = "/configuracion/list/impresoras",
                    params = params,
                    dataClass = ListImpresoras::class,
                    listaKey = "result",
                    headers = headers,
                    onSuccess = { response ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            val ips = response.map { it.IP }
                            if (ips.isNotEmpty()) {
                                actualizarAutoCompleteComboBox(ips)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "SIN DATOS",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    onError = { error ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "Error al obtener datos de la API: $error",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            } catch (e: Exception) {
                lifecycleScope.launch(Dispatchers.Main) {
                    MensajesDialog.showMessage(requireContext(), "Ocurrió un error: ${e.message}")
                }
            }
        }

        buttonOcultar = view.findViewById(R.id.buttonOcultarImpresionEtiquetas)
        buttonOcultar.setOnClickListener {
            backgroundBlockerListener?.hideBackgroundBlocker()
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commitNow()

            val lbCodigo: TextView = view.findViewById(R.id.lb_codigo_impresion)
            lbCodigo.text = ""
        }

        view.findViewById<Button>(R.id.buttonConfirmar_impresion).setOnClickListener {
            val cantidad = txtImpresorasCantidad.text.toString().toIntOrNull() ?: 1
            val cantidad_max =arguments?.getInt(ARG_CANTIDAD_MAX)

            if (cantidad_datos==1){
                if (cantidad<=cantidad_max.toString().toInt() && cantidad>0){

                    val zpl = """
                        ^XA
                        ^CI28
                        ^LH5,0
                        ^FO20,155^A0N,14,14
                        ^FB300,2,0^FDSKU:${sku}^FS
                        ^FB280,2,2
                        ^FO20,115^A0N,14,14^FD ${title} / ${color} ^FS
                        ^FO30,15^BY2^BCN,55,N,N
                        ^FD ${inventory} ^FS
                        ^FT128,100^A0N,22,22^FH\^FD ${inventory}^FS
                        ^LH0,0
                        ^FO360,20^A0B,20,20^FDID: ${envio}^FS
                        ^PQ${cantidad},0,1,Y
                        ^XZ
                    """.trimIndent();
                    val etiquetaPrinter = EtiquetaPrinter()

                    lifecycleScope.launch {

                        var exitoPrimario = false
                        try {
                            exitoPrimario = etiquetaPrinter.imprimirEtiqueta(requireContext(), cbSelectImpresora.text.toString(), zpl)
                        } catch (e: Exception) {
                            // No mostramos el error, simplemente fallamos silenciosamente
                        }

                        if (exitoPrimario) {
                            AlertDialog.Builder(requireContext())
                                .setMessage("Impresión completada correctamente en IP ${cbSelectImpresora.text.toString()}")
                                .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                                .setCancelable(false)
                                .create()
                                .show()
                        }else {
                            AlertDialog.Builder(requireContext())
                                .setMessage("Error al imprimir")
                                .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                                .setCancelable(false)
                                .create()
                                .show()
                        }
                    }

                }else{
                    MensajesDialog.showMessage(requireContext(), "No puedes imprimir etiquetas de más o imprimir 0")
                }
            }else{
                MensajesDialog.showMessage(requireContext(), "No se puede imprimir ese Código");
            }


        }

        parentFragmentManager.beginTransaction()
            .show(this)
            .commit()
        backgroundBlockerListener?.showBackgroundBlocker()

    }

    private fun actualizarAutoCompleteComboBox(opciones: List<String>) {
        val adaptador = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            opciones
        )
        cbSelectImpresora.setAdapter(adaptador)
    }

    private fun obtenerDatos(){
        try {
            val params= mapOf(
                "id_envio" to  arguments?.getString(ARG_FOLIO).orEmpty(),
                "codigo" to arguments?.getString(ARG_CODIGO).orEmpty()
            )
            val headers = mapOf("Token" to GlobalUser.token.toString());

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/tareas/packing/envios/items/etiquetas",
                        params=params,
                        dataClass = listaEtiquetaPackingMeli::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                cantidad_datos = lista.size
                                Log.d("EtiquetaCount", "Cantidad de etiquetas: $cantidad_datos")
                                if (lista.isNotEmpty()) {
                                    val item = lista[0]
                                    sku=item.SKU ?:""
                                    color=item.COLOR ?:""
                                    title=item.TITLE ?:""
                                    inventory=item.INVENTORY_ID ?:""
                                    envio=item.ENVIO_ID?:""
                                } else {
                                    MensajesDialog.showMessage(requireContext(), "Lista vacía")
                                }
                            }
                        },
                        onError = { error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(requireContext(), "Error: $error")
                            }
                        } )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(requireContext(), "Ocurrió un error: ${e.message}")
                    }
                }
            }
        }catch (e: Exception){
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(
                    requireContext(),
                    "Ocurrió un error: ${e.message}"
                );
            }
        }

    }

}




