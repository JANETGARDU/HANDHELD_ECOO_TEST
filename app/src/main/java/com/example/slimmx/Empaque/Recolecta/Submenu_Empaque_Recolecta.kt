package com.example.slimmx.Empaque.Recolecta

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.Empaque.Recolecta.Asignacion.Empaque_Recolecta_Asiganacion
import com.example.slimmx.Empaque.Recolecta.Empaque.Packing_Recolecta
import com.example.slimmx.GlobalUser
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.R
import com.example.slimmx.Submenus.Submenu_packing
import com.example.slimmx.Validaciones.LogsEntradaSalida

class Submenu_Empaque_Recolecta : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_submenu_empaque_recolecta)
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

        val btn_empaque=findViewById<Button>(R.id.buttonEmpa_Sub);
        val btn_asig=findViewById<Button>(R.id.buttonAsig_Sub);

        btn_empaque.setOnClickListener {
            startActivity(Intent(this@Submenu_Empaque_Recolecta, Packing_Recolecta::class.java));
            LogsEntradaSalida.logsPorModulo( this@Submenu_Empaque_Recolecta, lifecycleScope, "EMPAQUE/RECOLECTA/TAREA", "ENTRADA");
            finish();
        }

        btn_asig.setOnClickListener {
            startActivity(Intent(this@Submenu_Empaque_Recolecta, Empaque_Recolecta_Asiganacion::class.java));
            LogsEntradaSalida.logsPorModulo( this@Submenu_Empaque_Recolecta, lifecycleScope, "EMPAQUE/RECOLECTA/ASIGNACION", "ENTRADA");
            finish();
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Submenu_packing::class.java));
        //LogsEntradaSalida.logsPorModulo( this@Submenu_Empaque_Recolecta, lifecycleScope, "EMPAQUE/RECOLECTA", "SALIDA");
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