package com.android.printApis;

public class Utils {
    public static byte[] byteMergeAll(byte[]... value) {
        int length_byte = 0;
        for (int i = 0; i < value.length; i++) {
            length_byte += value[i].length;
        }
        byte[] all_byte = new byte[length_byte];
        int countLength = 0;
        for (int i = 0; i < value.length; i++) {
            byte[] b = value[i];
            System.arraycopy(b, 0, all_byte, countLength, b.length);
            countLength += b.length;
        }
        return all_byte;
    }

    //byte[]转string后再转byte[]
    public static byte[] byteStringTobyte(String bstr) {
        String[] sa = bstr.substring(1, bstr.length() - 1).split(", ");
        byte[] barr = new byte[sa.length];
        try {
            for (int i = 0; i < barr.length; i++) {
                barr[i] = Byte.parseByte(sa[i]);
            }
        } catch (Exception e) {
            return null;
        }
        return barr;
    }
}