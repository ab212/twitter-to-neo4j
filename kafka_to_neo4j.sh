#!/bin/sh

$SPARK_HOME/bin/spark-submit \
  --master "local[*]" \
  --deploy-mode client \
  --class com.godatadriven.twitter_classifier.KafkaToNeo4j \
  --driver-class-path /Users/rvanweverwijk/.m2/repository/org/apache/kafka/kafka_2.10/0.8.2.1/kafka_2.10-0.8.2.1.jar:/Users/rvanweverwijk/.ivy2/cache/com.101tec/zkclient/jars/zkclient-0.3.jar:/Users/rvanweverwijk/.ivy2_old/cache/com.yammer.metrics/metrics-core/jars/metrics-core-2.2.0.jar:/Users/rvanweverwijk/.m2/repository/org/apache/kafka/kafka-clients/0.8.2.1/kafka-clients-0.8.2.1.jar \
  target/scala-2.10/twitter-to-neo4j-assembly-1.0.jar