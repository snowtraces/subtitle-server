package org.xinyo.subtitle.netty.init;

import org.xinyo.subtitle.netty.annotation.RestMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControllerInitializer {
    private List<Class> controllerList = new ArrayList<>();
    private static Map<String, Object[]> mappingMap = new HashMap<>();

    public ControllerInitializer add(Class clazz) {
        controllerList.add(clazz);
        return this;
    }

    public ControllerInitializer add(List<Class> clazzs) {
        controllerList.addAll(clazzs);
        return this;
    }

    public static Map<String, Object[]> getMappingMap() {
        return mappingMap;
    }

    public void init() {
        if (controllerList.size() == 0) {
            // TODO warning log
            return;
        }

        for (Class clazz : controllerList) {
            Method[] methods = clazz.getMethods();
            if (methods == null || methods.length == 0) {
                continue;
            }

            for (Method method : methods) {
                boolean isMappingMethod = method.isAnnotationPresent(RestMapping.class);
                if (isMappingMethod) {
                    RestMapping restMapping = method.getAnnotation(RestMapping.class);
                    String[] paths = restMapping.value();

                    for (String path : paths) {
                        Object[] clazzAndMethod = new Object[3]; // clazz, method, params
                        clazzAndMethod[0] = clazz;
                        clazzAndMethod[1] = method;
                        clazzAndMethod[2] = method.getParameters();

                        mappingMap.put(path, clazzAndMethod);
                    }
                }
            }
        }
    }

}
