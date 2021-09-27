package com.example.smsverify.adapter

import android.app.AlertDialog
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smsverify.DataStoreManager
import com.example.smsverify.R
import com.example.smsverify.database.BankDatabase
import com.example.smsverify.database.Message
import com.example.smsverify.database.MessageDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException
import java.util.*

class MessageListAdapter :
    ListAdapter<Message, MessageListAdapter.MessageViewHolder>(MessageComparator()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.id, current.from, current.context, current.status, current.timestamp)
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageItemView: TextView = itemView.findViewById(R.id.message_textView)
        private val messageTimeView: TextView = itemView.findViewById(R.id.messageTime_textView)

        fun bind(id: Long, from: String, text: String, status: Boolean, timestamp: Long) {
            val formatDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val time = Date(timestamp)
            messageTimeView.text = formatDate.format(time);
            messageItemView.text = text
            if (status) {
                messageItemView.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.green
                    )
                )
            }

            messageItemView.setOnClickListener {
                val dialog = AlertDialog.Builder(it.context)
                    .setTitle(it.context.getString(R.string.alert_resend_title))
                    .setMessage(it.context.getString(R.string.alert_from, from) + "\n" + it.context.getString(R.string.alert_context, text))
                    .setPositiveButton(
                        it.context.getString(R.string.confirm)
                    ) { p0, p1 ->
                        GlobalScope.launch {
                            val messageDao =
                                MessageDatabase.getDatabase(it.context, this).messageDao()
                            val message = messageDao.findById(id)

                            val bankDao = BankDatabase.getDatabase(it.context, this).bankDao()
                            val bank = bankDao.getBankByFrom(message.from)

                            val requestData = JSONObject()
                            requestData.put("from_sms", JSONObject().put(bank.slug, bank.from))
                            requestData.put("content_sms", message.context)
                            requestData.put(
                                "phone_number",
                                DataStoreManager.getStringValue(
                                    it.context,
                                    DataStoreManager.DataKey.PHONE_NUMBER.getKey()
                                )
                            )

                            resendRequest(it.context, requestData.toString(), id)
                        }
                    }
                    .setNegativeButton(it.context.getString(R.string.cancel), null)
                dialog.show()
            }
        }

        companion object {
            fun create(parent: ViewGroup): MessageViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_item, parent, false)
                return MessageViewHolder(view)
            }
        }

        private suspend fun resendRequest(context: Context, requestData: String, messageId: Long) {
            val okHttpClient = OkHttpClient()

            val requestUrl =
                DataStoreManager.getStringValue(context, DataStoreManager.DataKey.TO_URL.getKey())

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
                    if (responseString.isNotEmpty() && responseString.subSequence(
                            0,
                            1
                        ) == "1"
                    ) { //驗證server是否收到
                        GlobalScope.launch {
                            val messageDao = MessageDatabase.getDatabase(context, this).messageDao()
                            messageDao.updateStatus(messageId, true)

                            DataStoreManager.setValue(
                                context,
                                DataStoreManager.DataKey.CONNECT_STATUS.getKey(),
                                true
                            )
                            DataStoreManager.setValue(
                                context,
                                DataStoreManager.DataKey.CONNECT_TIMESTAMP.getKey(),
                                System.currentTimeMillis()
                            )
                        }
                    } else {
                        Log.d("sendRequest", responseString)
                    }
                }
            })
        }
    }

    class MessageComparator : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.context == newItem.context
        }
    }
}

