package org.example.sqlite3;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.builder.AgentBuilder;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.named;

@Slf4j
public class Sqlite3Agent {

    private static final String JDBC_PREPARED_STATEMENT = "org.sqlite.jdbc4.JDBC4PreparedStatement";

    public static void premain(String args, Instrumentation inst) {
        log.info("进入到Sqlite3Agent, args: {}", args);
        new AgentBuilder.Default()
                .type(named(JDBC_PREPARED_STATEMENT))
                .transform(new Sqlite3Transformer())
                .installOn(inst);
    }
}