package org.xinyo.subtitle.netty;

import com.google.gson.Gson;
import io.netty.handler.codec.http.FullHttpRequest;
import org.xinyo.subtitle.netty.annotation.Param;
import org.xinyo.subtitle.netty.init.ControllerInitializer;
import org.xinyo.subtitle.util.HttpUtils;
import org.xinyo.subtitle.util.SpringContextHolder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpServerDispatchHandler {
    public static String dispatch(FullHttpRequest request) {
        String result = null;

        HttpUtils.RequestParams params = HttpUtils.extractRequestParams(request);

        System.err.println(params);

        try {
            String uri = params.getUri();
            Map<String, Object[]> mappingMap = ControllerInitializer.getMappingMap();
            if (mappingMap.containsKey(uri)) {
                Object[] objects = mappingMap.get(uri);
                Class clazz = (Class) objects[0];
                Method method = (Method) objects[1];
                Parameter[] parameters = (Parameter[]) objects[2];

                Object[] methodParams = generateParams(parameters, params.getParams());

                Object invoke = method.invoke(SpringContextHolder.getBean(clazz), methodParams);

                if (invoke != null && !(invoke instanceof String)) {
                    result = new Gson().toJson(invoke);
                }
            } else {
                // TODO 404
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
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
                Param pName = p.getAnnotation(Param.class);
                if (pName == null) {
                    // 默认情况下拿不到参数名称，只能通过注解解决
                    throw new RuntimeException("annotation @Param does not exist!");
                }
                String name = pName.value();
                List<Object> objects = params.get(name);
                if (objects != null && objects.size() > 0) {
                    paramList.add((clazz.equals(String.class) || clazz.isPrimitive()) ? objects.get(0) : objects.toArray());
                }
            } else {
                // 2. java bean
                Object o = clazz.newInstance();

                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    String fieldName = field.getName();
                    Class<?> fieldType = field.getType();
                    List<Object> objects = params.get(fieldName);
                    if (objects != null && objects.size() > 0) {
                        String setMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

                        Method setMethod = clazz.getDeclaredMethod(setMethodName, fieldType);
                        Object value = objects.get(0);

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
                                ||fieldType.isAssignableFrom(Long.class)
                                || fieldType.isAssignableFrom(Float.class)
                                || fieldType.isAssignableFrom(Double.class)) {
                            // 数字类型不一致，需要强制转型
                            value = getNumberValue(objects.get(0), fieldType);
                        }

                        setMethod.invoke(o, value);
                    }
                }

                paramList.add(o);
            }
        }
        return paramList.toArray();
    }

    /**
     * 数字强制转型
     * @param o
     * @param fieldType
     * @return
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
