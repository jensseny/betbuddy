package com.betbuddy.poller.services.team

import com.betbuddy.data.StatRepository
import com.betbuddy.http.HttpClient
import com.betbuddy.util.Parser
import scala.collection.immutable.Range
import org.w3c.dom.{NodeList, Node}

case class FangraphsTeamStats(name: String, wins: Int, losses: Int, runsScoredPerGame: Double, runsAllowedPerGame: Double,
                                            projectedWins: Int, projectedLosses: Int, projectedRunsScoredPerGame: Double, projectedRunsAllowedPerGame: Double)

class FangraphsTeamService(network: HttpClient, repository: StatRepository) {

   def standingsUrl = "http://www.fangraphs.com/depthcharts.aspx?position=Standings"

   def getStandingsData: List[FangraphsTeamStats] = {
     val response = network.get(standingsUrl).response
     //val document = Parser.tidyHTML(response)
     //Parser.printChildNodes(document.getDocumentElement)
     val teamNodeList = getTeamNodesFromHtml(response)
     Parser.printChildNodes(teamNodeList.item(1))

//     val teamList = (for {
//       i <- Range(0, teamNodeList.getLength)
//       player = getTeamStatsFromNode(teamNodeList.item(i))
//     } yield player).toList
//     teamList
     null
   }

   private def getTeamNodesFromHtml(response: String): NodeList = {
     val document = Parser.tidyHTML(response)
     document.getDocumentElement.normalize()
     document.getDocumentElement.getChildNodes.item(1)
       .getChildNodes.item(0)
       .getChildNodes.item(27)
       .getChildNodes.item(1)
       .getChildNodes.item(8)
       .getChildNodes.item(0)
       .getChildNodes
   }

   private def getTeamStatsFromNode(node: Node): FangraphsTeamStats = {
//     val nodeList = node.getChildNodes
//     FangraphsTeamStats(nodeList.item(1).getFirstChild.getFirstChild.getNodeValue,
//       nodeList.item(12).getFirstChild.getNodeValue.toDouble,
//       nodeList.item(13).getFirstChild.getNodeValue.toDouble,
//       nodeList.item(14).getFirstChild.getNodeValue.toDouble,
//       nodeList.item(15).getFirstChild.getNodeValue.toDouble,
//       nodeList.item(16).getFirstChild.getNodeValue.toDouble,
//       nodeList.item(17).getFirstChild.getNodeValue.toDouble,
//       nodeList.item(20).getFirstChild.getNodeValue.toDouble)
     null
   }
 }

