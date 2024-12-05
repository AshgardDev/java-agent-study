package org.example.skywalking;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * 模拟java agent增强类
 */
public class JavaAgentMain {

    /*
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        new AgentBuilder.Default()
                // 增强类通过类名匹配
                .type(ElementMatchers.named("org.example.skywalking.TestService"))
                // 自定义Transformer
                .transform(
                        new AgentBuilder.Transformer() {
                            @Override
                            public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
                                                                    ClassLoader classLoader, JavaModule javaModule, ProtectionDomain protectionDomain) {
                                return builder.method(ElementMatchers.named("report"))
                                        .intercept(MethodDelegation.to(new DelegateTemplate(new TestServiceInterceptor())));
                            }
                        }
                ).installOn(instrumentation);
    }*/

    public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        /*
        ByteBuddyAgent.install();
        new AgentBuilder.Default()
                .type(ElementMatchers.named("org.example.skywalking.TestService"))
                .transform(
                        new AgentBuilder.Transformer() {
                            @Override
                            public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
                                                                    ClassLoader classLoader, JavaModule javaModule, ProtectionDomain protectionDomain) {
                                return builder.method(ElementMatchers.named("report"))
                                        .intercept(MethodDelegation.to(new DelegateTemplate(new TestServiceInterceptor())));
                            }
                        }
                ).installOn(ByteBuddyAgent.getInstrumentation());
        */
        ByteBuddy byteBuddy = new ByteBuddy();
        TypePool typePool = TypePool.Default.ofSystemLoader();
        byteBuddy.rebase(typePool.describe("org.example.skywalking.TestService").resolve(),
                        ClassFileLocator.ForClassLoader.ofSystemLoader())
                .name("MyTestService")
                .method(ElementMatchers.named("report"))
                .intercept(MethodDelegation.to(new DelegateTemplate(new TestServiceInterceptor())))
                .make()
                .saveIn(new File(JavaAgentMain.class.getProtectionDomain().getCodeSource().getLocation().getPath()));
    }
}