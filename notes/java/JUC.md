### 线程

#### 一个线程调用两次start()

java不允许调用两次start（），会抛出运行时异常，被认为是编程错误。

**Object方法**

`wait` `notify` `notifyAll`

**Thread方法**

`sleep` `join` `start` `yield` `interrupt`

### **讲下interrupt作用**

**Blocking状态**

当线程处于`BLOCKED`状态，说明该线程由于竞争某个对象的锁失败而被挂在了该对象的阻塞队列上了。

那么此时发起中断操作不会对该线程产生任何影响，依然只是设置中断标志位。例如：

```Java
/**
 * 自定义线程类
 */
public class MyThread extends Thread{

    public synchronized static void doSomething(){
        while(true){
            // 空转
        }
    }
    @Override
    public void run(){
        doSomething();
    }

    public static void main(String[] args) throws InterruptedException {
        // 启动两个线程
        Thread thread1 = new MyThread();
        thread1.start();
        Thread thread2 = new MyThread();
        thread2.start();

        Thread.sleep(1000);
        System.out.println(thread1.getState());
        System.out.println(thread2.getState());

        System.out.println(thread2.isInterrupted());
        thread2.interrupt();
        System.out.println(thread2.isInterrupted());
        System.out.println(thread2.getState());
    }
}
```

结果为：

```
RUNNABLE
BLOCKED
false
true
BLOCKED
```

thread2处于`BLOCKED`状态，执行中断操作之后，该线程仍然处于`BLOCKED`状态，但是中断标志位却已被修改。

这种状态下的线程和处于`RUNNABLE`状态下的线程是类似的，给了我们程序更大的灵活性去判断和处理中断。

 **WAITING/TIMED_WAITING**

这两种状态本质上是同一种状态，只不过`TIMED_WAITING`在等待一段时间后会自动释放自己，而`WAITING`则是无限期等待，需要其他线程调用类似`notify`方法释放自己。但是他们都是线程在运行的过程中由于缺少某些条件而被挂起在某个对象的等待队列上。

当这些线程遇到中断操作的时候，会抛出一个`InterruptedException`异常，并清空中断标志位。例如：

```Java
/**
 * 自定义线程类
 */
public class MyThread extends Thread{

    @Override
    public void run(){
        synchronized (this){
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("catch InterruptedException");

            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new MyThread();
        thread.start();

        Thread.sleep(1000);
        System.out.println(thread.getState());

        System.out.println(thread.isInterrupted());
        thread.interrupt();
        Thread.sleep(1000);
        System.out.println(thread.isInterrupted());
    }
}
```

结果为：

```
WAITING
false
catch InterruptedException
false
```

从运行结果看，当线程启动之后就被挂起到该线程对象的等待队列上，然后我们调用`interrupt()`方法对该线程进行中断，输出了我们在catch中的输出语句，显然是捕获了`InterruptedException`异常，接着就看到该线程的中断标志位被清空。

因此我们要么就在`catch`语句中结束线程，否则就在`catch`语句中加上`this.interrupt();`，再次设置标志位，这样也方便在之后的逻辑或者其他地方继续判断。

**总结**

1. `RUNNABLE`和`BLOCKED`类似，对于中断操作只是设置中断标志位并没有强制终止线程，对于线程的终止权利依然在程序手中。
2. `WAITING`和`TIMED_WAITING`状态下的线程对于中断操作是敏感的，他们会抛出异常并清空中断标志位。

### join

join 方法是一个阻塞方法，用来进行线程之间的交流。线程 A 调用 线程 B 的 join 方法，则线程 A 将阻塞，等待线程 B 执行结束后 线程 A 开始执行。



### **线程状态**

![img](https://img-blog.csdn.net/2018070117435683?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3BhbmdlMTk5MQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

• **初始（NEW）**，new Thread()
• **就绪（RUNNABLE）**,这里可以细分为  RUNNABLE（可运行）和RUNNING(运行中) 状态，当**可运行**状态中获取到操作系统资源如CPU后，才会进入到**运行中**状态。

**注意** 在API角度只有RUNNABLE这种状态

• **阻塞（BLOCKED）（锁池状态）**，阻塞表示线程在等待Monitor lock。比如，线程试图通过synchronized去获取某个锁，但是其他线程已经独占了，那么当前线程就会处于阻塞状态
• **等待（WAITING）**，wait\sleep\join 没有设置定时时间，需要手动唤醒
• **计时等待（TIMED_WAIT**），wait\sleep\join 都可以进入该状态，并且到时自动唤醒。
• **终止（TERMINATED）**，不管是意外退出还是正常执行结束，线程已经完成使命，终止运行，也有人把这个状态叫作死亡。

### 方法解读

**1.** Thread.sleep(long millis)，当前线程调用此方法，当前线程进入**TIMED_WAITING**状态，但**不释放锁**，millis后线程自动苏醒进入就绪状态。作用：给其它线程执行机会的最佳方式。

**2.** Thread.yield()，当前线程调用此方法，当前线程放弃获取的**CPU时间片**，但**不释放锁**资源，由运行状态变为就绪状态，让OS再次选择线程。作用：让相同优先级的线程轮流执行，但并不保证一定会轮流执行。实际中无法保证yield()达到让步目的，因为让步的线程还有可能被线程调度程序再次选中。Thread.yield()不会导致阻塞。该方法与sleep()类似，只是不能由用户指定暂停多长时间。

**3.** t.join()/t.join(long millis)，其它线程 join ，当前线程进入WAITING/TIMED_WAITING状态，当前线程**不会释放锁**。线程t执行完毕或者millis时间到，当前线程进入就绪状态。

**4.** obj.wait()，当前线程调用对象的wait()方法，当前线程**释放对象锁**，进入等待队列（waitSet）。依靠notify()/notifyAll()唤醒或者wait(long timeout) timeout时间到自动唤醒。

**5.**obj.notify()唤醒在此对象监视器上等待的单个线程，选择是**随机**的。notifyAll()唤醒在此对象监视器上等待的所有线程。

**yield放弃CPU但不释放锁，那有什么用？**

CPU资源和锁没有直接关系，CPU是由系统分配。

yield表示当前线程通知**线程调度器**当前线程可以让出CPU，线程调度器可以响应或忽略该请求。

- 线程调度器并不一定响应这个请求。
- 响应请求时，仅仅将当前线程变为可运行状态。其他处于可运行状态的线程将竞争CPU资源，高优先级线程将会比相同优先级的线程有较高的概率获得CPU资源，但并不保证。

1. 不要混淆cpu和锁，线程交出cpu并不等于一定要交出锁，这个yield只是让出cpu，让其他线程可以使用cpu，但是如果其他线程wait在该线程hold住的锁上的话，那些线程是不会被执行的，其实就是即使运行也还是继续wait。
2. 所有就绪的线程都可以竞争，高优先级的只是概率大些，但未必一定会先执行。而且刚刚用yield让出cpu的线程也有可能被再次调度到。





![img](https://img-blog.csdn.net/20170604114223462?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvamF2YXplamlhbg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

### 线程关闭

**1.** 使用标志位

在线程中加入一个成员变量，当一个flag使用。在线程run()方法中轮流去检查这个变量，变量变化时就退出这个线程(return)。

**2.** 使用 interrupt

run方法逻辑中判断线程是否被interrupt，通过设置和判断interrpupt状态来暂停线程。

### 怎么理解notify为随机唤醒

notify随机唤醒并不是每次notify都是随机变化的，而是根据线程的优先级相关，优先级高的可能一直唤醒的都是它，即 **你不能以特定的顺序唤醒线程，但是虚拟机会以特定的顺序唤醒线程。**

让线程进入等待状态（WAITING）并唤醒线程会占用大量CPU周期，在多核计算机上，实现通常使等待线程首先进入轮询循环一段时间，以允许其他线程满足条件并仅在轮询循环期间未发生通知时才使它们进入等待状态。即处于空循环的线程优先级会高些。

**会根据policy策略去选择唤醒cxq or entryList中的线程**

### AtomicInteger

基于无锁操作CAS实现线程安全。

**CAS是一种系统原语，原语属于操作系统用语范畴，是由若干条指令组成的，用于完成某个功能的一个过程，并且原语的执行必须是连续的，在执行过程中不允许被中断，也就是说CAS是一条CPU的原子指令，不会造成所谓的数据不一致问题。**

内部调用**Unsafe类**方法，**Unsafe方法**都是**native**，直接调用操作系统底层资源执行相应任务，解决多线程安全问题。

```
//内部存储的变量用volatile修饰，避免指令重排，保证多线程可见性，即变量修改时其它线程能够获得最新值
private volatile int value;
//下述变量value在AtomicInteger实例对象内的内存偏移量
所谓的偏移量可以简单理解为指针指向该变量的内存地址
    private static final long valueOffset;
    
  static {
        try {
           //通过unsafe类的objectFieldOffset()方法，获取value变量在对象内存中的偏移
           //通过该偏移量valueOffset，unsafe类的内部方法可以获取到变量value对其进行取值或赋值操作
            valueOffset = unsafe.objectFieldOffset
                (AtomicInteger.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }
    
//当前值加1，返回新值，底层CAS操作
public final int incrementAndGet() {
     return unsafe.getAndAddInt(this, valueOffset, 1) + 1;
 }
 
 //Unsafe类中的getAndAddInt方法
 //通过一个while循环不断的重试更新要设置的值，直到成功为止。
public final int getAndAddInt(Object o, long offset, int delta) {
        int v;
        do {
            v = getIntVolatile(o, offset);
        } while (!compareAndSwapInt(o, offset, v, v + delta));
        return v;
    }
```

### CAS

- V表示要更新的变量
- E表示预期值
- N表示新值

如果V值等于E值，则将V的值设为N。若V值和E值不同，则说明已经有其他线程做了更新，则当前线程什么都不做或者再次尝试。

**乐观锁**

总是假设最好的情况，每次去拿数据的时候都认为别人不会修改，所以不会上锁，但是在更新的时候会判断一下在此期间别人有没有去更新这个数据，可以使用版本号机制和CAS算法实现

**悲观锁**

总是假设最坏的情况，每次去拿数据的时候都认为别人会修改，所以每次在拿数据的时候都会上锁，这样别人想拿这个数据就会阻塞直到它拿到锁（共享资源每次只给一个线程使用，其它线程阻塞，用完后再把资源转让给其它线程）。Java中`synchronized`和`ReentrantLock`等独占锁就是悲观锁思想的实现。

**优点**

不加锁，通过CPU指令实现，不用进行线程状态切换。

### CAS ABA问题

#### AtomicStampedReference

通过一个键值对Pair存储数据和时间戳，在更新时对数据和时间戳进行比较，只有两者都符合预期才会调用Unsafe的compareAndSwapObject方法执行数值和时间戳替换，也就避免了ABA的问题。

```
 //存储数值和时间的内部类
 private volatile Pair<V> pair;
 //通过Pair内部类存储数据和时间戳
    private static class Pair<T> {
        final T reference;
        final int stamp;
    }
    //of方法放回一个新的Pair
     static <T> Pair<T> of(T reference, int stamp) {
            return new Pair<T>(reference, stamp);
     }
      compareAndSet方法中，只有期望对象的引用和版本号和目标对象的引用和版本号都一样时，才会新建一个Pair对象，然后用新建的Pair对象和原理的Pair对象做CAS操作
        
```

### CAS原理（处理器怎么实现原子操作）

#### 处理器自动保证基本内存操作的原子性

首先处理器会自动保证 **基本** 的内存操作的原子性。处理器保证从系统内存当中读取或者写入一个字节是原子的，意思是当一个处理器读取一个字节时，其他处理器不能访问这个字节的内存地址。

处理器提供 **总线锁定** 和 **缓存锁定** 两个机制来保证 **复杂** 内存操作的原子性。

#### 使用总线锁保证原子性

所谓总线锁就是使用处理器提供的一个LOCK＃信号，当一个处理器在总线上输出此信号时，其他处理器的请求将被阻塞住,那么该处理器可以独占使用共享内存。

#### 使用缓存锁保证原子性

总线锁定把CPU和内存之间通信锁住了，这使得锁定期间，其他处理器不能操作其他内存地址的数据，由于总线缓存阻止了被阻塞处理器和所有内存之间的通信，而输出LOCK#信号的CPU可能只需要锁住特定的一块内存区域，因此总线锁定开销较大。

缓存锁定是某个CPU对缓存数据进行更改时，会通知缓存了该数据的该数据的CPU抛弃缓存的数据或者从内存重新读取。

**但是有两种情况下处理器不会使用缓存锁定。**

第一种情况是操作的数据不能被缓存在处理器内部，或者操作的数据跨多个缓存行（cache line）时，则处理器会调用总线锁定。

第二种情况是处理器不支持缓存锁定，对于Intel 486和Pentium处理器，就算锁定的内存区域在处理器的缓存行中也会调用总线锁定。



## 线程辅助器

### CountDownLauch

同步辅助器，允许一个或多个线程一直等待，直到其他线程执行操作完成。

```
 CountDownLatch latch=new CountDownLatch(2);构造器初始化次数
 latch.await();当线程调用await方法时，线程堵塞，直到其他线程将latch次数减到0才唤醒
 latch.countDown();latch次数减1
 主要是得多线程使用的是同一个latch才能配合
```

### CyclicBarrier

栅栏、屏障，可以循环使用的屏障。

多个线程会互相等待，直到所有线程达到一个同步点后，才执行屏障操作。

```
//参数为线程数，指需要几个线程同时准备好
public CyclicBarrier(int parties) {
}
//多个额外参数表明当所有线程到达屏障准备好时，优先执行barrierAction
public CyclicBarrier(int parties, Runnable barrierAction) {
}

barrier.await();//表明当前线程已准备好，等待其他线程
当所有线程准备好时，优先执行barrierAction，再执行await后面的操作
```

**循环**

当parties线程数小于实际参与屏障的人数时，循环分批执行。



### Semaphore

信号量，用于控制同一实际，资源可被访问的线程数量，一般用于流量的控制。

```
//同时执行线程数
public Semaphore(int permits) {
}

semaphore.acquire();//获得许可 
当许可数达到permits时，其他线程只能等待直到拥有者释放
semaphore.release();//释放许可
```



### 总结

1. CountDownLatch 是一个线程等待其他线程， CyclicBarrier 是多个线程互相等待。
2. CountDownLatch 的计数是减 1 直到 0，CyclicBarrier 是加 1，直到指定值。
3. CountDownLatch 是一次性的， CyclicBarrier  可以循环利用。
4. CyclicBarrier 可以在最后一个线程达到屏障之前，选择先执行一个操作。
5. Semaphore ，需要拿到许可才能执行，并可以选择公平和非公平模式。

## 读写锁

**读写锁ReentrantReadWriteLock**

要么多读，要么一写；读写锁适用去读多写少的情况。

共享锁上可加共享锁，不能加排他锁。排他锁上不能再加任何锁

公平情况下，读写锁都不能插队；非公平情况下，写锁可以插队，读锁只有在头结点不是写等待的情况下插队。

**读锁能否升级为写锁？**

不能，若两个线程的读锁都想升级写锁，则需要对方都释放自己锁，而双方都不释放，就会产生死锁。

**写锁是否降级为读锁？**

能，写锁只有一个，当写锁降级为读锁时，所有的都是读。

```
ReadWriteLock rtLock = new ReentrantReadWriteLock();
rtLock.writeLock().lock();
System.out.println("writeLock");
 
rtLock.readLock().lock();
System.out.println("get read lock");
```

这段代码虽然不会导致死锁，但没有正确的释放锁。从写锁降级成读锁，并不会自动释放当前线程获取的写锁，仍然需要显示的释放，否则别的线程永远也获取不到写锁。

## Syn方法/代码块

> 同步代码块：字节码指令 monitorenter 和 monitorexit 

monitorenter指令指向同步代码块的开始位置，monitorexit指令则指明同步代码块的结束位置，当执行monitorenter指令时，当前线程将试图获取 objectref(即对象锁) 所对应的 monitor 的持有权，当 objectref 的 monitor 的进入计数器为 0，那线程可以成功取得 monitor，并将计数器值设置为 1，取锁成功。如果当前线程已经拥有 objectref 的 monitor 的持有权，那它可以重入这个 monitor (关于重入性稍后会分析)，重入时计数器的值也会加 1。倘若其他线程已经拥有 objectref 的 monitor 的所有权，那当前线程将被阻塞，直到正在执行线程执行完毕，即monitorexit指令被执行，执行线程将释放 monitor(锁)并设置计数器值为0 ，其他线程将有机会持有 monitor 。

> 同步方法

JVM可以从方法常量池中的方法表结构(method_info Structure) 中的 ACC_SYNCHRONIZED 访问标志区分一个方法是否同步方法。当方法调用时，调用指令将会检查方法的 ACC_SYNCHRONIZED 访问标志是否被设置，如果设置了，执行线程将先持有monitor， 然后再执行方法，最后当方法完成(无论是正常完成还是非正常完成)时释放monitor。在方法执行期间，执行线程持有了monitor，其他任何线程都无法再获得同一个monitor。如果一个同步方法执行期间抛 出了异常，并且在方法内部无法处理此异常，那这个同步方法所持有的monitor将在异常抛到同步方法之外时自动释放。

## AQS

AbstractQueuedSynchronizer队列同步器，AQS维护一个**同步队列**和多个**等待队列**。

内部通过一个int类型的成员变量state来控制同步状态,当state=0时，则说明没有任何线程占有共享资源的锁，当state=1时，则说明有线程目前正在使用共享变量，其他线程必须加入**同步队列**进行等待。

AQS内部通过**内部类Node**构成FIFO的同步队列来完成线程获取锁的排队工作，同时利用**内部类ConditionObject**构建**等待队列**，当Condition调用wait()方法后，线程将会加入等待队列中，而当Condition调用signal()方法后，线程将从等待队列转移动同步队列中进行锁竞争。

**注意**：这里涉及到两种队列，一种是同步队列，当线程请求锁失败后将加入同步队列等待，而另一种则是等待队列(可有多个)，通过Condition调用await()方法释放锁后，将加入等待队列。

```
public abstract class AbstractQueuedSynchronizer
    extends AbstractOwnableSynchronizer{
//指向同步队列队头
private transient volatile Node head;
//指向同步的队尾
private transient volatile Node tail;
//同步状态，0代表锁未被占用，1代表锁已被占用
private volatile int state;
}

```

![img](https://img-blog.csdn.net/20170722111303134?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvamF2YXplamlhbg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

**head为空节点，不存储信息只是作为一个牵头节点  Node是线程的封装**

```
static final class Node {
    //共享模式
    static final Node SHARED = new Node();
    //独占模式
    static final Node EXCLUSIVE = null;

    //标识线程已处于结束状态
    static final int CANCELLED =  1;
    //等待被唤醒状态
    static final int SIGNAL    = -1;
    //条件状态，
    static final int CONDITION = -2;
    //在共享模式中使用表示获得的同步状态会被传播
    static final int PROPAGATE = -3;

    //等待状态,存在CANCELLED、SIGNAL、CONDITION、PROPAGATE 4种
    volatile int waitStatus;

    //同步队列中前驱结点
    volatile Node prev;

    //同步队列中后继结点
    volatile Node next;

    //请求锁的线程
    volatile Thread thread;

    //等待队列中的后继结点，这个与Condition有关
    Node nextWaiter;
}
```

### **共享模式**Semaphore信号量

一个锁允许多条线程同时操作，如信号量Semaphore采用的就是基于AQS的共享模式实现的。

*//设置信号量同时执行的线程数是5*        

final Semaphore semp = new Semaphore(5);  

//state由volatile修饰

  private volatile int state;

并且state执行加减是CAS操作，保证state的正确性。竞争线程根据state判断获取成功或失败。

**公平锁执行流程**

在AQS中存在一个变量state，当我们创建Semaphore对象传入许可数值时，最终会赋值给state，state的数值代表同一个时刻可同时操作共享数据的线程数量。

每当一个线程请求(如调用Semaphored的acquire()方法)获取同步状态成功，state的值将会减少1，直到state为0时，表示已没有可用的许可数，也就是对共享数据进行操作的线程数已达到最大值，其他后来的线程将被阻塞，此时AQS内部会将后来的线程封装成共享模式的Node结点，加入同步队列中等待并开启自旋操作。

只有当持有对共享数据访问权限的线程执行完成任务并释放同步状态后，同步队列中的对于的结点线程才有可能获取同步状态并被唤醒执行同步操作。

**非公平锁**

在尝试获取同步状态前，先调用了hasQueuedPredecessors()方法判断同步队列中是否存在结点，如果存在则返回-1，即将线程加入同步队列等待。从而保证先到来的线程请求一定会先执行，也就是所谓的公平锁。其他操作，与前面分析的非公平锁一样。


![img](https://img-blog.csdn.net/20170730162743625?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvamF2YXplamlhbg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

**应用场景：100个学生，10个窗口可以打饭。即100个线程，信号量为10 可以同时处理10个线程**



### **独占模式ReenTrantLock**

同一个时间段只能有一个线程对共享资源进行操作，多余的请求线程需要排队等待，如ReentranLock。

**节点状态waitStatue**

**CANCELLED：**值为1，在同步队列中等待的线程等待超时或被中断，需要从同步队列中取消该Node的结点，其结点的waitStatus为CANCELLED，即结束状态，进入该状态后的结点将不会再变化。

**SIGNAL：**值为-1，被标识为该等待唤醒状态的后继结点，当其前继结点的线程释放了同步锁或被取消，将会通知该后继结点的线程执行。说白了，就是处于唤醒状态，只要前继结点释放锁，就会通知标识为SIGNAL状态的后继结点的线程执行。

**CONDITION：**值为-2，与Condition相关，该标识的结点处于等待队列中，结点的线程等待在Condition上，当其他线程调用了Condition的signal()方法后，CONDITION状态的结点将从等待队列转移到同步队列中，等待获取同步锁。

**PROPAGATE：**值为-3，与共享模式相关，在共享模式中，该状态标识结点的线程处于可运行状态。

#### 非公平锁

默认创建非公平锁。

```
static final class NonfairSync extends Sync {
    //加锁
    final void lock() {
        //执行CAS操作，获取同步状态
        if (compareAndSetState(0, 1))
       //成功则将独占锁线程设置为当前线程  
          setExclusiveOwnerThread(Thread.currentThread());
        else
            //否则再次请求同步状态
            acquire(1);
    }
}
//AQS中的方法
public final void acquire(int arg) {
    //再次尝试获取同步状态
    if (!tryAcquire(arg) &&acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}

```

**lock()获取锁时**，首先对同步状态执行CAS操作，尝试把state的状态从0设置为1，如果返回true则代表获取同步状态成功。如果返回false，则表示已有线程持有该同步状态(其值为1)，获取锁失败。

注意这里存在并发的情景，也就是可能同时存在多个线程设置state变量，因此是CAS操作保证了state变量操作的原子性。

返回false后，执行 acquire(1)方法，该方法是AQS中的方法，它对中断不敏感，即使线程获取同步状态失败，进入同步队列，后续对该线程执行中断操作也不会从同步队列中移出。

**acquire再次尝试获取锁**

**一、**判断同步状态是否为0，是则尝试再次获取同步状态，如果获取成功则将当前线程设置为OwnerThread，否则失败。

**二、**同步状态不为0，则判断当前线程current是否为OwnerThread，如果是则属于重入锁，state自增1，并获取锁成功，返回true，反之失败，返回false，也就是tryAcquire(arg)执行失败，返回false。

**注意：**nonfairTryAcquire(int acquires)内部使用的是CAS原子性操作设置state值，可以保证state的更改是线程安全的。

因此只要任意一个线程调用nonfairTryAcquire(int acquires)方法并设置成功即可获取锁，**不管该线程是新到来的还是已在同步队列的线程**，毕竟这是非公平锁，并不保证同步队列中的线程一定比新到来线程请求(可能是head结点刚释放同步状态然后新到来的线程恰好获取到同步状态)先获取到锁。

**tryAcquire获取锁失败，则执行进入同步队列 addWaiter(Node mode)**

如果是第一次加入或者CAS操作没有成功则执行**enq(node)**入队操作

```
private Node enq(final Node node) {
    //死循环
    for (;;) {
         Node t = tail;
         //如果队列为null，即没有头结点
         if (t == null) { // Must initialize
             //创建并使用CAS设置头结点
             if (compareAndSetHead(new Node()))
                 tail = head;
         } else {//队尾添加新结点
             node.prev = t;
             if (compareAndSetTail(t, node)) {
                 t.next = node;
                 return t;
             }
         }
     }
    }
```

使用一个死循环进行CAS操作，可以解决多线程并发问题。

**一、**如果还没有初始同步队列则创建新结点并使用compareAndSetHead设置头结点，tail也指向head

**二、**是队列已存在，则将新结点node添加到队尾。

**注意:**这两个步骤都存在同一时间多个线程操作的可能，如果有一个线程修改head和tail成功，那么其他线程将继续循环，直到修改成功，这里使用CAS原子操作进行头结点设置和尾结点tail替换可以保证线程安全，从这里也可以看出head结点本身不存在任何数据，它只是作为一个牵头结点，而tail永远指向尾部结点(前提是队列不为null)
![img](https://img-blog.csdn.net/20170719223436655?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvamF2YXplamlhbg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

**进入同步队列后，节点便进入一个自旋过程**（for死循环）

![img](https://img-blog.csdn.net/20170719224339183?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvamF2YXplamlhbg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

![img](https://img-blog.csdn.net/20170720082720370?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvamF2YXplamlhbg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

**总结：**AQS同步器中维护着一个同步队列，当线程获取同步状态失败后，将会被封装成Node结点，加入到同步队列中并进行自旋操作，当当前线程结点的前驱结点为head时，将尝试获取同步状态，获取成功将自己设置为head结点。在释放同步状态时，则通过调用子类(ReetrantLock中的Sync内部类)的tryRelease(int releases)方法释放同步状态，释放成功则唤醒后继结点的线程。



#### 公平锁

该方法与nonfairTryAcquire(int acquires)方法唯一的不同是

在使用CAS设置尝试设置state值前，调用了hasQueuedPredecessors()判断同步队列是否存在结点，如果存在必须先执行完同步队列中结点的线程，当前线程进入等待状态。

这就是非公平锁与公平锁最大的区别，即公平锁在线程请求到来时先会判断同步队列是否存在结点，如果存在先执行同步队列中的结点线程，当前线程将封装成node加入同步队列等待。而非公平锁呢，当线程请求到来时，不管同步队列是否存在线程结点，直接尝试获取同步状态，获取成功直接访问共享资源。

#### Condition

同一个锁拥有多个Condition,即维护多个等待队列。

一个Condition对应一个等待队列，当ReenTrantLock唤醒线程时，可以根据Condition选择等待队列进行唤醒，而不是随机唤醒。

当一个线程调用了**await()**相关的方法，那么该线程将会释放锁，并构建一个Node节点封装当前线程的相关信息**加入到等待队列**中进行等待，直到被唤醒、中断、超时才从等待队列中移出，进入同步队列中自旋获取锁。

![img](https://img-blog.csdn.net/20170723212727787?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvamF2YXplamlhbg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

![img](https://img-blog.csdn.net/20170723212707310?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvamF2YXplamlhbg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

### 为什么同步队列为双向链表而等待队列为单向链表？

同步队列由于极端情况下入队的非原子操作和CANCELLED节点产生过程中断开Next指针的操作，可能会导致无法遍历所有的节点，所以使用双向链表，从后往前遍历

而等待队列由于以获取了锁资源的前提，线程安全所以不用双向链表



### 同步队列和等待队列入队/出队顺序

#### **同步队列**

**入队时机**

线程尝试获取锁失败后，会加入同步队列

**入队流程**

1.当前节点的prev指向前任tail 

2.CAS将tail指向当前节点 

3.前任tail的next指向当前节点 

**出队时机**

一个线程获取锁失败了，被放入同步队列，放入队列中的线程会不断去获取锁，直到获取成功或者不再需要获取（中断）。（即从后往前遍历出队）

![img](https://p0.meituan.net/travelcube/c124b76dcbefb9bdc778458064703d1135485.png)

**同步队列节点通过对前继节点判断当前节点是否堵塞（挂起）**

![img](https://p0.meituan.net/travelcube/9af16e2481ad85f38ca322a225ae737535740.png)

waitStatus==-1唤醒状态，前继节点已经被唤醒了，那么当前节点堵塞等待被唤醒就好了

waitStatus>0 canceled被取消状态，前继节点已被取消掉（不存在了），则从后往前遍历找到存在的节点，然后唤醒他

即找到一个唤醒的节点或唤醒一个节点

唤醒状态（-1） ： 当前线程节点已经准备好了，等待锁资源释放即死循环尝试获取锁，如果成功获取到锁资源则唤醒后继节点

**为什么出队要从后往前遍历？**

节点入队流程

1.当前节点的prev指向前任tail 

2.CAS将tail指向当前节点 

3.前任tail的next指向当前节点 

节点入队并不是原子操作，也就是说如果入队流程已经执行了1和2步后

如果这个时候执行了unparkSuccessor方法，就没办法从前往后找了，所以需要从后往前找。（因为是第一步当前节点先指向前继节点）

还有一点原因，在产生CANCELLED状态节点的时候，先断开的是Next指针，Prev指针并未断开，因此也是必须要从后往前遍历才能够遍历完全部的Node。

综上所述，如果是从前往后找，由于极端情况下入队的非原子操作和CANCELLED节点产生过程中断开Next指针的操作，可能会导致无法遍历所有的节点。

#### 等待队列

**入队时机**

获取锁资源的线程节点调用await方法进入对应的等待队列，直接加到队尾

**出队时机**

调用condition.signal方法，会从前往后唤醒首个节点加入到同步队列尾部

**等待队列出队是从前往后，因为是在有锁的情况下，不用担心线程安全问题**



### 共享模式和独占模式流程

![img](https://p0.meituan.net/travelcube/27605d483e8935da683a93be015713f331378.png)

![img](https://p0.meituan.net/travelcube/3f1e1a44f5b7d77000ba4f9476189b2e32806.png)

注意共享模型是共享锁，即用于ReentrantReadWriteLock，共享锁上只能加共享锁，不能加排他锁

### Synchronized与ReenTrantLock?

**两者都是可重入锁**

#### **性能比较**

**JDK6以前**

**ReentrantLock**底层实现依赖于特殊的CPU指令，比如发送lock指令和unlock指令，不需要用户态和内核态的切换，所以效率高（和volatile底层原理类似）。**synchronized**底层由监视器锁（monitor）是依赖于底层的操作系统的Mutex Lock需要用户态和内核态的切换，所以效率低。

**JDK6以后**

**synchronized经过优化后，很多操作使用了CAS，避免了切换态，两者性能差不多**

#### 便利性比较

Synchronized的使用方便简洁，并且由编译器去保证锁的加锁和释放，官方建议使用Synchronized。

ReentrantLock需要手工声明来加锁和释放锁，为了避免忘记手工释放锁造成死锁，所以最好在finally中声明释放锁。

#### ReentrantLock独有特点

ReentrantLock可以指定是公平锁还是非公平锁。而Synchronized只能是非公平锁。

 ReentrantLock提供了一个Condition类，用来实现分组唤醒需要唤醒的线程们，而Synchronized只能随机唤醒一个线程，或者唤醒全部线程。

 ReentrantLock提供了一种能够中断等待锁的线程的机制，通过lock.lockInterruptibly()来实现这个机制。正在等待的线程可以选择放弃等待，改为处理其他事情。

