package com.example.cellmonitor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneStateListener
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cellmonitor.databinding.ActivityListen5GactivityBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.timer

class listen5GActivity : AppCompatActivity() {

    private val TAG = "listen5GActivity"

    private lateinit var binding: ActivityListen5GactivityBinding
    private lateinit var mAdapter:nrViewAdapter
    private lateinit var recyclerView:RecyclerView
    lateinit var mStateList:ArrayList<NetworkData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListen5GactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.recyclerView1
        recyclerView.layoutManager = LinearLayoutManager(this)

        mAdapter = nrViewAdapter(this)
        recyclerView.adapter = mAdapter

        binding.fab.setOnClickListener {
            if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.R){
                getNetworkType()
            }else{
                Log.d(TAG,"API足りない")
                getFoo()
            }
        }


    }

    private fun getFoo(){
        //Handler を用いてTimerの処理をUIスレッドで行うようにする
        //画面に関する処理はUIスレッドで行う必要がある
        val handler = Handler(Looper.getMainLooper())
        timer(period = 10000,initialDelay = 1000){
            handler.post {
                Log.d(TAG,"Handler Posted")
                setNetworkStateText("Foo")
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    private fun getNetworkType() {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(object : PhoneStateListener() {

            @RequiresApi(Build.VERSION_CODES.R)
            override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.READ_PHONE_STATE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                super.onDisplayInfoChanged(telephonyDisplayInfo)

                when (telephonyDisplayInfo.overrideNetworkType) {
                    // LTE等(4G回線)
                    TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NONE -> setNetworkStateText("LTE等")
                    // LTE等(キャリアアグリゲーション)
                    TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_CA -> setNetworkStateText("LTE等(キャリアアグリゲーション)")
                    //  LTE Advanced Pro（5Ge）
                    TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_ADVANCED_PRO -> setNetworkStateText(
                        "LTE Advanced Pro（5Ge）"
                    )
                    // 5G NR（Sub-6）ネットワーク
                    TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA -> setNetworkStateText("5G NR（Sub-6）")
                    // 5G mmWave（5G+ / 5G UW）ネットワーク
                    TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE -> setNetworkStateText(
                        "5G mmWave（5G+ / 5G UW）"
                    )
                    else -> setNetworkStateText("その他")
                }
            }
        }, PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED)
    }

    private fun setNetworkStateText(state: String) {
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val networkData = NetworkData(date, state)
        mAdapter.add(networkData)
        recyclerView.scrollToPosition(recyclerView.adapter?.itemCount?.minus(1)!!)
    }
}