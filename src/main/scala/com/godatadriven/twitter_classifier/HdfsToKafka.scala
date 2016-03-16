package com.godatadriven.twitter_classifier

import com.google.gson.Gson
import org.apache.kafka.clients.producer.{ProducerRecord, KafkaProducer}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConversions._


/**
 * Collect at least the specified number of tweets into json text files.
 */
object HdfsToKafka {
  private var numTweetsCollected = 0L
  private var partNum = 0
  private var gson = new Gson()

  val kafkaProducerParams: Map[String, Object] = Map(
    "bootstrap.servers" -> "localhost:9092",
    "client.id" -> "twitter-producer",
    "key.serializer" -> "org.apache.kafka.common.serialization.StringSerializer",
    "value.serializer" -> "org.apache.kafka.common.serialization.StringSerializer"
  )

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName(this.getClass.getSimpleName)
    val sc = new SparkContext(conf)
    val textFile = sc.textFile("/tweets/*/*")
    textFile.foreachPartition(partition => {
      val producer = new KafkaProducer[String, String](mapAsJavaMap(kafkaProducerParams))
      partition.foreach(tweet => {
        val record: ProducerRecord[String, String] = new ProducerRecord[String,String]("tweets", tweet)
        producer.send(record)
      })
      producer.close()
    })
  }
}