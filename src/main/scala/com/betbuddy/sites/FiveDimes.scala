package com.betbuddy.sites

import com.betbuddy.http._
import grizzled.slf4j.Logging
import java.net.URL
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import com.betbuddy.http.HttpClient
import scala.xml.parsing.XhtmlParser
import com.betbuddy.util.Parser
import scala.collection.immutable.Range
import org.w3c.dom.Node
import com.betbuddy.config.FiveDimesConfig
import org.joda.time.DateTime

case class Line (name: String, spreadRange: Option[String], spreadLine: Option[Int], moneyLine: Option[Int], totalPointsRange: Option[String], totalPointsLine: Option[Int])
case class GameLine (home: Line, away: Line)

// REDO ^^^^^ when/if I decide to track spreads or over/unders

class FiveDimes (config: FiveDimesConfig, c: HttpClient) extends BaseSite (c) with Site with Logging {

  implicit val client = new ApacheHttpClient
  val basePage ="http://www.5dimes.eu/"
  val loginPage = basePage + "LoginVerify.Asp"
  val today = (new DateTime).dayOfWeek().getAsText
  println(today)

  def login(username: String, password: String): Boolean = {

    val loginResult = c.post(loginPage, "customerID=%s&password=%s&goto=S&submit1=Login" format (username, password))
    logger.info(loginResult)
    if (loginResult.response.contains("Failed") || loginResult.statusCode != 302) false else true
  }

  def getLines(): List[GameLine] = {

    def nodeToLine (node: Node): Line = {
      Parser.printChildNodes(node)
      val offset = if (node.getFirstChild.hasChildNodes) 0 else 1
      val nodeList = node.getChildNodes
      println(offset)
      Line(
        nodeList.item(offset).getFirstChild.getNodeValue.substring(6, nodeList.item(offset).getFirstChild.getNodeValue.length),
        {
          val n = nodeList.item(offset + 1).getFirstChild.getNodeValue
          if (n == null) None else Some(n)
        },
        {
          val n = nodeList.item(offset + 1).getChildNodes.item(1)
          if (n == null) None else if (n.getFirstChild.getNodeValue == null) None else Some(n.getFirstChild.getNodeValue.toInt)
        },
        {
          val n = nodeList.item(offset + 2).getFirstChild
          if (n == null) None else if (n.getFirstChild.getNodeValue == null) None else Some(n.getFirstChild.getNodeValue.toInt)
        },
        {
          val n = nodeList.item(offset + 3).getFirstChild
          if (n == null) None else if (n.getNodeValue == null) None else Some(n.getNodeValue)
        },
        {
          val n = nodeList.item(offset + 3).getChildNodes.item(1)
          if (n == null) None else if (n.getFirstChild.getNodeValue == null) None else Some(n.getFirstChild.getNodeValue.toInt)
        }
      )
    }

    val response = c.post("http://lines.5dimes.com/livelines/ajax/Player.LiveLines,LiveLines.ashx?_method=GetLinesForSport&_session=no", "strID=%s" format config.sportCode).response
    //println (response)
    //val response = scala.io.Source.fromFile("test2.txt").getLines().mkString("")
    val document = Parser.tidyHTML(response)
    document.getDocumentElement.normalize()
    //Parser.printChildNodes(document.getDocumentElement)
    val listLength = (for {
      nodeNum <- Range(0, document.getDocumentElement.getChildNodes.item(1).getChildNodes.getLength)
      node = {
        //println(nodeNum + "   " + document.getDocumentElement.getChildNodes.item(1).getChildNodes.item(nodeNum).hasChildNodes + "    " + document.getDocumentElement.getChildNodes.item(1).getChildNodes.item(nodeNum).getNodeValue)
        document.getDocumentElement.getChildNodes.item(1).getChildNodes.item(nodeNum)
      }
    } yield node).toList.takeWhile(n => !n.hasChildNodes).length
    //Parser.printChildNodes(document.getDocumentElement.getChildNodes.item(1).getChildNodes.item(listLength))
    //println(listLength)

    val nodeListA = for {
      nodeNum <- Range(0, document.getDocumentElement.getChildNodes.item(1).getChildNodes.item(listLength).getChildNodes.getLength)
      //a = println(nodeNum + "    " + Parser.printChildNodes(document.getDocumentElement.getChildNodes.item(1).getChildNodes.item(listLength).getChildNodes.item(nodeNum)))
      node = document.getDocumentElement.getChildNodes.item(1).getChildNodes.item(listLength).getChildNodes.item(nodeNum)
    } yield node

    //println(nodeListA.length)

    val nodeList = nodeListA.toList.filter(n => if (!n.getFirstChild.hasChildNodes) true else !n.getFirstChild.getFirstChild.getNodeValue.contains("Baseball"))
                        .takeWhile(n => if (!n.getFirstChild.hasChildNodes) true else if (n.getChildNodes.getLength != 1) true else n.getFirstChild.getFirstChild.getNodeValue.contains(today))
                        .filter(n => if (n.getChildNodes.getLength == 1 && !n.getFirstChild.hasChildNodes) false else true)
                        .filter(n => if (n.getChildNodes.getLength == 1 && n.getFirstChild.getChildNodes.getLength == 1) false else true)

    //println("******* " + nodeList.length)

    val gameLines: List[GameLine] = (for {
      //nodeNum <- Range(0, nodeList.length, 3)
      nodeNum <- Range(0, nodeList.length, 2)
      gameLine = {
        //println ("--------------------")
        //Parser.printChildNodes(nodeList(nodeNum))
        //println(nodeToLine(nodeList(nodeNum)))
        //println(nodeToLine(nodeList(nodeNum + 1)))
        //println("000 " + (if (nodeList(nodeNum).getChildNodes.getLength == 1 && !nodeList(nodeNum).getFirstChild.hasChildNodes) false else true) + "   " + Parser.printChildNodes(nodeList(nodeNum)))
        //println(nodeList(nodeNum).getChildNodes.getLength)
        //println(nodeList(nodeNum).getFirstChild.hasChildNodes)
        //println("*** " + Parser.printChildNodes(nodeList(nodeNum).getFirstChild))
        //println("111 " + (if (nodeList(nodeNum + 1).getChildNodes.getLength == 1 && !nodeList(nodeNum + 1).getFirstChild.hasChildNodes) false else true) + "   " + Parser.printChildNodes(nodeList(nodeNum + 1)))
        //println ("--------------------")
//        println("222" + Parser.printChildNodes(nodeList(nodeNum + 2)))
//        println("333" + Parser.printChildNodes(nodeList(nodeNum + 3)))
//        println("444" + Parser.printChildNodes(nodeList(nodeNum + 4)))
//        println("555" + Parser.printChildNodes(nodeList(nodeNum + 5)))
//        println("666" + Parser.printChildNodes(nodeList(nodeNum + 6)))
        GameLine(nodeToLine(nodeList(nodeNum)), nodeToLine(nodeList(nodeNum + 1)))
        //GameLine(null, null)
      }
    } yield gameLine).toList

    //println(gameLines)
    gameLines
  }
}
