package ink.usr.common.core.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * AES加密工具类
 * 用于对敏感数据如UUID进行加密和解密
 */
public class AESUtil {
    
    /**
     * AES加密算法
     */
    private static final String AES_ALGORITHM = "AES";
    
    /**
     * AES加密密钥 - 在生产环境中应该从配置文件或环境变量中获取
     */
    private static final String SECRET_KEY = "UsrInkUUIDSecKey"; // 16字节密钥
    
    /**
     * 获取AES密钥
     * @return SecretKey
     */
    private static SecretKey getSecretKey() {
        // 将密钥字符串转换为16字节
        byte[] keyBytes = new byte[16];
        byte[] secretBytes = SECRET_KEY.getBytes();
        System.arraycopy(secretBytes, 0, keyBytes, 0, Math.min(secretBytes.length, keyBytes.length));
        return new SecretKeySpec(keyBytes, AES_ALGORITHM);
    }
    
    /**
     * AES加密
     * @param plainText 明文
     * @return 加密后的Base64编码字符串
     */
    public static String encrypt(String plainText) {
        try {
            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("AES加密失败", e);
        }
    }
    
    /**
     * AES解密
     * @param encryptedText 加密后的Base64编码字符串
     * @return 解密后的明文
     */
    public static String decrypt(String encryptedText) {
        try {
            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("AES解密失败", e);
        }
    }
} 