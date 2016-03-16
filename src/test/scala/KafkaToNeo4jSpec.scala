package com.godatadriven.twitter_classifier

import com.google.gson.GsonBuilder
import org.scalatest._

import scala.io.Source

class KafkaToNeo4jSpec extends FlatSpec with Matchers {
  "The twitterparser" should "be able to parse the stored json" in {
    val reader: String = Source.fromInputStream(getClass.getResourceAsStream("/test.json"), "UTF-8").mkString
    val gson = new GsonBuilder().create();
    val tweet = gson.fromJson(reader, classOf[Tweet])
    tweet.inReplyToScreenName should be("lalydo")
    tweet.user.screenName should be("Labroussefranc2")
  }
}