package dev.herovitamin.hms.multiplekits

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hmf.tasks.Task
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.api.entity.core.CommonCode
import com.huawei.hms.support.api.entity.safetydetect.MaliciousAppsData
import com.huawei.hms.support.api.entity.safetydetect.MaliciousAppsListResp
import com.huawei.hms.support.api.entity.safetydetect.SysIntegrityResp
import com.huawei.hms.support.api.safetydetect.SafetyDetect
import com.huawei.hms.support.api.safetydetect.SafetyDetectStatusCodes
import kotlinx.android.synthetic.main.activity_safety_detect.*
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom


class SafetyDetectActivity : AppCompatActivity() {

    private val TAG : String? = SafetyDetectActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_safety_detect)

        setListeners()

    }

    private fun setListeners() {
        malicious_list_btn.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                clearMessage()
                invokeGetMaliciousApps()
            }
        })

        sys_integrity_btn.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                clearMessage()
                invokeSysIntegrity()
            }
        })
    }

    fun clearMessage(){
        safety_message_tv.text = ""
    }

    fun showMessage(message : String){
        val tmpText = safety_message_tv.text
        safety_message_tv.text = tmpText.toString() + message
    }

    fun invokeGetMaliciousApps(){
        val safetyDetectClient = SafetyDetect.getClient(this@SafetyDetectActivity)
        val task = safetyDetectClient.maliciousAppsList

        task
            .addOnSuccessListener(
                object : OnSuccessListener<MaliciousAppsListResp> {
                    override fun onSuccess(maliciousAppsListResp: MaliciousAppsListResp?) {
                        val appsDataList: List<MaliciousAppsData> =
                            maliciousAppsListResp!!.getMaliciousAppsList()

                        if (maliciousAppsListResp.rtnCode === CommonCode.OK) {
                            if (appsDataList.isEmpty()) {
                                showMessage("There are no known potentially malicious apps installed.")
                            } else {
                                showMessage("Potentially malicious apps are installed! \n")
                                for (maliciousApp in appsDataList) {
                                    showMessage("Information about a malicious app:" + "\n")
                                    showMessage("APK: " + maliciousApp.apkPackageName + "\n")
                                    showMessage("SHA-256: " + maliciousApp.apkSha256 + "\n")
                                    showMessage("Category: " + maliciousApp.apkCategory + "\n")
                                }
                            }
                        } else {
                            showMessage("getMaliciousAppsList failed: " + maliciousAppsListResp.errorReason)
                        }
                    }
                })
            .addOnFailureListener(object : OnFailureListener{
                override fun onFailure(e: Exception?) {
                    if (e is ApiException) {
                        val apiException = e
                        showMessage("Error: " + SafetyDetectStatusCodes.getStatusCodeString(apiException.statusCode) + ": " + apiException.statusMessage)
                    } else {
                        showMessage("ERROR: " + e?.message)
                    }
                }
            })
    }

    fun invokeSysIntegrity(){
        val client = SafetyDetect.getClient(this@SafetyDetectActivity)
        val nonce = ByteArray(24)

        try {
            val random: SecureRandom
            random = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                SecureRandom.getInstanceStrong()
            } else {
                SecureRandom.getInstance("SHA1PRNG")
            }
            random.nextBytes(nonce)
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, e.message)
        }

        //TODO: Insert your own app ID
        val task: Task<SysIntegrityResp> = client.sysIntegrity(nonce, "**********")

        task
            .addOnSuccessListener(object : OnSuccessListener<SysIntegrityResp>{
            override fun onSuccess(response: SysIntegrityResp) {
                val jwsStr = response.result
                Log.i(TAG, "Connection with server completed, parsing of result is required")

            } })
            .addOnFailureListener(object : OnFailureListener{
                override fun onFailure(e: java.lang.Exception?) {
                    if (e is ApiException) {
                        val apiException = e
                        Log.e(TAG, "Error: " + SafetyDetectStatusCodes.getStatusCodeString(apiException.statusCode) + ": " + apiException.message)
                    } else {
                        // A different, unknown type of error occurred.
                        Log.e(TAG, "ERROR:" + e?.message)
                    }
                }

            })




    }
}