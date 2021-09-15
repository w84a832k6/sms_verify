package com.example.smsverify

import android.app.Application
import com.example.smsverify.database.BankDatabase
import com.example.smsverify.database.MessageDatabase
import com.example.smsverify.repositories.BankRepository
import com.example.smsverify.repositories.MessageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MessageApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { MessageDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { MessageRepository(database.messageDao()) }

    val bankDatabase by lazy { BankDatabase.getDatabase(this, applicationScope) }
    val bankRepository by lazy { BankRepository(bankDatabase.bankDao()) }
}