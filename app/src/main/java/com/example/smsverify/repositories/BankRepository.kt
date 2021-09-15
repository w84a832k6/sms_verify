package com.example.smsverify.repositories

import com.example.smsverify.database.Bank
import com.example.smsverify.database.BankDao
import kotlinx.coroutines.flow.Flow

class BankRepository(private val bankDao: BankDao) {
    val getAll: Flow<List<Bank>> = bankDao.getAllBankName()

    fun insertAll(banks: List<Bank>) {
        bankDao.insertAll(banks)
    }

    fun getBankByFrom(from: String) {
        bankDao.getBankByFrom(from)
    }

    fun deleteTable() {
        bankDao.deleteTable()
    }
}