package com.example.slimmx.Login

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.Almacen2.DevolucionProveedor.Menu_Devolucion
import com.example.slimmx.GlobalConfig
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListaLogin
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MenuPrincipal
import com.example.slimmx.Pedir_datos_apis
import com.example.slimmx.Pedir_datos_apis_post
import com.example.slimmx.R
import com.example.slimmx.Validaciones.LogsEntradaSalida
import com.example.slimmx.listaAlmacenDatos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var name = ""
    private var password = ""
    private lateinit var btnAceptar:Button;
    private lateinit var cbSelect: AutoCompleteTextView;
    private lateinit var txtUsuario:EditText;
    private lateinit var listaAlmacenes: List<listaAlmacenDatos>

    // INICIA LA ACTIVIDAD PRINCIPAL
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )

        txtUsuario = findViewById(R.id.txtUsuario)
        val txtPassword = findViewById<EditText>(R.id.txtPassword)
        btnAceptar = findViewById(R.id.buttonaceptar)
        val btnSalir = findViewById<Button>(R.id.buttonSalir)
        val btnEliminarUser=findViewById<Button>(R.id.buttonEliminar_user)
        val btnEliminarPass=findViewById<Button>(R.id.buttonEliminar_pass)
        cbSelect = findViewById(R.id.cbSelect)

        //obtenerAlmacenes();

        txtUsuario.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    val inputText = txtUsuario.text.toString()
                    if (!inputText.isNullOrEmpty()) {
                        txtPassword.requestFocus();
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this@MainActivity, "Ocurrió un error: ${e.message}");
                }

                true
            } else {
                false
            }
        }


        txtPassword.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val inputText = txtPassword.text.toString().uppercase()
                try {
                    if (!inputText.isNullOrEmpty()) {
                        val name = txtUsuario.text.toString().trim()
                        val password = txtPassword.text.toString().trim()

                        if (name.isNotEmpty()&& password.isNotEmpty()){
                            getLogin(name, password, cbSelect.text.toString())

                            // Limpia los campos después de la acción
                            txtUsuario.setText("")
                            txtPassword.setText("")
                        }else{
                            MensajesDialog.showMessage(this@MainActivity,"Debes llenar todos los datos");
                        }
                    }
                }catch (e: Exception){
                    MensajesDialog.showMessage(this@MainActivity, "Ocurrió un error: ${e.message}");
                }
                true
            } else {
                false
            }
        }

        // Botón aceptar
        btnAceptar.setOnClickListener {
            try {
                name = txtUsuario.text.toString().trim()
                password = txtPassword.text.toString().trim()
                if (name.isNotEmpty() && password.isNotEmpty()) {
                    getLogin(name, password, cbSelect.text.toString())
                } else {
                    MensajesDialog.showMessage(this@MainActivity,"Debes llenar todos los datos");
                }
            }catch (e: Exception){
                MensajesDialog.showMessage(this@MainActivity, "Ocurrió un error: ${e.message}");
            }
        }

        // Botón salir
        btnSalir.setOnClickListener {
            finishAffinity() // Cierra la aplicación
        }

        btnEliminarUser.setOnClickListener {
            txtUsuario.text.clear();
            txtUsuario.requestFocus();
        }

        btnEliminarPass.setOnClickListener {
            txtPassword.text.clear();
            txtPassword.requestFocus();
        }


    }

    /*private fun obtenerAlmacenes() {
        try {
            GlobalConfig.puerto = "9092" // Valor por defecto inicial
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Pedir_datos_apis(
                        endpoint = "/obtener/almacenes",
                        params = emptyMap(),
                        dataClass = listaAlmacenDatos::class,
                        listaKey = "result",
                        headers = emptyMap(),
                        onSuccess = { lista ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (lista.isNotEmpty()) {
                                    listaAlmacenes = lista
                                    val opciones = lista.map { it.NOMBRE }
                                    actualizarcbBox(opciones)
                                } else {
                                    finish()
                                }
                            }
                        },
                        onError = { error ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                MensajesDialog.showMessage(this@MainActivity, "Salió un error: $error")
                            }
                        }
                    )
                } catch (e: Exception) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MensajesDialog.showMessage(this@MainActivity, "Ocurrió un error: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            lifecycleScope.launch(Dispatchers.Main) {
                MensajesDialog.showMessage(this@MainActivity, "Ocurrió un error: ${e.message}")
            }
        }
    }*/

    private fun actualizarcbBox(opciones: List<String>) {
        try {
            val adaptador = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                opciones
            )
            cbSelect.setAdapter(adaptador)

            cbSelect.setOnItemClickListener { _, _, position, _ ->
                val seleccion = opciones[position]

                // Buscar el objeto original por nombre y actualizar el puerto
                val almacenSeleccionado = listaAlmacenes.firstOrNull { it.NOMBRE == seleccion }
                if (almacenSeleccionado != null) {
                    GlobalConfig.puerto = almacenSeleccionado.PUERTO
                }

                txtUsuario.post { txtUsuario.requestFocus() }
            }
        } catch (e: Exception) {
            MensajesDialog.showMessage(this, "Ocurrió un error: ${e.message}")
        }
    }

    private fun getLogin(name: String, password: String, almacen:String) {
        if (name.isNotEmpty() && password.isNotEmpty()/* && almacen.isNotEmpty()*/) {
            btnAceptar.isEnabled=false;
            val body = mapOf(
                "username" to name,
                "password" to password
            )

            // Llama al método para realizar la peticion
            lifecycleScope.launch {
                Pedir_datos_apis_post(
                    endpoint = "/login",
                    body = body,
                    dataClass = ListaLogin::class,
                    listaKey = "result", // Clave JSON donde está la lista
                    onSuccess = { loginData ->
                        GlobalUser.nombre = loginData.username
                        GlobalUser.token = loginData.token
                        GlobalUser.roles = loginData.roles.map { it.rolname }.toString()
                        GlobalUser.device_type=loginData.device_type
                        if (GlobalUser.device_type.toString().contains("smartphone")){
                            if (GlobalUser.roles.toString().contains("SHIPPING_CLERK")){
                                startActivity(Intent(this@MainActivity, Menu_Devolucion::class.java));
                            }else{
                                startActivity(Intent(this@MainActivity, MenuPrincipal::class.java));
                                LogsEntradaSalida.logsPorModulo( this@MainActivity, lifecycleScope, "INICIO/SESION", "ENTRADA")
                            }
                        }else{
                            MensajesDialog.showMessage(this@MainActivity,"No tiene permisos para acceder a este dispositivo");
                        }

                        btnAceptar.isEnabled=true;
                    },
                    onError = { error ->
                        // Si hubo un error en la petición, mostramos un Toast
                        MensajesDialog.showMessage(this@MainActivity, "Error: $error");
                        btnAceptar.isEnabled=true;
                    }
                )
            }

        } else {
            MensajesDialog.showMessage(this@MainActivity,"Debes llenar todos los datos");
            btnAceptar.isEnabled=true;
        }
    }


}