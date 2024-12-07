package org.example;

import jdk.internal.org.objectweb.asm.Opcodes;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatchers;
import org.example.interceptor.ConstructorInterceptor5;
import org.example.interceptor.SelectUserNameInterceptor3;
import org.example.interceptor.SelectUserNameInterceptor4;
import org.example.interceptor.StaticMethodInterceptor6;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static net.bytebuddy.matcher.ElementMatchers.isStatic;
import static net.bytebuddy.matcher.ElementMatchers.named;

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
                                .and(ElementMatchers.takesArguments(1)))
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
                .method(named("selectUserName").and(ElementMatchers.takesArguments(1).and(ElementMatchers.returns(String.class))))
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
                .method(named("selectUserName").and(ElementMatchers.takesArguments(1).and(ElementMatchers.returns(String.class))))
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
                .constructor(ElementMatchers.takesArguments(1))
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

    @Test
    public void testModifyStaticMethod() throws Exception {
        DynamicType.Unloaded<ByteBuddyDemoService> unloaded = new ByteBuddy()
                .rebase(ByteBuddyDemoService.class)
                .name("org.example.RebaseByteBuddyDemoServiceWithModifyStaticMethod")
                .method(named("testStaticMethod").and(isStatic()))
                .intercept(MethodDelegation.to(new StaticMethodInterceptor6()))
                .make();

        unloaded.saveIn(new File(path));
        DynamicType.Loaded<ByteBuddyDemoService> load = unloaded.load(ClassLoader.getSystemClassLoader());
        load.getLoaded().getDeclaredMethod("testStaticMethod").invoke(null);
    }
}
