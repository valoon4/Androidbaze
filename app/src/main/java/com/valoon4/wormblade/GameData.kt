package com.valoon4.wormblade

import android.graphics.Color

data class LevelConfig(
    val id: Int,
    val name: String,
    val subtitle: String,
    val waves: Int,
    val wormsPerWave: Int,
    val segmentBase: Int,
    val baseHp: Int,
    val baseSpeed: Float,
    val amplitude: Float,
    val chestEvery: Int,
    val topColor: Int,
    val bottomColor: Int,
    val edgeColor: Int,
    val accentColor: Int,
    val wormLight: Int,
    val wormDark: Int,
    val boss: Boolean = false
)

data class UpgradeOption(
    val id: String,
    val title: String,
    val description: String,
    val maxStacks: Int
)

object GameContent {
    val levels = listOf(
        LevelConfig(1, "Sunny Trail", "Warm-up swarm", 3, 3, 10, 90, 158f, 0.035f, 7,
            Color.rgb(224, 177, 82), Color.rgb(242, 199, 104), Color.rgb(75, 148, 63), Color.rgb(70, 205, 242),
            Color.rgb(255, 226, 92), Color.rgb(239, 160, 31)),
        LevelConfig(2, "Honey Hollow", "Faster golden chains", 3, 4, 11, 120, 172f, 0.045f, 7,
            Color.rgb(210, 156, 70), Color.rgb(236, 187, 94), Color.rgb(90, 139, 55), Color.rgb(255, 194, 60),
            Color.rgb(255, 214, 74), Color.rgb(229, 137, 28)),
        LevelConfig(3, "Rune Garden", "Wide serpent curves", 4, 4, 12, 160, 180f, 0.060f, 6,
            Color.rgb(177, 158, 88), Color.rgb(219, 191, 111), Color.rgb(55, 126, 83), Color.rgb(75, 231, 255),
            Color.rgb(245, 220, 104), Color.rgb(197, 154, 45)),
        LevelConfig(4, "Coral Canyon", "Dense split lanes", 4, 5, 12, 205, 188f, 0.050f, 6,
            Color.rgb(214, 132, 83), Color.rgb(232, 168, 112), Color.rgb(86, 121, 70), Color.rgb(255, 117, 113),
            Color.rgb(255, 196, 86), Color.rgb(224, 121, 55)),
        LevelConfig(5, "Crown Burrow", "First royal worm", 4, 4, 13, 250, 190f, 0.060f, 5,
            Color.rgb(191, 143, 72), Color.rgb(224, 178, 87), Color.rgb(66, 121, 65), Color.rgb(255, 217, 70),
            Color.rgb(255, 220, 81), Color.rgb(211, 128, 28), boss = true),
        LevelConfig(6, "Crystal Creek", "Hard shells, rich loot", 4, 5, 14, 305, 198f, 0.068f, 5,
            Color.rgb(117, 160, 160), Color.rgb(160, 194, 177), Color.rgb(48, 112, 92), Color.rgb(82, 229, 255),
            Color.rgb(239, 224, 130), Color.rgb(158, 171, 87)),
        LevelConfig(7, "Moonlit Ruins", "Long night crawlers", 5, 5, 15, 365, 205f, 0.075f, 5,
            Color.rgb(87, 91, 141), Color.rgb(122, 116, 158), Color.rgb(42, 84, 79), Color.rgb(143, 139, 255),
            Color.rgb(225, 213, 140), Color.rgb(145, 127, 82)),
        LevelConfig(8, "Ember Orchard", "Hot, fast, crowded", 5, 6, 15, 430, 217f, 0.070f, 5,
            Color.rgb(182, 95, 67), Color.rgb(217, 128, 76), Color.rgb(91, 99, 57), Color.rgb(255, 139, 62),
            Color.rgb(255, 187, 74), Color.rgb(204, 96, 34)),
        LevelConfig(9, "Storm Ridge", "Aggressive zig-zags", 5, 6, 16, 510, 228f, 0.090f, 4,
            Color.rgb(79, 111, 138), Color.rgb(116, 145, 157), Color.rgb(49, 91, 78), Color.rgb(106, 219, 255),
            Color.rgb(235, 219, 113), Color.rgb(158, 143, 63)),
        LevelConfig(10, "Golden Queen", "Royal swarm finale", 5, 6, 17, 620, 232f, 0.095f, 4,
            Color.rgb(111, 72, 110), Color.rgb(164, 103, 117), Color.rgb(55, 75, 69), Color.rgb(255, 221, 74),
            Color.rgb(255, 227, 93), Color.rgb(210, 124, 33), boss = true)
    )

    val upgrades = listOf(
        UpgradeOption("multishot", "Twin Bolt", "+2 projectiles per volley", 4),
        UpgradeOption("damage", "Heavy Core", "+40% projectile damage", 5),
        UpgradeOption("firerate", "Rapid Loader", "+25% fire rate", 5),
        UpgradeOption("pierce", "Piercing Tip", "+1 enemy segment pierced", 4),
        UpgradeOption("crit", "Critical Spark", "+12% critical chance", 5),
        UpgradeOption("bladeCount", "Extra Blade", "+1 orbiting blade", 4),
        UpgradeOption("bladeDamage", "Razor Drive", "+35% blade damage", 5),
        UpgradeOption("bladeReach", "Wide Orbit", "+18% orbit reach and blade size", 4),
        UpgradeOption("armor", "Guardian Plate", "Take 15% less contact damage", 4),
        UpgradeOption("health", "Heart Tank", "+220 max HP and heal 260", 4),
        UpgradeOption("magnet", "Magnet Field", "+45% pickup range", 4),
        UpgradeOption("slow", "Frost Rounds", "Worms move 10% slower", 4),
        UpgradeOption("overdrive", "Overdrive", "Every 8th volley fires double", 1),
        UpgradeOption("lucky", "Treasure Sense", "Small chance for extra chest segments", 3)
    )
}
