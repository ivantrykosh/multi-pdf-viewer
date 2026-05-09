package com.ivantrykosh.app.multipdfviewer.ui.route

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ivantrykosh.app.multipdfviewer.R
import com.ivantrykosh.app.multipdfviewer.dimens.PdfViewerDimens
import com.ivantrykosh.app.multipdfviewer.ui.theme.Typography
import com.ivantrykosh.app.multipdfviewer.ui.viewmodel.PdfViewerViewModel
import com.ivantrykosh.app.multipdfviewer.ui.viewmodel.PdfViewerViewModelState
import com.rajat.pdfviewer.compose.PdfRendererViewCompose
import com.rajat.pdfviewer.util.PdfSource

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
        onFilePicked = pdfViewerViewModel::onFilePicked
    )
}

@Composable
fun PdfViewScreen(
    uiState: PdfViewerViewModelState,
    onSplitViewButtonClick: () -> Unit,
    onSingleViewButtonClick: () -> Unit,
    onFilePicked: (fileUri: Uri?) -> Unit
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
            uiState.fileUri?.let { fileUri ->
                PdfRendererViewCompose(
                    source = PdfSource.LocalUri(fileUri),
                    modifier = Modifier.weight(1f)
                )

                if (uiState.splitView) {
                    Spacer(modifier = Modifier.width(PdfViewerDimens.spacingSmall))
                }

                AnimatedVisibility(
                    modifier = Modifier.weight(1f),
                    visible = uiState.splitView
                ) {
                    PdfRendererViewCompose(
                        source = PdfSource.LocalUri(fileUri)
                    )
                }
            } ?: Text(
                modifier = Modifier.fillMaxSize().wrapContentSize(),
                text = stringResource(R.string.choose_file_label),
                style = Typography.titleLarge
            )
        }

        Column(
            modifier = Modifier.align(Alignment.TopEnd).padding(PdfViewerDimens.spacingNormal),
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
            }
        }
    }
}

@Preview
@Composable
private fun PdfViewScreenPreview() {
    PdfViewScreen(
        uiState = PdfViewerViewModelState(),
        onSingleViewButtonClick = {},
        onSplitViewButtonClick = {},
        onFilePicked = {}
    )
}