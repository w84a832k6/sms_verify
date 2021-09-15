package com.example.smsverify.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smsverify.R
import com.example.smsverify.database.Bank

class BankListAdapter: ListAdapter<Bank, BankListAdapter.BankViewHolder>(BankComparator()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankViewHolder {
        return BankViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: BankViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.name)
    }

    class BankViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bankItemView: TextView = itemView.findViewById(R.id.bankName_textView)

        fun bind(text: String) {
            bankItemView.text = text
        }

        companion object {
            fun create(parent: ViewGroup): BankViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.bank_recyclerview_item, parent, false)
                return BankViewHolder(view)
            }
        }
    }

    class BankComparator : DiffUtil.ItemCallback<Bank>() {
        override fun areItemsTheSame(oldItem: Bank, newItem: Bank): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Bank, newItem: Bank): Boolean {
            return oldItem.name == newItem.name
        }
    }
}

