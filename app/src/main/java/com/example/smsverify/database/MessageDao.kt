package com.example.smsverify.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM message")
    fun getAll(): Flow<List<Message>>

    @Query("SELECT * FROM message ORDER BY id ASC")
    fun getAlphabetizedWords(): Flow<List<Message>>

    @Query("SELECT * FROM message WHERE id IN (:ids)")
    fun findByIds(ids: IntArray): List<Message>

    @Query("SELECT * FROM message WHERE id = (:id)")
    fun findById(id: Int): Message

    @Insert
    fun insertAll(vararg message: Message)

    @Delete
    fun delete(message: Message)
}