package org.xinyo.subtitle.netty.init;

import org.xinyo.subtitle.netty.annotation.RestMapping;
import org.xinyo.subtitle.netty.util.ExecuteTarget;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ControllerInitializer {
    private final Pattern PATH_PARAM_PATTERN = Pattern.compile("^(.*)/\\$\\{([^}/]+)}$");

    private final List<Class> controllerList = new ArrayList<>();
    public static final Map<String, ExecuteTarget> MAPPING_MAP = new HashMap<>();
    public static final Map<String, String> PATH_WITH_PARAM = new HashMap<>();

    public ControllerInitializer add(Class clazz) {
        controllerList.add(clazz);
        return this;
    }

    void add(List<Class> classList) {
        controllerList.addAll(classList);
    }

    void init() {
        if (controllerList.size() == 0) {
            return;
        }

        for (Class clazz : controllerList) {
            Method[] methods = clazz.getMethods();
            if (methods.length == 0) {
                continue;
            }

            for (Method method : methods) {
                boolean isMappingMethod = method.isAnnotationPresent(RestMapping.class);
                if (isMappingMethod) {
                    RestMapping restMapping = method.getAnnotation(RestMapping.class);
                    String[] paths = restMapping.value();

                    for (String path : paths) {
                        Matcher matcher = PATH_PARAM_PATTERN.matcher(path);
                        if (matcher.find()) {
                            path = matcher.group(1);
                            PATH_WITH_PARAM.put(path, matcher.group(2));
                        }
                        ExecuteTarget target = new ExecuteTarget(clazz, method, method.getParameters());
                        MAPPING_MAP.put(path, target);
                    }
                }
            }
        }
    }

}
