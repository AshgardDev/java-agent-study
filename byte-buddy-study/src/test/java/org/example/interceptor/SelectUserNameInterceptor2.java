package org.example.interceptor;

public class SelectUserNameInterceptor2 {

    public static String selectUserName(Long id) {
        System.out.println("我是selectUserName拦截器方法2，我被调用了");
        return "用户ID=" + id;
    }
}
