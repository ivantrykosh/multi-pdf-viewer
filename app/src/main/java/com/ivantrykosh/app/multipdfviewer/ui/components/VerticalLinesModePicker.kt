package com.ivantrykosh.app.multipdfviewer.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ivantrykosh.app.multipdfviewer.R
import com.ivantrykosh.app.multipdfviewer.dimens.PdfViewerDimens

enum class LinesMode(
    @field:DrawableRes val iconRes: Int,
    @field:StringRes val stringRes: Int
) {
    DRAW(
        iconRes = R.drawable.baseline_wavy_lines_24,
        stringRes = R.string.lines_mode_draw_title
    ),

    LINES(
        iconRes = R.drawable.baseline_intersecting_lines_24,
        stringRes = R.string.lines_mode_lines_title
    ),

    SNAPPED_LINES(
        iconRes = R.drawable.baseline_perpendicular_lines_24,
        stringRes = R.string.lines_mode_snapped_lines_title
    )
}

@Composable
internal fun VerticalLinesModePicker(
    modifier: Modifier = Modifier,
    currentLinesMode: LinesMode,
    onSelectLinesMode: (linesMode: LinesMode) -> Unit
) {
    val shape = RoundedCornerShape(PdfViewerDimens.cornerRadiusBig)

    Column(
        modifier = modifier
            .background(color = Color.LightGray, shape = shape)
            .border(width = PdfViewerDimens.borderTiny, color = Color.DarkGray, shape = shape),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinesMode.entries.reversed().forEach { linesMode ->
            IconButton(
                onClick = { onSelectLinesMode(linesMode) },
                painter = painterResource(linesMode.iconRes),
                contentDescription = stringResource(linesMode.stringRes),
                background = if (currentLinesMode == linesMode) Color.DarkGray else Color.LightGray,
                tint = if (currentLinesMode == linesMode) Color.White else Color.Black
            )
        }
    }
}

@Preview
@Composable
private fun VerticalLinesModePickerPreview() {
    VerticalLinesModePicker(
        modifier = Modifier.size(
            width = PdfViewerDimens.pickerWidth,
            height = PdfViewerDimens.pickerHeight
        ),
        currentLinesMode = LinesMode.LINES,
        onSelectLinesMode = {}
    )
}