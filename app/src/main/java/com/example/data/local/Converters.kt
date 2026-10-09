package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.MediaType
import com.example.data.model.MessageStatus

class Converters {
    @TypeConverter
    fun fromMessageStatus(value: MessageStatus): String = value.name

    @TypeConverter
    fun toMessageStatus(value: String): MessageStatus = try {
        MessageStatus.valueOf(value)
    } catch (_: Exception) {
        MessageStatus.SENT
    }

    @TypeConverter
    fun fromMediaType(value: MediaType): String = value.name

    @TypeConverter
    fun toMediaType(value: String): MediaType = try {
        MediaType.valueOf(value)
    } catch (_: Exception) {
        MediaType.TEXT
    }
}
