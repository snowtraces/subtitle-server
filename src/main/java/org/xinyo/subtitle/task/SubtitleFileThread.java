package org.xinyo.subtitle.task;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import com.google.common.base.Strings;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.xinyo.subtitle.entity.Subtitle;
import org.xinyo.subtitle.entity.SubtitleFile;
import org.xinyo.subtitle.service.SubtitleFileService;
import org.xinyo.subtitle.service.SubtitleService;
import org.xinyo.subtitle.util.FileUtils;
import org.xinyo.subtitle.util.InputStreamCache;
import org.xinyo.subtitle.util.SpringContextHolder;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Log4j2
public class SubtitleFileThread implements Runnable, Serializable {
    private static final SubtitleFileService subtitleFileService = SpringContextHolder.getBean(SubtitleFileService.class);
    private static final SubtitleService subtitleService = SpringContextHolder.getBean(SubtitleService.class);
    private final int MAX_LINE = 30;

    private String subtitleId;
    private String basePath;
    private List<String> packageSuffixList = Arrays.asList("zip", "rar", "7z");
    private List<String> subtitleSuffixList = Arrays.asList("srt", "ass", "txt");

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

        // 3. 解压生成详情
        switch (suffix) {
            case "7z":
                un7z(path);
                break;
            case "zip":
                unZip(path);
                break;
            case "rar":
                unRar(path);
                break;
            default:
                break;
        }
    }


    private void un7z(String file) {
        try {
            SevenZFile sevenZFile = new SevenZFile(new File(file));
            SevenZArchiveEntry ze;
            int fileIdx = 0;
            while ((ze = sevenZFile.getNextEntry()) != null) {
                if (ze.isDirectory()) {
                    continue;
                }
                String fileName = ze.getName();
                long size = ze.getSize();
                trySaveSubtitleFile(fileIdx, fileName, size, () -> {
                    byte[] content = new byte[(int) size];
                    try {
                        sevenZFile.read(content, 0, content.length);
                        return new ByteArrayInputStream(content);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                });

                fileIdx++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void unRar(String rarFileName) {
        try {
            Archive archive = new Archive(new File(rarFileName), null);
            if (archive.isEncrypted()) {
                return;
            }

            int fileIdx = 0;
            List<FileHeader> files = archive.getFileHeaders();
            for (FileHeader fh : files) {
                if (fh.isEncrypted() || fh.isDirectory()) {
                    continue;
                }

                String fileName = fh.getFileNameW();
                if (Strings.isNullOrEmpty(fileName)) {
                    fileName = getRarFileName(fh);
                }
                long size = fh.getUnpSize();

                trySaveSubtitleFile(fileIdx, fileName, size, () -> {
                    try {
                        return archive.getInputStream(fh);
                    } catch (RarException e) {
                        e.printStackTrace();
                        return null;
                    }
                });

                fileIdx++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void unZip(String file) {
        try (ZipArchiveInputStream inputStream = getZipFile(new File(file))) {
            ZipArchiveEntry ze;
            int fileIdx = 0;
            while ((ze = inputStream.getNextZipEntry()) != null) {
                if (ze.isDirectory()) {
                    continue;
                }
                String fileName = ze.getName();
                long size = ze.getSize();

                trySaveSubtitleFile(fileIdx, fileName, size, () -> inputStream);

                fileIdx++;
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
        if (index == -1) {
            index = fullName.lastIndexOf("\\");
        }
        return fullName.substring(index + 1).toLowerCase();
    }

    private void trySaveSubtitleFile(int fileIdx, String fileName, long size, Supplier<InputStream> streamSupplier) {
        try {
            fileName = getFileNameWithoutFolder(fileName);
            String suffix = getSuffix(fileName);
            StringBuilder builder = new StringBuilder();
            if (size > 0 && suffix != null && subtitleSuffixList.indexOf(suffix) != -1) {
                // 缓存inputStream
                InputStream inputStream = streamSupplier.get();
                InputStreamCache cache = new InputStreamCache(inputStream);
                Charset charset = cache.getCharset();
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(cache.getInputStream(), charset)
                );
                String line;
                int idx = 0;
                while ((line = br.readLine()) != null && idx < MAX_LINE) {
                    idx++;
                    builder.append(line).append("\n");
                }
            }

            saveSubtitleFile(fileIdx, fileName, String.valueOf(size), builder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        private void saveSubtitleFile(int fileIdx, String fileName, String size, String content) {
        SubtitleFile subtitleFile = new SubtitleFile();
        subtitleFile.setSubtitleId(subtitleId);
        subtitleFile.setFileIndex(fileIdx);
        subtitleFile.setFileName(fileName);
        subtitleFile.setFileSize(String.valueOf(size));
        subtitleFile.setContent(content);
        subtitleFileService.save(subtitleFile);
    }

}
