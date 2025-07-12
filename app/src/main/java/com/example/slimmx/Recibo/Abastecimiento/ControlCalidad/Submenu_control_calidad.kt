package com.example.slimmx.Recibo.Abastecimiento.ControlCalidad

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListReciboAbastecimiento
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.R
import com.example.slimmx.Recibo.Abastecimiento.ControlCalidad.Alta.Entrada_Calidad
import com.example.slimmx.Recibo.Abastecimiento.ControlCalidad.Revision.ControlCalidad_List
import com.example.slimmx.Recibo.Abastecimiento.Seleccion_recibo_abastecimiento
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.Vista.FragmentPage3
import com.example.slimmx.Vista.FragmentPageEtiquetaBluetooth
import com.example.slimmx.Vista.FragmentPageImpresionEtiquetas
import com.example.slimmx.Vista.FragmentPageImpresionEtiquetas_Verificacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Submenu_control_calidad : AppCompatActivity(), FragmentPageImpresionEtiquetas_Verificacion.OnBackgroundBlockerListener {

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
        setContentView(R.layout.activity_submenu_control_calidad)
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

        var btn_asignacion=findViewById<Button>(R.id.buttonAsig_Control);
        var btn_revision=findViewById<Button>(R.id.buttonRevision);

        btn_asignacion.setOnClickListener {
            FoliosEntradaCalidad();
        }

        btn_revision.setOnClickListener {
            FoliosReciboCalidad();
        }


        val buttonMenu = findViewById<Button>(R.id.buttonMenu)
        buttonMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, buttonMenu)
            popupMenu.menuInflater.inflate(R.menu.menu_opciones, popupMenu.menu)
            val menuItemVerImagen = popupMenu.menu.findItem(R.id.item_ver_imagen)
            menuItemVerImagen.isVisible = false;
            val menuItemCombosPacks=popupMenu.menu.findItem(R.id.item_packs_combos);
            menuItemCombosPacks.isVisible=false;
            val menuImpresionBluetooth=popupMenu.menu.findItem(R.id.item_etiquetas_bluetooth);
            menuImpresionBluetooth.isVisible=false;
            val menuRecomendacion=popupMenu.menu.findItem(R.id.item_recomendaciones);
            menuRecomendacion.isVisible=false;
            val menuUbicacion=popupMenu.menu.findItem(R.id.item_f1);
            menuUbicacion.isVisible=false;
            val menuEtiquetas=popupMenu.menu.findItem(R.id.item_impresora);
            menuEtiquetas.isVisible=false;
            val menuInventory=popupMenu.menu.findItem(R.id.item_inventario);
            menuInventory.isVisible=false;
            popupMenu.setOnMenuItemClickListener { item ->
                try {
                    when (item.itemId) {
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


    }

    private fun FoliosEntradaCalidad() {
        try {
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/compras/recibo/calidad/folio/entrada",
                        params=emptyMap<String, String>(),
                        dataClass = ListReciboAbastecimiento::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.FOLIO   }.toTypedArray()
                                    val intent =
                                        Intent(this@Submenu_control_calidad, Entrada_Calidad::class.java)
                                    intent.putExtra("folios", opciones)
                                    startActivity(intent)
                                    LogsEntradaSalida.logsPorModulo( this@Submenu_control_calidad, lifecycleScope, "ABASTECIMIENTO/CALIDAD/ENTRADA", "ENTRADA");
                                    finish()
                                } else {
                                    MensajesDialog.showMessage(this@Submenu_control_calidad, "No hay folios disponibles");
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Submenu_control_calidad, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Submenu_control_calidad, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    private fun FoliosReciboCalidad() {
        try {
            val headers = mapOf(
                "Token" to GlobalUser.token.toString()
            );
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/compras/calidad/recibo/folios",
                        params=emptyMap<String, String>(),
                        dataClass = ListReciboAbastecimiento::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.FOLIO   }.toTypedArray()
                                    val intent =
                                        Intent(this@Submenu_control_calidad, ControlCalidad_List::class.java)
                                    intent.putExtra("folios", opciones)
                                    startActivity(intent);
                                    LogsEntradaSalida.logsPorModulo( this@Submenu_control_calidad, lifecycleScope, "ABASTECIMIENTO/CALIDAD/RECIBO", "ENTRADA");
                                    finish()
                                } else {
                                    MensajesDialog.showMessage(this@Submenu_control_calidad, "No hay folios disponibles");
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Submenu_control_calidad, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Submenu_control_calidad, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Seleccion_recibo_abastecimiento::class.java));
        //LogsEntradaSalida.logsPorModulo( this, lifecycleScope, "ABASTECIMIENTO/CALIDAD", "SALIDA");
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