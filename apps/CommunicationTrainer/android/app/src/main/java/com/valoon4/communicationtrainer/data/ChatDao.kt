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


    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    abstract suspend fun getMessage(id: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE clientMessageId = :clientId LIMIT 1")
    abstract suspend fun getByClientId(clientId: String): MessageEntity?

    @Query("UPDATE messages SET status = :nextStatus WHERE id = :id AND role = 'user' AND status = :expected")
    abstract suspend fun transitionUserStatus(id: String, expected: String, nextStatus: String): Int

    @Query("UPDATE conversations SET title = :title WHERE id = :id AND title = 'Neues Gespräch'")
    abstract suspend fun setInitialTitle(id: String, title: String)

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
    /**
     * One atomic local send: reject unsaved revisions and blank text, deduplicate the
     * client message id, insert exactly one queued message, then clear ONLY the
     * revision that was sent. No local text is lost if the transaction fails.
     */
    @Transaction
    open suspend fun enqueueSavedDraft(
        conversationId: String,
        expectedRevision: Long,
        clientMessageId: String,
        now: Long
    ): MessageEntity? {
        getByClientId(clientMessageId)?.let { return it }
        val conversation = getConversation(conversationId) ?: return null
        val branch = rootBranchId(conversationId)
        val draft = getDraft(conversationId, branch) ?: return null
        if (draft.editRevision != expectedRevision || draft.text.isBlank()) return null
        val content = draft.text.trim()
        val message = MessageEntity(
            id = clientMessageId,
            conversationId = conversationId,
            branchId = branch,
            clientMessageId = clientMessageId,
            role = "user",
            content = content,
            status = "queued",
            createdAt = now
        )
        insertMessage(message)
        replaceDraft(draft.copy(text = "", editRevision = expectedRevision + 1L, updatedAt = now))
        val title = content.replace(Regex("\\s+"), " ").take(42)
        if (conversation.title == "Neues Gespräch") setInitialTitle(conversationId, title)
        markActive(conversationId, now)
        return message
    }

    /**
     * Exactly one deterministic mock reply for a queued message. If this is
     * retried after an interruption, the existing reply is returned unchanged.
     */
    @Transaction
    open suspend fun completeMockReply(messageId: String, reply: String, now: Long): MessageEntity? {
        val user = getMessage(messageId) ?: return null
        if (user.role != "user") return null
        val replyClientId = "mock:${user.clientMessageId}"
        getByClientId(replyClientId)?.let { return it }
        if (user.status != "queued") return null
        val assistant = MessageEntity(
            id = replyClientId,
            conversationId = user.conversationId,
            branchId = user.branchId,
            clientMessageId = replyClientId,
            role = "assistant",
            content = reply,
            status = "complete",
            createdAt = now
        )
        insertMessage(assistant)
        transitionUserStatus(user.id, "queued", "sent")
        markActive(user.conversationId, now)
        return assistant
    }

    @Transaction
    open suspend fun prepareMockRetry(messageId: String): MessageEntity? {
        val original = getMessage(messageId) ?: return null
        if (original.role != "user" || getByClientId("mock:${original.clientMessageId}") != null) {
            return null
        }
        if (original.status == "failed") {
            transitionUserStatus(messageId, "failed", "queued")
        } else if (original.status != "queued") {
            return null
        }
        return getMessage(messageId)
    }

    @Transaction
    open suspend fun failMockReply(messageId: String) {
        transitionUserStatus(messageId, "queued", "failed")
    }
}
