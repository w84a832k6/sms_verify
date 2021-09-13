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

    @Query("SELECT * FROM message ORDER BY id DESC")
    fun getAlphabetizedWords(): Flow<List<Message>>

    @Query("SELECT * FROM message WHERE id IN (:ids)")
    fun findByIds(ids: IntArray): List<Message>

    @Query("SELECT * FROM message WHERE id = (:id)")
    fun findById(id: Long): Message

    @Query("UPDATE message SET status = (:status) WHERE id = (:id)")
    fun updateStatus(id: Long, status: Boolean)

    @Insert
    fun insertAll(vararg message: Message)

    @Insert
    fun insert(message: Message): Long

    @Delete
    fun delete(message: Message)
}