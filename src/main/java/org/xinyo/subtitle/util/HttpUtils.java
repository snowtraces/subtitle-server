package org.xinyo.subtitle.util;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;
import org.xinyo.subtitle.netty.HttpServerDispatchHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUtils {

    // 提取请求中的参数
    public static RequestParams extractRequestParams(FullHttpRequest request) {
        RequestParams requestParams = new RequestParams();
        Map<String, List<Object>> params = new HashMap<>();

        // 1. url 参数
        String uri = request.uri();
        QueryStringDecoder stringDecoder = new QueryStringDecoder(uri);

        requestParams.setUri(stringDecoder.rawPath());
        Map<String, List<String>> pathParams = stringDecoder.parameters();
        for (Map.Entry<String, List<String>> entry : pathParams.entrySet()) {
            List<Object> values = new ArrayList<>();
            values.addAll(entry.getValue());
            params.put(entry.getKey(), values);
        }

        // 2. request body 参数
        HttpPostRequestDecoder requestDecoder = new HttpPostRequestDecoder(request);

        try {
            // json data
            if(request.content().isReadable()){
                String json=request.content().toString(CharsetUtil.UTF_8);

                Gson gson = new Gson();
                Map<String, Object> map = gson.fromJson(json, Map.class);

                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    String name = entry.getKey();
                    List<Object> values = params.containsKey(name) ? params.get(name) : new ArrayList<>();
                    values.add(entry.getValue());
                    params.put(name, values);
                }
            }

            // form data
            requestDecoder.offer(request);
            List<InterfaceHttpData> bodyHttpDatas = requestDecoder.getBodyHttpDatas();
            for (InterfaceHttpData data : bodyHttpDatas) {
                Attribute attribute = (Attribute) data;

                String name = attribute.getName();
                List<Object> values = params.containsKey(name) ? params.get(name) : new ArrayList<>();
                values.add(attribute.getValue());
                params.put(name, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        requestParams.pushParams(params);

        // 3. cookie参数
        // TODO

        return requestParams;
    }

    public static void response(ChannelHandlerContext ctx, String result, HttpResponseStatus status){
        ByteBuf content = result == null ? null : Unpooled.copiedBuffer(result, CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content == null ? null : content.readableBytes());
        response.content().writeBytes(content);

        ctx.writeAndFlush(response);
    }

    public static void response(ChannelHandlerContext ctx, Object result, HttpResponseStatus status) {
        if (result instanceof String) {
            response(ctx, (String)result, status);
        } else {
            response(ctx, new Gson().toJson(result), status);
        }
    }

    public static void response(ChannelHandlerContext ctx, HttpResponseStatus status){
        response(ctx, "{\"result\":\"ok\"}", status);
    }

    public static void response(ChannelHandlerContext ctx, HttpServerDispatchHandler.Result result){
        if (Strings.isNullOrEmpty(result.getData())) {
            response(ctx, result.getStatus());
        } else {
            response(ctx, result.getData(), result.getStatus());
        }
    }

    public static class RequestParams {
        private String uri;
        private Map<String, List<Object>> cookies = new HashMap<>();
        private Map<String, List<Object>> params = new HashMap<>();

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public Map<String, List<Object>> getCookies() {
            return cookies;
        }

        public void setCookies(Map<String, List<Object>> cookies) {
            this.cookies = cookies;
        }

        public Map<String, List<Object>> getParams() {
            return params;
        }

        public void setParams(Map<String, List<Object>> params) {
            this.params = params;
        }

        public void pushParams(Map<String, List<Object>> params) {
            this.params.putAll(params);
        }

        public void pushCookies(Map<String, List<Object>> params) {
            this.cookies.putAll(params);
        }

        @Override
        public String toString() {
            return "RequestParams{" +
                    "uri='" + uri + '\'' +
                    ", cookies=" + cookies +
                    ", params=" + params +
                    '}';
        }
    }

}
