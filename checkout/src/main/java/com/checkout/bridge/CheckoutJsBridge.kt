package com.checkout.bridge

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import org.json.JSONArray
import org.json.JSONObject
import java.util.regex.Pattern


class CheckoutJsBridge(private val _context: AppCompatActivity) {

    init {
        registerForResult()
    }

    private var _mode: Modes = Modes.UPI
    private var _webView: WebView? = null
    private var _smsReceiverIsRegistered: Boolean = false
    private var _activityResultLauncher: ActivityResultLauncher<Intent>? = null

    private val _smsVerificationReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                checkAndLaunchOTPConsentDialog(intent)
            }
        }
    }

    /**
     * @param webView : {@link WebView}
     * @param metaData : {@link JsMetaData}
     */
    @JvmOverloads
    fun setJsBridge(webView: WebView, metaData: JsMetaData = JsMetaData()) {
        try {
            this._webView = webView
            _webView?.apply {
                settings.apply {
                    domStorageEnabled = true
                    javaScriptEnabled = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    allowFileAccess = false
                    allowContentAccess = false
                }
            }

            if (metaData.addUpiJS) _webView?.addJavascriptInterface(this, getName(BRIDGE_NAME))
            if (metaData.addOtpJS) _webView?.addJavascriptInterface(this, getName(OTP_BRIDGE_NAME))
        } catch (ex: Exception) {
            Log.e(TAG, ex.message.toString())
        }
    }

    /**
     * Call this method in onDestroy of your view. This will clear references & memory.
     */
    fun resetData() {
        _webView = null
        _activityResultLauncher = null
        deregisterSMSReceiver()
    }


    @JavascriptInterface
    fun getAppList(name: String?): String {
        val packageNames = JSONArray()
        try {
            val intent = getIntent(name)
            val pm: PackageManager = _context.packageManager
            val resInfo: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)
            for (info in resInfo) {
                val appInfo = JSONObject().apply {
                    put(
                        APP_NAME,
                        pm.getApplicationLabel(info.activityInfo.applicationInfo).toString()
                    )
                    put(APP_PACKAGE, info.activityInfo.packageName)
                }
                packageNames.put(appInfo)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return packageNames.toString()
    }

    @JavascriptInterface
    fun openApp(upiClientPackage: String?, upiURL: String?): Boolean {
        try {
            val intent = getIntent(upiURL)
            var foundPackageFlag = false
            val resInfo: List<ResolveInfo> =
                _context.packageManager.queryIntentActivities(intent, 0)
            var upiClientResolveInfo: ResolveInfo? = null
            for (info in resInfo) {
                if (info.activityInfo.packageName.equals(upiClientPackage, true)) {
                    foundPackageFlag = true
                    upiClientResolveInfo = info
                    break
                }
            }

            if (foundPackageFlag) {
                intent.setClassName(
                    upiClientResolveInfo!!.activityInfo.packageName,
                    upiClientResolveInfo.activityInfo.name
                )

                _activityResultLauncher?.launch(intent)
            }
        } catch (exception: Exception) {
            Log.e(TAG, "${exception.message}")
        }
        return true
    }

    @JavascriptInterface
    fun readOTP() {
        try {
            registerAndStartReceiver()
        } catch (e: Exception) {
            Log.e(TAG, "Error readOTP ::${e.message}")
        }
    }

    @JavascriptInterface
    fun deregisterSMSReceiver() {
        if (_smsReceiverIsRegistered) {
            try {
                _context.unregisterReceiver(_smsVerificationReceiver)
            } catch (e: Exception) {
                Log.e(TAG, "Error deregisterSMSReceiver ::${e.message}")
            }
            _smsReceiverIsRegistered = false
        }
    }

    private fun getIntent(upiURL: String?): Intent {
        return Intent().apply {
            setAction(Intent.ACTION_VIEW)
            setData(Uri.parse(upiURL))
        }
    }

    private fun registerForResult() {
        _mode = Modes.UPI
        try {
            _activityResultLauncher = _context.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK && _webView != null) {
                    when (_mode) {
                        Modes.UPI -> _webView?.evaluateJavascript(getName(UPI_VERIFY_BRIDGE)) {}
                        Modes.OTP -> {
                            val message: String? =
                                result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                            getOTP(message)?.let { otp ->
                                _webView?.evaluateJavascript(
                                    String.format(
                                        getName(OTP_VERIFY_BRIDGE),
                                        otp
                                    )
                                ) {}
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, ex.message.toString())
        }
    }

    private fun registerAndStartReceiver() {
        registerSMSReceiver()
        startSmsRetriever()
    }

    private fun registerSMSReceiver() {
        if (!_smsReceiverIsRegistered) {
            val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
            try {
                ContextCompat.registerReceiver(
                    _context,
                    _smsVerificationReceiver,
                    intentFilter,
                    ContextCompat.RECEIVER_EXPORTED
                )
                _smsReceiverIsRegistered = true
            } catch (e: Exception) {
                Log.e(TAG, "Error registering SMS Broadcast::${e.message}")
            }
        }
    }

    private fun startSmsRetriever() {
        val task = SmsRetriever.getClient(_context).startSmsUserConsent(null)
        task.addOnFailureListener { e: Exception ->
            Log.e(TAG, "${e.message}")
        }
    }

    private fun getOTP(message: String?): String? {
        val otp: String? = null
        if (message == null) return otp
        val p = Pattern.compile(getName(OTP_REGEX))
        val m = p.matcher(message)
        try {
            if (m.find()) {
                for (i in 1..m.groupCount()) {
                    if (m.group(i) != null) {
                        return m.group(i)
                    }
                }
            }
            return otp
        } catch (exception: Exception) {
            return otp
        }
    }

    private fun checkAndLaunchOTPConsentDialog(intent: Intent?) {
        val extras = intent?.extras
        if (extras != null && SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val smsRetrieverStatus: Status? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    extras.getParcelable(SmsRetriever.EXTRA_STATUS, Status::class.java)
                } else {
                    extras.getParcelable(SmsRetriever.EXTRA_STATUS)
                }

            if (smsRetrieverStatus != null && CommonStatusCodes.SUCCESS == smsRetrieverStatus.statusCode) {
                val consentIntent: Intent? =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        extras.getParcelable(
                            SmsRetriever.EXTRA_CONSENT_INTENT,
                            Intent::class.java
                        )
                    } else {
                        extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT)
                    }

                try {
                    _activityResultLauncher?.launch(consentIntent)
                    _mode = Modes.OTP
                } catch (e: ActivityNotFoundException) {
                    Log.e(TAG, e.message.toString())
                }
            }
        }
    }

    private fun getName(data: String) =
        String(Base64.decode(data.toByteArray(), Base64.DEFAULT), charset("UTF-8"))

    internal companion object {
        private const val BRIDGE_NAME = "QW5kcm9pZA=="
        private const val OTP_BRIDGE_NAME = "QW5kcm9pZE9UUEludGVyZmFjZQ=="
        private const val TAG = "CheckoutJsBridge"
        private const val APP_NAME = "appName"
        private const val APP_PACKAGE = "appPackage"
        private const val UPI_VERIFY_BRIDGE = "d2luZG93LnNob3dWZXJpZnlVSSgp"
        private const val OTP_VERIFY_BRIDGE = "d2luZG93LnNldE9UUCgnJXMnKQ=="
        private const val OTP_REGEX = "aXNccyhcZHs2LDh9KXwoXGR7Niw4fSlcc2lzfGlzXHMoXGR7NH0p"
    }
}