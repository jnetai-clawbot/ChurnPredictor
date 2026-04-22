package com.jnetai.churnpredictor.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jnetai.churnpredictor.model.Customer
import com.jnetai.churnpredictor.model.Interaction
import com.jnetai.churnpredictor.util.Converters

@Database(entities = [Customer::class, Interaction::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun interactionDao(): InteractionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "churn_predictor_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}