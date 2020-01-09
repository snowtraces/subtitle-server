package org.xinyo.subtitle.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * http 响应包装类
 *
 * @author CHENG
 */
@Data
public class Resp {
    private int code;
    private String msg;
    private Object data;
    private List<?> rows;
    private long total;

    public static Resp success(Object data) {
        return success(data, null);
    }

    public static Resp success(Object data, String msg) {
        Resp resp = new Resp();
        resp.code = 100;
        resp.data = data;
        resp.msg = msg;

        return resp;
    }

    public static Resp failure(String msg) {
        Resp resp = new Resp();
        resp.code = 200;
        resp.msg = msg;

        return resp;
    }

    public static Resp forPage(long total, List<?> rows) {
        Resp resp = new Resp();
        resp.code = 100;
        resp.rows = rows;
        resp.total = total;

        return resp;
    }

}
