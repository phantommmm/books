### 介绍

项目简介：基于Netty、Zookeeper实现的分布式RPC框架，参考Dubbo实现了分层结构，基于动态代理实现透明RPC，并为其编写了Spring Boot Starter，采用多种序列化方式并进行性能测试。

技术点：

Netty实现长连接式的RPC，包括心跳保持、断线重连、解决粘包半包等

Zookeeper实现分布式服务注册与发现，并实现了轮询、随机、加权随机、一致性哈希等负载均衡算法

实现了TCP、HTTP、InJvm等多种协议

实现了Protostuff、jdk、fastjson、hessian序列化方式，并进行性能对比

使用适配者设计模式，提高扩展性和复用性

### 问题

#### 为什么不用http而用rpc?

**http**

**缺点：** 有用信息占比少，http工作在第七层，包含了大量的http头 等信息，效率、安全性低。请求量大会受到瓶颈

**优点：** 可读性、通用性高

**rpc**

**优点：** 更加灵活定制化、保密、效率高，包含重试机制、路由策略、负载均衡等复杂策略

**缺点：** 需要交互双方清楚定制规则（如序列化反序列化等），实现复杂

**可读性和效率之间的抉择**，**通用性和易用性之间的抉择**

#### 为什么使用Dubbo

Dubbo封装了 **负载均衡** **服务注册和发现** **熔断降级** 等高级特性，更加定制化。

面向接口的远程方法调用，智能容错和负载均衡，以及服务自动注册和发现。

#### 为什么使用ZooKeeper做注册中心

ZK数据存储于内存，性能高。

ZK支持集群，高可用。

ZK支持事件监听机制吧

#### 为什么使用Netty

- NIO的类库和API繁杂，使用麻烦，你需要熟练掌握Selector,ServerSocketChannel、SocketChannel、ByteBuffer等。
- 可靠性能力补齐，工作量和难度非常大。例如客户端面临断连重连、网络闪断、半包读写、失败缓存、网络拥塞和异常码流的处理等问题，NIO编程的特点是功能开发相对容易，但是可靠性能力补齐的工作量和难度都非常大。
- JDK NIO的BUG，例如epoll bug，它会导致Selector空轮询，最终导致CPU 100%。官方验证例子基于以上原因，在大多数场景下，不建议直接使用JDK的NIO类库，除非你精通NIO编程或者有特殊的需求。在绝大多数的业务场景中，我们可以使用NIO框架Netty来进行NIO编程，它既可以作为客户端也可以作为服务端，同时支持UDP和异步文件传输，功能非常强大。
  Netty特性总结如下：
- API使用简单，开发门槛低
- 功能强大，预置了多种编解码功能，支持多种主流协议
- 定制能力强，可以通过ChannelHandler对通信框架进行灵活地扩展
- 性能高，通过与其他业界主流的NIO框架对比，Netty的综合性能最优
- 成熟、稳定，Netty修复了已经发现的所有JDK NIO BUG，业务开发人员不需要再为NIO的BUG而烦恼
- 社区活跃，版本迭代周期短，发现的BUG可以被及时修复，同时更多的新功能会加入

   

#### Netty高性能体现？

(1) IO线程模型 ：同步非阻塞，用最少的资源做更多的事情。
(2) 内存零拷贝 ：尽量减少不必要的内存拷贝，实现了更高效率的传输。
(3) 内存池设计 ：申请的内存可以重用，主要指直接内存。内部实现是用一颗二叉查找树管理内存分配情况。
(4) 串行化处理读写 ：避免使用锁带来的性能开销。即消息的处理尽可能再同一个线程内完成，期间不进行线程切换，这样就避免了多线程竞争和同步锁。表面上看，串行化设计似乎CPU利用率不高，并发程度不够。但是，通过调整NIO线程池的线程参数，可以同时启动多个串行化的线程并行运行，这种局部无锁化的串行线程设计相比一个队里-多个工作线程模型性能更优。
(5) 高性能序列化协议 ：支持protobuf等高性能序列化协议。
(6) 高效并发编程的体现 ：volatile的大量、正确使用；CAS和原子类的广泛使用；线程安全容器的使用；通过读写锁提升并发性能。

#### RPC关键

**`序列化性能`**

**指标**

1.对象序列化后的字节大小（码流）

2.序列化（反序列化）对象消耗时间

二进制协议的 protostuff 要比文本协议的 jackson fastjson 有明显优势；文本协议中，jackson(开启了afterburner) 要比 fastjson 有明显的优势。

**`消息格式`**

```
public class RpcInvocation implements Invocation, Serializable {
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] arguments;
}
```

**经典** 方法名称 参数类型 参数

浪费空间

**优化** 

```
public classRequestimplementsSerializable{
    private int requestId;
    private int serviceId;
    private MethodParam methodParam;
}
```

通过指定方法和参数的定义id，减少传输数据大小。 维护一个集合存放id。

**`网络传输框架`**

Netty

`线程模型`

**线程模型解决的问题**，是如何高效的利用多个物理核，进行工作任务的调度，使得系统能够有更高有效的吞吐，更加低的延迟。而不是把时间花在大量的比如系统层面的工作：比如context-switch（PS：实际contextSwitch的时间），cache的同步、线程等待等contention上面）

线程模型这块当前典型的线程模型有几大类

- **连接独占模型**：也就是一个连接进来请求后，独占一个线程（进程）进行处理。（无论其中中间在做什么事情，比如调用第三方的服务，等待过程中也是独占着整个线程），比如传统的tomcat servlet就是这么干的。
- **单线程Reactor模型**：使用单个线程处理所有连接上的请求，使用epoll-wait 方式，实现事件多路复用机制。典型比如Redis，适用于简单比如小数据的内存数据的获取。每一个回调逻辑都比较简单。（缺点就是：某个回调卡住，真个Reactor 反应堆就block了）
- **多线程Reactor模型**: 单线程Reactor不能利用多线程Reactor的优势，所以当前大多数RPC/反向代理的框架大多数都是按照这个来玩的。也就是多个线程/进程Accept同一个连接上的请求（如何更好的处理惊群问题参考见 Nginx）。

### 技术点介绍

#### **Netty**

**心跳检测、断线重连**

背景：TCP心跳检测2小时触发一次，固定且长，而且当服务端死锁时，TCP层仍然可以进行心跳检测，但此时服务端是不应该发出响应的，即会给客户端造成误解，服务端仍然提供服务，因此在应用层实现心跳检测机制。

设置超时时间=重试次数+每次超时时长

**服务端 只负责接收**

pipeline中添加 IdleStateHandler 并设置超时时间readeridleTimeSeconds即该时间内没收到消息，未触发ChannelRead()方法，则执行一次userEventTrigger()方法，逻辑为超过重传次数则断开连接。

**客户端 负责发送**

pipeline中添加 IdleStateHandler 并设置超时时间writeridleTimeSeconds即该时间内没写内容，未触发write()方法，则执行一次userEventTrigger()方法，逻辑为未超过重传次数则发送PING心跳包给服务端，否则认为连接断开，尝试重连服务端，对doConnect方法加同步锁，防止并发问题。

**总结**

IdleStateHandler心跳检测主要是通过向线程任务队列中添加定时任务，判断channelRead()方法或write()方法是否调用空闲超时，如果超时则触发超时事件执行自定义userEventTrigger()方法；

**IdleStateHandler **在socket通道建立时channelActive触发，即初始化。

该心跳机制不是双向机制，而是单向即客户端发送心跳包，服务端接收但不回复。因为如果服务端同时有上千个连接，心跳的回复需要消耗大量网络资源；如果服务端一段时间内未收到客户端的心跳数据包则认为客户端已经下线，将通道关闭避免资源的浪费；在这种心跳模式下服务端可以感知客户端的存活情况，无论是宕机的正常下线还是网络问题的非正常下线，服务端都能感知到，而客户端不能感知到服务端的非正常下线；

连接建立成功 触发channelActive() 连接断开成功 触发channelInActive 可以感知正常下线情况，但无法感知网络异常等非正常下线情况。



**编解码 粘包半包**

Client和Server pipeline顺序一样

<img src="C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200607203100198.png" alt="image-20200607203100198" style="zoom: 67%;" />

**编码：其它格式--> 二进制字节**

**解码：二进制字节-->其它格式**

通过BasedFrameDecoder解码成只有数据的数据包，通过对应的解码器解码成实体类，执行Handler后，使用对应的编码器编码，再使用Prepender编码成具有长度域的数据。

数据包首字节为Message.Type，后面为数据内容 request/response。

BasedFrameDecoder每次按照规则读取数据，即可解决沾包半包问题。



#### 基于TCP、HTTP通信协议

**TCP:**本质是Netty Socket通信，

1、服务的调用方Consumer通过Socket建立起与服务的提供方Provider的连接；
2、Consumer将需要调用的方法名称和参数通过Socket发送给Provider；
3、Provider获取Consumer请求的数据并进行解析，执行具体的某一个方法，构造返回数据，返回给Consumer；
4、Consumer获得Provider返回的数据进行相应的处理；

**HTTP：**通过GET/POST http请求URL调用服务，省去TCP序列化，可以直接通过浏览器访问

**区别**

RPC是基于socket通信，在协议层面处于较底层，优点是传输效率高，但是开发难度相对较高，需要程序员自己开发。

HTTP处于较高层面，开发难度相对较小，可以复用HTTP工具，方便CRUD测试、允许传输xml、json等文本格式等，不用维护socket端口和数据序列化相关问题，但是传输效率比起TCP来低了一些



#### 序列化方式

Protostuff、jdk、fastjson、hessian序列化方式对RPC性能影响

**性能测试**

**指标**

1.对象序列化后的字节大小（码流）

2.序列化（反序列化）对象消耗时间

**测试用例**

1.boolean existUser(String email), 判断某个 email 是否存在
 输入是很短的字符串，输出是 bool 值，这个测试用例用于衡量小 Request 小
 Response 的性能。

2.boolean createUser(User user), 添加一个 用户
 输入是一个 User 的对象，输出是 bool 值，这个测试用例用于衡量大 Request 小 Response 的性能。

3.User getUser(long id), 根据 id 获取一个用户
 输入是一个 long 类型的值，输出是 User 对象，这个测试用例用于衡量小 Request 大 Response 的性能。

4.Page<User> listUser(int pageNo), 获取用户列表
 输入是 int 类型的值，输出是一个包含15个 User 的列表，这个测试用例用于衡量小 Request 超大 Response 的性能。

**测试方法**

客户端的测试逻辑是设置32个线程，每个线程发出如3000次请求，共用同一个客户端连接。
首先进行若干轮的**warmup**，避免JIT等运行时优化带来的影响，然后是进行若干轮正式的测试，最后对正式测试取一个平均值，将正式测试结果与平均结果都写入到CSV文件中。
测试项为QPS，平均响应时间AVG_RT，P90（90%的请求的RT都小于该值），P99，P999。QPS是越高表示性能越高，RT是越小表示性能越高。

**不足之处**

仅1个客户端32个线程其实是非常不严谨的，正确的做法应该是从1个线程一直到32k个线程逐步增加，从1台客户端机器到1000台客户端机器逐步增加（客户端数量 线程数量 应该是一个笛卡尔积）。不过每轮测试实在都太耗费时间了

**测试结果**

**QPS:每毫秒处理的请求数** = 处理的总请求数/花费的时间（单位毫秒） 

即求32个线程中每3000个请求的QPS然后取平均值

**RT:时延** =调用方法前后花费的时间

即求所有请求中方法调用花费时间求平均值

**码流：**Protostuff<fastJson<Hessian<JDK

**时间：**Protostuff<fastjson<jdk<hessian

**结论：**使用protostuff比使用jdk方式性能提高了2-3倍。

**优缺点**

`FastJson`  简单易用开发成本低,轻量级数据交换,体积大，影响高并发,片段的创建和验证过程比一般的XML复杂

`jdk` 使用方便，可序列化所有类，不支持跨语言，因为加入了序列化版本号，类名等信息，所以导致码流变大，速度变慢

`hessian` 面向接口，通过接口暴露服务，支持跨语言 效率高，复杂对象序列化速度仅次于RMI，简单对象序列化优于RMI，二进制传输，缺乏安全机制，传输没有加密处理，异常机制不完善，总是报一些错误

`protostuff` 跨语言，可自定义数据结构,二进制消息，效率高，性能高, 二进制格式，可读性差,

**JDK序列化慢的原因？**

Java本身并不支持跨语言，因为加入了序列化版本号，类名等信息，所以导致码流变大，速度变慢。而Protostuff等不需要额外加入信息，而是生成的代码里含有类型信息，码流下，则速度快。



|            | 码流大小(byte) | 10次(us) | 100次(us) | 1000次(us) | 10000次(us) | 100000次(us) |      |
| ---------- | -------------- | -------- | --------- | ---------- | ----------- | ------------ | ---- |
| FastJson   | 305            | 116-243  | 106-185   | 90-140     | 26-39       | 8-12         |      |
| JDK        | 866            | 383-777  | 502-1101  | 123-334    | 54-237      | 15-76        |      |
| Hessian    | 520            | 959-3836 | 376-567   | 191-329    | 99-161      | 30-47        |      |
| Protostuff | 193            | 103-145  | 90-137    | 75-135     | 15-24       | 5-8          |      |
|            |                |          |           |            |             |              |      |



**warmup:**jvm预热，即先进行若干论的调用方法对代码块进行预热，经常被调用的方法被加载到本地缓存中，运行访问时即可立即使用。

**JVM预热**

一旦类加载完成，所有重要的类（在进程启动时使用）都会被推送到JVM缓存（本机代码）中，这使得它们在运行时可以更快地访问。其他类是根据每个请求加载的。
对Java Web应用程序的第一个请求通常比进程的生命周期中的平均响应时间慢得多。这个预热期通常可以归因于延迟类加载和及时编译。
记住，对于低延迟应用程序，我们需要预先缓存所有类，以便在运行时访问时立即可用。
这种调优JVM的过程称为预热。

#### 传输参数（格式）

客户端传输给服务端的消息协议（格式） 包括以下几个属性

```java
public class RPCRequest implements Serializable(){
 private String interfaceName;//接口名
 private String methodName; //方法名
 private String[] parameterTypes;//参数类型
 private Object[] parameters;//参数
 }
```

服务端返回消息

```java
public class RPCResponse implements Serializable(){
 private Integer statusCode;//状态码
 private String message; //补充信息
 private T data;//响应数据
 
 }
```

#### 传输协议

```
+---------------+---------------+-----------------+-------------+
|  Magic Number |  Package Type | Serializer Type | Data Length |
|    4 bytes    |    4 bytes    |     4 bytes     |   4 bytes   |
+---------------+---------------+-----------------+-------------+
|                          Data Bytes                           |
|                   Length: ${Data Length}                      |
+---------------------------------------------------------------+
```

| 字段            | 解释                                                         |
| --------------- | ------------------------------------------------------------ |
| Magic Number    | 魔数，表识一个 MRF 协议包，0xCAFEBABE                        |
| Package Type    | 包类型，标明这是一个调用请求还是调用响应还是Ping还是Pong     |
| Serializer Type | 序列化器类型，标明这个包的数据的序列化方式                   |
| Data Length     | 数据字节的长度                                               |
| Data Bytes      | 传输的对象，通常是一个`RpcRequest`或`RpcClient`对象，取决于`Package Type`字段，对象的序列化方式取决于`Serializer Type`字段。 |

#### 拆包器

根据上面协议知，偏移量为4+4+4=12 长度域为4 所以构成以下

使用基于长度拆包器， new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 12, 4);

第一个参数为数据包最大长度，第二个指的是长度域的偏移量，第三个是长度域的长度。

这样在进行decode操作时，ByteBuf就是一个完整的**自定义协议数据包**。

#### 服务调用异常

**服务降级**

当服务提供端某一个非关键的服务出错时候，在dubbo中可以对**消费端的调用进行降级**，这样服务消费端就避免了在去调用出错的服务提供端，而是使用自定义的返回值直接在在本地返回。

dubbo可以使用`mock`来设置**本地伪装**，提供了两种服务降级策略：
1、**force:return策略**：表示服务调用方在调用该接口服务时候会直接在客户端内返回设置的mock值，而不会在通过远程调用方式调用服务提供者。比如使用`mock=force:return+null`表示消费方对该服务的方法调用都直接返回null值，不发起远程调用。用来屏蔽不重要服务不可用时对调用方的影响
2、**fail:return策略**：表示服务调用方调用服务提供方服务失败后再返回设置的mock值，并不会抛出异常。需要注意的是并不是调用一次失败后就直接返回mock值，具体和设置的集群容错方式(retries)有关。比如使用`mock=fail:return+null`表示消费方对该服务的方法调用在失败后(在抛出RpcException异常时执行)，不抛异常直接返回null值。用来容忍不重要服务不稳定时对调用方的影响

dubbo的本地伪装通常用于服务降级，比如某验权服务，当服务提供方全部挂掉后，客户端不抛出异常，而是通过Mock数据返回授权失败。在 spring 配置文件中按以下方式配置：

```
<dubbo:reference interface="com.foo.BarService" mock="true" />
或
<dubbo:reference interface="com.foo.BarService" mock="com.foo.BarServiceMock" />在工程中提供Mock实现
或
<dubbo:reference interface="com.foo.BarService" mock="return null" />
```

**调用过程**

当服务消费者具体发起远程调用时候会根据路由规则和负载均衡算法从服务提供者列表选择一个IP作为调用目标，然后具体发起远程调用前，会看是否设置了`force:return`降级策略，如果设置了则直接返回mock值，并不发起远程调用。否则发起远程调用，如果远程调用结果成功，则直接返回远程调用返回的结果。如果远程调用失败，则看当前的集群从容策略是什么，如果是失败重试，那么还要看通过retries设置的重试次数是多少，比如重试2次，则会在调用失败后再进行重试两次调用，如果其中一次成功了则直接返回结果。如果两次都失败了，则看当前是否设置了`fail:return`的降级策略，如果设置了则直接返回mock值，否者返回调用远程服务失败的具体原因

**服务熔断**

一般来说熔断器需要实现三个状态机：
1、Closed：熔断器关闭状态，调用失败次数积累，到了阈值(或一定比例)则启动熔断机制
2、Open：熔断器打开状态，此时对下游的调用都内部直接返回错误，不走网络，但设计了一个时钟选项，默认的时钟达到了一定时间(这个时间一般设置成平均故障处理时间，也就是MTTR)，到了这个时间，进入半熔断状态
3、Half-Open：半熔断状态，允许定量的服务请求，如果调用都成功(或一定比例)则认为恢复了，关闭熔断器，否则认为还没好，又回到熔断器打开状态



Dubbo本身不提供熔断器，结合Hystrix。

**Hystrix**

**每个服务分配一个线程池，每个线程池里的线程仅仅用于请求那个服务**

在客户端依赖多个服务，各个服务间相互调用的情况下，防止因为某一个服务出现异常，引起连锁反应，导致整个请求失败，系统不可用。

**隔离：**各个服务的线程互不干扰一个服务卡死不会影响其它服务

**熔断：**一定时间内请求次数失败则开启熔断策略，对积分服务熔断，请求积分服务之间返回。

**降级：**积分服务熔断后，每次调用积分服务，在数据库添加一条记录，给某某用户加了多少分，表明因为积分服务挂了，导致没增加成功，等积分服务恢复后，可以根据db记录手动添加积分。

当服务调用超时时，执行 `fallback` 方法

**提供回查和反馈接口**



#### 客户端实现-动态代理

客户端这边可以通过动态代理的方式生成实例，并且调用方法时生成需要的RpcRequest对象并且发送给服务端。

```java
public class RpcClientProxy implements InvocationHandler {
    private String host;
    private int port;

    public RpcClientProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }
}
```

通过IP和HOST指定服务端位置，使用`getProxy`生成代理对象，下面重写代理增强方法

```java
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .build();
        RpcClient rpcClient = new RpcClient();
        return ((RpcResponse) rpcClient.sendRequest(rpcRequest, host, port)).getData();
    }
```

增强方法即 生成`RPCRequest` 并且发送至指定服务端

#### 服务端实现-反射

核心逻辑如下

```java
    @Override
    public void run() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            Object returnObject = method.invoke(service, rpcRequest.getParameters());
            objectOutputStream.writeObject(RpcResponse.success(returnObject));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.error("调用或发送时有错误发生：", e);
        }
    }

```

首先通过反序列化成`RPCRequest` 然后通过class.getMethod方法，传入方法名和方法参数类型即可获得Method对象。如果你上面RpcRequest中使用String数组来存储方法参数类型的话，这里你就需要通过反射生成对应的Class数组了。通过method.invoke方法，传入对象实例和参数，即可调用并且获得返回值。

#### Zookeeper集群负载均衡

**随机**：使用random函数，在List<Invoker> 中获取随机一个（即zk节点列表中随机获取一个）

**加权随机：**假设有n个invoker，第i个invoker的权值为weight[i]。那么随机到该invoker的概率为 weight[i]/sum(weight)

**轮询：**List<Invoker>里 一个个轮询

**一致性哈希：**通过对Invoker进行hash映射到相应的invoker

#### ZooKeeper节点内容

使用临时节点，解决服务器宕机节点自动删除。

![这里写图片描述](https://img-blog.csdn.net/20180811205322778?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Z1MTIzMTIzZnU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

![img](https://upload-images.jianshu.io/upload_images/6869115-4e5afc4574943cbf?imageMogr2/auto-orient/strip|imageView2/2/w/772/format/webp)

根节点是固定的Path(自己取)，其子节点是所有**服务**名字，各个服务节点path是 ip:port，每个ip:port节点是临时节点，服务启动时会创建该path的临时节点，宕机后变自动删除。

**即服务节点下有多个服务器节点，用于负载均衡**

**服务端启动**

服务端启动时，将自己的服务注册到注册中心，（一次遍历服务节点，若自己有对应的服务，则将自己的IP PORT加入它的子节点中）通过 `getChildren` 方法获取服务列表，依次遍历查看。

**客户端订阅**

客户端维护一个 结构 ，key 为interfaceName即服务名，value为集群节点

```
private Map<Key, List<String>> adressMap  = new HashMap<>();
```

客户端首先在 addressMap 中找到服务对应的节点，若无，则去到ZK中获取最新的地址（集群节点 ip port），更新到 addressMap中，并且设置监听机制，当增加或减少集群节点时，更新addressMap。



#### Dubbo分层架构

使用这种方式可以使各个层之间解耦合（或者最大限度地松耦合）

其它各层均为 SPI层，SPI 意味着下面各层都是组件化可以被替换的，这也是 Dubbo 设计的比较好的一点。Dubbo 增强了 JDK 中提供的标准 SPI 功能，在 Dubbo 中除了 Service 和 Config 层外，其它各层都是通过实现扩展点接口来提供服务的；Dubbo 增强的 SPI 增加了对扩展点 IoC 和 AOP 的支持，一个扩展点可以直接 setter 注入其它扩展点；并且不会一次性实例化扩展点的所有实现类，这避免了当扩展点实现类初始化很耗时，但当前还没用上它的功能时仍进行加载实例化，浪费资源的情况；增强的 SPI 是在具体用某一个实现类的时候才对具体实现类进行实例化。



接口层（Service）：该层是与实际业务逻辑相关的，根据服务提供方和服务消费方的业务设计对应的接口和实现。

配置层（Config）：对外配置接口，以ServiceConfig和ReferenceConfig为中心，可以直接new配置类，也可以通过spring解析配置生成配置类。

代理层（Proxy）：服务接口透明代理，该层主要是对服务消费端使用的接口进行代理，把本地调用透明的转换为远程调用；另外对服务提供方的服务实现类进行代理，把服务实现类转换为 Wrapper 类，这是为了减少反射的调用

注册层（Registry）：服务提供者启动时候会把服务注册到服务注册中心，消费者启动时候会去服务注册中心获取服务提供者的地址列表，Registry层主要功能是封装服务地址的注册与发现逻辑。以服务URL为中心，扩展接口为RegistryFactory、Registry和RegistryService。

集群层（Cluster）：封装多个服务提供者的路由规则、负载均衡、集群容错的实现，并桥接服务注册中心，以Invoker为中心，扩展接口为Cluster、Directory、Router和LoadBalance。将多个服务提供方组合为一个服务提供方，实现对服务消费方来透明，只需要与一个服务提供方进行交互。

监控层（Monitor）：RPC调用次数和调用时间监控，以Statistics为中心，扩展接口为MonitorFactory、Monitor和MonitorService。

远程调用层（Protocol）：封将RPC调用逻辑，以Invocation和Result为中心，扩展接口为Protocol、Invoker和Exporter。Protocol是服务域，它是Invoker暴露和引用的主功能入口，它负责Invoker的生命周期管理。Invoker是实体域，它是Dubbo的核心模型，其它模型都向它靠扰，或转换成它，它代表一个可执行体，可向它发起invoke调用，它有可能是一个本地的实现，也可能是一个远程的实现，也可能一个集群实现。

信息交换层（Exchange）：封装请求响应模式，同步转异步，以Request和Response为中心，扩展接口为Exchanger、ExchangeChannel、ExchangeClient和ExchangeServer。

网络传输层（Transport）：抽象mina和netty为统一接口，以Message为中心，对应实现有 NettyChannel（默认），扩展接口为Channel、Transporter、Client、Server和Codec。

数据序列化层（Serialize）：可复用的一些工具，对应扩展实现有 DubboSerialization、FastJsonSerialization、Hessian2Serialization、JavaSerialization等



#### ZK宕机

在实际生产中，假如zookeeper注册中心宕掉，一段时间内服务消费方还是能够调用提供方的服务的，实际上它使用的本地缓存进行通讯，这只是dubbo健壮性的一种体现。

消费者会从注册中心拉取注册的生产者的接口等数据，缓存到本地。每次调用时，按照本地存储的地址进行调用。

注册中心对等集群，任意一台宕掉后，将自动切换到另一台。

注册中心全部宕掉后，服务提供者和服务消费者仍能通过本地缓存通讯。

**dubbo的健壮性表现：**

1. 监控中心宕掉不影响使用，只是丢失部分采样数据
2. 数据库宕掉后，注册中心仍能通过缓存提供服务列表查询，但不能注册新服务
3. 注册中心对等集群，任意一台宕掉后，将自动切换到另一台
4. 注册中心全部宕掉后，服务提供者和服务消费者仍能通过本地缓存通讯
5. 服务提供者无状态，任意一台宕掉后，不影响使用
6. 服务提供者全部宕掉后，服务消费者应用将无法使用，并无限次重连等待服务提供者恢复

#### 适配者模式

**背景：** 普通类实现接口，得重写全部抽象方法，当抽象方法过多时，重写全部抽象方法过于麻烦而且没必要。

**接口：**主要是规范的方法

**解决：** 添加（过度类）抽象类实现接口，普通类继承抽象类。抽象类重写部分通用的方法，普通类重写剩下抽象类未重写的方法。即 接口=抽象+普通



**RPC核心 动态代理+Socket**

1.服务端暴露服务，绑定一个端口，利用Socket轮询，等待接受客户端的请求。

2.客户端引用服务，利用动态代理，隐藏掉每个接口方法的实际调用，实际上是代理类执行服务。

3.客户端将方法名、参数类型、方法所需参数传给服务端，服务端接受到客户端传过来的方法名、参数类型、方法所需参数之后，利用反射，执行具体的接口方法，然后将执行结果返回给客户端

客户端看到的是接口的行为（这个行为没有被实现），
服务端方的是接口行为的具体实现。

客户端把行为和行为入参提供给服务端，然后服务端的接口实现执行这个行为，最后再把执行结果返回给客户端。

看起来是客户端执行了行为，但其实是通过动态代理交给服务端执行的。其中，行为和入参这些数据通过socket由客户端传给了服务端。


### RPC

#### RPC介绍

远程过程调用，调用其他机器上的接口就像在本地调用一样。

<img src="https://images2015.cnblogs.com/blog/434101/201603/434101-20160316102651631-1816064105.png" alt="img" style="zoom: 67%;" />

- Server: 暴露服务的服务提供方。
- Client: 调用远程服务的服务消费方。
- Registry: 服务注册与发现的注册中心。

服务提供者启动后主动向注册中心注册机器ip、port以及提供的服务列表； 服务消费者启动时向注册中心获取服务提供方地址列表，可实现软负载均衡和Failover；

#### RPC调用流程

![img](https://user-gold-cdn.xitu.io/2018/5/28/163a6fb4d3a1aded?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

1）服务消费方（client）调用以本地调用方式调用服务；

2）client stub接收到调用后通过序列化负责将方法、参数等组装成能够进行网络传输的消息体；

3）client stub找到服务地址，并将消息发送到服务端；

4）server stub收到消息后进行反序列化获取参数；

5）server stub根据结果调用本地的服务；

6）本地服务执行并将结果返回给server stub；

7）server stub将返回结果打包成消息并发送至消费方；

8）client stub接收到消息，并进行解码；

9）服务消费方得到最终结果。

RPC就是把2~8的步骤封装起来，对用户的使用透明。stub表示动态代理类。

![img](https://upload-images.jianshu.io/upload_images/13459583-42c1bec0dd2d3aed.jpg?imageMogr2/auto-orient/strip|imageView2/2/w/795/format/webp)

#### 服务端怎么区分多个连接

服务端使用Map  `map<ip,socket>` 通过ip区分

#### 单一长连接接收同一对端并发请求

**客户端和服务端都只用一个长连接处理接收和发送所有请求**

客户端多线程并发对服务端并发调用，这些请求都是通过该单一Channel发送和获取结果的，而Netty所有请求都是异步，故dubbo如何保证这些并发线程能正确获取到自己的请求结果，而不会造成数据混乱呢？

**1.** 每个客户端进程通过AtomicLong生成的当前进程全局唯一id

**2.** 使用Map存储对应的回调 `Future ` ，发起调用线程堵塞等待结果

**3.** 服务端响应数据时带上id

**4.** 客户端Socket专门监听消息的线程收到消息后，根据id从Map中取出 `Future` 并写入值，然后唤醒该堵塞的线程。

**原理如下**

1. client一个线程调用远程接口，生成一个唯一的ID（比如一段随机字符串，UUID等），Dubbo是使用AtomicLong从0开始累计数字的
2. 将打包的方法调用信息（如调用的接口名称，方法名称，参数值列表等），和处理结果的回调对象callback，全部封装在一起，组成一个对象object
3. 向专门存放调用信息的全局ConcurrentHashMap里面put(ID, object)
4. 将ID和打包的方法调用信息封装成一对象connRequest，使用IoSession.write(connRequest)异步发送出去
5. 当前线程再使用callback的get()方法试图获取远程返回的结果，在get()内部，则使用synchronized获取回调对象callback的锁，再先检测是否已经获取到结果，如果没有，然后调用callback的wait()方法，释放callback上的锁，让当前线程处于等待状态。
6. 服务端接收到请求并处理后，将结果（此结果中包含了前面的ID，即回传）发送给客户端，客户端socket连接上专门监听消息的线程收到消息，分析结果，取到ID，再从前面的ConcurrentHashMap里面get(ID)，从而找到callback，将方法调用结果设置到callback对象里。
7. 监听线程接着使用synchronized获取回调对象callback的锁（因为前面调用过wait()，那个线程已释放callback的锁了），再notifyAll()，唤醒前面处于等待状态的线程继续执行（callback的get()方法继续执行就能拿到调用结果了），至此，整个过程结束。

**线程怎么做到暂停，得到回调后再向后执行？**

先生成一个对象obj，在一个全局map里put(ID,obj)存放起来，再用synchronized获取obj锁，再调用obj.wait()让当前线程处于等待状态，然后另一消息监听线程等到服务端结果来了后，再map.get(ID)找到obj，再用synchronized获取obj锁，再调用obj.notifyAll()唤醒前面处于等待状态的线程。



**dubbo为什么不适用于传输大文件**

因dubbo协议采用单一长连接，收到网卡约束，网络成为瓶颈。

dubbo适用于小数据量（数据包小）大并发（高并发）的服务调用，以及服务消费者机器数远大于服务提供者机器数量的场景，特别是接口响应耗时短的场景，但不适用于传输大数据的服务调用。

#### 技术点

1.客户端、服务端的动态代理类。

2.序列化方法。

3.网络传输Netty

4.注册中心zookeeper



#### 具体实现

1. 调用接口时，传递参数给代理类，代理类的invoke方法中，将函数的调用信息（函数名，参数列表等）打包成可以序列化的Invocation对象，Invocation对象对应调用方式如同步、CallBack、OneWay等，通过Netty指定server端的socket地址，然后申请建立socket连接。建立好连接后，会将Invocation对象通过网络发送给server端。发送的序列化消息只是函数的调用信息（函数名，参数列表）。
2. 对于server端，通过客户端传递过来的 className+method+param找到容器中方法执行反射调用，通过反射生成代理对象。

#### 详细调用过程

![img](https://img-blog.csdn.net/20160330210322989)

![img](https://img-blog.csdn.net/20160330212347122)

![img](https://img-blog.csdn.net/20160330213844175)

**代理层**

 对消费者来时，在RPC调用过程中，使用第1步、第2步、第3步、第4步是透明的，其他的都是使用RPC框架去封装这些事情。当应用开始调用PRC的方式时，就会去容器中去取Bean对象，所以我们应该首先注册Bean对象到容器中，我们通过Java的动态代理，将代理过程封装到代理对象中，代理对象实现接口，创建实例到容器中。相应的，在调用远程对象的对象方法时，就会调用动态代理中的方法，这就是代理层的作用。

代理对象在获取到请求方法、接口和参数时，就会用序列化层，将这些信息封装成一个请求报文，再让通信层向服务端传送报文的内容，然后就到了生产者这块。

 相应的服务必须有个监听器，来监听来自其他服务的请求，一般都会用容器做消息的监听，就会调用对应的Bean对象的方法，去处理响应的请求。当然，RPC框架不会让容器中的每一个框架都会被调用，所以只有注册了的Bean才会被RPC的请求调用到。然后，通过请求中的类、方法、参数，反射调用对应的Bean，拿到结果之后，通过序列化层，封装好结果报文，服务端的通信层将报文反馈给调用方，调用方解析到返回值，动态代理类返回结果，调用结束。

![img](https://img-blog.csdn.net/20160330214747335)

### Zookeeper

- **ZooKeeper 本身就是一个分布式程序（只要半数以上节点存活，ZooKeeper 就能正常服务）。**

- **为了保证高可用，最好是以集群形态来部署 ZooKeeper，这样只要集群中大部分机器是可用的（能够容忍一定的机器故障），那么 ZooKeeper 本身仍然是可用的。**

- **ZooKeeper 将数据保存在内存中，这也就保证了 高吞吐量和低延迟**（但是内存限制了能够存储的容量不太大，此限制也是保持znode中存储的数据量较小的进一步原因）。

- **ZooKeeper 是高性能的。 在“读”多于“写”的应用程序中尤其地高性能，因为“写”会导致所有的服务器间同步状态。**（“读”多于“写”是协调服务的典型场景。）

- ZooKeeper 底层其实只提供了两个功能：①管理（存储、读取）用户程序提交的数据；②为用户程序提交数据节点监听服务。

zk总结
https://www.cnblogs.com/aspirant/p/9088322.html

zk面试题
https://www.cnblogs.com/ibigboy/p/11356221.html

![/user-guide/images/zookeeper.jpg](http://dubbo.apache.org/docs/zh-cn/user/sources/images/zookeeper.jpg)

- 服务提供者启动时: 向 `/dubbo/com.foo.BarService/providers` 目录下写入自己的 URL 地址
- 服务消费者启动时: 订阅 `/dubbo/com.foo.BarService/providers` 目录下的提供者 URL 地址。并向 `/dubbo/com.foo.BarService/consumers` 目录下写入自己的 URL 地址
- 监控中心启动时: 订阅 `/dubbo/com.foo.BarService` 目录下的所有提供者和消费者 URL 地址。

#### 主要应用场景

- 服务注册与订阅（共用节点）**（RPC）**
- 分布式通知（监听znode）
- 服务命名（znode特性）
- 数据订阅、发布（watcher）**(分布式配置中心)**
- 分布式锁（临时节点）



#### 简而概之

zookeeper=文件系统+监听通知机制

<img src="https://img-blog.csdn.net/201807121434154?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2phdmFfNjY2NjY=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70" alt="img"  />

**背景**

假设程序是分布式部署在多个机器上，当配置文件需要修改时，需要逐台机器去修改，麻烦。

**监听通知机制**

将配置文件存放到zookeeper某个目录节点中，然后所有程序监听该节点，当配置信息发生变化时，每个节点会收到通知，可以读取新配置并进行相应操作。

**节点类型**

- 持久化节点（zk断开节点还在）
- 持久化顺序编号目录节点
- 临时目录节点（客户端断开后节点就删除了）
- 临时目录编号目录节点

节点名称都是唯一的。



#### 集群节点

zookeeper集群节点，所有节点维护相同的数据服务。
负载均衡需要自行实现，可以将集群节点维护成list,对list取节点时，采用不同的负载均衡方法实现

用zookeeper保存服务器的连接信息：

1、当服务器启动时，往zookeeper的节点里写入数据（服务）（节点的类型是临时节点）；

2、当关闭服务器时，从zookeeper移除相应的节点数据（服务）；

3、当服务器宕机，zookeeper因为没有检测到心跳，自动把该节点（服务）移除，并通知其他服务器，其他服务器得知该机器已宕机，在分配连接时，不会分配到这台机器上，这点也就是本文中所说，负载均衡中使用zookeeper的原因。

注意别搞乱 zookeeper集群节点    节点中的数据节点（服务）



##### 处理请求

![image-20200621163844798](C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200621163844798.png)

事务性请求包括：更新操作、新增操作、删除操作，结合上面的分析，因为这些操作是会影响数据的，所以要保证这些操作在整个集群内的事务性，所以这些操作就是事务性请求。

Zookeeper集群中，当某一个集群节点接收到一个写请求操作时，该节点需要将这个写请求操作发送给其他节点，以使其他节点同步执行这个写请求操作，从而达到各个节点上的数据保持一致，也就是数据一致性。

非事务性请求就好理解的，像查询操作、exist操作这些不影响数据的操场，就不需要集群来保持事务性，所以这些操场就是非事务性请求。

对于上图，Zookeeper真正的底层实现，zk1是Leader，zk2和zk3是Learner

非事务性请求直接读取DataTree上的内容，DataTree是在内存中的，所以会非常快。



#### 内存结构

内存结构为文件系统，树形层级结构，每个节点可以存放数据，节点存放上限是1M。不建议存放过多数据，因为客户端的写请求在服务端是串行处理的，数据传输会大大影响性能。(在写操作达到一定次数后，会对内存数据库拍快照，将其序列化到磁盘上)

处理客户端对目标节点的请求时，是通过hashmap直接定位到目标节点而不是层层遍历。

![img](http://ningg.top/images/zookeeper/zk-data-impl-details.png)

1. DataTree 中 nodes 是 Map，表示所有的 ZK 节点，那其内部 key 是什么？
   - Re：ZNode 的唯一标识 `path` 作为 key
2. ephemerals 是Map，用于存储临时节点，那其内部 key 是什么？value 又是什么？
   - Re：临时节点是跟 Session 绑定的，sessionId 作为 key

zNode表示一个节点,不仅是一个路径，还携带数据，节点维护的信息如下

**1. 版本号**

znode的数据每次更新时，该版本号递增。当客户端请求该znode时，数据和版本号会一起发回。另外，当znode重建时版本号会被重置。当客户端执行更改、删除操作时，它必须提供它正在更改的znode数据的版本，如果它提供的版本与数据的实际版本不匹配，则更新将失败。

**2.权限控制ACL**

Access Control List，用来限定哪些账号可以操作该znode。

**world：**默认方式，相当于全部都能访问
**auth**：代表已经认证通过的用户(cli中可以通过addauth digest user:pwd 来添加当前上下文中的授权用户)
**digest**：即用户名:密码这种方式认证，这也是业务系统中最常用的。用 *username:password* 字符串来产生一个MD5串，然后该串被用来作为ACL ID。认证是通过明文发送*username:password* 来进行的，当用在ACL时，表达式为*username:base64* ，base64是password的SHA1摘要的编码。
**ip**：使用客户端的主机IP作为ACL ID 。这个ACL表达式的格式为*addr/bits* ，此时addr中的有效位与客户端addr中的有效位进行比对。



#### 磁盘数据

针对客户端的每一次事务操作，Zookeeper都会将他们记录到事务日志中，同时也会将数据变更应用到内存数据库中，Zookeeper在进行若干次（snapCount）事务日志记录后，将内存数据库的全量数据Dump到本地文件中，这就是数据快照。

**事务日记**

ZK会将每次更新操作以事务日志的形式写入磁盘，写入成功后才会给予客户端响应，多条日记首先写入缓存，后一起写入磁盘。

**关键点：**

1.事务日记频繁flush到磁盘，消耗大量磁盘IO。

2.磁盘预分配，日记剩余空间小于4kB,文件增大64M，为了减少磁盘seek寻址次数。

3.建议：事务日志，采用独立磁盘单独存放

**日志截断**

在Zookeeper运行过程中，可能出现非Leader记录的事务ID比Leader上大，这是非法运行状态。此时，需要保证所有机器必须与该Leader的数据保持同步，即Leader会发送TRUNC命令给该机器，要求进行日志截断，Learner收到该命令后，就会删除所有包含或大于该事务ID的事务日志文件。



**数据快照**

当事务日记写满切换事务日记时，发生数据快照，为了防止数据没有记录。

数据快照用来记录zk服务器上某一时刻的全量内存数据内容，并将其写入到指定的磁盘文件中，目的是快速恢复内存中的数据

可以理解 切换事务日志文件的时机，实际是生成快照文件的时机。

快照文件是 Fuzzy 快照，不是精确到某一时刻的快照，而是某一时间段内的快照

**关键点：**

1.异步；异步线程生成快照

2.快照文件生成中仍然有新的事务提交，因此快照文件不是精确到某一时刻的快照文件，而是模糊的。

**初始化**

ZK 服务器启动时，首先会进行数据初始化，将磁盘中数据，加载到内存中，恢复现场。

**数据同步**

ZK 集群服务器启动之后，会进行 2 个动作：

1. 选举 Leader：分配角色
2. Learner 向 Leader 服务器注册：数据同步

数据同步，**本质**：将没有在 Learner 上执行的事务，同步给 Learner。

集群启动后，什么时候能够对外提供服务？需要等所有 Learner 都完成数据同步吗？

- Re：`过半策略`：只需要半数 Learner 完成数据同步，Learder 向所有已经完成数据同步的 Learner 发送 UPTODATE 命令，表示集群具备了对外服务能力



**为什么同时包含日记和快照？**

出于数据粒度的考虑

- 如果只包含快照，那恢复现场的时候，会有数据丢失，因为生成快照的时间间隔太大，即，快照的粒度太粗了
- 事务日志，针对每条提交的事务都会 flush 到磁盘，因此粒度很细，恢复现场时，能够恢复到事务粒度上



#### CAP

**Zookeeper保证CP**
当向注册中心查询服务列表时，我们可以容忍注册中心返回的是几分钟以前的注册信息，但不能接受服务直接down掉不可用。也就是说，服务注册功能对可用性的要求要高于一致性。但是zk会出现这样一种情况，当master节点因为网络故障与其他节点失去联系时，剩余节点会重新进行leader选举。问题在于，选举leader的时间太长，30 ~ 120s, 且选举期间整个zk集群都是不可用的，这就导致在选举期间注册服务瘫痪。在云部署的环境下，因网络问题使得zk集群失去master节点是较大概率会发生的事，虽然服务能够最终恢复，但是漫长的选举时间导致的注册长期不可用是不能容忍的。

**Eureka保证AP**
Eureka看明白了这一点，因此在设计时就优先保证可用性。Eureka各个节点都是平等的，几个节点挂掉不会影响正常节点的工作，剩余的节点依然可以提供注册和查询服务。而Eureka的客户端在向某个Eureka注册或时如果发现连接失败，则会自动切换至其它节点，只要有一台Eureka还在，就能保证注册服务可用(保证可用性)，只不过查到的信息可能不是最新的(不保证强一致性)。除此之外，Eureka还有一种自我保护机制，如果在15分钟内超过85%的节点都没有正常的心跳，那么Eureka就认为客户端与注册中心出现了网络故障，此时会出现以下几种情况：

1. Eureka不再从注册列表中移除因为长时间没收到心跳而应该过期的服务
2. Eureka仍然能够接受新服务的注册和查询请求，但是不会被同步到其它节点上(即保证当前节点依然可用)
3. 当网络稳定时，当前实例新的注册信息会被同步到其它节点中
因此， Eureka可以很好的应对因网络故障导致部分节点失去联系的情况，而不会像zookeeper那样使整个注册服务瘫痪。   
