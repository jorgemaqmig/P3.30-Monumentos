package com.example.p3_30_monumentos_gabriel_jorge.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.p3_30_monumentos_gabriel_jorge.R
import com.example.p3_30_monumentos_gabriel_jorge.data.Datasource
import com.example.p3_30_monumentos_gabriel_jorge.model.Monumento
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface UiState {
        data class Loading(val progress: Float = 0f) : UiState
        data class Success(val monumentos: List<Monumento>) : UiState
        data class Error(val message: String) : UiState
}

// Se gestiona la traducción de los datos de la aplicación
class MonumentosViewModel(application: Application) : AndroidViewModel(application) {

        private fun getAllStringIds(): List<Int> {
                val ids = mutableListOf<Int>()
                try {
                        val fields = R.string::class.java.fields
                        for (field in fields) {
                                if (field.type == Int::class.javaPrimitiveType ||
                                                field.type == Int::class.java
                                ) {
                                        ids.add(field.getInt(null))
                                }
                        }
                } catch (e: Exception) {
                        e.printStackTrace()
                }
                return ids
        }

        private val _uiState = MutableStateFlow<UiState>(UiState.Loading())
        val uiState: StateFlow<UiState> = _uiState.asStateFlow()

        private val _uiStrings = MutableStateFlow<Map<Int, String>>(emptyMap())
        val uiStrings: StateFlow<Map<Int, String>> = _uiStrings.asStateFlow()

        init {
                loadAndTranslateMonuments()
        }

        // Actualiza el idioma de traducción
        fun updateLanguage(languageMode: String) {
                loadAndTranslateMonuments(languageMode)
        }

        // Carga y traduce los datos de los monumentos
        private fun loadAndTranslateMonuments(languageMode: String = "SYSTEM") {
                viewModelScope.launch {
                        _uiState.value = UiState.Loading(0f)

                        val originalList = Datasource(getApplication()).loadMonuments()

                        val targetIsoCode =
                                if (languageMode == "SYSTEM") {
                                        Locale.getDefault().language
                                } else {
                                        languageMode
                                }

                        println("Target Language: $targetIsoCode (Mode: $languageMode)")

                        if (targetIsoCode == "es") {
                                _uiState.value = UiState.Success(originalList)
                                _uiStrings.value = emptyMap()
                                return@launch
                        }

                        val targetLanguage =
                                when (targetIsoCode) {
                                        "en" -> TranslateLanguage.ENGLISH
                                        "fr" -> TranslateLanguage.FRENCH
                                        "de" -> TranslateLanguage.GERMAN
                                        "it" -> TranslateLanguage.ITALIAN
                                        "pt" -> TranslateLanguage.PORTUGUESE
                                        else -> TranslateLanguage.ENGLISH
                                }

                        translateAll(originalList, targetLanguage)
                }
        }

        // Traduce todos los datos
        private suspend fun translateAll(monuments: List<Monumento>, targetLang: String) {
                val options =
                        TranslatorOptions.Builder()
                                .setSourceLanguage(TranslateLanguage.SPANISH)
                                .setTargetLanguage(targetLang)
                                .build()

                val translator = Translation.getClient(options)
                val conditions = DownloadConditions.Builder().build()

                try {
                        translator.downloadModelIfNeeded(conditions).await()

                        val translatedMonuments = ArrayList<Monumento>()
                        val total = monuments.size
                        for ((index, monument) in monuments.withIndex()) {
                                val tituloTraducido = translator.translate(monument.titulo).await()
                                val descTraducida =
                                        translator.translate(monument.descripcion).await()
                                val paisTraducido = translator.translate(monument.pais).await()

                                translatedMonuments.add(
                                        monument.copy(
                                                titulo = tituloTraducido,
                                                descripcion = descTraducida,
                                                pais = paisTraducido
                                        )
                                )

                                val progress = (index + 1).toFloat() / (total + 1)
                                _uiState.value = UiState.Loading(progress)
                        }

                        val newUiStringsMap = mutableMapOf<Int, String>()

                        for (id in getAllStringIds()) {
                                try {
                                        val originalText =
                                                getApplication<Application>().getString(id)
                                        val translatedText =
                                                translator.translate(originalText).await()
                                        newUiStringsMap[id] = translatedText
                                } catch (e: Exception) {

                                }
                        }

                        _uiStrings.value = newUiStringsMap
                        _uiState.value = UiState.Success(translatedMonuments)
                } catch (e: Exception) {
                        e.printStackTrace()
                        _uiState.value = UiState.Success(monuments)
                        _uiStrings.value = emptyMap()
                } finally {
                        translator.close()
                }
        }
}
