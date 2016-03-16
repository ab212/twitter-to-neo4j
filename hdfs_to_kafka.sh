#!/bin/sh

$SPARK_HOME/bin/spark-submit \
  --master "local[*]" \
  --deploy-mode client \
  --class com.godatadriven.twitter_classifier.HdfsToKafka \
  target/scala-2.10/twitter-to-neo4j-assembly-1.0.jar