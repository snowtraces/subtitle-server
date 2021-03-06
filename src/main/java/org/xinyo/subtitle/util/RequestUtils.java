package org.xinyo.subtitle.util;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Supplier;

@Log4j2
public class RequestUtils {
    private static CookieStore cookieStore = new BasicCookieStore();
    private final static String USER_AGENT_CHROME = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36";

    private static final int RETRY_TIMES = 1;

    public static <T> T tryRequest(Supplier<T> supplier, String errorMsg) {
        int tryTimes = 0;
        T result = null;
        while (tryTimes++ < RETRY_TIMES && result == null) {
            if (tryTimes > 1) {
                log.warn(">>>请求失败，正在重试");
            }
            result = supplier.get();
        }
        if (result == null) {
            log.error("请求失败：{}", errorMsg);
        }
        return result;
    }

    public static String requestText(String url) {
        InputStream inputStream = request(url);
        return stream2Text(inputStream);
    }

    public static String requestText(String url, List<String> params, List<String> headers) {
        InputStream inputStream = requestPost(url, params, headers);
        return stream2Text(inputStream);
    }

    private static String stream2Text(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            return CharStreams.toString(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean fetchBinary(String url, String savePath) {
        return fetchBinary(url, savePath, null);
    }

    public static boolean fetchBinary(String rawUrl, String savePath, String fileName) {
        try {
            String url = urlEncode(rawUrl);

            if (Strings.isNullOrEmpty(fileName)) {
                fileName = getFileNameFromUrl(url);
            }

            savePath += File.separator + fileName;
            File file = new File(savePath);
            if (file.exists() && file.length() > 0) {
                log.info("文件已存在，不重复下载");
            } else {
                if (file.exists()) {
                    file.delete();
                }

                FileOutputStream outputStream = new FileOutputStream(file);
                InputStream in = tryRequest(() -> request(url), "请求失败 - " + url);
                if (in == null) {
                    return false;
                }

                ByteStreams.copy(in, outputStream);

                log.info(">>>文件保存成功：url: {}, file: {}", url, savePath);
                outputStream.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static String getFileNameFromUrl(String url) {
        String fileName;
        if (url.contains("?")) {
            fileName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?"));
        } else {
            fileName = url.substring(url.lastIndexOf("/") + 1);
        }

        // 文件名过长处理
        if (fileName.length() > 255) {
            fileName = Hashing.md5().hashString(fileName, Charsets.UTF_8).toString()
                    + fileName.substring(fileName.lastIndexOf("."));
        }
        return fileName;
    }

    public static String getSubFixFromUrl(String url) {
        String fileName = getFileNameFromUrl(url);
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * 请求
     *
     * @return
     */
    public static InputStream request(String url) {
        CloseableHttpClient httpClient = getHttpClient();
        CloseableHttpResponse response = null;
        try {
            HttpGet httpget = new HttpGet(url);
            response = httpClient.execute(httpget);

            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity.getContent();
            } else {
                log.error("Unexpected response status: " + status);
                log.error(url);
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return null;
        }
    }

    /**
     * 请求
     *
     * @return
     */
    public static InputStream requestPost(String url, List<String> params, List<String> headers) {
        CloseableHttpClient httpClient = getHttpClient();
        CloseableHttpResponse response = null;
        try {
            URIBuilder builder = new URIBuilder(url);
            HttpPost httpPost = new HttpPost(builder.build());

            // form_data
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            if (params != null && params.size() > 0) {
                for (String s : params) {
                    String[] split = s.split("[=:]");
                    entityBuilder.addPart(split[0], new StringBody(split[1], ContentType.TEXT_PLAIN));
                }
            }
            httpPost.setEntity(entityBuilder.build());

            // header
            if (headers != null && headers.size() > 0) {
                for (String s : headers) {
                    String[] split = s.split("[=:]");
                    httpPost.setHeader(split[0], split[1]);
                }
            }

            response = httpClient.execute(httpPost);

            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                return is;
            } else {
                log.error("Unexpected response status: " + status);
                log.error(url);
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return null;
        }
    }

    public static CloseableHttpClient getHttpClient() {

        RequestConfig config = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .setSocketTimeout(30000)
                .setConnectTimeout(30000)
                .setConnectionRequestTimeout(30000)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setDefaultRequestConfig(config)
                .setUserAgent(USER_AGENT_CHROME)
                .build();

        return httpClient;
    }

    private static String urlEncode(String url) throws UnsupportedEncodingException {
        return url.substring(0, url.lastIndexOf("/") + 1)
                + URLEncoder.encode(url.substring(url.lastIndexOf("/") + 1), "utf8")
                .replaceAll("\\+", "%20")
                .replaceAll("%3F", "?");
    }


    public static void main(String[] args) throws IOException {
        String url = "https://www.baeldung.com/wp-content/uploads/2018/08/RWS-Widget-Option2.jpg";
        InputStream inputStream = request(url);

        File img = new File("img.jpg");
        FileOutputStream outputStream = new FileOutputStream(img);

        ByteStreams.copy(inputStream, outputStream);
    }

}
