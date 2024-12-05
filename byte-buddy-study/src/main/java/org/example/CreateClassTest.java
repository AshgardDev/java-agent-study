package org.example;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

import java.io.File;

/**
 * Hello world!
 */
public class CreateClassTest {
    public static void main(String[] args) throws Exception {
        // 1.创建一个类
        createClassByNamingStrategy(new NamingStrategy.AbstractBase() {

            @Override
            protected String name(TypeDescription typeDescription) {
                return "i.love.bytebuddy.MyByteBuddy";
            }
        });
        createClassByNamingStrategy(new NamingStrategy.SuffixingRandom("suffix", "com.xxx"));
        createClassByNamingStrategy(new NamingStrategy.Suffixing("suffix", "com.yyy"));
        createClassByNamingStrategy(new NamingStrategy.PrefixingRandom("prefix"));
        createClassByClassloadingStrategy(ClassLoader.getSystemClassLoader(), ClassLoadingStrategy.Default.INJECTION);
        createClassByClassloadingStrategy(ClassLoader.getSystemClassLoader(), ClassLoadingStrategy.Default.WRAPPER);
        createClassByClassloadingStrategy(ClassLoader.getSystemClassLoader(), ClassLoadingStrategy.Default.CHILD_FIRST);
        createClassByClassloadingStrategy(ClassLoader.getSystemClassLoader(), ClassLoadingStrategy.Default.WRAPPER_PERSISTENT);
        createClassByClassloadingStrategy(ClassLoader.getSystemClassLoader(), ClassLoadingStrategy.Default.CHILD_FIRST_PERSISTENT);
    }

    public static void createClassByNamingStrategy(NamingStrategy namingStrategy) throws Exception {
        ByteBuddy byteBuddy = new ByteBuddy();
        byteBuddy
                .with(namingStrategy)
                .subclass(Object.class)
                .make()
                .saveIn(new File(CreateClassTest.class.getProtectionDomain().getCodeSource().getLocation().getFile()));
    }

    public static void createClassByClassloadingStrategy(ClassLoader classLoader, ClassLoadingStrategy classLoadingStrategy) throws Exception {
        ByteBuddy byteBuddy = new ByteBuddy();
        Class clazz = byteBuddy.with(new NamingStrategy.PrefixingRandom("com"))
                .subclass(Object.class)
                .make()
                .load(classLoader, classLoadingStrategy).getLoaded();
        System.out.println(classLoadingStrategy + ", clazz.getName() = " + clazz.getName() + ", clazz.getClassLoader() = " + clazz.getClassLoader());
    }
}
