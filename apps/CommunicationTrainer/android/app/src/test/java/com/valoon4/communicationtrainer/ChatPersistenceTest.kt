package com.valoon4.communicationtrainer

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.valoon4.communicationtrainer.data.ChatDatabase
import com.valoon4.communicationtrainer.data.ConversationEntity
import com.valoon4.communicationtrainer.data.DraftEntity
import com.valoon4.communicationtrainer.data.MessageEntity
import com.valoon4.communicationtrainer.data.ReplayBranchEntity
import com.valoon4.communicationtrainer.data.rootBranchId
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
class ChatPersistenceTest {
    private lateinit var context: Context
    private lateinit var database: ChatDatabase
    private val filename = "d02-test-${java.util.UUID.randomUUID()}.db"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(filename)
        database = open()
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase(filename)
    }

    private fun open(): ChatDatabase = Room.databaseBuilder(
        context, ChatDatabase::class.java, filename
    ).build()

    private suspend fun create(id: String) {
        database.chatDao().createWithMainBranch(
            ConversationEntity(id = id, title = "Entwurf $id", createdAt = 100, updatedAt = 100)
        )
    }

    @Test
    fun committedDraftSurvivesCloseAndReopen() = runBlocking {
        create("one")
        database.chatDao().saveIfNewer(
            DraftEntity("one", rootBranchId("one"), "Meine Worte bleiben erhalten", 1, 101)
        )
        database.close()
        database = open()
        val draft = database.chatDao().getDraft("one", rootBranchId("one"))
        assertEquals("Meine Worte bleiben erhalten", draft?.text)
        assertEquals(1L, draft?.editRevision)
        assertEquals(1, database.chatDao().observeConversations().first().size)
    }

    @Test
    fun newerRevisionWinsEvenWhenOldWriteArrivesLate() = runBlocking {
        create("one")
        val branch = rootBranchId("one")
        database.chatDao().saveIfNewer(DraftEntity("one", branch, "Neue Version", 12, 120))
        database.chatDao().saveIfNewer(DraftEntity("one", branch, "Alte Version", 11, 110))
        assertEquals("Neue Version", database.chatDao().getDraft("one", branch)?.text)
        assertEquals(12L, database.chatDao().getDraft("one", branch)?.editRevision)
    }

    @Test
    fun conversationsAndDraftsStayIsolated() = runBlocking {
        create("one")
        create("two")
        database.chatDao().saveIfNewer(
            DraftEntity("one", rootBranchId("one"), "Privater Text", 1, 110)
        )
        assertNull(database.chatDao().getDraft("two", rootBranchId("two")))
        assertEquals("Privater Text", database.chatDao().getDraft("one", rootBranchId("one"))?.text)
    }

    @Test
    fun deleteCascadesDraftMessagesAndReplayBranches() = runBlocking {
        create("one")
        val branch = rootBranchId("one")
        database.chatDao().saveIfNewer(DraftEntity("one", branch, "Zu löschen", 1, 101))
        database.chatDao().insertMessage(
            MessageEntity("m1", "one", branch, "client-m1", "user", "Lokal", "complete", 102)
        )
        database.chatDao().insertBranch(
            ReplayBranchEntity("alternate", "one", branch, "m1", "snapshot", 103)
        )
        assertEquals(2, database.chatDao().countBranches("one"))
        assertEquals(1, database.chatDao().countMessages("one"))
        assertEquals(1, database.chatDao().deleteConversation("one"))
        assertNull(database.chatDao().getConversation("one"))
        assertNull(database.chatDao().getDraft("one", branch))
        assertEquals(0, database.chatDao().countBranches("one"))
        assertEquals(0, database.chatDao().countMessages("one"))
    }

    @Test
    fun differentConversationCannotReuseAnotherConversationsBranch() = runBlocking {
        create("one")
        create("two")
        val attempted = runCatching {
            database.chatDao().insertMessage(
                MessageEntity(
                    "wrong", "two", rootBranchId("one"), "request-wrong",
                    "user", "Soll scheitern", "queued", 100
                )
            )
        }
        assertTrue("A replay branch belongs to exactly one conversation", attempted.isFailure)
        assertEquals(0, database.chatDao().countMessages("two"))
    }

    @Test
    fun newlyCreatedDatabaseHasVersionedSchema() = runBlocking {
        create("one")
        assertEquals(1, database.openHelper.readableDatabase.version)
        assertNotNull(database.chatDao().getConversation("one"))
    }
}
