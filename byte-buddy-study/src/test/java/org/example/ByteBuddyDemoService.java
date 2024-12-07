package org.example;

public class ByteBuddyDemoService {

    public ByteBuddyDemoService() {
        System.out.println("我是ByteBuddyDemoService构造方法");
    }

    public ByteBuddyDemoService(String name) {
        System.out.println("我是构造函数, name=" + name);
    }

    public String print() {
        System.out.println("this is print method");
        return "print";
    }

    public String selectUserName(Long id) {
        return "用户ID=" + id;
    }

    public void saveUser() {
        System.out.println("saveUser()");
    }

    public static void testStaticMethod() {
        System.out.println("staticMethod test");
    }
}
