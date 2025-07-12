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
import com.example.slimmx.R
import com.example.slimmx.Recibo.Abastecimiento.Seleccion_recibo_abastecimiento
import com.example.slimmx.Recibo.Devolucion.Seleccion_devolucion
import com.example.slimmx.Validaciones.LogsEntradaSalida

class Submenu_recibo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_submenu_recibo)
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

        var btn_abastecimiento=findViewById<Button>(R.id.buttonAbast_Menu_recibo);
        var btn_retorno=findViewById<Button>(R.id.buttonRetorno_Menu_recibo);


        val message = intent.getStringExtra("MESSAGE")
        if (!message.isNullOrEmpty()) {
            MensajesDialog.showMessage(this, message)
        }

        btn_abastecimiento.setOnClickListener {
           /* GlobalUser.RECIBO="A";
            GlobalUser.DEVOLUCION=0;
            startActivity(Intent(this@Submenu_recibo, Recibo_list::class.java));
            finish();*/
            startActivity(Intent(this@Submenu_recibo, Seleccion_recibo_abastecimiento::class.java));
            //LogsEntradaSalida.logsPorModulo( this, lifecycleScope, "ABASTECIMIENTO", "ENTRADA");
            finish();
        }

        btn_retorno.setOnClickListener {
            startActivity(Intent(this@Submenu_recibo, Seleccion_devolucion::class.java));
            //LogsEntradaSalida.logsPorModulo( this, lifecycleScope, "DEVOLUCION", "ENTRADA");
            finish();
        }

    }

    override fun onBackPressed() {
        LogsEntradaSalida.logsPorModulo( this@Submenu_recibo, lifecycleScope, "RECIBO", "SALIDA")
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