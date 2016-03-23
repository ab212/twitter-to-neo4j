package com.godatadriven.twitter_classifier

import java.util.concurrent.{TimeUnit, TimeoutException}

import com.google.gson.Gson
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{Accumulator, SparkConf}
import org.neo4j.driver.internal.value.{ListValue, StringValue}
import org.neo4j.driver.v1.{GraphDatabase, Session, Value}
import twitter4j.Status

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

case class StoreToNeo4j(checkpointDirectory: String) {
  val languages = Array[String]("en", "en-gb", "nl")
  val insertStatement =
    """WITH {mentions} as mentions
      |MERGE (u:User {username: {username}})
      |CREATE (t:Tweet {text: {text}, language: {language}})
      |CREATE (u)-[:TWEETS]->(t)
      |foreach(name in mentions | MERGE (m:User {username: name}) CREATE (t)-[:MENTIONS]->(m))
    """.stripMargin

  private var gson = new Gson()

  def run(): Unit = {
    val streamingContext = StreamingContext.getOrCreate(checkpointDirectory, createStreamingContext)
    streamingContext.start()
    streamingContext.awaitTermination()
  }

  private def createStreamingContext(): StreamingContext = {
    val sparkConfig = new SparkConf().setAppName("Spark Twitter Example")
    sparkConfig.set("spark.streaming.backpressure.enabled", "true")

    val streamingContext = new StreamingContext(sparkConfig, Seconds(1))
    val failedMessages: Accumulator[Int] = streamingContext.sparkContext.accumulator(0, "failedMessages")
    val successMessages: Accumulator[Int] = streamingContext.sparkContext.accumulator(0, "successMessages")
    val allTweets = TwitterUtils.createStream(streamingContext, None)

    allTweets.foreachRDD { rdd =>
      rdd.foreachPartition { partition =>
        val driver = GraphDatabase.driver("bolt://localhost")
        val session = driver.session()
        partition.foreach { record =>
          try {
            Await.ready(Future {
              processRecord(session, record)
            }, Duration.apply(100, TimeUnit.MILLISECONDS))
            successMessages.add(1)
          } catch {
            case e: TimeoutException => failedMessages.add(1)
          }
        }
        session.close()
        driver.close()
      }
    }
    streamingContext
  }

  def processRecord(session: Session, record: Status): Unit = {
    if (shouldProcessRecord(record)) {
      val user = record.getUser
      val userName = new StringValue(user.getScreenName)
      val text = new StringValue(record.getText)
      val mentionNames: Array[Value] = record.getUserMentionEntities.map { x => new StringValue(x.getScreenName) }
      val mentions = new ListValue(mentionNames: _*)
      val tweet = scala.collection.mutable.Map[String, Value](
          "username" -> userName,
          "mentions" -> mentions,
          "text" -> text,
          "source" -> new StringValue(record.getSource),
          "language" -> new StringValue(record.getUser.getLang.toLowerCase))
      session.run(insertStatement, tweet)
    }

  }

  def shouldProcessRecord(record: Status): Boolean = {
    val language: String = record.getUser.getLang.toLowerCase
    //    !record.isRetweet && languages.contains(language)
    true
  }
}