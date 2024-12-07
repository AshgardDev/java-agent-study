package org.example.interceptor;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import java.util.Arrays;

public class ConstructorInterceptor5 {

    @RuntimeType
    public void interceptConstructor(@This Object target, @AllArguments Object[] allArguments) {
        System.out.println("尝试拦截构造函数");
        System.out.println("target = " + target);
        System.out.println("Arrays.asList(allArguments) = " + Arrays.asList(allArguments));
    }
}
