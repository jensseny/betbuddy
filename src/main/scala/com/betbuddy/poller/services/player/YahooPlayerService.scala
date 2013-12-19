package com.betbuddy.poller.services.player

import com.betbuddy.data.StatRepository
import com.betbuddy.http.HttpClient
import com.betbuddy.util.Parser
import scala.collection.immutable.Range
import org.w3c.dom.{NodeList, Node}

// NOTE: These classes contain additional fields to what Yahoo returns.  These are just reserving fields to store later
// stats derived from the Yahoo ones, such as xFIP.  This simplifies working with the class and the DB.
case class YahooBatter(name: String, atBats: Double, runs: Double, hits: Double, doubles: Double, triples: Double,
                       homeRuns: Double, rbis: Double, walks: Double, strikeouts: Double,
                       average: Double, onBasePercentage: Double, slugging: Double, ops: Double)

case class YahooPitcher(name: String, ip: Double, hits: Double, runs: Double, earnedRuns: Double, homeRuns: Double,
                        walks: Double, strikeouts: Double, era: Double, whip: Double, battingAverageAgainst: Double,
                        fip: Double, xFip: Double)

case class YahooTeam(batters: List[YahooBatter], pitchers: List[YahooPitcher])

class YahooPlayerService (network: HttpClient, repository: StatRepository) {

  def teamBattingStatUrl(teamCode: String, year: Int): String = "http://sports.yahoo.com/mlb/teams/%s/stats?season=%s_2&stat_category=mlb.stat_category.1&cut_type=&qualified=0" format (teamCode, year)
  def teamPitchingStatUrl(teamCode: String, year: Int): String = "http://sports.yahoo.com/mlb/teams/%s/stats?season=%s_2&stat_category=mlb.stat_category.2&cut_type=&qualified=0" format (teamCode, year)

  def getTeamData(teamCode: String, year: Int): YahooTeam = {
    val battingResponse = network.get(teamBattingStatUrl(teamCode, year)).response
    val battingNodeList = getPlayerNodesFromHtml(battingResponse)
    val pitchingResponse = network.get(teamPitchingStatUrl(teamCode, year)).response
    val pitchingNodeList = getPlayerNodesFromHtml(pitchingResponse)

    val batterList = (for {
      i <- Range(0, battingNodeList.getLength)
      player = getBatterFromNode(battingNodeList.item(i))
    } yield player).toList
    val pitcherList = (for {
      i <- Range(0, pitchingNodeList.getLength)
      player = getPitcherFromNode(pitchingNodeList.item(i))
    } yield player).toList
    YahooTeam(batterList, pitcherList)
  }

  private def getPlayerNodesFromHtml(response: String): NodeList = {
    val document = Parser.tidyHTML(response)
    document.getDocumentElement.normalize()
    document.getDocumentElement.getChildNodes.item(1)
      .getChildNodes.item(2)
      .getChildNodes.item(1)
      .getChildNodes.item(12)
      .getChildNodes.item(2)
      .getChildNodes.item(1)
      .getFirstChild
      .getFirstChild
      .getChildNodes.item(3)
      .getChildNodes
  }

  private def getBatterFromNode(node: Node): YahooBatter = {
    val nodeList = node.getChildNodes
    YahooBatter(nodeList.item(0).getFirstChild.getFirstChild.getNodeValue,
      nodeList.item(2).getFirstChild.getNodeValue.toDouble,
      nodeList.item(3).getFirstChild.getNodeValue.toDouble,
      nodeList.item(4).getFirstChild.getNodeValue.toDouble,
      nodeList.item(5).getFirstChild.getNodeValue.toDouble,
      nodeList.item(6).getFirstChild.getNodeValue.toDouble,
      nodeList.item(7).getFirstChild.getNodeValue.toDouble,
      nodeList.item(8).getFirstChild.getNodeValue.toDouble,
      nodeList.item(9).getFirstChild.getNodeValue.toDouble,
      nodeList.item(10).getFirstChild.getNodeValue.toDouble,
      nodeList.item(11).getFirstChild.getNodeValue.toDouble,
      nodeList.item(14).getFirstChild.getNodeValue.toDouble,
      nodeList.item(15).getFirstChild.getNodeValue.toDouble,
      nodeList.item(16).getFirstChild.getNodeValue.toDouble)
  }

  private def getPitcherFromNode(node: Node): YahooPitcher = {
    val nodeList = node.getChildNodes
    YahooPitcher(nodeList.item(0).getFirstChild.getFirstChild.getNodeValue,
      nodeList.item(10).getFirstChild.getNodeValue.toDouble,
      nodeList.item(11).getFirstChild.getNodeValue.toDouble,
      nodeList.item(12).getFirstChild.getNodeValue.toDouble,
      nodeList.item(13).getFirstChild.getNodeValue.toDouble,
      nodeList.item(14).getFirstChild.getNodeValue.toDouble,
      nodeList.item(15).getFirstChild.getNodeValue.toDouble,
      nodeList.item(16).getFirstChild.getNodeValue.toDouble,
      nodeList.item(17).getFirstChild.getNodeValue.toDouble,
      nodeList.item(18).getFirstChild.getNodeValue.toDouble,
      nodeList.item(19).getFirstChild.getNodeValue.toDouble,
      0.0, 0.0)
  }
}

