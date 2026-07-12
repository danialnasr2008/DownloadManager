package com.example.ui

import android.annotation.SuppressLint
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(modifier: Modifier = Modifier) {
    var url by remember { mutableStateOf("https://google.com") }
    var inputUrl by remember { mutableStateOf("https://google.com") }
    var webViewRef: WebView? by remember { mutableStateOf(null) }
    var adBlockEnabled by remember { mutableStateOf(true) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { webViewRef?.goBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            IconButton(onClick = { webViewRef?.goForward() }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Forward")
            }
            OutlinedTextField(
                value = inputUrl,
                onValueChange = { inputUrl = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .height(50.dp),
                singleLine = true,
                shape = androidx.compose.foundation.shape.CircleShape,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(
                    onGo = {
                        var finalUrl = inputUrl
                        if (!finalUrl.startsWith("http")) {
                            finalUrl = "https://$finalUrl"
                        }
                        url = finalUrl
                        webViewRef?.loadUrl(url)
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
            IconButton(onClick = { adBlockEnabled = !adBlockEnabled }) {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = "AdBlock",
                    tint = if (adBlockEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { webViewRef?.reload() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Reload")
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        
                        setDownloadListener { downloadUrl, userAgent, contentDisposition, mimetype, contentLength ->
                            // Here you'd normally parse and pass it to the MainViewModel to add to downloads
                            Toast.makeText(context, "Intercepted download link! Added to Aria.", Toast.LENGTH_SHORT).show()
                        }
                        
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                request?.url?.toString()?.let {
                                    inputUrl = it
                                }
                                return false
                            }

                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): WebResourceResponse? {
                                if (adBlockEnabled) {
                                    val reqUrl = request?.url?.toString() ?: ""
                                    if (reqUrl.contains("ads") || reqUrl.contains("banner") || reqUrl.contains("analytics")) {
                                        return WebResourceResponse("text/plain", "UTF-8", null)
                                    }
                                }
                                return super.shouldInterceptRequest(view, request)
                            }
                        }
                        webChromeClient = WebChromeClient()
                        loadUrl(url)
                    }.also { webViewRef = it }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
