name := "twitter-to-neo4j"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq("org.apache.spark" %% "spark-core" % "1.5.0",
                            "org.apache.spark" %  "spark-streaming_2.10" % "1.5.0",
                            "org.apache.spark" %  "spark-streaming-twitter_2.10" % "1.5.0",
                            "org.apache.spark" %% "spark-streaming-kafka" % "1.5.0",
                            "org.apache.spark" %  "spark-mllib_2.10" % "1.5.0",
                            "org.apache.kafka" %%  "kafka" % "0.8.2.1",
                            "org.apache.kafka" %  "kafka-clients" % "0.8.2.1",
                            "org.neo4j.driver" %  "neo4j-java-driver" % "1.0.0-M04",
                            "org.slf4j" % "slf4j-api" % "1.7.10",
                            "org.slf4j" % "slf4j-log4j12" % "1.7.10",
                            "com.google.code.gson" % "gson" % "2.6.2",
                            "org.scalactic" %% "scalactic" % "2.2.6",
                            "org.scalatest" %% "scalatest" % "2.2.6" % "test")

assemblyMergeStrategy in assembly := {
  case PathList("javax", "xml", xs @ _*) => MergeStrategy.last
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case PathList("com", "google", xs @ _*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
    