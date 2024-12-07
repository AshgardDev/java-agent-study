package org.example.interceptor;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.example.OverrideCallback;

import java.util.Arrays;

public class SelectUserNameInterceptor4 {

    /**
     * 拦截后，动态修改参数
     *
     * @param callback     重写的回调方法
     * @param allArguments -- 被拦截的方法的参数
     * @return
     */
    @RuntimeType
    public String selectUserName(
            @Morph OverrideCallback callback,
            @AllArguments Object[] allArguments
    ) {
        System.out.println("我是selectUserName拦截器方法4，我被调用了");
        System.out.println("Arrays.asList(allArguments) = " + Arrays.asList(allArguments));
        try {
            if (allArguments.length > 0) {
                allArguments[0] = Long.parseLong(allArguments[0].toString()) + 1;
            }
            Object result = callback.call(allArguments);
            return "执行拦截，返回结果：" + result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
