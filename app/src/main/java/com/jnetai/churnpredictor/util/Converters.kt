package com.jnetai.churnpredictor.util

import androidx.room.TypeConverter
import com.jnetai.churnpredictor.model.PlanType

class Converters {
    @TypeConverter
    fun fromPlanType(value: PlanType): String = value.name

    @TypeConverter
    fun toPlanType(value: String): PlanType = PlanType.valueOf(value)
}