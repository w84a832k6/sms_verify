package com.example.smsverify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.widget.Toast
import com.example.smsverify.database.Message
import com.example.smsverify.database.MessageDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pdus = intent?.extras?.get("pdus") as Array<*>
        pdus.forEach {
            val sms = SmsMessage.createFromPdu(it as ByteArray, "3gpp")
            val body = sms.messageBody
            val fromNumber = sms.originatingAddress

            Toast.makeText(context, "from:$fromNumber\nbody : $body", Toast.LENGTH_SHORT).show()

            GlobalScope.launch {
                val messageDao = MessageDatabase.getDatabase(context, this).messageDao()
                messageDao.insertAll(Message(fromNumber.toString(), body))
            }
            //存入sqlite 送出request
//            val messageDao = MessageDatabase.getDatabase(context, GlobalScope).messageDao()
//            GlobalScope.launch {
//                messageDao.insertAll(Message(from = fromNumber.toString(), context = body))
//            }
        }
    }
}