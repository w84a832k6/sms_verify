package com.example.smsverify.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BankDao {
    @Query("SELECT * FROM bank GROUP BY name")
    fun getAllBankName(): Flow<List<Bank>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(banks: List<Bank>)

    @Query("SELECT * FROM bank WHERE `from` = (:from)")
    fun getBankByFrom(from: String): Bank

    @Query("DELETE FROM bank")
    fun deleteTable(): Int
}