# java-agent-study
Java Agent入门学习

java agent探针技术是专门对字节码做增强的，主要在程序运行前和运行中做增强的，因此，需要对字节码操作有了解。  
市场上常见的字节码编辑技术有：asm， javassist， bytebuddy等，它们之间的对比网上材料多，这里不赘述了，
本文采用bytebuddy字节码技术。

## ByteBuddy学习

### ByteBuddy增强字节码的三种方式：

1.subclass(Type)
通过继承已有的类，动态创建一个新类。
subclass可以自定义属性、方法，也可以重写现有方法。

```java
/**
 * 创建一个空类 
 *
 */
@Runner
public class SimpleCreateRunner implements Runnable {
    static String newClassName = "net.bytepuppy.subclass.HelloWorld";

    @SneakyThrows
    @Override
    public void run() {
        // DynamicType.Unloaded，顾名思义，创建了字节码，但未加载到虚拟机
        DynamicType.Unloaded<?> dynamicType = new ByteBuddy()
                // 继承Object.class
                .subclass(Object.class)
                // 指定固定的名字
                .name(newClassName)
                // 创建字节码
                .make();
        // 将字节码保存到指定文件
        dynamicType.saveIn(Consts.newFile(Consts.CLASS_OUTPUT_BASE_DIR));
        System.out.println("save class: " + Consts.CLASS_OUTPUT_BASE_DIR + newClassName);
    }
}
```

2. redefine(Type) 重写
   可以对一个现有类的属性、方法进行增、删、改。  
   重写的前提是redefine后的类名不变，如果重命名redefine后的类，其实跟subclass效果相当。  
   属性、方法被redefine后，原定义（属性、方法）会丢失，好像类被重写了一样  
   JVM runtime redefine一个类，不能被加载到JVM中，因为会报错：java.lang.IllegalStateException: Class already loaded: class
   xxx
   JVM runtime类替换的的方法之一，是JVM热加载。byte buddy通过ByteBuddyAgent.install() +
   ClassReloadingStrategy.fromInstalledAgent() 封装了简洁的热加载调用。
   但遗憾的是，JVM 热加载不允许增减原class的schema（比如增减属性、方法），因此使用场景非常受限。
   修改Schema后热加载报错：UnsupportedOperationException: class redefinition failed: attempted to change the schema (
   add/remove fields)

3. rebase 增强
   rebase功能与redefine相当，也可以已有类的方法、属性自定义增删改。
   rebase与redefine的区别，redefine后的原属性、原方法丢失；rebase后的原属性、原方法被拷贝 + 重命名保留在class内。
   rebase可以实现一些类似java.lang.reflect.Proxy的代理功能。但rebase与redefine一样，热加载类的问题依然存在。

PS：rebase和redefine为了更直观看到区别，可以直接saveIn后，直接查看源代码，对比下，会发现rebase会将就方法重命名+$old，但不会删除。

### ByteBuddy类加载器

ByteBuddy buddy = new ByteBuddy() 新建一个对象后，就可以设置其属性，以下介绍几个比较重要的属性：  
buddy.with(new NamingStrategy.xxx)来配置生成的类名。  
NamingStrategy：如果不给名字，则默认生成策略： net.bytebuddy.named.[parent类的全限定名]$ByteBuddy$[8位随机数]  
AbstractBase：用户自己实现。
Suffixing：[net.bytebuddy.named || 指定包名].[parent类的全限定名]$[suffix]
SuffixingRandom：  [net.bytebuddy.named || 指定包名].[parent类的全限定名]$[suffix]$[8位随机数]
PrefixingRandom：  [prefix].[parent类的全限定名]$[8位随机数]
buddy.with().subclass(xxx) 通过继承已有的类，动态创建一个新类。  
buddy.with().subclass().make() 会生成类对应的字节码二进制数组，并缓存在内存中，但不会被ClassLoader加载到jvm     
buddy.with().subclass().load() 会将二进制数组通过classloader加载到jvm中，成为一个真正的类。  
这里load的参数非常重要，这涉及到了那个classloader加载了这个class, 方法：
load(S, ClassLoadingStrategy<S extends ClassLoader>)
第一类是Classloader，第二个参数是ClassLoadingStrategy，第二个参数不传时，默认策略是ClassLoadingStrategy.Default.WRAPPER

1. ClassLoadingStrategy.Default.INJECTION
   原理：将生成的类注入到现有的类加载器中。
   适用场景：当需要将新生成的类注入到现有类加载器中，而不希望创建新的类加载器时。
2. ClassLoadingStrategy.Default.WRAPPER
   原理：通过创建一个新的类加载器，将生成的类加载到 JVM。
   适用场景：当不希望干扰现有的类加载器，或者需要隔离新生成的类时。
3. ClassLoadingStrategy.Default.CHILD_FIRST
   原理：生成一个子类加载器，并优先从子类加载器中加载新生成的类。
   适用场景：当希望在类加载过程中优先使用动态生成的类，而不是现有的类时。
4. ClassLoadingStrategy.Default.CHILD_FIRST_PERSISTENT
   原理：生成一个子类加载器，并优先从子类加载器中加载新生成的类，当加载相同类时，会复用已经加载过的类。
   适用场景：当希望在类加载过程中优先使用动态生成的类，而不是现有的类时，并且希望复用已经加载过的类时。
5. ClassLoadingStrategy.Default.WRAPPER_PERSISTENT
   原理：生成一个子类加载器，并优先从子类加载器中加载新生成的类，当加载相同类时，会复用已经加载过的类。
   适用场景：当希望在类加载过程中优先使用动态生成的类，而不是现有的类时，并且希望复用已经加载过的类时。
   如何选择合适的策略
   选择 INJECTION：当你希望动态生成的类与现有类共享类加载器，避免类隔离时。
   选择 WRAPPER：当需要类隔离，或避免干扰现有类时，适合使用。
   选择 CHILD_FIRST：在解决类加载冲突或实现插件机制时，较为合适。

bytebuddy的使用示例请看测试用例 ByteBuddyTest.java, 里面包含了所有常见的方法使用,
推荐写得最全的文章： https://www.cnblogs.com/crazymakercircle/p/16635330.html#autoid-h2-12-3-0
推荐视频教程：https://www.bilibili.com/video/BV1G24y1a7bd?spm_id_from=333.788.player.switch&vd_source=bd99477a13c4939885a67f476eed959a&p=11