package com.tuapp.signlanguage

import android.app.Application
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import com.tuapp.signlanguage.ml.HandLandmarkerHelper
import com.tuapp.signlanguage.ml.HandResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SignViewModel(app: Application) : AndroidViewModel(app) {

    private val _handResult = MutableStateFlow(HandResult())
    val handResult: StateFlow<HandResult> = _handResult.asStateFlow()

    private val _translatedText = MutableStateFlow("")
    val translatedText: StateFlow<String> = _translatedText.asStateFlow()

    private val helper = HandLandmarkerHelper(app) { _handResult.value = it }

    fun onFrame(proxy: ImageProxy) = helper.detect(proxy, isFrontCamera = true)

    fun appendCurrentSign() {
        val currentSign = _handResult.value.prediction.label
        if (currentSign.isNotBlank() && currentSign != "Esperando..." && currentSign != "No hay mano" && currentSign != "Detectando...") {
            val charToAdd = currentSign.split(" ").firstOrNull() ?: ""
            if (charToAdd != "") {
                _translatedText.value += charToAdd
            }
        }
    }

    fun addSpace() {
        if (_translatedText.value.length > 0 && !_translatedText.value.endsWith(" ")) {
            _translatedText.value += " "
        }
    }

    fun deleteLastChar() {
        if (_translatedText.value.length > 0) {
            _translatedText.value = _translatedText.value.dropLast(1)
        }
    }

    fun clearText() {
        _translatedText.value = ""
    }

    override fun onCleared() {
        helper.close()
    }
}
