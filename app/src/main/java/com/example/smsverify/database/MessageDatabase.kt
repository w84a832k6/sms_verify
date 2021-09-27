package com.example.smsverify.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Message::class], version = 2)
abstract class MessageDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao

    private class MessageDatabaseCallback(private val scope: CoroutineScope) :
        RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                {
                    scope.launch {
                        val messageDao = database.messageDao()

                        val message = Message("test", "test")
                        messageDao.insertAll(message)
                    }
                }
            }
        }
    }

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: MessageDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): MessageDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MessageDatabase::class.java,
                    "message"
                ).addMigrations(Migration1To2()).addCallback(MessageDatabaseCallback(scope)).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

    class Migration1To2: Migration(1,2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE message ADD COLUMN timestamp INTEGER NOT NULL DEFAULT(0)")
        }
    }
}