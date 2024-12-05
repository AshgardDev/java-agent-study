package org.example;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;

import java.lang.reflect.Modifier;

/**
 * 重写一个类，并在类加载前替换类，然后再实例化
 */
public class RedefineClassTest {
    public static void main(String[] args) throws Exception {
        DynamicType.Unloaded unloaded = createWithoutTriggerClassLoad();
        // 这个方法和load不要同时存在，idea会将save后的类生成到类路径，然后jvm就会自动加载，导致报 Class already loaded。
        // unloaded.saveIn(new File(ClassLoader.getSystemClassLoader().getResource(".").getPath()));
        Object demoService = unloaded.load(ClassLoader.getSystemClassLoader(), ClassLoadingStrategy.Default.WRAPPER).getLoaded().newInstance();
        Object o = demoService.getClass()
                .getMethod("report", String.class)
                .invoke(demoService, "report method call");
        System.out.println(o.toString());
        System.out.println(demoService.getClass().getDeclaredField("name").get(demoService));
    }

    private static DynamicType.Unloaded createWithoutTriggerClassLoad() {
        TypePool typePool = TypePool.Default.ofSystemLoader();
        DynamicType.Unloaded unloaded = new ByteBuddy()
                // try redefine
                .redefine(typePool.describe("org.example.DemoService").resolve(),
                        ClassFileLocator.ForClassLoader.ofSystemLoader())
                .name("org.example.WhatEver")
                // 如果用ClassLoadingStrategy.Default.WRAPPER，那必须为新类指定一个名字，否则在相同ClassLoader中名字冲突
                // ClassLoadingStrategy.Default.CHILD_FIRST，name定义可以省略
                .defineField("name", String.class, Modifier.PUBLIC)
                .method(ElementMatchers.named("report"))
                .intercept(FixedValue.value("Hello World!"))
                .make();

        return unloaded;
    }
}