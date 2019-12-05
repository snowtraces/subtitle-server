package org.xinyo.subtitle.util;

import org.xinyo.subtitle.netty.annotation.Reference;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanUtils {
    private final static Map BEAN_CACHE = new HashMap();
    private final static List<Class> CLASS_LIST = new ArrayList();

    public static void addBean(Class clazz) {
        if (!CLASS_LIST.contains(clazz)) {
            CLASS_LIST.add(clazz);
        }
    }

    public static void addBean(List<Class> clazzs) {
        if (clazzs != null && clazzs.size() > 0) {
            for (Class clazz : clazzs) {
                addBean(clazz);
            }
        }
    }

    public static Object getBean(Class clazz) {
        String name = clazz.toString();
        return BEAN_CACHE.get(name);
    }

    public static void init() {
        for (Class clazz : CLASS_LIST) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                boolean hasReference = field.isAnnotationPresent(Reference.class);
                if (hasReference) {
                    Class<?> type = field.getType();
                    boolean contains = CLASS_LIST.contains(type);
                    if(!contains) {
                        throw new RuntimeException("没有找类(@Reference)：" + type.toString());
                    }
                }
            }
        }

        doInit(CLASS_LIST.size());
    }


    private static void doInit(int size) {
        // 1. 递归终止条件
        if (size == 0) {
            return;
        }

        for (Class clazz : CLASS_LIST) {
            boolean isReady = true;
            String key = clazz.toString();
            boolean hasBean = BEAN_CACHE.containsKey(key);
            if (!hasBean) {
                // 尚未初始化, 判断是否有引用其他bean
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    boolean hasReference = field.isAnnotationPresent(Reference.class);
                    if (hasReference) {
                        Class<?> type = field.getType();
                        String name = type.toString();

                        if (!BEAN_CACHE.containsKey(name)) {
                            // 依赖尚未初始化
                            isReady = false;
                            break;
                        }
                    }
                }

                // 所有依赖均初始化成功
                if (isReady) {
                    try {
                        size--;
                        Object o = clazz.newInstance();

                        for (Field field : fields) {
                            boolean hasReference = field.isAnnotationPresent(Reference.class);
                            if (hasReference) {
                                Class<?> type = field.getType();
                                Object reference = BEAN_CACHE.get(type.toString());
                                field.setAccessible(true);
                                field.set(o, reference);
                            }
                        }

                        BEAN_CACHE.put(clazz.toString(), o);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // 2. 递归
        doInit(size);
    }
}
