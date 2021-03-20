package com.spark.encryption

import org.apache.log4j._
import com.spark.encryption.RSAUtils
import org.apache.spark.sql.functions._
import org.apache.spark.sql.SparkSession

/* Referencias:
*  https://sparkbyexamples.com/spark/spark-concatenate-dataframe-columns/
*  https://stackoverflow.com/questions/42389203/how-to-convert-the-datasets-of-spark-row-into-string/42393617
*  https://stackoverflow.com/questions/10007147/getting-a-illegalblocksizeexception-data-must-not-be-longer-than-256-bytes-when
*  https://stackoverflow.com/questions/25844026/key-length-limit-with-java-cryptography-extension
*  https://stackoverflow.com/questions/6481627/java-security-illegal-key-size-or-default-parameters
* */

object EncryptMaxLen {

  case class RandString(rand_1: String, rand_2: String, rand_3: String, rand_4: String, rand_5: String/*,
                        rand_6: String, rand_7: String, rand_8: String, rand_9: String, rand_10: String*/)

  /** We set our encryptor class instance and its configurations **/
  val encryptor = new RSAUtils()
  encryptor.setKeyPath("D:/SparkScalaCourse/SparkTest")

  /** Our main function where the action happens */
  def main(args: Array[String]) {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Create a SparkSession using every core of the local machine
    val spark = SparkSession
      .builder
      .appName("PopularMoviesNicer")
      .master("local[*]")
      .getOrCreate()

    // Load up movie data as dataset
    import spark.implicits._
    val randData = spark.read
      .option("inferSchema", "true")
      .option("header", "true")
      .csv("data/random_data.csv")
      .select("rand_1", "rand_2", "rand_3", "rand_4", "rand_5"/*,
        "rand_6", "rand_7", "rand_8", "rand_9", "rand_10"*/)
      .as[RandString]

    randData.show()

    val stringsDS = randData.toDF.map(row => row.mkString(","))

    // We declare another "anonymous function" for encryption
    val encryptCol : String => String = (myVal: String)=>{
      encryptor.encryptWithRSA(myVal)
    }

    // Then wrap it with a udf
    val encryptColUDF = udf(encryptCol)

    // We declare another "anonymous function" for encryption
    val decryptCol : String => String = (myVal: String)=>{
      encryptor.decryptWithRSA(myVal)
    }

    // Then wrap it with a udf
    val decryptColUDF = udf(decryptCol)

    // Add a movieTitle column using our new udf
    val encryptedSDS = stringsDS
      .withColumn("value_len", length($"value"))
      .withColumn("encrypted", encryptColUDF(col("value")))
      .withColumn("encrypted_len", length($"encrypted"))
      .withColumn("decrypted", decryptColUDF(col("encrypted")))
      .withColumn("decrypted_len", length($"decrypted"))

    encryptedSDS.show()

  }


}