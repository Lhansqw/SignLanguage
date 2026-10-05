package com.tuapp.signlanguage

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import com.tuapp.signlanguage.ml.HandLandmarkerHelper
import com.tuapp.signlanguage.ml.HandResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class SavedSentence(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val timestamp: String
)

class SignViewModel(app: Application) : AndroidViewModel(app), TextToSpeech.OnInitListener {

    private val _handResult = MutableStateFlow(HandResult())
    val handResult: StateFlow<HandResult> = _handResult.asStateFlow()

    private val _translatedText = MutableStateFlow("")
    val translatedText: StateFlow<String> = _translatedText.asStateFlow()

    private val _isFrontCamera = MutableStateFlow(true)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera.asStateFlow()

    private val _showOverlay = MutableStateFlow(true)
    val showOverlay: StateFlow<Boolean> = _showOverlay.asStateFlow()

    private val _autoSpeak = MutableStateFlow(false)
    val autoSpeak: StateFlow<Boolean> = _autoSpeak.asStateFlow()

    private val _savedHistory = MutableStateFlow<List<SavedSentence>>(emptyList())
    val savedHistory: StateFlow<List<SavedSentence>> = _savedHistory.asStateFlow()

    private var tts: TextToSpeech? = TextToSpeech(app, this)
    private var isTtsReady = false

    private val helper = HandLandmarkerHelper(app) { _handResult.value = it }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("es", "ES"))
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
            }
        }
    }

    fun onFrame(proxy: ImageProxy) = helper.detect(proxy, isFrontCamera = _isFrontCamera.value)

    fun toggleCamera() {
        _isFrontCamera.value = !_isFrontCamera.value
    }

    fun toggleOverlay() {
        _showOverlay.value = !_showOverlay.value
    }

    fun toggleAutoSpeak() {
        _autoSpeak.value = !_autoSpeak.value
    }

    fun speakText(text: String) {
        if (isTtsReady && text.isNotBlank()) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SignLanguageTTS")
        }
    }

    fun appendCurrentSign() {
        val currentSign = _handResult.value.prediction.label
        if (currentSign.isNotBlank() && currentSign != "Esperando..." && currentSign != "No hay mano" && currentSign != "Detectando...") {
            val charToAdd = currentSign.split(" ").firstOrNull() ?: ""
            if (charToAdd != "") {
                _translatedText.value += charToAdd
                if (_autoSpeak.value) {
                    speakText(charToAdd)
                }
            }
        }
    }

    fun addSpace() {
        if (_translatedText.value.isNotEmpty() && !_translatedText.value.endsWith(" ")) {
            _translatedText.value += " "
        }
    }

    fun deleteLastChar() {
        if (_translatedText.value.isNotEmpty()) {
            _translatedText.value = _translatedText.value.dropLast(1)
        }
    }

    fun clearText() {
        _translatedText.value = ""
    }

    fun saveCurrentSentence() {
        val text = _translatedText.value.trim()
        if (text.isNotEmpty()) {
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val timestamp = sdf.format(java.util.Date())
            _savedHistory.value = listOf(SavedSentence(text = text, timestamp = timestamp)) + _savedHistory.value
        }
    }

    fun deleteSavedSentence(id: Long) {
        _savedHistory.value = _savedHistory.value.filter { it.id != id }
    }

    fun clearHistory() {
        _savedHistory.value = emptyList()
    }

    override fun onCleared() {
        helper.close()
        tts?.stop()
        tts?.shutdown()
    }
}
