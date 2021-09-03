package com.example.smsverify.repositories

import com.example.smsverify.database.Message
import com.example.smsverify.database.MessageDao
import kotlinx.coroutines.flow.Flow

class MessageRepository(private val MessageDao: MessageDao) {
    val allMessage: Flow<List<Message>> = MessageDao.getAlphabetizedWords()

    fun insertAll(vararg message: Message) {
        MessageDao.insertAll(*message)
    }
}