package org.xinyo.subtitle.util;


import org.xinyo.subtitle.entity.SubtitleFile;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author CHENG
 */
public class RarUtils {

    private static final int TOP_REMOVE_LINE = 8;
    private static final int MAX_LINE = 30 + TOP_REMOVE_LINE + 1;
    private static List<String> subtitleSuffixList = Arrays.asList("srt", "ass", "ssa", "txt");


    private static String WIN_RAR_PATH;

    public static void initRarPath(String rarPath) {
        RarUtils.WIN_RAR_PATH = rarPath;
    }

    public static List<SubtitleFile> getFileList(String file) {
        File rarFile = new File(file);
        if (!rarFile.exists()) {
            return null;
        }

        String cmd = WIN_RAR_PATH + " v " + rarFile;
        System.out.println(cmd);
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            InputStream inputStream = proc.getInputStream();
            InputStreamCache inputStreamCache = new InputStreamCache(inputStream);
            String output = inputStreamCache.getAllLines();
            String[] lines = output.split("\n");

            boolean startFileList = false;
            List<String> fileList = new ArrayList<>();
            for (String line : lines) {
                if (line.startsWith("----")) {
                    startFileList = !startFileList;
                    continue;
                }

                if (startFileList) {
                    fileList.add(line);
                }
            }

            List<SubtitleFile> subtitleFileList = new ArrayList<>();
            for (String fileInfo : fileList) {
                fileInfo = fileInfo.trim();
                String[] fileAttrs = fileInfo.split(" +");
                String fileType = fileAttrs[0];
                String fileSize = fileAttrs[1];
                StringBuilder fileName = new StringBuilder(fileAttrs[7]);
                if (fileAttrs.length > 7) {
                    for (int i = 8; i < fileAttrs.length; i++) {
                        fileName.append(fileAttrs[i]);
                    }
                }

                if ("..A....".equals(fileType)) {
                    SubtitleFile subtitleFile = new SubtitleFile();
                    subtitleFile.setFileName(fileName.toString());
                    subtitleFile.setFileSize(fileSize);

                    subtitleFileList.add(subtitleFile);
                }
            }

            subtitleFileList.forEach(subtitleFile -> {
                String suffix = FileUtils.getSuffix(subtitleFile.getFileName());
                if (subtitleSuffixList.indexOf(suffix) != -1) {
                    String fileInfo = getFilePreview(file, subtitleFile.getFileName());
                    subtitleFile.setContent(fileInfo);
                }
            });
            return subtitleFileList;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getFilePreview(String file, String fileName) {
        File rarFile = new File(file);
        if (!rarFile.exists()) {
            return null;
        }

        String cmd = String.format("%s p %s %s", WIN_RAR_PATH, rarFile, fileName);
        System.out.println(cmd);
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            InputStream inputStream = proc.getInputStream();

            InputStreamCache cache = new InputStreamCache(inputStream, 1024);
            String fixedLines = cache.getFixedLines(MAX_LINE);
            String[] lineArray = fixedLines.split("\n");

            StringBuilder builder = new StringBuilder();
            for (int i = TOP_REMOVE_LINE; i < lineArray.length; i++) {
                if (i == TOP_REMOVE_LINE) {
                    lineArray[i] = lineArray[i].substring(lineArray[i].indexOf("%") + 1);
                }
                builder.append(lineArray[i]).append("\n");
            }
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void main(String[] args) {

        String file = "C:\\Users\\CHENG\\Downloads\\91744364061134848.rar";
        String dist = "C:\\Users\\CHENG\\Downloads\\testRar";

        List<SubtitleFile> fileList = getFileList(file);
        System.err.println(fileList);
    }


}
