package com.betbuddy.services

import com.betbuddy.data.{Player, StatRepository}

class BattingStatsService (repository: StatRepository) {

  def calculateTripleSlash(player: Player): String = {
    val average: Double = player.hits / player.atBats
    val onBasePercentage: Double = (player.hits + player.walks + player.hitByPitches) / (player.atBats + player.walks + player.hitByPitches + player.sacFlies)
    val slugging: Double = (player.hits + player.doubles + 2 * player.triples + 3 * player.homeruns) / player.atBats
    val ops = onBasePercentage + slugging
    "%.3f/%.3f/%.3f/%.3f" format (average, onBasePercentage, slugging, ops)
  }
}
