package org.example.skywalking;

import java.lang.reflect.Method;

/**
 * 代理拦截接口，抽象方法调用前后的两个切面
 */
public interface InstMethodAroundInterceptor {

    /**
     * 拦截点前
     *
     * @param inst:                    被增强类实例
     * @param interceptPoint：被增强方法
     * @param allArguments：被增强方法入参
     * @param argumentsTypes：被增强方法入参类型
     * @param result：result            包装类
     */
    void beforeMethod(Object inst, Method interceptPoint,
                      Object[] allArguments, Class<?>[] argumentsTypes,
                      ResultWrapper result);

    Object afterMethod(Object inst, Method interceptPoint,
                       Object[] allArguments, Class<?>[] argumentsTypes,
                       Object ret);

    void handleMethodException(Object inst, Method method, Object[] allArguments,
                               Class<?>[] argumentsTypes, Throwable t);
}