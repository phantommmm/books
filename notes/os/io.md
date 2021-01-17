## BIO

**BIO** 是同步阻塞式 IO，JDK1.4 之前的 IO 模型。服务器实现模式为一个连接请求对应一个线程，服务器需要为每一个客户端请求创建一个线程，如果这个连接不做任何事会造成不必要的线程开销。可以通过线程池改善，这种 IO 称为伪异步 IO。适用连接数目少且服务器资源多的场景。

##  NIO

**NIO** 是 JDK1.4 引入的同步非阻塞 IO。服务器实现模式为多个连接请求对应一个线程，客户端连接请求会注册到一个多路复用器 Selector ，Selector 轮询到连接有 IO 请求时才启动一个线程处理。适用连接数目多且连接时间短的场景。

同步是指线程还是要不断接收客户端连接并处理数据，非阻塞是指如果一个管道没有数据，不需要等待，可以轮询下一个管道。

核心组件：

- **Selector：** 多路复用器，轮询检查多个 Channel 的状态，判断注册事件是否发生，即判断 Channel 是否处于可读或可写状态。使用前需要将 Channel 注册到 Selector，注册后会得到一个 SelectionKey，通过 SelectionKey 获取 Channel 和 Selector 相关信息。

- **Channel：** 双向通道，替换了 BIO 中的 Stream 流，不能直接访问数据，要通过 Buffer 来读写数据，也可以和其他 Channel 交互。

- **Buffer：** 缓冲区，本质是一块可读写数据的内存，用来简化数据读写。Buffer 三个重要属性：position 下次读写数据的位置，limit 本次读写的极限位置，capacity 最大容量。

  -   `flip` 将写转为读，底层实现原理把 position 置 0，并把 limit 设为当前的 position 值。 
  -   `clear` 将读转为写模式（用于读完全部数据的情况，把 position 置 0，limit 设为 capacity）。 
  -   `compact` 将读转为写模式（用于存在未读数据的情况，让 position 指向未读数据的下一个）。 
  -   通道方向和 Buffer 方向相反，读数据相当于向 Buffer 写，写数据相当于从 Buffer 读。 

  使用步骤：向 Buffer 写数据，调用 flip 方法转为读模式，从 Buffer 中读数据，调用 clear 或 compact 方法清空缓冲区。

## AIO

一个有效请求一个线程。用户进程只需要发起一个IO操作然后立即返回，等IO操作真正的完成以后，应用程序会得到IO操作完成的通知，此时用户进程只需要对数据进行处理就好了，不需要进行实际的IO读写操作，因为真正的IO读取或者写入操作已经由内核完成了。

异步是指服务端线程接收到客户端管道后就交给底层处理IO通信，自己可以做其他事情，非阻塞是指客户端有数据才会处理，处理好再通知服务器。

实现方式包括通过 Future 的 `get` 方法进行阻塞式调用以及实现 CompletionHandler 接口，重写请求成功的回调方法 `completed` 和请求失败回调方法 `failed`。

## 区别

首先，线程是较为重量级的资源。bio当并发量大，而后端服务或客户端处理数据慢时就会产生产生大量线程处于等待中，即上述的**阻塞，是非常严重的资源浪费**。此外，**线程的切换**也会导致cpu资源的浪费，单机内存限制也无法过多的线程，只能单向以流的形式读取数据。



![img](https:////upload-images.jianshu.io/upload_images/10998555-936b0a5780138362.png?imageMogr2/auto-orient/strip|imageView2/2/w/499/format/webp)



nio使用单线程或者只使用少量的多线程，多个连接共用一个线程，消耗的线程资源会大幅减小。并且当处于等待（没有事件）的时候线程资源可以释放出来处理别的请求，通过事件驱动模型当有accept/read/write等事件发生后通知（唤醒）主线程分配资源来处理相关事件。以buffer缓冲区的形式处理数据，处理更为方便。



- BIO方式适用于连接数目比较小且固定的架构，这种方式对服务器资源要求比较高，并发局限于应用中，JDK1.4以前的唯一选择，但程序直观简单易理解。
- NIO方式适用于连接数目多且连接比较短（轻操作）的架构，比如聊天服务器，并发局限于应用中，编程比较复杂，JDK1.4开始支持。 
- AIO方式使用于连接数目多且连接比较长（重操作）的架构，比如相册服务器，充分调用OS参与并发操作，编程比较复杂，JDK7开始支持。 

##  **五种主要的IO模型**

#### 同步阻塞IO （Blocking IO）

<img src="https://img-blog.csdnimg.cn/20190105163801795.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2NyYXp5bWFrZXJjaXJjbGU=,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述" style="zoom:80%;" />

（1）当用户线程调用了read系统调用，内核就开始了IO的第一个阶段：准备数据（即系统进程将用户线程所需的系统资源写入内核缓冲区）。很多时候，数据在一开始还没有到达（比如，还没有收到一个完整的Socket数据包），这个时候kernel就要等待足够的数据到来。

（2）当kernel一直等到数据准备好了，它就会将数据从kernel内核缓冲区，拷贝到用户缓冲区（用户内存），然后kernel返回结果。

（3）从开始IO读的read系统调用开始，用户线程就进入阻塞状态。一直到kernel返回结果后，用户线程才解除block的状态，重新运行起来。

所以，blocking IO的特点就是在内核进行IO执行的两个阶段，用户线程都被block了。

**BIO的优点：**

程序简单，在阻塞等待数据期间，用户线程挂起。用户线程基本不会占用 CPU 资源。

**BIO的缺点：**

一般情况下，会为每个连接配套一条独立的线程，或者说一条线程维护一个连接成功的IO流的读写。在并发量小的情况下，这个没有什么问题。但是，当在高并发的场景下，服务器需要大量的线程来维护大量的网络连接，内存、线程切换开销会非常巨大。因此，基本上，BIO模型在高并发场景下是不可用的。

#### **同步非阻塞NIO（None Blocking IO）**

<img src="https://img-blog.csdnimg.cn/20190105163821398.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2NyYXp5bWFrZXJjaXJjbGU=,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述" style="zoom:67%;" />

（1）在内核数据没有准备好的阶段，用户线程发起IO请求时，立即返回。用户线程需要不断地发起IO系统调用，直到获取到资源。

（2）内核数据到达后，用户线程发起系统调用，用户线程阻塞。内核开始复制数据。它就会将数据从kernel内核缓冲区，拷贝到用户缓冲区（用户内存），然后kernel返回结果。

（3）用户线程才解除block的状态，重新运行起来。经过多次的尝试，用户线程终于真正读取到数据，继续执行。

**NIO的特点：**

应用程序的线程需要不断的进行 I/O 系统调用，轮询数据是否已经准备好，如果没有准备好，继续轮询，直到完成系统调用为止。

**NIO的优点：**每次发起的 IO 系统调用，在内核的等待数据过程中可以立即返回。用户线程不会阻塞，实时性较好。

**NIO的缺点：**需要不断的重复发起IO系统调用，这种不断的轮询，将会不断地询问内核，这将占用大量的 CPU 时间，系统资源利用率较低。

总之，NIO模型在高并发场景下，也是不可用的。一般 Web 服务器不使用这种 IO 模型。一般很少直接使用这种模型，而是在其他IO模型中使用非阻塞IO这一特性。java的实际开发中，也不会涉及这种IO模型。

**注意：**Java NIO（New IO） 不是IO模型中的NIO模型，而是另外的一种模型，叫做IO多路复用模型（ IO multiplexing ）。

#### **IO多路复用模型(I/O multiplexing）：服务器复用同一线程 接受 多个客户端请求线程**

**用户线程轮询---》系统内核select/epoll 轮询 数据是否准备好**

<img src="https://img-blog.csdnimg.cn/20190105163846560.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2NyYXp5bWFrZXJjaXJjbGU=,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述" style="zoom:67%;" />

select/epoll会在内核里维护一个列表，每个连接的socket都会在这个列表里，当有数据来的时候，会遍历这里面的socket，唤起连接到工作队列。

在这种模式中，首先不是进行read系统调动，而是进行select/epoll系统调用。当然，这里有一个前提，需要将目标网络连接，提前注册到select/epoll的可查询socket列表中。然后，才可以开启整个的IO多路复用模型的读流程。

（1）进行select/epoll系统调用，查询可以读的连接。kernel会查询所有select的可查询socket列表，当任何一个socket中的数据准备好了，select就会返回。

当用户进程调用了select，那么整个线程会被block（阻塞掉）。

（2）用户线程获得了目标连接后，发起read系统调用，用户线程阻塞。内核开始复制数据。它就会将数据从kernel内核缓冲区，拷贝到用户缓冲区（用户内存），然后kernel返回结果。

（3）用户线程才解除block的状态，用户线程终于真正读取到数据，继续执行。

**多路复用IO的特点：**

IO多路复用模型，建立在操作系统kernel内核能够提供的多路分离系统调用select/epoll基础之上的。多路复用IO需要用到两个系统调用（system call）， 一个select/epoll查询调用，一个是IO的读取调用。

和NIO模型相似，多路复用IO需要轮询。负责select/epoll查询调用的线程，需要不断的进行select/epoll轮询，查找出可以进行IO操作的连接。

另外，多路复用IO模型与前面的NIO模型，是有关系的。对于每一个可以查询的socket，一般都设置成为non-blocking模型。只是这一点，对于用户程序是透明的（不感知）。

**多路复用IO的优点：**

用select/epoll的优势在于，它可以同时处理成千上万个连接（用户请求）。与一条线程维护一个连接相比，I/O多路复用技术的最大优势是：系统不必创建线程，也不必维护这些线程，从而大大减小了系统的开销。

多个连接的情况下，不会有连接被阻塞，select/epoll会在内核里维护一个列表，每个连接的socket都会在这个列表里，当有数据来的时候，会遍历这里面的socket，唤起连接到工作队列

Java的NIO（new IO）技术，使用的就是IO多路复用模型。在linux系统上，使用的是epoll系统调用。

**多路复用IO的缺点：**

本质上，select/epoll系统调用，属于同步IO，也是阻塞IO。都需要在读写事件就绪后，自己负责进行读写，也就是说这个读写过程是阻塞的。

#### 信号驱动IO

<img src="https://img-blog.csdn.net/20180630234803839?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3pfcnlhbg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70" alt="这里写图片描述" style="zoom:67%;" />

允许Socket进行信号驱动IO,并注册一个信号处理函数，进程继续运行并不阻塞。当数据准备好时，进程会收到一个SIGIO信号，可以在信号处理函数中调用I/O操作函数处理数据。

#### **异步IO模型（asynchronous IO）**

<img src="https://img-blog.csdnimg.cn/20190105163914730.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2NyYXp5bWFrZXJjaXJjbGU=,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述" style="zoom:67%;" />

kernel的数据准备是将（请求数据）从网络物理设备（网卡）读取到内核缓冲区；kernel的数据复制是将数据从内核缓冲区拷贝到用户程序空间的缓冲区。

（1）当用户线程调用了read系统调用，立刻就可以开始去做其它的事，用户线程不阻塞。

（2）内核（kernel）就开始了IO的第一个阶段：准备数据。当kernel一直等到数据准备好了，它就会将数据从kernel内核缓冲区，拷贝到用户缓冲区（用户内存）。

（3）kernel会给用户线程发送一个信号（signal），或者回调用户线程注册的回调接口，告诉用户线程read操作完成了。

（4）用户线程读取用户缓冲区的数据，完成后续的业务操作。

**异步IO模型的特点：**

在内核kernel的等待数据和复制数据的两个阶段，用户线程都不是block(阻塞)的。用户线程需要接受kernel的IO操作完成的事件，或者说注册IO操作完成的回调函数，到操作系统的内核。所以说，异步IO有的时候，也叫做信号驱动 IO 。

**异步IO模型缺点：**

需要完成事件的注册与传递，这里边需要底层操作系统提供大量的支持，去做大量的工作。

目前来说， Windows 系统下通过 IOCP 实现了真正的异步 I/O。但是，就目前的业界形式来说，Windows 系统，很少作为百万级以上或者说高并发应用的服务器操作系统来使用。

而在 Linux 系统下，异步IO模型在2.6版本才引入，目前并不完善。所以，这也是在 Linux 下，实现高并发网络编程时都是以 IO 复用模型模式为主。

### 零拷贝

指的是内核空间与用户空间之间零次拷贝

**好处**

- 减少甚至完全避免不必要的CPU拷贝，从而让CPU解脱出来去执行其他的任务

- 减少内存带宽的占用

- 通常零拷贝技术还能够减少用户空间和操作系统内核空间之间的上下文切换

  

##### **DMA**

 直接内存访问

DMA是允许外设组件将I/O数据直接传送到主存储器中并且传输不需要CPU的参与，以此将CPU解放出来去完成其他的事情。

 而用户空间与内核空间之间的数据传输并没有类似DMA这种可以不需要CPU参与的传输工具，因此用户空间与内核空间之间的数据传输是需要CPU全程参与的。

所也就有了通过零拷贝技术来减少和避免不必要的CPU数据拷贝过程。



**传统IO**

通过read()将数据放入缓冲区buffer,再通过write输出。

![两个系统调用中的拷贝过程](https://img-blog.csdnimg.cn/20190608195853576.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTAzNzY3ODg=,size_16,color_FFFFFF,t_70)

<img src="https://img-blog.csdn.net/20180122133411574?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMzA5NjA4OA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast" alt="这里写图片描述" style="zoom:50%;" />



\1. JVM发出read() 系统调用。
\2. OS上下文切换到内核模式（第一次上下文切换）并将数据读取到内核空间缓冲区(DMA引擎中执行)。(第一次拷贝：hardware —-> kernel buffer）
\3. OS内核然后将数据复制到用户空间缓冲区（CPU拷贝）(第二次拷贝: kernel buffer ——> user buffer)，然后read系统调用返回。而系统调用的返回又会导致一次内核空间到用户空间的上下文切换(第二次上下文切换)。
\4. JVM处理代码逻辑并发送write（）系统调用。
\5. OS上下文切换到内核模式(第三次上下文切换)并从用户空间缓冲区复制数据到内核空间缓冲区（CPU拷贝）(第三次拷贝: user buffer ——> kernel buffer)。

本例中的socket buffer就是kernel buffer（user buffer ——> socket buffer）

socket buffer：内核中与该socket连接有关的缓冲区 每个socket被创建后都会分配一个输入缓冲区和一个输出缓冲区

\6. write系统调用返回，导致内核空间到用户空间的再次上下文切换(第四次上下文切换)。将内核空间缓冲区中的数据写到hardware(第四次拷贝: kernel buffer ——> hardware)。(DMA拷贝)

（socket buffer ——> protocol engine）本例中的 protocol engine就是hardware

这次拷贝是一个独立且异步的过程。

Q：你可能会问独立和异步这是什么意思？难道是调用会在数据被传输前返回？
 A：事实上调用的返回并不保证数据被传输；它甚至不保证传输的开始。它只是意味着将我们要发送的数据放入到了一个待发送的队列中，在我们之前可能有许多数据包在排队。除非驱动器或硬件实现优先级环或队列，否则数据是以先进先出的方式传输的。



总的来说，传统的I/O操作进行了4次用户空间与内核空间的上下文切换，以及4次数据拷贝。显然从内核空间到用户空间内存的复制是完全不必要的，所以，我们能不能直接从hardware读取数据到kernel buffer后，再从kernel buffer写到目标地点不就好了。注意，不同的操作系统对零拷贝的实现各不相同。在这里我们介绍linux下的零拷贝实现。

##### sendfile

![这里写图片描述](https://upload-images.jianshu.io/upload_images/4235178-66c23adafbfbd47f.jpeg?imageMogr2/auto-orient/strip%7CimageView2/2/w/418)

\1. 发出sendfile系统调用，导致用户空间到内核空间的上下文切换(第一次上下文切换)。通过DMA将磁盘文件中的内容拷贝到内核空间缓冲区中(第一次拷贝: hard driver ——> kernel buffer)。
\2. 然后再将数据从内核空间缓冲区拷贝到内核中与socket相关的缓冲区中(第二次拷贝: kernel buffer ——> socket buffer)。（socket buffer才能将数据传递给协议引擎）
\3. sendfile系统调用返回，导致内核空间到用户空间的上下文切换(第二次上下文切换)。通过DMA引擎将内核空间socket缓冲区中的数据传递到协议引擎(第三次拷贝: socket buffer ——> protocol engine)。

通过sendfile实现的零拷贝I/O只使用了2次用户空间与内核空间的上下文切换，以及3次数据的拷贝。
你可能会说操作系统仍然需要在内核内存空间中复制数据（kernel buffer —>socket buffer）。 是的，但从操作系统的角度来看，这已经是零拷贝，因为没有数据从内核空间复制到用户空间。 

Q：但通过是这里还是存在着一次CPU拷贝操作，即，kernel buffer ——> socket buffer。是否有办法将该拷贝操作也取消掉了？
 A：有的。但这需要底层操作系统的支持。从Linux 2.4版本开始，操作系统底层提供了scatter/gather这种DMA的方式来从内核空间缓冲区中将数据直接读取到协议引擎中，而无需将内核空间缓冲区中的数据再拷贝一份到内核空间socket相关联的缓冲区中。

#### 带有DMA收集拷贝功能的sendfile

![这里写图片描述](https://upload-images.jianshu.io/upload_images/4235178-df9323d3ae59b8f8.jpeg?imageMogr2/auto-orient/strip%7CimageView2/2/w/432)

\1. 发出sendfile系统调用，导致用户空间到内核空间的上下文切换(第一次上下文切换)。

通过DMA引擎将磁盘文件中的内容拷贝到内核空间缓冲区中(第一次拷贝: hard drive ——> kernel buffer)。
\2. 没有数据拷贝到socket缓冲区。

取而代之的是只有相应的描述符信息会被拷贝到相应的socket缓冲区当中。该描述符包含了两方面的信息：a) kernel buffer的内存地址；b) kernel buffer的偏移量。

\3. sendfile系统调用返回，导致内核空间到用户空间的上下文切换(第二次上下文切换)。

DMA gather copy根据socket缓冲区中描述符提供的位置和偏移量信息直接将内核空间缓冲区中的数据拷贝到协议引擎上(第二次拷贝: kernel buffer ——> protocol engine（可以看出是目标文件）)，这样就避免了最后一次CPU数据拷贝。

带有DMA收集拷贝功能的sendfile实现的I/O只使用了2次用户空间与内核空间的上下文切换，以及2次数据的拷贝，而且这2次的数据拷贝都是非CPU拷贝。这样一来我们就实现了最理想的零拷贝I/O传输了，不需要任何一次的CPU拷贝，以及最少的上下文切换。

**sendfile零拷贝消除了所有内核空间缓冲区与用户空间缓冲区之间的数据拷贝过程，因此sendfile零拷贝I/O的实现是完成在内核空间中完成的，这对于应用程序来说就无法对数据进行操作了**

### 解决:

通过mmap实现的零拷贝 只有使用mmap的kernel buffer内核缓冲区才是在用户空间和内核空间是共享的

![这里写图片描述](https://upload-images.jianshu.io/upload_images/4235178-2700bead4cf14739.jpeg?imageMogr2/auto-orient/strip%7CimageView2/2/w/429)

\1. 发出mmap系统调用，导致用户空间到内核空间的上下文切换(第一次上下文切换)。

通过DMA引擎将磁盘文件中的内容拷贝到内核空间缓冲区中(第一次拷贝: hard drive ——> kernel buffer)。
\2. mmap系统调用返回，导致内核空间到用户空间的上下文切换(第二次上下文切换)。

接着用户空间和内核空间共享这个缓冲区，而不需要将数据从内核空间拷贝到用户空间。因为用户空间和内核空间共享了这个缓冲区数据，所以用户空间就可以像在操作自己缓冲区中数据一般操作这个由内核空间共享的缓冲区数据。
\3. 发出write系统调用，导致用户空间到内核空间的上下文切换(第三次上下文切换)。将数据从内核空间缓冲区拷贝到内核空间socket相关联的缓冲区(第二次拷贝: kernel buffer ——> socket buffer)。
\4. write系统调用返回，导致内核空间到用户空间的上下文切换(第四次上下文切换)。通过DMA引擎将内核空间socket缓冲区中的数据传递到协议引擎(第三次拷贝: socket buffer ——> protocol engine)

通过mmap实现的零拷贝I/O进行了4次用户空间与内核空间的上下文切换，以及3次数据拷贝。其中3次数据拷贝中包括了2次DMA拷贝和1次CPU拷贝。明显，它与传统I/O相比仅仅少了1次内核空间缓冲区和用户空间缓冲区之间的CPU拷贝。

这样的好处是，我们可以将整个文件或者整个文件的一部分映射到内存当中，用户直接对内存中对文件进行操作，然后是由操作系统来进行相关的页面请求并将内存的修改写入到文件当中。我们的应用程序只需要处理内存的数据，这样可以实现非常迅速的I/O操作。

#### Java NIO中的ByteBuffer

**HeapByteBuffer**

调用ByteBuffer.allocate()时使用。它被称为堆，因为它保存在JVM的堆空间中，因此您可以获得所有优势，如GC支持和缓存优化。 但是，它不是页面对齐的，这意味着如果您需要通过JNI与本地代码交谈，JVM将不得不复制到对齐的缓冲区空间。

**DirectByteBuffer**

在调用ByteBuffer.allocateDirect（）时使用。 JVM将使用malloc（）在堆空间之外分配内存空间。 因为它不是由JVM管理的，所以你的内存空间是页面对齐的，不受GC影响，这使得它成为处理本地代码的完美选择。 然而，你要C程序员一样，自己管理这个内存，必须自己分配和释放内存来防止内存泄漏。

**MappedByteBuffer**

在调用FileChannel.map（）时使用。 与DirectByteBuffer类似，这也是JVM堆外部的情况。 

它基本上作为OS mmap（）系统调用的包装函数，以便代码直接操作映射的物理内存数据。  