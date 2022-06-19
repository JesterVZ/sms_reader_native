package com.example.smsreceiver

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.CountDownTimer
import android.os.Message
import android.provider.Telephony
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import com.example.smsreceiver.databinding.ActivityMainBinding
import java.time.LocalDateTime
import java.util.*

class MainActivity : AppCompatActivity() {
    var messageText:TextView? = null
    var button:Button? = null
    var canListen: Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannel()
        messageText = findViewById(R.id.messageText)
        button = findViewById(R.id.startButton)
        button!!.setOnClickListener {
            if(canListen == false){
                canListen = true
                button!!.text = "Stop"
            } else {
                canListen = false
                button!!.text = "Start"
            }
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS), 111)
        } else {
            receiveMsg()
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun scheduleNotification(title: String, message: String){
        var intent = Intent(applicationContext, Notification::class.java)
        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, message)

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            pendingIntent
        )
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val name = "SmsReceiver"
        val descr = "Description"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance)
        channel.description = descr
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            receiveMsg()
        }
    }
    private fun receiveMsg(){
        var br = object: BroadcastReceiver(){
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceive(context: Context?, intent: Intent?) {
                for(sms in Telephony.Sms.Intents.getMessagesFromIntent(intent)){
                    if(canListen){
                        messageText!!.text = sms.displayMessageBody
                        scheduleNotification(sms.displayOriginatingAddress, sms.displayMessageBody)
                    }
                }
            }
        }
        registerReceiver(br, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
    }
}