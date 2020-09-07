package dev.herovitamin.hms.multiplekits.hquic

import android.content.Context
import android.util.Log
import com.huawei.hms.hquic.HQUICManager
import com.huawei.hms.hquic.HQUICManager.HQUICInitCallback
import org.chromium.net.CronetEngine
import org.chromium.net.UrlRequest
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class HQuicService(var context : Context){

    private val TAG = "HQuicService"
    private val DEFAULT_PORT = 443
    private val DEFAULT_ALTERNATEPORT = 443
    private val executor: Executor = Executors.newSingleThreadExecutor()

    private var cronetEngine: CronetEngine? = null
    private var callback: UrlRequest.Callback? = null

    init {
        HQUICManager.asyncInit(
            context,
            object : HQUICInitCallback {
                override fun onSuccess() {
                    Log.i(TAG, "HQUICManager asyncInit success")
                }

                override fun onFail(e: Exception) {
                    Log.w(TAG, "HQUICManager asyncInit fail")
                }
            })
    }

    private fun createCronetEngine(url: String): CronetEngine? {
        cronetEngine?.let { return it }

        val builder = CronetEngine.Builder(context)
        builder.enableQuic(true)
        builder.addQuicHint(getHost(url), DEFAULT_PORT, DEFAULT_ALTERNATEPORT)
        cronetEngine = builder.build()
        return cronetEngine
    }

    private fun buildRequest(url: String, method: String): UrlRequest? {
        val cronetEngine = createCronetEngine(url)
        val requestBuilder =
            cronetEngine!!.newUrlRequestBuilder(url, callback, executor).setHttpMethod(method)
        return requestBuilder.build()
    }

    fun sendRequest(url: String, method: String) {
        Log.i(TAG, "callURL: url is " + url + "and method is " + method)
        val urlRequest = buildRequest(url, method)
        urlRequest?.start()
    }

    private fun getHost(url: String): String? {
        var host: String? = null
        try {
            val url1 = URL(url)
            host = url1.host
        } catch (e: MalformedURLException) {
            Log.e(TAG, "getHost: ", e)
        }
        return host
    }

    fun setCallback(mCallback: UrlRequest.Callback) {
        callback = mCallback
    }
}