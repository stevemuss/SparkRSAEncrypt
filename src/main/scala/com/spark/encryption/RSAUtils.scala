package com.spark.encryption

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.security.{Key, KeyPair, KeyPairGenerator, Security}
import sun.misc.BASE64Decoder
import sun.misc.BASE64Encoder
import javax.crypto.Cipher

class RSAUtils() { // La ubicación predeterminada de almacenamiento de archivos persistentes de clave pública
  private var PUBLIC_KEY_FILE = "PublicKey"
  // La ubicación predeterminada de almacenamiento de archivos persistentes de clave privada
  private var PRIVATE_KEY_FILE = "PrivateKey"

  // Muestra la ubicación de las llaves de encriptado
  def showKeyPath(): Unit = {
    println(s"La llave pública se ubica en: $PUBLIC_KEY_FILE")
    println(s"La llave privada se ubica en: $PRIVATE_KEY_FILE")
  }

  // Establecer la ubicación de almacenamiento de los archivos persistentes de clave pública y privada
  def setKeyPath(path: String): Unit = {
    if (PUBLIC_KEY_FILE == "PublicKey") {
      PUBLIC_KEY_FILE = path + (if (path.endsWith("//")) "PublicKey.pem"
      else "/PublicKey.pem")
      PRIVATE_KEY_FILE = path + (if (path.endsWith("//")) "PrivateKey.pem"
      else "/PrivateKey.pem")
    }
  }

  // Crear un par de claves pública y privada
  def createKeyPair(): Unit = {
    Security.setProperty("crypto.policy", "unlimited")
    // Use el algoritmo RSA para obtener el objeto generador de pares de claves keyPairGenerator
    val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
    // Establece la longitud de la clave en 1024
    keyPairGenerator.initialize(16350)
    //keyPairGenerator.initialize(32400)
    // Generar par de claves
    val keyPair = keyPairGenerator.generateKeyPair
    // Obtener la clave pública
    val publicKey = keyPair.getPublic
    // Obtenga la clave privada
    val privateKey = keyPair.getPrivate
    // Guardar objetos de clave pública y privada como archivos persistentes
    /*var oos1 = null
    var oos2 = null*/
    try {
      val oos1 = new ObjectOutputStream(new FileOutputStream(PUBLIC_KEY_FILE))
      val oos2 = new ObjectOutputStream(new FileOutputStream(PRIVATE_KEY_FILE))
      oos1.writeObject(publicKey)
      oos2.writeObject(privateKey)
      oos1.close()
      oos2.close()
    } catch {
      case e: IOException =>
        throw new RuntimeException(e)
    }
  }

  // Cifrado RSA
  def encryptWithRSA(str: String): String = {
    // Leer el objeto de clave pública persistente
    val ois = new ObjectInputStream(new FileInputStream(PUBLIC_KEY_FILE))
    val publicKey = ois.readObject.asInstanceOf[Key]
    // Obtenga un objeto de cifrado cuyo algoritmo de cifrado sea RSA.
    val cipher = Cipher.getInstance("RSA")
    // Establecer en modo de cifrado y dar la clave pública para cifrar.
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    // Obtener el texto cifrado
    val secret = cipher.doFinal(str.getBytes)
    // codificación Base64
    new BASE64Encoder().encode(secret)
  }

  // Descifrado RSA
  def decryptWithRSA(secret: String): String = {
    // Leer el objeto de clave privada persistente
    val ois = new ObjectInputStream(new FileInputStream(PRIVATE_KEY_FILE))
    val privateKey = ois.readObject.asInstanceOf[Key]
    // Obtenga un objeto de cifrado cuyo algoritmo de descifrado sea RSA.
    val cipher = Cipher.getInstance("RSA")
    // Pase la clave privada y configúrelo en modo de descifrado.
    cipher.init(Cipher.DECRYPT_MODE, privateKey)
    // El descifrador descifra el texto cifrado decodificado por Base64 para obtener una matriz de bytes de texto sin formato
    val b = cipher.doFinal(new BASE64Decoder().decodeBuffer(secret))
    // Convertir a cadena
    new String(b)
  }

}