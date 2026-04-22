package com.jnetai.churnpredictor

import android.app.Application
import com.jnetai.churnpredictor.data.AppDatabase

class ChurnPredictorApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
}