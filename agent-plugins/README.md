# agent-plugins

agent的插件集合
开发了两个测试插件：
1.mybatis-plugin
2.springmvc-plugin

采用的是最原始的agent插件开发方式，即premain和Instrumentation的方式，并打包一个fat jar，pom中配置Premain-Class，生成一个独立的插件
最后在app的启动程序Main运行时，添加agent vm参数：
-javaagent:
/Users/hbj/Study/java-agent-study/agent-plugins/springmvc-plugin/target/springmvc-plugin-1.0-SNAPSHOT-jar-with-dependencies.jar
-javaagent:
/Users/hbj/Study/java-agent-study/agent-plugins/mybatis-plus-plguin/target/mybatis-plus-plguin-1.0-SNAPSHOT-jar-with-dependencies.jar
实现了最简单的拦截功能，可以参考代码



