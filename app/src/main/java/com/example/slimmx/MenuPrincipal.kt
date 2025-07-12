package com.example.slimmx

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.Consulta_Paquetes.Consultas_paquetes
import com.example.slimmx.Packs_Combos.Busqueda_Packs
import com.example.slimmx.Submenus.Submenu_acomodo
import com.example.slimmx.Submenus.Submenu_embarque
import com.example.slimmx.Submenus.Submenu_inventarios
import com.example.slimmx.Submenus.Submenu_packing
import com.example.slimmx.Submenus.Submenu_picking
import com.example.slimmx.Submenus.Submenu_recibo
import com.example.slimmx.Validaciones.LogsEntradaSalida

class MenuPrincipal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_principal)
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
        var textViewUsuario = findViewById<TextView>(R.id.lb_Usuario_menu)
        var btn_cerrar_sesion = findViewById<Button>(R.id.buttonCerrarSesion)
        var btn_submenu_empaque = findViewById<Button>(R.id.buttonPacking)
        var btn_submenu_picking = findViewById<Button>(R.id.buttonPicking)
        var btn_recibo = findViewById<Button>(R.id.buttonRecibo);
        var btn_acomodo = findViewById<Button>(R.id.buttonAcomodo);
        var btn_packs_combo = findViewById<Button>(R.id.buttonPacksCombo);
        var btn_consulta_etiquetas = findViewById<Button>(R.id.buttonConsulta_etiquetas);
        var btn_inventarios = findViewById<Button>(R.id.buttonInventarios);
        var btn_embarque = findViewById<Button>(R.id.buttonEmbarque);

        textViewUsuario.setText("Usuario: " + GlobalUser.nombre)

        btn_cerrar_sesion.setOnClickListener {
            LogsEntradaSalida.logsPorModulo(
                this@MenuPrincipal,
                lifecycleScope,
                "INICIO/SESION",
                "SALIDA"
            )
            finishAffinity();
        }

        btn_submenu_empaque.setOnClickListener {
            startActivity(Intent(this@MenuPrincipal, Submenu_packing::class.java));
            LogsEntradaSalida.logsPorModulo(
                this@MenuPrincipal,
                lifecycleScope,
                "EMPAQUE",
                "ENTRADA"
            )
        }

        btn_submenu_picking.setOnClickListener {
            startActivity(Intent(this@MenuPrincipal, Submenu_picking::class.java));
            LogsEntradaSalida.logsPorModulo(
                this@MenuPrincipal,
                lifecycleScope,
                "PICKING ",
                "ENTRADA"
            )
        }

        val message = intent.getStringExtra("MESSAGE")
        if (!message.isNullOrEmpty()) {
            MensajesDialog.showMessage(this, message)
        }

        btn_recibo.setOnClickListener {
            startActivity(Intent(this@MenuPrincipal, Submenu_recibo::class.java));
            LogsEntradaSalida.logsPorModulo(this@MenuPrincipal, lifecycleScope, "RECIBO", "ENTRADA")
        }

        btn_acomodo.setOnClickListener {
            startActivity(Intent(this@MenuPrincipal, Submenu_acomodo::class.java));
            LogsEntradaSalida.logsPorModulo(
                this@MenuPrincipal,
                lifecycleScope,
                "ACOMODO",
                "ENTRADA"
            )
        }

        btn_packs_combo.setOnClickListener {
            startActivity(Intent(this@MenuPrincipal, Busqueda_Packs::class.java));
            LogsEntradaSalida.logsPorModulo(
                this@MenuPrincipal,
                lifecycleScope,
                "PACK_COMBO",
                "ENTRADA"
            )
        }

        btn_consulta_etiquetas.setOnClickListener {
            startActivity(Intent(this@MenuPrincipal, Consultas_paquetes::class.java));
            LogsEntradaSalida.logsPorModulo(
                this@MenuPrincipal,
                lifecycleScope,
                "CONSULTA/ETIQUETAS",
                "ENTRADA"
            )
        }

        btn_inventarios.setOnClickListener {
            startActivity(Intent(this@MenuPrincipal, Submenu_inventarios::class.java));
            LogsEntradaSalida.logsPorModulo(
                this@MenuPrincipal,
                lifecycleScope,
                "INVENTARIOS",
                "ENTRADA"
            )
        }

        btn_embarque.setOnClickListener {
            startActivity(Intent(this@MenuPrincipal, Submenu_embarque::class.java));
            LogsEntradaSalida.logsPorModulo(
                this@MenuPrincipal,
                lifecycleScope,
                "EMBARQUE",
                "ENTRADA"
            )
        }

    }


    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Desea cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                finish()
                LogsEntradaSalida.logsPorModulo(
                    this@MenuPrincipal,
                    lifecycleScope,
                    "INICIO/SESION",
                    "SALIDA"
                )
            }
            .setNegativeButton("No", null)
            .show()
    }


}