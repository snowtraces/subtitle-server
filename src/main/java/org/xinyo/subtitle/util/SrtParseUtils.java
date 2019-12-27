package org.xinyo.subtitle.util;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import org.xinyo.subtitle.entity.SRTSubtitleUnit;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author CHENG
 */
public class SrtParseUtils {
    private static List<String> subtitleSuffix = Arrays.asList("srt", "ass", "ssa");

    private static String numberRegex = "^\\d+$";
    private static String timeRegex = "(^\\d{2}:\\d{2}:\\d{2},\\d{3})[^0-9]+(\\d{2}:\\d{2}:\\d{2},\\d{3}$)";
    private static Pattern timePattern = Pattern.compile(timeRegex);

    public static List<SRTSubtitleUnit> read(InputStream inputStream) {
        return readSubtitle(new InputStreamCache(inputStream).getAllLineList());
    }

    public static List<SRTSubtitleUnit> readSubtitle(List<String> lines) {

        List<SRTSubtitleUnit> list = new ArrayList<>();

        boolean isBegin = true;
        SRTSubtitleUnit unit = new SRTSubtitleUnit();
        for (String line : lines) {

            // 1. 新建单元
            if (isBegin && !Strings.isNullOrEmpty(line)) {
                unit = new SRTSubtitleUnit();
            }

            line = line.trim();
            // 2. 存入数据
            if ((line.matches(numberRegex) || line.startsWith("\uFEFF")) && isBegin) {
                // a. 数字
                unit.setNumber(Integer.valueOf(line.replaceAll("\uFEFF", "")));
            } else if (line.matches(timeRegex)) {
                // b. 时间
                int[] time = buildTime(line);
                unit.setStart(time[0]);
                unit.setEnd(time[1]);
            } else {
                // 3. 文本
                if (!Strings.isNullOrEmpty(line)) {
                    unit.getText().add(line);
                }
            }

            if (Strings.isNullOrEmpty(line)) {
                list.add(unit);
                isBegin = true;
            } else {
                isBegin = false;
            }
        }

        return list;
    }

    private static int[] buildTime(String line) {
        Matcher matcher = timePattern.matcher(line);
        if (matcher.find()) {
            int start = calcTime(matcher.group(1));
            int end = calcTime(matcher.group(2));

            return new int[]{start, end};
        }
        throw new RuntimeException("时间格式异常");
    }

    private static int calcTime(String time) {
        String[] split = time.split("[:,]");
        int hour = Integer.parseInt(split[0]);
        int min = Integer.parseInt(split[1]);
        int sec = Integer.parseInt(split[2]);
        int mill = Integer.parseInt(split[3]);

        return hour * 60 * 60 * 1000 + min * 60 * 1000 + sec * 1000 + mill;
    }


    public static boolean isSubtitle(File file) {
        String suffix = Files.getFileExtension(file.getName());
        if (subtitleSuffix.contains(suffix)) {
            return true;
        }
        return false;
    }

}
