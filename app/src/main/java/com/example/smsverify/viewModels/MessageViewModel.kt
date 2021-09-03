package com.example.smsverify.viewModels

import androidx.lifecycle.*
import com.example.smsverify.database.Message
import com.example.smsverify.repositories.MessageRepository
import kotlinx.coroutines.launch

class MessageViewModel (private val repository: MessageRepository): ViewModel() {
    val allMessages: LiveData<List<Message>> = repository.allMessage.asLiveData()

    fun insertAll(vararg message: Message) = viewModelScope.launch {
        repository.insertAll(*message)
    }
}

class MessageViewModelFactory(private val repository: MessageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MessageViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}