package com.spark.encryption

import org.apache.spark.sql._
import org.apache.log4j._
import org.apache.spark.sql.functions.sha2

/* Docs
* https://dev.mysql.com/doc/refman/8.0/en/encryption-functions.html#function_sha2
* https://spark.apache.org/docs/latest/api/scala/org/apache/spark/sql/functions$.html
*/

object SHA2 {

  case class Person(id:Int, name:String, age:Int, friends:Int)

  /** Our main function where the action happens */
  def main(args: Array[String]) {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Use new SparkSession interface in Spark 2.0
    val spark = SparkSession
      .builder
      .appName("SparkSQL")
      .master("local[*]")
      .getOrCreate()

    // Convert our csv file to a DataSet, using our Person case
    // class to infer the schema.
    import spark.implicits._
    val people = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv("data/fakefriends.csv")
      .as[Person]

    people.select("id", "name")
      .withColumn("hashed_name", sha2($"name", 512)).show()

    spark.stop()
  }

}