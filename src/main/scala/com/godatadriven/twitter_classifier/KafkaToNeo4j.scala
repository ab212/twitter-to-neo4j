package com.godatadriven.twitter_classifier

import java.util.concurrent.{TimeUnit, TimeoutException}

import com.google.common.base.Stopwatch
import com.google.gson.GsonBuilder
import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.api.java.StorageLevels
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.neo4j.driver.internal.value.{ListValue, StringValue}
import org.neo4j.driver.v1._

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object KafkaToNeo4j {
  val languages = Array[String]("en", "en-gb", "nl")
  val insertStatement =
    """WITH {mentions} as mentions
      |MERGE (u:User {username: {username}})
      |CREATE (t:Tweet {text: {text}, language: {language}})
      |CREATE (u)-[:TWEETS]->(t)
      |foreach(name in mentions | MERGE (m:User {username: name}) CREATE (t)-[:MENTIONS]->(m))
    """.stripMargin

  val kafkaConsumerParams: Map[String, String] = Map(
    "zookeeper.connect" -> "localhost:2181",
    "group.id" -> "kafka-to-neo4j",
    "auto.offset.reset" -> "largest"
  )
  val topics: Map[String, Int] = Map(
    "tweets" -> 36
  )

  def main(args: Array[String]): Unit = {
    KafkaToNeo4j.run()
  }

  def run(): Unit = {
    val streamingContext = StreamingContext.getOrCreate("~/spark-checkpoints", createStreamingContext)
    streamingContext.start()
    streamingContext.awaitTermination()
  }

  private def createStreamingContext(): StreamingContext = {
    val sparkConfig = new SparkConf().setAppName("Spark Twitter Example")
    sparkConfig.set("spark.streaming.backpressure.enabled", "true")
    sparkConfig.set("spark.streaming.receiver.maxRate", "5000")

    val streamingContext = new StreamingContext(sparkConfig, Seconds(1))
    val allTweets = KafkaUtils.createStream[String,String,StringDecoder, StringDecoder](streamingContext, kafkaConsumerParams, topics, StorageLevels.MEMORY_ONLY)

    allTweets.foreachRDD { rdd =>
      rdd.foreachPartition { partition =>
        try {
          Await.ready(Future {
            val driver = GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "test"))
            val session = driver.session()
            val transaction = session.beginTransaction()
            partition.foreach { record =>
              processRecord(transaction, record._2)
            }
            transaction.success()
            transaction.close()
            session.close()
            driver.close()
          }, Duration.apply(10, TimeUnit.SECONDS))
        } catch {
          case e: TimeoutException => {
            println("xxxxxxxxx")
          }
        }
      }

    }
    streamingContext
  }

  def processRecord(transaction: Transaction, json: String): Unit = {
    try {
      Await.ready(Future {
        parseTweet(transaction, json)
      }, Duration.apply(600, TimeUnit.MILLISECONDS))
    } catch {
      case e: TimeoutException => {
        println(json)
      }
    }
  }

  def parseTweet(transaction: Transaction, json: String) = {
    val gson = new GsonBuilder().create();
    val record = gson.fromJson(json, classOf[Tweet])
    val text = new StringValue(record.getText)
    val userName = new StringValue(record.getUser.getScreenName)
    val mentionNames: Array[Value] = record.getUserMentionEntities.map { x => new StringValue(x.getScreenName) }
    val mentions = new ListValue(mentionNames: _*)
    val tweet = scala.collection.mutable.Map[String, Value](
        "username" -> userName,
        "mentions" -> mentions,
        "text" -> text,
        "source" -> new StringValue(record.getSource),
        "language" -> new StringValue(record.getUser.getLanguage))
    transaction.run(insertStatement, tweet)
  }
}