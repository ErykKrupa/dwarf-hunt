package com.example.krasnalhunt.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng


@Entity(tableName = "dwarf_item")
data class DwarfItem(
    val name: String,
    val address: String,
    val coordinates: LatLng,
    val location: String,
    val author: String,
    val fileName: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)

class LatLngTypeConverter {
    @TypeConverter
    fun toString(coordinates: LatLng) = "${coordinates.latitude},${coordinates.longitude}"
    @TypeConverter
    fun toLatLng(s: String) = s.split(",").let {
        LatLng(it[0].toDouble(), it[1].toDouble())
    }
}