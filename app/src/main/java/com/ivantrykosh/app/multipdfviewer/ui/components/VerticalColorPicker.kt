package com.ivantrykosh.app.multipdfviewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivantrykosh.app.multipdfviewer.dimens.PdfViewerDimens

@Composable
internal fun VerticalColorPicker(
    modifier: Modifier = Modifier,
    colorPickerWidth: Dp = PdfViewerDimens.pickerWidth,
    pickedColor: Color,
    onSelectColor: (color: Color) -> Unit
) {
    val initialHsv = remember(pickedColor) {
        FloatArray(3).apply {
            android.graphics.Color.colorToHSV(
                android.graphics.Color.argb(
                    (pickedColor.alpha * 255).toInt(),
                    (pickedColor.red * 255).toInt(),
                    (pickedColor.green * 255).toInt(),
                    (pickedColor.blue * 255).toInt()
                ),
                this
            )
        }
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var mixPosition by remember {
        val incomingSaturation = initialHsv[1]
        val incomingValue = initialHsv[2]
        val pos = if (incomingValue < 1f) {
            0.5f + ((1f - incomingValue) / 2f)
        } else {
            incomingSaturation / 2f
        }
        mutableFloatStateOf(pos)
    }

    var barHeightPx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(hue, mixPosition) {
        val calculatedColor = if (mixPosition <= 0.5f) {
            val saturation = mixPosition * 2f
            Color.hsv(hue = hue, saturation = saturation, value = 1f)
        } else {
            val value = 1f - ((mixPosition - 0.5f) * 2f)
            Color.hsv(hue = hue, saturation = 1f, value = value)
        }
        onSelectColor(calculatedColor)
    }

    val pureHueColor = remember(hue) { Color.hsv(hue = hue, saturation = 1f, value = 1f) }
    val rainbowColors = remember {
        listOf(
            Color.Red, Color.Yellow, Color.Green,
            Color.Cyan, Color.Blue, Color.Magenta, Color.Red
        )
    }

    val mixerThumbColor = remember(hue, mixPosition) {
        val currentColor = if (mixPosition <= 0.5f) {
            val saturation = mixPosition * 2f
            Color.hsv(hue = hue, saturation = saturation, value = 1f)
        } else {
            val value = 1f - ((mixPosition - 0.5f) * 2f)
            Color.hsv(hue = hue, saturation = 1f, value = value)
        }

        Color(
            red = 1f - currentColor.red,
            green = 1f - currentColor.green,
            blue = 1f - currentColor.blue,
            alpha = 1f
        )
    }

    fun updateHue(y: Float, height: Int) {
        val fraction = (y / height).coerceIn(0f, 1f)
        hue = fraction * 360f
    }

    fun updateMix(y: Float, height: Int) {
        mixPosition = (y / height).coerceIn(0f, 1f)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(colorPickerWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(PdfViewerDimens.cornerRadiusBig))
                .background(brush = Brush.verticalGradient(colors = rainbowColors))
                .onGloballyPositioned { barHeightPx = it.size.height.toFloat() }
                .pointerInput(Unit) {
                    detectTapGestures(onPress = { offset -> updateHue(offset.y, size.height) })
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        updateHue(change.position.y, size.height)
                    }
                }
        ) {
            val thumbY = with(LocalDensity.current) {
                val fraction = hue / 360f
                (fraction * barHeightPx).toDp()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sliderHeight)
                    .offset(y = thumbY - sliderHeight / 2)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(PdfViewerDimens.sliderCornerRadius)
                    )
            )
        }

        Spacer(modifier = Modifier.width(PdfViewerDimens.spacingSmall))

        Box(
            modifier = Modifier
                .width(colorPickerWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(PdfViewerDimens.cornerRadiusBig))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White, pureHueColor, Color.Black)
                    )
                )
                .pointerInput(Unit) {
                    detectTapGestures(onPress = { offset -> updateMix(offset.y, size.height) })
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        updateMix(change.position.y, size.height)
                    }
                }
        ) {
            val thumbY = with(LocalDensity.current) {
                (mixPosition * barHeightPx).toDp()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sliderHeight)
                    .offset(y = thumbY - sliderHeight / 2)
                    .background(
                        color = mixerThumbColor,
                        shape = RoundedCornerShape(PdfViewerDimens.sliderCornerRadius)
                    )
            )
        }
    }
}

val sliderHeight = 6.dp

@Preview
@Composable
private fun VerticalColorPickerPreview() {
    val color = remember { mutableStateOf(Color.Blue) }
    VerticalColorPicker(
        modifier = Modifier.height(PdfViewerDimens.pickerHeight),
        pickedColor = color.value,
        onSelectColor = { color.value = it }
    )
}