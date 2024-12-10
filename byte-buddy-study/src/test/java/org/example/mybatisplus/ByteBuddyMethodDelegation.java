package org.example.mybatisplus;

import net.bytebuddy.implementation.bind.annotation.RuntimeType;

public class ByteBuddyMethodDelegation {

    public static String testStaticDelegation(Long id) {
        return "this is static testDelegation";
    }

    @RuntimeType
    public static String testRuntimeTypeDelegation(Long id) {
        return "this is runtime type testDelegation";
    }

    @RuntimeType
    public static String selectUserName(Long id) {
        return "this is selectUserName delegation";
    }

    public String testDelegation() {
        return "this is testDelegation";
    }
}
