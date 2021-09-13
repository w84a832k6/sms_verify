package com.example.smsverify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import com.example.smsverify.database.Message
import com.example.smsverify.database.MessageDao
import com.example.smsverify.database.MessageDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException


class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pdus = intent?.extras?.get("pdus") as Array<*>
        pdus.forEach {
            val sms = SmsMessage.createFromPdu(it as ByteArray, "3gpp")
            val body = sms.messageBody
            val fromNumber = sms.originatingAddress

            Toast.makeText(context, "from:$fromNumber\nbody : $body", Toast.LENGTH_SHORT).show()

            GlobalScope.launch {
                //存入sqlite
                val messageDao = MessageDatabase.getDatabase(context, this).messageDao()
                val rawId = messageDao.insert(Message(fromNumber.toString(), body))

                var requestData = JSONObject()
                requestData.put("from_sms", fromNumber)
                requestData.put("content_sms", body)
                requestData.put("phone_number", DataStoreManager.getStringValue(context, DataStoreManager.DataKey.PHONENUMBER.getKey()))

                //發送request
                sendRequest(context, requestData.toString(), rawId)
            }
        }
    }

    private suspend fun sendRequest(context: Context, requestData: String, messageId: Long) {
        val okHttpClient = OkHttpClient()

        val requestUrl = DataStoreManager.getStringValue(context, DataStoreManager.DataKey.TOURL.getKey())

        val postFormBody =
            RequestBody.create("application/json; charset=utf-8".toMediaType(), requestData)
        val request = Request.Builder().url(requestUrl).post(postFormBody).build()

        val call = okHttpClient.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpClient.newCall(call.request())
            }

            override fun onResponse(call: Call, response: Response) {
                val responseString = response.body?.string() ?: ""
                if (responseString.isNotEmpty()) { //驗證server是否收到
                    GlobalScope.launch {
                        val messageDao = MessageDatabase.getDatabase(context, this).messageDao()
                        messageDao.updateStatus(messageId, true)
                    }
                } else {
                    Log.d("sendRequest", responseString)
                }
            }
        })
    }
}