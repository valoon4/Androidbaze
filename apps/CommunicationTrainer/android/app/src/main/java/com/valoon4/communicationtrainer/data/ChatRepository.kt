package com.valoon4.communicationtrainer.data

import android.content.Context
import java.util.UUID
import kotlinx.coroutines.flow.Flow

class ChatRepository(context: Context) {
    private val dao = ChatDatabase.get(context).chatDao()
    val preferences = AppPreferences(context)

    val conversations: Flow<List<ConversationEntity>> = dao.observeConversations()

    suspend fun newConversation(title: String = "Neues Gespräch"): String {
        val id = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        dao.createWithMainBranch(
            ConversationEntity(
                id = id,
                title = title,
                createdAt = timestamp,
                updatedAt = timestamp
            )
        )
        return id
    }

    suspend fun getConversation(id: String): ConversationEntity? = dao.getConversation(id)

    suspend fun getDraft(id: String): DraftEntity? = dao.getDraft(id, rootBranchId(id))

    suspend fun saveDraft(id: String, text: String, revision: Long) {
        dao.saveIfNewer(
            DraftEntity(
                conversationId = id,
                branchId = rootBranchId(id),
                text = text,
                editRevision = revision,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteConversation(id: String) {
        dao.deleteConversation(id)
    }
}
