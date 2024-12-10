package org.example;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import static net.bytebuddy.matcher.ElementMatchers.named;

@Slf4j
public class AgentStarter {
    public static void premain(String args, Instrumentation instrumentation) {
        log.info("进入到AgentStater, args: {}", args);
        new AgentBuilder.Default()
                .type()
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
                        return null;
                    }
                })
                .installOn(instrumentation);
    }
}