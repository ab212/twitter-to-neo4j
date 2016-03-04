name := "twitter-to-neo4j"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq("org.apache.spark" %% "spark-core" % "1.5.0",
                            "org.apache.spark" % "spark-streaming_2.10" % "1.5.0",
                            "org.apache.spark" % "spark-streaming-twitter_2.10" % "1.5.0",
                            "org.apache.spark" % "spark-mllib_2.10" % "1.5.0",
                            "org.neo4j.driver" % "neo4j-java-driver" % "1.0.0-M04")

assemblyMergeStrategy in assembly := {
  case PathList("javax", "xml", xs @ _*) => MergeStrategy.last
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case PathList("com", "google", xs @ _*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
    