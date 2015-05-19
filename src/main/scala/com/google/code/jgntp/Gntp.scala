package com.google.code.jgntp


import java.awt.image.RenderedImage
import java.net.URI
import java.security.spec.KeySpec
import javax.crypto.{Cipher, SecretKeyFactory}
import javax.crypto.spec.{IvParameterSpec, DESKeySpec}

import com.google.code.jgntp.internal.Priority.Priority
import java.security._
import com.google.code.jgntp.internal.message.HeaderValue
import com.google.code.jgntp.internal.message.write.EncryptedGntpMessageWriter
import com.google.code.jgntp.util.Hex
import com.google.common.base.Charsets

case class GntpApplicationInfo(name: String,
                               icon: Option[Either[URI, RenderedImage]],
                               notificationInfos: Seq[GntpNotificationInfo])

case class GntpNotificationInfo(name: String,
                                icon: Option[Either[URI, RenderedImage]],
                                displayName: Option[String] = None,
                                enabled: Boolean = true)

case class GntpNotification(applicationName: String,
                            name: String,
                            title: String,
                            text: Option[String],
                            callbackTarget: Option[URI] = None,
                            headers: Map[String, HeaderValue],
                            icon: Option[Either[URI, RenderedImage]] = None,
                            id: Option[String] = None,
                            sticky: Option[Boolean] = None,
                            priority: Option[Priority] = None,
                            context: Option[AnyRef] = None, //TODO remove
                            coalescingId: Option[String] = None)




object GntpPassword {
  val DEFAULT_RANDOM_SALT_ALGORITHM: String = "SHA1PRNG"
  val DEFAULT_SALT_SIZE: Int = 16
  val DEFAULT_KEY_HASH_ALGORITHM: String = "SHA-512"
  val DEFAULT_ALGORITHM: String = "DES"
  val DEFAULT_TRANSFORMATION: String = "DES/CBC/PKCS5Padding"
  val NONE_ENCRYPTION_ALGORITHM: String = "NONE"
}

case class GntpPassword(textPassword: String = "",
                        encrypted: Boolean = false,
                        hashAlgorithm: String = GntpPassword.DEFAULT_KEY_HASH_ALGORITHM,
                        randomSaltAlgorithm: String = GntpPassword.DEFAULT_RANDOM_SALT_ALGORITHM) {
  private val _salt: Seq[Byte] = getSalt
  private val _key = hash(textPassword.getBytes(Charsets.UTF_8).toSeq ++ _salt)
  private val _secretKey = SecretKeyFactory.getInstance(GntpPassword.DEFAULT_ALGORITHM).generateSecret(new DESKeySpec(_key.toArray))
  val salt: String = Hex.toHexadecimal(_salt.toArray)
  val keyHash: String = Hex.toHexadecimal(hash(_key).toArray)
  val keyHashAlgorithm: String = hashAlgorithm.replaceAll("-", "")
  val iv = new IvParameterSpec(_secretKey.getEncoded)
  private val _cipher = Cipher.getInstance(GntpPassword.DEFAULT_TRANSFORMATION)
  _cipher.init(Cipher.ENCRYPT_MODE, _secretKey, iv)

  protected def getSeed: Long = System.currentTimeMillis()

  def encrypt(in: Array[Byte]) = if(encrypted) _cipher.doFinal(in) else in

  def getEncryptionSpec = if(encrypted) GntpPassword.DEFAULT_ALGORITHM + ':' + Hex.toHexadecimal(iv.getIV) else GntpPassword.NONE_ENCRYPTION_ALGORITHM

  private def getSalt: Seq[Byte] = {
    val  random = SecureRandom.getInstance(randomSaltAlgorithm)
    random.setSeed(getSeed)
    val saltArray: Array[Byte] = new Array[Byte](GntpPassword.DEFAULT_SALT_SIZE)
    random.nextBytes(saltArray)
    saltArray.toSeq
  }


  private def hash(keyToUse: Seq[Byte]): Seq[Byte] = {
      MessageDigest.getInstance(hashAlgorithm).digest(keyToUse.toArray).toSeq
  }
}




