package com.michaelpolla.mikalib;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility classes for String manipulations.
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public class StringUtils {

    /**
     * Creates a filename based on the current date and time and with the extension provided.
     * Example : 2016-10-27-15-05-02.jpg
     * @param fileExt The file extension with separator, like : .jpg
     * @return The filename created.
     */
    public static String getDateTimeFilename(String fileExt) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String timestamp = dateFormat.format(new Date());
        return timestamp + fileExt;
    }

    /**
     * Converts bytes to their corresponding hexadecimal values.
     * @param in array of bytes.
     * @return the resulting value as a String.
     */
    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
