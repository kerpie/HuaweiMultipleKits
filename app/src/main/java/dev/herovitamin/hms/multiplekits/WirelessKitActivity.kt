package dev.herovitamin.hms.multiplekits

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hms.common.ApiException
import com.huawei.hms.wireless.*
import kotlinx.android.synthetic.main.activity_wireless_kit.*


class WirelessKitActivity : AppCompatActivity() {

    private val TAG : String? = WirelessKitActivity::class.simpleName
    private val NETWORK_QOE_INFO_TYPE = 0

    private val channelIndex = IntArray(4)
    private val uLRtt = IntArray(4)
    private val dLRtt = IntArray(4)
    private val uLBandwidth = IntArray(4)
    private val dLBandwidth = IntArray(4)
    private val uLRate = IntArray(4)
    private val dLRate = IntArray(4)
    private val netQoeLevel = IntArray(4)
    private val uLPkgLossRate = IntArray(4)

    lateinit var networkQoEClient : NetworkQoeClient
    var qoeService : IQoeService? = null
    lateinit var serviceConnection: ServiceConnection
    lateinit var callback : IQoeCallBack


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wireless_kit)

        initValues()

        bindService()

        registerService()
    }

    private fun bindService() {
        showStatus("Binding Service")
        networkQoEClient?.let {
            it.networkQoeServiceIntent
                .addOnSuccessListener(object : OnSuccessListener<WirelessResult> {
                    override fun onSuccess(wirelessResult: WirelessResult?) {
                        var intent: Intent? = wirelessResult?.intent ?: return
                        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
                        showStatus("Binding successful")
                    }

                })
                .addOnFailureListener(object : OnFailureListener {
                    override fun onFailure(e: Exception?) {
                        if (e is ApiException) {
                            var errorCode = e.statusCode
                            showStatus("Binding Error: error code: ${errorCode}")
                        }
                    }
                })
        }
    }

    private fun registerService() {
        showStatus("Registering Service")
        if (qoeService != null) {
            try {
                var ret =
                    qoeService?.registerNetQoeCallBack("dev.herovitamin.hms.multiplekits", callback)
                showStatus("Registering successful")
            } catch (ex: RemoteException) {
                showStatus("Registering failed")
                Log.i(TAG, "remote exception while registering QoE callback")
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    private fun obtainRealTimeData() {
        if (qoeService != null) {
            try {
                var qoeInfo: Bundle =
                    qoeService!!.queryRealTimeQoe("dev.herovitamin.hms.multiplekits");
                if (qoeInfo == null) return
                var channelNum: Int = qoeInfo.getInt("channelNum");
                for (i in 0..channelNum) {
                    uLRtt[i] = qoeInfo.getInt("uLRtt" + i);
                    dLRtt[i] = qoeInfo.getInt("dLRtt" + i);
                    uLBandwidth[i] = qoeInfo.getInt("uLBandwidth" + i);
                    dLBandwidth[i] = qoeInfo.getInt("dLBandwidth" + i);
                    uLRate[i] = qoeInfo.getInt("uLRate" + i);
                    dLRate[i] = qoeInfo.getInt("dLRate" + i);
                    netQoeLevel[i] = qoeInfo.getInt("netQoeLevel" + i);
                    uLPkgLossRate[i] = qoeInfo.getInt("uLPkgLossRate" + i);

                    showValues(
                        channelIndex[i].toString() + "," + uLRtt[i] + "," + dLRtt[i] + "," + uLBandwidth[i] + ","
                                + dLBandwidth[i] + "," + uLRate[i] + "," + dLRate[i] + "," + netQoeLevel[i] + ","
                                + uLPkgLossRate[i]
                    )
                }
            } catch (ex: RemoteException) {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        var ret = 0
        unregisterCallback(ret)
        unbindService()
    }

    private fun unregisterCallback(ret: Int) {
        var ret1 = ret
        qoeService?.let {
            try {
                ret1 = it.unRegisterNetQoeCallBack(
                    "dev.herovitamin.hms.multiplekits",
                    callback
                )
            } catch (ex: RemoteException) {

            }
        }
    }

    private fun unbindService() {
        unbindService(serviceConnection)
    }

    private fun initValues() {
        showStatus("Initializing values")
        networkQoEClient = WirelessClient.getNetworkQoeClient(this)

        serviceConnection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
                qoeService = null
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                qoeService = IQoeService.Stub.asInterface(service)
                obtainRealTimeData()
            }
        }

        callback = object : IQoeCallBack.Stub() {
            override fun callBack(type: Int, qoeInfo: Bundle?) {
                if(qoeInfo != null || type != NETWORK_QOE_INFO_TYPE){
                    Log.e(TAG, "callback failed type: ${type}")
                }
                var channel : Int = 0
                if(qoeInfo?.containsKey("channelNum")!!){
                    channel = qoeInfo?.getInt("channelNum")
                }
                var channelQoe = channel.toString()
                for (i in 0 until channel) {
                    uLRtt[i] = qoeInfo.getInt("uLRtt$i")
                    dLRtt[i] = qoeInfo.getInt("dLRtt$i")
                    uLBandwidth[i] = qoeInfo.getInt("uLBandwidth$i")
                    dLBandwidth[i] = qoeInfo.getInt("dLBandwidth$i")
                    uLRate[i] = qoeInfo.getInt("uLRate$i")
                    dLRate[i] = qoeInfo.getInt("dLRate$i")
                    netQoeLevel[i] = qoeInfo.getInt("netQoeLevel$i")
                    uLPkgLossRate[i] = qoeInfo.getInt("uLPkgLossRate$i")
                    channelIndex[i] = qoeInfo.getInt("channelIndex$i")
                    // channelQoe can be displayed on the user interface through EditText.
                    channelQoe += ("," + channelIndex[i].toString() + "," + uLRtt[i]
                        .toString() + "," + dLRtt[i].toString() + "," + uLBandwidth[i]
                        .toString() + ","
                            + dLBandwidth[i].toString() + "," + uLRate[i]
                        .toString() + "," + dLRate[i].toString() + "," + netQoeLevel[i]
                        .toString() + ","
                            + uLPkgLossRate[i])
                }
            }

        }
    }

    private fun showStatus(message : String){
        var text = QoEstatus.text
        QoEstatus.text = "${text}\n${message}"
    }

    private fun showValues(message : String){
        QoEvalues.text = message
    }
}