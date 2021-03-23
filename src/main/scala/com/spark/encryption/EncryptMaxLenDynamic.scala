package com.spark.encryption

import org.apache.log4j._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

/*References:
* https://stackoverflow.com/questions/42761328/add-columns-in-dataframes-dynamically-with-column-names-as-elements-in-list
* https://stackoverflow.com/questions/6051302/what-does-colon-underscore-star-do-in-scala
* https://stackoverflow.com/questions/45347294/spark-scala-dynamic-column-selection-from-dataframe
* https://stackoverflow.com/questions/62878601/how-to-concat-all-columns-in-a-spark-dataframe-using-java
* https://stackoverflow.com/questions/31450846/concatenate-columns-in-apache-spark-dataframe
* https://stackoverflow.com/questions/48389438/compare-two-columns-to-create-a-new-column-in-spark-dataframe
* As last resource for using RSA
* https://stackoverflow.com/questions/41400504/spark-scala-repeated-calls-to-withcolumn-using-the-same-function-on-multiple-c
* http://allaboutscala.com/tutorials/chapter-8-beginner-tutorial-using-scala-collection-functions/scala-foldleft-example/
*/

object EncryptMaxLenDynamic {

  /** We set our encryptor class instance and its configurations **/
  val encryptor = new RSAUtils()
  encryptor.setKeyPath("D:/SparkScalaCourse/SparkTest")

  // We declare an "anonymous function" for encryption
  val encryptCol : String => String = (myVal: String)=>{
    encryptor.encryptWithRSA(myVal)
  }

  // Then wrap it with a udf
  val encryptColUDF = udf(encryptCol)

  // We declare another "anonymous function" for encryption
  val decryptCol : String => String = (myVal: String)=>{
    encryptor.decryptWithRSA(myVal)
  }

  // And again wrap it with a udf
  val decryptColUDF = udf(decryptCol)

  /** Our main function where the action happens */
  def main(args: Array[String]) {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    /** Create a SparkSession using every core of the local machine **/
    val spark = SparkSession
      .builder
      .appName("EncryptMaxLenDynamic")
      .master("local[*]")
      .getOrCreate()

    /** Loading random data for our example **/

    // Columns array
    val featureColArr = Array("rand_1", "rand_2", "rand_3", "rand_4", "rand_5",
                              "rand_6", "rand_7", "rand_8", "rand_9", "rand_10")

    // Load up data dynamically
    val randData = importCsvDynamic(spark, "data/random_data.csv", featureColArr)
    randData.show(5)

    /** Concatenating selected columns into a single string **/

    // Columns that are needed to be concatenated
    val selectedColArr = Array("rand_1")

    // Building the concatenated string column
    val stringsDS = columnsToString(spark, randData)
    stringsDS.show(5)

    /** Examples of the algorithms **/
    // Adding length of columns values and hashing algorithm SHA512
    val encryptedSDS = stringsDS
      .select("value")
      //.withColumn("value", concat(col("value"), col("value")))
      .withColumn("value_len", length(col("value")))
      /*.withColumn("sha512", sha2(col("value"), 512))
      .withColumn("sha512_len", length(col("sha512")))*/
      .withColumn("encrypted", encryptColUDF(col("value")))
      .withColumn("encrypted_len", length(col("encrypted")))
      .withColumn("decrypted", decryptColUDF(col("encrypted")))
      .withColumn("decrypted_len", length(col("decrypted")))
      .withColumn("flag",
        when(col("value") === col("decrypted"), 1)
          .otherwise(0)
      )

    encryptedSDS.show(5)

  }

  def importCsvDynamic(spark: SparkSession, path: String, features: Array[String] = Array()): DataFrame = {

    if (features.length > 0) {
      val colNames = features.map(name => col(name))
      val df = spark.read
        .option("inferSchema", "true")
        .option("header", "true")
        .csv(path)
        .select(colNames: _*)
      df
    }else {
      val df = spark.read
        .option("inferSchema", "true")
        .option("header", "true")
        .csv(path)
      df
    }

  }

  def columnsToString(spark: SparkSession, df: DataFrame, features: Array[String] = Array()): DataFrame = {
    // import spark.implicits._
    if (features.length > 0) {
      val colNames = features.map(name => col(name))
      //df.select(colNames: _*).map(row => row.mkString(","))
      df.withColumn("value", concat(colNames: _*))
    }else{
      //df.map(row => row.mkString(","))
      //df.withColumn("value", expr("concat_ws(',',*)"))
      df.withColumn("value", expr("concat_ws('',*)"))
    }
  }

}
