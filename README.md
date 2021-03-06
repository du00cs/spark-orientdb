spark-orientdb
==============
[![Build Status](https://travis-ci.org/sbcd90/spark-orientdb.svg?branch=master)](https://travis-ci.org/sbcd90/spark-orientdb)    [ ![Download](https://api.bintray.com/packages/sbcd90/org.apache.spark/spark-orientdb-1.6.2_2.10/images/download.svg) ](https://bintray.com/sbcd90/org.apache.spark/spark-orientdb-1.6.2_2.10/_latestVersion)

Apache Spark datasource for OrientDB

OrientDB documentation
======================

Here is the latest documentation on [OrientDB](http://orientdb.com/orientdb/)

Compatibility
=============

`Spark`: 1.6+
`OrientDB`: 2.2.0+

Getting Started
===============

- Add the repository

```
<repository>
   <id>bintray</id>
   <name>bintray</name>
   <url>https://dl.bintray.com/sbcd90/org.apache.spark/</url>
</repository>
```

### For Spark 1.6

- Add the datasource as a maven dependency

```
<dependency>
   <groupId>org.apache.spark</groupId>
   <artifactId>spark-orientdb-1.6.2_2.10</artifactId>
   <version>1.3</version>
</dependency>
```

### For Spark 2.0

- Add the datasource as a maven dependency

```
<dependency>
   <groupId>org.apache.spark</groupId>
   <artifactId>spark-orientdb-2.0.0_2.10</artifactId>
   <version>1.3</version>
</dependency>
```

Scala api
=========

### OrientDB Documents

#### Write api:

```
import org.apache.spark.sql.SQLContext

val sqlContext = new SQLContext(sc)
sqlContext.createDataFrame(sc.parallelize(Array(1, 2, 3, 4, 5)), 
      StructType(Seq(StructField("id", IntegerType)))
      .write
      .format("org.apache.spark.orientdb.documents")
      .option("dburl", ORIENTDB_CONNECTION_URL)
      .option("user", ORIENTDB_USER).option("password", ORIENTDB_PASSWORD)
      .option("class", test_table)
      .mode(SaveMode.Overwrite)
      .save()
```

#### Read api:

```
import org.apache.spark.sql.SQLContext

val sqlContext = new SQLContext(sc)
val loadedDf = sqlContext.read
      .format("org.apache.spark.orientdb.documents")
      .option("dburl", ORIENTDB_CONNECTION_URL)
      .option("user", ORIENTDB_USER)
      .option("password", ORIENTDB_PASSWORD)
      .option("class", test_table)
      .option("query", s"select * from $test_table where teststring = 'asdf'")
      .load()
```

#### Query using OrientDB SQL:

```
import org.apache.spark.sql.SQLContext

val sqlContext = new SQLContext(sc)
val loadedDf = sqlContext.read
      .format("org.apache.spark.orientdb.documents")
      .option("dburl", ORIENTDB_CONNECTION_URL)
      .option("user", ORIENTDB_USER)
      .option("password", ORIENTDB_PASSWORD)
      .option("class", test_table)
      .option("query", s"select * from $test_table where teststring = 'asdf'")
      .load()
```

### OrientDB Graphs:

#### Create Vertex api:

```
import org.apache.spark.sql.SQLContext

val sqlContext = new SQLContext(sc)
sqlContext.createDataFrame(sc.parallelize(Array(1, 2, 3, 4, 5)),
      StructType(Seq(StructField("id", IntegerType)))
      .write
      .format("org.apache.spark.orientdb.graphs")
      .option("dburl", ORIENTDB_CONNECTION_URL)
      .option("user", ORIENTDB_USER)
      .option("password", ORIENTDB_PASSWORD)
      .option("vertextype", test_vertex_type2)
      .mode(SaveMode.Overwrite)
      .save()
```

#### Create Edge api:

```
import org.apache.spark.sql.SQLContext

val sqlContext = new SQLContext(sc)
sqlContext.createDataFrame(
      sc.parallelize(Seq(
            Row(1, 2, "friends"),
            Row(2, 3, "enemy"),
            Row(3, 4, "friends"),
            Row(4, 1, "enemy")
      )),
      StructType(Seq(
            StructField("src", IntegerType),
            StructField("dst", IntegerType),
            StructField("relationship", StringType)
          )))
      .write
      .format("org.apache.spark.orientdb.graphs")
      .option("dburl", ORIENTDB_CONNECTION_URL)
      .option("user", ORIENTDB_USER)
      .option("password", ORIENTDB_PASSWORD)
      .option("vertextype", test_vertex_type2)
      .option("edgetype", test_edge_type2)
      .mode(SaveMode.Overwrite)
      .save()
```

#### Read Vertex api:

```
import org.apache.spark.sql.SQLContext

val sqlContext = new SQLContext(sc)
val loadedDf = sqlContext.read
                    .format("org.apache.spark.orientdb.graphs")
                    .option("dburl", ORIENTDB_CONNECTION_URL)
                    .option("user", ORIENTDB_USER)
                    .option("password", ORIENTDB_PASSWORD)
                    .option("vertextype", test_vertex_type2)
                    .load()
```

#### Read edge api:

```
import org.apache.spark.sql.SQLContext

val sqlContext = new SQLContext(sc)
val loadedDf = sqlContext.read
                   .format("org.apache.spark.orientdb.graphs")
                   .option("dburl", ORIENTDB_CONNECTION_URL)
                   .option("user", ORIENTDB_USER)
                   .option("password", ORIENTDB_PASSWORD)
                   .option("edgetype", test_edge_type2)
                   .load()
```

#### Query using OrientDB Graph SQL:

```
import org.apache.spark.sql.SQLContext

val sqlContext = new SQLContext(sc)
val loadedVerticesDf = sqlContext.read
                 .format("org.apache.spark.orientdb.graphs")
                 .option("dburl", ORIENTDB_CONNECTION_URL)
                 .option("user", ORIENTDB_USER)
                 .option("password", ORIENTDB_PASSWORD)
                 .option("vertextype", test_vertex_type2)
                 .option("query", s"select * from $test_vertex_type2 where teststring = 'asdf'")
                 .load()
                 
val loadedEdgesDf = sqlContext.read
                 .format("org.apache.spark.orientdb.graphs")
                 .option("dburl", ORIENTDB_CONNECTION_URL)
                 .option("user", ORIENTDB_USER)
                 .option("password", ORIENTDB_PASSWORD)
                 .option("edgetype", test_edge_type2)
                 .option("query", s"select * from $test_edge_type2 where relationship = 'friends'")
                 .load()                 
```

### Integration with GraphFrames

```
import org.apache.spark.sql.SQLContext

val sqlContext = new SQLContext(sc)
val loadedVerticesDf = sqlContext.read
                 .format("org.apache.spark.orientdb.graphs")
                 .option("dburl", ORIENTDB_CONNECTION_URL)
                 .option("user", ORIENTDB_USER)
                 .option("password", ORIENTDB_PASSWORD)
                 .option("vertextype", test_vertex_type2)
                 .option("query", s"select * from $test_vertex_type2 where teststring = 'asdf'")
                 .load()
                 
val loadedEdgesDf = sqlContext.read
                 .format("org.apache.spark.orientdb.graphs")
                 .option("dburl", ORIENTDB_CONNECTION_URL)
                 .option("user", ORIENTDB_USER)
                 .option("password", ORIENTDB_PASSWORD)
                 .option("edgetype", test_edge_type2)
                 .option("query", s"select * from $test_edge_type2 where relationship = 'friends'")
                 .load()
                 
val g = GraphFrame(loadedVerticesDf, loadedEdgesDf)                 
```

A full example can be found in directory `src/main/examples`