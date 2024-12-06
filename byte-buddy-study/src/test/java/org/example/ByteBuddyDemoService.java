package org.example;

public class ByteBuddyDemoService {

    public ByteBuddyDemoService() {

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
}
