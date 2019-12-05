package org.xinyo.subtitle.util;

import com.google.common.io.Files;
import lombok.extern.log4j.Log4j2;
import org.xinyo.subtitle.entity.UploadFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Log4j2
public class FileUtils {
    private static String basePath = "/data/";
    private static String[] textSuffix = new String[]{"srt", "ass", "log", "text"};
    private static String[] subtitleSuffix = new String[]{"srt", "ass"};


    public static String createPath(String basePath, List<String> idPath){
        return mkdirs(basePath, idPath);
    }

    public static List<String> generatePath() {
        String[] path = new String[4];
        LocalDateTime now = LocalDateTime.now();

        path[0] = String.valueOf(now.getYear());
        path[1] = String.valueOf(now.getMonthValue());
        path[2] = String.valueOf(now.getDayOfMonth());
        path[3] = String.valueOf(now.getSecond() % 10);

        return Arrays.asList(path);
    }

    public static String mkdirs(String basePath, List<String> path) {
        if (basePath.endsWith("/")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        String filePath = basePath + File.separator;
        for (String s : path) {
            filePath += s + File.separator;
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        }

        return filePath;
    }

    public static UploadFile createFullPath(String basePath, String fileName) {
        List<String> path = generatePath();
        String filePath = mkdirs(basePath, path);

        Long id = SnowFlake.getId();
        String suffix = Files.getFileExtension(fileName);
        String newName = id + "." + suffix;

        UploadFile uploadFile = new UploadFile();
        uploadFile.setFileId(SnowFlake.getId());
        uploadFile.setSourceName(fileName);
        uploadFile.setFileName(newName);
        uploadFile.setFullPath(filePath);

        return uploadFile;
    }

    public static UploadFile createFullPath(String fileName) {
        return createFullPath(basePath, fileName);
    }

    public static boolean isAsciiText(File file) throws IOException {

        /**
         * 通过后缀判断
         */
        String name = file.getName();
        String suffix = Files.getFileExtension(name);
        if (suffix != null && Arrays.asList(textSuffix).contains(suffix)) {
            return true;
        }

        /**
         * 通过控制字符判断
         */
        try (InputStream in = new FileInputStream(file)) {
            byte[] bytes = new byte[50];

            in.read(bytes, 0, bytes.length);
            short bin = 0;
            int i = 0;

            for (byte thisByte : bytes) {
                i++;
                if ((thisByte < 32 || thisByte > 126)
                        && thisByte != 8 && thisByte != 9 && thisByte != 10 && thisByte != 13) {
                    bin++;
                }
                if (bin >= 5) {
                    log.error(i);
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isSubtitle(File file) {
        String suffix = Files.getFileExtension(file.getName());
        if (suffix != null && Arrays.asList(subtitleSuffix).contains(suffix)) {
            return true;
        }
        return false;
    }

    /**
     * 分割字符串
     * @param in 输入
     * @param len 分割长度
     * @param steps 步数
     */
    public static List<String> separateString(String in, int len, int steps ) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < steps; i++) {
            list.add(in.substring(i * len, (i + 1) * len));
        }

        return list;
    }

    public static void main(String[] args) {
        String s = "ABC123XYZ99999";
        List<String> strings = separateString(s, 3, 3);

        log.error(strings);
    }
}
