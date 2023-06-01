package com.example.cellmonitor

import android.Manifest
import android.annotation.TargetApi
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.Settings
import android.telephony.*
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.cellmonitor.databinding.ActivityMainBinding
import java.lang.StringBuilder
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var context: Context
    private val REQUEST_CODE : Int = 1000
    private val TAG = "appMainActivity"

    private val permissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.READ_PHONE_STATE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context =applicationContext

        checkPermission(permissions,REQUEST_CODE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            checkLogPermission()
        }

        binding.showCell.setOnClickListener {


            cellInfoToTV()
            //Timer処理は無し
            /*
            //Handler を用いてTimerの処理をUIスレッドで行うようにする
            //画面に関する処理はUIスレッドで行う必要がある
            val handler = Handler(Looper.getMainLooper())
            timer(period = 1000,initialDelay = 1000){
                handler.post {
                    Log.d(TAG,"Handler Posted")
                    cellInfoToTV()
                }
            }

             */
        }

        binding.btnLogStart.setOnClickListener {
            mySerCheck()
            val intent = Intent(this,MyService::class.java)
            startForegroundService(intent)
        }
        binding.btnLogStop.setOnClickListener {
            mySerCheck()
            val intent = Intent(this,MyService::class.java)
            stopService(intent)
        }

        binding.test5GListener.setOnClickListener {
            val intent = Intent(this,listen5GActivity::class.java)
            startActivity(intent)
        }



    }

    //戻るボタンを押した際にダイアログを表示
    override fun onBackPressed() {
        val dialog = BackPressedDialogFragment()
        dialog.show(supportFragmentManager,"id")
    }

    override fun onDestroy() {
        Log.d(TAG,"onDestroy")
        super.onDestroy()
    }

    fun mySerCheck():Boolean{
        //ログ保存サービスが起動中かをチェック
        val am: ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val serlist = am.getRunningServices(Integer.MAX_VALUE)
        for (info in serlist) {
            if (MyService::class.java.canonicalName.equals(info.service.getClassName())) {
                Log.d("MyService","ser running")
                return true
            }
        }
        Log.d("MyService","ser isn't ruunning")
        return false
    }


    //Permissionチェックのメソッド
    fun checkPermission(permissions: Array<String>?, request_code: Int) {
        // 許可されていないものだけダイアログが表示される
        ActivityCompat.requestPermissions(this, permissions!!, request_code)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun cellInfoToTV(){
        getCellInformation { cellInfoList , signalStrength ->

            val gld = GetLogData(context)
            val file = gld.getFileStatus(GetTimeData().getFileName()+".txt")

            val sb = StringBuilder()
            val length = cellInfoList.size



            sb.append(
                signalStrength.toString()
            )
            sb.append("\n")

            sb.append("-------------------")

            sb.append("\n")

            sb.append(
                cellInfoList.toString()
            )
            sb.append("\n---------------------\n")
            sb.append("GetCell：${length}")
            sb.append("\n")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                for (cellInfo in cellInfoList){
                    when(cellInfo){
                        is CellInfoLte ->{
                            Log.d(TAG,"CellInfoLTE")
                            val timeStamp = cellInfo.timeStamp
                            val cellID = cellInfo.cellIdentity.ci
                            val mccmnc = cellInfo.cellIdentity.mobileNetworkOperator
                            val EARFCN = cellInfo.cellIdentity.earfcn
                            val pci = cellInfo.cellIdentity.pci
                            val tac = cellInfo.cellIdentity.tac
                            val rsrp = cellInfo.cellSignalStrength.rsrp
                            val rsrq = cellInfo.cellSignalStrength.rsrq
                            val rssi = cellInfo.cellSignalStrength.rssi
                            val rssnr = cellInfo.cellSignalStrength.rssnr
                            val cqi = cellInfo.cellSignalStrength.cqi

                            sb.append("TimeStamp:${timeStamp}ns\n")
                            sb.append("CellType:LTE\n")
                            sb.append("CellID:$cellID\n")
                            sb.append("MCC+MNC:$mccmnc\n")
                            sb.append("CellType:LTE\n")
                            sb.append("EARFCN:$EARFCN\n")
                            sb.append("PCI:$pci\n")
                            sb.append("TAC:$tac\n")
                            sb.append("RSRP:$rsrp\n")
                            sb.append("RSSQ:$rsrq\n")
                            sb.append("RSSI:$rssi\n")
                            sb.append("RSSNR$rssnr\n")
                            sb.append("CQI:$cqi\n")
                            sb.append("------------------")
                            sb.append("\n")
                        }
                        is CellInfoNr->{
                            Log.d(TAG,"CellInfoNR")
                            val timeStamp = cellInfo.timeStamp
                            val cellID = (cellInfo.cellIdentity as CellIdentityNr).nci
                            val mcc =(cellInfo.cellIdentity as CellIdentityNr).mccString
                            val mnc =(cellInfo.cellIdentity as CellIdentityNr).mncString
                            val NRARFCN =(cellInfo.cellIdentity as CellIdentityNr).nrarfcn
                            val pci =(cellInfo.cellIdentity as CellIdentityNr).pci
                            val tac =(cellInfo.cellIdentity as CellIdentityNr).tac
                            val ssRsrp =(cellInfo.cellSignalStrength as CellSignalStrengthNr).ssRsrp
                            val ssRsrq =(cellInfo.cellSignalStrength as CellSignalStrengthNr).ssRsrq
                            val ssSinr =(cellInfo.cellSignalStrength as CellSignalStrengthNr).ssSinr
                            val csiRsrp = (cellInfo.cellSignalStrength as CellSignalStrengthNr).csiRsrp
                            val csiRsrq =(cellInfo.cellSignalStrength as CellSignalStrengthNr).csiRsrq
                            val csiSinr =(cellInfo.cellSignalStrength as CellSignalStrengthNr).csiSinr

                            sb.append("TimeStamp:${timeStamp}ns\n")
                            sb.append("CellType:NR\n")
                            sb.append("NCI:$cellID\n")
                            sb.append("MCC+MNC:${mcc+mnc}\n")
                            sb.append("NRARFCN:$NRARFCN\n")
                            sb.append("PCI:$pci\n")
                            sb.append("TAC:$tac\n")
                            sb.append("(SS)RSRP:$ssRsrp\n")
                            sb.append("(SS)RSRQ:$ssRsrq\n")
                            sb.append("(SS)SINR:$ssSinr\n")
                            sb.append("(CSI)RSRP:$csiRsrp\n")
                            sb.append("(CSI)RSRQ:$csiRsrq\n")
                            sb.append("(CSI)SINR:$csiSinr\n")
                            sb.append("------------------")
                            sb.append("\n")
                        }
                    }
                }
            }
            else{
                for (cellInfo in cellInfoList){
                    when(cellInfo){
                        is CellInfoLte ->{
                            Log.d(TAG,"CellInfoLTE")
                            val timeStamp = cellInfo.timeStamp
                            val cellID = cellInfo.cellIdentity.ci
                  //          val mccmnc = cellInfo.cellIdentity.mobileNetworkOperator
                            val EARFCN = cellInfo.cellIdentity.earfcn
                            val pci = cellInfo.cellIdentity.pci
                            val tac = cellInfo.cellIdentity.tac
                            val rsrp = cellInfo.cellSignalStrength.rsrp
                            val rsrq = cellInfo.cellSignalStrength.rsrq
                  //          val rssi = cellInfo.cellSignalStrength.rssi
                            val rssnr = cellInfo.cellSignalStrength.rssnr
                            val cqi = cellInfo.cellSignalStrength.cqi

                            sb.append("TimeStamp:${timeStamp}ns\n")
                            sb.append("CellType:LTE\n")
                            sb.append("CellID:$cellID\n")
                    //        sb.append("MCC+MNC:$mccmnc\n")
                            sb.append("CellType:LTE\n")
                            sb.append("EARFCN:$EARFCN\n")
                            sb.append("PCI:$pci\n")
                            sb.append("TAC:$tac\n")
                            sb.append("RSRP:$rsrp\n")
                            sb.append("RSSQ:$rsrq\n")
                      //      sb.append("RSSI:$rssi\n")
                            sb.append("RSSNR$rssnr\n")
                            sb.append("CQI:$cqi\n")
                            sb.append("------------------")
                            sb.append("\n")
                        }
                    }
                }
            }

            binding.cellinfo.text = sb.toString()
            gld.getLog(file,sb.toString())
        }
    }

    //Cellの取得
    @RequiresApi(Build.VERSION_CODES.P)
    private fun getCellInformation(result:(List<CellInfo>, SignalStrength)-> Unit) {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

               Toast.makeText(this,"位置情報の権限がONではありません。",Toast.LENGTH_SHORT).show()
                return
            }


            telephonyManager.requestCellInfoUpdate(mainExecutor, object : TelephonyManager.CellInfoCallback() {
                override fun onCellInfo(cellInfoList: MutableList<CellInfo>) {
                    telephonyManager.signalStrength?.let { result.invoke(cellInfoList, it) }

                }
            })
        }else{
            telephonyManager.signalStrength?.let { result.invoke(telephonyManager.allCellInfo, it) }
        }

    }

    // requestPermissionsのコールバック
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE -> {
                var i = 0
                while (i < permissions.size) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        /*     Toast toast = Toast.makeText(this,
                                "Added Permission: " + permissions[i], Toast.LENGTH_SHORT);
                        toast.show(); */
                    } else {
                        val toast = Toast.makeText(this,
                            "設定より権限をオンにした後、アプリを再起動してください", Toast.LENGTH_LONG)
                        toast.show()
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        //Fragmentの場合はgetContext().getPackageName()
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    i++
                }
            }
            else -> {
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.R)
    private fun checkLogPermission(){
        if (Environment.isExternalStorageManager()){
            //todo when permission is granted
            Log.d(TAG,"MANAGE_EXTERNAL_STORAGE is Granted")
        }else{
            //request for the permission
            val logIntent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            val uri = Uri.fromParts("package",packageName,null)
            logIntent.data = uri
            startActivity(logIntent)
        }
    }


}