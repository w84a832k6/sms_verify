package com.example.smsverify.repositories

import com.example.smsverify.database.Message
import com.example.smsverify.database.MessageDao
import kotlinx.coroutines.flow.Flow

class MessageRepository(private val messageDao: MessageDao) {
    val allMessage: Flow<List<Message>> = messageDao.getAlphabetizedWords()

    fun insertAll(vararg message: Message) {
        messageDao.insertAll(*message)
    }

    fun insert(message: Message) {
        messageDao.insert(message)
    }

    fun updateStatus(id: Long, status: Boolean) {
        messageDao.updateStatus(id, status)
    }
}