
# 高可用技术
    持久化：数据的单机备份问题（内存->硬盘）
    主从复制：解决数据的多机备份，负载均衡，故障恢复（非自动）
    哨兵：基于主从复制，解决主节点故障恢复的自动化问题
    集群：数据分区，主节点故障自动转移（与哨兵相似），在主从模式下，主节点支持读和写，子节点只支持读                       

## 面试题
#### Redis为什么快？

    1、完全基于内存，绝大部分请求是纯粹的内存操作，读写数据的时候都不会受到硬盘 I/O 速度的限制，所以速度极快。
       数据存在内存中，类似于HashMap，HashMap的优势就是查找和操作的时间复杂度都是O(1)；
    
    2、数据结构简单，对数据操作也简单，Redis中的数据结构是专门进行设计的；
    
    3、采用单线程，避免了不必要的上下文切换和竞争条件，也不存在多进程或者多线程导致的切换而消耗 CPU，不用去考虑各种锁的问题，不存在加锁释放锁操作，没有因为可能出现死锁而导致的性能消耗；
    
    4、使用多路I/O复用模型，非阻塞IO；
    
    5、使用底层模型不同，它们之间底层实现方式以及与客户端之间通信的应用协议不一样，Redis直接自己构建了VM 机制 ，因为一般的系统调用系统函数的话，会浪费一定的时间去移动和请求；
#### Redis为什么是单线程?
一次完整的redis请求事件有多个阶段：（客户端到服务器的网络连接-->redis读写事件发生-->redis服务端的数据处理（单线程）-->数据返回）

Redis单线程是指在处理我们的网络请求的时候只有一个线程来处理，即服务端的数据处理阶段，不牵扯网络连接    
##### 1.客户端到服务端的连接
    客户端和服务器是socket通信方式，socket服务端监听可同时接受多个客户端请求   
    redis的客户端与服务器端通信是基于TCP连接 
##### 2.redis读写事件发生并向服务端发送请求数据
    假设是set（写）事件，此时redis客户端开始向建立的网络流中送数据，服务端可以理解为给每一个网络连接创建一个线程同时接收客户端的请求数据。    
##### 3.redis服务端的数据处理
    服务端完成了第二阶段的数据接收，接下来开始依据接收到的数据做逻辑处理，然后得到处理后的数据。
    数据处理可以理解为一次方法调用，带参调用方法，最终得到方法返回值。不要想复杂，重在理解流程。
##### 4.数据返回
    当reids服务端数据处理完后 就会立即返回处理后的数据 
#### 单线程运行方式 
    客户端与服务端建立连接交由socket，可以同时建立多个连接（这里应该是多线程/多进程），建立的连接redis是知道的（为什么知道，去看socket编程），
        然后redis会基于这些建立的连接去探测哪个连接已经接收完了客户端的请求数据（注意：不是探测哪个连接建立好了，而是探测哪个接收完了请求数据），
            而且这里的探测动作就是单线程的开始，一旦探测到则基于接收到的数据开始数据处理阶段，然后返回数据，再继续探测下一个已经接收完请求数据的网络连接。
                注意，从探测到数据处理再到数据返回，全程单线程。这应该就是所谓的redis单线程。

#### 原因     
     1.redis本身就是基于内存操作的，所以每个操作执行速度都很快。
     如果使用多线程，就需要解决多线程同步的问题，就会涉及到线程的频繁切换而消耗CPU。
     单线程的使用避免了不必要的上下文切换和竞争条件，不用去考虑各种锁的问题，不存在加锁释放锁操作，没有因为可能出现死锁而导致的性能消耗。
     
     2.redis中的数据结构比较简单，对数据的操作也就比较快。没必要多线程。
     
     3.使用多路复用IO，即非阻塞IO。
     这样提高了redis的吞吐量。多路是指可以处理多个网络连接产生的流，复用是指一个线程可以服务多条IO流。     
#### I/O多路复用模型-- select poll epoll

**单个线程通过记录跟踪每一个Sock(I/O流)的状态(对应空管塔里面的Fight progress strip槽)来同时管理多个I/O流**. 主要是为了尽量多的提高服务器的吞吐能力。

**fd集合** ：可以理解为即一个客户端需要的操作集合（文件描述符集合）

##### select(最早) O（n）:通过设置或者检查存放fd标志位的数据结构来进行下一步处理

**过程** 

将fd集合（文件描述符集合）从用户空间传递到内核空间，内核遍历所有fd集合，找到就绪的fd并进行相应处理，将结果返回（拷贝）给用户空间,用户空间任然需要遍历所有fd才能找出就绪的fd。

它仅仅知道了，有Socket I/O事件（操作）发生了，却并不知道是哪几个流（什么操作）（可能有一个，多个，甚至全部），我们只能无差别轮询所有流，找出能读出数据，或者写入数据的流，对他们进行操作。

**原理：**内核轮询查询已注册的socket列表中，哪些连接就绪（有需求发生）。

​		即每次 select/poll 用户态将所有的socket连接拷贝到内核态

产生问题：

       1.单个进程可监视的fd数量被限制，即能监听端口的大小有限。32位 1024个 64位 2048个
       2.对socket进行扫描时是线性遍历扫描，即采用轮询的方法，效率较低，浪费cpu：    
       3.需要维护一个用来存放大量fd的数据结构，这样会使得用户空间和内核空间在传递该结构时复制开销大
       4.select 不是线程安全的，如果你把一个socket加入到select, 然后突然另外一个线程发现，尼玛，这个socket不用，要收回。对不起，这个select 不支持的，如果你丧心病狂的竟然关掉这个sock, select的标准行为是。。呃。。不可预测的， 这个可是写在文档中的哦.
       5.每次调用select需要把fd集合从用户态拷贝到内核态，当fd很大时，开销大。
       	内核遍历传递进来的所有fd，开销大。

##### poll O(n)
本质上和select没有区别，它将用户传入的数组拷贝到内核空间，然后查询每个fd对应的设备状态， 但是它没有最大连接数的限制，原因是它是基于链表来存储的.
##### epoll O(1)

**原理：** 维护一个简易的文件系统（创建一个(epoll对象)eventpoll），包含一颗红黑树（每个节点都是一个socket，注册了回调函数，当产生事件（有需求时）放入链表中），一张准备就绪句柄双向链表（存放的是红黑树节点上产生事件（有需求）的节点），少量的内核cache。

<img src="https://img-blog.csdnimg.cn/20190627183709895.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl8zODEwMjc3MQ==,size_16,color_FFFFFF,t_70" alt="img" style="zoom:67%;" />

把 select/poll 分成3步

```
int epoll_create(int size);  //建立一个epoll对象
int epoll_ctl(int epfd, int op, int fd, struct epoll_event *event); 
//epoll对象中添加套接字（连接）
int epoll_wait(int epfd, struct epoll_event *events,int maxevents, int timeout);
//收集发生事件的连接
```

**调用epoll_create时**：内核在epoll文件系统里建了个file结点，在内核cache里建了个红黑树用于存储socket外，还会再建立一个rdllist双向链表，用于存储准备就绪的事件。

**执行epoll_ctl时：**增加socket句柄，则检查在红黑树中是否存在，存在立即返回，不存在则添加到树干上，**关键是然后向内核（网卡）注册回调函数**，用于当中断事件来临时向准备就绪链表中插入数据，告诉内核，如果这个句柄的中断到了，就把它放到准备就绪list链表里。

当一个socket上有数据到了，内核在把网卡上的数据copy到内核中后就来把socket插入到准备就绪链表里了，唤醒等待队列中的进程进入运行状态。

**执行epoll_wait时：**立刻返回准备就绪链表里的数据即可。

所有添加到epoll中的事件都会与设备(如网卡)驱动程序建立回调关系，也就是说相应事件的发生时会调用这里的回调方法。这个回调方法在内核中叫做**ep_poll_callback**，它会把这样的事件放到上面的rdllist双向链表中。

**当fd状态改变 （不可读->可读 不可写->可写）触发fd上的回调函数ep_poll_callback被调用**

**过程**

（1）在使用epoll时，首先会构建epoll对象。
（2）有连接接入时，会插入到epoll对象中，epoll对象里实际是一个红黑树+双向链表，fd插入到红黑树中，通过红黑树查找到是否重复
（3）一旦fd就绪，会触发回调把fd的插入到就绪链表中，并唤醒等待队列中的线程。
（4）调用epoll_wait方法时只需要检查就绪链表，如有则返回给用户程序，如没有进入等待队列。

由于epoll把fd管理起来，不需要每次都重复传入，而且只返回就绪的fd，因此减少了用户空间和内核空间的相互拷贝，在fd数量庞大的时候更加高效。



**redis为什么没用阻塞io模式而用了epoll**

**redis kv读写是串行的，为什么epoll就能提高整体tps呢**

redis kv操作基于内存，跟io没关系。redis跟io有关的是作为网络服务器。堵塞io并发需要多线程，多线程并发不了太多。非堵塞io采用一个线程就可以并发，比如轮询io是否就绪，但是效率不高，因此采用io多路复用，让epoll告诉你哪个io就绪

cpu运行速度很快，1s内可以执行完大量的kv操作，所以串行没有性能问题。

如果是堵塞io模式，本可以用来执行kv命令的cpu会被浪费于线程切换

**io多路复用一定要配合非堵塞，才能提高并发**

即当数据没完全到达时，无需等待，而是处理下一个数据，等到数据完整时再通过epoll唤醒




触发模式

    epolllt(默认):LT模式下，只要这个fd还有数据可读，每次 epoll_wait都会返回它的事件，提醒用户程序去操作
    
    epollet(高速):它只会提示一次，直到下次再有数据流入之前都不会再提示了，无论fd中是否还有数据可读。
                 所以在ET模式下，read一个fd的时候一定要把它的buffer读光，也就是说一直读到read的返回值小于请求值，或者遇到EAGAIN错误

为什么要有epollet模式?

    如果采用EPOLLLT模式的话，系统中一旦有大量你不需要读写的就绪文件描述符，它们每次调用epoll_wait都会返回，这样会大大降低处理程序检索自己关心的就绪文件描述符的效率.
    而采用EPOLLET这种边沿触发模式的话，当被监控的文件描述符上有可读写事件发生时，epoll_wait()会通知处理程序去读写.
    如果这次没有把数据全部读写完(如读写缓冲区太小)，那么下次调用epoll_wait()时，它不会通知你，也就是它只会通知你一次，直到该文件描述符上出现第二次可读写事件才会通知你！！！
    这种模式比水平触发效率高，系统不会充斥大量你不关心的就绪文件描述符 

epoll优点？

    1、有最大连接数限制，但很大几乎接近无限制，能打开的FD的上限远大于1024（1G的内存上能监听约10万个端口 2G 20万）；
    2、效率提升，不是轮询的方式，不会随着FD数目的增加效率下降。只有活跃可用的FD才会调用callback函数；
       即Epoll最大的优点就在于它只管你“活跃”的连接，而跟连接总数无关，因此在实际的网络环境中，Epoll的效率就会远远高于select和poll。  
    3、内存拷贝，利用mmap()文件映射内存加速与内核空间的消息传递；即epoll使用mmap减少复制开销。
    4.线程安全          

总结 

    1、表面上看epoll的性能最好，但是在连接数少并且连接都十分活跃的情况下，select和poll的性能可能比epoll好，毕竟epoll的通知机制需要很多函数回调。
    
    2、select低效是因为每次它都需要轮询。但低效也是相对的，视情况而定，也可通过良好的设计改善
