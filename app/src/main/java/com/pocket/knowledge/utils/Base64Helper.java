package com.pocket.knowledge.utils;


import android.util.Base64;

import java.io.UnsupportedEncodingException;

public class Base64Helper {

    private static final byte[] keyValue =
            new byte[]{'k', 'n', 'o', 'w', 'l', 'e', 'd', 'g', 'e', 'b', 'a', 'n', 'k'};


    public static String encrypt(String text) throws UnsupportedEncodingException {
        byte[] data = text.getBytes("UTF-8");
        String base64 = Base64.encodeToString(data, Base64.DEFAULT);
        return  base64;
    }

    public static String decrypt(String base64) throws UnsupportedEncodingException {
        byte[] data = Base64.decode(base64, Base64.DEFAULT);
        String text = new String(data, "UTF-8");
        return text;

    }





}
