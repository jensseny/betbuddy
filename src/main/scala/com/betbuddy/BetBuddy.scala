package com.betbuddy

import grizzled.slf4j.Logging
import com.betbuddy.http.HttpClient
import com.betbuddy.sites.{GameLine, FiveDimes}
import com.typesafe.config.{ConfigFactory, Config}
import com.betbuddy.config.MoneyballConfig
import com.betbuddy.util.JDBCHelpers
import com.betbuddy.data.{Player, StatRepository}
import com.betbuddy.services.BattingStatsService
import com.betbuddy.poller.data.YahooPlayerRepository
import com.betbuddy.poller.services.player.FangraphsPlayerService
import com.betbuddy.poller.services.team.FangraphsTeamService

object BetBuddy extends App with Logging {
  logger.info("Testing...")

  logger.info("Parsing config...")
  logger.info("Loading config...")
  val moneyballConfig = ConfigFactory.load()

  logger.info("Config from %s" format moneyballConfig.root())
  val config = MoneyballConfig(moneyballConfig)

  logger.info("Initializing database...")
  val dataSource = JDBCHelpers.createDataSource(config.database)

  logger.info("Initializing HTTP client...")
  val client = new HttpClient

  logger.info("Initializing repository...")
  val statRepository = new StatRepository(dataSource)
  val pollerRepository = new YahooPlayerRepository(dataSource)

  logger.info("Initializing services...")
  val battingStateService = new BattingStatsService(statRepository)
  val pollingService = new FangraphsPlayerService(client, statRepository)
  val teamStandingsService = new FangraphsTeamService(client, statRepository)

  println (teamStandingsService.getStandingsData)

//  val fiveDimes = new FiveDimes(config.fiveDimes, client)
//  val moneyLines = fiveDimes.getLines().filter(line => line.away.moneyLine.isDefined)
//  println(moneyLines)

  //if (!loginResult) throw new Exception("Login failed!")

  //val games = fiveDimes.getGames
  //println("Games: " + games)
}


