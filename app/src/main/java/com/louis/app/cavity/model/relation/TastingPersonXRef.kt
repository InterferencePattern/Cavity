package com.louis.app.cavity.model.relation

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["tasting_id", "friend_id"])
data class TastingPersonXRef(
    @ColumnInfo(name = "tasting_id") val tastingId: Long,
    @ColumnInfo(name = "firend_id") val friendId: Long
)
