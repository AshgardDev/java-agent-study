package org.example.mybatisplus;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.builder.AgentBuilder;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.named;

@Slf4j
public class MybatisPlusAgent {

    private static final String JDBC_PREPARED_STATEMENT = "org.sqlite.jdbc3.JDBC3PreparedStatement";

    public static void premain(String args, Instrumentation inst) {
        log.info("进入到MybatisPlusAgent, args: {}", args);
        new AgentBuilder.Default()
                .type(named(JDBC_PREPARED_STATEMENT))
                .transform(new MybatisPlusTransformer())
                .installOn(inst);
    }
}