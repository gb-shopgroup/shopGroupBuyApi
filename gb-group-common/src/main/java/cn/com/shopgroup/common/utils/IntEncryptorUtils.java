package cn.com.shopgroup.common.utils;

import java.util.Base64;

public class IntEncryptorUtils {


    private static final int KEY = 0x5F3A7C9D;


    public static String encrypt(int num) {

        int encrypted = num ^ KEY;

        byte[] bytes = new byte[4];
        bytes[0] = (byte) (encrypted >> 24);
        bytes[1] = (byte) (encrypted >> 16);
        bytes[2] = (byte) (encrypted >> 8);
        bytes[3] = (byte) encrypted;


        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }


    public static int decrypt(String str) {

        byte[] bytes = Base64.getUrlDecoder().decode(str);

        int encrypted = ((bytes[0] & 0xFF) << 24)
                | ((bytes[1] & 0xFF) << 16)
                | ((bytes[2] & 0xFF) << 8)
                | (bytes[3] & 0xFF);

        return encrypted ^ KEY;
    }

}
