package com.ivantrykosh.app.multipdfviewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ivantrykosh.app.multipdfviewer.dimens.PdfViewerDimens

enum class WidthOption(val width: Float) {
    WIDTH_1(1f),
    WIDTH_3(3f),
    WIDTH_6(6f),
    WIDTH_12(12f),
    WIDTH_20(20f),
    WIDTH_32(32f)
}

@Composable
internal fun VerticalWidthPicker(
    modifier: Modifier = Modifier,
    selectedWidth: WidthOption,
    onSelectWidth: (width: WidthOption) -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(PdfViewerDimens.cornerRadiusBig))
            .background(Color.LightGray)
            .padding(vertical = PdfViewerDimens.spacingSmall, horizontal = PdfViewerDimens.spacingTiny),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        WidthOption.entries.reversed().forEach { widthItem ->
            val isSelected = widthItem == selectedWidth

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(
                        color = if (isSelected) Color.DarkGray else Color.Transparent
                    )
                    .clickable { onSelectWidth(widthItem) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(WIDTH_INDICATOR_WIDTH_FRACTION)
                        .height(maxOf(minIndicatorHeight, (widthItem.width / 1.5f).dp))
                        .background(
                            if (isSelected) Color.White else Color.Gray,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

private val minIndicatorHeight = 1.dp
private const val WIDTH_INDICATOR_WIDTH_FRACTION = 0.75f

@Preview
@Composable
private fun VerticalWidthPickerPreview() {
    VerticalWidthPicker(
        modifier = Modifier.size(
            width = PdfViewerDimens.pickerWidth,
            height = PdfViewerDimens.pickerHeight
        ),
        selectedWidth = WidthOption.WIDTH_3,
        onSelectWidth = {}
    )
}