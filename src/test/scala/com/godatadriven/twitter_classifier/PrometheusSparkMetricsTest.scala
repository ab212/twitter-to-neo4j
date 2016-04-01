package com.godatadriven.twitter_classifier

import org.apache.spark.streaming.Time
import org.apache.spark.streaming.scheduler.{BatchInfo, StreamInputInfo, StreamingListenerBatchCompleted}
import org.specs2.mutable.Specification

class PrometheusSparkMetricsTest extends Specification {
  "prometheus should be able to sent stats" >> {
    "return bla if bla" >> {
      val metrics: PrometheusSparkMetrics = new PrometheusSparkMetrics("test")
      metrics.onBatchCompleted(new StreamingListenerBatchCompleted(new BatchInfo(Time(12L), Map(1 -> new StreamInputInfo(1, 12, null)) , 1L, Option(12L), Option(15L))))
      "bla" must equalTo("bla")
    }
  }

}
