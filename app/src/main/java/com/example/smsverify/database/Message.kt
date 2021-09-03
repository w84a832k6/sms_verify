package com.example.smsverify.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Message(
    @ColumnInfo(name = "from") var from: String = "",
    @ColumnInfo(name = "context") var context: String = "",
    @ColumnInfo(name = "status") var status: Boolean = false
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
