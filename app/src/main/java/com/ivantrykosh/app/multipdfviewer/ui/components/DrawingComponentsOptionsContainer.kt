package com.ivantrykosh.app.multipdfviewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ivantrykosh.app.multipdfviewer.R
import com.ivantrykosh.app.multipdfviewer.dimens.PdfViewerDimens

enum class DrawingType {
    PENCIL, HIGHLIGHTER, ERASER
}

@Composable
internal fun DrawingComponentsOptionsContainer(
    modifier: Modifier = Modifier,
    drawingType: DrawingType,
    onPencilClick: () -> Unit,
    onHighlighterClick: () -> Unit,
    onEraserClick: () -> Unit,
    onUndoLastDrawClick: () -> Unit,
    onExitDrawingClick: () -> Unit
) {
    val shape = RoundedCornerShape(PdfViewerDimens.cornerRadiusNormal)

    Row(
        modifier = modifier
            .background(color = Color.LightGray, shape = shape)
            .border(width = PdfViewerDimens.borderTiny, color = Color.DarkGray, shape = shape)
            .padding(PdfViewerDimens.spacingTiny),
        horizontalArrangement = Arrangement.spacedBy(PdfViewerDimens.spacingSmall)
    ) {
        IconButton(
            onClick = onPencilClick,
            painter = painterResource(R.drawable.baseline_draw_24),
            contentDescription = stringResource(R.string.pencil_button_title),
            background = if (drawingType == DrawingType.PENCIL) Color.DarkGray else Color.LightGray,
            tint = if (drawingType == DrawingType.PENCIL) Color.White else Color.Black
        )

        IconButton(
            onClick = onHighlighterClick,
            painter = painterResource(R.drawable.outline_stylus_highlighter_24),
            contentDescription = stringResource(R.string.highlighter_button_title),
            background = if (drawingType == DrawingType.HIGHLIGHTER) Color.DarkGray else Color.LightGray,
            tint = if (drawingType == DrawingType.HIGHLIGHTER) Color.White else Color.Black
        )

        IconButton(
            onClick = onEraserClick,
            painter = painterResource(R.drawable.outline_eraser_24),
            contentDescription = stringResource(R.string.eraser_button_title),
            background = if (drawingType == DrawingType.ERASER) Color.DarkGray else Color.LightGray,
            tint = if (drawingType == DrawingType.ERASER) Color.White else Color.Black
        )

        IconButton(
            onClick = onUndoLastDrawClick,
            painter = painterResource(R.drawable.outline_undo_24),
            contentDescription = stringResource(R.string.undo_last_drawing_button_title),
            background = Color.LightGray,
            tint = Color.Black
        )

        IconButton(
            onClick = onExitDrawingClick,
            painter = painterResource(R.drawable.baseline_cancel_24),
            contentDescription = stringResource(R.string.cancel_button_title),
            background = Color.LightGray,
            tint = Color.Black
        )
    }
}

@Composable
private fun IconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    painter: Painter?,
    contentDescription: String? = null,
    tint: Color = LocalContentColor.current,
    background: Color = Color.White
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(color = background)
        )

        IconButton(
            onClick = onClick,
            modifier = Modifier.size(PdfViewerDimens.iconSizeLarge)
        ) {
            painter?.let { painter ->
                Icon(
                    painter = painter,
                    contentDescription = contentDescription,
                    tint = tint
                )
            }
        }
    }
}

@Preview
@Composable
private fun DrawingComponentsOptionsContainerPreview() {
    DrawingComponentsOptionsContainer(
        drawingType = DrawingType.PENCIL,
        onUndoLastDrawClick = {},
        onExitDrawingClick = {},
        onPencilClick = {},
        onHighlighterClick = {},
        onEraserClick = {}
    )
}