package org.xinyo.subtitle.util;

import org.xinyo.subtitle.netty.annotation.Reference;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanUtils {
    private final static Map beanCache = new HashMap();
    private final static List<Class> clazzList = new ArrayList();

    public static void addBean(Class clazz) {
        if (!clazzList.contains(clazz)) {
            clazzList.add(clazz);
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
        return beanCache.get(name);
    }

    public static void init() {
        for (Class clazz : clazzList) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                boolean hasReference = field.isAnnotationPresent(Reference.class);
                if (hasReference) {
                    Class<?> type = field.getType();
                    boolean contains = clazzList.contains(type);
                    if(!contains) {
                        throw new RuntimeException("没有找类(@Reference)：" + type.toString());
                    }
                }
            }
        }

        doInit(clazzList.size());
    }


    private static void doInit(int size) {
        // 1. 递归终止条件
        if (size == 0) {
            return;
        }

        for (Class clazz : clazzList) {
            boolean isReady = true;
            String key = clazz.toString();
            boolean hasBean = beanCache.containsKey(key);
            if (!hasBean) {
                // 尚未初始化, 判断是否有引用其他bean
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    boolean hasReference = field.isAnnotationPresent(Reference.class);
                    if (hasReference) {
                        Class<?> type = field.getType();
                        String name = type.toString();

                        if (!beanCache.containsKey(name)) {
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
                                Object reference = beanCache.get(type.toString());
                                field.setAccessible(true);
                                field.set(o, reference);
                            }
                        }

                        beanCache.put(clazz.toString(), o);
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
