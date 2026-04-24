package com.example.fishial_recog

import android.content.Context

data class FishInfo(
    val species: String,
    val lures: String,
    val conditions: String,
    val bestTime: String,
    val bestSeason: String
)

object FishDataHelper {

    private var fishMap: Map<String, FishInfo> = emptyMap()

    fun load(context: Context) {
        val map = mutableMapOf<String, FishInfo>()
        context.assets.open("fish_data.csv").bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line ->
                val parts = line.split(",")
                if (parts.size >= 5) {
                    val species = parts[0].trim()
                    map[species] = FishInfo(
                        species = species,
                        lures = parts[1].trim(),
                        conditions = parts[2].trim(),
                        bestTime = parts[3].trim(),
                        bestSeason = parts[4].trim()
                    )
                }
            }
        }
        fishMap = map
    }

    fun getInfo(species: String): FishInfo {
        return fishMap[species] ?: FishInfo(
            species = species,
            lures = "Check local guides",
            conditions = "Varies by season",
            bestTime = "Early morning, Late evening",
            bestSeason = "Spring, Fall"
        )
    }
}