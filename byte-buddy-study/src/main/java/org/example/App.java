package org.example;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

import java.io.File;
import java.io.IOException;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws Exception {
        // 1.创建一个类
        createClass();
    }

    public static void createClass() throws Exception {
        ByteBuddy byteBuddy = new ByteBuddy();
        DynamicType.Builder<Object> builder = byteBuddy
                .with(new NamingStrategy.AbstractBase() {
                    @Override
                    protected String name(TypeDescription typeDescription) {
                        return "i.love.bytebuddy.MyByteBuddy";
                    }
                })
                .subclass(Object.class);
        DynamicType.Unloaded<Object> maker = builder.make();
        Class<?> aClass = App.class.getClassLoader().loadClass("i.love.bytebuddy.MyByteBuddy");
        System.out.println("aClass = " + aClass);
        maker.saveIn(new File(App.class.getProtectionDomain().getCodeSource().getLocation().getFile()));
    }
}
