package com.webview.jsbridge

import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.webview.jsbridge.databinding.PaymentActivityBinding
import com.checkout.bridge.CheckoutJsBridge
import com.checkout.bridge.JsMetaData

class PaymentActivity : AppCompatActivity() {


    private val url: String by lazy { "https://www.cashfree.com/devstudio/preview/pg/web/checkout#renderCheckout" }
    private var paymentJsBridge: CheckoutJsBridge? = null

    private lateinit var binding: PaymentActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = PaymentActivityBinding.inflate(this.layoutInflater)
        setContentView(binding.root)
        initAndLoadWebView()
        initJsBridges()
        loadUrl()
    }


    private fun initAndLoadWebView() {
        binding.paymentWebview.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    request?.let {
                        request.url?.host?.let { host ->
                            if (host.isNotEmpty() && host.contains("sandbox.cashfree.com")) {
                                paymentJsBridge?.setJsBridge(
                                    binding.paymentWebview,
                                    JsMetaData(true, true)
                                )
                            }
                        }
                    }
                    return super.shouldOverrideUrlLoading(view, request)
                }
            }

        }
    }

    private fun initJsBridges() {
        paymentJsBridge = CheckoutJsBridge(this)
    }


    private fun loadUrl() {
        binding.paymentWebview.loadUrl(url)
    }


    override fun onDestroy() {
        super.onDestroy()
        paymentJsBridge?.resetData()
        paymentJsBridge = null
    }

}