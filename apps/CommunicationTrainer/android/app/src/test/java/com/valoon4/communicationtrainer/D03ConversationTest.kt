package com.valoon4.communicationtrainer

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.valoon4.communicationtrainer.data.ChatDatabase
import com.valoon4.communicationtrainer.data.ConversationEntity
import com.valoon4.communicationtrainer.data.DraftEntity
import com.valoon4.communicationtrainer.data.MockConversationEngine
import com.valoon4.communicationtrainer.data.rootBranchId
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class D03ConversationTest {
    private lateinit var context: Context
    private lateinit var db: ChatDatabase
    private val dbName = "d03-" + UUID.randomUUID() + ".db"

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(dbName)
        db = open()
    }

    @After
    fun tearDown() {
        db.close()
        context.deleteDatabase(dbName)
    }

    private fun open(): ChatDatabase = Room.databaseBuilder(
        context, ChatDatabase::class.java, dbName
    ).build()

    private suspend fun create(id: String, draft: String = "Hallo") {
        db.chatDao().createWithMainBranch(
            ConversationEntity(id = id, title = "Neues Gespräch", createdAt = 10, updatedAt = 10)
        )
        db.chatDao().saveIfNewer(DraftEntity(id, rootBranchId(id), draft, 1, 11))
    }

    @Test
    fun sendingIsAtomicAndDraftIsClearedOnlyAfterInsertion() = runBlocking {
        create("a", " Hallo!  ")
        val result = db.chatDao().enqueueSavedDraft("a", 1, "client-one", 20)
        assertNotNull(result)
        assertEquals("Hallo!", result?.content)
        assertEquals("queued", result?.status)
        assertEquals("user", result?.role)
        assertEquals("", db.chatDao().getDraft("a", rootBranchId("a"))?.text)
        assertEquals(2L, db.chatDao().getDraft("a", rootBranchId("a"))?.editRevision)
        assertEquals("Hallo!", db.chatDao().getConversation("a")?.title)
        assertEquals(1, db.chatDao().countMessages("a"))
    }

    @Test
    fun duplicateClientMessageIdDoesNotSendTwice() = runBlocking {
        create("a", "Meine erste Nachricht")
        val one = db.chatDao().enqueueSavedDraft("a", 1, "stable-client-id", 20)
        val again = db.chatDao().enqueueSavedDraft("a", 1, "stable-client-id", 21)
        assertEquals(one, again)
        assertEquals(1, db.chatDao().countMessages("a"))
    }

    @Test
    fun unsavedOrBlankDraftCannotBeSentAndRemainsIntact() = runBlocking {
        create("a", "Noch nicht")
        assertNull(db.chatDao().enqueueSavedDraft("a", 2, "wrong-revision", 20))
        assertEquals("Noch nicht", db.chatDao().getDraft("a", rootBranchId("a"))?.text)
        create("b", "   ")
        assertNull(db.chatDao().enqueueSavedDraft("b", 1, "blank-draft", 20))
        assertEquals(0, db.chatDao().countMessages("b"))
    }

    @Test
    fun fixedResponseCreatedExactlyOnceEvenOnRepeatedCompletion() = runBlocking {
        create("a", "Hallo")
        val user = db.chatDao().enqueueSavedDraft("a", 1, "stable-uuid", 20)!!
        val answer = MockConversationEngine.answer(user.content)
        val first = db.chatDao().completeMockReply(user.id, answer, 30)
        val repeated = db.chatDao().completeMockReply(user.id, answer, 40)
        assertEquals(first, repeated)
        assertEquals("assistant", first?.role)
        assertEquals("complete", first?.status)
        assertEquals(2, db.chatDao().countMessages("a"))
        assertEquals("sent", db.chatDao().getMessage(user.id)?.status)
        assertNull(db.chatDao().prepareMockRetry(user.id))
    }

    @Test
    fun failureCanBeRetriedWithoutResendingTheUserMessage() = runBlocking {
        create("a", "Wie geht das?")
        val user = db.chatDao().enqueueSavedDraft("a", 1, "stable", 20)!!
        db.chatDao().failMockReply(user.id)
        assertEquals("failed", db.chatDao().getMessage(user.id)?.status)
        assertEquals(1, db.chatDao().countMessages("a"))
        val retry = db.chatDao().prepareMockRetry(user.id)
        assertEquals(user.id, retry?.id)
        assertEquals("queued", retry?.status)
        db.chatDao().completeMockReply(user.id, MockConversationEngine.answer(user.content), 60)
        assertEquals(2, db.chatDao().countMessages("a"))
        assertEquals("sent", db.chatDao().getMessage(user.id)?.status)
    }

    @Test
    fun interruptedQueuedMessageIsStillRecoverableAfterDatabaseReopen() = runBlocking {
        create("a", "Unfertiger Lauf")
        val user = db.chatDao().enqueueSavedDraft("a", 1, "restart", 20)!!
        db.close()
        db = open()
        assertEquals("queued", db.chatDao().getMessage(user.id)?.status)
        assertNotNull(db.chatDao().prepareMockRetry(user.id))
        db.chatDao().completeMockReply(user.id, MockConversationEngine.answer(user.content), 50)
        assertEquals(2, db.chatDao().observeMessages("a", rootBranchId("a")).first().size)
    }

    @Test
    fun deletingConversationAlsoDeletesActualChatHistory() = runBlocking {
        create("a", "A")
        create("b", "B")
        val user = db.chatDao().enqueueSavedDraft("a", 1, "a-message", 20)!!
        db.chatDao().completeMockReply(user.id, "Antwort", 30)
        db.chatDao().enqueueSavedDraft("b", 1, "b-message", 20)
        assertEquals(1, db.chatDao().deleteConversation("a"))
        assertEquals(0, db.chatDao().countMessages("a"))
        assertEquals(1, db.chatDao().countMessages("b"))
        assertNull(db.chatDao().getConversation("a"))
        assertNotNull(db.chatDao().getConversation("b"))
    }

    @Test
    fun mockAnswerIsDeterministicAndNotAnAiRequest() {
        assertEquals(MockConversationEngine.answer("Hallo"), MockConversationEngine.answer("Hallo"))
        assertTrue(MockConversationEngine.answer("Termin?").isNotBlank())
        assertTrue(MockConversationEngine.answer("").isNotBlank())
    }
}
