package org.xinyo.subtitle.netty;

import com.google.gson.Gson;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.xinyo.subtitle.netty.annotation.Param;
import org.xinyo.subtitle.netty.init.ControllerInitializer;
import org.xinyo.subtitle.netty.util.ExecuteTarget;
import org.xinyo.subtitle.netty.util.HttpUtils;
import org.xinyo.subtitle.netty.util.RequestParam;
import org.xinyo.subtitle.util.SpringContextHolder;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
public class HttpServerDispatchHandler {
    @Data
    public static class Result {
        private String data;
        private HttpResponseStatus status;
    }

    static Result dispatch(FullHttpRequest request) {
        Result result = new Result();

        RequestParam params = HttpUtils.extractRequestParams(request);

        log.info(params.toString());

        try {
            String uri = params.getUri();

            if (ControllerInitializer.MAPPING_MAP.containsKey(uri)) {
                ExecuteTarget target = ControllerInitializer.MAPPING_MAP.get(uri);

                Object[] methodParams = generateParams(target.getParameters(), params.getParams());

                Object invoke = target.getMethod().invoke(
                        SpringContextHolder.getBean(target.getClazz()), methodParams
                );

                if (invoke != null && !(invoke instanceof String)) {
                    String data = new Gson().toJson(invoke);
                    result.setData(data);
                    result.setStatus(HttpResponseStatus.OK);
                } else {
                    result.setStatus(HttpResponseStatus.NOT_FOUND);
                }
            } else {
                result.setStatus(HttpResponseStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String getParameterName(Parameter parameter) {
        Param pName = parameter.getAnnotation(Param.class);
        if (pName == null) {
            if (parameter.isNamePresent()) {
                return parameter.getName();
            } else {
                // 默认情况下拿不到参数名称，只能通过注解解决
                throw new RuntimeException("annotation @Param does not exist!");
            }

        }
        return pName.value();
    }

    private static Object[] generateParams(Parameter[] parameters, Map<String, List<Object>> params) throws Exception {
        if (parameters == null || parameters.length == 0) {
            return null;
        }

        List<Object> paramList = new ArrayList<>();
        for (Parameter p : parameters) {
            Class<?> clazz = p.getType();
            if (clazz.equals(String.class) || clazz.isPrimitive() || clazz.isArray()) {
                // 1. primitive / array
                String name = getParameterName(p);
                List<Object> requestParams = params.get(name);
                if (requestParams != null && requestParams.size() > 0) {
                    paramList.add((clazz.equals(String.class) || clazz.isPrimitive()) ? requestParams.get(0) : requestParams.toArray());
                }
            } else {
                // 2. java bean
                Object o = clazz.newInstance();

                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    String fieldName = field.getName();
                    Class<?> fieldType = field.getType();
                    List<Object> requestParams = params.get(fieldName);
                    if (requestParams != null && requestParams.size() > 0) {
                        Object value = requestParams.get(0);

                        if (fieldType.isAssignableFrom(String.class) && !(value instanceof String)) {
                            String stringValue = String.valueOf(value);
                            if (!stringValue.contains(".")) {
                                value = stringValue;
                            } else {
                                int lastIdx = stringValue.lastIndexOf(".");
                                String subFix = stringValue.substring(lastIdx);
                                if (subFix.matches("^\\.0+$")) {
                                    value = stringValue.substring(0, lastIdx);
                                } else {
                                    value = stringValue;
                                }
                            }

                        } else if (fieldType.isAssignableFrom(Short.class)
                                || fieldType.isAssignableFrom(Integer.class)
                                || fieldType.isAssignableFrom(Long.class)
                                || fieldType.isAssignableFrom(Float.class)
                                || fieldType.isAssignableFrom(Double.class)) {
                            // 数字类型不一致，需要强制转型
                            value = getNumberValue(requestParams.get(0), fieldType);
                        } else {
                            // TODO 非基本类型
                        }

                        field.setAccessible(true);
                        field.set(o, value);
                    }
                }

                paramList.add(o);
            }
        }
        return paramList.toArray();
    }

    /**
     * 数字强制转型
     */
    private static Object getNumberValue(Object o, Class<?> fieldType) {
        if (fieldType.equals(o.getClass())) {
            return o;
        }

        Object numberValue = null;
        String stringValue = String.valueOf(o);
        if (fieldType.isAssignableFrom(Short.class)) {
            stringValue = stringValue.substring(0, stringValue.indexOf("."));
            numberValue = Short.parseShort(stringValue);
        } else if (fieldType.isAssignableFrom(Integer.class)) {
            stringValue = stringValue.substring(0, stringValue.indexOf("."));
            numberValue = Integer.parseInt(stringValue);
        } else if (fieldType.isAssignableFrom(Long.class)) {
            stringValue = stringValue.substring(0, stringValue.indexOf("."));
            numberValue = Long.parseLong(stringValue);
        } else if (fieldType.isAssignableFrom(Float.class)) {
            numberValue = Float.parseFloat(stringValue);
        } else if (fieldType.isAssignableFrom(Double.class)) {
            numberValue = Double.parseDouble(stringValue);
        }

        return numberValue;

    }
}
