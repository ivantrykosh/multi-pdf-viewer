package com.ivantrykosh.app.multipdfviewer.ui.viewmodel

import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.ivantrykosh.app.multipdfviewer.constants.PdfViewerConstants
import com.ivantrykosh.app.multipdfviewer.ui.components.DEFAULT_WIDTH
import com.ivantrykosh.app.multipdfviewer.ui.components.DrawingType
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

    fun startDrawing(point: Offset, pageIndex: Int, pageWidth: Float, pageHeight: Float, isLeft: Boolean = true) {
        val relativePoint = Offset(
            x = point.x / pageWidth,
            y = point.y / pageHeight
        )

        _uiState.update { uiState ->
            if (isLeft) {
                uiState.copy(
                    currentPathPoints = listOf(relativePoint),
                    currentDrawingPageIndex = pageIndex
                )
            } else {
                uiState.copy(
                    currentPathPoints2 = listOf(relativePoint),
                    currentDrawingPageIndex2 = pageIndex
                )
            }
        }
    }

    fun addPointToCurrentPath(point: Offset, pageWidth: Float, pageHeight: Float, isLeft: Boolean = true) {
        val relativePoint = Offset(
            x = point.x / pageWidth,
            y = point.y / pageHeight
        )

        _uiState.update { uiState ->
            if (isLeft) {
                uiState.copy(
                    currentPathPoints = uiState.currentPathPoints + relativePoint
                )
            } else {
                uiState.copy(
                    currentPathPoints2 = uiState.currentPathPoints2 + relativePoint
                )
            }
        }
    }

    fun finishCurrentPath(isLeft: Boolean = true) {
        if (isLeft) {
            _uiState.update { uiState ->
                val pageIndex = uiState.currentDrawingPageIndex
                if (uiState.currentPathPoints.isNotEmpty() && pageIndex != null) {
                    uiState.copy(
                        drawingPaths = uiState.drawingPaths + DrawingPath(
                            points = uiState.currentPathPoints,
                            pageIndex = pageIndex,
                            color = uiState.colorWithAlpha,
                            strokeWidth = uiState.currentWidth
                        ),
                        currentPathPoints = emptyList(),
                        currentDrawingPageIndex = null,
                        isLastDrawingOnLeft = true
                    )
                } else {
                    uiState.copy(
                        currentPathPoints = emptyList(),
                        currentDrawingPageIndex = null,
                        isLastDrawingOnLeft = true
                    )
                }
            }
        } else {
            _uiState.update { uiState ->
                val pageIndex = uiState.currentDrawingPageIndex2
                if (uiState.currentPathPoints2.isNotEmpty() && pageIndex != null) {
                    uiState.copy(
                        drawingPaths2 = uiState.drawingPaths2 + DrawingPath(
                            points = uiState.currentPathPoints2,
                            pageIndex = pageIndex,
                            color = uiState.colorWithAlpha,
                            strokeWidth = uiState.currentWidth
                        ),
                        currentPathPoints2 = emptyList(),
                        currentDrawingPageIndex2 = null,
                        isLastDrawingOnLeft = false
                    )
                } else {
                    uiState.copy(
                        currentPathPoints2 = emptyList(),
                        currentDrawingPageIndex2 = null,
                        isLastDrawingOnLeft = false
                    )
                }
            }
        }
    }

    fun onDrawClick() {
        _uiState.update { uiState ->
            uiState.copy(isDrawingMode = true)
        }
    }

    fun onSelectWidth(widthOption: Float) {
        _uiState.update { uiState ->
            uiState.copy(
                currentWidth = widthOption
            )
        }
    }

    fun onSelectColor(color: Color) {
        _uiState.update { uiState ->
            uiState.copy(
                currentColor = color
            )
        }
    }

    fun onPencilClick() {
        _uiState.update { uiState ->
            if (uiState.currentDrawingType == DrawingType.PENCIL) {
                uiState.copy(
                    widthPickerOpened = uiState.widthPickerOpened.not(),
                    colorPickerOpened = uiState.colorPickerOpened.not()
                )
            } else {
                uiState.copy(
                    currentDrawingType = DrawingType.PENCIL,
                    widthPickerOpened = true,
                    colorPickerOpened = true
                )
            }
        }
    }

    fun onHighlighterClick() {
        _uiState.update { uiState ->
            if (uiState.currentDrawingType == DrawingType.HIGHLIGHTER) {
                uiState.copy(
                    widthPickerOpened = uiState.widthPickerOpened.not(),
                    colorPickerOpened = uiState.colorPickerOpened.not()
                )
            } else {
                uiState.copy(
                    currentDrawingType = DrawingType.HIGHLIGHTER,
                    widthPickerOpened = true,
                    colorPickerOpened = true
                )
            }
        }
    }

    fun onUndoLastDrawClick() {
        _uiState.update { uiState ->
            if (uiState.isLastDrawingOnLeft || uiState.splitView.not()) {
                uiState.copy(
                    drawingPaths = uiState.drawingPaths.dropLast(1)
                )
            } else {
                uiState.copy(
                    drawingPaths2 = uiState.drawingPaths2.dropLast(1)
                )
            }
        }
    }

    fun onExitDrawingClick() {
        _uiState.update { uiState ->
            uiState.copy(
                isDrawingMode = false,
                colorPickerOpened = false,
                widthPickerOpened = false
            )
        }
    }

    fun onEraserClick() {
        _uiState.update { uiState ->
            if (uiState.currentDrawingType == DrawingType.ERASER) {
                uiState.copy(
                    widthPickerOpened = uiState.widthPickerOpened.not(),
                    colorPickerOpened = false
                )
            } else {
                uiState.copy(
                    currentDrawingType = DrawingType.ERASER,
                    widthPickerOpened = true,
                    colorPickerOpened = false
                )
            }
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
    val currentDrawingPageIndex2: Int? = null,
    val isLastDrawingOnLeft: Boolean = true,
    val currentWidth: Float = DEFAULT_WIDTH,
    val currentColor: Color = Color.Black,
    val currentDrawingType: DrawingType = DrawingType.PENCIL,
    val colorPickerOpened: Boolean = false,
    val widthPickerOpened: Boolean = false
) {
    val colorWithAlpha = when (currentDrawingType) {
        DrawingType.PENCIL -> currentColor
        DrawingType.HIGHLIGHTER -> currentColor.copy(alpha = PdfViewerConstants.HIGHLIGHTER_COLOR_ALPHA)
        DrawingType.ERASER -> Color.Transparent
    }
}