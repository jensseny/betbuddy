package com.betbuddy.poller

import com.betbuddy.poller.services.player.{YahooPitcher, YahooPlayerService}
import com.betbuddy.poller.services.team.FangraphsTeamService

class Poller (playerService: YahooPlayerService, teamService: FangraphsTeamService) {

  val teamList = Seq("min")
  val yearList = Seq(2013)

  def start {
    for (team <- teamList) {
      for (year <- yearList) {
        val teamStats = playerService.getTeamData(team, year)
        for (player <- teamStats.pitchers) yield setPitchingStats(player)
      }
    }
  }

  private def setPitchingStats(player: YahooPitcher): YahooPitcher = {
    player
  }
}
