package com.valoon4.kindhell.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject

data class GuildReply(val author: String, val message: String)
data class GuildPost(
    val id: Long,
    val author: String,
    val message: String,
    val replies: List<GuildReply> = emptyList(),
    val isDemo: Boolean = false
)

data class GuildMember(
    val alias: String,
    val intro: String,
    val interests: List<String>,
    val contact: String,
    val language: String
)

val sampleMembers = listOf(
    GuildMember("Lumi", "Sammelt Geschichten und sucht Leute für nächtliche Gespräche.", listOf("Anime", "Bücher"), "Freundschaft", "Deutsch"),
    GuildMember("Mika", "Co-op-Games, spontane Ideen und viel Quatsch.", listOf("Gaming", "Musik"), "Austausch", "Deutsch"),
    GuildMember("Nova", "Kunst, Cosplay und neugierig auf andere Perspektiven.", listOf("Kunst", "Anime"), "Freundschaft", "Englisch"),
    GuildMember("Kira", "Ich liebe Soundtracks und seltsame Internet-Nostalgie.", listOf("Musik", "Gaming"), "Austausch", "Deutsch"),
    GuildMember("Robin", "Hier für gute Gespräche, auch ohne gemeinsames Hobby.", listOf("Bücher", "Technik"), "Freundschaft", "Deutsch"),
    GuildMember("Aki", "Zeichne gern und entdecke jede Woche etwas Neues.", listOf("Kunst", "Technik"), "Austausch", "Englisch")
)

/**
 * Purely local v0.0.1 demonstration store. No login, server, tracking or transmitted content.
 * Demo entries are always labeled as demo in the UI; real community is not simulated.
 */
class LocalGuildStore(context: Context) {
    private val prefs = context.getSharedPreferences("kind_hell_local_demo", Context.MODE_PRIVATE)

    var alias by mutableStateOf(prefs.getString("alias", "Wanderer") ?: "Wanderer")
        private set
    var introduction by mutableStateOf(prefs.getString("introduction", "") ?: "")
        private set

    val posts = mutableStateListOf<GuildPost>().apply { addAll(loadPosts()) }
    val bookmarkedMembers = mutableStateListOf<String>().apply {
        val values = runCatching { JSONArray(prefs.getString("bookmarks", "[]")) }.getOrDefault(JSONArray())
        for (index in 0 until values.length()) {
            add(values.optString(index))
        }
    }

    fun updateAlias(value: String) {
        val clean = value.trim().take(24)
        if (clean.isBlank()) return
        alias = clean
        prefs.edit().putString("alias", clean).apply()
    }

    fun updateIntroduction(value: String) {
        introduction = value.trim().take(260)
        prefs.edit().putString("introduction", introduction).apply()
    }

    fun addPost(value: String) {
        val clean = value.trim().take(400)
        if (clean.isBlank()) return
        posts.add(0, GuildPost(System.currentTimeMillis(), alias, clean))
        savePosts()
    }

    fun addReply(postId: Long, value: String) {
        val clean = value.trim().take(250)
        if (clean.isBlank()) return
        val index = posts.indexOfFirst { it.id == postId }
        if (index < 0) return
        posts[index] = posts[index].copy(
            replies = posts[index].replies + GuildReply(alias, clean)
        )
        savePosts()
    }

    fun toggleBookmark(memberAlias: String) {
        if (bookmarkedMembers.contains(memberAlias)) bookmarkedMembers.remove(memberAlias)
        else bookmarkedMembers.add(memberAlias)
        val values = JSONArray()
        bookmarkedMembers.forEach { values.put(it) }
        prefs.edit().putString("bookmarks", values.toString()).apply()
    }

    private fun loadPosts(): List<GuildPost> {
        val raw = prefs.getString("posts", null) ?: return listOf(
            GuildPost(1L, "Lumi", "Was hat euch heute unerwartet zum Lächeln gebracht?", listOf(GuildReply("Mika", "Ein völlig absurdes Gespräch über Drachen.")), true),
            GuildPost(2L, "Kira", "Kennt ihr noch diese Foren, in denen man einfach drauflosreden konnte?", listOf(GuildReply("Nova", "Genau dieses Gefühl vermisse ich!")), true),
            GuildPost(3L, "Robin", "Kein Thema nötig. Wie geht's euch gerade?", isDemo = true)
        )
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { index ->
                val post = array.getJSONObject(index)
                val replies = post.optJSONArray("replies") ?: JSONArray()
                GuildPost(
                    id = post.getLong("id"),
                    author = post.optString("author"),
                    message = post.optString("message"),
                    replies = (0 until replies.length()).map { replyIndex ->
                        val reply = replies.getJSONObject(replyIndex)
                        GuildReply(reply.optString("author"), reply.optString("message"))
                    },
                    isDemo = post.optBoolean("demo", false)
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun savePosts() {
        val array = JSONArray()
        posts.forEach { post ->
            val replies = JSONArray()
            post.replies.forEach { reply ->
                replies.put(JSONObject().put("author", reply.author).put("message", reply.message))
            }
            array.put(
                JSONObject()
                    .put("id", post.id)
                    .put("author", post.author)
                    .put("message", post.message)
                    .put("demo", post.isDemo)
                    .put("replies", replies)
            )
        }
        prefs.edit().putString("posts", array.toString()).apply()
    }
}
