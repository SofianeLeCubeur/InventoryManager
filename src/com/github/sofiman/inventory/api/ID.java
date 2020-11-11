package com.github.sofiman.inventory.api;

import java.util.concurrent.ThreadLocalRandom;

public class ID {

    public static byte[] fromHex(String hexString){
        int length = (int) Math.ceil(hexString.length()/2f);
        byte[] buf = new byte[length];
        int j = 0;
        for (int i = 0; i < hexString.length(); i += 2) {
            String b = hexString.substring(i, i+2);
            buf[j++] = (byte)(Integer.parseInt(b, 16) & 0xff);
        }
        return buf;
    }

    public static String toHex(byte[] id){
        StringBuilder sb = new StringBuilder(id.length * 2);
        for(byte b : id)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static byte[] generateKeyed(byte key, int length){
        byte[] buf = new byte[length+1];
        ThreadLocalRandom.current().nextBytes(buf);
        buf[0] = key;
        return buf;
    }
}
