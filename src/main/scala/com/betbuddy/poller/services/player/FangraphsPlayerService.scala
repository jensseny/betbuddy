package com.betbuddy.poller.services.player

import com.betbuddy.data.StatRepository
import com.betbuddy.http.HttpClient
import com.betbuddy.util.Parser
import scala.collection.immutable.Range
import org.w3c.dom.{NodeList, Node}

case class FangraphsBatter (name: String, average: Double, onBasePercentage: Double, slugging: Double, ops: Double,
                            woba: Double, wrc: Double, war: Double)

case class FangraphsPitcher (name: String, era: Double, fip: Double, xFip: Double, war: Double)

case class FangraphsTeam (batters: List[FangraphsBatter], pitchers: List[FangraphsPitcher])

class FangraphsPlayerService (network: HttpClient, repository: StatRepository) {

  val teamNameMap = Map(("min" -> "8"))

  def teamBattingStatUrl(teamCode: String, year: Int): String = "http://www.fangraphs.com/leaders.aspx?pos=all&stats=bat&lg=all&qual=0&type=8&season=%s&month=0&season1=%s&ind=0&team=%s&rost=0&age=0&filter=&players=0" format (year, year, teamCode)
  def teamPitchingStatUrl(teamCode: String, year: Int): String = "http://www.fangraphs.com/leaders.aspx?pos=all&stats=pit&lg=all&qual=0&type=8&season=%s&month=0&season1=%s&ind=0&team=%s&rost=0&age=0&filter=&players=0" format (year, year, teamCode)

  def getTeamData(teamName: String, year: Int): FangraphsTeam = {
    val teamCode: String = teamNameMap.getOrElse(teamName, "1")
    val battingResponse = network.get(teamBattingStatUrl(teamCode, year)).response
    val battingNodeList = getPlayerNodesFromHtml(battingResponse)
    val pitchingResponse = network.get(teamPitchingStatUrl(teamCode, year)).response
    val pitchingNodeList = getPlayerNodesFromHtml(pitchingResponse)

    val batterList = (for {
      i <- Range(0, battingNodeList.getLength)
      player = getBatterFromNode(battingNodeList.item(i))
    } yield player).toList
    println(batterList)
    val pitcherList = (for {
      i <- Range(0, pitchingNodeList.getLength)
      player = getPitcherFromNode(pitchingNodeList.item(i))
    } yield player).toList
    FangraphsTeam(batterList, pitcherList)
  }

  private def getPlayerNodesFromHtml(response: String): NodeList = {
    val document = Parser.tidyHTML(response)
    document.getDocumentElement.normalize()
    document.getDocumentElement.getChildNodes.item(1)
      .getChildNodes.item(0)
      .getChildNodes.item(42)
      .getChildNodes.item(1)
      .getChildNodes.item(18)
      .getChildNodes.item(0)
      .getChildNodes.item(2)
      .getChildNodes
  }

  private def getBatterFromNode(node: Node): FangraphsBatter = {
    val nodeList = node.getChildNodes
    FangraphsBatter(nodeList.item(1).getFirstChild.getFirstChild.getNodeValue,
      nodeList.item(12).getFirstChild.getNodeValue.toDouble,
      nodeList.item(13).getFirstChild.getNodeValue.toDouble,
      nodeList.item(14).getFirstChild.getNodeValue.toDouble,
      nodeList.item(15).getFirstChild.getNodeValue.toDouble,
      nodeList.item(16).getFirstChild.getNodeValue.toDouble,
      nodeList.item(17).getFirstChild.getNodeValue.toDouble,
      nodeList.item(20).getFirstChild.getNodeValue.toDouble)
  }

  private def getPitcherFromNode(node: Node): FangraphsPitcher = {
    val nodeList = node.getChildNodes
    FangraphsPitcher(nodeList.item(1).getFirstChild.getFirstChild.getNodeValue,
      nodeList.item(15).getFirstChild.getNodeValue.toDouble,
      nodeList.item(16).getFirstChild.getNodeValue.toDouble,
      nodeList.item(17).getFirstChild.getNodeValue.toDouble,
      nodeList.item(18).getFirstChild.getNodeValue.toDouble)
  }

  private def printChildNodes(node: Node, padding: String = "", depth: Int = 1) {
    if (!node.hasChildNodes) {
      println(padding + depth + " " + node.getNodeName + " / " + node.getNodeValue)
    } else {
      val nodeList = node.getChildNodes
      for (idx <- Range(0, nodeList.getLength)) {
        val node = nodeList.item(idx)
        println(padding + depth + " " + node.getNodeValue)
        if (node.hasChildNodes) printChildNodes(node, padding + "  ", depth + 1)
      }
    }
  }
}

