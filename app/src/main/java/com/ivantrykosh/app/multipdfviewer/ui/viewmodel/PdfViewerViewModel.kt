package com.ivantrykosh.app.multipdfviewer.ui.viewmodel

import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import com.rizzi.bouquet.ResourceType
import com.rizzi.bouquet.VerticalPdfReaderState
import com.rizzi.bouquet.common.DrawingPath
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
                uiState.copy(
                    fileUri = fileUri,
                    leftViewReaderState = VerticalPdfReaderState(
                        resource = ResourceType.Local(fileUri),
                        isZoomEnable = true,
                        isAccessibleEnable = true
                    ),
                    rightViewReaderState = VerticalPdfReaderState(
                        resource = ResourceType.Local(fileUri),
                        isZoomEnable = true,
                        isAccessibleEnable = true
                    )
                )
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

    fun startDrawing(point: Offset, pageIndex: Int, isLeft: Boolean = true, pageWidth: Float, pageHeight: Float) {
        val relativePoint = Offset(
            x = point.x / pageWidth,
            y = point.y / pageHeight
        )

        if (isLeft) {
            _uiState.update { state ->
                state.copy(
                    currentPathPoints = listOf(relativePoint),
                    currentDrawingPageIndex = pageIndex
                )
            }
        } else {
            _uiState.update { state ->
                state.copy(
                    currentPathPoints2 = listOf(relativePoint),
                    currentDrawingPageIndex2 = pageIndex
                )
            }
        }
    }

    fun addPointToCurrentPath(point: Offset, isLeft: Boolean = true) {
        if (isLeft) {
            _uiState.update { state ->
                state.copy(
                    currentPathPoints = state.currentPathPoints + point
                )
            }
        } else {
            _uiState.update { state ->
                state.copy(
                    currentPathPoints2 = state.currentPathPoints2 + point
                )
            }
        }
    }

    fun addPointToCurrentPath(point: Offset, pageWidth: Float, pageHeight: Float, isLeft: Boolean = true) {
        _uiState.update { state ->
            val relativePoint = Offset(
                x = point.x / pageWidth,
                y = point.y / pageHeight
            )

            if (isLeft) {
                state.copy(
                    currentPathPoints = state.currentPathPoints + relativePoint
                )
            } else {
                state.copy(
                    currentPathPoints2 = state.currentPathPoints2 + relativePoint
                )
            }
        }
    }

    fun finishCurrentPath(isLeft: Boolean = true) {
        if (isLeft) {
            _uiState.update { state ->
                val pageIndex = state.currentDrawingPageIndex
                if (state.currentPathPoints.isNotEmpty() && pageIndex != null) {
                    state.copy(
                        drawingPaths = state.drawingPaths + DrawingPath(
                            points = state.currentPathPoints,
                            pageIndex = pageIndex
                        ),
                        currentPathPoints = emptyList(),
                        currentDrawingPageIndex = null
                    )
                } else {
                    state.copy(
                        currentPathPoints = emptyList(),
                        currentDrawingPageIndex = null
                    )
                }
            }
        } else {
            _uiState.update { state ->
                val pageIndex = state.currentDrawingPageIndex2
                if (state.currentPathPoints2.isNotEmpty() && pageIndex != null) {
                    state.copy(
                        drawingPaths2 = state.drawingPaths2 + DrawingPath(
                            points = state.currentPathPoints2,
                            pageIndex = pageIndex
                        ),
                        currentPathPoints2 = emptyList(),
                        currentDrawingPageIndex2 = null
                    )
                } else {
                    state.copy(
                        currentPathPoints2 = emptyList(),
                        currentDrawingPageIndex2 = null
                    )
                }
            }
        }
    }

    fun onDrawClick() {
        _uiState.update { uiState ->
            uiState.copy(isDrawingMode = uiState.isDrawingMode.not())
        }
    }
}

data class PdfViewerViewModelState(
    val fileUri: Uri? = null,
    val splitView: Boolean = false,
    val leftViewReaderState: VerticalPdfReaderState? = null,
    val rightViewReaderState: VerticalPdfReaderState? = null,
    val isDrawingMode: Boolean = false,
    val drawingPaths: List<DrawingPath> = emptyList(),
    val currentPathPoints: List<Offset> = emptyList(),
    val currentDrawingPageIndex: Int? = null,
    val drawingPaths2: List<DrawingPath> = emptyList(),
    val currentPathPoints2: List<Offset> = emptyList(),
    val currentDrawingPageIndex2: Int? = null
)