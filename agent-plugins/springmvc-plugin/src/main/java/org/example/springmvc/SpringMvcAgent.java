package org.example.springmvc;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.builder.AgentBuilder;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Slf4j
public class SpringMvcAgent {

    private static final String CONTROLLER_NAME = "org.springframework.stereotype.Controller";
    private static final String REST_CONTROLLER_NAME = "org.springframework.web.bind.annotation.RestController";

    public static void premain(String args, Instrumentation inst) {
        log.info("进入到SpringMvcAgent, args: {}", args);
        new AgentBuilder.Default()
                .ignore(nameStartsWith("net.bytebuddy")
                        .or(nameStartsWith("org.springframework"))
                ).type(isAnnotatedWith(named(CONTROLLER_NAME)).or(isAnnotatedWith(named(REST_CONTROLLER_NAME))))
                .transform(new SpringMvcTransformer())
                .installOn(inst);
    }
}