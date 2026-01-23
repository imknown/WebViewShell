package net.imknown.android.webviewshell

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.webkit.ClientCertRequest
import android.webkit.ConsoleMessage
import android.webkit.HttpAuthHandler
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.google.android.material.color.DynamicColors
import net.imknown.android.webviewshell.databinding.ActivityMainBinding
import androidx.appcompat.R as AppCompatR

class MainActivity : AppCompatActivity() {

    companion object {
        private const val URL_HOME = "https://www.google.com/ncr"
    }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val etUrl by lazy { binding.etUrl }
    private val btnGo by lazy { binding.btnGo }
    private val progressBar by lazy { binding.progressBar }

    private val btnLog by lazy { binding.btnLog }
    private val btnClear by lazy { binding.btnClear }
    private val spinnerProxy by lazy { binding.spinnerProxy }

    private val webView by lazy { binding.webView }

    private val svLog by lazy { binding.svLog }
    private val tvLog by lazy { binding.tvLog }

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivitiesIfAvailable(application)

        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { root, windowInsetsCompat ->
            val insets = windowInsetsCompat.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            root.updatePadding(
                left = insets.left,
                right = insets.right,
                top = insets.top,
            )
            binding.tvLog.updatePadding(
                left = insets.left,
                right = insets.right,
                bottom = insets.bottom,
            )
            windowInsetsCompat
        }

        initWebSettings()

        val url = intent.dataString ?: URL_HOME

        etUrl.setText(url)

        goUrl(url)

        btnGo.setOnClickListener {
            hideKeyboard()

            log("===")

            goUrl(etUrl.text.toString())
        }

        btnLog.setOnClickListener {
            svLog.isVisible = svLog.isVisible.not()
        }

        btnClear.setOnClickListener {
            tvLog.text = null
        }

        spinnerProxy.adapter = ArrayAdapter(
            this,
            AppCompatR.layout.support_simple_spinner_dropdown_item,
            proxies.map(Proxy::name)
        )
        spinnerProxy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                setProxy(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        onBackPressedDispatcher.addCallback(this) {
            val canGoBack = webView.canGoBack()
            if (canGoBack) {
                webView.goBack()
            } else {
                finish()
            }
        }
    }

    private fun log(msg: String?) {
        @Suppress("SetTextI18n")
        tvLog.text = msg + "\n\n" + tvLog.text
    }

    private fun logDetail(
        isForMainFrame: Boolean?, url: String?, msg: String?
    ) {
        val msgDetail = """
            |url: $url
            |isForMainFrame: $isForMainFrame
            |msg: $msg
            """.trimMargin()
        log(msgDetail)
    }

    private fun initWebSettings() {
        with(WebViewCompat.getCurrentWebViewPackage(this)) {
            this ?: return@with
            val label = applicationInfo?.loadLabel(packageManager)
            val result = "$packageName\n$label\n$versionName"
            log("Current WebView implementation:\n$result")
        }

        WebView.setWebContentsDebuggingEnabled(true)

        @SuppressLint("SetJavaScriptEnabled")
        webView.settings.javaScriptEnabled = true

        webView.settings.allowContentAccess = true
        webView.settings.allowFileAccess = true
        // webView.settings.databaseEnabled = true
        webView.settings.domStorageEnabled = true

        webView.settings.javaScriptCanOpenWindowsAutomatically = true

        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true

        webView.setDownloadListener { url: String, userAgent: String, contentDisposition: String, mimetype: String, contentLength: Long ->
            log("onDownloadStart: $url, userAgent: $userAgent, contentDisposition: $contentDisposition, mimetype: $mimetype, contentLength: $contentLength")
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.isVisible = newProgress < 100
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                super.onShowCustomView(view, callback)
                log("onShowCustomView")
            }

            override fun onHideCustomView() {
                super.onHideCustomView()
                log("onHideCustomView")
            }

            override fun onJsBeforeUnload(
                view: WebView?, url: String?, message: String?, result: JsResult?
            ): Boolean {
                log("onJsBeforeUnload: url = $url, message = $message")
                return super.onJsBeforeUnload(view, url, message, result)
            }

            override fun onJsAlert(
                view: WebView?, url: String?, message: String?, result: JsResult?
            ): Boolean {
                log("onJsAlert: url = $url, message = $message")
                return super.onJsAlert(view, url, message, result)
            }

            override fun onJsConfirm(
                view: WebView?, url: String?, message: String?, result: JsResult?
            ): Boolean {
                log("onJsConfirm: url = $url, message = $message")
                return super.onJsConfirm(view, url, message, result)
            }

            override fun onJsPrompt(
                view: WebView?, url: String?, message: String?,
                defaultValue: String?, result: JsPromptResult?
            ): Boolean {
                log("onJsPrompt: url = $url, message = $message, defaultValue = $defaultValue")
                return super.onJsPrompt(view, url, message, defaultValue, result)
            }

            override fun onCreateWindow(
                view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?
            ): Boolean {
                log("onCreateWindow: isDialog = $isDialog, resultMsg = $resultMsg")
                return false
            }

            override fun onCloseWindow(window: WebView?) {
                log("onCloseWindow")
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                log("onReceivedTitle: $title")
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                val messageLevel = consoleMessage?.messageLevel()
                val sourceId = consoleMessage?.sourceId()
                val lineNumber = consoleMessage?.lineNumber()
                val message = consoleMessage?.message()
                val msg = """
                    |Level = $messageLevel, sourceId = $sourceId, lineNumber = $lineNumber
                    |Msg = $message
                    """.trimMargin()
                log("onConsoleMessage: $msg")
                return false
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                // log("onPageStarted: $url")

                etUrl.setText(url)
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                // log("onPageCommitVisible: $url")
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                // log("onLoadResource: $url")
            }

            override fun onReceivedClientCertRequest(view: WebView?, request: ClientCertRequest?) {
                super.onReceivedClientCertRequest(view, request)
                log("onReceivedClientCertRequest: host = ${request?.host}")
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                logDetail(request?.isForMainFrame, request?.url?.path, "shouldOverrideUrlLoading")
                return false
            }

            override fun shouldInterceptRequest(
                view: WebView?, request: WebResourceRequest?
            ): WebResourceResponse? {
                // lifecycleScope.launch {
                //     logDetail(request?.isForMainFrame, request?.url?.path, "shouldInterceptRequest")
                // }
                return null
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                // log("onPageFinished: $url")
            }

            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest?, error: WebResourceError?
            ) {
                logDetail(
                    request?.isForMainFrame,
                    request?.url.toString(),
                    "Error:\n${error?.description} (${error?.errorCode})"
                )
            }

            override fun onReceivedHttpError(
                view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?
            ) {
                logDetail(
                    request?.isForMainFrame,
                    request?.url.toString(),
                    "Http Error:\n${errorResponse?.reasonPhrase} (${errorResponse?.statusCode})"
                )
            }

            override fun onReceivedSslError(
                view: WebView?, handler: SslErrorHandler?, error: SslError?
            ) {
                logDetail(
                    null,
                    error?.url.toString(),
                    "SSL Error:\n$error"
                )

                @Suppress("WebViewClientOnReceivedSslError")
                handler?.proceed()
            }

            override fun onReceivedHttpAuthRequest(
                view: WebView?, handler: HttpAuthHandler?, host: String?, realm: String?
            ) {
                handler?.proceed(null, null)
            }
        }
    }

    private fun setProxy(position: Int) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            val proxy = proxies[position]

            val proxyController = ProxyController.getInstance()
            val proxyName = proxy.name
            val executor = { r: Runnable ->
                r.run()
                log("Change proxy $proxyName setting: executed.")
            }
            val listener = { log("Change proxy $proxyName setting: applied.") }

            if (position == 0) {
                proxyController.clearProxyOverride(executor, listener)
            } else {
                val proxyConfig = ProxyConfig.Builder().apply {
                    bypasses.forEach(::addBypassRule)
                }.addProxyRule(proxy.url).build()
                proxyController.setProxyOverride(proxyConfig, executor, listener)
            }
        } else {
            log("Proxy feature not supported.")
        }
    }

    private fun goUrl(url: String) {
        webView.loadUrl(url)
    }

    private fun hideKeyboard() {
        WindowCompat.getInsetsController(window, binding.root).hide(WindowInsetsCompat.Type.ime())
        // getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(etUrl.windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()

        (webView.parent as ViewGroup).removeView(webView)
        webView.destroy()
    }

}