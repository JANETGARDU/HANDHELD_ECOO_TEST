package com.example.slimmx.Almacen2.DevolucionProveedor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.slimmx.Almacen2.DevolucionProveedor.EntregaCedis.Confirmacion_Entrega_Cedis
import com.example.slimmx.Almacen2.DevolucionProveedor.EntregaProveedor.Confirmacion_EntregaProveedor
import com.example.slimmx.GlobalUser
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.MenuPrincipal
import com.example.slimmx.R

class Menu_Devolucion : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_devolucion)
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

        val lb_Usuario=findViewById<TextView>(R.id.lb_Usuario_menu);
        val btn_recepcion_cedis=findViewById<Button>(R.id.buttonEntrega)
        val btn_recepcion_proveedor=findViewById<Button>(R.id.buttonEntregaProveedor)
        var btn_cerrar_sesion=findViewById<Button>(R.id.buttonCerrarSesion)


        lb_Usuario.setText(GlobalUser.nombre);

        btn_recepcion_cedis.setOnClickListener {
            startActivity(Intent(this@Menu_Devolucion, Confirmacion_Entrega_Cedis::class.java));
        }

        btn_recepcion_proveedor.setOnClickListener {
            startActivity(Intent(this@Menu_Devolucion, Confirmacion_EntregaProveedor::class.java));
        }

        btn_cerrar_sesion.setOnClickListener {
            finishAffinity()
        }

    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Desea cerrar sesión?")
            .setPositiveButton("Sí") { _, _ -> finish() }
            .setNegativeButton("No", null)
            .show()
    }

}