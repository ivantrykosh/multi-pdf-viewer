package com.rizzi.bouquet

import android.content.Context
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.rizzi.bouquet.common.DrawingPath
import com.rizzi.bouquet.network.getDownloadInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

@Composable
fun VerticalPDFReader(
    state: VerticalPdfReaderState,
    modifier: Modifier,
    isDrawingMode: Boolean,
    drawingPaths: List<DrawingPath>,
    currentDrawingPageIndex: Int,
    currentPathPoints: List<Offset>,
    onDrawStart: (point: Offset, pageIndex: Int, pageWidth: Float, pageHeight: Float) -> Unit,
    onDraw: (point: Offset, pageWidth: Float, pageHeight: Float) -> Unit,
    onDrawEnd: () -> Unit
) {
    var readerWidth by remember { mutableStateOf(0f) }
    var readerHeight by remember { mutableStateOf(0f) }

    BoxWithConstraints(
        modifier = modifier
            .clipToBounds()
            .onGloballyPositioned { coordinates ->
                readerWidth = coordinates.size.width.toFloat()
                readerHeight = coordinates.size.height.toFloat()
            },
        contentAlignment = Alignment.TopCenter
    ) {
        val ctx = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val lazyState = state.lazyState

        DisposableEffect(key1 = Unit) {
            load(
                coroutineScope,
                ctx,
                state,
                constraints.maxWidth,
                constraints.maxHeight,
                true
            )

            onDispose {
                state.close()
            }
        }

        state.pdfRender?.let { pdf ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .tapToZoomVertical(
                        state = state,
                        isDrawingMode = isDrawingMode,
                        boxWidth = if (readerWidth > 0f) readerWidth else constraints.maxWidth.toFloat(),
                        boxHeight = if (readerHeight > 0f) readerHeight else constraints.maxHeight.toFloat()
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                state = lazyState
            ) {
                items(pdf.pageCount) { pageIndex ->
                    val pageContent = pdf.pageLists[pageIndex].stateFlow.collectAsState().value
                    DisposableEffect(key1 = Unit) {
                        pdf.pageLists[pageIndex].load()
                        onDispose {
                            pdf.pageLists[pageIndex].recycle()
                        }
                    }

                    var pageWidth by remember { mutableStateOf<Float?>(null) }
                    var pageHeight by remember { mutableStateOf<Float?>(null) }
                    Box(
                        modifier = Modifier
                            .onGloballyPositioned {
                                pageWidth = it.size.width.toFloat()
                                pageHeight = it.size.height.toFloat()
                            }
                    ) {
                        when (pageContent) {
                            is PageContentInt.PageContent -> {
                                PdfImage(
                                    bitmap = { pageContent.bitmap.asImageBitmap() },
                                    contentDescription = pageContent.contentDescription
                                )
                            }

                            is PageContentInt.BlankPage -> BlackPage(
                                width = pageContent.width,
                                height = pageContent.height
                            )
                        }

                        Canvas(modifier = Modifier.matchParentSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height

                            clipRect(
                                left = 0f,
                                top = 0f,
                                right = canvasWidth,
                                bottom = canvasHeight
                            ) {
                                drawingPaths
                                    .filter { it.pageIndex == pageIndex }
                                    .forEach { path ->
                                        if (path.points.isEmpty()) return@forEach

                                        if (path.points.size == 1) {
                                            val point = path.points.first()
                                            drawCircle(
                                                color = path.color,
                                                radius = 2.5f,
                                                center = Offset(point.x * canvasWidth, point.y * canvasHeight)
                                            )
                                        } else {
                                            val scaledPath = Path().apply {
                                                val firstPoint = path.points.first()
                                                moveTo(firstPoint.x * canvasWidth, firstPoint.y * canvasHeight)
                                                path.points.drop(1).forEach { p ->
                                                    lineTo(p.x * canvasWidth, p.y * canvasHeight)
                                                }
                                            }

                                            drawPath(
                                                path = scaledPath,
                                                color = path.color,
                                                style = Stroke(width = 5f)
                                            )
                                        }
                                    }

                                if (currentDrawingPageIndex == pageIndex && currentPathPoints.isNotEmpty()) {
                                    if (currentPathPoints.size == 1) {
                                        val point = currentPathPoints.first()
                                        drawCircle(
                                            color = Color.Blue,
                                            radius = 2.5f,
                                            center = Offset(point.x * canvasWidth, point.y * canvasHeight)
                                        )
                                    } else {
                                        val scaledPath = Path().apply {
                                            val firstPoint = currentPathPoints.first()
                                            moveTo(firstPoint.x * canvasWidth, firstPoint.y * canvasHeight)
                                            currentPathPoints.drop(1).forEach { p ->
                                                lineTo(p.x * canvasWidth, p.y * canvasHeight)
                                            }
                                        }

                                        drawPath(
                                            path = scaledPath,
                                            color = Color.Blue,
                                            style = Stroke(width = 5f)
                                        )
                                    }
                                }
                            }
                        }

                        if (isDrawingMode) {
                            Spacer(
                                modifier = Modifier
                                    .matchParentSize()
                                    .pointerInput(Unit) {
                                        awaitEachGesture {
                                            val down = awaitFirstDown(requireUnconsumed = false)

                                            onDrawStart(down.position, pageIndex, pageWidth ?: 0f, pageHeight ?: 0f)

                                            var pointer = down
                                            while (true) {
                                                val event = awaitPointerEvent()
                                                val anyPressed = event.changes.any { it.pressed }

                                                if (!anyPressed) break

                                                val pointerChange = event.changes.firstOrNull { it.id == pointer.id } ?: break

                                                if (pointerChange.positionChanged()) {
                                                    pointerChange.consume()
                                                    onDraw(pointerChange.position, pageWidth ?: 0f, pageHeight ?: 0f)
                                                    pointer = pointerChange
                                                }
                                            }

                                            onDrawEnd()
                                        }
                                    }
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(color = Color.Gray).align(Alignment.BottomCenter))
                    }
                }
            }
        }
    }
}

@Composable
fun Int.dp(): Dp {
    val density = LocalDensity.current.density
    return (this / density).dp
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HorizontalPDFReader(
    state: HorizontalPdfReaderState,
    modifier: Modifier
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        val ctx = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        DisposableEffect(key1 = Unit) {
            load(
                coroutineScope,
                ctx,
                state,
                constraints.maxWidth,
                constraints.maxHeight,
                constraints.maxHeight > constraints.maxWidth
            )
            onDispose {
                state.close()
            }
        }
        state.pdfRender?.let { pdf ->
            HorizontalPager(
                modifier = Modifier
                    .fillMaxSize()
                    .tapToZoomHorizontal(state, constraints),
                count = state.pdfPageCount,
                state = state.pagerState,
                userScrollEnabled = state.scale == 1f
            ) { page ->
                val pageContent = pdf.pageLists[page].stateFlow.collectAsState().value
                DisposableEffect(key1 = Unit) {
                    pdf.pageLists[page].load()
                    onDispose {
                        pdf.pageLists[page].recycle()
                    }
                }
                when (pageContent) {
                    is PageContentInt.PageContent -> {
                        PdfImage(
                            bitmap = { pageContent.bitmap.asImageBitmap() },
                            contentDescription = pageContent.contentDescription
                        )
                    }

                    is PageContentInt.BlankPage -> BlackPage(
                        width = pageContent.width,
                        height = pageContent.height
                    )
                }
            }
        }
    }
}

private fun load(
    coroutineScope: CoroutineScope,
    context: Context,
    state: PdfReaderState,
    width: Int,
    height: Int,
    portrait: Boolean
) {
    runCatching {
        if (state.isLoaded) {
            coroutineScope.launch(Dispatchers.IO) {
                runCatching {
                    val pFD =
                        ParcelFileDescriptor.open(state.mFile, ParcelFileDescriptor.MODE_READ_ONLY)
                    val textForEachPage =
                        if (state.isAccessibleEnable) getTextByPage(context, pFD) else emptyList()
                    state.pdfRender =
                        BouquetPdfRender(pFD, textForEachPage, width, height, portrait)
                }.onFailure {
                    state.mError = it
                }
            }
        } else {
            when (val res = state.resource) {
                is ResourceType.Local -> {
                    coroutineScope.launch(Dispatchers.IO) {
                        runCatching {
                            context.contentResolver.openFileDescriptor(res.uri, "r")?.let {
                                val textForEachPage = if (state.isAccessibleEnable) {
                                    getTextByPage(context, it)
                                } else emptyList()
                                state.pdfRender =
                                    BouquetPdfRender(it, textForEachPage, width, height, portrait)
                                state.mFile = context.uriToFile(res.uri)
                            } ?: throw IOException("File not found")
                        }.onFailure {
                            state.mError = it
                        }
                    }
                }

                is ResourceType.Remote -> {
                    coroutineScope.launch(Dispatchers.IO) {
                        runCatching {
                            val bufferSize = 8192
                            var downloaded = 0
                            val file = File(context.cacheDir, generateFileName())
                            val response = getDownloadInterface(
                                res.headers
                            ).downloadFile(
                                res.url
                            )
                            val byteStream = response.byteStream()
                            byteStream.use { input ->
                                file.outputStream().use { output ->
                                    val totalBytes = response.contentLength()
                                    var data = ByteArray(bufferSize)
                                    var count = input.read(data)
                                    while (count != -1) {
                                        if (totalBytes > 0) {
                                            downloaded += bufferSize
                                            state.mLoadPercent =
                                                (downloaded * (100 / totalBytes.toFloat())).toInt()
                                        }
                                        output.write(data, 0, count)
                                        data = ByteArray(bufferSize)
                                        count = input.read(data)
                                    }
                                }
                            }
                            val pFD = ParcelFileDescriptor.open(
                                file,
                                ParcelFileDescriptor.MODE_READ_ONLY
                            )
                            val textForEachPage = if (state.isAccessibleEnable) {
                                getTextByPage(context, pFD)
                            } else emptyList()
                            state.pdfRender =
                                BouquetPdfRender(pFD, textForEachPage, width, height, portrait)
                            state.mFile = file
                        }.onFailure {
                            state.mError = it
                        }
                    }
                }

                is ResourceType.Base64 -> {
                    coroutineScope.launch(Dispatchers.IO) {
                        runCatching {
                            val file = context.base64ToPdf(res.file)
                            val pFD = ParcelFileDescriptor.open(
                                file,
                                ParcelFileDescriptor.MODE_READ_ONLY
                            )
                            val textForEachPage = if (state.isAccessibleEnable) {
                                getTextByPage(context, pFD)
                            } else emptyList()
                            state.pdfRender =
                                BouquetPdfRender(pFD, textForEachPage, width, height, portrait)
                            state.mFile = file
                        }.onFailure {
                            state.mError = it
                        }
                    }
                }

                is ResourceType.Asset -> {
                    coroutineScope.launch(Dispatchers.IO) {
                        runCatching {
                            val bufferSize = 8192
                            val inputStream = context.resources.openRawResource(res.assetId)
                            val outFile = File(context.cacheDir, generateFileName())
                            inputStream.use { input ->
                                outFile.outputStream().use { output ->
                                    var data = ByteArray(bufferSize)
                                    var count = input.read(data)
                                    while (count != -1) {
                                        output.write(data, 0, count)
                                        data = ByteArray(bufferSize)
                                        count = input.read(data)
                                    }
                                }
                            }
                            val pFD = ParcelFileDescriptor.open(
                                outFile,
                                ParcelFileDescriptor.MODE_READ_ONLY
                            )
                            val textForEachPage = if (state.isAccessibleEnable) {
                                getTextByPage(context, pFD)
                            } else emptyList()
                            state.pdfRender =
                                BouquetPdfRender(pFD, textForEachPage, width, height, portrait)
                            state.mFile = outFile
                        }.onFailure {
                            state.mError = it
                        }
                    }
                }
            }
        }
    }.onFailure {
        state.mError = it
    }
}

fun Modifier.tapToZoomVertical(
    state: VerticalPdfReaderState,
    isDrawingMode: Boolean,
    boxWidth: Float,
    boxHeight: Float
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "verticalTapToZoom"
        properties["state"] = state
    }
) {
    val coroutineScope = rememberCoroutineScope()

    this
        .pointerInput(isDrawingMode, boxWidth, boxHeight) {
            if (isDrawingMode) return@pointerInput
            detectTapGestures(
                onDoubleTap = { tapCenter ->
                    if (!state.isZoomEnable) return@detectTapGestures
                    if (state.scale > 1.0f) {
                        state.mScale = 1.0f
                        state.offset = Offset(0f, 0f)
                    } else {
                        state.mScale = 3.0f
                        val maxOffsetX = (boxWidth * 3.0f - boxWidth) / 2f
                        val maxOffsetY = (boxHeight * 3.0f - boxHeight) / 2f

                        val targetX = (boxWidth / 2f - tapCenter.x) * 2.0f
                        val targetY = (boxHeight / 2f - tapCenter.y) * 2.0f

                        state.offset = Offset(
                            x = targetX.coerceIn(-maxOffsetX, maxOffsetX),
                            y = targetY.coerceIn(-maxOffsetY, maxOffsetY)
                        )
                    }
                }
            )
        }
        .pointerInput(isDrawingMode, boxWidth, boxHeight) {
            if (isDrawingMode) return@pointerInput
            detectTransformGestures(panZoomLock = true) { centroid, pan, zoom, _ ->
                if (!state.isZoomEnable) return@detectTransformGestures

                val oldScale = state.scale
                val newScale = (oldScale * zoom).coerceIn(1.0f, 5.0f)

                val isPinching = zoom != 1.0f

                val verticalPan = if (pan.y > 0) {
                    if (state.lazyState.canScrollBackward && !isPinching) 0f else pan.y
                } else {
                    if (state.lazyState.canScrollForward && !isPinching) 0f else pan.y
                }

                val listScroll = pan.y - verticalPan

                val centerX = boxWidth / 2f
                val centerY = boxHeight / 2f

                val zoomXOffset = (centroid.x - centerX) * (1f - newScale / oldScale)
                val zoomYOffset = (centroid.y - centerY) * (1f - newScale / oldScale)

                val targetX = (state.offset.x * (newScale / oldScale)) + pan.x + zoomXOffset
                val targetY = (state.offset.y * (newScale / oldScale)) + verticalPan + zoomYOffset

                val maxOffsetX = maxOf(0f, (boxWidth * newScale - boxWidth) / 2f)
                val maxOffsetY = maxOf(0f, (boxHeight * newScale - boxHeight) / 2f)

                state.mScale = newScale
                state.offset = Offset(
                    x = targetX.coerceIn(-maxOffsetX, maxOffsetX),
                    y = targetY.coerceIn(-maxOffsetY, maxOffsetY)
                )

                if (!isPinching && listScroll != 0f) {
                    coroutineScope.launch {
                        state.lazyState.scrollBy(-listScroll / state.scale)
                    }
                }
            }
        }
        .graphicsLayer {
            scaleX = state.scale
            scaleY = state.scale
            translationX = state.offset.x
            translationY = state.offset.y
            this.transformOrigin = TransformOrigin.Center
            clip = true
        }
}

fun Modifier.tapToZoomHorizontal(
    state: HorizontalPdfReaderState,
    constraints: Constraints
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "horizontalTapToZoom"
        properties["state"] = state
    }
) {
    this
        .pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = { tapCenter ->
                    if (!state.isZoomEnable) return@detectTapGestures
                    if (state.mScale > 1.0f) {
                        state.mScale = 1.0f
                        state.offset = Offset(0f, 0f)
                    } else {
                        state.mScale = 3.0f
                        val center = Pair(constraints.maxWidth / 2, constraints.maxHeight / 2)
                        val xDiff = (tapCenter.x - center.first) * state.scale
                        val yDiff = ((tapCenter.y - center.second) * state.scale).coerceIn(
                            minimumValue = -(center.second * 2f),
                            maximumValue = (center.second * 2f)
                        )
                        state.offset = Offset(-xDiff, -yDiff)
                    }
                }
            )
        }
        .pointerInput(Unit) {
            detectTransformGestures(true) { centroid, pan, zoom, rotation ->
                val nOffset = if (state.scale > 1f) {
                    val maxT = (constraints.maxWidth * state.scale) - constraints.maxWidth
                    val maxY = (constraints.maxHeight * state.scale) - constraints.maxHeight
                    Offset(
                        x = (state.offset.x + pan.x).coerceIn(
                            minimumValue = (-maxT / 2) * 1.3f,
                            maximumValue = (maxT / 2) * 1.3f
                        ),
                        y = (state.offset.y + pan.y).coerceIn(
                            minimumValue = (-maxY / 2) * 1.3f,
                            maximumValue = (maxY / 2) * 1.3f
                        )
                    )
                } else {
                    Offset(0f, 0f)
                }
                state.offset = nOffset
            }
        }
        .graphicsLayer {
            scaleX = state.scale
            scaleY = state.scale
            translationX = state.offset.x
            translationY = state.offset.y
        }
}

@Composable
fun BlackPage(
    width: Int,
    height: Int
) {
    Box(
        modifier = Modifier
            .size(
                width = width.dp(),
                height = height.dp()
            )
            .background(color = Color.White)
    )
}
