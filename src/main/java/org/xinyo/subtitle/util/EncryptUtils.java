package org.xinyo.subtitle.util;

import com.google.common.base.Strings;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

/**
 * @author CHENG
 */
public class EncryptUtils {
    public static String baseEnc(String in) {
        if (Strings.isNullOrEmpty(in)) {
            throw new NullPointerException();
        }
        //noinspection UnstableApiUsage
        return Hashing.sha256().hashString(in, StandardCharsets.UTF_8).toString();
    }
}
