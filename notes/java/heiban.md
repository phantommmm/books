**把IOC容器的工作模式看做是工厂模式的升华，可以把IOC容器看作是一个工厂，这个工厂里要生产的对象都在配置文件中给出定义，然后利用编程语言提供的反射机制，根据配置文件中给出的类名生成相应的对象**。

IOC最大的好处是把对象生成放在了XML里定义

  <!--也可以使用构造器参数命名来指定值的类型-->
    <bean id="user3" class="org.spring.ioc.entity.User">
        <constructor-arg name="id" value="1234"/>
        <constructor-arg name="name" value="spring"/>
    </bean>

Setter

<bean id="author" class="org.spring.ioc.entity.Author">
        <property name="name" value="luoliang"/>
        <property name="age" value="18"/>
        <property name="url" value="https://luoliangdsga.github.io"/>
    </bean>

**@Autowired与@Resource**

1、 @Autowired与@Resource都可以用来装配bean. 都可以写在字段上,或写在setter方法上。

2、 @Autowired默认按类型装配（这个注解是属业spring的），默认情况下必须要求依赖对象必须存在，如果要允许null值，可以设置它的required属性为false，如：@Autowired(required=false) ，如果我们想使用名称装配可以结合@Qualifier注解进行使用，如下：

```
@Autowired()@Qualifier("baseDao")
privateBaseDao baseDao;
```

3、@Resource（这个注解属于J2EE的），默认按照名称进行装配，名称可以通过name属性进行指定，如果没有指定name属性，当注解写在字段上时，默认取字段名进行安装名称查找，如果注解写在setter方法上默认取属性名进行装配。当找不到与名称匹配的bean时才按照类型进行装配。但是需要注意的是，如果name属性一旦指定，就只会按照名称进行装配。

```
@Resource(name="baseDao")
privateBaseDao baseDao;
```

**创建线程消耗什么资源**

Java线程的线程栈所占用的内存是在Java堆外的，所以是不受java程序控制的，只受系统资源限制，默认一个线程的线程栈大小是1M（当让这个可以通过设置`-Xss`属性设置，但是要注意栈溢出问题），但是，如果每个用户请求都新建线程的话，1024个用户光线程就占用了1个G的内存，如果系统比较大的话，一下子系统资源就不够用了，最后程序就崩溃了。

需要给它分配内存、列入调度,同时在线程切换的时候还要执行内存换页,CPU 的缓存被 清空,切换回来的时候还要重新从内存中读取信息,破坏了数据的局部性。

**List 转 int**

```
List<Integer> list=new ArrayList<>();
list.add(1);
Integer[] arr=new Integer[1]; //声明为Integer
arr=list.toArray(new Integer[1]);

int[] arr2=new int[2];
arr2=list2.stream().mapToInt(Integer::valueOf).toArray();
```

**int 转 List**

```
list=Arrays.asList(arr);
```

