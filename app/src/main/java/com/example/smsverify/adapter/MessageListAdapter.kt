package com.example.smsverify.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smsverify.R
import com.example.smsverify.database.Message

class MessageListAdapter: ListAdapter<Message, MessageListAdapter.MessageViewHolder>(MessageComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.context)
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageItemView: TextView = itemView.findViewById(R.id.textView)

        fun bind(text: String?) {
            messageItemView.text = text
        }

        companion object {
            fun create(parent: ViewGroup): MessageViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_item, parent, false)
                return MessageViewHolder(view)
            }
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

