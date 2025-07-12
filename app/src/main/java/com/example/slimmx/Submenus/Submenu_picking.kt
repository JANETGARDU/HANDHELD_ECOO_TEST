package com.example.slimmx.Submenus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalUser
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.Picking.Cedis.Cedis_seleccion_menu
import com.example.slimmx.Picking.Envio.Picking.Picking_envio_list
import com.example.slimmx.Picking.Envio.Submenu_Picking_Envio
import com.example.slimmx.Picking.Recolecta.Picking.Picking_recolecta_list
import com.example.slimmx.Picking.Recolecta.Submenu_Picking_Recolecta
import com.example.slimmx.R
import com.example.slimmx.Validaciones.LogsEntradaSalida

class Submenu_picking : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_submenu_picking)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (GlobalUser.nombre.isNullOrEmpty()){
            MensajesDialogConfirmaciones.showMessage(this, "Ocurrio un error se cerrara la aplicacion, lamento el inconveniente"){
                finishAffinity();
            }
        }

        val message = intent.getStringExtra("MESSAGE")
        if (!message.isNullOrEmpty()) {
            MensajesDialog.showMessage(this, message)
        }

        var btn_envio=findViewById<Button>(R.id.buttonEnvio_Menu_picking);
        var btn_recolecta=findViewById<Button>(R.id.buttonRecolecta_picking);
        var btn_cedis=findViewById<Button>(R.id.buttonCedis);

        btn_envio.setOnClickListener {
            startActivity(Intent(this@Submenu_picking, Submenu_Picking_Envio ::class.java));
            //LogsEntradaSalida.logsPorModulo( this@Submenu_picking, lifecycleScope, "PICKING/ENVIO", "ENTRADA");
            finish();
        }

        btn_recolecta.setOnClickListener {
            startActivity(Intent(this@Submenu_picking, Submenu_Picking_Recolecta ::class.java));
            //LogsEntradaSalida.logsPorModulo( this@Submenu_picking, lifecycleScope, "PICKING/RECOLECTA", "ENTRADA");
            finish();
        }

        btn_cedis.setOnClickListener {
            startActivity(Intent(this@Submenu_picking,Cedis_seleccion_menu::class.java));
            //LogsEntradaSalida.logsPorModulo( this@Submenu_picking, lifecycleScope, "PICKING/CEDIS", "ENTRADA");
            finish();
        }
    }

    override fun onBackPressed() {
        LogsEntradaSalida.logsPorModulo( this@Submenu_picking, lifecycleScope, "PICKING", "SALIDA")
        finish()
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