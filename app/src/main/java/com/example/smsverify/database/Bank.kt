package com.example.smsverify.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["slug", "from"], name = "slug_from_index", unique = true)])
data class Bank(
    @ColumnInfo(name = "slug") var slug: String = "",
    @ColumnInfo(name = "name") var name: String = "",
    @ColumnInfo(name = "from") var from: String = "",
    ) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
