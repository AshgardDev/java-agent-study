package org.example.mybatisplus.interceptor;

import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class SelectUserNameInterceptor3 {

    /**
     * 拦截方法selectUserName，被@RuntimeType拦截的方法，返回值可以类型不一致，
     * 且在运行期间会给注解标注的对象赋值，注解常见如下：（net.bytebuddy.implementation.bind.annotation.*）
     *
     * @param target       -- 被拦截的目标对象，静态方法不可用
     * @param method       -- 被拦截的目标对象的方法
     * @param zuper        -- 被拦截的目标对象，静态方法不可用
     * @param allArguments -- 被拦截的方法的参数
     * @param zuperCall    -- 被拦截的回调方法
     * @param id           -- 具体某个参数
     * @return
     */
    @RuntimeType
    public String selectUserName(@This Object target, @Origin Method method, @Super Object zuper,
                                 @AllArguments Object[] allArguments, @SuperCall Callable<?> zuperCall, @Argument(0) Long id) {
        System.out.println("我是selectUserName拦截器方法3，我被调用了");
        System.out.println("target = " + target);
        System.out.println("method = " + method);
        System.out.println("Arrays.asList(allArguments) = " + Arrays.asList(allArguments));
        System.out.println("zuper = " + zuper);
        System.out.println("zuperCall = " + zuperCall);
        System.out.println("id = " + id);
        try {
            Object result = zuperCall.call();
            return "执行拦截，返回结果：" + result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
