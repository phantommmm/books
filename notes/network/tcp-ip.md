## TCP首部格式

<img src="https://uploadfiles.nowcoder.com/images/20190726/7237888_1564127868082_6DF1E20277DF6F79D98752D08402CEF7" alt="img" style="zoom:50%;" />

三次握手过程中的报文是不含数据的，只包含tcp协议首部。

**序号：**  seq

 占4字节。序号范围是 0 到 2的32次方-1 ，序号增加到 2的32次方-1 后，下一个序列号就回到了1。也就是说，序号使用mod 2的32次方 运算。TCP是面向字节流的，TCP连接中传送的字节流中的每一个字节都按顺序编号。首部中的序号就是指本报文段所发送数据的第一个字节的序号。  

 **确认号：**  ack

  占4个字节，是期望收到对方下一个报文段的一个数据字节的序号。  

  **确认ACK：** 占一位  当携带数据时占一个序列号seq 4个字节 （第三次握手）

  当ACK=1时，确认号有效，当ACK=0时，确认号无效。当建立连接后所有传送的报文段都必须把ACK置1。 （区分开确认号和ACK）  

  **同步SYN：**  占一个序列号seq 4个字节 不能携带数据

  当SYN=1，ACK=0时，表明这是一个连接请求报文段。 （ 第一次握手）

  当SYN=1,   ACK=1时，表明这是一个连接响应报文段。    （第二次握手）

 握手完成后 SYN=0；

  **终止FIN：**  占一个序列号seq 4个字节 不能携带数据

  当FIN=1时。表明此报文的发送方的数据已经发送完毕，并要求释放运输连接。  

**初始序列号seq为什么随机？**

TCP在开始传输数据前，客户端和服务器需要随机生成自己的初始序列号（ISN），然后通过三次握手进行交换确认。

1. 防止接受网络上粘滞的TCP包，如果都从0开始的话，极其容易接受之前断开连接发送的**粘滞包**。虽然可以采用每次TCP会话都使用一个UUID作为标记，但是考虑到每次都要携带UUID，比较浪费流量，所以就采用随机序列号的方法。
2. 防止Hack猜测序列号，然后伪装TCP报文，当然这种防御其实很弱。

 **为什么SYN FIN占一个序列号？**

由于报文在网络中路径不同，可能会导致报文的乱序，SYN带sn号，对方就知道你要发送的数据第一个字节的sn号了，这样对方就能根据sn号排序啦；
FIN也会占一个sn号，当你收到FIN时，就可以根据这个sn号和你之前收到的数据进行对比，是不是还有数据没收到啊；
sn号只是对方用来对数据进行重组排序，确认数据有没有丢失，因此空的ack当然不用占一个sn了；

**什么时候完成连接?**

客户端在第三次握手，发送完报文后，即进入established（已建立连接）状态

服务器在收到报文后，才进入established（已建立连接）状态

**ACK可以携带数据？**

TCP标准规定，第三次握手的报文，可以携带数据。因为此时客户端已经处于established状态了呀。

三次握手最后一个消息是客户端发过来的ACK，如果让应用层数据与这个信令数据合二为一，可以减少发送的IP包的数目，还可以提高效率，何乐不为呢？

假设第三次握手客户端发送报文的seq是x+1，

**如果有携带数据**，下次客户端发送的报文，seq=服务器发回的ACK号。

**如果没有携带数据**，那么第三次握手的报文不消耗seq。下次客户端发送的报文，seq序列号还是和第三次握手的报文的seq一样，为x+1。这是因为，seq和报文中的数据在整条数据流流中的位置是一一对应的。如果报文没有携带数据，那么seq当然也不会更新。



**什么时候完成断开？**

服务器在收到客户端最后一次报文时，断开连接

客户端在发送完最后一次报文时，等待2msl后，都断开连接

**超时重传机制**

首先客户端发送一个数据报，序号为1，数据长度是1000。这里有开启一个**超时计时器**。  

  那么服务器接收到后，会发送一个确认报文，确认号为1000+1。表示你下一个该发送的是1001。  

 那么如何处理发送丢包呢以及确认丢包呢 ？

 当客户端的**超时计时器**到期了，还没有收到服务器的确认，无论是发送的包丢了，还是确认的包丢了，都会重新发送数据报。

#### 滑动窗口

**TCP按顺序发送一个包接受一个包，怎么提高吞吐量？**

**滑动窗口，同时发送多个包**

<img src="https://img2018.cnblogs.com/blog/1629488/201906/1629488-20190622120313249-1589098511.png" alt="实现" style="zoom:80%;" />

发送方的发送缓存内的数据都可以被分为4类:         接收方的缓存数据分为3类：
1. 已发送，已收到ACK 											1.已接收
2. 已发送，未收到ACK                                             2.未接收但准备接收
3. 未发送，但允许发送                                            3.未接收而且不准备接收
4. 未发送，但不允许发送

其中类型2和3都属于发送窗口。                               其中类型2属于接收窗口。

**原理：**

并不是一个报文段就回复一个ACK,可能对多个报文段回复一个ACK（累计ACK）

比如说发送方有1/2/3    3个报文段，先发送了2,3 两个报文段，但是接收方期望收到1报文段，这个时候2,3报文段就只能放在缓存中等待报文1的空洞被填上，如果报文1，一直不来，报文2/3也将被丢弃，如果报文1来了，那么会发送一个ACK对这3个报文进行一次确认。

TCP双方都维护一个 **接受窗口**和**发送窗口**

各自的`接收窗口`大小取决于应用、系统、硬件的限制（TCP传输速率不能大于应用的数据处理速率）。各自的`发送窗口`则要求取决于对端通告的`接收窗口`，要求相同。

1.接收窗口只有在**前面的段**确认收到的情况下，窗口右移。

2.发送窗口只有收到ACK后，窗口才右移。

3.接收端将自己的接收缓冲区的大小放入TCP首部的窗口大小字段，通过ACK通知发送端，发送端根据接收端响应回的窗口大小决定发多大的数据

4.窗口大小字段越大，说明网络的吞吐量越高

5.接收端一旦发现自己的接收缓冲区快满了，就会将窗口大小设置成一个更小的值通知给发送端，发送端收到这个窗口大小之后，就会减慢自己的速度

6.如果接收端缓冲区满了，就会把窗口置为0，这时发送方不再发送数据，但是需要定期发送一个窗口探测数据段，使接收端把窗口大小告诉发送端。（避免死锁）

TCP为每个连接设有一个持续计时器。只要TCP连接的一方收到对方的零窗口通知，就启动持续计时器，若持续计时器设置的时间到期，就发送一个零窗口探测报文段（仅携带1字节的数据），而对方就在确认这个探测报文段时给出了现在的窗口值。

```
1. 假设32~45 这些数据，是上层Application发送给TCP的，TCP将其分成四个Segment来发往internet

2. seg1 32~34 seg2 35~36 seg3 37~41 seg4 42~45  这四个片段，依次发送出去，此时假设接收端之接收到了seg1 seg2 seg4

3. 此时接收端的行为是回复一个ACK包说明已经接收到了32~36的数据，并将seg4进行缓存（保证顺序，产生一个保存seg3 的hole）

4. 发送端收到ACK之后，就会将32~36的数据包从发送并没有确认切到发送已经确认，提出窗口，这个时候窗口向右移动

5. 假设接收端通告的Window Size仍然不变，此时窗口右移，产生一些新的空位，这些是接收端允许发送的范畴

6. 对于丢失的seg3，如果超过一定时间，TCP就会重新传送（重传机制），重传成功会seg3 seg4一块被确认，不成功，seg4也将被丢弃

```

接收端可以根据自己的状况通告窗口大小，从而控制发送端的接收，进行流量控制

TCP是发送报文段为单位的，假如每发一个报文就要等ACK，那么对于大数据包，等待时间就太长了。只要发送的报文在滑动窗口里面，不用等每个ACK回来就可以向右滑动。

窗口大小不能大于序号空间大小的一半。目的是为了不让两个窗口出现交迭，比如总大小为7，窗口大小都为4，接收窗口应当滑动4，但只剩3个序号，导致两个窗口交迭。

#### 对比滑动窗口和拥塞窗口

滑动窗口是控制接收以及同步数据范围的，通知发送端目前接收的数据范围，用于流量控制，接收端使用，不仅仅是对速率这个属性。还控制了流向，控制了服务对象等其他属性。

拥塞窗口是控制发送速率的，避免发的过多，发送端使用。因为tcp是全双工，所以两边都有滑动窗口。TCP适用而UDP不太适应，因为无法很好的控制UDP的发送速率。

两个窗口的维护是独立的，滑动窗口主要由接收方反馈缓存情况来维护，拥塞窗口主要由发送方的拥塞控制算法检测出的网络拥塞程度来决定的。

**流量控制控制端到端的速率，而拥塞控制控制全局网络的速率，保证传输的可靠性。**

1.宽带速率1Gb/s，网络只有两台机器，从一台主机传送数据到另一台，这需要流量控制，以保证接收方能正常接收数据。
2.宽带速率1Gb/s，网络中有成千上万台机器，几万台主机发送到另外几万台，这需要拥塞控制，不然网络会瘫痪。

流量控制是指，友人A的讲话速率过快，你无法听清，请求他说慢一些；

拥塞控制是指，友人A、B、C同时讲话，你的大脑无法处理这么多信息，于是你要求A先说，然后B说，然后C说。

![img](https://pic4.zhimg.com/80/v2-97d22b909f5a068764557cce0e8cebc1_1440w.jpg?source=1940ef5c)

### 流量控制 

 **为什么要进行流量控制，是为了让发送方的发送速率不要太快，让接收方来得及接收，减少丢失。**
 接收端处理数据的速度是有限的，如果发送方的速度太快，就会把缓冲区打满。这个时候如果继续发送数据，就会导致丢包等一系列连锁反应。

TCP首部中, 有一个16位窗口字段存放了窗口大小信息;

那么问题来了, 16位数字最大表示65535, 那么TCP窗口最大就是65535字节么?

实际上, TCP首部40字节选项中还包含了一个窗口扩大因子M,
实际窗口大小是 窗口字段的值左移 M 位;

接收端将自己可以接收的缓冲区大小放入 TCP 首部中的 “窗口大小” 字段, 通过ACK端通知发送端;窗口大小字段越大, 说明网络的吞吐量越高;

<img src="https://img-blog.csdn.net/20180605190737615?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3pnZWdl/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70" alt="这里写图片描述" style="zoom: 67%;" />

  这里使用滑动窗口机制，例如在建立连接时，B告诉A接收窗口rwnd=400，那么发送方的窗口不能超过接收方给出的接收窗口数值。 

  **若一开始就发送大量数据？**

#### **拥塞控制 Reno算法**

拥塞窗口：可以同时发送的数量

拥塞窗口发送开始的时候, 定义拥塞窗口大小为1;每次收到一个ACK应答, 拥塞窗口加1;

例如：刚开始A 只能同时发送一个数据

​			接受到一个ACK后，拥塞窗口加1，可以同时发送两个数据

​			接受到两个ACK后，拥塞窗口加2，可以同时发送四个数据

<img src="https://img-blog.csdn.net/20180606160356899?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3pnZWdl/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70" alt="这里写图片描述" style="zoom:67%;" />

**慢启动**

以2-4个开始，以指数增长，阀值为 2^16=65535

**拥塞避免&阀值**

不能单纯的让拥塞窗口指数增长，当拥塞窗口超过这个阈值的时候,进入拥塞避免， 不再按照指数方式增长, 而是按照线性方式增长，即无论收到多少个ACK，拥塞窗口都只加1

**网络拥塞**

当发生网络拥塞时，让慢启动门限设置为当前拥塞窗口一半，并把拥塞窗口设为1，重新进入慢启动。

少量的丢包, 我们仅仅是触发超时重传; 大量的丢包, 我们就认为网络拥塞;

发送端判断网络发生拥塞的依据是：发送端设置一个重传计时器RTO，对于某个已发出的数据报文段，如果在RTO计时到期后，还没有收到来自接收端的确认，则认为此时网络发生了拥塞。

在TCP中，**丢包**被用作判断拥塞发生与否的指标，用来衡量是否应该实施相应的响应措施来避免或至少减缓拥塞。其他拥塞探测方法，如**时延测量**和显式拥塞通知（ECN）会使TCP能在丢包发生前检测拥塞。

**快重传**

发送端连续收到3个相同的ACK，则会进行快重传，不必等到重传计时器到期。

**快重传后会进入慢启动吗**

不会，在这种情况下没有执行慢启动的原因是由于收到重复的ACK不仅仅告诉我们一个分组丢失了。由于接收方只有在收到另一个报文段时才会产生重复的ACK，而该报文段已经离开了网络并进入了接收方的缓存。也就是说，在收发两端之间仍然有流动的数据，而我们不想执行慢启动来突然减少数据流。

**快恢复**

发送端连续收到3个相同的ACK，会进行以下操作

1.把慢开始门限减半

2.把拥塞窗口再设置为门限的值(具体实现有些为ssthresh+3)

3.重新进入拥塞避免阶段，每次收到ACK+1

为什么是3个？因为1-2个重复ACK，很有可能是乱序，只有在3个及以上的时候才是有可能丢包了。

例子：

B等待A发送首字节序号为3的报文段，给A发送“ack = 3”的确认报文段（ACK），而A在发送过程中出现了丢失，B收到的只有4、5字节的报文段；
此时B给A发送的确认报文中ack字段仍然等于3（因为字节3还没收到）；
接着，在B收到字节6后，给A发送的确认报文仍然是“ack = 3”



<img src="https://img-blog.csdn.net/20180730185435947?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM4NjIzNjIz/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70" alt="img" style="zoom:80%;" />



###


#### RST标志位：reset复位

RST用于异常的关闭连接，发送RST包关闭连接时，不必等待缓冲区的包都发出去（例如FIN），直接丢弃缓存区的包发送RST包，而接收端收到RST包后，也不必发送ACK包确认，直接关闭连接资源即可。（然后可以重新发起连接）

#### RST包没有顺利到达对端？

导致双方状态不同步，一方为 "established(已建立连接)"，一方为 "closed"。已建立连接方仍然能往closed方发送数据，就会触发closed方发送RST。

**若已建立连接方，一直不发送消息，那么连接一直处于泄露状态？**

理论上是这样的，但如今的TCP会在几十秒内发送一次KeepAlive(心跳检测)，便解决这个问题

#### RST攻击

A和服务器B之间建立了TCP连接，此时C伪造了一个TCP包发给B，使B异常的断开了与A之间的TCP连接，就是RST攻击了。

**那么伪造什么样的TCP包可以达成目的呢？**

（1）假定C伪装成A发过去的包，这个包如果是RST包的话，毫无疑问，B将会丢弃与A的缓冲区上所有数据，强制关掉连接。
（2）假定C伪装成A发过去的包，这个包如果是SYN包的话，那么，B会表示A已经发疯了(AB已经正常连接时，A又来请求建立新连接) ==> B会主动向A发个RST包，并在自己这端强制关掉连接。

**如何能伪造成A发给B的包呢？这里有两个关键因素，源端口和序列号。**

（1）一个TCP连接都是四元组，由源IP、源端口、目标IP、目标端口唯一确定一个连接。

所以，如果C要伪造A发给B的包，要在上面提到的IP头和TCP头，把源IP、源端口、目标IP、目标端口都填对。这里B作为服务器，IP和端口是公开的，A是我们要下手的目标，IP当然知道，但A的源端口就不清楚了，因为这可能是A随机生成的。

当然，如果能够对常见的OS如windows和linux找出生成source port规律的话，还是可以搞定的。

（2）序列号问题是与滑动窗口对应的，伪造的TCP包里需要填序列号，如果序列号的值不在A之前向B发送时B的滑动窗口内，B是会主动丢弃的。所以我们要找到能落到当时的AB间滑动窗口的序列号。

这个可以暴力解决，因为一个sequence（序列号）长度是32位，取值范围0-4294967296，如果窗口大小像上图中我抓到的windows下的65535的话，只需要相除，就知道最多只需要发65537（4294967296/65535=65537）个包就能有一个序列号落到滑动窗口内。RST包是很小的，IP头＋TCP头也才40字节，算算我们的带宽就知道这实在只需要几秒钟就能搞定。


#### 什么情况下，会收到对端的RST包？

1. 客户端`connect`一个不存在的端口，客户端会收到一条`RST`，报错`Connection refused`；

2. 程序崩溃或异常退出，会向对端发送。

3. 对端断电重启（连接断开），本端（不知道连接已经断开）`send`数据时会收到来自对端的`RST`。因为对端连接已经断开，通过RST告知异常断开情况。

4. 某些服务器对连接的IP有限制，对禁止连接的IP，会发送RST包禁止连接

5. 客户端发起TCP连接，经过服务器防火墙时，防火墙查询自己的安全策略，这是一个不被允许的连接请求，于是防火墙以服务器IP的名义，返还给用户一个Reset状态位，用户以为是服务器发的，其实服务器压根不知道，是防火墙作为中间人发的。

   

### TCP连接异常断开

如果客户端异常断开而服务器未及时清理这些连接，则发送连接泄露直至服务端资源耗尽拒绝提供服务（`connection refused exception`）

#### 客户端程序崩溃或异常退出

当客户端程序因未知原因崩溃或异常退出后，操作系统会给服务端发送一条`RST`消息

#### 客户端断电或网络异常：无法即时发送RST

如果客户端断电或网络异常，并且连接通道内没有任何数据交互，服务端是感知不到客户端掉线的，此时需要借助心跳机制来感知这种状况，客户端收到不明的消息包，返回RST告知连接状态

#### keep alive VS heart beart

使用场景：客户端拔网线，服务器不知道。

原理相似：都是发送一个信号给对方，如果多次发送都没有响应的话，则判断连接中断。

它们的不同点在于，**keepalive是tcp实现中内置的机制，是在创建tcp连接时通过设置参数启动keepalive机制**；而**heart-beat则需要在tcp之上的应用层实现（程序员自己实现逻辑）。**

一个简单的heart-beat实现一般测试连接是否中断采用的时间间隔都比较短，可以很快的决定连接是否中断。并且，由于是在应用层实现，因为可以自行决定当判断连接中断后应该采取的行为，而keepalive在判断连接失败后只会将连接丢弃。

Keep alive适用于清除死亡时间比较长的连接,它会先要求此连接一定时间没有活动（一般是几个小时），然后发出数据段，经过多次尝试后（每次尝试之间也有时间间隔），如果仍没有响应，则判断连接中断。可想而知，整个周期需要很长的时间。

heart beart适用于快速或者实时监控连接状态的机制，分布式环境中。

关于heart-beat，应该在传输真正数据的连接中发送“心跳”信号，还是可以专门创建一个发送“心跳”信号的连接呢？

比如说，A，B两台机器之间通过连接m来传输数据，现在为了能够检测A，B之间的连接状态，我们是应该在连接m中传输“心跳”信号，还是创建新的连接n来专门传输“心跳”呢？

我个人认为两者皆可。如果担心的是端到端的连接状态，那么就直接在该条连接中实现“心跳”。但很多时候，关注的是网络状况和两台主机间的连接状态，这种情况下， 创建专门的“心跳”连接也未尝不可。

### UDP

无连接协议，每个数据报都是一个独立的信息，包括完整的源地址或目的地址，在网络上以任何可能的路径传往目的地，因此能否到达目的地，到达目的地的时间以及内容的正确性都是不能被保证的。



### TCP UDP区别

**udp**

​    1、每个数据报中都给出了完整的地址信息，因此无需要建立发送方和接收方的连接。

​    2、UDP传输数据时是有大小限制的，每个被传输的数据报必须限定在64KB之内。

​    3、UDP是一个不可靠的协议，发送方所发送的数据报并不一定以相同的次序到达接收方

**tcp**

1、面向连接的协议，在socket之间进行数据传输之前必然要建立连接，所以在TCP中需要连接时间。

2、TCP传输数据没有大小限制，一旦连接建立起来，双方的socket就可以按统一的格式传输大的数据。

3、TCP是一个可靠的协议，它确保接收方完全正确地获取发送方所发送的全部数据。



### 设计可靠的UDP

传输层无法保证数据的可靠传输，只能通过应用层来实现了。实现的方式可以参照tcp可靠性传输的方式，只是实现不在传输层，实现转移到了应用层。

**应用层确认机制、重传机制、滑动窗口**

**给数据包进行编号，按顺序接收并存储**。

接收端收到数据包后发送确认信息给发送端，发送端接收到确认信息后继续发送，若接收端接收的数据不是期望的顺序编号，则要求重发；（主要解决丢包和包无序的问题）

### TCP怎么可靠、安全

**1.序列号 确认号 数据包分片**

**2.超时重传 快速重传**

**3.流量控制（滑动窗口）**

**4.拥塞控制（拥塞窗口）**

### TCP连接可能出现的问题

[彻底弄懂TCP协议：从三次握手说起 (qq.com)](https://mp.weixin.qq.com/s/6LiZGMt2KRiIoMaLwx-lkQ)

#### 第三次握手失败了怎么办？

当client与server的第三次握手失败了之后，即client发送至server的确认建立连接报文段未能到达server，server在等待client回复ACK的过程中超时了，那么server会向client发送一个RTS报文段并进入关闭状态，即：并不等待client第三次握手的ACK包重传，直接关闭连接请求，这主要是为了防止泛洪攻击，即坏人伪造许多IP向server发送连接请求，从而将server的未连接队列塞满，浪费server的资源。

#### 三次握手有哪些漏洞（可能会收到什么攻击）？

**SYN洪水攻击** 客户端在第三次握手阶段不完成，即不发送确认连接信息给server。

server无法完成连接且不会马上放弃，server会不停重试并且等待一定时间后放弃该连接，这段时间为 SYN timeout。

黑客仿造IP大量的向server发送TCP连接请求报文包，从而将server的半连接队列（上文所说的未连接队列，即server收到连接请求SYN之后将client加入半连接队列中）占满，从而使得server拒绝其他正常的连接请求。即 **拒绝服务攻击**

#### **怎么防范这种攻击？**

**1.** 缩短服务器接受客户端SYN后的等待连接时间，即**SYN timeout**时间，也就是server接收到SYN报文段，到最后放弃此连接请求的超时时间，将SYN timeout设置的更低，便可以成倍的减少server的负荷，但是过低的SYN timeout可能会影响正常的TCP连接的建立，一旦网络不通畅便可能导致client连接请求失败
**2.**SYN cookie + SYN proxy

	a. SYN cookie:当server接收到client的SYN之后，不立即分配资源，而是根据client发送过来的SYN包计算出一个cookie值，这个cookie值用来存储server返回给client的SYN+ACK数据包中的初始序列号，当client返回第三次握手的ACK包之后进行校验，如果校验成功则server分配资源，建立连接。
	b. SYN proxy代理，作为server与client连接的代理，代替server与client建立三次握手的连接，同时SYN proxy与client建立好了三次握手连接之后，确保是正常的TCP连接，而不是TCP泛洪攻击，那么SYN proxy就与server建立三次握手连接，作为代理（网关？）来连通client与server。（类似VPN了解一下。）

### TCP实现HTTP？

使用Socket定义一个连接，通过TCP发送特定格式的数据。

设置想要发送的连接的 **IP**和**端口号**，发送数据为get请求

```
char sendData[] = "GET / HTTP/1.1\r\nHOST:www.baidu.com\r\n\r\n";
```



### time_wait

**大量time_wait的危害**

只有主动进行关闭连接的机器会进入time_wait状态。

在**高并发短连接**的TCP服务器上，当客户端完成访问后立刻主动正常关闭连接，就会出现大量time_wait。

这时该客户端在访问同一服务器，会启用另一个端口，因为刚刚那个端口处于time_wait状态。

这时，如果客户端并发量持续很高，就会部分客户端连接不上。

**1.高并发可以让客户端在短时间范围内同时占用大量端口**，而端口有个0~65535的范围，并不是很多，刨除系统和其他服务要用的，剩下的就更少了。

**2.短连接表示“业务处理+传输数据的时间 远远小于 TIMEWAIT超时的时间”的连接**。

比如取一个web页面，1秒钟的http短连接处理完业务，在关闭连接之后，这个业务用过的端口会停留在TIMEWAIT状态几分钟，而这几分钟，其他HTTP请求来临的时候是无法占用此端口的(占着茅坑不拉翔)。

**解决**

```
调整 time_wait时间 net.ipv4.tcp_fin_timeout=30

//首先打开 timestamps
net.ipv4.tcp_timestamps = 1
//机器作为客户端时起作用，开启后time_wait在一秒内回收
net.ipv4.tcp_tw_reuse = 1
（不要开启，现在互联网NAT结构很多，可能直接无法三次握手）
net.ipv4.tcp_tw_recycle = 0 

reuse:复用time_wait连接。
recycle:快速回收。
```

**timestamps**

开启timestamps时间戳，实现打开同一个IP 不同端口，同一源IP的socket connect请求中的 timestamps必须是递增的。

**reuse**

这是因为一来TIME_WAIT创建时间必须超过一秒才可能会被复用；二来只有连接的时间戳是递增的时候才会被复用。

满足以下任意一点即可复用

**1.初始序列号比TW老连接的末序列号大**
**2.如果使能了时间戳，那么新到来的连接的时间戳比老连接的时间戳大**

**recycle**

**先发送的数据包 后到达 导致被丢弃**

Linux实现了一个TIME_WAIT状态快速回收的机制，即无需等待两倍的MSL这么久的时间，而是等待一个Retrans时间即释放，也就是等待一个重传时间(一般超级短，以至于你都来不及能在netstat -ant中看到TIME_WAIT状态)随即释放。

```
假设PC1和PC2均启用了TCP时间戳，它们经过NAT设备N1往服务器S1的22端口连接：
PC1：192.168.100.1
PC2：192.168.100.2
N1外网口(即NAT后的地址)：172.16.100.1
S1：172.16.100.2
所有涉事机器的配置：
net.ipv4.tcp_tw_recycle = 1
net.ipv4.tcp_tw_reuse = 1
net.ipv4.tcp_timestamps = 1
由于有NAT设备，S1看来是同一台机器发出的，且出现了时间戳倒流，连接拒绝！
仅仅两台机器就出现了这个问题，试问如果大量的源端机器在服务器的入口处遇到了NAT设备会怎样？即一台三层NAT设备部署在高负载网站的入口处...没有谁能保证时间戳小的机器一定先发起连接，各个机器频繁连接断开后依然按照时间戳从小到大的顺序连接！！
```

经过nat之后，如果前面相同的端口被使用过，且时间戳大于这个链接发出的syn中的时间戳，服务器上就会忽略掉这个syn，不返会syn-ack消息，表现为用户无法正常完成tcp3次握手，从而不能打开web页面。在业务闲时，如果用户nat的端口没有被使用过时，就可以正常打开；业务忙时，nat端口重复使用的频率高，很难分到没有被使用的端口，从而产生这种问题。

NAT设备为所有的内部设备代理一个IP地址即主机标识，然而**却不触动其时间戳**，而各个机器的时间戳并不满足任何规律...

### close_wait

**服务器保持了大量的close_wait**

当客户端发送一个FIN后，服务器没有返回ACK确认。即程序没有检测到需要关闭连接。这个资源就一直被程序占着。

**产生大量Too many open files原因**

在服务器与客户端通信过程中，因服务器发生了socket未关导致的closed_wait发生，致使监听port打开的句柄数到了1024个，且均处于close_wait的状态，最终造成配置的port被占满出现“Too many open files”，无法再进行通信。 

**措施**

原因是因为调用ServerSocket类的accept()方法和Socket输入流的read()方法时会引起线程阻塞，所以应该用setSoTimeout()方法设置超时。 



**解决**

（1）关闭正在运行的程序，这个需要视业务情况而定。
（2）尽快的修改程序里的bug，然后测试提交到线上服务器。

**HttpClient未释放连接，会造成close_wait**

服务器A是一台爬虫服务器，它使用简单的HttpClient去请求资源服务器B上面的apache获取文件资源，正常情况下，如果请求成功，那么在抓取完资源后，服务器A会主动发出关闭连接的请求，这个时候就是主动关闭连接，服务器A的连接状态我们可以看到是TIME_WAIT。

如果一旦发生异常呢？假设请求的资源服务器B上并不存在，那么这个时候就会由服务器B发出关闭连接的请求，服务器A就是被动的关闭了连接，如果服务器A被动关闭连接之后程序员忘了让HttpClient释放连接，那就会造成CLOSE_WAIT的状态了。

### NAT

**介绍：**在通信中将内网的ip和端口映射为外围的ip和端口。

NAT是将私有IP地址通过边界路由转换成外网IP地址，在边界路由的**NAT地址转换表**记录下这个转换映射记录，当外部数据返回时，路由使用NAT技术查询NAT转换表，再将目标地址替换成内网用户IP地址。

**出现原因：ip地址不够用**

**三种NAT技术**

**静态NAT：**静态NAT就是一对一映射，内部有多少私有地址需要和外部通信，就要配置多少外网IP地址与其对应

**动态NAT：**动态NAT是在路由器上配置一个外网IP地址池，当内部有计算机需要和外部通信时，就从地址池里动态的取出一个外网IP，并将他们的对应关系绑定到NAT表中，通信结束后，这个外网IP才被释放，可供其他内部IP地址转换使用，这个DHCP租约IP有相似之处。

**PAT(port address Translation，端口地址转换，也叫端口地址复用)：**这是最常用的NAT技术，也是IPv4能够维持到今天的最重要的原因之一，它提供了一种多对一的方式，对多个内网IP地址，边界路由可以给他们分配**一个外网IP**，利用这个外网IP的不同端口和外部进行通信。

**优点：**节约了公网ip，并且隐藏和保护了内网

**缺点：**使网络报文被修改，增加了网络的转发时延。

### 创建Socket

**server**

```
//创建serversocket并监听指定端口
ServerSocket server=new ServerSocket(port);
//使用accept监听客户端连接
Socket socket=server.accpet();

//获取输入流，读取客户端信息，将字节流转化为字符流，保存在缓冲区
InputStream input=new InputStream(socket.getInputStream())
socket.shutdownInput();

socket.close();
```

**client**

```
//创建socket指定 ip port
Socket socket=new Socket(ip,port);

socket.close();

```

