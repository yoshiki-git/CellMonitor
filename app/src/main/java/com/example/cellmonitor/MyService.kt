package com.example.cellmonitor

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.telephony.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.File
import java.lang.StringBuilder
import kotlin.concurrent.timer

class MyService : Service() {
    val TAG = "MyService"
    private lateinit var context: Context
    private lateinit var getLogData:GetLogData
    private lateinit var file: File
    private val getTimeData = GetTimeData()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG,"MyService is onCreate")
        //context取得
        context=applicationContext
        //getLogData生成
        getLogData= GetLogData(context)

        //ファイル名を現在時刻に設定する
        val start_time=getTimeData.getFileName()
        //拡張子をつける
        val fileName=start_time+"_Log"+".csv"
        file=getLogData.getFileStatus(fileName)

        //カラムの作成
        val columns = mutableListOf<String>()
        columns.add("timeStamp")
        columns.add("CellType")
        columns.add("CellID/NCI")
        columns.add("MCC+MNC")
        columns.add("EARFCN/NRARFCN")
        columns.add("PCI")
        columns.add("TAC")
        columns.add("RSRP/(SS)RSRP")
        columns.add("RSRQ/(SS)RSRQ")
        columns.add("RSSI")
        columns.add("RSSNR/(SS)SINR")
        columns.add("CQI")
        columns.add("(CSI)RSRP")
        columns.add("(CSI)RSRQ")
        columns.add("(CSI)SINR")
        getLogData.getColumn(file,columns)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG,"MyService is onStartCommand")
        val requestCode = intent!!.getIntExtra("REQUEST_CODE", 0)
        //    val context = applicationContext
        val channelId = "default"
        val title = context.getString(R.string.app_name)

        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager


        // Notification　Channel 設定
        val channel = NotificationChannel(
            channelId, title, NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Silent Notification"
        channel.setSound(null,null)
        channel.enableLights(false)
        channel.lightColor = Color.BLUE
        channel.enableVibration(false)


        notificationManager.createNotificationChannel(channel)
        val notification = Notification.Builder(context, channelId)
            .setContentTitle(title) // android標準アイコンから
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentText("Cellログ取得中")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setWhen(System.currentTimeMillis())
            .build()

        // startForeground 第一引数のidで通知を識別
        startForeground(9999, notification)

        cellInfoToLogdata()
        setNextAlarmService(context)


        //return START_NOT_STICKY;
        //return START_STICKY;
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        Log.d(TAG,"MyService is onDestroy")
        super.onDestroy()
        stopAlarmService()
        stopSelf()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    private fun cellInfoToLogdata(){
        getCellInformation { cellInfoList ->
            val sb = StringBuilder()
            val logtime = getTimeData.getNowTime()
          //  val length = cellInfoList.size
          //  sb.append("取得Cell：${length}個"
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

                            sb.append("$logtime,")
                            sb.append("$timeStamp,")
                            sb.append("LTE,")
                            sb.append("$cellID,")
                            sb.append("$mccmnc,")
                            sb.append("$EARFCN,")
                            sb.append("$pci,")
                            sb.append("$tac,")
                            sb.append("$rsrp,")
                            sb.append("$rsrq,")
                            sb.append("$rssi,")
                            sb.append("$rssnr,")
                            sb.append("$cqi,")
                            sb.append("-,")
                            sb.append("-,")
                            sb.append("-")
                            sb.append("\n")
                        }
                        is CellInfoNr ->{
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

                            sb.append("$logtime,")
                            sb.append("$timeStamp,")
                            sb.append("NR,")
                            sb.append("$cellID,")
                            sb.append("${mcc+mnc},")
                            sb.append("$NRARFCN,")
                            sb.append("$pci,")
                            sb.append("$tac,")
                            sb.append("$ssRsrp,")
                            sb.append("$ssRsrq,")
                            sb.append("-,")
                            sb.append("$ssSinr,")
                            sb.append("-,")
                            sb.append("$csiRsrp")
                            sb.append(csiRsrq)
                            sb.append(csiSinr)
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

                            sb.append("$logtime,")
                            sb.append("$timeStamp,")
                            sb.append("LTE,")
                            sb.append("$cellID,")
                            sb.append("-,")
                            sb.append("$EARFCN,")
                            sb.append("$pci,")
                            sb.append("$tac,")
                            sb.append("$rsrp,")
                            sb.append("$rsrq,")
                            sb.append("-,")
                            sb.append("$rssnr,")
                            sb.append("$cqi,")
                            sb.append("-,")
                            sb.append("-,")
                            sb.append("-")
                            sb.append("\n")
                        }
                    }
                }
            }
            val logdata = sb.toString()
            getLogData.getLog(file,logdata)
        }
    }


    //Cellの取得
    private fun getCellInformation(result:(List<CellInfo>)-> Unit) {
        val telephonyManager = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q){
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                Toast.makeText(context,"位置情報の権限がONではありません。", Toast.LENGTH_SHORT).show()
                return
            }
            //ちゃんと動作するのか不明
            telephonyManager.requestCellInfoUpdate(mainExecutor, object : TelephonyManager.CellInfoCallback() {
                override fun onCellInfo(cellInfoList: MutableList<CellInfo>) {
                    result.invoke(cellInfoList)
                }
            })
        }else{
            result.invoke(telephonyManager.allCellInfo)
        }

    }

    // 次のアラームの設定
    private fun setNextAlarmService(context: Context) {

        // 5s毎のアラーム設定
        val repeatPeriod = (5 * 1000).toLong()
        val intent = Intent(context, MyService::class.java)
        val startMillis = System.currentTimeMillis() + repeatPeriod
        val pendingIntent = PendingIntent.getService(context, 0, intent, 0)
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            startMillis, pendingIntent
        )
    }

    private fun stopAlarmService() {
        val indent = Intent(context, MyService::class.java)
        val pendingIntent = PendingIntent.getService(context, 0, indent, 0)

        // アラームを解除する
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

}