package com.example.slimmx.Validaciones

import android.content.Context
import android.content.Intent
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.example.slimmx.Acomodo.Acomodo_list
import com.example.slimmx.GlobalUser
import com.example.slimmx.ListaFolioPickingEnvio
import com.example.slimmx.MensajesDialog
import com.example.slimmx.Pedir_datos_apis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object BoxValidator {
    fun validateBox(
        input: String,
        successComponent: () -> Unit,
        failureComponent: (String) -> Unit
    ) {
        if (input == "S/B" || input.startsWith("BX-")) {
            successComponent()
        } else {
            failureComponent("Ese no es un BOX")
        }
    }
}

object MostrarTeclado {

    fun getMostrarTeclado(): View.OnTouchListener {
        return View.OnTouchListener { view, _ ->
            if (GlobalUser.nombre in listOf("JAIR", "JAGS","JEDUARDO", "ALEX")) {
                view.requestFocus()
                showKeyboard(view)
                setUpperCase(view)
            } else {
                view.requestFocus()
                hideKeyboard(view)
            }
            true
        }
    }

    private fun showKeyboard(view: View) {
        val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setUpperCase(view: View) {
        if (view is EditText) {
            view.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        }
    }
}
