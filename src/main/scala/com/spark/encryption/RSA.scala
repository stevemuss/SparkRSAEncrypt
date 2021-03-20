package com.spark.encryption

import java.security.KeyPairGenerator
import sun.misc.BASE64Encoder
import sun.misc.BASE64Decoder
import java.security.KeyPair
import javax.crypto.Cipher

/*Docs:
* https://programmerclick.com/article/7911127530/
* https://gist.github.com/urcadox/6173812
* https://stackoverflow.com/questions/11410770/load-rsa-public-key-from-file
* https://mvnrepository.com/artifact/org.bouncycastle/bcprov-jdk15on/1.68
* http://www.bouncycastle.org/documentation.html
*/
object RSA {

  def main(args: Array[String]): Unit = {

    /* Se generan las llaves */

    // Use el algoritmo RSA para obtener el objeto generador de pares de claves keyPairGenerator
    val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")
    // Establece la longitud de la clave en 1024
    keyPairGenerator.initialize(1024)
    // Generar par de claves
    val keyPair: KeyPair = keyPairGenerator.generateKeyPair
    // Obtener la clave pública
    val publicKey = keyPair.getPublic
    println(publicKey)
    // Obtenga la clave privada
    val privateKey = keyPair.getPrivate
    println(privateKey)

    /* Ciframos un string */

    // Obtenga un objeto de cifrado cuyo algoritmo de cifrado sea RSA.// Obtenga un objeto de cifrado cuyo algoritmo de cifrado sea RSA.

    val secret = {
      val cipher = Cipher.getInstance("RSA")
      // Establecer en modo de cifrado y dar la clave pública para cifrar.
      cipher.init(Cipher.ENCRYPT_MODE, publicKey)
      // Obtener el texto cifrado
      val encword = cipher.doFinal("Hola mundo".getBytes)
      // codificación Base64 y retorno
      new BASE64Encoder().encode(encword)
      //encword
    }
    println("Palabra secreta")
    println(secret)

    /* Desciframos un string */

    val message = {
      val cipher = Cipher.getInstance("RSA")
      // Pase la clave privada y configúrelo en modo de descifrado.
      cipher.init(Cipher.DECRYPT_MODE, privateKey)
      // El descifrador descifra el texto cifrado decodificado por Base64 para obtener una matriz de bytes de texto sin formato
      val decword = cipher.doFinal(new BASE64Decoder().decodeBuffer(secret))
      //val b = cipher.doFinal(secret)
      // Convertir a cadena
      new String(decword)
    }
    println("Frase real")
    println(message)

  }

}