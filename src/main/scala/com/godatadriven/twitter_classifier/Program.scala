package com.godatadriven.twitter_classifier

import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.neo4j.driver.internal.value.StringValue
import org.neo4j.driver.v1.{GraphDatabase, Value}

import scala.collection.JavaConversions._

case class Program(checkpointDirectory: String) {
  val windowDuration = Seconds(30)
  val slideDuration = Seconds(5)
  val topUserCount = 3
  val maxTwitterNameLength = 15
  val nameRegex = s"([a-zA-Z0-9_]{1,$maxTwitterNameLength})".r
  val insertStatement =
    """MERGE (u:User {username: {username}})
       MERGE (m:User {username: {mentions}})
       MERGE (u)-[:MENTIONS]->(m)
    """.stripMargin

  def run(): Unit = {
    val streamingContext = StreamingContext.getOrCreate(checkpointDirectory, createStreamingContext)
    streamingContext.start()
    streamingContext.awaitTermination()
  }

  private def createStreamingContext(): StreamingContext = {
    val sparkConfig = new SparkConf().setAppName("Spark Twitter Example")

    val streamingContext = new StreamingContext(sparkConfig, Seconds(10))

    // Input stream:
    val allTweets = TwitterUtils.createStream(streamingContext, None)

    allTweets.foreachRDD { rdd =>
      rdd.partitions.length
      rdd.foreachPartition { partition =>
        val session = GraphDatabase.driver("bolt://localhost").session()
        partition.foreach { record =>
          if (record.getUserMentionEntities.length > 0) {
            val user = record.getUser
            val mentions = record.getUserMentionEntities.apply(0)
            session.run(insertStatement, scala.collection.mutable.Map[String, Value]("username" -> new StringValue(user.getScreenName), "mentions" -> new StringValue(mentions.getScreenName)))
            //          println("userScreenName: " + user.getScreenName + " username: " + user.getName)
          }
        }
      }
    }

    streamingContext
  }
}