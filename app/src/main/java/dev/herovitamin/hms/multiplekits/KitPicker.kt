package dev.herovitamin.hms.multiplekits

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_kit_picker.*

class KitPicker : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kit_picker)
        initListeners()
    }

    private fun initListeners() {
        wireless_kit_btn.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                startActivity(
                    Intent(
                        this@KitPicker,
                        WirelessKitActivity::class.java
                    )
                )
            }
        })

        hquic_kit_btn.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                startActivity(
                    Intent(
                        this@KitPicker,
                        HQuicKitActivity::class.java
                    )
                )
            }
        })

        safety_detect_btn.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                startActivity(
                    Intent(
                        this@KitPicker,
                        SafetyDetectActivity::class.java
                    )
                )
            }

        })
    }

}