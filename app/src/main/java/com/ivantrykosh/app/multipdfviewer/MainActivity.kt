package com.ivantrykosh.app.multipdfviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.ivantrykosh.app.multipdfviewer.ui.route.PdfViewRoute
import com.ivantrykosh.app.multipdfviewer.ui.theme.MultiPdfViewerTheme
import com.ivantrykosh.app.multipdfviewer.ui.viewmodel.PdfViewerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MultiPdfViewerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PdfViewRoute(
                        pdfViewerViewModel = viewModels<PdfViewerViewModel>().value
                    )
                }
            }
        }
    }
}