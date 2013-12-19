package com.betbuddy.data

import javax.sql.DataSource

case class Player (name: String, atBats: Double, hits: Double, doubles: Double, triples: Double, homeruns: Double, walks: Double, hitByPitches: Double, sacFlies: Double)

class StatRepository (datastore: DataSource) {

  def getPlayer(name: String): Player = {
    Player("Test Testerson", 600, 200, 40, 5, 25, 90, 15, 30)
  }
}
