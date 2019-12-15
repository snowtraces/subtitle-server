package org.xinyo.subtitle.util;


import org.xinyo.subtitle.entity.SubtitleFile;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author CHENG
 */
public class RarUtils {
    public static List<SubtitleFile> getFileList(String file) {
        File rarFile = new File(file);

        // 获取WinRAR.exe的路径，放在java web工程下的WebRoot路径下
        String winrarPath = "C:\\Users\\CHENG\\CODE\\Projects\\subtitle_server\\src\\main\\resources\\rar\\win\\UnRAR.exe";
        if (!rarFile.exists()) {
            return null;
        }

        String cmd = winrarPath + " v " + rarFile;
        System.out.println(cmd);
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            InputStream inputStream = proc.getInputStream();
            InputStreamCache inputStreamCache = new InputStreamCache(inputStream);
            Charset charset = inputStreamCache.getCharset();


            String output = FileUtils.readInputStream(inputStreamCache.getInputStream(), charset);

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
            return subtitleFileList;

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
