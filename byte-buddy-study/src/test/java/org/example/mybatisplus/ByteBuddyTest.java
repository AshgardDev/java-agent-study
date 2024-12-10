package org.example.mybatisplus;

import cn.hutool.core.io.FileUtil;
import jdk.internal.org.objectweb.asm.Opcodes;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.*;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import org.example.mybatisplus.interceptor.ConstructorInterceptor5;
import org.example.mybatisplus.interceptor.SelectUserNameInterceptor3;
import org.example.mybatisplus.interceptor.SelectUserNameInterceptor4;
import org.example.mybatisplus.interceptor.StaticMethodInterceptor6;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class ByteBuddyTest {

    // 这里不要把path设置成类路径，否则会因为ByteBuddy调用saveIn方法，将类文件保存到类路径，idea就会默认加载生成的类
    // 导致每次启动都报错 Class already exist，所以设置一个临时路径即可
    private static String path;

    static {
        // path = ByteBuddyTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        path = System.getProperty("user.dir") + "/tmp/classes";
    }

    /**
     * 测试类的生成、类名设置、类字节码保存
     *
     * @throws Exception
     */
    @Test
    public void testCreateClass() throws Exception {
        DynamicType.Unloaded<?> unloaded = new ByteBuddy()
                // 命名规则
                // 1.在不指定类名的情况下
                // 父类如果是jdk包，则使用net.bytebuddy.renamed.父类包名+类名+$+ByteBuddy+$随机数
                // 父类不是jdk包，则使用父类的全类名加$ByteBuddy$+$随机数
                // 2.指定类名，则使用指定的类名或者类名生成策略，有下面几种，可以单独测试一下


                // net.bytebuddy.renamed.java.lang.Object$ByteBuddy$WPMmGhgb.class
                //.subclass(Object.class)

                // org.example.ByteBuddyDemoService$Demo$q8C7xFK1.class
                //.with(new NamingStrategy.SuffixingRandom("Demo", "com.xxx"))

                // com.prefix.org.example.ByteBuddyDemoService$ChgHrvwf.class
                //.with(new NamingStrategy.PrefixingRandom("com.prefix"))

                // org.example.ByteBuddyDemoService$suffix
                // .with(new NamingStrategy.Suffixing("suffix"))

                // 等价于name方法
                //.with(new NamingStrategy.AbstractBase() {
                //    @Override
                //    protected String name(TypeDescription typeDescription) {
                //        return "com.xyz.Test";
                //    }
                //})

                // 关闭类名和包名校验
                //.with(TypeValidation.DISABLED)

                // org.example.ByteBuddyDemoService$ByteBuddy$PPiv7g4E.class
                .subclass(ByteBuddyDemoService.class)

                // com.yyy.NewClassName.class
                //.name("com.yyy.NewClassName")

                // Illegal type name: com.123.456Test for class com.123.456Test
                // 可以通过关闭校验来达到目的
                //.name("com.123.456Test")

                .make();

        // 获取字节码
        // byte[] bytes = unloaded.getBytes();
        unloaded.saveIn(new File(path));
        // 注入到jar文件中
        // unloaded.inject(new File("path/to/file.jar"));
    }

    /**
     * 测试类的方法修改
     *
     * @throws Exception
     */
    @Test
    public void testModifyMethod() throws Exception {
        ByteBuddy byteBuddy = new ByteBuddy();
        // unloaded表示生成的字节码还没加载到jvm
        DynamicType.Unloaded<ByteBuddyDemoService> unloaded = byteBuddy
                .subclass(ByteBuddyDemoService.class)
                .name("org.example.SubClassByteBuddyDemoServiceModifyMethod")
                // 拦截toString方法
                .method(named("toString"))
                // 指定拦截到方法后的操作
                // FixedValue是指拦截后返回固定指hello
                .intercept(FixedValue.value("hello"))
                .make();
        // load表示生成的字节码已经加载到jvm
        DynamicType.Loaded<ByteBuddyDemoService> load = unloaded.load(ByteBuddyDemoService.class.getClassLoader());
        // 这里注意，DynamicType.Loaded 和 DynamicType.Unloaded都继承DynamicType接口，所以，他们有很多相同的操作
        // 包括saveIn，getBytes等

        // 保存后查看一下字节码是否变更了
        // load.saveIn(new File(path));

        // load.getLoaded().newInstance().toString() = hello
        System.out.println("load.getLoaded().newInstance().toString() = " + load.getLoaded().newInstance().toString());
    }

    /**
     * 测试类的修改方式
     * subclass 继承父类
     * redefined 重写，会覆盖原有的方法
     * rebase 变基，原有的方法会被重命名：methodName$old, 不会直接删除
     */
    @Test
    public void testModifyType() throws IOException {
        DynamicType.Unloaded<ByteBuddyDemoService> unloaded = new ByteBuddy()
                // 继承父类
                // .subclass(ByteBuddyDemoService.class)

                // 重写,redefine的类是不可以直接load的，会报错类已存在！但可以saveIn来查看
                // redefine的类，不再继承父类，且方法被直接覆盖了
                //.redefine(ByteBuddyDemoService.class)
                // 重命名后的类org.example.RedefineByteBuddyDemoService
                //.name("org.example.RedefineByteBuddyDemoService")

                // 变基,rebase的类是不可以直接load的，会报错类已存在！但可以saveIn来查看
                // 注意这里直接打开文件，是看不到变基后隐藏的方法，需要通过查看字节码才能看到
                // idea->view->Show Bytecode
                // 可以看到方法变成私有，且方法名变成private selectUserName$original$OOaFYyAH
                .rebase(ByteBuddyDemoService.class)
                .name("org.example.RebaseByteBuddyDemoServiceModifyType")
                // 拦截print
                .method(named("print"))
                .intercept(FixedValue.value("hello"))

                //拦截selectUserName
                .method(
                        named("selectUserName")
                                .and(ElementMatchers.returns(String.class))
                                .and(takesArguments(1)))
                .intercept(FixedValue.nullValue())

                // 拦截saveUser
                .method(named("saveUser")
                        .and(ElementMatchers.returns(void.class))
                )
                .intercept(FixedValue.value(TypeDescription.ForLoadedType.of(void.class)))
                .make();

        unloaded.saveIn(new File(path));

        // 只有重命名了 name(xxx)，才能load成功，否则会报错类已存在！
        DynamicType.Loaded<ByteBuddyDemoService> load = unloaded.load(ByteBuddyDemoService.class.getClassLoader());
    }

    /**
     * 测试创建新方法 包括get set方法
     *
     * @throws Exception
     */
    @Test
    public void testCreateMethod() throws Exception {
        DynamicType.Unloaded<ByteBuddyDemoService> unloaded = new ByteBuddy()
                .rebase(ByteBuddyDemoService.class)
                .name("org.example.RebaseByteBuddyDemoServiceWithCreateMethod")
                // 创建一个实例方法
                // 包括方法的名字、返回值、访问权限
                .defineMethod("newUser", String.class, Modifier.PUBLIC)
                // 定义方法参数
                .withParameters(String.class, long.class)
                .intercept(FixedValue.value("创建用户 名字：张三， id： 10001"))
                // 创建一个静态方法
                .defineMethod("getInstance", ByteBuddyDemoService.class, Modifier.PUBLIC + Modifier.STATIC)
                .withParameter(String[].class, "args")
                .intercept(FixedValue.value(new ByteBuddyDemoService()))
                .make();

        unloaded.saveIn(new File(path));
        DynamicType.Loaded<ByteBuddyDemoService> load = unloaded.load(ByteBuddyDemoService.class.getClassLoader());
    }

    /**
     * 测试创建新字段
     * 只有静态字段能设置初始值，实例字段和final字段都不能设置初始值
     * 静态字段：在类加载时初始化，JVM 会根据 ConstantValue 属性或类静态块进行初始化。
     * 非静态字段：在实例创建时初始化，由构造方法或实例初始化块负责赋值。
     *
     * @throws Exception
     */
    @Test
    public void testCreateField() throws Exception {
        DynamicType.Unloaded<ByteBuddyDemoService> unloaded = new ByteBuddy()
                .rebase(ByteBuddyDemoService.class)
                .name("org.example.RebaseByteBuddyDemoServiceWithCreateField")
                // 创建一个实例字段,这个方法的value是不会真正生效的，因为对象实例的属性赋值动作，其实是在对象建立后，才会设置到对象上的
                // 普通对象实例的field，是没办法直接设置的，但也有解决办法，就是通过构造函数来设置
                // 创建一个字段，并设置初始值， --不会生效
                .defineField("newField", String.class, Opcodes.ACC_PUBLIC)
                .value("newFieldValue")

                // 创建一个静态字段并设置默认值 -- 会生效
                .defineField("staticField", String.class, Modifier.PUBLIC | Modifier.STATIC)
                .value("staticFieldValue")

                // 创建一个final的非静态字段并设置默认值--不会生效
                .defineField("finalField", String.class, Modifier.PUBLIC | Modifier.FINAL)
                .value("finalFieldValue")

                // 创建一个带有get和set方法的字段
                .defineField("name", String.class, Visibility.PRIVATE)
                // 定义 getter 方法 getName
                .defineMethod("getName", String.class, Visibility.PUBLIC)
                .intercept(FieldAccessor.ofField("name"))
                // 定义 setter 方法 setName
                .defineMethod("setName", void.class, Visibility.PUBLIC)
                .withParameter(String.class, "name")
                .intercept(FieldAccessor.ofField("name").setsArgumentAt(0))

                // 定义一个text字段,并实现set和get接口
                .defineField("text", String.class, Visibility.PUBLIC)
                .implement(ByteBuddyInterface.class)
                .intercept(FieldAccessor.ofField("text"))

                .make();

        unloaded.saveIn(new File(path));
        DynamicType.Loaded<ByteBuddyDemoService> load = unloaded.load(ClassLoader.getSystemClassLoader());

        // 创建对象实例并传递构造函数参数
        Object obj = load.getLoaded().newInstance();
        for (Field field : obj.getClass().getFields()) {
            System.out.println("field = " + field.getName() + ", value=" + field.get(obj));
        }
    }

    /**
     * 测试方法委托
     *
     * @throws Exception
     */
    @Test
    public void testMethodDelegation() throws Exception {
        DynamicType.Unloaded<ByteBuddyDemoService> unloaded = new ByteBuddy()
                .rebase(ByteBuddyDemoService.class)
                .name("org.example.RebaseByteBuddyDemoServiceWithMethodDelegation")
                .method(named("selectUserName").and(takesArguments(1).and(ElementMatchers.returns(String.class))))
                //.intercept(FixedValue.value("delegate"))

                // 分多种情况讨论
                // 1.拦截的是普通方法
                // 拦截的类实例只有一个方法，则必选用这个方法，即使方法签名完全不一样
                // 拦截的类实例有多个方法
                //    优先根据返回值类型匹配方法，返回值一样，
                //       只有一个方法，则采用该方法
                //       有多个方法，根据参数匹配，参数完全一样的，则用该方法
                //          参数不匹配，根据方法名匹配，方法名一样，则用该方法
                //          都不匹配，报错模糊方法(这里情况非常复杂，没列举全，先这么理解，正常也不会写不一样的匹配)
                // .intercept(MethodDelegation.to(new ByteBuddyMethodDelegation()))

                // 2.拦截的是静态方法
                // 静态方法，没有实例，所以委托方法必须是静态的！否则，报错None of [] allows for delegation ...
                // 其他匹配规则，参考普通方法的匹配规则
                //.intercept(MethodDelegation.to(ByteBuddyMethodDelegation.class))

                // 3.指定注解@RuntimeType，主要用于方法委托（Method Delegation）时指定目标方法的返回值和参数的运行时动态类型检查
                // 一.动态返回类型匹配： 当目标方法的返回类型无法在编译时静态确定，或者需要与原始方法不一致时，@RuntimeType 允许在运行时确定返回类型。
                // 二.动态参数类型匹配： 对于方法参数，如果方法委托需要接受不同类型的参数（如从 Object 到具体类型），@RuntimeType 可以让 Byte Buddy 在运行时处理类型转换。
                // .intercept(MethodDelegation.to(ByteBuddyMethodDelegation.class))

                // 这里的匹配方法很乱，所以我们一般规范使用，约定如下：
                // 1.当传入的是实例时，，一般方法签名要完全保持一致--对应SelectUserNameInterceptor1
                // 2.当传入的是类时，一般方法签名要完全保持一致，且方法必须是静态的--对应SelectUserNameInterceptor2
                // 3.除上面的约束外，还可以通过注解@RuntimeType来指定，此时，方法签名才允许保持不一致--对应SelectUserNameInterceptor3
                // .intercept(MethodDelegation.to(new SelectUserNameInterceptor1()))
                // .intercept(MethodDelegation.to(SelectUserNameInterceptor2.class))
                .intercept(MethodDelegation.to(new SelectUserNameInterceptor3()))
                .make();

        unloaded.saveIn(new File(path));
        DynamicType.Loaded<ByteBuddyDemoService> load = unloaded.load(ClassLoader.getSystemClassLoader());

        // 创建对象实例并传递构造函数参数
        Object obj = load.getLoaded().newInstance();
        for (Method method : obj.getClass().getMethods()) {
            if (method.getName().equals("selectUserName")) {
                System.out.println("selectUserName(100L) = " + method.invoke(obj, 100L));
            }
        }
    }

    /**
     * 测试方法委托时，动态修改入参
     * 1.必须先声明一个接口，用于覆盖原有的callback方法
     * 2.必须在方法委托的时候，绑定binder，表示在拦截方法中注入@Morph对应的回调
     * 3.在拦截方法中，通过@Morph注解，获取到对应的回调，并调用其call方法，传入参数
     *
     * @throws Exception
     */
    @Test
    public void testMethodDelegationMorph() throws Exception {
        DynamicType.Unloaded<ByteBuddyDemoService> unloaded = new ByteBuddy()
                .rebase(ByteBuddyDemoService.class)
                .name("org.example.RebaseByteBuddyDemoServiceWithMethodDelegationMorph")
                .method(named("selectUserName").and(takesArguments(1).and(ElementMatchers.returns(String.class))))
                // 修改委托方法，注入自定义的回调类，这样才能动态修改参数
                .intercept(MethodDelegation.withDefaultConfiguration()
                        .withBinders(Morph.Binder.install(OverrideCallback.class))
                        .to(new SelectUserNameInterceptor4())
                )
                .make();

        unloaded.saveIn(new File(path));
        DynamicType.Loaded<ByteBuddyDemoService> load = unloaded.load(ClassLoader.getSystemClassLoader());

        // 创建对象实例并传递构造函数参数
        Object obj = load.getLoaded().newInstance();
        for (Method method : obj.getClass().getMethods()) {
            if (method.getName().equals("selectUserName")) {
                System.out.println("selectUserName(100L) = " + method.invoke(obj, 100L));
            }
        }
    }

    /**
     * 测试修改构造函数
     *
     * @throws Exception
     */
    @Test
    public void testModifyConstructor() throws Exception {
        DynamicType.Unloaded<ByteBuddyDemoService> unloaded = new ByteBuddy()
                .rebase(ByteBuddyDemoService.class)
                .name("org.example.RebaseByteBuddyDemoServiceWithModifyConstructor")
                .constructor(takesArguments(1))
                // 这里必须要指定为SuperMethodCall，否则，会报错，因为要先调用父类的构造方法或者变基后的构造方法
                .intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.to(new ConstructorInterceptor5())))
                .make();

        unloaded.saveIn(new File(path));
        DynamicType.Loaded<ByteBuddyDemoService> load = unloaded.load(ClassLoader.getSystemClassLoader());

        // 创建对象实例并传递构造函数参数
        Object obj = load.getLoaded().getDeclaredConstructor(String.class).newInstance("hbj");
        for (Method method : obj.getClass().getMethods()) {
            if (method.getName().equals("selectUserName")) {
                System.out.println("selectUserName(100L) = " + method.invoke(obj, 100L));
            }
        }
    }

    /**
     * 测试静态方法
     * ps：这里要学的知识点是是否可以使用redefine 和 subclass
     * subclass是继承，但静态方法是不可继承的，所以superCall是不行的，因此，subclass不行
     * redefine是重写，是对原有方法进行覆盖，也就是原有的方法没有了，那supercall当然也是没有的，因此也不行
     * 所以对静态方法拦截，只能用rebase
     *
     * @throws Exception
     */
    @Test
    public void testModifyStaticMethod() throws Exception {
        DynamicType.Unloaded<ByteBuddyDemoService> unloaded = new ByteBuddy()
                .rebase(ByteBuddyDemoService.class)
                // 原始方法没有保留
                //.redefine(ByteBuddyDemoService.class)
                // 静态方法不能继承
                //.subclass(ByteBuddyDemoService.class)
                .name("org.example.RebaseByteBuddyDemoServiceWithModifyStaticMethod")
                .method(named("testStaticMethod").and(isStatic()))
                .intercept(MethodDelegation.to(new StaticMethodInterceptor6()))
                .make();

        unloaded.saveIn(new File(path));
        DynamicType.Loaded<ByteBuddyDemoService> load = unloaded.load(ClassLoader.getSystemClassLoader());
        load.getLoaded().getDeclaredMethod("testStaticMethod").invoke(null);
    }

    /***
     * 测试类加载器
     * @throws Exception
     */
    @Test
    public void testClassLoader() throws Exception {
        DynamicType.Unloaded<ByteBuddyDemoService> unloaded = new ByteBuddy()
                .rebase(ByteBuddyDemoService.class)
                .name("org.example.RebaseByteBuddyDemoServiceWithClassLoader")
                .make();

        DynamicType.Loaded<ByteBuddyDemoService> loader;
        // 默认策略，是采用appClassLoader的子类加载器加载ByteArrayClassLoader
        loader = unloaded.load(ClassLoader.getSystemClassLoader());
        // 同默认加载器策略一样
        //loader = unloaded.load(ClassLoader.getSystemClassLoader(), ClassLoadingStrategy.Default.WRAPPER);
        // 注入到第一参数指定的类加载器，当前即注入appClassLoader
        loader = unloaded.load(ClassLoader.getSystemClassLoader(), ClassLoadingStrategy.Default.INJECTION);
        // 打破双亲委派机制，优先查找子类加载器是否加载，此时一般不报类已经加载错误，因为子类加载器没有加载过该类
        //loader = unloaded.load(ClassLoader.getSystemClassLoader(), ClassLoadingStrategy.Default.CHILD_FIRST);

        Class<?> loaded = loader.getLoaded();
        System.out.println("loaded = " + loaded);
    }

    /**
     * 测试类加载器定位路径
     *
     * @throws Exception
     */
    @Test
    public void testClassLoaderLocator() throws Exception {
        // 类文件定位器
        // ForCLassLoader:获取指定类加载器加载的类 -- 这里指定了系统类加载器，可以加载类加载器已经加载的类（包括父类能加载到的！）
        ClassFileLocator classFileLocator1 = ClassFileLocator.ForClassLoader.of(ClassLoader.getSystemClassLoader());
        byte[] resolve11 = classFileLocator1.locate("org.example.ByteBuddyDemoService").resolve();
        FileUtil.writeBytes(resolve11, new File(path + "/hbj/org/example/ByteBuddyDemoService.class"));
        // 能加载父类的吗？？(哦呵，可以按双亲委派机制查找定位到类，加载字节码哦)
        byte[] resolve12 = classFileLocator1.locate("java.lang.String").resolve();
        FileUtil.writeBytes(resolve12, new File(path + "/hbj/org/example/String.class"));
        try {
            Class<?> loadedCls = Class.forName("org.example.mybatisplus.ByteBuddyDemoService");
            System.out.println("类加载到了，我真是个小聪明：" + loadedCls);

            Class<?> loadedCls2 = Class.forName("java.lang.String");
            System.out.println("类加载到了，我真是个小聪明：" + loadedCls2);
        } catch (Exception e) {
            System.out.println("类没找到，真是武则天当寡妇，失去理智啊");
        }

        // ForJarLocator: Jar文件定位器，可以定位jar中的类，还没加载到类加载器中，只是二进制码或者说.class文件
        ClassFileLocator classFileLocator2 = ClassFileLocator.ForJarFile.of(new File("/Users/hbj/.m2/repository/commons-io/commons-io/2.6/commons-io-2.6.jar"));
        byte[] resolve21 = classFileLocator2.locate("org.apache.commons.io.FileUtils").resolve();
        FileUtil.writeBytes(resolve21, new File(path + "/hbj/org/apache/commons/io/FileUtils.class"));
        try {
            Class<?> loadedCls = Class.forName("org.apache.commons.io.FileUtils");
            System.out.println("类加载到了，真是小刀喇屁股，开眼了：" + loadedCls);
        } catch (Exception e) {
            System.out.println("类没找到，还没被加载到jvm中，预料之中");
        }

        // ForFolder: 文件夹定位器，可以定位文件夹中的类，还没加载到类加载器中，只是二进制码或者说.class文件
        // 这里要注意，包名最后会转成对应的路径
        ClassFileLocator classFileLocator3 = ClassFileLocator.ForFolder.of(new File("/Users/hbj/Study/java-agent-study/byte-buddy-study/tmp/classes/hbj/"), ClassFileVersion.ofThisVm());
        byte[] resolve31 = classFileLocator3.locate("org.apache.commons.io.FileUtils").resolve();
        FileUtil.writeBytes(resolve31, new File(path + "/hbj2/org/apache/commons/io/FileUtils.class"));

        // 其他加载方式让他去吧，这些够用了
    }

    /**
     * 测试类型描述
     * PS：
     * Byte Buddy 本身无法直接从 ClassFileLocator.Compound 加载类到 JVM 中。
     * ClassFileLocator 的职责是定位类文件（或字节码），而类的加载需要通过 ClassLoader 实现。
     * 因此，ClassFileLocator.Compound 提供了定位功能，加载功能则需要结合 ClassLoader 或 Byte Buddy 提供的 DynamicType 和 ClassLoadingStrategy。
     *
     * @throws Exception
     */
    @Test
    public void testTypeDescription() throws Exception {
        // 现在问题来了，从测试类文件定位器，我们已经加载到类文件的二进制字节码了，我怎么用ByteBuddy去加载和修改呢？？
        // 我们知道有三种方式来重写类，subclass,redefine,rebase,
        // 这三种方式其实都是通过TypeDescription来描述类信息的，这个也是ByteBuddy提供的类的描述方式
        // subclass(TypeDescription type)
        // redefine(TypeDescription type, ClassFileLocator classFileLocator)
        // rebase(TypeDescription type, ClassFileLocator classFileLocator)
        File jarFIle = new File("/Users/hbj/.m2/repository/commons-io/commons-io/2.17.0/commons-io-2.17.0.jar");
        try (
                ClassFileLocator systemClassLoaderLocator = ClassFileLocator.ForClassLoader.ofSystemLoader();
                // 和上面的写法等价，但下面的写法可以自己指定classloader
                // ClassFileLocator systemClassLoaderLocator = ClassFileLocator.ForClassLoader.of(ClassLoader.getSystemClassLoader());
                ClassFileLocator jarFileLocator = ClassFileLocator.ForJarFile.of(jarFIle);
                // 组合类的类文件定位器
                // 这里要说明一下为啥要进行组合？
                // 因为，如果指定类文件定位器，则只会在类文件的定位器里，搜索类信息，所以是加载不到系统类信息的，比如Object等，所以要一般要将系统类定位器加入进来，进行组合
                ClassFileLocator compound = new ClassFileLocator.Compound(jarFileLocator, systemClassLoaderLocator);
        ) {
            TypePool typePool = TypePool.Default.of(compound);
            // describe并不会触发类的加载
            TypeDescription typeDescription = typePool.describe("org.apache.commons.io.FileUtils").resolve();

            DynamicType.Unloaded<Object> unloaded = new ByteBuddy()
                    .redefine(typeDescription, compound)
                    .name("org.apache.commons.io.MyFileUtils")
                    .method(named("sizeOf"))
                    .intercept(FixedValue.value(0))
                    .method(named("openInputStream").and(takesArguments(File.class)))
                    .intercept(StubMethod.INSTANCE)
                    .make();

            //unloaded.saveIn(new File(path + "/hbj3/"));
            // 类class文件加载到jvm中是不会检查头部的依赖类是否都在jvm中的，所以，这里可能会报错，找不到FileExistsException类或者其他依赖包
            // 因此，需要将compound的依赖通过自定义类加载器进行加载
            // 以下写法肯定会报错，不管用哪种类加载器，因为指定了第一个类加载器（appClassLoader）是加载不到FileUtils里依赖的其他类,上面的compound主要作用是提供字节码定位功能，而不是直接加载类
            // Class<?> loaded = unloaded.load(ClassLoader.getSystemClassLoader(), ClassLoadingStrategy.Default.INJECTION).getLoaded();
            // 如果要正确加载，并把它的依赖类也导入，则需要使用自定义类加载器，将jar包或者依赖类，放到类加载器可扫描的位置，比如URLClassLoader
            // 这里测试类三种类加载器
            // INJECTION： OK
            Class<?> loaded;
            //loaded = unloaded.load(new URLClassLoader(new URL[]{jarFIle.toURI().toURL()}), ClassLoadingStrategy.Default.INJECTION).getLoaded();
            // WRAPPER：ERROR(类已经加载)，要重命名一下类 named("org.apache.commons.io.FileUtils2")
            loaded = unloaded.load(new URLClassLoader(new URL[]{jarFIle.toURI().toURL()}), ClassLoadingStrategy.Default.WRAPPER).getLoaded();
            // CHILD_FIRST：OK
            //loaded = unloaded.load(new URLClassLoader(new URL[]{jarFIle.toURI().toURL()}), ClassLoadingStrategy.Default.CHILD_FIRST).getLoaded();
            System.out.println("loaded = " + loaded);
            for (Method method : loaded.getMethods()) {
                System.out.println("method.getName() = " + method.getName());
            }

        }
    }
}
