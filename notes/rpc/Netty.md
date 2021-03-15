```
Jedis多线程下的坑
read time out 
jedis 操作redis的时候，对底层操作命令进行了缓存，所以当某一次redis操作出现异常时（数据类型不对，时间过长等异常），jedis实例中的缓存数据不会被清空，然后被放回连接池，当下次其他人从连接池拿出该连接时，发送的命令还是上次留下的缓存，所以如果两个线程使用的数据类型不一样，就会ClassCastException。但是加入两个线程使用的数据类型是一样的，那么系统不会报异常，但是数据可能全是错乱的，后果将不可设想。
解决方法：catch到异常时，关闭jedis。

对Jedispool要用双重加锁判断，防止多线程获取到的不是同一个jedispool
jedispool.getResource是线程安全，无需再加锁

MaxActive:最大连接数
MaxIdle:最大空闲数
MAXWait:最大等待时间，单位毫秒 当连接池中无可用连接时会进行等待MAXWait直到报出Could not get Resource异常

Redis操作内存时间平均是毫秒级别，但数据量大时，也会发送错误

jedis操作过程：
例如 jedis.sadd() 源码如下：
public Long sadd(final String key, final String... members) {
checkIsInMulti();
client.sadd(key, members);
return client.getIntegerReply();
}
client是Client（与redis服务器连接的一个客户端）的一个实例
client extends Connection;Connection包装了对Redis Server的Socket操作，命令操作通过Socket.getOutputStream()输出流将命令信息发送到redis.server，然后等待server的响应后，通过socket.getInputStream()将命令结果返回。
redisserver是单线程执行所有连接发送过来的命令的，按照默认的FIFO方式处理请求
所以client.getIntegerReply会等待timeout时间，直到报出read time out 异常
再初始化jedispool时可以设置timeout new JedisPool(config,host,port,timeout);
```

<img src="https://img-blog.csdn.net/20180211180227660?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWNoZW55dWFu/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70" alt="img"  />

![img](https://images2015.cnblogs.com/blog/758472/201510/758472-20151015175229944-54806156.png)

#### UDP不会沾包

UDP是面向消息的协议，每个UDP段都是一条消息，应用程序必须以消息为单位提取数据，不能一次提取任意字节的数据，在每个UDP包中就有了消息头（消息来源地址，端口等信息）UDP是个"数据包"协议,也就是两段数据间是有界限的,在接收端要么接收不到数据要么就是接收一个完整的一段数据,不会少接收也不会多接收.

#### TCP沾包拆包

TCP是个“流”协议，所谓流，就是没有界限的一串数据。大家可以想想河里的流水，是连成一片的，其间并没有分界线。TCP底层并不了解上层业务数据的具体含义，它会根据TCP缓冲区（滑动窗口）的实际情况进行包的划分，所以在业务上认为，一个完整的包可能会被TCP拆分成多个包进行发送，也有可能把多个小的包封装成一个大的数据包发送，这就是所谓的TCP粘包和拆包问题。**包问题实际上是在应用层里提取出自己想要的信息的问题**

##### 举例：![img](https://images2015.cnblogs.com/blog/990532/201612/990532-20161212192914995-1758321651.png)

假设客户端分别发送了两个数据包D1和D2给服务端，由于服务端一次读取到的字节数是不确定的，故可能存在以下4种情况。

（1）服务端分两次读取到了两个独立的数据包，分别是D1和D2，没有粘包和拆包；

（2）服务端一次接收到了两个数据包，D1和D2粘合在一起，被称为TCP粘包；

（3）服务端分两次读取到了两个数据包，第一次读取到了完整的D1包和D2包的部分内容，第二次读取到了D2包的剩余内容，这被称为TCP拆包；

（4）服务端分两次读取到了两个数据包，第一次读取到了D1包的部分内容D1_1，第二次读取到了D1包的剩余内容D1_2和D2包的整包。

如果此时服务端TCP接收滑窗非常小，而数据包D1和D2比较大，很有可能会发生第五种可能，即服务端分多次才能将D1和D2包接收完全，期间发生多次拆包。

**TCP 以太网帧**

在以太网**链路**上的数据包称作以太帧，在802.3标准里，规定了一个以太帧的**数据部分(Payload)的最大长度是1500个字节(MTU)**，再加上**14字节链路头和4字节的FCS**，所以以太网帧的最大长度为1518。另外，以太网帧的最小长度为64字节。

**TCP MSS**

MSS，最大报文段长度。在连接建立的时候，即在发送SYN段的时候，同时会将MSS发送给对方（MSS选项只能出现在SYN段中！！！），告诉对端他期望接收的TCP报文段数据部分最大长度。

**MSS作用**

一般来说，TCP 报文段携带的数据当然是越多越好。

如果 TCP 报文段传输的数据只有一个字节，在 IP 层传输的数据报大小就是 40 + 1 = 41 字节（至少 20 字节的 IP 头 + 20 字节的 TCP 头 + 1 字节数据）。这样网络的利用率就只有 1/41. 传输 n 字节的数据利用率就是 n/n+40，显然 TCP 报文段传输的数据如果越大，网络利用率就越高。

但是实际上并非如此。因为网络传输数据时，数据是最终是要交付到链路层协议上的，也就是说最后要封装成“帧”。

二型以太网（Ethernet Type 2）中规定，帧的大小不能超过 1518 个字节（14 字节的帧头 + 4 字节帧校验和 + 最多 1500 字节数据）。所以 IP 数据报的大小如果超过了 1500 字节，要想交付给链路层就必须进行“分片”。

“分片”指的是一个IP数据报太大，需要拆分成一个一个的小段，变成多个IP数据报。

这种分片显然是不利的，有一定的开销。为了避免分片开销，我们希望IP数据报的大小不超过1500字节。除去IP数据报的首部20字节，也就是希望TCP报文段不超过1480字节。再减去TCP报文段首部20字节，也就是TCP携带的数据不超过1460字节。

#### TCP沾包拆包发送原因

1.应用程序写入的数据大于套接字缓冲区大小，这将会发生拆包。

2.应用程序写入数据小于套接字缓冲区大小，网卡将应用多次写入的数据发送到网络上，这将会发生粘包。

3.进行MSS（最大报文长度）大小的TCP分段，当TCP报文长度-TCP头部长度>MSS的时候将发生拆包。

4.接收方法不及时读取套接字缓冲区数据，这将发生粘包。

5.由Nagle算法造成的发送端的粘包:Nagle算法是一种改善网络传输效率的算法.

简单的说,当我们提交一段数据给TCP发送时,TCP并不立刻发送此段数据,而是等待一小段时间,看看在等待期间是否还有要发送的数据,若有则会一次把这两段数据发送出去.

<img src="https://images2015.cnblogs.com/blog/990532/201612/990532-20161212193751011-992309759.png" alt="img" style="zoom:80%;" />

![img](https://img-blog.csdn.net/20160529140813320?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQv/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

```
TCP:报文（1460（最大数据）+20（IP首部））（大于1460拆包）
而UDP协议没有MSS，所以发送给IP层时，由IP协议完成分片。例如发送数据长度为2000字节，使用TCP协议时(例如MSS是1460)，TCP分成两个包上送到IP层，此时IP层不用分片；而使用UDP时，一次上送到IP层，此时IP层需要分片。
IP：报文（1480（最大数据）+20（IP首部））（大于1480分片）
链路层：以太网帧（1500（最大数据）+14（帧头）+4（帧校验和））
```

#### 沾包问题解决策略

由于底层的TCP无法理解上层的业务数据，所以在底层是无法保证数据包不被拆分和重组的，这个问题只能通过上层的应用协议栈设计来解决，根据业界的主流协议的解决方案，可以归纳如下。

（1）消息定长，例如每个报文的大小为固定长度200字节，如果不够，空位补空格；

（2）在包尾增加回车换行符进行分割，例如FTP协议；

（3）将消息分为消息头和消息体，消息头中包含表示消息总长度（或者消息体长度）的字段，通常设计思路为消息头的第一个字段使用int32来表示消息的总长度；

（4）更复杂的应用层协议。

**拆包原理**

1. 如果当前读取的数据不足以拼接成一个完整的业务数据包，那就保留该数据，继续从 TCP 缓冲区中读取，直到得到一个完整的数据包。
2. 如果当前读到的数据加上已经读取的数据足够拼接成一个数据包，那就将已经读取的数据拼接上本次读取的数据，构成一个完整的业务数据包传递到业务逻辑，多余的数据仍然保留，以便和下次读到的数据尝试拼接。

### Netty自带的拆包器（Netty的优势）

#### 1. 固定长度的拆包器 FixedLengthFrameDecoder

如果你的应用层协议非常简单，每个数据包的长度都是固定的，比如 100，那么只需要把这个拆包器加到 pipeline 中，Netty 会把一个个长度为 100 的数据包 (ByteBuf) 传递到下一个 channelHandler。

#### 2. 行拆包器 LineBasedFrameDecoder

从字面意思来看，发送端发送数据包的时候，每个数据包之间以换行符作为分隔，接收端通过 LineBasedFrameDecoder 将粘过的 ByteBuf 拆分成一个个完整的应用层数据包。

#### 3. 分隔符拆包器 DelimiterBasedFrameDecoder

DelimiterBasedFrameDecoder 是行拆包器的通用版本，只不过我们可以自定义分隔符。

#### 4. 基于长度域拆包器 LengthFieldBasedFrameDecoder

最后一种拆包器是最通用的一种拆包器，只要你的自定义协议中包含长度域字段，均可以使用这个拆包器来实现应用层拆包。由于上面三种拆包器比较简单，读者可以自行写出 demo，接下来，我们就结合我们小册的自定义协议，来学习一下如何使用基于长度域的拆包器来拆解我们的数据包。

**StringDecoder的功能非常简单，就是将接收到的对象转换成字符串，然后继续调用后面的handler。**

## 基于长度域拆包器 LengthFieldBasedFrameDecoder

netty将具体如何拆包抽象出一个decode抽象方法，不同的拆包器实现不同的decode,就能实现不同协议的拆包

**只要能decode出来的，都是完整的数据包**

### 1.基于长度的拆包

![img](https://upload-images.jianshu.io/upload_images/1357217-f34bbbe0438cb9e7.png?imageMogr2/auto-orient/strip|imageView2/2/w/1100/format/webp)

```java
前面几个字节表示数据包的长度（不包括长度域），后面是具体的数据。
拆完之后数据包是一个完整的带有长度域的数据包（之后即可传递到应用层解码器进行解码），
new LengthFieldBasedFrameDecoder(Integer.MAX, 0, 4，0，0);
1.第一个参数是 maxFrameLength 表示的是包的最大长度，超出包的最大长度netty将会做一些特殊处理，后面会讲到
2.第二个参数指的是长度域的偏移量lengthFieldOffset，在这里是0，表示无偏移 表示从那个字节开始是长度域
3.第三个参数指的是长度域长度lengthFieldLength，这里是4，表示长度域的长度为4 表示包体有多少字节数据
4.header的长度，即相关协议头（魔数等）的长度
5.第五个参数initialBytesToStrip表示忽略前面多少个字节 这里0 表示不忽略
```

### 2.基于长度的截断拆包

![img](https://upload-images.jianshu.io/upload_images/1357217-1b7e9bb680560c93.png?imageMogr2/auto-orient/strip|imageView2/2/w/1016/format/webp)

```java
如果我们的应用层解码器不需要使用到长度字段，那么我们希望netty拆完包之后，是这个样子
new LengthFieldBasedFrameDecoder(Integer.MAX, 0, 4, 0, 4);
前面三个参数的含义和上文相同，第四个参数我们后面再讲，而这里的第五个参数就是initialBytesToStrip，这里为4，表示获取完一个完整的数据包之后，忽略前面的四个字节，应用解码器拿到的就是不带长度域的数据包
```

### 3.基于偏移长度的拆包

![img](https://upload-images.jianshu.io/upload_images/1357217-1a6c42583c44e1dc.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

```java
下面这种方式二进制协议是更为普遍的，前面几个固定字节表示协议头，通常包含一些magicNumber，protocol version 之类的meta信息，紧跟着后面的是一个长度域，表示包体有多少字节的数据

lengthFieldOffset 是4，表示跳过4个字节之后的才是长度域，即前面4个字节是相关协议头
new LengthFieldBasedFrameDecoder(Integer.MAX, 4, 4);
```

### 4.基于可调整长度的拆包

![img](https://upload-images.jianshu.io/upload_images/1357217-661c399a5b1fee9a.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

```java
即长度域在前，header在后，这种情况又是如何来调整参数达到我们想要的拆包效果呢？

1.长度域在数据包最前面表示无偏移，lengthFieldOffset 为 0
2.长度域的长度为3，即lengthFieldLength为3
2.长度域表示的包体的长度略过了header，这里有另外一个参数，叫做 lengthAdjustment，包体长度调整的大小，长度域的数值表示的长度加上这个修正值表示的就是带header的包，这里是 12+2，header和包体一共占14个字节

new LengthFieldBasedFrameDecoder(Integer.MAX, 0, 3, 2, 0);//2 表示header长度
```

#### ChannelHandler的生命周期

1. `handlerAdded()` ：指的是当检测到新连接之后，调用 `ch.pipeline().addLast(new LifeCyCleTestHandler());` 之后的回调，表示在当前的 channel 中，已经成功添加了一个 handler 处理器。

2. `channelRegistered()`：这个回调方法，表示当前的 channel 的所有的逻辑处理已经和某个 NIO 线程建立了绑定关系，类似我们在[Netty 是什么？](https://juejin.im/book/5b4bc28bf265da0f60130116/section/5b4bc28b5188251b1f224ee5)这小节中 BIO 编程中，accept 到新的连接，然后创建一个线程来处理这条连接的读写，只不过 Netty 里面是使用了线程池的方式，只需要从线程池里面去抓一个线程绑定在这个 channel 上即可，这里的 NIO 线程通常指的是 `NioEventLoop`,不理解没关系，后面我们还会讲到。

3. `channelActive()`：当 channel 的所有的业务逻辑链准备完毕（也就是说 channel 的 pipeline 中已经添加完所有的 handler）以及绑定好一个 NIO 线程之后，这条连接算是真正激活了，接下来就会回调到此方法。

4. `channelRead()`：客户端向服务端发来数据，每次都会回调此方法，表示有数据可读。

5. `channelReadComplete()`：服务端每次读完一次完整的数据之后，回调该方法，表示数据读取完毕。

6. `channelInactive()`: 表面这条连接已经被关闭了，这条连接在 TCP 层面已经不再是 ESTABLISH 状态了

7. `channelUnregistered()`: 既然连接已经被关闭，那么与这条连接绑定的线程就不需要对这条连接负责了，这个回调就表明与这条连接对应的 NIO 线程移除掉对这条连接的处理

8. `handlerRemoved()`：最后，我们给这条连接上添加的所有的业务逻辑处理器都给移除掉。

   **handlerAdded()与handlerRemoved()**

   这两个方法用于为channel添加handler和移除handler

   **channelActive()与channelInActive()**

   表明TCP连接的建立与释放，channelActive被调用连接数+1，channelInActive被调用连接数-1

   当对端断开时，就会触发inactive 对端连接上时，触发active

   **channelRead**

   拆包
   
   ![img](https://user-gold-cdn.xitu.io/2018/10/14/1666fdc2bdcf3f9e?w=2176&h=658&f=png&s=104798)

#### 身份验证

直接在执行逻辑前添加用户验证Handler

防止次次都验证身份，在身份验证成功后添加一句ctx.pipeline().remove(this);移除当前身份验证handler（热拔插机制）

## NIO Reactor模型

### 为什么需要Reactor模式？

处理WEB请求一般有两种，**基于线程** 或 **事件驱动**

**基于线程**

服务器每收到一个请求就开启一个新的线程处理请求。

**缺点**

高并发情况下，不断创建新的线程，降低web服务器的性能，并且当线程在执行IO操作时，CPU处于空闲的状态，同样也会造成cpu资源的浪费。

**(IO操作)** 

对于一个IO读操作，首先CPU会发出一个读取IO数据的指令，不过IO读写是不消耗CPU的，此时CPU会处于空闲状态，
可以去做其他线程的一些处理，然后等IO操作完成时，会通知CPU数据已读取完毕，也就相当于内核通知线程数据已经准备完毕，可以拿过去用了。

### Reactor思想：分而治之+事件驱动

**分而治之**

reactor会解耦并发请求的服务并分发给对应的事件处理器来处理

一个连接完整的网络处理过程一般为：accept、read、decode、process、encode、send这几步

Reactor模式将每个步骤映射为一个Task,服务端线程执行的最小逻辑单元不再是一个完整的网络请求而是一个个Task,且采用非阻塞方式执行。

**事件驱动**

每个Task对应特定网络事件，当Task准备就绪时，Reactor收到对应的网络事件通知，并将Task分发给绑定了对应事件的Handler处理

**Reactor：**负责响应事件，将事件分发给绑定了该事件的Handler处理；

**Handler：**事件处理器，绑定了某类事件，负责执行对应事件的Task对事件进行处理；

**Acceptor：**Handler的一种，绑定了connect事件。当客户端发起connect请求时，Reactor会将accept事件分发给Acceptor处理

#### 各种Reactor模式

##### 单线程Reactor

<img src="https://upload-images.jianshu.io/upload_images/5249993-a5f8399bf59b25c6.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp" alt="img" style="zoom:80%;" />

**缺点：**

a)不能利用多核CPU；

b)一个线程需要执行处理所有的accept、read、decode、process、encode、send事件，处理成百上千的链路时性能上无法支撑；

c)一旦reactor线程意外跑飞或者进入死循环，会导致整个系统通信模块不可用。

##### 多线程Reactor

<img src="https://upload-images.jianshu.io/upload_images/5249993-5318716bb8f8cfda.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp" alt="img" style="zoom:80%;" />

**特点：**

a)有专门一个reactor线程用于监听服务端ServerSocketChannel，接收客户端的TCP连接请求；

b)网络IO的读/写操作等由一个**worker reactor线程池**负责，由线程池中的NIO线程负责监听SocketChannel事件，进行消息的读取、解码、编码和发送。

c)一个NIO线程可以同时处理N条链路（客户端），但是一个链路（客户端）只注册在一个NIO线程上处理，防止发生并发操作问题。

##### 主从多线程

<img src="https://upload-images.jianshu.io/upload_images/5249993-eea74e49531abc2f.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp" alt="img" style="zoom:80%;" />

一个NIO线程负责监听和处理所有的客户端连接可能会存在性能问题时使用主从多线程

**特点：**

a)服务端用于接收客户端连接的不再是个1个单独的reactor线程，而是一个boss reactor线程池；

b)服务端启用多个ServerSocketChannel监听不同端口时，每个ServerSocketChannel的监听工作可以由线程池中的一个NIO线程完成。

### Netty线程模型

<img src="https://upload-images.jianshu.io/upload_images/5249993-a67abc1374958c5d.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp" alt="img" style="zoom:80%;" />

netty线程模型采用“服务端监听线程”和“IO线程”分离的方式，与多线程Reactor模型类似。

抽象出**NioEventLoop**来表示**一个**不断循环执行处理任务的**线程**，每个NioEventLoop有一个selector，用于监听绑定在其上的socket链路。

![image-20200119011423238](C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200119011423238.png)

#### 1、串行化设计避免线程竞争

netty采用串行化设计理念，从消息的读取->解码->处理->编码->发送，始终由IO线程NioEventLoop负责。整个流程不会进行线程上下文切换，数据无并发修改风险。

一个NioEventLoop聚合一个多路复用器selector，因此可以处理多个客户端连接。

Netty的处理策略是每当有一个新的客户端接入，则从NioEventLoop线程组中顺序获取一个可用的NioEventLoop，当到达数组上限之后，重新返回到0，通过这种方式，可以基本保证各个NioEventLoop的负载均衡。一个客户端连接只注册到一个NioEventLoop上，这样就避免了多个IO线程去并发操作它。

netty只负责提供和管理“IO线程”，其他的业务线程模型由用户自己集成。

时间可控的简单业务建议直接在“IO线程”上处理，复杂和时间不可控的业务建议投递到后端业务线程池中处理。

#### 2、定时任务与时间轮

NioEventLoop中的Thread线程按照时间轮中的步骤不断循环执行：

a)在时间片Tirck内执行selector.select()轮询监听IO事件；

b)处理监听到的就绪IO事件；

c)执行任务队列taskQueue/delayTaskQueue中的非IO任务。

### 三、NioEventLoop与NioChannel类关系

![img](https://upload-images.jianshu.io/upload_images/5249993-65625225f53d2cbb.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

一个NioEventLoopGroup下包含多个NioEventLoop

每个NioEventLoop中包含有一个Selector，一个taskQueue，一个delayedTaskQueue

每个NioEventLoop的Selector上可以注册监听多个AbstractNioChannel.ch

每个AbstractNioChannel只会绑定在唯一的NioEventLoop上

每个AbstractNioChannel都绑定有一个自己的DefaultChannelPipeline

### 四、NioEventLoop线程执行过程

#### 1、轮询监听的IO事件

**1）netty的轮询注册机制**

netty将AbstractNioChannel内部的jdk类SelectableChannel对象注册到NioEventLoopGroup中的jdk类Selector对象上去，并且将AbstractNioChannel作为SelectableChannel对象的一个attachment附属上。

这样在Selector轮询到某个SelectableChannel有IO事件发生时，就可以直接取出IO事件对应的AbstractNioChannel进行后续操作。

**2）循环执行阻塞selector.select(timeoutMIllis)操作直到以下条件产生**

a)轮询到了IO事件（selectedKey != 0）

b)oldWakenUp参数为true

c)任务队列里面有待处理任务（hasTasks()）

d)第一个定时任务即将要被执行（hasScheduledTasks()）

e)用户主动唤醒（wakenUp.get()==true）

**3）解决JDK的NIO epoll bug**

该bug会导致Selector一直空轮询，最终导致cpu 100%。

在每次selector.select(timeoutMillis)后，如果没有监听到就绪IO事件，会记录此次select的耗时。如果耗时不足timeoutMillis，说明select操作没有阻塞那么长时间，可能触发了空轮询，进行一次计数。

计数累积超过阈值（默认512）后，开始进行Selector重建：

a)拿到有效的selectionKey集合

b)取消该selectionKey在旧的selector上的事件注册

c)将该selectionKey对应的Channel注册到新的selector上，生成新的selectionKey

d)重新绑定Channel和新的selectionKey的关系

**4）netty优化了sun.nio.ch.SelectorImpl类中的selectedKeys和publicSelectedKeys这两个field的实现**

netty通过反射将这两个filed替换掉，替换后的field采用数组实现。

这样每次在轮询到nio事件的时候，netty只需要O(1)的时间复杂度就能将SelectionKey塞到set中去，而jdk原有field底层使用的hashSet需要O(lgn)的时间复杂度。

#### 2、处理IO事件

**1）对于boss NioEventLoop来说，轮询到的是基本上是连接事件（OP_ACCEPT）：**

a)socketChannel = ch.accept()；

b)将socketChannel绑定到worker NioEventLoop上；

c)socketChannel在worker NioEventLoop上创建register0任务；

d)pipeline.fireChannelReadComplete();

**2）对于worker NioEventLoop来说，轮询到的基本上是IO读写事件（以OP_READ为例）：**

a)ByteBuffer.allocateDirect(capacity)；

b)socketChannel.read(dst);

c)pipeline.fireChannelRead();

d)pipeline.fireChannelReadComplete();

#### 3、处理任务队列

**1）处理用户产生的普通任务**

NioEventLoop中的Queue<Runnable> taskQueue被用来承载用户产生的普通Task。

taskQueue被实现为netty的mpscQueue，即多生产者单消费者队列。netty使用该队列将外部用户线程产生的Task聚集，并在reactor线程内部用单线程的方式串行执行队列中的Task。

当用户在非IO线程调用Channel的各种方法执行Channel相关的操作时，比如channel.write()、channel.flush()等，netty会将相关操作封装成一个Task并放入taskQueue中，保证相关操作在IO线程中串行执行。

**2）处理用户产生的定时任务**

NioEventLoop中的Queue<ScheduledFutureTask<?>> delayedTaskQueue = new PriorityQueue被用来承载用户产生的定时Task。

当用户在非IO线程需要产生定时操作时，netty将用户的定时操作封装成ScheduledFutureTask，即一个netty内部的定时Task，并将定时Task放入delayedTaskQueue中等待对应Channel的IO线程串行执行。

为了解决多线程并发写入delayedTaskQueue的问题，netty将添加ScheduledFutureTask到delayedTaskQueue中的操作封装成普通Task，放入taskQueue中，通过NioEventLoop的IO线程对delayedTaskQueue进行单线程写操作。

**3）处理任务队列的逻辑**

a)将已到期的定时Task从delayedTaskQueue中转移到taskQueue中

b)计算本次循环执行的截止时间

c)循环执行taskQueue中的任务，每隔64个任务检查一下是否已过截止时间，直到taskQueue中任务全部执行完或者超过执行截止时间。

### 五、Netty中Reactor线程和worker线程所处理的事件

#### 1、Server端NioEventLoop处理的事件

![img](https://upload-images.jianshu.io/upload_images/5249993-2ec7c451bd3e5ce9.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

#### 2、Client端NioEventLoop处理的事件

![img](https://upload-images.jianshu.io/upload_images/5249993-8c7ddead886c3b47.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

### Boss线程和Worker线程

server在启动的时候会开启两个线程：bossGroup和workerGroup，这两个线程分别是boss线程池（用于接收client请求）和worker线程池（用于处理具体的读写操作），这两个线程调度器都是NioEventLoopGroup，bossGroup有一个NioEventLoop，而worker线程池有n*cup数量个NioEventLoop。

Boss线程：每个server服务器都会有一个boss线程，每绑定一个InetSocketAddress都会产生一个boss线程，比如：我们开启了两个服务器端口80和443，则我们会有两个boss线程。一个boss线程在端口绑定后，会接收传进来的连接，一旦连接接收成功，boss线程会指派一个worker线程处理连接。

Worker线程：一个NioServerSocketChannelFactory会有一个或者多个worker线程。一个worker线程在非阻塞模式下为一个或多个Channels提供非阻塞读或写。

boss线程只是接收客户端socket并初始化客户端channle，将channel丢给acceptor，acceptor会将这个channel注册到worker线程中。整个loop过程都是一个非阻塞过程（全部异步化），同时boss中不会做耗时的I/O读取，只是将channel丢给worker。因此是一个高效的loop过程。

### Netty零拷贝和操作系统零拷贝

在操作系统层面上的零拷贝是指避免在**用户态与内核态之间来回拷贝数据**的技术。

Netty中的零拷贝与操作系统层面上的零拷贝不完全一样, Netty的零拷贝**完全是在用户态**(Java层面)的，更多是数据操作的优化。

### Netty零拷贝

**以下5点体现零拷贝**

**1.进行网络读写IO时（Socket），使用堆外内存，即直接由堆外内存--》网卡，而如果是 堆内内存 ，需要首先将堆内内存 转至 堆外内存，再转成 网卡 多一次拷贝**

**2.Netty**的文件传输调用transferTo方法，可以直接将文件缓冲区的数据发送到目标Channel，避免通过循环write方式导致的内存拷贝问题。

**3.Netty**提供**CompositeByteBuf类**, 可以**将多个ByteBuf在逻辑上合并为一个ByteBuf**, 避免了各个ByteBuf之间的拷贝。**逻辑上的拷贝**。

**4**.通过**wrap**操作, 我们可以将byte[]数组、ByteBuf、ByteBuffer等包装成一个Netty ByteBuf对象, 进而避免拷贝操作。

**5.ByteBuf**支持**slice**操作，可以将ByteBuf分解为多个共享同一个存储区域的ByteBuf, 避免内存的拷贝。**逻辑上的拆分**。

### Netty责任链设计模式

**责任链模式：**为请求创建一个处理对象的链。

**发起请求**和具体**处理请求**过程**解耦**，责任链上的Handler负责处理请求，客户端只需要发送请求到链上不用关心谁来处理该请求。

**channelInBoundHandler:**处理入站IO事件

**channeloutBoundHandler:**处理出战IO事件

Netty Pipeline的责任链是通过ChannelHandlerContext对象串联的，ChannelHandlerContext对象里封装了ChannelHandler对象，通过prev和next节点实现双向链表。Pipeline的首尾节点分别是head和tail，当selector轮询到socket有read事件时，将会触发Pipeline责任链，从head开始调起第一个InboundHandler的ChannelRead事件。

1.通过ctx.fireChannelRead方法依次触发Pipeline上的下一个ChannelInboundHandler。

2.通过ctx.write传递到ChannelOutboudHander

3.Channel**In**boundHandler按照注册的先后**顺序**执行；Channel**Out**boundHandler按照注册的先后顺序**逆序**执行

4.InBoundHandler通过fire事件决定是否要执行下一个InBoundHander,如果不适用fire则后面断掉。

![img](http://static.oschina.net/uploads/space/2014/0604/160325_8JZy_251220.jpg)

![img](https://img2018.cnblogs.com/blog/154738/201910/154738-20191024212239111-37645871.png)



### 线程模型

IO线程池+业务线程池

https://www.jianshu.com/p/7504e2cbe8db

### 内存池

#### 使用内存池

在创建服务端或客户端的时候进行配置

```javascript
//Boss线程池内存池配置.
 .option(ChannelOption.ALLOCATOR,PooledByteBufAllocator.DEFAULT)

 //Work线程池内存池配置.
 .childOption(ChannelOption.ALLOCATOR,PooledByteBufAllocator.DEFAULT);

 // 在内存池中申请 直接内存
 ByteBuf directByteBuf = ByteBufAllocator.DEFAULT.directBuffer(1024);
 // 归还到内存池
 directByteBuf.release();
```

1.I/O处理线程使用内存池中的直接内存，开启以上配置

2.在handler处理业务的时候，使用内存池中的堆内存

#### 内存池结构

![这里写图片描述](https://img-blog.csdn.net/20171124195052572?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMzk2NzE3NQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)内存分级从上到下主要分为：Arena，ChunkList，Chunk，Page，SubPage五级；

**1.**PooledArena是一块连续的内存块，为了优化并发性能在**Netty内存池**中存在一个由**多个Arena组成的数组**，在多个线程进行内存分配时会按照**轮询**策略选择一个Arena进行内存分配；

**2.**一个PoolArena内存块是由**两个SubPagePools(用来存储零碎内存)**和多个**ChunkList**组成，两个SubpagePools数组分别为**tinySubpagePools**和**smallSubpagePools**。每个ChunkList里包含多个Chunk按照**双向链表**排列，每个Chunk（16MB）里包含多个Page（默认**2048个**），每个Page（默认大小为**8k**字节）由多个Subpage组成。

**3.**每个ChunkList里包含的Chunk数量会动态变化，比如当该chunk的内存利用率变化时会向其它ChunkList里移动。

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWQtaW1hZ2VzLmppYW5zaHUuaW8vdXBsb2FkX2ltYWdlcy8yMDM2MzcyLWQ1ODhlYTEwZGI1NDMyODkucG5nP2ltYWdlTW9ncjIvYXV0by1vcmllbnQvc3RyaXAlN0NpbWFnZVZpZXcyLzIvdy81NTYvZm9ybWF0L3dlYnA)

- qInit：存储内存利用率0-25%的chunk

- q000：存储内存利用率1-50%的chunk

- q025：存储内存利用率25-75%的chunk

- q050：存储内存利用率50-100%的chunk

- q075：存储内存利用率75-100%的chunk

- q100：存储内存利用率100%的chunk

  各个区间范围是交叉的，这是为了防止临界值导致Chunk不断改变ChunkList位置。

  随着chunk中page的不断分配和释放，会导致很多碎片内存段，大大增加了之后分配一段连续内存的失败率，针对这种情况，可以把内存使用率较大的chunk放到PoolChunkList链表更后面。

**chunklist分配顺序**

q050-q025-q000-qinit-q075

**为什么不从q000开始分配？**

当应用在实际运行过程中，碰到访问高峰，这时需要分配的内存是平时的好几倍，当然也需要创建好几倍的chunk，如果先从q000开始，这些在高峰期创建的chunk被回收的概率会大大降低，延缓了内存的回收进度，造成内存使用的浪费。

在q000中的chunk使用率为0，直接从chunklist中移除，释放内存。

**为什么从q050开始分配**

1、q050保存的是内存利用率50%~100%的chunk，这应该是个折中的选择！这样大部分情况下，chunk的利用率都会保持在一个较高水平，提高整个应用的内存利用率；
 2、qinit的chunk利用率低，但不会被回收；
 3、q075和q100由于内存利用率太高，导致内存分配的成功率大大降低，因此放到最后；

#### 内存分配区

线程私有分片区 和 内存池公有分配区

当内存被分配给某个线程之后，在释放内存时释放的内存不会直接返回给公有分配区，而是直接在线程私有分配区中缓存，当线程频繁的申请内存时会提高分配效率，同时当线程申请内存的动作不活跃时可能会造成内存浪费的情况，这时候内存池会对线程私有分配区中的情况进行监控，当发现线程的分配活动并不活跃时会把线程缓存的内存块释放返回给公有区。

在整个内存分配时可能会出现分配的内存过大导致内存池无法分配的情况，这时候就需要JVM堆直接分配，所以严格的讲有三层分配区。

##### 线程私有分配

线程内缓存 tiny small normall内存块

通过计算出索引之后就可以定位到线程中的内存块（MemoryRegionCache）了，MemoryRegionCache维护了一个Entry列表，每个Entry都对应一个可分配的内存单元Chunk以及一个长整形数handle。

##### 全局分配

内存池初始阶段，线程是没有内存缓存的，刚开始的内存分配是在全局分配中，也包括 tiny small normall块。

#### 内存池内存分配规则

**slab分配思路 内存划分为不同大小的内存单元，分配内存时根据使用者请求的内存大小进行计算，匹配最接近的内存单元。**

**1.**对于小于PageSize大小的内存分配，会在tinySubPagePools和smallSubPagePools中分配，**tinySubPagePools**用来分配**小于512**字节的内存，**smallSubPagePools**用来分配**大于512字节小于PageSize**的内存；

**2.**对于大于PageSize小于ChunkSize的内存分配，会在PoolChunkList中的Chunk中分配

**3.**对于大于ChunkSize的内存分配，直接由JVM堆分配，并且该Chunk不会放在内存池中重用缓存。

**4**.每个Page会被切分成大小相同的多个存储块，存储块大小由第一次申请的内存卡大小决定。第一次申请的是1K，则分成8个存储块，每个通过链表连接起来。

#### **大于pageSize内存分配**

**1.** memory 物理内存 chunkSize大小(16MB)的byte数组

**2.**memoryMap 大小4096 数组存放内存分配信息，结构上是满二叉树，每个节点元素是page,大小等于2*pageSize，总共有4096个节点，高度为12层。下标从1开始，0不适用。

**3.**depthMap 大小4096 存放节点高度信息

通过memoryMap和depthMap值比较判断分配内存。

1.memoryMap[id] = depthMap[id] ：该节点没有被分配
2.memoryMap[id] > depthMap[id] ： 至少有一个子节点被分配，不能再分配该高度满足的内存，但可以根据实际分配较小一些的内存。
3.mempryMap[id] = 最大高度 + 1（12）： 该节点及其子节点已被完全分配， 没有剩余空间。

![img](https://img2018.cnblogs.com/blog/1383365/201908/1383365-20190805111856716-1483427171.png)



```
//根据内存大小找到节点的高度 maxOrder=11 pageShifts=13
int d=maxOrder - (log2(normCapacity) - pageShifts)
```

#### tiny/small SubPagePools

用于存放小于pageSize大小的内存，Subpage数组，数组元素都是一个Subpage链表头节点，每个链表连接相同大小的Subpage。

```
private final PoolSubpage<T>[] tinySubpagePools;
private final PoolSubpage<T>[] smallSubpagePools;

PoolSubpage属性
elemSize，每次分配的内存大小。
maxNumElems，内存页最多能被分配多少次，也即一页可划分成多少大小相同的内存单元，maxNumElems=pageSize/elemSize。
numAvail，内存页还能分配多少次，它的初始值等同于maxNumElems，分配一次值递减。

nextAvail，内存页内的下一个待分配块的索引，在上面分析Normal分配的时候了解到了，当内存分配成功之后会返回一个handle整形数，通过这个整数型来计算偏移量来确定最终的物理内存，而页内分配除了返回handle整数计算在chunk内的偏移量，同时还需要返回一个bitmapIdx来计算页内的偏移量，通过这两个偏移量来确定最终的物理内存，这个bitmapIdx的值等同于nextAvail。

bitmap=new long[pageSize/page分块数/64];//64表示long所占的bit数
通过bitmap数组元素的二进制来记录Page的使用状态，每个二进制位代表Page内的一个内存单元，当二进制位为1表示对应的内存块被分配过，第一个元素对应0-63号内存单元，第二个元素对应64-127号内存单元。
bitmap[0]=0b0000000...0001111表示0,1,2,3这四个内存单元都已经被分配给对应的请求了。这个bitmap用来辅助计算下一个分配块的索引。
```

![netty](https://img-blog.csdnimg.cn/20190114163222678.png)

#### 内存释放

在线程申请到内存使用完成之后归还内存时优先把内存块缓存到线程中，除非该内存块不适合缓存在线程中（内存太大），当当前线程内存分配动作非常活跃时，这样会明显的提高分配效率，但是当它不活跃时对内存又是极大的浪费，所以内存池会监控该线程，随时做好把内存从线程缓存中删除的准备。

当缓存中空闲的内存块数量超过总Entry数的一半时说明线程的内存分配动作不活跃，释放所有Entry对应的chunk。

当内存分配的次数达到一定次数时就会进行一次活跃度检查并释放不活跃线程缓存中空闲的内存。



Netty中使用引用计数机制来管理资源，ByteBuf实现了ReferenceCounted接口，当实例化ByteBuf对象时，引用计数加1。

当应用代码保持一个对象引用时，会调用retain方法将计数增加1，对象使用完毕进行释放，调用release将计数器减1.

当引用计数变为0时，对象将释放所有的资源，返回内存池。

**没有成对使用ByteBuf的 retain和 release方法导致内存泄漏?**

ByteBuf引用WeakReference,弱引用不影响目标对象的垃圾回收，但是会在目标对象被JVM垃圾回收时加入到引用队列ResourceLeak，当ResourceLeak中存在元素，即表明存在内存泄漏,对内存进行跟踪。

#### 本地线程存储

虽然提供了多个PoolArena减少线程间的竞争，但是难免还是会存在锁竞争，所以需要利用**ThreaLocal**进一步优化，把**已申请的内存放入到ThreaLocal**自然就没有竞争了。

大体思路是在ThreadLocal里面放一个PoolThreadCache对象，然后释放的内存都放入到PoolThreadCache里面，下次申请先从PoolThreadCache获取。

但是，如果thread1申请了一块内存，然后传到thread2在线程释放，这个Netty在内存holder对象里面会引用PoolThreadCache，所以还是会释放到thread1里。


#### Recycler对象池

用于缓存对象从而避免大量创建同一个类型的对象PooledByteBuf

Recycler对象池与Threadlocal结合，每个线程都有一个对象池。

每个线程维护 WeakMap<Stack,WeakOrderQueue> stack线程唯一，WeakOrderQueue多线程共享

如果 queue 所在的线程被回收了，就将这个线程对应的 queue 中的所有数据全部转移到 stack

get()：获取一个实例

recycle(T, Handle<T>)：回收一个实例（放入对象池）

newObject(Handle<T>)：创建一个实例

**对象回收**

当对象从其它线程对象池中取出来后，回收也要返回给该线程。



![这里写图片描述](https://img-blog.csdn.net/20171124194642988?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMzk2NzE3NQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

![这里写图片描述](https://img-blog.csdn.net/20171124194628835?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMzk2NzE3NQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

1. ThreadLocal中的Stack相当于是一级缓存，同一个线程内的使用和回收都将使用一个Stack
2. 每个线程都会有一个自己对应的Stack，如果回收的线程不是Stack的线程，将元素放入到Queue中
3. 所有的Queue组合成一个链表，Stack可以从这些链表中回收元素（实现了多线程之间共享回收的实例）

#### 总结

堆外内存释放依赖于GC，而且容易造成OOM。

内存池主要是将内存分配管理起来不经过JVM的内存分配（堆外内存），有效减小内存碎片避免内存浪费，同时也能减少频繁GC带来的性能影响；

内存池内存分配入口是PoolByteBufAllocator类，该类最终将内存分配委托给PoolArena进行；为了减少高并发下多线程内存分配碰撞带来的性能影响，PoolByteBufAllocator维护着一个PoolArena数组，**线程通过轮询获取其中一个进行内存分配**，进而实现锁分离；

内存分配的基本单元是PoolChunk，从PoolArena中分配获取一个PoolChunk，一个PoolChunk包含多个Page内存页，通过完全二叉树维护多个内存页用于内存分配；
