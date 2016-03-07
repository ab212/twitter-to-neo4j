package com.godatadriven.twitter_classifier

import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.neo4j.driver.internal.value.{ListValue, StringValue}
import org.neo4j.driver.v1.{GraphDatabase, Value}

import scala.collection.JavaConversions._

case class Program(checkpointDirectory: String) {
  val languages = Array[String]("en", "en-gb", "nl")
  val windowDuration = Seconds(30)
  val slideDuration = Seconds(5)
  val topUserCount = 3
  val maxTwitterNameLength = 15
  val nameRegex = s"([a-zA-Z0-9_]{1,$maxTwitterNameLength})".r
  val insertStatement =
    """WITH {mentions} as mentions
      |MERGE (u:User {username: {username}})
      |CREATE (t:Tweet {text: {text}, language: {language}})
      |CREATE (u)-[:TWEETS]->(t)
      |foreach(name in mentions | MERGE (m:User {username: name}) CREATE (t)-[:MENTIONS]->(m))
    """.stripMargin

  def run(): Unit = {
    val streamingContext = StreamingContext.getOrCreate(checkpointDirectory, createStreamingContext)
    streamingContext.start()
    streamingContext.awaitTermination()
  }

  private def createStreamingContext(): StreamingContext = {
    val sparkConfig = new SparkConf().setAppName("Spark Twitter Example")
    sparkConfig.set("spark.streaming.backpressure.enabled","true")

    val streamingContext = new StreamingContext(sparkConfig, Seconds(1))

    // Input stream:

    val allTweets = TwitterUtils.createStream(streamingContext, None)//, filters = filters)

    allTweets.foreachRDD { rdd =>
      rdd.partitions.length
      rdd.foreachPartition { partition =>
        val driver = GraphDatabase.driver("bolt://localhost")
        val session = driver.session()
        partition.foreach { record =>
            val language: String = record.getUser.getLang.toLowerCase
          if (! record.isRetweet && languages.contains(language) ) {
            val user = record.getUser
            val userName = new StringValue(user.getScreenName)
            val text = new StringValue(record.getText)
            val mentionNames: Array[Value] = record.getUserMentionEntities.map{x => new StringValue(x.getScreenName)}
            val mentions = new ListValue(mentionNames:_*)
            session.run(insertStatement,
              scala.collection.mutable.Map[String, Value](
                "username" -> userName,
                "mentions" -> mentions,
                "text" -> text,
                "language" -> new StringValue(language)))
            //          println("userScreenName: " + user.getScreenName + " username: " + user.getName)
          }
        }
        session.close()
        driver.close()
      }
    }

    streamingContext
  }
}