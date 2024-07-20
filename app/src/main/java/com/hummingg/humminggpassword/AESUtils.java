package com.hummingg.humminggpassword;

import android.os.Build;

import androidx.annotation.RequiresApi;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESUtils {

    private static final String ALGORITHM = "AES";

    // 生成随机密钥
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(128); // AES 128-bit
        return keyGenerator.generateKey();
    }

    // 从字节数组中生成密钥
    public static SecretKey getKeyFromPassword(String password) throws Exception {
        byte[] keyBytes = new byte[16]; // 128 bits
        System.arraycopy(password.getBytes(), 0, keyBytes, 0, Math.min(password.length(), keyBytes.length));
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    // 加密数据
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String encrypt(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // 解密数据
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String decrypt(String encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }
}
