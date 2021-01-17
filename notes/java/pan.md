

## 登录RSA

### RSA简介

RSA是一种非对称性加密。通过 **公钥** 和 **私钥** 进行加密解密。

### 加密与签名

**加密** 是为了防止信息被泄露。

RSA的加密过程如下：

（1）A生成一对密钥（公钥和私钥），私钥不公开，A自己保留。公钥为公开的，任何人可以获取。

（2）A传递自己的公钥给B，B用A的公钥对消息进行加密。

（3）A接收到B加密的消息，利用A自己的私钥对消息进行解密。

　　在这个过程中，只有2次传递过程，第一次是A传递公钥给B，第二次是B传递加密消息给A，即使都被敌方截获，也没有危险性，因为只有A的私钥才能对消息进行解密，防止了消息内容的泄露。

**签名** 是为了防止信息被篡改

RSA签名的过程如下：

（1）A生成一对密钥（公钥和私钥），私钥不公开，A自己保留。公钥为公开的，任何人可以获取。

（2）A用自己的私钥对消息加签，形成签名，并将加签的消息和消息本身一起传递给B。

（3）B收到消息后，在获取A的公钥进行验签，如果验签出来的内容与消息本身一致，证明消息是A回复的。

　　在这个过程中，只有2次传递过程，第一次是A传递加签的消息和消息本身给B，第二次是B获取A的公钥，即使都被敌方截获，也没有危险性，因为只有A的私钥才能对消息进行签名，即使知道了消息内容，也无法伪造带签名的回复给B，防止了消息内容的篡改。

**问题**

第一个场景虽然被截获的消息没有泄露，但是可以利用截获的公钥，将假指令进行加密，然后传递给A。第二个场景虽然截获的消息不能被篡改，但是消息的内容可以利用公钥验签来获得，并不能防止泄露。所以在实际应用中，要根据情况使用，也可以同时使用加密和签名，比如A和B都有一套自己的公钥和私钥，当A要给B发送消息时，先用B的公钥对消息加密，再对加密的消息使用A的私钥加签名，达到既不泄露也不被篡改，更能保证消息的安全性。

**总结 公钥加密、私钥解密、私钥签名、公钥验签。**

**PS: **RSA加密对明文的长度有所限制，规定需加密的明文最大长度=密钥长度-11（单位是字节，即byte），所以在**加密和解密的过程中需要分块进行**。而密钥默认是1024位，即1024位/8位-11=128-11=117字节。所以默认加密前的明文最大长度117字节，解密密文最大长度为128字。那么为啥两者相差11字节呢？是因为RSA加密使用到了填充模式（padding），即内容不足117字节时会自动填满，用到填充模式自然会占用一定的字节，而且这部分字节也是参与加密的。

### 项目实际

<img src="https://pic002.cnblogs.com/images/2012/379997/2012030522491911.png" alt="img" style="zoom:50%;" />

1.用户请求登录页面，服务器生成对应的 **公钥** 和 **私钥**，将 **公钥** 返回给浏览器，**私钥** 用Map/redis或mysql保存。（key 为公钥 value 为私钥）

2.用户 用 **公钥** 对密码进行加密 发给 服务器。

3.服务器用 **私钥** 解密 判断。

### 为什么不用MD5（区别）？

理论上讲，rsa基于大质数分解的复杂度远高于md5+salt。

实际上，md5+salt比rsa更适合。

1、不可能对所有的用户建立一个私钥，主要原因是管理复杂（基于安全的考虑不可能把私钥存在数据库中，那存储密码的复杂度就从保存密码本身变成了安全保存所有私钥）

2、如果只设计一个master私钥的话，所有密码验证共用一个私钥会带来更多的安全问题（第一点安全保存私钥的问题和master泄密的问题）

而使用md5+salt的隐患是彩虹表攻击，但是由于salt是随机生成的，作为明文和md5(passwd+salt)一起存放在数据库中，可以让所有用户密码泄漏的概率从O(N)变成O(N)*用户数（这里O(N)是指彩虹表破解一个用户密码的复杂度）。在绝大部分安全等级没那么高的系统中，这样设计已经足够满足需求了。



### 多线程访问Controller线程安全？

Controller默认是 **单例模式**，即只创建一个实例，若实例中有 **基本数据类型变量**，则该变量是所有线程共享，会造成线程安全问题。



### 同名Controller访问问题？

Controller由请求方式和value共同决定，与方法名无关。

```
value="/login" method=get
fun1();
value="/login" method=post
fun1();
访问成功
value="/login" method=get
fun1();
value="/login" method=get
fun2();
访问失败


```

## 验证码

**用户注册** / **忘记密码** 

服务端生成验证码，存储在redis中 (userPhone,code)设置过期时间5分钟。

5分钟输入仍然有效，5分钟后清楚 key。

若5分钟内 同一号码再次 获取验证码 则覆盖原来code，重设过期时间。

客户端输入验证码后，通过 userPhone从redis中获取code对比。

**禁止客户恶意多次获取验证码**

redis可以维护一个Hash结构，key 为userPhone记录上次发送时间戳，发送次数等来禁止客户多次获取验证码。



## 用户认证

### JWT(Json Web Token)

 一种加密字符串Token。

一个 jwt 是由 3 部分组成，用 . 连接起来 Header.Payload.Signature。可以设置过期时间。

**加密：** Signature 是服务器通过 私匙 将 Header 与 Payload的信息进行算法加密后得到的。

**解密：**将获取到的jwt分成上面三部分，再次进行加密，将加密后的 Sign与请求jwt中的Sign对比。 

**与session最大的区别是 不存放token 即每次客户端请求的时候根据之前的生成方法再生成一次来验证**

**缺点**

jwt无法主动控制用户会话过期，比如 后台踢出用户操作 修改密码需要重新登录 等操作。

**解决**

**修改密码：** 添加用户的密码 一起加密解密。

**主动踢出：** redis增加黑名单，拦截器判断若用户uuid在 黑名单中，直接判处token 无效。

重新授权，修改密码，多端登录 都会触发黑名单。 

**redis增加黑名单 那和session直接放session 有区别？**

1: 存储成本小。jwt 的 payload 大部分不需要存储在 redis 里，因为可以用签名来验证，真正需要的只有一个 uuid ；而 session 共享要全都存储。
2: 带宽压力小。jwt 只需要判断一 exist，session 共享需要 get。

#### Token

Token是服务端生成的一串字符串，以作客户端进行请求的一个令牌，当第一次登录后，服务器生成一个Token便将此Token返回给客户端，以后客户端只需带上这个Token前来请求数据即可，无需再次带上用户名和密码

**客户端将token存储在 localStorage sessionStorage等**

#### 为什么使用Token（Token与Session区别）

**token更加灵活 方便**

1.支持移动端APP ，移动端没有Cookie

2.性能高于session，只需一次token解密即可，不用到db/redis中查询session信息。

3.解决跨域问题

4.无状态（服务可扩展行），token机制服务端不需要存储session信息，易于扩展。

5.session对服务器压力会越来越大，多台服务器session共享问题。

#### Token认证机制

![img](https://img-blog.csdnimg.cn/20181126161842605.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_90,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTMwMzQyMjY=,size_20,color_FFFFFF,t_70)



客户端请求时，服务器 过滤器 对 过期时间 是否已经注销 判断，然后进行 token验证

#### Cookie

浏览器把Cookie以KV形式保存下来的一种数据。

服务器生成Cookie发送给浏览器，浏览器保存Cookie，下次访问时带上Cookie标识身份



**若Cookie被认为关闭怎么办？**

**URL重写**

把session id直接附加在URL路径后面，表示当前用户的编号。

*http://www.test.com/test;jsessionid=ByOK3vjFD75aPnrF7C2HmdnV6QZcEbzWoWiBYEnLerjQ99zWpBng!-145788764*

获取时获取session再获取session id

**增加表单隐藏字段**

就是服务器会自动修改表单，添加一个隐藏字段，以便在表单提交时能够把session id传递回服务器。

```
<form name=”"testform”" action=”"/xxx”">
<input type=”"hidden”" name=”"jsessionid”" value=”"ByOK3vjFD75aPnrF7C2HmdnV6QZcEbzWoWiBYEnLerjQ99zWpBng!-145788764″”> <input type=”"text”"> </form>
```



#### Session

一般存储 用户对象信息，包括 用户名，用户ID，创建时间等。

服务器需要知道当前访问自己的是谁。

所以服务器给每个访客一个 **身份标识 sessionId** ，浏览器保存该 **身份标识** 即Cookie 服务器保存该 **身份标识** 即Session

服务器使用 session 把用户的信息临时保存到服务器上。

服务器保存 session 方式 可能是 存放内存中 或redis 等 设置过期时间

**创建：**sessionid第一次产生是在直到某server端程序调用 HttpServletRequest.getSession(true)这样的语句时才被创建。
**删除：**超时；程序调用HttpSession.invalidate()；程序关闭。

**session会因为浏览器的关闭而删除吗？不会，session只会通过上面提到的方式去关闭**



#### 服务器验证方式

![这里写图片描述](https://img-blog.csdn.net/20180705090758830?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI4Mjk2OTI1/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

**缺点：**

1.对服务器内存压力大。

2.扩展性不强，负载均衡 多个服务器问题。

3.CSRF(跨站请求伪造)：session基于Cookie进行用户识别，若Cookie被伪造网站拦截，会受到跨站 **请求伪造** 攻击

**例子**

假如一家银行用以运行转账操作的URL地址如下：http://www.examplebank.com/withdraw?account=AccoutName&amount=1000&for=PayeeName

那么，一个恶意攻击者可以在另一个网站上放置如下代码： <img src="http://www.examplebank.com/withdraw?account=Alice&amount=1000&for=Badman">

如果有账户名为Alice的用户访问了恶意站点，而她之前刚访问过银行不久，登录信息尚未过期，那么她就会损失1000资金。

4.移动端 APP 没有Cookie 无法进行服务器验证方式。



## Springcloud

### 为什么使用微服务？

**单体应用：**应用程序所有功能打包成一个独立单元。

**优点：**开发简单直接

**缺点：**

1.开发效率低，所有开发在同一个项目修改代码，代码冲突不断

2.代码维护难，代码功能耦合在一起，新人不知从何下手

3.部署不灵活，任何小的修改都要重建整个项目

4.稳定性低，某个小问题可能导致整个应用挂掉。

5.扩展性不强

### 怎么理解微服务？

微服务是一种架构风格。它将**单一应用程序**划分成**多个服务**（根据业务具体划分），每个服务运行在其**独立的自己的进程**中，服务之间互相协调。采用轻量级通信机制（HTTP RESTful API）通信。

### 微服务的优势？

1.服务内聚 小 代码容易理解 每个服务只需围绕自己的业务进行构建。

2.提高容错率，单个服务奔溃不会影响到整个系统崩溃。

3.服务独立部署，测试、升级 灵活性高、易于扩展。

### 微服务的缺点？

1.服务个数多，单个服务易于修改和维护，但对于整个系统而言管理复杂度高。

2.各个服务间通信问题

3.服务故障系统追踪问题，系统监控服务问题。

4.各个服务由谁来启动，暂停，什么时候启动，暂停问题。

### **为什么使用springcloud？**

将不同服务剥离出来不同模块，不同模块之间各自维护，不影响其它模块，当某个模块出现问题时，不用将系统暂停，只需修改单个模块即可，减少各模块间的出现问题的几率。

方便调用其他服务接口，同时它提供了各种管理机制来维护这些服务，比如服务失效如何处理，服务请求过多如何限流。日志记录。单个服务崩溃不会影响到其它的服务，容错率高。每个服务能够单独部署。

1. feign通过从服务注册中心拉取的服务注册列表，通过服务名来调用远程接口实现，相比于httpclient、webservice直接通过ip调用，实现了解耦。
2. feign实现了客户端负载均衡
3. feign可以整合hystrix实现断路器功能，而其他方式则不行



### **springcloud大致工作流程？**

1.创建一个 **服务注册中心**

```
eureka-server
启动类添加@EnableEurekaServer
```

2.将 服务 注册 到 服务注册中心

3.客户端通过服务发现机制从注册中心找到服务

```
启动类添加@EnableDiscoveryClient注册成为Eureka客户端
```



### 服务的注册与发现

服务启动类添加@EnableDiscoveryClient

服务启动时会将自己（服务名包括IP和端口）注册到Eruake注册中心中，同一个服务修改端口可以启动多个实例。

服务调用：传递服务名称通过注册中心获取所有的可用实例 通过负载均衡策略调用（ribbon和feign）对应的服务

### eureka主动下线服务方式

**1.**服务自己关闭，eureka心跳检查，90s没有收到eureka服务的续约则会把实例从注册表中删除。

**2**.向eureka注册中心发送delete请求，/eureka/apps/app-name

**注意，Eureka客户端每隔一段时间（默认30秒）会发送一次心跳到注册中心续约。如果通过这种方式下线了一个服务，而没有及时停掉的话，该服务很快又会回到服务列表中，所以先停掉服务，再发送delete请求**

**3.**客户端如果是springboot，那么调用方

DiscoveryManager.getInstance().shutdownComponent();即可。

### CAP

**C强一致性，**分布式系统中所有数据在同一时刻是同样的值（所有节点同一份最新的数据副本）

**A可用性，**集群中部分节点故障后，集群整体是否还能响应客户端的读写请求。

**P分区容错性**，分布式系统分布在多个子网络，每个子网络称为一个区，区间通信失败即为分区容错。（容易出错）网络通信失败无法避免。

CAP理论 一个分布式系统不可能同时满足CAP，由于P分区容错性是必须要保证的，所以只能从A 和 C中权衡。

**A 和 C的矛盾**

本质原因：因为可能通信失败（即出现分区容错）。

如果保证 G2 的一致性，那么 G1 必须在写操作时，锁定 G2 的读操作和写操作。只有数据同步后，才能重新开放读写。锁定期间，G2 不能读写，没有可用性不。

如果保证 G2 的可用性，那么势必不能锁定 G2，所以一致性不成立。

**Eureka遵循 AP**

Eureka各个节点都是平等的，几个节点挂掉不会影响正常节点的工作，剩余的节点依然提供注册和查询服务。

Eureka的客户端在向某个Eureka注册时如果发现连接失败，则会自动切换至其它节点，只要有一台Eureka还在，就能保证服务可用（保证可用性），只不过查到的信息可能不是最新的（不保证强一致性）。

**保护机制**

默认情况下，eureka server在一定时间内没有收到客户端的心跳，便会把该实例从注册表中删除（默认90s）

Eureka保护机制，如果Eureka Server最近1分钟收到renew的次数（所有节点的心跳比例）小于阈值（即预期的最小值，默认为0.85）那么Eureka就认为客户端与注册中心出现了网络故障，此时会出现以下几种情况：

- 1、Eureka不再从注册列表中移除因为长时间没收到心跳而应该过期的服务。
- 2、Eureka仍然能够接受新服务注册和查询请求，但是不会被同步到其他节点上（即保证当前节点仍然可用）
- 3、当网络稳定时，当前实例新的注册信息会被同步到其他节点中。

直到 renew次数大于阀值才会退出自我保护模式。



**出现无法退出保护模式情况？**

两个实例，把一个实例停掉后不再使用，导致一直处于保护模式，被删掉的实例一直处于实例列表中。

**解决：**手动把实例从实例列表中删除

![img](https://img-blog.csdn.net/20180507212209843?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3NpbmF0XzI1NTk2OTY3/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

**注意：**Eureka客户端每隔一段时间（默认30秒）会发送一次心跳到注册中心续约。如果通过这种方式下线了一个服务，而没有及时停掉的话，该服务很快又会回到服务列表中。

**Eureka服务注册中心可配置多节点**

![img](https://img-blog.csdn.net/20180926133910804?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1dlaXhpYW9odWFp/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

新建多个配置文件，相互注册，组成一个集群，实现高可用。各个节点可以看到 Client客户端。

```
server:
  port: 3333  #服务端口号
eureka:
  client:
    service-url:
      #相互注册，组成一个集群，实现高可用
      defaultZone: http://availability2:4444/eureka/,http://availability3:5555/eureka/
  instance:
    #主机名
    hostname: availability1
spring:
  application:
    #服务名称
    name: eureka-availability-server
```



### Rest

Restful面向资源，资源通过URI进行暴露。适合crud

**特征**

1.客户-服务器（Client-Server），提供服务的服务器和使用服务的客户需要被隔离对待。

2.无状态（Stateless），来自客户的每一个请求必须包含服务器处理该请求所需的所有信息。换句话说，服务器端不能存储来自某个客户的某个请求中的信息，并在该客户的其他请求中使用。

3.可缓存（Cachable），服务器必须让客户知道请求是否可以被缓存。（Ross：更详细解释请参考 理解本真的REST架构风格 以及 StackOverflow 的这个问题 中对缓存的解释。）

4.分层系统（Layered System），服务器和客户之间的通信必须被这样标准化：允许服务器和客户之间的中间层（Ross：代理，网关等）可以代替服务器对客户的请求进行回应，而且这些对客户来说不需要特别支持。

5.统一接口（Uniform Interface），客户和服务器之间通信的方法必须是统一化的。（Ross：GET,POST,PUT.DELETE, etc）

6.支持按需代码（Code-On-Demand，可选），服务器可以提供一些代码或者脚本（Ross：Javascrpt，flash，etc）并在客户的运行环境中执行。这条准则是这些准则中唯一不必必须满足的一条。（Ross：比如客户可以在客户端下载脚本生成密码访问服务器。）



### 五大组件？

**服务注册中心——Netflix Eureka** 

各个服务启动时，Eureka Client都会将服务注册到Eureka Server，并且Eureka Client还可以反过来从Eureka Server拉取注册表，从而知道其他服务在哪里

**客服端负载均衡——Netflix Ribbon（Feign包含了Ribbon）**

服务间发起请求的时候，基于Ribbon做负载均衡，从一个服务的多台机器中选择一台

基于Feign的动态代理机制，根据注解和选择的机器，拼接请求URL地址，发起请求

**断路器——Netflix Hystrix（Feign默认支持Hystrix）**

发起请求是通过Hystrix的线程池来走的，不同的服务走不同的线程池，实现了不同服务调用的隔离，避免了服务雪崩的问题

**服务网关——Netflix Zuul**

如果前端、移动端要调用后端系统，统一从Zuul网关进入，由Zuul网关转发请求给对应的服务

**分布式配置——Spring Cloud Config**

中心配置文件，方便管理所有服务配置。



**怎么定位那个服务挂了？**

Eureka服务状态监听。

增加Eureka服务监听类 EurekaStateChangeListener,配置监听事件。

如 服务下线发送邮件信息给程序员。

```
EurekaInstanceCanceledEvent 服务下线事件
EurekaInstanceRegisteredEvent 服务注册事件
EurekaInstanceRenewedEvent 服务续约事件
EurekaRegistryAvailableEvent Eureka注册中心启动事件
EurekaServerStartedEvent Eureka Server启动事件

@Component
public class EurekaStateChangeListener {
    
    @EventListener
    public void listen(EurekaInstanceCanceledEvent eurekaInstanceCanceledEvent) {
        //服务断线事件
        String appName = eurekaInstanceCanceledEvent.getAppName();
        String serverId = eurekaInstanceCanceledEvent.getServerId();
        System.out.println(appName);
        System.out.println(serverId);
    }

```



### Hystrix

**隔离 熔断 降级**

**每个服务分配一个线程池，每个线程池里的线程仅仅用于请求那个服务**

在客户端依赖多个服务，各个服务间相互调用的情况下，防止因为某一个服务出现异常，引起连锁反应，导致整个请求失败，系统不可用。

例如：订单服务调用积分服务，当订单服务最多有100个线程可以处理请求，然后积分服务挂了，每次订单服务调用积分服务时，都会卡住几秒钟，然后抛出超时异常。

1.如果系统处于高并发场景下，大量请求过来，订单服务100个线程都会卡在请求积分服务，导致订单服务没有一个线程可以处理再来的请求。

2.当还有请求来订单服务时，发现订单服务也挂了，不响应任何请求。

**隔离：**订单服务调用积分服务的线程卡死不会影响订单服务调用 其它服务

**熔断：**一定时间内请求次数失败则开启熔断策略，对积分服务熔断，请求积分服务之间返回。

**降级：**积分服务熔断后，每次调用积分服务，在数据库添加一条记录，给某某用户加了多少分，表明因为积分服务挂了，导致没增加成功，等积分服务恢复后，可以根据db记录手动添加积分。

![img](https://img-blog.csdn.net/20171030152058581?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvbWFveWVxaXU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

![img](https://img-blog.csdnimg.cn/2018110708282718.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQyMDQ2MTA1,size_16,color_FFFFFF,t_70)

**熔断器开关逻辑**

服务的健康状况 = 请求失败数 / 请求总数. 
熔断器开关由关闭到打开的状态转换是通过当前**服务健康状况**和设定**阈值**比较决定的.

1. 当熔断器开关关闭时, 请求被允许通过熔断器. 如果当前健康状况高于设定阈值, 开关继续保持关闭. 如果当前健康状况低于设定阈值, 开关则切换为打开状态.
2. 当熔断器开关打开时, 请求被禁止通过.
3. 当熔断器开关处于打开状态, 经过一段时间后, 熔断器会自动进入半开状态, 这时熔断器只允许一个请求通过. 当该请求调用成功时, 熔断器恢复到关闭状态. 若该请求失败, 熔断器继续保持打开状态, 接下来的请求被禁止通过.

熔断器的开关能保证服务调用者在调用异常服务时, 快速返回结果, 避免大量的同步等待. 并且熔断器能在一段时间后（默认5S）继续侦测请求执行结果, 提供恢复服务调用的可能.

**使用**

1.客户端中设置配置文件

2.添加类继承于 HystrixCommand实现具体服务调用逻辑(run)和服务调用失败后的降级逻辑(getFallback)

```
public class UserTimeOutCommand extends HystrixCommand<String> {

    public UserTimeOutCommand() {
    //在Command构造方法中，可以定义当前服务线程池和熔断器相关参数
        super();
    }

    @Override
    protected String run() throws Exception {
    
    }

    @Override
    protected String getFallback() {
        // 执行超时、出错或者开启熔断之后，使用这个方法返回
        // 这种策略称为服务降级
        // 这里可以是一个固定返回值，查询缓存等
        return "服务降级，暂时不可用";
    }
}
```

![img](https://img-blog.csdn.net/20171030152140596?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvbWFveWVxaXU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)



### Feign

**默认使用 轮询 负载均衡算法**

在Feign配置文件中设置负载均衡算法 轮询 随机 响应时间权重

**通过Feign实现不同服务间通信**

**只需用@FeignClient定义一个FeignClient接口 然后调用接口即可**

**启动类添加 @EnableFeignClients**

![img](https://img-blog.csdnimg.cn/20181107082710723.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQyMDQ2MTA1,size_16,color_FFFFFF,t_70)

1.定义一个 api 模块，里面 对应接口 暴露 其它服务 的Controller 方法

<img src="C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200216170253122.png" alt="image-20200216170253122" style="zoom:50%;" />

![image-20200216170324385](C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200216170324385.png)

![image-20200216170921495](C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200216170921495.png)

2.本服务中通过 创建接口 继承 api中的接口，即可使用 api 中的方法，并使用

@FeignClient注册Bean

<img src="C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200216170716608.png" alt="image-20200216170716608" style="zoom:50%;" />

**通过 value 标识 是哪个服务**

![image-20200216170732695](C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200216170732695.png)

3.@Autowired引入Bean 使用

![image-20200216171021581](C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200216171021581.png)

![image-20200216171051853](C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200216171051853.png)

### 

将Http请求抽象化为一个Interface客户端，可以**调用接口**的形式来**执行Http请求**，以达到简化Http调用的目的。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191201125843452.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2NyYXp5bWFrZXJjaXJjbGU=,size_16,color_FFFFFF,t_70)

**第1步：通过Spring IOC 容器实例，装配代理实例，然后进行远程调用。**

加上@FeignClient注解的接口，在服务启动时，会创建一个本地JDK Proxy代理实例，并注册到Spring IOC容器。

**第2步：执行 InvokeHandler 调用处理器的invoke(…)方法**

JDK Proxy动态代理实例的真正的方法调用过程，具体是通过 InvokeHandler 调用处理器完成的。

InvocationHandle，内部保持了一个**远程调用方法实例**和**方法处理器**的一个Key-Value键值对Map映射 

在invoke(…)方法中，会根据Java反射的方法实例，在dispatch 映射对象中，找到对应的 MethodHandler 方法处理器，然后由后者完成实际的HTTP请求和结果的处理。

**第3步：执行 MethodHandler 方法处理器的invoke(…)方法**

invoke(…)方法主要是通过内部成员feign客户端成员 client，完成远程 URL 请求执行和获取远程结果。

**第4步：通过 feign.Client 客户端成员，完成远程 URL 请求执行和获取远程结果**

正常在SpringMVC的Controller中，是将Http请求的信息提取出来注入@RequestMapping标识的方法中；

而Feign是将接口中的信息提取出来，封装成一个Http请求的相关信息，是对SpringMVC解析过程的一个逆向处理。

### Zuul

包含对请求的 **路由** 和 **过滤** 功能

**路由：** 负责将外部请求转发到具体的微服务实例上,是实现外部访问统一入口的基础

**过滤**： 负责对请求的处理过程进行干预,是实现请求校验、服务聚合等功能的基础.

Zuul和Eureka进行整合,将Zuul自身注册为Eureka服务治理下的应用,同时从Eureka中获得其他微服务的消息,也即以后的访问微服务都是通过Zuul跳转后获得.

![img](https://images2017.cnblogs.com/blog/842651/201801/842651-20180125173015115-1769639988.png)

**Zuul挂了怎么办？**

**Nginx+Zuul集群**

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190511211800658.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM4MjUyMDM5,size_16,color_FFFFFF,t_70)

### RPC

RPC 是一种远程调用协议，底层你可以使用 socket 实现，http 实现，netty（NIO）实现，解决分布式系统中服务调用问题，使调用者调用时像在本地调用一样方便。

**SpringCloud Feign基于HTTP协议**

## 文件上传

### MappedByteBuffer内存映射

普通read调用

1. 从磁盘到内核缓冲区的拷贝
2. 内核缓冲区到JVM进程直接缓冲区的拷贝

实现DirectByteBuffer(JVM堆外之间物理内存)

JVM进程通过内存映射方式加载物理文件，将内核空间和用户空间的虚拟地址映射到同一个物理地址，省去了内核与用户空间的往来拷贝。（**不对不对的**）

![img](https://upload-images.jianshu.io/upload_images/5555632-3e0a19a9e2709c81.png?imageMogr2/auto-orient/strip|imageView2/2/w/332/format/webp)

#### 删除文件失败？

**原因** 文件句柄没有释放，JVM中存在着指向文件的指针，导致重命名文件和删除文件失败。

例如：ByteBuffer bb = ByteBuffer.allocateDirect(1024)，这段代码的执行会在堆外占用1k的内存，Java堆内只会占用一个对象的指针引用的大小，堆外的这1k的空间只有当bb对象被回收时，才会被回收，这里会发现一个明显的不对称现象，就是堆外可能占用了很多，而堆内没占用多少，导致还没触发GC，那就很容易出现Direct Memory造成物理内存耗光。

### 分段上传/断点续传

**思路 前端分片webuploader 后端组合**

**首先**计算文件MD5值，从数据库中判断是否存在文件。（超大文件可以取头和尾chunk内容以及整个文件的name+上传时间一起计算md5）

将文件MD5值 作为文件唯一标识（fileId）。

​	1.存在，则 **秒速上传**，直接将 该文件地址 以及相关信息持久化到数据库。

​	2.不存在，则 **正常上传**。

**正常上传：包括 检测MD5 和 上传 两部分**，初次上传 执行 uploadFile操作，创建文件，后面执行 追加操作。

前端根据固定大小对文件进行分片，并且请求中要带上**分片序号和大小和MD5**。前端发送请求顺利到达后台后，服务器只需要按照请求数据中给的分片序号和每片分块大小（分片大小是固定且一样的）算出开始位置，与读取到的文件片段数据，写入文件即可。

**前端**

将大文件依次分片成小文件，每次将小文件读取进内存中，依次计算小文件的MD5值和计算一次大文件的Id(MD5)，进行正常上传，将它们传给后台。

**后台**

每次首先在redis中存放小文件的MD5值 然后以大文件MD5查询数据库判断文件是否存在，然后执行上传逻辑。若MD5存在，则直接移除redis MD5。否则接着通过文件Id和块MD5继续上传剩余部分。

**通过redis 存放文件块MD5值 进行分段上传**

**怎么做到准确判断MD5是否已经存在？（判断不存在后，另一个客户端刚好完成上传任务，数据库写入MD5）**

**前端**

每次执行 **正常上传** 首先 检测MD5，不存在。再执行上传。

**检测大文件 MD5** 存在则 快速上传，否则 正常上传。

**检测小文件块 MD5** 找到继续上传的文件块

```
// 创建上传
var uploader = WebUploader.create({
    swf: '/webuploader-0.1.5/Uploader.swf',
    server: 'index.php',     // 服务端地址
    pick: '#picker',         // 指定选择文件的按钮容器
    resize: false,
    chunked: true,           //开启分片上传
    chunkSize: 1024*1024*4,  //每一片的大小
    chunkRetry: 100,         // 如果遇到网络错误,重新上传次数
    threads: 3,              //上传并发数。允许同时最大上传进程数。
});
```

**后端**

每次检测数据库MD5前，redis中放入 （fileId,fileMD5)。

1.判断MD5存在，走 秒速上传 逻辑中 移除 该 key value;

2.MD5不存在，正常上传逻辑 加锁 ，上传文件前 再判断一次MD5，存在则秒速上传。否则，上传文件。

**对同一个文件的上传 同时只允许一个上传操作**

### 暂停续传

**点击暂停按钮 表示不管后台是否完成了那个文件块的上传，前端都不继续发送下一个文件块给后台，但后台会完成该文件块的上传**

若文件未完全上传完毕，则redis中存放的是 (fileId,MD5), **fileId** 是大文件的MD5 **MD5** 是小文件块的MD5

**点击继续按钮 ** 则通过大文件MD5找出已经上传了最后一块MD5 前端 依次执行判断，直到找到 redis中MD5的下一块后 继续执行上传。

**实现原理**，就是在每个文件上传前，就获取到文件MD5取值，在上传文件前调用接口（/index/checkFileMd5，没错也是秒传的检验接口）如果获取的文件状态是未完成，则返回所有的还没上传的分块的编号，然后前端进行条件筛算出哪些没上传的分块，然后进行上传。

**优化**

不进行 一个个文件块顺序上传 。

而是 通过借助 http 的可并发性，同时上传多个切片，这样从原本传一个大文件，变成了同时传多个小的文件切片，可以大大减少上传时间，需要为每个 文件块排序，并且先在文件库 创建一个文件夹 存放 多个文件切片，最后执行 合并文件操作。

**并发上传**

每次上传前检查文件块MD5，若redis中存在则进行下一块上传，然后将该MD5放入redis中，等到整个文件上传完毕后，移除该文件所有块redis md5。（List结构存放MD5 序号1,序号2）

或者 上传文件带有一个临时文件存放文件块对应的字节状态。

**检查文件上传进度**

前端发送 文件块总数和当前文件块位置 

后端 通过 文件块总数和文件块位置判断是否上传完毕。

**若最后一块上传完毕 但是前面还未上传完毕?**

不能只用文件块总数和文件块位置判断。

添加临时文件，可以判断哪一个上传完毕，那一块没有上传完毕。

**1.** 设置文件长度为总文件块数（7块则文件大小为7byte（字节））

**2.**设置起始偏移量（第几块(byte)）

**3.**写入Byte.MAX_VALUE 127,1字节8位（全为1则=127）

4.通过文件每个字节与127取与运算判断，文件块是否上传完毕，当所有文件块上传完毕，则删除临时文件。

```
File confFile = new File(filePath,fileName+".conf");
        RandomAccessFile confAccessFile = new RandomAccessFile(confFile,"rw");
        //设置文件长度为总文件块数
        confAccessFile.setLength(param.getChunkTotal());
        //设置起始偏移量
        confAccessFile.seek(param.getChunk());
        //将指定的一个字节写入文件中 127，
        confAccessFile.write(Byte.MAX_VALUE);
        byte[] completeStatusList = FileUtils.readFileToByteArray(confFile);
        byte isComplete = Byte.MAX_VALUE;
　　　　　//这一段逻辑有点复杂，看的时候思考了好久，创建conf文件文件长度为总分片数，每上传一个分块即向conf文件中写入一个127，那么没上传的位置就是默认的0,已上传的就是Byte.MAX_VALUE 127
        for(int i = 0; i<completeStatusList.length && isComplete==Byte.MAX_VALUE; i++){
　　　　　　　// 按位与运算，将&两边的数转为二进制进行比较，有一个为0结果为0，全为1结果为1  eg.3&5  即 0000 0011 & 0000 0101 = 0000 0001   因此，3&5的值得1。
            isComplete = (byte)(isComplete & completeStatusList[i]);
            System.out.println("check part " + i + " complete?:" + completeStatusList[i]);
        }
        if(isComplete == Byte.MAX_VALUE){
　　　　　　  //如果全部文件上传完成，删除conf文件
　　　　　　  confFile.delete();
            return true;
        }
```

**具体代码**

**文件实体**

```
public class MultipartFileParam {
    @ApiModelProperty("文件传输ID MD5")
    private String taskId;
    @ApiModelProperty("当前为第几分片")
    private int chunk;
    @ApiModelProperty("每个分块的大小")
    private long size;
    @ApiModelProperty("分片总数")
    private int chunkTotal;
    @ApiModelProperty("分块文件传输对象")
    private MultipartFile file;
    }
```

**分块上传**

```
第一步：获取RandomAccessFile,随机访问文件类的对象
第二步：调用RandomAccessFile的getChannel()方法，打开文件通道 FileChannel
第三步：获取当前是第几个分块，计算文件的最后偏移量
第四步：获取当前文件分块的字节数组，用于获取文件字节长度
第五步：使用文件通道FileChannel类的 map（）方法创建直接字节缓冲器  MappedByteBuffer
第六步：将分块的字节数组放入到当前位置的缓冲区内  mappedByteBuffer.put(byte[] b);
第七步：释放缓冲区
第八步：检查文件是否全部完成上传

		//第一步
        RandomAccessFile raf = new RandomAccessFile(tempFile,"rw");
        //第二步
        FileChannel fileChannel = raf.getChannel();
        //第三步
        long offset = param.getChunk() * param.getSize();
        //第四步
        byte[] fileData = param.getFile().getBytes();
        //第五步
        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE,offset,fileData.length);
        //第六步
        mappedByteBuffer.put(fileData);
        //第七步
        FileUtil.freeMappedByteBuffer(mappedByteBuffer);
        fileChannel.close();
        raf.close();
        //第八步
        boolean isComplete = checkUploadStatus(param,fileName,filePath);
        if(isComplete){
            renameFile(tempFile,fileName);
        }
```

**释放mappedByteBuffer**

```
/**
     * 在MappedByteBuffer释放后再对它进行读操作的话就会引发jvm crash，在并发情况下很容易发生
     * 正在释放时另一个线程正开始读取，于是crash就发生了。所以为了系统稳定性释放前一般需要检 查是否还有线程在读或写
     * @param mappedByteBuffer
     */
    public static void freedMappedByteBuffer(final MappedByteBuffer mappedByteBuffer) {
        try {
            if (mappedByteBuffer == null) {
                return;
            }
            mappedByteBuffer.force();
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    try {
                        Method getCleanerMethod = mappedByteBuffer.getClass().getMethod("cleaner", new Class[0]);
                        //可以访问private的权限
                        getCleanerMethod.setAccessible(true);
                        //在具有指定参数的 方法对象上调用此 方法对象表示的底层方法
                        sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod.invoke(mappedByteBuffer,
                                new Object[0]);
                        cleaner.clean();
                    } catch (Exception e) {
                        logger.error("clean MappedByteBuffer error!!!", e);
                    }
                    logger.info("clean MappedByteBuffer completed!!!");
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
```



```
前端分片 一个个上传					  
fileReader.readAsArrayBuffer(blobSlice.call(file, 0, chunkSize));

初次上传
path = appendFileStorageClient.uploadAppenderFile(UpLoadConstant.DEFAULT_GROUP, file.getInputStream(),file.getSize(), FileUtil.extName((String) paramMap.get("name")));

追加方式实际实用如果中途出错多次,可能会出现重复追加情况,这里改成修改模式,即时多次传来重复文件块,依然可以保证文件拼接正确
appendFileStorageClient.modifyFile(UpLoadConstant.DEFAULT_GROUP, noGroupPath, file.getInputStream(), file.getSize(),historyUpload);
```

**问题 多个客户端同时上传同一个文件？**

即多个线程同时 上传 操作问题，上传操作加锁。

**初次上传：**可能有多个线程同时进入 上传操作外 等待，即可能创建多个相同的文件。（概率较低 所以不加处理）

**追加上传：**使用修改上传操作，计算偏移量，即多个线程上传相同内容，会被忽略。

**上传成功：**redis中不存在该文件的任意文件块MD5，数据库中存储大文件MD5

**上传一半：** redis中存放的文件块MD5标识最后一块以上传的文件块，存储时间 30天 ，即30天内不继续上传且无其他人上传过相同文件，则需要重新上传。

**redis文件结构  (fileId,MD5,index,group,appendFileName)**

### 下载

**流程**

**前端**： 获取 用户id 和 点击的多个文件vidList（Virtualaddress）传给后台。

**后台：**首先将 用户Id和VirtualaddressId结合找到FileId。通过FileId找到FileName和FileLocation。然后依次下载。

**批量下载**

将多个文件下载压缩成压缩包。

首先 创建一个压缩文件，将各个文件依次写入压缩文件中。



## 公开/私密 分享文件

1.选择文件点击 **分享** 选择 **加密/公开** 设置 **有效期** ，创建分享链接。

2.将 文件服务器位置  发送给后台。

3.后台 通过 文件服务器位置 获取文件名 并生成随机shareId字符串和 分享密码

4.构建shareDO实例主要是 shareId userId password lock createTime expireTime

5.构建shareMapDo实例主要是 shareId virtAddress

6.后台返回给客户端 访问网址和分享密码

7.客户 访问网址 选择保存文件或者下载文件 输入分享密码。