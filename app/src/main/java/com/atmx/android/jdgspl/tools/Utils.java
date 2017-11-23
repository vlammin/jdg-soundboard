/*
 * Utils
 * JDGSoundboard
 *
 * Copyright (c) 2017 Vincent Lammin
 */

package com.atmx.android.jdgspl.tools;

import java.text.Normalizer;
import java.util.Formatter;
import java.util.Locale;

public class Utils {

    /**
     * Convert a time in milliseconds to a mm:ss formatted string.
     *
     * @param msTime the time to convert in milliseconds.
     * @return the converted string.
     */
    public static String msTimeToString(int msTime) {
        int totalSeconds = msTime / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;

        StringBuilder builder = new StringBuilder();
        Formatter formatter = new Formatter(builder, Locale.getDefault());

        builder.setLength(0);
        return formatter.format("%02d:%02d", minutes, seconds).toString();
    }

    /**
     * Convert each non-ASCII characters of a string to their ASCII equivalent.
     * Faster than {@link org.apache.commons.lang3.StringUtils}.stripAccents()
     *
     * @param string the string to convert.
     * @return the converted string.
     */
    public static String toAscii(String string) {
        if (string == null)
            return null;

        char[] out = new char[string.length()];
        string = Normalizer.normalize(string, Normalizer.Form.NFKD);

        int j = 0;
        for (int i = 0, n = string.length(); i < n; ++i) {
            char c = string.charAt(i);
            if (c <= '\u007F') out[j++] = c;
        }

        return new String(out);
    }

    /**
     * Remove each invalid filename character of a string.
     *
     * @param string the input string.
     * @return the output string.
     */
    public static String safeFilename(String string) {
        if (string == null)
            return null;

        return string
            .replaceAll("[\\\\/:;,*!.?\"<>|]", "")
            .replaceAll("\\s+", " ")
            .trim();
    }
}
