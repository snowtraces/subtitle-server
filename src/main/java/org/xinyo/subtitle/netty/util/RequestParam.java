package org.xinyo.subtitle.netty.util;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author CHENG
 */
@Data
public class RequestParam {
    private String uri;
    private Map<String, List<Object>> cookies = new HashMap<>();
    private Map<String, List<Object>> params = new HashMap<>();

    public void pushParams(Map<String, List<Object>> params) {
        if (params == null) {
            return;
        }

        params.forEach((key, value) -> {
            if (this.params.containsKey(key)) {
                List<Object> existValues = this.params.get(key);
                existValues.addAll(value);
                this.params.put(key, existValues);
            } else {
                this.params.put(key, value);
            }
        });
    }

    public void pushCookies(Map<String, List<Object>> cookies) {
        this.cookies.putAll(cookies);
    }
}
