package com.example.slimmx.Submenus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.Empaque.Envio.Packing.SeleccionList_Envio
import com.example.slimmx.Empaque.Envio.Submenu_Empaque_Envio
import com.example.slimmx.Empaque.InsumoGeneral.Insumos
import com.example.slimmx.Empaque.Recolecta.Empaque.Packing_Recolecta
import com.example.slimmx.Empaque.Recolecta.Submenu_Empaque_Recolecta
import com.example.slimmx.GlobalUser
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.R
import com.example.slimmx.Validaciones.LogsEntradaSalida

class Submenu_packing : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_submenu_empaque)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (GlobalUser.nombre.isNullOrEmpty()){
            MensajesDialogConfirmaciones.showMessage(this, "Ocurrio un error se cerrara la aplicacion, lamento el inconveniente"){
                finishAffinity()
            }
        }
        val btnEnvio=findViewById<Button>(R.id.buttonEnvio_Menu)
        val btnRecolecta=findViewById<Button>(R.id.buttonRecolecta_Packing);
        val btnInsumosInternos=findViewById<Button>(R.id.buttonInsumoIn);

        val message = intent.getStringExtra("MESSAGE")
        if (!message.isNullOrEmpty()) {
            MensajesDialog.showMessage(this, message)
        }

        btnEnvio.setOnClickListener {
            startActivity(Intent(this@Submenu_packing, Submenu_Empaque_Envio::class.java));
            //LogsEntradaSalida.logsPorModulo( this@Submenu_packing, lifecycleScope, "EMPAQUE/ENVIO", "ENTRADA");
            finish();
        }

        btnRecolecta.setOnClickListener {
            startActivity(Intent(this@Submenu_packing, Submenu_Empaque_Recolecta::class.java));
            //LogsEntradaSalida.logsPorModulo( this@Submenu_packing, lifecycleScope, "EMPAQUE/RECOLECTA", "ENTRADA");
            finish();
        }

        btnInsumosInternos.setOnClickListener {
            startActivity(Intent(this@Submenu_packing,Insumos::class.java));
            //LogsEntradaSalida.logsPorModulo( this@Submenu_packing, lifecycleScope, "INSUMOS", "ENTRADA");
            finish();
        }

    }

    override fun onBackPressed() {
        LogsEntradaSalida.logsPorModulo( this@Submenu_packing, lifecycleScope, "EMPAQUE", "SALIDA")
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