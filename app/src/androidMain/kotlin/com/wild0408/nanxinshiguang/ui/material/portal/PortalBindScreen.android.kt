@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.wild0408.nanxinshiguang.ui.portal

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.wild0408.nanxinshiguang.data.model.PortalPasskeyBundle
import com.wild0408.nanxinshiguang.data.portal.PortalProfileResult
import com.wild0408.nanxinshiguang.data.portal.PortalSession
import com.wild0408.nanxinshiguang.data.repository.PortalCredentialRepository
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.arrow_back_24px
import nanxinshiguang.generated.resources.portal_bind_title
import nanxinshiguang.generated.resources.portal_retry
import nanxinshiguang.generated.resources.portal_status_registering
import nanxinshiguang.generated.resources.portal_status_verifying
import nanxinshiguang.generated.resources.portal_status_waiting_page
import nanxinshiguang.generated.resources.portal_status_navigating
import nanxinshiguang.generated.resources.portal_switch_account

private const val PORTAL_HOST = "authserver.nuist.edu.cn"
private const val PORTAL_URL = "https://$PORTAL_HOST/authserver/login"
private const val DESKTOP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/128.0.0.0 Safari/537.36"
private const val PORTAL_DESKTOP_VIEWPORT_FIX_SCRIPT = """
(function() {
  try {
    var metas = document.getElementsByTagName('meta');
    for (var i = metas.length - 1; i >= 0; i--) {
      if (metas[i].getAttribute('name') === 'viewport') {
        metas[i].parentNode.removeChild(metas[i]);
      }
    }
    var meta = document.createElement('meta');
    meta.name = 'viewport';
    meta.content = 'width=1280, initial-scale=0.28, minimum-scale=0.1, maximum-scale=5.0, user-scalable=yes';
    document.head.appendChild(meta);
    window.dispatchEvent(new Event('resize'));
  } catch (e) {
    console.error('portal viewport fix failed', e);
  }
})();
"""

private const val PORTAL_MODAL_LAYOUT_FIX_SCRIPT = """
(function() {
  try {
    var styleId = '__shiguang_portal_modal_layout_fix__';
    var style = document.getElementById(styleId);
    if (!style) {
      style = document.createElement('style');
      style.id = styleId;
      style.textContent =
        '.ivu-modal-wrap:not(.ivu-modal-hidden) {' +
        'top:16px !important;' +
        'bottom:auto !important;' +
        'left:50% !important;' +
        'height:auto !important;' +
        'max-height:none !important;' +
        'transform:translateX(-50%) !important;' +
        'overflow:auto !important;' +
        '}';
      (document.head || document.documentElement).appendChild(style);
    }
  } catch (e) {
    console.error('portal modal layout fix failed', e);
  }
})();
"""

@Composable
@OptIn(ExperimentalMaterial3Api::class)
actual fun PortalBindScreen(
    onBack: () -> Unit,
    onCompleted: () -> Unit,
) {
    val repository: PortalCredentialRepository = koinInject()
    val portalSession: PortalSession = koinInject()
    AndroidPortalBindScreen(repository, portalSession, onBack, onCompleted)
}

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
private fun AndroidPortalBindScreen(
    repository: PortalCredentialRepository,
    portalSession: PortalSession,
    onBack: () -> Unit,
    onCompleted: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var progress by remember { mutableFloatStateOf(0f) }
    var status by remember { mutableStateOf(PortalStatus.WaitingPage) }
    var error by remember { mutableStateOf<String?>(null) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    fun loadPortal() {
        error = null
        status = PortalStatus.Navigating
        webView?.loadUrl(PORTAL_URL)
    }

    BackHandler { if (webView?.canGoBack() == true) webView?.goBack() else onBack() }

    Scaffold(
        topBar = {
            if (status != PortalStatus.Verifying && status != PortalStatus.Registering) TopAppBar(
                title = { Text(stringResource(Res.string.portal_bind_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            vectorResource(Res.drawable.arrow_back_24px),
                            contentDescription = stringResource(Res.string.a11y_back),
                        )
                    }
                },
                actions = {
                    when {
                        error != null -> {
                            TextButton(onClick = ::loadPortal) {
                                Text(stringResource(Res.string.portal_retry))
                            }
                        }
                        status != PortalStatus.Success -> {
                            TextButton(onClick = {
                                android.webkit.CookieManager.getInstance().removeAllCookies(null)
                                loadPortal()
                            }) {
                                Text(stringResource(Res.string.portal_switch_account))
                            }
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
            // Keep the web page unobstructed while its own verification dialog is open.
            // The portal already shows the instructions inside the page; the extra
            // status panel would shrink the WebView and clip the dialog's top edge.
            if (error != null || status == PortalStatus.Navigating || status == PortalStatus.WaitingPage) {
                Text(
                    text = error ?: status.label,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (error == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                )
            }
            AndroidView(
                // Keep the native WebView inside the viewport left by the app chrome.
                // Using fillMaxSize here lets fixed-position portal dialogs extend
                // underneath the Compose top bar on some Android versions.
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .imePadding(),
                factory = {
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.userAgentString = DESKTOP_USER_AGENT
                        // Match the reference project's webview_flutter Android setup.
                        // Without overview mode, the desktop portal can calculate a
                        // dialog against a wider virtual viewport and clip its top.
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.layoutAlgorithm = android.webkit.WebSettings.LayoutAlgorithm.NORMAL
                        settings.javaScriptCanOpenWindowsAutomatically = true
                        settings.setSupportMultipleWindows(true)
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                        android.webkit.CookieManager.getInstance().setAcceptCookie(true)
                        addJavascriptInterface(PortalPasskeyBridge { raw ->
                            scope.launch {
                                handlePortalMessage(raw, repository, portalSession, onCompleted) { nextStatus, nextError ->
                                    status = nextStatus
                                    error = nextError
                                }
                            }
                        }, "NuistPasskey")
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress / 100f
                            }

                        }
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val url = request?.url ?: return false
                                if (url.scheme == "http" && url.host == PORTAL_HOST) {
                                    view?.loadUrl(url.buildUpon().scheme("https").build().toString())
                                    return true
                                }
                                return false
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                status = PortalStatus.WaitingPage
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                view?.evaluateJavascript(PORTAL_DESKTOP_VIEWPORT_FIX_SCRIPT, null)
                                view?.evaluateJavascript(PORTAL_MODAL_LAYOUT_FIX_SCRIPT, null)
                                if (url?.contains("personCenter", ignoreCase = true) == true) {
                                    status = PortalStatus.Navigating
                                    val script = runCatching {
                                        context.assets.open("portal_passkey.js").bufferedReader().use { it.readText() }
                                            .replace("__DEVICE_NAME__", "\"拾光(Android)\"")
                                    }.getOrNull()
                                    if (script == null) {
                                        error = "绑定脚本加载失败"
                                    } else {
                                        view?.evaluateJavascript(script, null)
                                    }
                                }
                            }

                            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, webError: android.webkit.WebResourceError?) {
                                if (request?.isForMainFrame == true) {
                                    error = webError?.description?.toString() ?: "门户页面加载失败"
                                }
                            }

                            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: android.net.http.SslError?) {
                                handler?.cancel()
                            }
                        }
                        webView = this
                        loadUrl(PORTAL_URL)
                    }
                },
                update = { webView = it },
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.apply {
                removeJavascriptInterface("NuistPasskey")
                stopLoading()
                webViewClient = WebViewClient()
                destroy()
            }
            webView = null
        }
    }
}

private class PortalPasskeyBridge(private val onMessage: (String) -> Unit) {
    @JavascriptInterface
    fun postMessage(message: String) = onMessage(message)
}

private enum class PortalStatus(val label: String) {
    Navigating("正在跳转到通行密钥页…"),
    WaitingPage("等待页面加载…"),
    Verifying("请在页面中完成身份验证"),
    Registering("正在注册通行密钥…"),
    Success("绑定成功"),
}

private suspend fun handlePortalMessage(
    raw: String,
    repository: PortalCredentialRepository,
    portalSession: PortalSession,
    onCompleted: () -> Unit,
    update: (PortalStatus, String?) -> Unit,
) {
    val root = runCatching { Json.parseToJsonElement(raw).jsonObject }.getOrNull()
        ?: return update(PortalStatus.WaitingPage, "绑定页面消息格式异常，请重试")
    when (root["type"]?.jsonPrimitive?.content) {
        "status" -> when (root["stage"]?.jsonPrimitive?.content) {
            "navigating" -> update(PortalStatus.Navigating, null)
            "waiting_page" -> update(PortalStatus.WaitingPage, null)
            "verifying" -> update(PortalStatus.Verifying, null)
            "registering" -> update(PortalStatus.Registering, null)
        }
        "error" -> update(PortalStatus.WaitingPage, root["message"]?.jsonPrimitive?.content ?: "绑定失败")
        "success" -> {
            val bundleJson = root["bundle"]?.jsonObject ?: return update(PortalStatus.WaitingPage, "注册结果格式异常")
            val bundle = PortalPasskeyBundle(
                rpId = bundleJson["rpId"]?.jsonPrimitive?.content.orEmpty(),
                credentialId = bundleJson["credentialId"]?.jsonPrimitive?.content.orEmpty(),
                privateKeyPkcs8Pem = bundleJson["privateKeyPkcs8Pem"]?.jsonPrimitive?.content.orEmpty(),
                userId = bundleJson["userId"]?.jsonPrimitive?.content.orEmpty(),
                anonBiometricsId = bundleJson["anonbiometricsd"]?.jsonPrimitive?.content.orEmpty(),
                deviceName = bundleJson["deviceName"]?.jsonPrimitive?.content ?: "拾光(Android)",
                createdAt = bundleJson["createdAt"]?.jsonPrimitive?.content?.toLongOrNull()
                    ?: kotlin.time.Clock.System.now().toEpochMilliseconds(),
            )
            if (bundle.rpId.isBlank() || bundle.credentialId.isBlank() || bundle.privateKeyPkcs8Pem.isBlank() || bundle.userId.isBlank()) {
                update(PortalStatus.WaitingPage, "注册结果缺少必要凭据")
            } else {
                repository.save(bundle)
                val profileMessage = when (val result = portalSession.fetchUserProfile(force = true)) {
                    is PortalProfileResult.Success -> {
                        repository.saveProfile(result.profile)
                        null
                    }
                    is PortalProfileResult.NotLoggedIn -> "绑定成功，但暂时无法读取门户身份信息"
                    is PortalProfileResult.NetworkError -> "绑定成功，但网络暂时不可用，身份信息未保存"
                    is PortalProfileResult.Error -> "绑定成功，但身份信息读取失败"
                }
                update(PortalStatus.Success, profileMessage)
                onCompleted()
            }
        }
    }
}
