package utils

import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec

object AESCrypt{

    //AES加密
    fun encrypt(input:String,password:String): String{

        //初始化cipher对象
        val cipher = Cipher.getInstance("AES")
        // 生成密钥
        val keySpec: SecretKeySpec? = SecretKeySpec(password.toByteArray(),"AES")
        cipher.init(Cipher.ENCRYPT_MODE,keySpec)
        //加密解密
        val encrypt = cipher.doFinal(input.toByteArray())
        val result = Base64.getEncoder().encode(encrypt)

        return String(result)
    }

    fun aesEncryptSimple(text: String): String {
        try {
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(128)
            val secretKey = keyGenerator.generateKey()

            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val encryptedBytes = cipher.doFinal(text.toByteArray())
            return Base64.getEncoder().encodeToString(encryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    fun aesDecrypt(encryptedText: String): String {
        try {
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(128)
            val secretKey = keyGenerator.generateKey()

            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)

            val encryptedBytes = Base64.getDecoder().decode(encryptedText)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    //AES解密
    fun decrypt(input: String,password: String): String{

        //初始化cipher对象
        val cipher = Cipher.getInstance("AES")
        // 生成密钥
        val keySpec:SecretKeySpec? = SecretKeySpec(password.toByteArray(),"AES")
        cipher.init(Cipher.DECRYPT_MODE,keySpec)
        //加密解密
        val encrypt = cipher.doFinal(Base64.getDecoder().decode(input.toByteArray()))
        //AES解密不需要用Base64解码
        val result = String(encrypt)

        return result
    }


}

fun test() {
    val password = "1234567887654321"//密钥
    val input = "AES加密解密测试"

    val encrypt = AESCrypt.encrypt(input, password)
    val decrypt = AESCrypt.decrypt(encrypt, password)

    println("AES加密结果:"+encrypt)
    println("AES解密结果:"+decrypt)
}