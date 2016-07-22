package com.yoho.yhorder.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Created by yoho on 2016/3/22.
 */
public class ImageUtils {

    private static final List<String> YOHOOD_QRCODE_DOMAINS;

    static {
        YOHOOD_QRCODE_DOMAINS = new ArrayList<>();
        YOHOOD_QRCODE_DOMAINS.add("http://img03.static.yhbimg.com");
        YOHOOD_QRCODE_DOMAINS.add("http://img04.static.yhbimg.com");
        YOHOOD_QRCODE_DOMAINS.add("http://img05.static.yhbimg.com");
        YOHOOD_QRCODE_DOMAINS.add("http://img06.static.yhbimg.com");
    }

    public static String yohoodQrcode(String type, String dataStr, int version, int boxSize) {
        return yohoodQrcode(type, dataStr, version, boxSize, 1, 1);
    }

    private static String yohoodQrcode(String type, String dataStr, int version, int boxSize, int border, int color) {
        String data = type + "-" + dataStr;
        int nodeNum = (int) (crc32(data) % YOHOOD_QRCODE_DOMAINS.size());
        int filePath = (int) (Math.abs(crc32(data)) % 20000);
        return new StringBuffer()
                .append(YOHOOD_QRCODE_DOMAINS.get(nodeNum))
                .append("/yohood/")
                .append(filePath)
                .append("/")
                .append(data)
                .append("-")
                .append(version)
                .append("-")
                .append(boxSize)
                .append("-")
                .append(border)
                .append("-")
                .append(color)
                .append(".jpg")
                .toString();
    }

    private static long crc32(String data) {
        CRC32 crc32 = new CRC32();
        crc32.update(data.getBytes());
        return crc32.getValue();
    }

}
