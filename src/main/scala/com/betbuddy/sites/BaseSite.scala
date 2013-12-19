package com.betbuddy.sites

import com.betbuddy.http.HttpClient

trait Site {
  def login(username: String, password: String): Boolean
  def getLines(): List[GameLine]
}

class BaseSite (client: HttpClient) {

}