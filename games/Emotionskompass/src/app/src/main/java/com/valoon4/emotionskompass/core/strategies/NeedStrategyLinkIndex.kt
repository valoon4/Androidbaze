package com.valoon4.emotionskompass.core.strategies

import com.valoon4.emotionskompass.core.model.HumanNeed
import com.valoon4.emotionskompass.core.model.NeedCatalog
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.int

/** One editorially reasoned *possibility*, not a personalized recommendation. */
data class NeedStrategyAssociation(
    val needId: String,
    val strategyId: String,
    val reason: String
)

data class NeedStrategyOption(val strategy: StrategyCard, val reason: String)
data class StrategyNeedOption(val need: HumanNeed, val reason: String)

/**
 * Manually curated educational cross-links. Deliberately excludes some needs
 * when no sufficiently clear strategy exists in the current 30-card atlas.
 *
 * No lookups depend on diary entries, current emotions, user-selected needs,
 * inferred traits, network services or hidden user data.
 */
class NeedStrategyLinkIndex private constructor(
    val associations: List<NeedStrategyAssociation>
) {
    private val byNeed: Map<String, List<NeedStrategyAssociation>> =
        associations.groupBy { it.needId }
    private val byStrategy: Map<String, List<NeedStrategyAssociation>> =
        associations.groupBy { it.strategyId }

    val linkedNeedIds: Set<String> get() = byNeed.keys

    fun optionsForNeed(needId: String, strategies: StrategyCatalog): List<NeedStrategyOption> =
        byNeed[needId].orEmpty().mapNotNull { link ->
            strategies.find(link.strategyId)?.let { NeedStrategyOption(it, link.reason) }
        }

    fun needsForStrategy(strategyId: String, needs: NeedCatalog): List<StrategyNeedOption> =
        byStrategy[strategyId].orEmpty().mapNotNull { link ->
            needs.find(link.needId)?.let { StrategyNeedOption(it, link.reason) }
        }

    companion object {
        /**
         * Fail the *additional* index closed if even one shard has a broken
         * reference. Core atlases and encrypted diary remain independent.
         */
        fun fromJsonParts(
            shards: List<String>,
            needs: NeedCatalog,
            strategies: StrategyCatalog
        ): NeedStrategyLinkIndex {
            require(shards.isNotEmpty() && shards.size <= 12)
            val knownNeedIds = needs.needs.map { it.id }.toSet()
            val links = shards.flatMap { raw ->
                val root = Json.parseToJsonElement(raw).jsonObject
                require(root.keys == setOf("schemaVersion", "locale", "links"))
                require(root.getValue("schemaVersion").jsonPrimitive.int == 1)
                require(root.getValue("locale").jsonPrimitive.content == "de")
                root.getValue("links").jsonArray.flatMap { entry ->
                    val item = entry.jsonObject
                    require(item.keys == setOf("needId", "items"))
                    val needId = item.getValue("needId").jsonPrimitive.content
                    require(needId in knownNeedIds)
                    val strategiesForNeed = item.getValue("items").jsonArray
                    require(strategiesForNeed.size in 1..3)
                    strategiesForNeed.map { link ->
                        val content = link.jsonObject
                        require(content.keys == setOf("strategyId", "reason"))
                        val strategyId = content.getValue("strategyId").jsonPrimitive.content
                        val reason = content.getValue("reason").jsonPrimitive.content
                        val strategy = strategies.find(strategyId)
                        require(strategy != null && "patterns" !in strategy.familyIds)
                        require(reason == reason.trim() && reason.length in 55..300)
                        NeedStrategyAssociation(needId, strategyId, reason)
                    }.also { options ->
                        require(options.map { it.strategyId }.distinct().size == options.size)
                    }
                }
            }
            require(links.isNotEmpty() && links.size <= 246)
            require(links.map { it.needId to it.strategyId }.distinct().size == links.size)
            // A need must be defined in exactly one editorial shard, even if it
            // has more than one possible path.
            val grouped = links.groupBy { it.needId }
            require(grouped.all { (_, values) -> values.size in 1..3 })
            return NeedStrategyLinkIndex(links)
        }
    }
}
