package dev.herovitamin.hms.multiplekits

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import dev.herovitamin.hms.multiplekits.hquic.HQuicService
import kotlinx.android.synthetic.main.activity_h_quic_kit.*
import org.chromium.net.CronetException
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import java.nio.ByteBuffer

class HQuicKitActivity : AppCompatActivity() {

    private val TAG = HQuicKitActivity::class.simpleName
    private val CAPACITY = 102400
    private var callStr: String? = null
    private val startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_h_quic_kit)
        init()
    }

    fun init(){
        createHQuic()
    }

    private fun createHQuic() {
        val hquicService = HQuicService(this@HQuicKitActivity)
        hquicService.setCallback(object : UrlRequest.Callback(){
            override fun onResponseStarted(request: UrlRequest?, info: UrlResponseInfo?) {
                Log.i(TAG, "onResponseStarted: method is called");
                request?.read(ByteBuffer.allocateDirect(CAPACITY));
            }

            override fun onReadCompleted(
                request: UrlRequest?,
                info: UrlResponseInfo?,
                byteBuffer: ByteBuffer?
            ) {
                Log.i(TAG, "onReadCompleted: method is called");
                request?.read(ByteBuffer.allocateDirect(CAPACITY));
            }

            override fun onFailed(
                request: UrlRequest?,
                info: UrlResponseInfo?,
                error: CronetException?
            ) {
                Log.e(TAG, "onFailed: method is called ", error)
                callStr += "onFailed: method is called $error"
                runOnUiThread {
                    hquic_tv.text = callStr
                }
            }

            override fun onSucceeded(request: UrlRequest?, info: UrlResponseInfo?) {
                Log.i(TAG, "onSucceeded: method is called")
                Log.i(
                    TAG,
                    "onSucceeded: protocol is " + info?.getNegotiatedProtocol()
                )
                val endTime = System.currentTimeMillis()
                val duration: Long = endTime - startTime
                callStr += "duration -> " + duration + "ms" + System.lineSeparator()
                callStr += "protocol -> " + info?.getNegotiatedProtocol()
                    .toString() + System.lineSeparator()
                val list: MutableList<Map.Entry<String, String>> = info?.getAllHeadersAsList()!!.toMutableList()
                for ((key, value) in list) {
                    callStr += "$key -> "
                    callStr += value + System.lineSeparator()
                }
                runOnUiThread { hquic_tv.text = callStr }
            }

            override fun onRedirectReceived(
                request: UrlRequest?,
                info: UrlResponseInfo?,
                newLocationUrl: String?
            ) {
                Log.i(TAG, "onRedirectReceived: method is called");
                request?.followRedirect();
            }

        })
    }
}