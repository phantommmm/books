# Java堆内存

## 堆内内存

**Java 虚拟机具有一个堆(Heap)，堆是运行时数据区域，所有类实例和数组的内存均从此处分配。堆是在 Java 虚拟机启动时创建的。**

JVM启动时分配的内存即为 **堆内内存**

**特点**

**1.** 对象的堆内存由垃圾回收器的垃圾回收机制回收，并遵守JVM的内存管理机制。

**2.** 堆的内存不需要是连续空间，因此堆的大小没有具体要求，既可以固定，也可以扩大和缩小。

**堆内内存=年轻代+老年代+永久代**

![img](https://img-blog.csdnimg.cn/20181204225115944.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1NlYXJjaGluX1I=,size_16,color_FFFFFF,t_70)

## 堆外内存

**JVM管理以外的内存称为 堆外内存**

**堆外内存**就是把内存对象分配在Java虚拟机的堆以外的内存，这些内存直接**受操作系统管理**（而不是虚拟机），即直接使用 `malloc` 申请的内存。

### DirectByteBuffer

JDK中使用`DirectByteBuffer`对象来表示堆外内存，每个`DirectByteBuffer`对象在初始化时，都会创建一个对应的`Cleaner`对象，这个`Cleaner`对象会在合适的时候执行`unsafe.freeMemory(address)`，从而回收这块堆外内存。

初始化`DirectByteBuffer`时操作如下

![img](https://img-blog.csdnimg.cn/20181204230058751.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1NlYXJjaGluX1I=,size_16,color_FFFFFF,t_70)

其中**`first`**是**`Cleaner`类**的静态变量，**`Cleaner`对象**在初始化时会被添加到**`Clener`链表**中，和**`first`**形成引用关系，**`ReferenceQueue`**是用来保存需要回收的**`Cleaner`**对象。

如果该`DirectByteBuffer`对象在一次GC中被回收了，即

![img](https://img-blog.csdnimg.cn/20181204230401599.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1NlYXJjaGluX1I=,size_16,color_FFFFFF,t_70)

此时，只有**`Cleaner`**对象唯一保存了堆外内存的数据（开始地址、大小和容量），在**下一次FGC**时，把该**`Cleaner`对象**放入到**`ReferenceQueue`**中，并**触发`clean`**方法。

**`Cleaner`对象的`clean`方法主要有两个作用：**

1. 把自身从`Cleaner`链表删除，从而在下次GC时能够被回收
2. 释放堆外内存

**通过虚引用删除对外内存**

cleaner对象是虚引用，当发生GC时，先回收cleaner对象，并将它的包装类phantomRef虚引用放入ReferenceQueue中，在ReferenceQueue中执行clean方法释放堆外内存

**`ReferenceQueue`**  作用

我们希望当一个对象**被GC掉**的时候通知用户线程，进行**额外的处理**时，就需要使用引用队列了。ReferenceQueue即这样的一个对象，当一个实例OBJ被GC掉之后，其相应的包装类，即ref对象会被放入queue中。我们可以从queue中获取到相应的对象信息，同时进行额外的处理。比如反向操作，数据清理等。

**若一直不触发FGC内外内存就不回收了吗？**

在初始化 `DirectByteBuffer` 对象时，会首先判断堆外内存环境，如果苛刻就会主动调用 System.gc 方法，去促进FGC。

**优点**

可以使用更大的内存空间。

避免了GC，不会出现stop the world之类暂停时间。

**缺点**

当对堆外内存进行数据处理时，如编码、过滤时，还是需要将数据拷贝至JVM中。

## 两者区别

**HeapByteBuffer 堆内内存** **DirectByteBuffer 堆外内存**

当遇到 **网络IO** 或者 **读写IO** 请求时，Java会首先将 `HeapByteBuffer` 拷贝至 `DirectByteBuffer` 中，再进行读写操作，因此直接使用 `DirectByteBuffer` 可以省下从 **堆内内存** 到 **堆外内存** 的一次拷贝。

**为什么执行IO时，一定要通过堆外内存？**

**1.** 因为当我们使用JNI（java native interface）传递给底层C库的时候，有一个基本的要求，即 **地址上的内容不能失效**，然而，在GC管理下的对象在堆是会移动的，就是可能传递一个给底层的地址，但是这段内存因为GC垃圾回收而失效了，因此一定把待发送的数据放到一个不会改变的位置，这就是调用native方法前，数据一定要在堆外的原因。

**2.** JVM堆内内存可以不是连续的，而执行读写Native方法要求地址是连续的。