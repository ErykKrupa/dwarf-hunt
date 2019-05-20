package com.example.krasnalhunt.model

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface DwarfItemDao {
    @Query("SELECT * FROM dwarf_item")
    fun findItems(): LiveData<List<DwarfItem>>

    @Query("SELECT * FROM dwarf_item WHERE id = :id")
    fun findItem(id: Int): DwarfItem

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItems(vararg items: DwarfItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItems(items: List<DwarfItem>)

    @Update
    fun updateItems(vararg items: DwarfItem)

    @Delete
    fun deleteItems(vararg items: DwarfItem)

    @Query("UPDATE dwarf_item SET caught = 0")
    fun resetCaught()
}