package com.spark.encryption

object RSAUtilsTest {

  def main(args: Array[String]): Unit ={
    import javax.crypto.Cipher
    val maxKeyLength = Cipher.getMaxAllowedKeyLength("RSA")
    println(maxKeyLength)

    val encryptor = new RSAUtils()
    encryptor.setKeyPath("[YOUR PROJECT PATH]")
    // encryptor.createKeyPair()
    encryptor.showKeyPath()
    val secret = encryptor.encryptWithRSA("Hola")
    println("Palabra secreta:")
    println(secret)
    val str = encryptor.decryptWithRSA(secret)
    println("Palabra secreta para humanos:")
    println(str)
  }

}