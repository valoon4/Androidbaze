package com.valoon4.communicationtrainer.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Stable, locally generated UUIDs; never derive IDs from message text. */
@Entity(tableName = "conversations", indices = [Index(value = ["updatedAt"])])
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val mode: String = "free",
    val personaRef: String? = null,
    val personaVersion: Int? = null,
    val scenarioRef: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "replay_branches",
    foreignKeys = [ForeignKey(
        entity = ConversationEntity::class,
        parentColumns = ["id"],
        childColumns = ["conversationId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["conversationId"])]
)
data class ReplayBranchEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val parentBranchId: String? = null,
    val forkMessageId: String? = null,
    val snapshotRef: String? = null,
    val createdAt: Long
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ReplayBranchEntity::class,
            parentColumns = ["id"],
            childColumns = ["branchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversationId", "createdAt"]),
        Index(value = ["branchId"]),
        Index(value = ["clientMessageId"], unique = true)
    ]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val branchId: String,
    val clientMessageId: String,
    val role: String,
    val content: String,
    val status: String,
    val createdAt: Long
)

@Entity(
    tableName = "drafts",
    primaryKeys = ["conversationId", "branchId"],
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ReplayBranchEntity::class,
            parentColumns = ["id"],
            childColumns = ["branchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["branchId"])]
)
data class DraftEntity(
    val conversationId: String,
    val branchId: String,
    val text: String,
    val editRevision: Long,
    val updatedAt: Long
)

fun rootBranchId(conversationId: String): String = "$conversationId:main"
