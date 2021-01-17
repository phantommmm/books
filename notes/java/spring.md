### Spring

#### Bean作用域

- singleton : 唯一 bean 实例，Spring 中的 bean 默认都是单例的。
- prototype : 每次请求都会创建一个新的 bean 实例。
- request : 每一次HTTP请求都会产生一个新的bean，该bean仅在当前HTTP request内有效。
- session : 每一次HTTP请求都会产生一个新的 bean，该bean仅在当前 HTTP session 内有效。
- global-session： 全局session作用域，仅仅在基于portlet的web应用中才有意义，Spring5已经没有了。Portlet是能够生成语义代码(例如：HTML)片段的小型Java Web插件。它们基于portlet容器，可以像servlet一样处理HTTP请求。但是，与 servlet 不同，每个 portlet 都有不同的会话

#### Spring Bean生命周期

**1.**Spring启动，查找并加载需要被Spring管理的Bean，进行**Bean的实例化**（new）

**2.**使用依赖注入，按照Bean定义信息配置Bean所有属性 **属性注入IOC**

**3.**如果Bean实现了BeanNameAware接口的话，Spring将Bean的Id传递给setBeanName()方法

**4.**如果Bean实现了BeanFactoryAware接口的话，Spring将调用setBeanFactory()方法，将BeanFactory容器实例传入

**5.**如果Bean实现了ApplicationContextAware接口的话，Spring将调用Bean的setApplicationContext()方法，将bean所在应用上下文引用传入进来。

**6.**如果Bean实现了BeanPostProcessor接口，Spring就将调用他们的postProcessBeforeInitialization()方法。**对于所有Bean初始化前后的管理、加工接口，例如：打印日记、记录时间**

**7.**如果Bean 实现了InitializingBean接口，Spring将调用他们的afterPropertiesSet()方法。类似的，如果bean使用init-method声明了初始化方法，该方法也会被调用

**8.**如果Bean 实现了BeanPostProcessor接口，Spring就将调用他们的postProcessAfterInitialization()方法。

**9.**此时，Bean已经准备就绪，可以被应用程序使用了。他们将一直驻留在应用上下文中，直到应用上下文被销毁。

**10.**如果bean实现了DisposableBean接口，Spring将调用它的destory()接口方法，同样，如果bean使用了destory-method 声明销毁方法，该方法也会被调用。

![img](https://pic3.zhimg.com/80/v2-2a8565eb02d88025d0fbe1015ef323d6_1440w.jpg)

#### MVC流程

Web 容器启动时会通知 Spring 初始化容器，加载 Bean 的定义信息并初始化所有单例 Bean，然后遍历容器中的 Bean，获取每一个 Controller 中的所有方法访问的 URL，将 URL 和对应的 Controller 保存到一个 Map 集合中。

所有的请求会转发给 DispatcherServlet 前端处理器处理，DispatcherServlet 会请求 HandlerMapping 找出容器中被 `@Controler` 注解修饰的 Bean 以及被 `@RequestMapping` 修饰的方法和类，生成 Handler 和 HandlerInterceptor 并以一个 HandlerExcutionChain 处理器执行链的形式返回。

之后 DispatcherServlet 使用 Handler 找到对应的 HandlerApapter，通过 HandlerApapter 调用 Handler 的方法，将请求参数绑定到方法的形参上，执行方法处理请求并得到 ModelAndView。

最后 DispatcherServlet 根据使用 ViewResolver 试图解析器对得到的 ModelAndView 逻辑视图进行解析得到 View 物理视图，然后对视图渲染，将数据填充到视图中并返回给客户端。



#### 多线程访问Controller

每一个请求都由一个线程来处理。

**tomcat是怎样多线程处理http请求并将代码执行到controller里的**

1.线程池，thread = threadPool.getThread(),

thread.executeHttp(htttpRequest),thread的start方法执行里面调用：每个thread里再获取所有的controller，根据传进入thread的httprequest找到相应的controllerer对象获取出来，controller对象就开始执行了嘛。

2.轨迹：线程池-》线程-》传request->线程找到对应的controller，执行

3.Main线程负责向子线程传入参数，任何线程的启动都是由主线程来启动加载的

像Tomcat的线程池 `maxThreads` 是200， `minSpareThreads` 是25。实际中单个Tomcat服务器的最大并发数只有几百，部分原因就是只能同时处理这么多线程上的任务。

#### 为什么使用spring

**1.方便解耦，简化开发**

通过Spring提供的IoC容器，我们可以将对象之间的依赖关系交由Spring进行控制，避免硬编码所造成的过度程序耦合。有了Spring，用户不必再为单实例模式类、属性文件解析等这些很底层的需求编写代码，可以更专注于上层的应用。

**3. **声明式事务的支持

在Spring中，我们可以从单调烦闷的事务管理代码中解脱出来，通过声明式方式灵活地进行事务的管理，提高开发效率和质量。

**5.方便集成各种优秀框架**

Spring不排斥各种优秀的开源框架，相反，Spring可以降低各种框架的使用难度，Spring提供了对各种优秀框架（如Struts,Hibernate、Hessian、Quartz）等的直接支持。

**6.降低Java EE API的使用难度**

　　Spring对很多难用的Java EE API（如JDBC，JavaMail，远程调用等）提供了一个薄薄的封装层，通过Spring的简易封装，这些Java EE API的使用难度大为降低。

#### Spring SpringMVC SpringBoot SpringCloud

**spring 核心是AOP 面向横切面编程 IOC 提供了依赖注入的容器 包含了很多衍生产品 boot、jpa等**

**springMVC是基于spring的MVC框架，主要解决WEB开发问题，包括前端视图开发、文件配置、后台接口逻辑开发等**

**Springboot是Spring框架衍生的快速开发，免去各种配置文件，实现自动配置，简化开发者开发，同时集成了大量第三方库（JDBC Redis）**

**springCloud基于springboot,管理多个springboot单体微服务，更加关注全局的微服务管理**

#### Springboot

之所以能够做到简化配置文件，主要依靠策略：**开箱即用**  **约定大于配置**

**开箱即用：**在开发过程中，通过maven项目的pom文件中添加相关依赖包，然后通过相应的注解来代替繁琐的XML配置以管理对象的生命周期。（Redis JDBC Mail等）**自动装配**

**约定大于配置：**我们的配置文件（.yml）应该放在哪个目录下，配置文件的命名规范，项目启动时扫描的Bean，组件的默认配置是什么样的（比如SpringMVC的视图解析器）等等这一系列的东西，都可以被称为约定，

例如 SpringBoot的注解扫描的默认规则是SpringBoot的入口类所在包及其子包。

SpringBoot所有自动配置类都是在启动的时候进行扫描并加载，通过spring.factories可以找到自动配置类的路径，但是不是所有存在于spring,factories中的配置都进行加载，而是通过@ConditionalOnClass注解进行判断条件是否成立（只要导入相应的stater，条件就能成立），如果条件成立则加载配置类，否则不加载该配置类。 

#### 自动装配

SpringBootApplication启动类注解 包括（@SpringBootConfiguration、@EnableAutoConfiguration和@ComponentScan）组成。

`@SpringBootConfiguration`：底层是**Configuration**注解，说白了就是支持**JavaConfig**的方式来进行配置(**使用Configuration配置类等同于XML文件**)。

`@ComponentScan`：就是**扫描**注解，默认是扫描**当前类下**的package。将`@Controller/@Service/@Component/@Repository`等注解加载到IOC容器中。

`@EnableAutoConfiguration`：开启**自动配置**功能(后文详解)，**自动载入**应用程序所需要的所有**默认配置**。

包括（@AutoConfigurationPackage，@Import）注解



`@AutoConfigurationPackage`：自动配置包

在**默认**的情况下就是将：主配置类(`@SpringBootApplication`)的所在包及其子包里边的组件扫描到Spring容器中。

**和ComponentScan的区别：**

你用了Spring Data JPA，可能会在实体类上写`@Entity`注解。这个`@Entity`注解由`@AutoConfigurationPackage`扫描并加载，而我们平时开发用的`@Controller/@Service/@Component/@Repository`这些注解是由`ComponentScan`来扫描并加载的。

这二者**扫描的对象是不一样**的。

`@Import`：给IOC容器导入组件

- Spring启动的时候会扫描所有jar路径下的`META-INF/spring.factories`，将其文件包装成Properties对象
- 从Properties对象获取到key值为`EnableAutoConfiguration`的数据，然后添加到容器里边,自动装配类就生效，帮助我们完成自动装配工作。

**EnableAutoConfiguration**内部实际上就去加载`META-INF/spring.factories`文件的信息，然后筛选出以`EnableAutoConfiguration`为key的数据，加载到IOC容器中，实现自动配置功能！

**每次全量加载配置类？**

并不是把spring.factory的配置全量加载进来，而是通过@ConditionalOnXXX注解 只有满足条件才加载

![img](https://pic2.zhimg.com/v2-8d994b8cbca6f52c67020a79364256dd_b.jpg)

**结论**

SpringBoot所有自动配置类都是在启动的时候进行扫描并加载，通过spring.factories可以找到自动配置类的路径，但是不是所有存在于spring,factories中的配置都进行加载，而是通过@ConditionalOnClass注解进行判断条件是否成立（只要导入相应的stater，条件就能成立），如果条件成立则加载配置类，否则不加载该配置类。 

![img](https://pic3.zhimg.com/v2-ec490d9baecef2b0ff77af59598c8c12_b.jpg)



#### @Resource与@Autowired

这两个注解都可用于装配bean

**@Resource** byName Java

使用在 成员属性 和 setter方法上

默认情况下@Resource按照名称注入，如果没有显式声明名称则按照变量名称或者方法中对应的参数名称进行注入。

@Resource装配顺序
　　1. 如果同时指定了name和type，则从Spring上下文中找到唯一匹配的bean进行装配，找不到则抛出异常
　　2. 如果指定了name，则从上下文中查找名称（id）匹配的bean进行装配，找不到则抛出异常
　　3. 如果指定了type，则从上下文中找到类型匹配的唯一bean进行装配，找不到或者找到多个，都会抛出异常
　　4. 如果既没有指定name，又没有指定type，则自动按照byName方式进行装配；如果没有匹配，则回退为一个原始类型进行匹配，如果匹配则自动装配；

**@Autowired** byType Spring

@Autowired通常适用于构造函数，成员变量以及setter方法上。

结合@Qualifter使用按名称装配



#### @Autowired原理

1. 通过`findAutowiringMetadata` 方法获取带有 `@Autowired` 注解的元数据metadata。

2. 从缓存中取出metadata数据，然后通过 `inject` 方法反射注入，分成字段和方法，如果是字段的话，则是去赋值 `set` ，如果是方法，则执行 `method.invoke`方法

   ```
   @Autowired
   ObjectTest objectTest; //采用字段赋值，赋值对象的引用
   ```

   

**注入的bean和用它的bean的关系是如何维护的？**

无论以何种方式注入，注入的bean就相当于类中的一个普通对象引用，这是它的实例化是spring去容器中找符合的bean进行实例化，并注入到类当中的。他们之间的关系就是普通的一个对象持有另一个对象引用的关系。只是这些对象都是spring当中的bean而已。

#### BeanFactory与FactoryBean区别？

BeanFacotry是个IOC容器，Spring中的Bean由BeanFactory管理,并且对IOC进行规范。

FactoryBean是一个接口，使用了工厂方法模式，用户通过实现该接口**自定义定制实例化Bean**的逻辑，归BeanFactory管理。

```
public interface FactoryBean<T> {  
 //返回由FactoryBean创建的Bean实例
    T getObject() throws Exception; 
    返回由FactoryBean创建的Bean类型
    Class<?> getObjectType();  
    boolean isSingleton();  
}   

public class FactoryBeanLearn implements FactoryBean {

    @Override
    public Object getObject() throws Exception {
        //这个Bean是我们自己new的，这里我们就可以控制Bean的创建过程了
        return new FactoryBeanServiceImpl();
    }
```

BeanA实现FactoryBean的接口，A则成了一个工厂，根据A的名称获取到的实际上是工厂加工后，通过getBean()方法返回的对象，而不是FactoryBean本身，如果想要获取A自身实例，通过名称前加‘&’符号。

#### BeanFactory与ApplicationContext区别？

`BeanFactory` Spring低级接口，提供最简单的容器功能 获取实例化对象和注册实例化对象，可以简单理解为Map,key为 beanName value为 bean实例

`BeanFactory` 启动时不会初始化bean,只有在使用到Bean时才初始化，懒加载。

`ApplicationContext` 高级容器，具备更多功能，如AOP,资源访问ResourceLoader、国际化MessageSource等

`ApplicationContext` 启动时就初始化所有Bean,非懒加载。

**总结**

`BeanFactory` 懒加载，如果Bean没有注入，在调用getBean方法后才会抛出异常。

`ApplicationContext` 启动就会初始化并检查，可以及时检查依赖是否注入（建议使用）

#### AOP

能够将那些与业务无关，**却为业务模块所共同调用的逻辑或责任（例如事务处理、日志管理、权限控制等）封装起来**，便于**减少系统的重复代码**，**降低模块间的耦合度**，并**有利于未来的可拓展性和可维护性**。 **Spring AOP就是基于动态代理的**

如果要代理的对象，实现了某个接口，那么Spring AOP会使用**JDK Proxy**，去创建代理对象，而对于没有实现接口的对象，就无法使用 JDK Proxy 去进行代理了，这时候Spring AOP会使用**Cglib** ，这时候Spring AOP会使用 **Cglib** 生成一个被代理对象的子类来作为代理。

**概念**

(1)Aspect(切面): 通常是一个**类**，里面可以定义**切入点**和**通知**

(2)JoinPoint(连接点): 程序执行过程中明确的点，一般是方法的调用。

(3)Advice(通知): 在特定的切入点上执行的增强处理，有before,after,afterReturning,afterThrowing,around

(4)PointCut(切入点):就是带有通知的连接点，表明哪些Target目标需要被增强。

**AOP中，所有方法执行都是JoinPoint,带有通知的JoinPoint是PointCut**。



#### IOC简述

IoC 即控制反转，简单来说就是把原来代码里需要实现的对象创建、依赖反转给容器来帮忙实现，需要创建一个容器并且需要一种描述让容器知道要创建的对象间的关系，在 Spring 中管理对象及其依赖关系是通过 Spring 的 IoC 容器实现的。

IoC 的实现方式有依赖注入和依赖查找，由于依赖查找使用的很少，因此 IoC 也叫做依赖注入。

依赖注入指对象被动地接受依赖类而不用自己主动去找，对象不是从容器中查找它依赖的类，而是在容器实例化对象时主动将它依赖的类注入给它。

假设一个 Car 类需要一个 Engine 的对象，那么一般需要需要手动 new 一个 Engine，利用 IoC 就只需要定义一个私有的 Engine 类型的成员变量，容器会在运行时自动创建一个 Engine 的实例对象并将引用自动注入给成员变量。

#### IOC原理  

**反射+XML**

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190420155455877.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3poYW5nY29uZ3lpNDIw,size_16,color_FFFFFF,t_70)

当Spring容器启动的时候，spring的全局bean的管理器会去xml配置文件中扫描的包下面获取到所有的类，并根据你使用的注解，进行对应的封装，封装到Spring容器中进行管理，一旦容器初始化完毕，beanID以及bean实例化的类对象信息就全部存在了，现在我们需要在某个service里面调用另一个bean的某个方法的时候，我们只需要依赖注入进来另一个bean的Id即可，调用的时候，spring会去初始化完成的bean容器中获取即可，如果存在就把依赖的bean的类的实例化对象返回给你，你就可以调用依赖的bean的相关方法或属性等；

a. 加载配置文件，解析成 BeanDefinition 放在 Map 里。

b. 调用 getBean 的时候，从 BeanDefinition 所属的 Map 里，拿出 Class 对象进行实例化，同时，如果有依赖关系，将递归调用 getBean 方法 —— 完成依赖注入。

![img](https://img-blog.csdn.net/20150320140507987)

#### IOC好处

Bean由IOC容器管理

IOC核心思想在于 资源不由使用资源的双方管理，而由不使用资源的第三方管理，这可以带来很多好处。

第一，资源集中管理，实现资源的可配置和易管理。第二，降低了使用资源双方的依赖程度，也就是我们说的耦合度。

依赖注入 甲开放接口，在它需要的时候，能够将乙注入。

#### IOC容器初始化过程

容器的初始化由 `refresh` 方法启动。在创建 IoC 容器前如果已有容器存在，需要把已有的容器销毁，保证在 `refresh` 方法后使用的是新创建的 IoC 容器。

![springIOC的大体图示](https://img-blog.csdnimg.cn/20181108175648394.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM0MjAzNDky,size_16,color_FFFFFF,t_70)

IOC初始化流程

1. Resource定位；指对BeanDefinition的资源定位过程。Bean 可能定义在XML中，或者是一个注解，或者是其他形式。这些都被用Resource来定位, 读取Resource获取BeanDefinition 并注册到 Bean定义注册表中。
2. BeanDefinition的载入；把用户定义好的Javabean表示为IoC容器内部的数据结构，这个容器内部的数据结构就是BeanDefinition。
3. 向IoC容器注册这些BeanDefinition。

获取Bean的流程

1. 第一次向容器getBean操作会触发Bean的创建过程,实列化一个Bean时,根据BeanDefinition中类信息等实列化Bean.
2. 将实列化的Bean放到单例Bean缓存内。
3. 此后再次获取向容器getBean就会从缓存中获取。

#### Servlet是单实例多线程模式

**单实例**

当请求到来时，对应的servlet只会被实例化一次。

**多线程**

Servlet容器维护了一个线程池来服务请求，线程池内的**工作者线程**等待执行代码，Servlet容器使用一个**调度线程**来管理工作者线程，当容器收到一个访问Servlet的请求，调度者线程从线程池中选出一个工作者线程，将请求传递给该线程，然后由该线程来执行Servlet的service方法，当容器同时收到对同一Servlet的多个请求，那这个Servlet的service方法将在多线程中并发的执行，线程复制一份servlet实例对象的**成员变量**和**临时变量**，紧接着开始自己的事务处理。

**多个servlet?**

多个servlet是依据业务划分的，比如结账买单是一个servlet处理，客户预约是另外一个servlet处理



#### Mybaties启动过程

**1.**SqlSessionFactoryBuilder加载解析xml文件，创建Configuration对象完成初始化

**2.**用Configuration对象创建SqlSessionFactory对象

**3.**调用SqlSessionFactory对象的openSession方法，获取SqlSession类，该类封装了和数据库的连接，还有一系列事务管理功能。

**4.**通过SqlSession类的方法扫描到配置文件，生成mapperProxy对象，该对象将读取对象的xxxMapper.xml文件和对应的xxx接口。采用动态代理方法，生成一个接口实现类。

**5.**执行sql语句时，通过反射的方式，调用实现类的方法，其中有两个参数，对应的方法名和sql语句。这由mapperMethed对象进行封装。

**6.**mapperMethed传入sqlsession内封装的CRUD方法，内有对应的执行器类Executor；  

**7.** 执行器类将调用方法query（），取出mapperMethed内的sql语句，生成prepareStatement。  

   最后，像执行jdbc一样，进行数据库查询。



#### Springboot启动过程

**1.初始化SpringApplication对象**

通过**SpringFactoriesLoader**找到**spring.factories**文件中配置的
**ApplicationContextInitializer**和**ApplicationListener**

**2.执行run方法**

1.创建了应用的监听器SpringApplicationRunListeners并开始监听

2.根据配置文件及参数配置环境(Environment)加入到监听器对象中(SpringApplicationRunListeners)

3.调用createApplicationContext()方法创建上下文对象，创建上下文对象同时会注册spring的核心组件类（ConfigurationClassPostProcessor 、AutowiredAnnotationBeanPostProcessor 等）

4.调用refreshContext() 方法启动Spring容器和内置的Servlet容器



#### Springboot如何启动Mybatis

在refreshContext（）方法调用后，会去将加载进来的各种配置进行处理，装入我们的springboot容器中，包括了spring，和mybatis。mybaitis的具体过程，其实就是上面初始化的过程，最后把sqlsessionFactory放入了容器中。

#### Mybatis缓存

**缓存：**将数据存放到内存中，用于减轻数据库查询的压力，用Hashmap存储数据。

**一级缓存（默认）**

默认作用域是一个SqlSession,在同一个SqlSession中，执行相同的查询，第一次取数据库中查询后，写入缓存中，后面直接取缓存中取。（当发生增删改操作或手动清空，会清空缓存）

若不想使用一级缓存，将一级缓存范围指定为StateMent，则每执行完一个Mapper中的语句都会清空一级缓存。

**注意：**当Mybatis整合Spring后，直接通过Spring注入Mapper的形式，每个事务对应不同的SqlSession,即不会命中一级缓存，但是在同一个事务中时共用的是同一个SqlSession。

**问题** 使用一级缓存的时候，因为缓存不能跨会话共享，不同的会话之间对于相同的数据可能有不一样的缓存。

**二级缓存**

作用域是namespace下的mapper映射文件内容，多个SqlSession共享，默认使用LRU算法回收对象。

只适用于在单表上使用二级缓存



#### #与$区别？

#{}是预编译处理，${}是字符串替换。

Mybatis在处理#{}时，会将sql中的#{}替换为?号，调用PreparedStatement的set方法来赋值；

Mybatis在处理${}时，就是把${}替换成变量的值。

使用#{}可以有效的防止SQL注入，提高系统安全性。

#### Spring事务为什么只作用于public

Spring本身是没有事务一说的，数据库对事务的支持才是Spring事务的本质。

Spring事务基于动态代理，包括JDK和CGLIB两种。

JDK需要实现接口，重写方法，那么就必须要方法为Public

CGLIB需要继承类，重写方法，一样需要方法为Public

Spring首先会对添加了注解的方法进行验证，若不为Public不进行后面步骤

### @RequestParam和@PathVariable

`@RequestParam` 是从request里面拿取值，而 `@PathVariable` 是从一个URI模板里面来填充

比如http://localhost:8080/springmvc/hello/101?param1=10&param2=20

```
@RequestMapping("/hello/{id}")
    public String getDetails(@PathVariable(value="id") String id,
    @RequestParam(value="param1", required=true) String param1,
    @RequestParam(value="param2", required=false) String param2){
.......
}
```

### 依赖循环

 Spring的单例对象的初始化主要分为三步：

​    （1）createBeanInstance：实例化，其实也就是调用对象的构造方法实例化对象

​    （2）populateBean：填充属性，这一步主要是多bean的依赖属性进行填充

​    （3）initializeBean：调用spring xml中的init 方法。

循环依赖主要发生在第一、第二步，也就是构造器循环依赖和field循环依赖。

**三级缓存**

```
singletonFactories ： 单例对象工厂的cache 三级

earlySingletonObjects ：提前暴光的单例对象的Cache 二级

singletonObjects：单例对象的cache 一级
```

在创建bean的时候，首先想到的是从cache中获取这个单例的bean，这个缓存就是singletonObjects。如果获取不到，并且对象正在创建中，就再从二级缓存earlySingletonObjects中获取。如果还是获取不到且允许singletonFactories通过getObject()获取，就从三级缓存singletonFactory.getObject()(三级缓存)获取，如果获取到了则：从singletonFactories中移除，并放入earlySingletonObjects中。其实也就是从三级缓存移动到了二级缓存。



从上面三级缓存的分析，我们可以知道，Spring解决循环依赖的诀窍就在于singletonFactories这个三级cache。这个cache的类型是ObjectFactory。这里就是解决循环依赖的关键，发生在createBeanInstance之后，也就是说单例对象此时已经被创建出来(调用了构造器)。这个对象已经被生产出来了，虽然还不完美（还没有进行初始化的第二步和第三步），但是已经能被人认出来了（根据对象引用能定位到堆中的对象），所以Spring此时将这个对象提前曝光出来让大家认识，让大家使用。

**关键点 ** `singletonFactories` 三级缓存（提前暴露），当对象被创建（调用了构造器），但还没被初始化时，会被放入该三级缓存中。



假设对象A和对象B循环依赖：

| 步骤 |                             操作                             | 三层列表中的内容                                             |
| :--- | :----------------------------------------------------------: | :----------------------------------------------------------- |
| 1    |                       开始初始化对象A                        | singletonFactories：earlySingletonObjects：singletonObjects： |
| 2    |            调用A的构造，把A放入singletonFactories            | singletonFactories：A earlySingletonObjects：singletonObjects： |
| 3    |               开始注入A的依赖，发现A依赖对象B                | singletonFactories：A earlySingletonObjects：singletonObjects： |
| 4    |                       开始初始化对象B                        | singletonFactories：A ,B earlySingletonObjects：singletonObjects： |
| 5    |            调用B的构造，把B放入singletonFactories            | singletonFactories：A,B earlySingletonObjects：singletonObjects： |
| 6    |               开始注入B的依赖，发现B依赖对象A                | singletonFactories：A,B earlySingletonObjects：singletonObjects： |
| 7    | 开始初始化对象A，发现A在singletonFactories里有，则直接获取A，把A放入earlySingletonObjects，把A从singletonFactories删除 | singletonFactories：B earlySingletonObjects：A singletonObjects： |
| 8    |                     对象B的依赖注入完成                      | singletonFactories：B earlySingletonObjects：A singletonObjects： |
| 9    | 对象B创建完成，把B放入singletonObjects，把B从earlySingletonObjects和singletonFactories中删除 | singletonFactories：earlySingletonObjects：A singletonObjects：B |
| 10   |       对象B注入给A，继续注入A的其他依赖，直到A注入完成       | singletonFactories：earlySingletonObjects：A singletonObjects：B |
| 11   | 对象A创建完成，把A放入singletonObjects，把A从earlySingletonObjects和singletonFactories中删除 | singletonFactories：earlySingletonObjects： singletonObjects：A,B |
| 12   |           循环依赖处理结束，A和B都初始化和注入完成           | singletonFactories：earlySingletonObjects：singletonObjects：A,B |

 