package org.example.skywalking;

import java.lang.reflect.Method;

/**
 * TestService增强切面，实现切面接口InstMethodAroundInterceptor
 */
public class TestServiceInterceptor implements InstMethodAroundInterceptor {
    @Override
    public void beforeMethod(Object inst, Method interceptPoint, Object[] allArguments,
                             Class<?>[] argumentsTypes, ResultWrapper result) {
        System.out.println("TestService Interceptor in ...");
    }

    @Override
    public Object afterMethod(Object inst, Method interceptPoint, Object[] allArguments,
                              Class<?>[] argumentsTypes, Object ret) {
        System.out.println("TestService Interceptor out ...");
        return ret;
    }

    @Override
    public void handleMethodException(Object inst, Method method, Object[] allArguments,
                                      Class<?>[] argumentsTypes, Throwable t) {
        System.out.println("TestService Interceptor error handle ...");
    }
}