package com.example.smsverify

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smsverify.adapter.MessageListAdapter
import com.example.smsverify.viewModels.MessageViewModel
import com.example.smsverify.viewModels.MessageViewModelFactory
import kotlinx.android.synthetic.main.activity_message_list.*

class MessageListActivity : AppCompatActivity() {
    private val messageViewModel: MessageViewModel by viewModels {
        MessageViewModelFactory((application as MessageApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_message_list)

        val recyclerView = recyclerview
        val adapter = MessageListAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        messageViewModel.allMessages.observe(this) { messages ->
            messages.let {
                adapter.submitList(it)
            }
        }
    }
}