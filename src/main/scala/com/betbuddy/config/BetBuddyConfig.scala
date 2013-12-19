package com.betbuddy.config

import com.typesafe.config.{ConfigFactory, Config}
import scala.collection.JavaConversions._

case class DatabaseConfig(c: Config) {
  val config = c.getConfig("database")

  val name: String = config.getString("name")
  val driverClass: String = config.getString("driverClass")
  val user: String = config.getString("user")
  val password: String = config.getString("password")
  val url: String = config.getString("url")
  val properties: Map[String, String] = config.getConfig("properties").entrySet().map(e => e.getKey -> e.getValue.unwrapped().toString).toMap
  val validationQuery: String = config.getString("validationQuery")
  val maxActive: Int = config.getInt("maxActive")

  // Properties are sorted by property name for ease of testing
  val urlWithProperties = url + "?" + properties.toSeq.sortBy(_._1).map(p => p._1 + "=" + p._2).mkString("&")

  override def toString = Seq(driverClass,user,password,url,properties,validationQuery,maxActive,urlWithProperties).mkString(productPrefix + "(",",",")")
}

case class FiveDimesConfig (c: Config) {
  val config = c.getConfig("fiveDimes")

  val sportCode: String = config.getString("sportCode")

  override def toString = Seq(sportCode).toString
}

case class BetBuddyConfig (config: Config) {
  val cbConfig = config.getConfig("betbuddy")
  val database = DatabaseConfig(cbConfig)
  val fiveDimes = FiveDimesConfig(cbConfig)
  override def toString = Seq(database).mkString(productPrefix + "(",",",")")
}
