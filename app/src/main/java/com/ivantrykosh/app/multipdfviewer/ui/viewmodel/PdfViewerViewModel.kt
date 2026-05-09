package com.ivantrykosh.app.multipdfviewer.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class PdfViewerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        value = PdfViewerViewModelState()
    )

    val uiState = _uiState.asStateFlow()

    fun onFilePicked(fileUri: Uri?) {
        fileUri?.let { fileUri ->
            _uiState.update { uiState ->
                uiState.copy(fileUri = fileUri)
            }
        }
    }

    fun onSplitViewButtonClicked() {
        _uiState.update { uiState ->
            uiState.copy(splitView = true)
        }
    }

    fun onSingleViewButtonClicked() {
        _uiState.update { uiState ->
            uiState.copy(splitView = false)
        }
    }
}

data class PdfViewerViewModelState(
    val fileUri: Uri? = null,
    val splitView: Boolean = false
)