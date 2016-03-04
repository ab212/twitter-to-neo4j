# twitter-to-neo4j
Test project to create a network of users in Neo4j from a spark streaming twitter job

## Get started

### Install Hadoop + Spark
Make sure you have a Spark installation running.
I'm using https://github.com/krisgeus/ansible_local_cdh_hadoop to get a pseudo distributed CDH cluster.

### Configure Twitter
add twitter4j.properties to the root of this project with the content

```
oauth.consumerKey=******************
oauth.consumerSecret=******************
oauth.accessToken=******************
oauth.accessTokenSecret=******************
```

### build and run
```
sbt assembly
```

start run_on_spark.sh to run on a local Spark cluster

```
./run_on_spark.sh
```