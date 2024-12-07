package org.example.interceptor;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class StaticMethodInterceptor6 {

    @RuntimeType
    public void intercept(
            // 该参数不可用
            // @This Object target,
            @Origin Class<?> clazz,
            @Origin Method method,
            // 该参数不可用
            //@Super Object zuper,
            @AllArguments Object[] allArguments,
            @SuperCall Callable<?> zuperCall) throws Exception {
        System.out.println("尝试拦截静态函数");
        System.out.println("clazz = " + clazz);
        System.out.println("method = " + method);
        System.out.println("Arrays.asList(allArguments) = " + Arrays.asList(allArguments));
        try {
            Object call = zuperCall.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
