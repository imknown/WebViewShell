package net.imknown.android.webviewshell

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    companion object {
        private const val URL_HOME = "https://www.baidu.com"
    }

    private val etUrl: EditText by lazy { findViewById(R.id.etUrl) }
    private val btnGo: Button by lazy { findViewById(R.id.btnGo) }
    private val webView: WebView by lazy { findViewById(R.id.webView) }
    private val progressBar: ProgressBar by lazy { findViewById(R.id.progressBar) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initWebSettings()

        etUrl.setText(URL_HOME)

        btnGo.setOnClickListener {
            hideKeyboard()

            goUrl(etUrl.text.toString())
        }

        goUrl(URL_HOME)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebSettings() {
        webView.settings.javaScriptEnabled = true
        webView.settings.allowContentAccess = true
        webView.settings.allowFileAccess = true
        webView.settings.databaseEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)

                if (newProgress >= 100) {
                    progressBar.visibility = ProgressBar.GONE
                } else {
                    progressBar.visibility = ProgressBar.VISIBLE
                }
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                etUrl.setText(url)
            }
        }
    }

    private fun goUrl(url: String) {
        webView.loadUrl(url)
    }

    private fun hideKeyboard() =
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(etUrl.windowToken, 0)

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        webView.removeAllViewsInLayout()
        webView.destroy()
        webView.removeAllViews()
    }

}