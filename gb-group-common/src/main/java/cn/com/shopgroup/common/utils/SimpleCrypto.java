package cn.com.shopgroup.common.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;


public class SimpleCrypto {


    private static final byte[] SECRET_KEY = "dianxiaotuan".getBytes(StandardCharsets.UTF_8);


    public static String encrypt(String data) {


        byte[] encrypted = xor(data.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(encrypted);
    }


    public static String decrypt(String encryptedData) {


        byte[] decoded = Base64.getDecoder().decode(encryptedData);

        byte[] decrypted = xor(decoded);
        return new String(decrypted, StandardCharsets.UTF_8);
    }


    private static byte[] xor(byte[] data) {

        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ SECRET_KEY[i % SECRET_KEY.length]);
        }
        return result;
    }

}
