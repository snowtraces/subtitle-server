package org.xinyo.subtitle.task;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import com.google.common.base.Strings;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.xinyo.subtitle.entity.Subtitle;
import org.xinyo.subtitle.entity.SubtitleFile;
import org.xinyo.subtitle.service.SubtitleFileService;
import org.xinyo.subtitle.service.SubtitleService;
import org.xinyo.subtitle.util.FileUtils;
import org.xinyo.subtitle.util.SpringContextHolder;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@Log4j2
public class SubtitleFileThread implements Runnable, Serializable {
    private static final SubtitleFileService subtitleFileService = SpringContextHolder.getBean(SubtitleFileService.class);
    private static final SubtitleService subtitleService = SpringContextHolder.getBean(SubtitleService.class);
    private final int MAX_LINE = 30;

    private String subtitleId;
    private String basePath;
    private List<String> packageSuffixList = Arrays.asList("zip", "rar");
    private List<String> subtitleSuffixList = Arrays.asList("srt", "ass");

    public SubtitleFileThread(String subtitleId, String basePath) {
        this.subtitleId = subtitleId;
        this.basePath = basePath;
    }

    @Override
    public void run() {
        // 1. 读取字幕对象
        Subtitle subtitle = subtitleService.getById(subtitleId);
        if (subtitle == null) {
            return;
        }

        // 2. 读取字幕
        String fileName = subtitle.getFileName();
        String suffix = getSuffix(fileName);
        if (suffix == null || packageSuffixList.indexOf(suffix) == -1) {
            return;
        }

        List<String> idPath = FileUtils.separateString(subtitle.getSubjectId(), 1, 5);
        String path = FileUtils.createPath(basePath + "subtitles", idPath);
        path += fileName;

        System.err.println(path);

        // 3. 解压生成详情
        switch (suffix) {
            case "zip":
                unzip(path);
                break;
            case "rar":
                unrar(path);
                break;
            default:
                break;
        }
    }

    public boolean unrar(String rarFileName) {
        boolean flag = false;
        try {
            Archive archive = new Archive(new File(rarFileName), null);
            if (archive == null || archive.isEncrypted()) {
                return false;
            }

            int fileIdx = 0;
            List<FileHeader> files = archive.getFileHeaders();
            for (FileHeader fh : files) {
                if (fh.isEncrypted()) {
                    return false;
                }
                String fileName = fh.getFileNameW();
                if (Strings.isNullOrEmpty(fileName)) {
                    fileName = getRarFileName(fh);
                }
                StringBuilder builder = new StringBuilder();
                try {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(archive.getInputStream(fh)));
                    String line;
                    int idx = 0;
                    while ((line = br.readLine()) != null && idx < MAX_LINE) {
                        idx++;
                        builder.append(line).append("\n");
                    }
                } catch (RarException e) {
                    e.printStackTrace();
                }

                SubtitleFile subtitleFile = new SubtitleFile();
                subtitleFile.setSubtitleId(subtitleId);
                subtitleFile.setFileIndex(fileIdx);
                subtitleFile.setFileName(fileName);
                subtitleFile.setFileSize(String.valueOf(fh.getUnpSize()));
                subtitleFile.setContent(builder.toString());
                subtitleFileService.save(subtitleFile);

                fileIdx++;
            }
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    private String getRarFileName(FileHeader fileHeader) {
        try {
            Field fileName = FileHeader.class.getDeclaredField("fileName");
            fileName.setAccessible(true);
            return (String) fileName.get(fileHeader);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void unzip(String file) {
        try (ZipArchiveInputStream inputStream = getZipFile(new File(file))) {
            ZipArchiveEntry ze;
            int fileIdx = 0;
            BufferedReader br = null;
            while ((ze = inputStream.getNextZipEntry()) != null) {
                if (ze.isDirectory()) {
                } else {
                    String fileName = getFileNameWithoutFolder(ze.getName());
                    StringBuilder builder = new StringBuilder();
                    long size = ze.getSize();
                    String suffix = getSuffix(fileName);
                    if (size > 0
                            && suffix != null && subtitleSuffixList.indexOf(suffix) != -1) {
                        br = new BufferedReader(
                                new InputStreamReader(inputStream));
                        String line;
                        int idx = 0;
                        while ((line = br.readLine()) != null && idx < MAX_LINE) {
                            idx++;
                            builder.append(line).append("\n");
                        }
                    }
                    SubtitleFile subtitleFile = new SubtitleFile();
                    subtitleFile.setSubtitleId(subtitleId);
                    subtitleFile.setFileIndex(fileIdx);
                    subtitleFile.setFileName(fileName);
                    subtitleFile.setFileSize(String.valueOf(size));
                    subtitleFile.setContent(builder.toString());
                    subtitleFileService.save(subtitleFile);

                    fileIdx++;
                }
            }
            if (br != null) {
                br.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ZipArchiveInputStream getZipFile(File zipFile) throws Exception {
        return new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
    }


    private String getSuffix(String fileName) {
        String extension = null;
        if (Strings.isNullOrEmpty(fileName)) {
            return null;
        }

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }

    private String getFileNameWithoutFolder(String fullName) {
        if (Strings.isNullOrEmpty(fullName)) {
            return fullName;
        }

        int index = fullName.lastIndexOf("/");
        return fullName.substring(index + 1);
    }

}
