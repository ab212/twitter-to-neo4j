package com.godatadriven.twitter_classifier

import io.prometheus.client.exporter.PushGateway
import io.prometheus.client.{CollectorRegistry, Gauge}
import org.apache.spark.streaming.scheduler.{StreamingListener, StreamingListenerBatchCompleted}

class PrometheusSparkMetrics(sparkJob: String) extends StreamingListener {

  override def onBatchCompleted(batchCompleted: StreamingListenerBatchCompleted): Unit = {
    val registry: CollectorRegistry = new CollectorRegistry()
    val pushGateway: PushGateway = new PushGateway("127.0.0.1:9091")
    addInputRate(batchCompleted, registry)
    addSchedulingDelay(batchCompleted, registry)
    addProcessingTime(batchCompleted, registry)
    addTotalDelay(batchCompleted, registry)
    pushGateway.push(registry, "spark_streaming_exporter")
  }

  def addInputRate(batchCompleted: StreamingListenerBatchCompleted, registry: CollectorRegistry): Unit = {
    addMetric(registry, batchCompleted.batchInfo.numRecords, "spark_streaming_input_rate", "The input rate of our spark streaming job")
  }

  def addSchedulingDelay(batchCompleted: StreamingListenerBatchCompleted, registry: CollectorRegistry) = {
    addMetric(registry, batchCompleted.batchInfo.schedulingDelay.get, "spark_streaming_scheduling_delay", "The scheduling delay of our spark streaming job")
  }
  def addProcessingTime(batchCompleted: StreamingListenerBatchCompleted, registry: CollectorRegistry) = {
    addMetric(registry, batchCompleted.batchInfo.processingDelay.get, "spark_streaming_processing_time", "The processing delay of our spark streaming job")
  }

  def addTotalDelay(batchCompleted: StreamingListenerBatchCompleted, registry: CollectorRegistry) = {
    addMetric(registry, batchCompleted.batchInfo.totalDelay.get, "spark_streaming_total_delay", "The total delay of our spark streaming job")
  }

  def addMetric(registry: CollectorRegistry, value: Double, name: String, helpText: String): Unit = {
    val totalDelay: Gauge = Gauge.build()
      .help(helpText)
      .name(name)
      .labelNames("spark_job")
      .register(registry)
    totalDelay.labels(sparkJob).set(value)
  }
}
