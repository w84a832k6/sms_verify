package com.example.smsverify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.widget.Toast
import android.os.Bundle


class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val pdus = intent?.extras?.get("pdus") as Array<*>
        pdus.forEach {
            val sms = SmsMessage.createFromPdu(it as ByteArray, "3gpp")
            val body = sms.messageBody

            Toast.makeText(context, body, Toast.LENGTH_SHORT).show()

            //存入sqlite 送出request
        }
    }
}