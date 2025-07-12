package com.example.slimmx.Recibo.Devolucion.Calidad.Verificacion

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.GlobalUser
import com.example.slimmx.MensajesDialog
import com.example.slimmx.MensajesDialogConfirmaciones
import com.example.slimmx.R
import com.example.slimmx.RetrofitPostInstance_imagenes
import com.example.slimmx.Validaciones.LogsEntradaSalida
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import java.io.File
import java.io.IOException

class CargaImagenes : AppCompatActivity() {

    private lateinit var photoContainer: LinearLayout
    private val photoUris = mutableListOf<Uri>()
    private var lastPhotoUri: Uri? = null
    private var photoCounter = 1
    private val photoFiles = mutableListOf<File>()
    private var referencia:String="";

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        lastPhotoUri?.let { uri ->
            if (success) {
                photoUris.add(uri)
                addPhotoView(uri)
            }
            lastPhotoUri = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_carga_imagenes)
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
        photoContainer = findViewById(R.id.photoContainer)
        referencia=intent.getStringExtra("referencia").toString()
        val btnTakePhoto=findViewById<Button>(R.id.btnTakePhoto);

        /*btnTakePhoto.setOnClickListener {
            val fileName = "${referencia}-${photoCounter}.jpg"
            photoCounter++

            val file = File(cacheDir, fileName)
            val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
            lastPhotoUri = uri

            takePhoto.launch(uri)

            // Guarda el archivo para usarlo luego
            photoFiles.add(file)
        }*/

        btnTakePhoto.setOnClickListener {
            if (photoUris.size >= 4) {
                MensajesDialog.showMessage(this, "Solo puedes tomar un máximo de 4 fotos");
                return@setOnClickListener
            }

            val fileName = "${referencia}-${photoCounter}.jpg"
            photoCounter++

            val file = File(cacheDir, fileName)
            val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
            lastPhotoUri = uri

            takePhoto.launch(uri)
            //Se guarda la imagen para despues usarla
            photoFiles.add(file)
        }

        /*findViewById<Button>(R.id.btnUploadPhotos).setOnClickListener {
            if (photoFiles.isEmpty()) {
                MensajesDialog.showMessage(this, "No hay fotos para subir")
                return@setOnClickListener
            }
            for (file in photoFiles) {
                subirImagen(file)
            }
        }*/

        findViewById<Button>(R.id.btnUploadPhotos).setOnClickListener {
            if (photoFiles.isEmpty()) {
                MensajesDialog.showMessage(this, "No hay fotos para subir");
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setTitle("Confirmar subida")
                .setMessage("¿Estás seguro de que deseas subir las fotos?")
                .setPositiveButton("Sí") { _, _ ->
                    subirTodasLasImagenes()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

    }

    private fun addPhotoView(uri: Uri) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 24
            }
        }

        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 500
            )
            setImageURI(uri)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        val deleteButton = Button(this).apply {
            text = "Eliminar"
            setOnClickListener {
                photoUris.remove(uri)
                photoContainer.removeView(container)
            }
        }

        container.addView(imageView)
        container.addView(deleteButton)
        photoContainer.addView(container)
    }

    private fun subirImagen(file: File) {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        //MensajesDialog.showMessage(this, "${file.name}")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitPostInstance_imagenes.api.getDatos_Post_imagenes(
                    url = "upload_image",
                    file = body,
                    headers = mapOf("Token" to GlobalUser.token.toString())
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        MensajesDialog.showMessage(this@CargaImagenes, "Imagenes subidas con exito");
                    } else {
                        MensajesDialog.showMessage(this@CargaImagenes,"Error al subir las imagenes");
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    MensajesDialog.showMessage(this@CargaImagenes, "Excepción: ${e.message}");
                }
            }
        }
    }

    private fun subirTodasLasImagenes() {
        CoroutineScope(Dispatchers.IO).launch {
            var todasExitosas = true

            for (file in photoFiles) {
                try {
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                    val response = RetrofitPostInstance_imagenes.api.getDatos_Post_imagenes(
                        url = "upload_image",
                        file = body,
                        headers = mapOf("Token" to GlobalUser.token.toString())
                    )

                    if (!response.isSuccessful) {
                        todasExitosas = false
                    }

                } catch (e: Exception) {
                    todasExitosas = false
                }
            }

            withContext(Dispatchers.Main) {
                if (todasExitosas) {
                    MensajesDialogConfirmaciones.showMessage(this@CargaImagenes, "Imágenes subidas con éxito"){
                        finish();
                    }
                } else {
                    MensajesDialog.showMessage(this@CargaImagenes, "Error al subir una o más imágenes");
                }
            }
        }
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