package com.valoon4.communicationtrainer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ChatDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    abstract fun observeConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    abstract suspend fun getConversation(id: String): ConversationEntity?

    @Query("SELECT * FROM drafts WHERE conversationId = :conversationId AND branchId = :branchId LIMIT 1")
    abstract suspend fun getDraft(conversationId: String, branchId: String): DraftEntity?

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND branchId = :branchId ORDER BY createdAt, id")
    abstract fun observeMessages(conversationId: String, branchId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertBranch(branch: ReplayBranchEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceDraft(draft: DraftEntity)

    @Query("UPDATE conversations SET updatedAt = :timestamp WHERE id = :id")
    abstract suspend fun markActive(id: String, timestamp: Long)

    @Query("DELETE FROM conversations WHERE id = :id")
    abstract suspend fun deleteConversation(id: String): Int

    @Query("SELECT COUNT(*) FROM replay_branches WHERE conversationId = :id")
    abstract suspend fun countBranches(id: String): Int

    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :id")
    abstract suspend fun countMessages(id: String): Int

    @Transaction
    open suspend fun createWithMainBranch(conversation: ConversationEntity) {
        insertConversation(conversation)
        insertBranch(
            ReplayBranchEntity(
                id = rootBranchId(conversation.id),
                conversationId = conversation.id,
                createdAt = conversation.createdAt
            )
        )
    }

    /**
     * Revision gate + transaction prevents slow older writes from overwriting newer keystrokes.
     * A deleted conversation cannot be recreated by a delayed draft write (FKs are enforced).
     */
    @Transaction
    open suspend fun saveIfNewer(draft: DraftEntity) {
        val current = getDraft(draft.conversationId, draft.branchId)
        if (current == null || draft.editRevision > current.editRevision) {
            replaceDraft(draft)
            markActive(draft.conversationId, draft.updatedAt)
        }
    }
}
