# agent-plugins

## agent的插件集合 第一版
开发了两个测试插件：
1.sqlite3-plugin
2.springmvc-plugin

采用的是最原始的agent插件开发方式，即premain和Instrumentation的方式，并打包一个fat jar，pom中配置Premain-Class，生成一个独立的插件
最后在app的启动程序Main运行时，添加agent vm参数：
-javaagent:
/Users/hbj/Study/java-agent-study/agent-plugins/springmvc-plugin/target/springmvc-plugin-1.0-SNAPSHOT-jar-with-dependencies.jar
-javaagent:
/Users/hbj/Study/java-agent-study/agent-plugins/sqlite3-plugin/target/sqlite3-plugin-1.0-SNAPSHOT-jar-with-dependencies.jar
实现了最简单的拦截功能，可以参考代码

思考：
从agent-plugins模块可知，我们需要配置多个agent，需要在启动类添加
-javaagent:/path/to/springmvc-plugin.jar
-javaagent:/path/to/sqlite3-plugin.jar

当插件非常多的时候，配置多个agent就比较麻烦了，而且每个插件引入的jar包差不都，且都是一个fat包，体积非常大，怎么办？

## apm-sniffer模拟SkyWalking agent可插拔式架构实现