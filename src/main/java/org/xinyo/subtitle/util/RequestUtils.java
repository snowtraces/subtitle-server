package org.xinyo.subtitle.util;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;

public class RequestUtils {
    static CookieStore cookieStore = new BasicCookieStore();
    public static String USER_AGENT_CHROME = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36";

    public static String requestText(String url) {
        InputStream inputStream = request(url);
        try {
            return CharStreams.toString(new InputStreamReader(inputStream, "utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void fetchBinary(String url, String savePath) {
        String fileName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?"));
        savePath += File.separator + fileName;

        File file = new File(savePath);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            ByteStreams.copy(request(url), outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 请求
     * @return
     */
    public static InputStream request(String url) {
        CloseableHttpClient httpClient = getHttpClient();

        HttpGet httpget = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpget);

            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                return is;
            } else {
                System.err.println("Unexpected response status: " + status);
                return  null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            if(response != null){
                try {
                    response.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return null;
        }
    }

    public static final CloseableHttpClient getHttpClient() {

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

         return  httpClient;
    }


    public static void main(String[] args) throws IOException {
        String url = "https://www.baeldung.com/wp-content/uploads/2018/08/RWS-Widget-Option2.jpg";
        InputStream inputStream = request(url);

        File img = new File("img.jpg");
        FileOutputStream outputStream = new FileOutputStream(img);

        ByteStreams.copy(inputStream, outputStream);
    }

}
