package org.example.mybatisplus.interceptor;

public class SelectUserNameInterceptor1 {

    public String selectUserName(Long id) {
        System.out.println("我是selectUserName拦截器方法1，我被调用了");
        return "用户ID=" + id;
    }
}
