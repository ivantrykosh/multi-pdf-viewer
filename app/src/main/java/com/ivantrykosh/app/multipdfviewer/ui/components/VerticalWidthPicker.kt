package com.ivantrykosh.app.multipdfviewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import com.ivantrykosh.app.multipdfviewer.dimens.PdfViewerDimens

const val DEFAULT_WIDTH = 3f
val widthRange = 1f..50f

@Composable
internal fun VerticalWidthPicker(
    modifier: Modifier = Modifier,
    selectedWidth: Float,
    onSelectWidth: (width: Float) -> Unit,
    activeColor: Color
) {
    var barHeightPx by remember { mutableFloatStateOf(0f) }

    val minWidth = widthRange.start
    val maxWidth = widthRange.endInclusive

    val fraction = (selectedWidth - minWidth) / (maxWidth - minWidth)
    val invertedFraction = 1f - fraction

    fun updateWidthFromY(y: Float, height: Int) {
        val touchFraction = (y / height).coerceIn(0f, 1f)
        val invertedTouchFraction = 1f - touchFraction
        val calculatedWidth = minWidth + (invertedTouchFraction * (maxWidth - minWidth))
        onSelectWidth(calculatedWidth)
    }

    val thumbColor = remember(activeColor) {
        Color(
            red = 1f - activeColor.red,
            green = 1f - activeColor.green,
            blue = 1f - activeColor.blue,
            alpha = 1f
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(PdfViewerDimens.cornerRadiusNormalSpecial))
            .background(color = activeColor.copy(alpha = BACKGROUND_COLOR_ALPHA))
            .onGloballyPositioned { barHeightPx = it.size.height.toFloat() }
            .pointerInput(Unit) {
                detectTapGestures(onPress = { offset -> updateWidthFromY(offset.y, size.height) })
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    updateWidthFromY(change.position.y, size.height)
                }
            }
            .drawWithContent {
                val centerX = size.width / 2f
                val minHalfWidth = minWidth / 2
                val maxHalfWidth = maxWidth / 2

                val trianglePath = Path().apply {
                    moveTo(centerX - maxHalfWidth, 0f)
                    lineTo(centerX + maxHalfWidth, 0f)

                    lineTo(centerX + minHalfWidth, size.height)
                    lineTo(centerX - minHalfWidth, size.height)
                    close()
                }

                drawPath(
                    path = trianglePath,
                    color = activeColor
                )

                drawContent()
            }
    ) {
        val thumbY = with(LocalDensity.current) {
            (invertedFraction * barHeightPx).toDp()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(sliderHeight)
                .offset(y = thumbY - sliderHeight / 2)
                .background(
                    color = thumbColor,
                    shape = RoundedCornerShape(PdfViewerDimens.sliderCornerRadius)
                )
        )
    }
}

private const val BACKGROUND_COLOR_ALPHA = 0.25F

@Preview
@Composable
private fun VerticalWidthPickerPreview() {
    VerticalWidthPicker(
        modifier = Modifier.size(
            width = PdfViewerDimens.pickerWidth,
            height = PdfViewerDimens.pickerHeight
        ),
        selectedWidth = 50f,
        onSelectWidth = {},
        activeColor = Color.Red
    )
}