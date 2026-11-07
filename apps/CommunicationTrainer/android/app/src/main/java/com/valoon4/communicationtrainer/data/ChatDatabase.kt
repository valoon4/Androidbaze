package com.valoon4.communicationtrainer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Initial D02 schema v1. Export the schema; no destructive migration fallback.
 * D01 had no database, so no historical data migration is required.
 * Every subsequent version bump must ship an explicit verified migration.
 */
@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        DraftEntity::class,
        ReplayBranchEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        const val DATABASE_NAME = "communicationtrainer.db"

        @Volatile
        private var instance: ChatDatabase? = null

        fun get(context: Context): ChatDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    DATABASE_NAME
                )
                    .build()
                    .also { instance = it }
            }
    }
}
