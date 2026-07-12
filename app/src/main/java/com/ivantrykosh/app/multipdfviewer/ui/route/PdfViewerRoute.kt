package com.ivantrykosh.app.multipdfviewer.ui.route

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ivantrykosh.app.multipdfviewer.R
import com.ivantrykosh.app.multipdfviewer.dimens.PdfViewerDimens
import com.ivantrykosh.app.multipdfviewer.ui.components.DrawingComponentsOptionsContainer
import com.ivantrykosh.app.multipdfviewer.ui.components.VerticalColorPicker
import com.ivantrykosh.app.multipdfviewer.ui.components.VerticalWidthPicker
import com.ivantrykosh.app.multipdfviewer.ui.components.WidthOption
import com.ivantrykosh.app.multipdfviewer.ui.theme.Typography
import com.ivantrykosh.app.multipdfviewer.ui.viewmodel.PdfViewerViewModel
import com.ivantrykosh.app.multipdfviewer.ui.viewmodel.PdfViewerViewModelState
import com.rizzi.bouquet.VerticalPDFReader

private const val PDF_MIME = "application/pdf"

@Composable
internal fun PdfViewRoute(
    pdfViewerViewModel: PdfViewerViewModel
) {
    val uiState by pdfViewerViewModel.uiState.collectAsStateWithLifecycle()

    PdfViewScreen(
        uiState = uiState,
        onSplitViewButtonClick = pdfViewerViewModel::onSplitViewButtonClicked,
        onSingleViewButtonClick = pdfViewerViewModel::onSingleViewButtonClicked,
        onFilePicked = pdfViewerViewModel::onFilePicked,
        startDrawing = pdfViewerViewModel::startDrawing,
        addPointToCurrentPath = pdfViewerViewModel::addPointToCurrentPath,
        finishCurrentPath = pdfViewerViewModel::finishCurrentPath,
        onDrawClick = pdfViewerViewModel::onDrawClick,
        onPencilClick = pdfViewerViewModel::onPencilClick,
        onHighlighterClick = pdfViewerViewModel::onHighlighterClick,
        onUndoLastDrawClick = pdfViewerViewModel::onUndoLastDrawClick,
        onExitDrawingClick = pdfViewerViewModel::onExitDrawingClick,
        onSelectColor = pdfViewerViewModel::onSelectColor,
        onSelectWidth = pdfViewerViewModel::onSelectWidth,
        onEraserClick = pdfViewerViewModel::onEraserClick
    )
}

@Composable
fun PdfViewScreen(
    uiState: PdfViewerViewModelState,
    onSplitViewButtonClick: () -> Unit,
    onSingleViewButtonClick: () -> Unit,
    onFilePicked: (fileUri: Uri?) -> Unit,
    startDrawing: (point: Offset, pageIndex: Int, pageWidth: Float, pageHeight: Float, isLeft: Boolean) -> Unit,
    addPointToCurrentPath: (point: Offset, pageWidth: Float, pageHeight: Float, isLeft: Boolean) -> Unit,
    finishCurrentPath: (isLeft: Boolean) -> Unit,
    onDrawClick: () -> Unit,
    onPencilClick: () -> Unit,
    onHighlighterClick: () -> Unit,
    onEraserClick: () -> Unit,
    onUndoLastDrawClick: () -> Unit,
    onExitDrawingClick: () -> Unit,
    onSelectColor: (color: Color) -> Unit,
    onSelectWidth: (width: WidthOption) -> Unit
) {
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        onFilePicked(uri)
    }

    val launchFilePicker = {
        filePicker.launch(arrayOf(PDF_MIME))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = PdfViewerDimens.spacingNormal)
        ) {
            uiState.fileUri?.let {
                uiState.leftViewReaderState?.let { readerState ->
                    VerticalPDFReader(
                        state = readerState,
                        modifier = Modifier.weight(1f),
                        isDrawingMode = uiState.isDrawingMode,
                        drawingPaths = uiState.drawingPaths,
                        currentDrawingPageIndex = uiState.currentDrawingPageIndex ?: 0,
                        currentPathPoints = uiState.currentPathPoints,
                        onDrawStart = { point, page, width, height -> startDrawing(point, page, width, height, true) },
                        onDraw = { point, width, height -> addPointToCurrentPath(point,width, height,true) },
                        onDrawEnd = { finishCurrentPath(true) },
                        currentStrokeColor = uiState.colorWithAlpha,
                        currentStrokeWidth = uiState.currentWidth.width
                    )
                }

                if (uiState.splitView) {
                    Spacer(modifier = Modifier
                        .width(PdfViewerDimens.spacingSmall)
                        .zIndex(1f))
                }

                AnimatedVisibility(
                    modifier = Modifier.weight(1f),
                    visible = uiState.splitView
                ) {
                    uiState.rightViewReaderState?.let { readerState ->
                        VerticalPDFReader(
                            state = readerState,
                            modifier = Modifier,
                            isDrawingMode = uiState.isDrawingMode,
                            drawingPaths = uiState.drawingPaths2,
                            currentDrawingPageIndex = uiState.currentDrawingPageIndex2 ?: 0,
                            currentPathPoints = uiState.currentPathPoints2,
                            onDrawStart = { point, page, width, height -> startDrawing(point, page, width, height, false) },
                            onDraw = { point, width, height -> addPointToCurrentPath(point, width, height,false) },
                            onDrawEnd = { finishCurrentPath(false) },
                            currentStrokeColor = uiState.colorWithAlpha,
                            currentStrokeWidth = uiState.currentWidth.width
                        )
                    }
                }
            } ?: Text(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize()
                    .clickable(onClick = launchFilePicker),
                text = stringResource(R.string.choose_file_label),
                style = Typography.titleLarge
            )
        }

        Text(
            text = if (uiState.isDrawingMode) {
                stringResource(R.string.drawing_mode)
            } else {
                stringResource(R.string.viewing_mode)
            },
            modifier = Modifier.align(Alignment.TopCenter),
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(PdfViewerDimens.spacingNormal),
            verticalArrangement = Arrangement.spacedBy(PdfViewerDimens.spacingSmall)
        ) {
            IconButton(
                onClick = launchFilePicker
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_upload_file_24),
                    contentDescription = stringResource(R.string.split_view_button_title)
                )
            }

            if (uiState.fileUri != null) {
                IconButton(
                    onClick = if (uiState.splitView) onSingleViewButtonClick else onSplitViewButtonClick
                ) {
                    if (uiState.splitView) {
                        Icon(
                            painter = painterResource(R.drawable.outline_split_scene_right_24),
                            contentDescription = stringResource(R.string.single_view_button_title)
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.outline_split_scene_24),
                            contentDescription = stringResource(R.string.split_view_button_title)
                        )
                    }
                }

                if (uiState.isDrawingMode.not()) {
                    IconButton(
                        onClick = onDrawClick
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_draw_24),
                            contentDescription = stringResource(R.string.enter_draw_mode_button_title)
                        )
                    }
                }
            }
        }

        if (uiState.isDrawingMode) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = PdfViewerDimens.spacingBig)
            ) {
                Row(
                    modifier = Modifier.padding(PdfViewerDimens.spacingTiny),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Spacer(modifier = Modifier.width(PdfViewerDimens.pickerWidth * 2 + PdfViewerDimens.spacingSmall))

                        AnimatedVisibility(
                            visible = uiState.colorPickerOpened,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            VerticalColorPicker(
                                modifier = Modifier.height(PdfViewerDimens.pickerHeight),
                                pickedColor = uiState.currentColor,
                                onSelectColor = onSelectColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(PdfViewerDimens.spacingSmall))

                    AnimatedVisibility(
                        visible = uiState.widthPickerOpened,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        VerticalWidthPicker(
                            modifier = Modifier.size(
                                width = PdfViewerDimens.pickerWidth,
                                height = PdfViewerDimens.pickerHeight
                            ),
                            selectedWidth = uiState.currentWidth,
                            onSelectWidth = onSelectWidth
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PdfViewerDimens.spacingSmall))

                DrawingComponentsOptionsContainer(
                    modifier = Modifier.padding(horizontal = PdfViewerDimens.spacingLarge),
                    drawingType = uiState.currentDrawingType,
                    onPencilClick = onPencilClick,
                    onHighlighterClick = onHighlighterClick,
                    onEraserClick = onEraserClick,
                    onUndoLastDrawClick = onUndoLastDrawClick,
                    onExitDrawingClick = onExitDrawingClick
                )
            }
        }
    }
}

@Preview
@Composable
private fun PdfViewScreenPreview() {
    PdfViewScreen(
        uiState = PdfViewerViewModelState(
            isDrawingMode = true,
            widthPickerOpened = true,
            colorPickerOpened = true
        ),
        onSingleViewButtonClick = {},
        onSplitViewButtonClick = {},
        onFilePicked = {},
        addPointToCurrentPath = { _, _, _, _ -> },
        finishCurrentPath = {},
        startDrawing = { _, _, _, _, _ -> },
        onDrawClick = {},
        onPencilClick = {},
        onHighlighterClick = {},
        onUndoLastDrawClick = {},
        onExitDrawingClick = {},
        onSelectColor = {},
        onSelectWidth = {},
        onEraserClick = {}
    )
}