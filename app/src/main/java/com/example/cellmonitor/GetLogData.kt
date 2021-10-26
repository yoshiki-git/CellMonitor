package com.example.cellmonitor

import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.lang.StringBuilder
import java.nio.charset.StandardCharsets

class GetLogData(val context: Context) {
    val TAG="TestApp"


    fun getFileStatus(fileName:String): File {
        val dir_myApp= File("/sdcard/Wifiアプリ")
        if (dir_myApp.exists()){
            Log.d(TAG,"App's dir is exist")
        }else{
            dir_myApp.mkdir()
            Log.d(TAG,"Made App's dir")
        }
        return File(dir_myApp,fileName)
    }


    fun getColumn(filepath: File, columns:List<String>){
        val sb = StringBuilder()
        sb.append("Time")
        for (value in columns){
            sb.append(",").append(value)
        }
        sb.append("\n")
        getLog(filepath,sb.toString())
    }

    fun getLog(filepath: File, log_data: String) {
        //ログを外部ストレージに保存する
        if (isExternalStorageWritable()) {
            try {
                FileOutputStream(filepath, true).use { fileOutputStream ->
                    OutputStreamWriter(
                        fileOutputStream,
                        StandardCharsets.UTF_8
                    ).use { outputStreamWriter ->
                        BufferedWriter(outputStreamWriter).use { bw ->
                            bw.write(log_data)
                            bw.flush()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Exception")
                e.printStackTrace()
                Toast.makeText(context,"ストレージが見つからないためログを保存できません。", Toast.LENGTH_LONG).show()
                Log.d("TAG", "トースト表示")
            }
        }
    }



    fun isExternalStorageWritable():Boolean{
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }
}