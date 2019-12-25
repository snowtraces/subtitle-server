package org.xinyo.subtitle.util;

import lombok.extern.log4j.Log4j2;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author CHENG
 */
@Log4j2
public class InputStreamCache {
    /**
     * 将InputStream中的字节保存到ByteArrayOutputStream中。
     */
    private ByteArrayOutputStream byteArrayOutputStream = null;

    public long length;

    public InputStreamCache(InputStream inputStream) {
        this(inputStream, -1);
    }

    public InputStreamCache(InputStream inputStream, long byteLimit) {
        if (inputStream == null) {
            return;
        }
        byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        long lengthCount = 0;
        int len;
        try {
            while ((len = inputStream.read(buffer)) > -1) {
                byteArrayOutputStream.write(buffer, 0, len);

                lengthCount += len;
                if (byteLimit != -1 && lengthCount > byteLimit) {
                    break;
                }
            }

            this.length = lengthCount;
            byteArrayOutputStream.flush();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public InputStream getInputStream() {
        if (byteArrayOutputStream == null) {
            return null;
        }
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    public Charset getCharset() {
        try {
            byte[] buf = new byte[1024];
            UniversalDetector detector = new UniversalDetector(null);
            InputStream inputStream = getInputStream();

            int n;
            while ((n = inputStream.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, n);
            }
            detector.dataEnd();

            String encoding = detector.getDetectedCharset();
            detector.reset();

            if (encoding != null) {
                log.info("Detected encoding = " + encoding);
                return Charset.forName(encoding);
            } else {
                log.info("No encoding detected.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StandardCharsets.UTF_8;
    }

    public String getFixedLines(int lineNumber) {
        return String.join("\n", getFixedLineList(lineNumber));
    }

    public List<String> getFixedLineList(int lineNumber) {
        try {
            Charset charset = getCharset();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(getInputStream(), charset)
            );
            String line;
            List<String> lineList = new ArrayList<>();
            int idx = 0;
            while (
                    (line = br.readLine()) != null
                            && (lineNumber == -1 || idx < lineNumber)
            ) {
                idx++;
                lineList.add(line);
            }
            return lineList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getAllLines() {
        return getFixedLines(-1);
    }

    public List<String> getAllLineList() {
        return getFixedLineList(-1);
    }


}
