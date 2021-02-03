package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.relation.BottleAndWineWithQGrapesAndFReviews

@Dao
interface BottleDao {
    @Insert
    suspend fun insertBottle(bottle: Bottle): Long

    @Update
    suspend fun updateBottle(bottle: Bottle)

    @Delete
    suspend fun deleteBottle(bottle: Bottle)

    @Query("SELECT * FROM bottle WHERE bottle_id=:bottleId")
    fun getBottleById(bottleId: Long): LiveData<Bottle>

    @Query("SELECT * FROM bottle WHERE bottle_id=:bottleId")
    suspend fun getBottleByIdNotLive(bottleId: Long): Bottle

    @Query("UPDATE bottle SET is_favorite = 1 WHERE bottle_id=:bottleId")
    suspend fun fav(bottleId: Long)

    @Query("UPDATE bottle SET is_favorite = 0 WHERE bottle_id=:bottleId")
    suspend fun unfav(bottleId: Long)

    @Query("UPDATE bottle SET count=:count + bottle.count WHERE bottle_id=:bottleId")
    suspend fun addBottles(bottleId: Long, count: Int)

    @Query("UPDATE bottle SET count=:count - bottle.count WHERE bottle_id=:bottleId")
    suspend fun removeBottles(bottleId: Long, count: Int)

    @Query("DELETE FROM bottle WHERE bottle_id=:bottleId")
    suspend fun deleteBottleById(bottleId: Long)

    @Transaction
    @Query("SELECT * FROM wine, bottle WHERE wine.wine_id = bottle.wine_id")
    suspend fun getBottleAndWineWithQGrapesAndFReview(): List<BottleAndWineWithQGrapesAndFReviews>
}
