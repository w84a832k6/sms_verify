package com.example.smsverify

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smsverify.adapter.MessageListAdapter
import com.example.smsverify.databinding.ActivityMessageListBinding
import com.example.smsverify.viewModels.MessageViewModel
import com.example.smsverify.viewModels.MessageViewModelFactory

class MessageListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMessageListBinding

    private val messageViewModel: MessageViewModel by viewModels {
        MessageViewModelFactory((application as MessageApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMessageListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = MessageListAdapter()
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(this)

        messageViewModel.allMessages.observe(this) { messages ->
            messages.let {
                adapter.submitList(it)
            }
        }
    }
}