package com.example.slimmx.Submenus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalUser
import com.example.slimmx.Inventario.Ajustes.Ajustes
import com.example.slimmx.Inventario.Consulta_inventarios.Consulta_inventario
import com.example.slimmx.Inventario.Muestreo.Muestreo_list
import com.example.slimmx.Inventario.Muestreo.Seleccion_muestreo
import com.example.slimmx.ListaFolioPickingEnvio
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.R
import com.example.slimmx.Validaciones.LogsEntradaSalida
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Submenu_inventarios : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_submenu_inventarios)
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
        val message = intent.getStringExtra("MESSAGE")
        if (!message.isNullOrEmpty()) {
            MensajesDialog.showMessage(this, message)
        }

        var btn_ajustes = findViewById<Button>(R.id.buttonAjustes);


        if (GlobalUser.nombre.equals("JAIR")|| GlobalUser.nombre.equals("ALEJANDROI") || GlobalUser.nombre.equals("CACRISTIAN")|| GlobalUser.nombre.equals("JAGS")|| GlobalUser.nombre.equals("ALEX")|| GlobalUser.nombre.equals("JEDUARDO")){
            btn_ajustes.visibility=View.VISIBLE;
        }else{
            btn_ajustes.visibility=View.GONE;
        }

        var btn_consulta_inventario=findViewById<Button>(R.id.buttonConsultaInventario);
        var btn_muestreo=findViewById<Button>(R.id.buttonMuestreo);

        btn_ajustes.setOnClickListener {
            startActivity(Intent(this@Submenu_inventarios, Ajustes::class.java));
            //LogsEntradaSalida.logsPorModulo(this@Submenu_inventarios, lifecycleScope, "INVENTARIOS/AJUSTES", "ENTRADA")
            finish();
        }

        btn_consulta_inventario.setOnClickListener {
            startActivity(Intent(this@Submenu_inventarios, Consulta_inventario::class.java));
            //LogsEntradaSalida.logsPorModulo(this@Submenu_inventarios, lifecycleScope, "INVENTARIOS/CONSULTA/INVENTARIO", "ENTRADA")
            finish();
        }

        btn_muestreo.setOnClickListener {
           if (GlobalUser.nombre=="caran"  || GlobalUser.nombre=="HUGO" || GlobalUser.nombre=="eregmh" || GlobalUser.roles?.contains("DESARROLLADOR") ?: false){
                startActivity(Intent(this@Submenu_inventarios,Seleccion_muestreo::class.java));
            }else{
                //startActivity(Intent(this@Submenu_inventarios,Muestreo_list::class.java));
               folios_lista_muestros();
            }
            //LogsEntradaSalida.logsPorModulo(this@Submenu_inventarios, lifecycleScope, "INVENTARIOS/MUESTREO", "ENTRADA")
        }
    }

    private fun folios_lista_muestros(){
        try {
            val headers= mapOf(
                "Token" to GlobalUser.token.toString()
            )

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/inventario/tareas/muestreo",
                        params= emptyMap<String,String>(),
                        dataClass = ListaFolioPickingEnvio::class,
                        listaKey = "result",
                        headers = headers,
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    val opciones = lista.map { it.ID }.toTypedArray()
                                    val intent =
                                        Intent(this@Submenu_inventarios, Muestreo_list::class.java)
                                    intent.putExtra("folios", opciones)
                                    startActivity(intent)
                                    finish();
                                } else {
                                    MensajesDialog.showMessage(this@Submenu_inventarios, "No hay folios disponibles")
                                }
                            }
                        }, onError = {error->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@Submenu_inventarios, "${error}");
                            }

                        }
                    )
                }catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@Submenu_inventarios, "Ocurrió un error: ${e.message}")
                    }
                }
            }

        }catch (e: Exception){
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}");
        }
    }

    override fun onBackPressed() {
        LogsEntradaSalida.logsPorModulo(this, lifecycleScope, "INVENTARIOS", "SALIDA")
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