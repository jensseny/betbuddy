package com.betbuddy.util

import scala.xml._
import parsing._
import org.w3c.dom.{Node, Document}
import org.w3c.tidy.Tidy
import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
import grizzled.slf4j.Logging
import scala.collection.immutable.Range


object Parser extends NoBindingFactoryAdapter with Logging {

  private val tidy = new Tidy

  override def loadXML(source : InputSource, _p: SAXParser) = {
    loadXML(source)
  }

  def loadXML(source : InputSource) = {
    import nu.validator.htmlparser.{sax,common}
    import sax.HtmlParser
    import common.XmlViolationPolicy

    val reader = new HtmlParser
    reader.setXmlPolicy(XmlViolationPolicy.ALLOW)
    reader.setContentHandler(this)
    reader.parse(source)
    rootElem
  }

  def tidyHTML(html: String): Document = {
    tidy.setXHTML(true)
    tidy.setQuiet(true) // SHUT UP ALREADY
    tidy.setShowWarnings(false)
    //tidy.setPrintBodyOnly(true)
    tidy.setWrapAttVals(true)
    val is = new ByteArrayInputStream(html.getBytes)
    val os = new ByteArrayOutputStream()
    val dom = tidy.parseDOM(is, os)
    is.close(); os.close
    //logger.info(os.toString)
    dom
  }

  def printChildNodes(node: Node, padding: String = "", depth: Int = 1) {
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
