测试agent的应用模块
1.引入spring-boot-starter-web，因为要测试agent拦截controller的方法，需要web环境
2.引入mybatis-plus和sqlite-jdbc，用于测试agent拦截sql执行的方法，模拟数据库操作

代码就是基础的springboot + mybatis-plus + sqlite项目