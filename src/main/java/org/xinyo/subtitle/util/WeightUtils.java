package org.xinyo.subtitle.util;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权重计算工具类
 *
 * @author CHENG
 */
public class WeightUtils {
    public static int DEFAULT_WEIGHT = 311;

    private static Map<String, Integer> SOURCE_TYPE = new ImmutableMap.Builder<String, Integer>()
            .put("bluray", 1).put("blu-ray", 1).put("bdrip", 1).put("bd-rip", 1)
            .put(".web.", 2).put("webdl", 2).put("web-dl", 2).put("webrip", 2).put("web-rip", 2)
            .put("hdrip", 3).put("hd-rip", 3).put("dvdrip", 3).put("dvd-rip", 3)
            .put("dvdscr", 4)
            .put("ts", 5).put("hdcam", 5)
            .build();
    private static Map<String, Integer> SPECIAL_TYPE = new ImmutableMap.Builder<String, Integer>()
            .put("extended", 0).put("remastered", 0).put("imax", 0)
            .build();
    private static Map<String, Integer> ENCODE_TYPE = new ImmutableMap.Builder<String, Integer>()
            .put("h265", 0).put("x265", 0)
            .put("h264", 1).put("x264", 1)
            .put("xvid", 2).put("divx", 2)
            .build();

    private static int getWeightOfSource(List<String> inList) {
        for (String in : inList) {
            for (String key : SOURCE_TYPE.keySet()) {
                if (in.contains(key)) {
                    return SOURCE_TYPE.get(key);
                }
            }
        }
        return 3;
    }

    private static int getWeightOfSpecial(List<String> inList) {
        for (String in : inList) {
            for (String key : SPECIAL_TYPE.keySet()) {
                if (in.contains(key)) {
                    return SPECIAL_TYPE.get(key);
                }
            }
        }
        return 1;
    }

    private static int getWeightOfEncode(List<String> inList) {
        for (String in : inList) {
            for (String key : ENCODE_TYPE.keySet()) {
                if (in.contains(key)) {
                    return ENCODE_TYPE.get(key);
                }
            }
        }
        return 1;
    }

    public static int getWeight(String... in) {
        if (in.length == 0) {
            return DEFAULT_WEIGHT;
        }
        return getWeight(Arrays.asList(in));
    }

    public static int getWeight(List<String> inList) {
        if (inList == null || inList.size() == 0) {
            return DEFAULT_WEIGHT;
        }

        inList = inList.stream()
                .filter(x -> !Strings.isNullOrEmpty(x))
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        if (inList.size() == 0) {
            return DEFAULT_WEIGHT;
        }

        int weightOfSource = getWeightOfSource(inList);
        int weightOfSpecial = getWeightOfSpecial(inList);
        int weightOfEncode = getWeightOfEncode(inList);

        return weightOfSource * 100 + weightOfSpecial * 10 + weightOfEncode;
    }

    public static void main(String[] args) {
        List<String> inList = Arrays.asList(
                "Interstellar.IMAX.2014.Bluray.720p.x264.AC3.2Audios-CMCT".toLowerCase(),
                "interstellar.imax.2014.bluray.720p.x264.ac3.2audios-cmct.简体&英文.ass".toLowerCase(),
                "interstellar.imax.2014.bluray.720p.x264.ac3.2audios-cmct.简体.ass".toLowerCase()
        );

        int weight = getWeight(inList);
        System.err.println(weight);
    }


}
