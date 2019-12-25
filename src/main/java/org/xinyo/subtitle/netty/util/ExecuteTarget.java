package org.xinyo.subtitle.netty.util;

import lombok.Data;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @author CHENG
 */
@Data
public class ExecuteTarget {
    private Class<?> clazz;
    private Method method;
    private Parameter[] parameters;

    public ExecuteTarget(Class<?> clazz, Method method, Parameter[] parameters) {
        this.clazz = clazz;
        this.method = method;
        this.parameters = parameters;
    }
}
