package com.godatadriven.twitter_classifier

import java.util.concurrent.{TimeUnit, TimeoutException}

import com.google.gson.{GsonBuilder, Gson}
import kafka.serializer.StringDecoder
import org.apache.spark.api.java.StorageLevels
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{Accumulator, SparkConf}
import org.neo4j.driver.internal.value.{ListValue, StringValue}
import org.neo4j.driver.v1.{GraphDatabase, Session, Value}
import twitter4j.Status
import twitter4j.json.DataObjectFactory

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
    sparkConfig.set("spark.streaming.receiver.maxRate", "1000")

    val streamingContext = new StreamingContext(sparkConfig, Seconds(1))
    val failedMessages: Accumulator[Int] = streamingContext.sparkContext.accumulator(0, "failedMessages")
    val successMessages: Accumulator[Int] = streamingContext.sparkContext.accumulator(0, "successMessages")
    val allTweets = KafkaUtils.createStream[String,String,StringDecoder, StringDecoder](streamingContext, kafkaConsumerParams, topics, StorageLevels.MEMORY_ONLY)

    allTweets.foreachRDD { rdd =>
      rdd.foreachPartition { partition =>
        val driver = GraphDatabase.driver("bolt://localhost")
        val session = driver.session()
        partition.foreach { record =>
          try {
            processRecord(session, record._2)
            Await.ready(Future {
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

  def processRecord(session: Session, json: String): Unit = {
    val gson = new GsonBuilder().create();
    val record = gson.fromJson(json, classOf[Tweet])
    if (shouldProcessRecord(record)) {
      val user = record.getUser
      if (user == null) {
        println("xxxxxx: user == null. JSON: " + json)
      }
      val userName = new StringValue(user.getScreenName)
      val text = new StringValue(record.getText)
      val mentionNames: Array[Value] = record.getUserMentionEntities.map { x => new StringValue(x.getScreenName) }
      val mentions = new ListValue(mentionNames: _*)
      val tweet = scala.collection.mutable.Map[String, Value](
        "username" -> userName,
        "mentions" -> mentions,
        "text" -> text,
        "source" -> new StringValue(record.getSource),
        "language" -> new StringValue(record.getUser.getLanguage.toLowerCase))
      session.run(insertStatement, tweet)
    }
  }

  def shouldProcessRecord(record: Tweet): Boolean = {
    val language: String = record.getUser.getLanguage.toLowerCase
    //    !record.isRetweet && languages.contains(language)
    true
  }
}