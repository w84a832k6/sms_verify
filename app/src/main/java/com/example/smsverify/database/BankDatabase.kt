package com.example.smsverify.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Bank::class], version = 1)
abstract class BankDatabase: RoomDatabase() {
    abstract fun bankDao(): BankDao

    private class BankDatabaseCallback(private val scope: CoroutineScope) :
        RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                {
                    scope.launch {
                        val bankDao = database.bankDao()

                        val bank = Bank("test", "test")
//                        bankDao.insertAll(bank)
                    }
                }
            }
        }
    }

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: BankDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BankDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BankDatabase::class.java,
                    "bank"
                ).addCallback(BankDatabaseCallback(scope)).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}