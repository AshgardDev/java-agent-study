package org.example.mybatisplus;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;

@Slf4j
public class MybatisPlusInterceptor {

    @RuntimeType
    public Object intercept(@This Object targetObject, @Origin Method targetMethod,
                            @AllArguments Object[] targetMethodArgs,
                            @SuperCall Callable<?> zuper) {
        log.info("MyBAtisPlus方法Before拦截， 拦截方法：{}， 参数：{}", targetMethod.getName(), Arrays.toString(targetMethodArgs));
        long start = System.currentTimeMillis();
        Object call = null;
        try {
            call = zuper.call();
            log.info("MyBAtisPlus方法After拦截， 拦截方法：{}， 参数：{}， 结果：{}", targetMethod.getName(),
                    Arrays.toString(targetMethodArgs), call);
        } catch (Exception e) {
            log.info("MyBAtisPlus方法Exception拦截， 拦截方法：{}， 异常：{}", targetMethod.getName(), e.getMessage(), e);
        } finally {
            long end = System.currentTimeMillis();
            log.info("MyBAtisPlus方法执行耗时统计：{}ms", (end - start));
        }
        return call;
    }
}
