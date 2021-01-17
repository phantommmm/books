# LeetCode（结构）

##### 堆：一种完全二叉树（要求孩子节点要小于或等于父亲节点（如果是最小堆则大于等于其节点））

具体实现有：最大堆、最小堆、二叉堆等

**PriorityQueue（优先队列 ），一个基于优先级堆的无界优先级队列。**

包含重复值

默认最小堆：元素从小到大排序  队首为最小的数，队尾为最大的数。升序排序

插入和删除的时间复杂度*O*(log*k*) N次插入为 O(Nlogk)

```
peek()//返回队首元素,即最小的数
poll()//返回队首元素，队首元素出队列，即最小的数
add()//添加元素
size()//返回队列元素个数
isEmpty()//判断队列是否为空，为空返回true,不空返回false
```

降序排序：从大到小

PriorityQueue<Integer> heap=new PriorityQueue((n1,n2)->n2-n1);

**TreeSet**

不包含重复值

默认降序 first为最下 last为最大

```
TreeSet<Integer> set=new TreeSet<>();
set.add();
set.contains();
set.first();
set.last();
set.pollFirst();
set.pollLast();

自定义规则 实现Comparable接口
//实现降序 first为最小
  @Override
    public int compareTo(Object o) {
        Person p=(Person)o;
        if(this.age>p.age) {
            return 1;
        }
        else if(this.age<p.age){
           return -1;
        }else{
            return 0;
        }
    }
```



### **BlockingQueue(阻塞队列)**

```
添加元素操作，往队列尾添加元素
add()//添加元素时，若长度大于最大容量，则会抛出异常
offer()//...若添加成功，返回true，否则返回false
put()//...会阻塞直到有空间
移除元素操作
remove()//若队列为空，抛出异常
poll()//返回null
take()//...阻塞到有元素
检查获取元素操作
element():获取但不移除队列的头元素，没有元素则抛异常
peek():获取但不移除队列的头元素，没有元素返回null
```

#### ArrayBlockingQueue

内部是通过一个可重入锁ReentrantLock和两个Condition条件对象来实现阻塞

```
/**notEmpty条件对象，用于通知take方法队列已有元素，可执行获取操作 */
    private final Condition notEmpty;

/**notFull条件对象，用于通知put方法队列未满，可执行添加操作 */
    private final Condition notFull;

```

通过一个ReentrantLock来同时控制添加线程与移除线程的并发访问。

只能同时进行一个操作（添加/删除）

![img](https://img-blog.csdn.net/20170902091035662?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvamF2YXplamlhbg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

##### 添加操作

**add():**//内部调用的是offer()

```
public boolean offer(E e) {
     checkNotNull(e);//检查元素是否为null
     final ReentrantLock lock = this.lock;
     lock.lock();//加锁
     try {
         if (count == items.length)//判断队列是否满
             return false;
         else {
             enqueue(e);//添加元素到队列
             return true;
         }
     } finally {
         lock.unlock();
     }
 }

```

**offer():加锁 入队 当putindex==num.length时将putindex=0 回到初始点**

![img](https://img-blog.csdn.net/20170902091307136?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvamF2YXplamlhbg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

**put() 真正的堵塞方法 其它两个不是堵塞方法**

```
//put方法，阻塞时可中断
 public void put(E e) throws InterruptedException {
     checkNotNull(e);
      final ReentrantLock lock = this.lock;
      lock.lockInterruptibly();//该方法可中断
      try {
          //当队列元素个数与数组长度相等时，无法添加元素
          while (count == items.length)
              //将当前调用线程挂起，添加到notFull条件队列中等待唤醒
              notFull.await();
          enqueue(e);//如果队列没有满直接添加。。
      } finally {
          lock.unlock();
      }
  }

```

![img](https://img-blog.csdn.net/20170902091522354?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvamF2YXplamlhbg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

如果队列元素已满，那么当前线程将会被notFull条件对象挂起加到等待队列中，直到队列有空档才会唤醒执行添加操作。但如果队列没有满，那么就直接调用enqueue(e)方法将元素加入到数组队列中。

##### 移除操作

**poll():**获取并删除队列头元素，队列没有数据就返回null，内部通过dequeue()方法删除头元素，唤醒notFull队列线程。

**remove(Object o):**

一是首先判断队列头部元素是否为删除元素，如果是直接删除，并唤醒添加线程.

二是如果要删除的元素并不是队列头元素，那么执行循环操作，从要删除元素的索引removeIndex之后的元素都**往前移动一个位置**，那么要删除的元素就被removeIndex之后的元素替换，从而也就完成了删除操作。

**take():**

take方法其实很简单，有就删除没有就阻塞，注意这个阻塞是可以中断的，如果队列没有数据那么就加入notEmpty条件队列等待(有数据就直接取走，方法结束)，如果有新的put线程添加了数据，那么put操作将会唤醒take线程,执行take操作。

![img](https://img-blog.csdn.net/20170902092114496?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvamF2YXplamlhbg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

#### LinkedBlockingQueue

基于链表的阻塞队列，内部维持一个基于链表的数据队列，实际上我们对LinkedBlockingQueue的API操作都是间接操作该数据队列。

内部分别使用了takeLock 和 putLock 对并发进行控制，也就是说，添加和删除操作并不是互斥操作，可以同时进行，这样也就可以大大提高吞吐量。

也使用了不同的Condition条件对象作为等待队列，用于挂起take线程和put线程。

![img](https://img-blog.csdn.net/20170902092632547?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvamF2YXplamlhbg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

##### 添加操作

**add()**//调用offer();

**offer()**

第一件事是判断队列是否满，满了就直接释放锁，没满就将节点封装成Node入队，然后再次判断队列添加完成后是否已满，不满就继续唤醒等到在条件对象notFull上的添加线程。

第二件事是，判断是否需要唤醒在notEmpty条件对象上的消费线程。

**为什么添加完成后是继续唤醒在条件对象notFull上的添加线程而不是像ArrayBlockingQueue那样直接唤醒notEmpty条件对象上的消费线程？**

在添加新元素完成后，会判断队列是否已满，不满就继续唤醒在条件对象notFull上的添加线程，这点与前面分析的ArrayBlockingQueue很不相同，在ArrayBlockingQueue内部完成添加操作后，会直接唤醒消费线程对元素进行获取，这是因为ArrayBlockingQueue只用了一个ReenterLock同时对添加线程和消费线程进行控制，这样**如果在添加完成后再次唤醒添加线程**的话(如果一直由添加操作)，**消费线程可能永远无法执行**(取出操作)。

而对于LinkedBlockingQueue来说就不一样了，其内部对添加线程和消费线程分别使用了各自的ReenterLock锁对并发进行控制，也就是说添加线程和消费线程是不会互斥的，所以添加锁只要管好自己的添加线程即可，添加线程自己直接唤醒自己的其他添加线程，如果没有等待的添加线程，直接结束了。

如果有就直到队列元素已满才结束挂起，当然offer方法并不会挂起，而是直接结束，只有put方法才会当队列满时才执行挂起操作。注意消费线程的执行过程也是如此。这也是为什么LinkedBlockingQueue的吞吐量要相对大些的原因。


**为什么要当if (c == 0)时才去唤醒消费线程呢？**

这是因为消费线程一旦被唤醒是一直在消费的（前提是有数据），所以c值是一直在变化的，**c值是添加完元素前 队列的大小**，此时c只可能是0或c>0，如果是c=0，那么说明之前消费线程已停止，条件对象上可能存在等待的消费线程，添加完数据后应该是c+1，那么有数据就直接唤醒等待消费线程，如果没有就结束啦，等待下一次的消费操作。

如果c>0那么消费线程就不会被唤醒，只能等待下一个消费操作（poll、take、remove）的调用，那为什么不是条件c>0才去唤醒呢？我们要明白的是消费线程一旦被唤醒会和添加线程一样，一直不断唤醒其他消费线程，如果添加前c>0，那么很可能上一次调用的消费线程后，数据并没有被消费完，条件队列上也就不存在等待的消费线程了，所以c>0唤醒消费线程得意义不是很大，当然如果添加线程一直添加元素，那么一直c>0，消费线程执行的换就要等待下一次调用消费操作了（poll、take、remove）。


##### 移除操作

**remove()**

同时对putLock和takeLock加锁？

这是因为remove方法删除的数据的位置不确定，为了避免造成并非安全问题，所以需要对2个锁同时加锁。

**poll()**

如果队列没有数据就返回null，如果队列有数据，那么就取出来，如果队列还有数据那么唤醒等待在条件对象notEmpty上的消费线程。然后判断if (c == capacity)为true就唤醒添加线程，这点与前面分析if(c==0)是一样的道理。因为只有可能队列满了，notFull条件对象上才可能存在等待的添加线程。

**先唤醒消费队列再唤醒添加队列**

**take()**

一是，如果队列没有数据就挂起当前线程到 notEmpty条件对象的等待队列中一直等待，如果有数据就删除节点并返回数据项，同时唤醒后续消费线程。

二是，尝试唤醒条件对象notFull上等待队列中的添加线程。 

#### 两者区别

1.队列大小有所不同，ArrayBlockingQueue是有界的初始化必须指定大小，而LinkedBlockingQueue可以是有界的也可以是无界的(Integer.MAX_VALUE)，对于后者而言，当添加速度大于移除速度时，在无界的情况下，可能会造成内存溢出等问题。

2.数据存储容器不同，ArrayBlockingQueue采用的是数组作为数据存储容器，而LinkedBlockingQueue采用的则是以Node节点作为连接对象的链表。

3.由于ArrayBlockingQueue采用的是数组的存储容器，因此在插入或删除元素时不会产生或销毁任何额外的对象实例，而LinkedBlockingQueue则会生成一个额外的Node对象（new Node()）。这可能在长时间内需要高效并发地处理大批量数据的时，对于GC可能存在较大影响。

4.两者的实现队列添加或移除的锁不一样，ArrayBlockingQueue实现的队列中的锁是没有分离的，即添加操作和移除操作采用的同一个ReenterLock锁，而LinkedBlockingQueue实现的队列中的锁是分离的，其添加采用的是putLock，移除采用的则是takeLock，这样能大大提高队列的吞吐量，也意味着在高并发的情况下生产者和消费者可以并行地操作队列中的数据，以此来提高整个队列的并发性能。


TreeSet : 二叉树排序**

TreeSet<Integer> treeSet=new TreeSet<>();

treeSet.first();//返回最小值

treeSet.last();//返回最大值

自定义比较规则

```
TreeSet<Person> treeSet=new Person<>();

class Person  implements Comparator<Person>{
	private int age;

	@Override //age小的放在左边 大的放在右边 相同的在比较姓名
    public int compareTo(Students o) {
        int num= this.age-o.age;
        // String类里面已经重写了compareTo方法
        // int    compareTo(String anotherString)  按字典顺序比较两个字符串
        return num == 0 ? this.name.compareTo(o.name) : num;
    }
```

}

异或：^ 返回的是出现一次的数 ones^=num;

或:		| 返回的是出现二次的数  tows|=ones&num 出现两次的数  

​					先将出现一次的数 与 比较的数 取与 然后 再与自己取 或 得到出现两次的数

​			清除出现三次的数  ones&=~threes;  对要清楚的数取反 然后和自己取与

**判断循环 用快慢指针** 

滑动窗口 [ i,j ) 左必右开

长度 -> 索引 -> -1

索引 -> 长度 -> +1

## List

#### ArrayList

基于动态数组

**remove方法**

```
public boolean remove(Object o) {
        if (o == null) {
            for (int index = 0; index < size; index++)
                if (elementData[index] == null) {
                    fastRemove(index);
                    return true;
                }
        } else {
            for (int index = 0; index < size; index++)
                if (o.equals(elementData[index])) {
                    fastRemove(index);
                    return true;
                }
        }
        return false;
    }
```

**1.**判断对象是否为null，是则寻找数组中为null的值移除

**2.**对象不为null，则根据 equal 找到数组值 移除

```
private void fastRemove(int index) {
        modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null; // clear to let GC do its work
    }

```

**fastRemove** 计算需要移动的元素 =数组元素大小-index-1

然后用arraycopy执行数组覆盖

**ArrayList循环过程删除情况**

```
// 迭代删除方式一：报错 java.util.ConcurrentModificationException
        for (String str : list) {
            System.out.println(str);
            if (str.contains("b")) {
                list.remove(str);
            }
        }
 
        // 迭代删除方式二
        /*
            报错：下标越界 java.lang.IndexOutOfBoundsException
　　　　    list移除了元素但size大小未响应变化,所以导致数组下标不对；
　　　　    list.remove(i)必须size--
　　　　    而且取出的数据的索引也不准确，同时需要做i--操作
         */
        int size = list.size();
        for (int i = 0; i < size; i++) {
            String str = list.get(i);
            System.out.println(str);
            if (str.contains("b")) {
                list.remove(i);
                //                size--;
                //                 i--;
            }
        }
 
        // 迭代删除方式三:正常删除，不推荐；每次循环都需要计算list的大小，效率低
        for (int i = 0; i < list.size(); i++) {
            String str = list.get(i);
            System.out.println(str);
            if (str.contains("b")) {
                list.remove(i);
            }
        }
 
        // 迭代删除方式四:正常删除，推荐使用
        for (Iterator<String> ite = list.iterator(); ite.hasNext(); ) {
            String str = ite.next();
            System.out.println(str);
            if (str.contains("b")) {
                ite.remove();
            }
        }
 
        // 迭代删除方式五:报错- java.util.ConcurrentModificationException
        for (Iterator<String> ite = list.iterator(); ite.hasNext(); ) {
            String str = ite.next();
            if (str.contains("b")) {
                list.remove(str);
            }
        }


```

**在循环过程中，删除元素容易抛出异常（快速失败 util包下的全是）**

原理是 迭代器初始化  expectedModCount=modCount;记录当前arraylist对象修改次数。

通过集合.remove时 modCount++；但是expectedModCount不改变，不相等

通过迭代器.remove时,modCount++,但是会重新expectedModCount=modCount,所以相等。

所以可以通过获取一次迭代器并且在迭代器上删除即可。

每当迭代器使用 hasNext/next遍历下一个元素前，会先检查modCount==expectedModCount，是的话返回遍历，否则抛出异常。

**安全失败（concurrent包下的全是）**

遍历是，不是直接在集合上访问，而是复制原有集合内容，在拷贝的集合上访问，当原集合数据改变时，拷贝集合并不知道。

##### 扩容机制

ArrayList构造器包括 有参 和 无参

int newCapacity = oldCapacity + (oldCapacity >> 1);

1.无参构造 初始化容量为0

当第一次添加数据。数组的容量会从0扩容成10。而后的数组扩容才是按照当前容量的1.5倍进行扩容；

2.有参构造 初始化参数为0

数组的容量会从0变成1。这边可以看到一个严重的问题，一旦我们执行了初始容量为0，那么前四次扩容每次都 +1，在第5次添加数据进行扩容的时候才是按照当前容量的1.5倍进行扩容。

因为oldCapacity=1 oldCapacity>>1(即/2)也等于1 所以先时每次扩容1

3.有参构造 初始化容量不为0 参数 ，扩容按照1.5被进行扩容。

**扩容时机**

当数组大小大于容量时扩容，数组容量10 添加11个元素时。

**扩容方式**

方法Arrays.copyOf是浅拷贝，**只复制了对象的引用（内存地址），并没有为每个元素新创建对象！**

对于数值类型的数组是深拷贝，对于引用类型是浅拷贝，基础数值类型都不是对象，都没有指向的，所以肯定只能拷贝值了，所以对于数值来说其实也不能真正的说上是深拷贝。

扩容的时候，会以新的容量建一个原数组的拷贝，修改原数组，指向这个新数组，原数组被抛弃，会被GC回收

**不同JDK**

jdk1.6扩容1.5倍+1

1.7 1.8都是1.5倍

#### LinkedList

基于链表结构

**remove** 一样先判断值是否为null 再判断equal

### 两者相关问题

**ArrayList和LinkedList有什么区别，它们是怎么扩容的？***

1.ArrayList是实现了基于动态数组的数据结构，LinkedList基于链表的数据结构。 
2.对于随机访问get和set，ArrayList觉得优于LinkedList，因为LinkedList要移动指针。 
3.对于新增和删除操作add和remove，LinedList比较占优势，因为ArrayList要移动数据

**两者都是线程不安全的，都没有加同步操作**

### CopyOnWriteArrayList

**ArrayList读多写少场景**

只能分别加锁操作 ，造成大量堵塞，能读不能写，能写不能读。

引入CopyOnWriteArrayList

```
public Object  read() {
    lock.readLock().lock();
    // 对ArrayList读取
    lock.readLock().unlock();
}
public void write() {
    lock.writeLock().lock();
    // 对ArrayList写
    lock.writeLock().unlock();
}
```

**写时复制思想**

多个线程对同一资源 **读** 不影响，**写** 时 复制一份副本后，在副本上操作写，然后替换掉原来的资源。

    	// 这个数组是核心的，因为用volatile修饰了
        // 只要把最新的数组对他赋值，其他线程立马可以看到最新的数组
        private transient volatile Object[] array;
    
    public boolean add(E e) {
        final ReentrantLock lock = this.lock;
        //添加操作加锁 防止多个线程同时操作 创建多个副本
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;
            // 对数组拷贝一个副本出来
            Object[] newElements = Arrays.copyOf(elements, len + 1);
            // 对副本数组进行修改，比如在里面加入一个元素
            newElements[len] = e;
            // 然后把副本数组赋值给volatile修饰的变量
            setArray(newElements);
            return true;
        } finally {
            lock.unlock();
        }
    }

实际上是使用 **空间换时间** ，写操作时基于副本，避免锁，然后用volatile变量赋值保证可见性，写操作 对读操作没有任何影响

**应用场景**

适合于 **读多写少** 的并发场景。

如：白名单，黑名单，商品类目的访问和更新场景。

**缺点**

**内存占用问题：**内存中占用两份对象

**数据一致性问题**。CopyOnWrite容器只能保证数据的最终一致性，不能保证数据的实时一致性。所以如果你希望写入的的数据，马上能读到，请不要使用CopyOnWrite容器。



### Vector

add、remove、get(int)方法都加了synchronized关键字，默认创建一个大小为10的Object数组，并将capacityIncrement设置为0。

容量扩充策略：如果capacityIncrement大于0，则将Object数组的大小扩大为现有size加上capacityIncrement的值；如果capacity等于或小于0，则将Object数组的大小扩大为现有size的两倍，这种容量的控制策略比ArrayList更为可控。

Vector是基于Synchronized实现的线程安全的ArrayList，但在插入元素时容量扩充的机制和ArrayList稍有不同，并可通过传入capacityIncrement来控制容量的扩充。



### 树

**节点深度：**根节点深度 0 第二层深度 1。。。

**度：**节点的子树数目

**叶子节点：**度为零的节点 即下面的节点

#### 二叉树遍历

<img src="https://img2020.cnblogs.com/blog/1542838/202008/1542838-20200809101414671-1310336891.png" alt="img" style="zoom:50%;" />

**前序遍历** ABDFECGHI

<img src="https://img2020.cnblogs.com/blog/1542838/202008/1542838-20200809103614279-2099623730.png" alt="img" style="zoom:50%;" />

**中序遍历** DBEFAGHCI

<img src="https://img2020.cnblogs.com/blog/1542838/202008/1542838-20200809104224685-1584706942.png" alt="img" style="zoom:50%;" />

**后序遍历** DEFBHGICA



#### 满二叉树

叶子节点都在同一层而且除了叶子节点外所有节点都有两个子节点

#### 完全二叉树

 若设二叉树的深度为k，除第k层外，其他各层（1～（k-1）层）的节点数都达到最大值，且第k层所有的节点都连续集中在最左边，这样的树就是完全二叉树。

#### 二叉搜索树(BST)（排序树）

若任意节点的左子树不空，则左子树上所有节点的值均小于它的根节点的值；
若任意节点的右子树不空，则右子树上所有节点的值均大于它的根节点的值；
任意节点的左、右子树也分别为二叉查找树；
没有键值相等的节点。

**二叉搜索树的中序遍历为 递增序列**

#### 平衡二叉树(AVL)

首先，它是一棵二叉搜索树

它是一 棵空树或它的左右两个子树的高度差的绝对值不超过1，并且左右两个子树都是一棵平衡二叉树，

#### 红黑树

##### 性质

1.每个节点非黑即红

2.根节点为黑

3.每个叶子节点（最后一排NULL节点（通常画图被省略掉的））为黑

4.若节点为红，则两个儿子为黑

5.从任意节点到它的**每个叶子节点**的所有简单路径都包含相同数目的黑节点

##### 旋转

![img](https://upload-images.jianshu.io/upload_images/2392382-a95db442f1b47f8a.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

![img](https://upload-images.jianshu.io/upload_images/2392382-0676a8e2a12e2a0b.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

**左旋**：以某个结点作为支点(旋转结点)，其右子结点变为旋转结点的父结点，右子结点的左子结点变为旋转结点的右子结点，左子结点保持不变。如图3。

**右旋**：以某个结点作为支点(旋转结点)，其左子结点变为旋转结点的父结点，左子结点的右子结点变为旋转结点的左子结点，右子结点保持不变。如图4。

**变色**：结点的颜色由红变黑或由黑变红。

##### 查找

<img src="https://upload-images.jianshu.io/upload_images/2392382-07b47eb3722981e6.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp" alt="img" style="zoom:67%;" />

##### 插入

包括两部分：一 查找插入位置 二 插入后自平衡

**插入节点为红色，**因为如果是黑色，则改变了性质5，这个节点所在路径比其他路径多出一个黑色节点，破坏了平衡。

<img src="C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200123000149956.png" alt="image-20200123000149956" style="zoom:200%;" />

![img](https://upload-images.jianshu.io/upload_images/2392382-9ac3d6b69ef7ead3.png?imageMogr2/auto-orient/strip|imageView2/2/w/662/format/webp)

###### 情景1：红黑树为空树

**处理：**把插入节点作为根节点，并设置为黑色。

###### 情景2：插入节点的key已存在（更新）

**处理：**把要插入的节点颜色设置为已经存在节点的颜色，更新插入节点的值（value）

###### 情景3：插入节点的父节点为黑节点

插入节点为红色，不影响平衡。

**处理：**直接插入

###### 情景4：插入节点的父节点为红节点

**因为根节点为黑，又因插入节点的父节点为红，则说明插入节点肯定存在祖父节点**

###### 情景4.1：叔叔节点存在且为红

![img](https://upload-images.jianshu.io/upload_images/2392382-9f2c746bf0769f49.png?imageMogr2/auto-orient/strip|imageView2/2/w/656/format/webp)

**l为插入节点，将父节点和叔节点设置为黑，祖父设置为红，然后把祖父当作新的插入节点（因为祖父破坏了平衡，继续做插入自平衡操作，直到平衡为止）**

**如果PP刚好是根节点，则因为性质2，又变为黑色，则导致从根节点到叶子节点的路径中，黑色节点数增加了（**这是唯一一种会增加红黑树黑色结点层数的插入情景**。）**

**红黑树的生长是自底向上的（从下往上改变）**

###### 情景4.2：叔节点为黑节点或不存在，并且插入节点的父亲节点是祖父结点的左子结点

叔节点非红即为NULL，因为如果叔节点为黑，而父节点为红，就不满足性质5.

###### 情景4.2.1：插入节点是其父节点的左子节点

![img](https://upload-images.jianshu.io/upload_images/2392382-ab4097b750826870.png?imageMogr2/auto-orient/strip|imageView2/2/w/670/format/webp)

**P可以为红 L PP为黑吗？**

可以，不过就又会出现4.1中P为根节点的情况。

###### 情景4.2.2：插入节点是其父节点的右子节点

![img](https://upload-images.jianshu.io/upload_images/2392382-fbfc4f299941cb8b.png?imageMogr2/auto-orient/strip|imageView2/2/w/1024/format/webp)

###### 情景4.3：叔结点为黑或不存在，并且插入结点的父结点是祖父结点的右子结点

###### 情景4.3.1：插入节点是其父节点的右子节点

![img](https://upload-images.jianshu.io/upload_images/2392382-2bc24a78b68dae51.png?imageMogr2/auto-orient/strip|imageView2/2/w/622/format/webp)

###### 情景4.3.2：插入节点是其父节点的左子节点

![img](https://upload-images.jianshu.io/upload_images/2392382-ee1a9027ddcc210a.png?imageMogr2/auto-orient/strip|imageView2/2/w/1016/format/webp)

##### 删除

**第一步：查找替代节点，第二步：删除后自平衡 **

当不存在目标节点时，忽略本次操作；否则删除后需要找节点来替代被删除节点的位置，否则子树和父辈节点会断开，除非删除的节点没有子节点，则不需要替代。

**二叉树删除节点找替代节点情景**

1.若删除节点无子节点，直接删除

2.若删除节点只有一个子节点，用子节点替代删除节点

3.若删除节点有两个节点，用后继节点（大于删除节点的最小节点即删除节点右子树中的最左节点）替换删除节点

**隐藏点**

把二叉树所有结点投射在X轴上，所有结点都是从左到右排好序的，所有目标结点的前后结点就是对应前继和后继结点。

![img](https://upload-images.jianshu.io/upload_images/2392382-dc4f0ab5d111ff96.png?imageMogr2/auto-orient/strip|imageView2/2/w/806/format/webp)

**重要思路**

删除节点被替代后，认为被删除的是替代节点。

**删除操作删除的结点可以看作 删除替代结点，而替代结点最后总是在树末。**

**R是即将被替换到删除结点的位置的替代结点，在删除前，它还在原来所在位置参与树的子平衡，平衡后再替换到删除结点的位置，才算删除完成。（树平衡后再删除替换节点）**

![img](https://upload-images.jianshu.io/upload_images/2392382-f45799daa674d0ad.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

![img](https://upload-images.jianshu.io/upload_images/2392382-edaf96e55f08c198.png?imageMogr2/auto-orient/strip|imageView2/2/w/1035/format/webp)

![img](https://upload-images.jianshu.io/upload_images/2392382-db3468a5977ad998.png?imageMogr2/auto-orient/strip|imageView2/2/w/1004/format/webp)

###### 情景1：替换节点是红色节点

**处理：**把替换节点颜色变为删除节点的颜色？ 应该是 直接替换值 即可 然后删除替换节点

###### 情景2：替换节点是黑色节点 

###### 情景2.1：替换节点是其父节点的左子节点

###### 情景2.1.1：替换节点的兄弟节点是红节点

![img](https://upload-images.jianshu.io/upload_images/2392382-1e4c3388491b588f.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

###### 情景2.1.2：替换节点的兄弟节点是黑节点

###### 情景2.1.2.1：替换节点的兄弟节点的右子节点是红节点，左子节点任意颜色

![img](https://upload-images.jianshu.io/upload_images/2392382-7eea721cbb855876.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

###### 情景2.1.2.2：替换节点的兄弟节点的右子节点为黑节点，左子节点为红节点

![img](https://upload-images.jianshu.io/upload_images/2392382-dc29605ce9889973.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

###### 情景2.1.2.3：替换节点的兄弟节点的子节点都为黑节点

![img](https://upload-images.jianshu.io/upload_images/2392382-75293515d8d87024.png?imageMogr2/auto-orient/strip|imageView2/2/w/778/format/webp)

###### 情景2.2：替换节点是其父节点的右子节点

和2.1类似

###### 情景2.2.1：替换节点的兄弟节点是红节点

![img](https://upload-images.jianshu.io/upload_images/2392382-387664c771b21f1b.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

###### 情景2.2.2：替换节点的兄弟节点是黑节点

###### 情景2.2.2.1：替换节点的兄弟节点的左子节点是红节点，右子节点任意颜色

![img](https://upload-images.jianshu.io/upload_images/2392382-b1ea52c823ce0b0b.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

###### 情景2.2.2.2：替换节点的兄弟节点的左子节点为黑节点，右子节点为红节点

![img](https://upload-images.jianshu.io/upload_images/2392382-edcb4ea6ac87e342.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

###### 情景2.2.2.3：替换节点的兄弟节点的子节点都为黑节点

![img](https://upload-images.jianshu.io/upload_images/2392382-6559c4cccf3df81c.png?imageMogr2/auto-orient/strip|imageView2/2/w/748/format/webp)

##### **总结**

1. 自己能搞定的自消化（情景1）
2. 自己不能搞定的叫兄弟帮忙（除了情景1、情景2.1.2.3和情景2.2.2.3）
3. 兄弟都帮忙不了的，通过父母，找远方亲戚（情景2.1.2.3和情景2.2.2.3）

#### 思考题

**思考题1：黑结点可以同时包含一个红子结点和一个黑子结点吗？**

可以。即红黑树同一层颜色不一定一样。

![img](https://upload-images.jianshu.io/upload_images/2392382-3e64f9f3481b209d.png?imageMogr2/auto-orient/strip|imageView2/2/w/880/format/webp)

**插入例题**

<img src="https://upload-images.jianshu.io/upload_images/2392382-f4c0891c264a2243.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp" alt="img" style="zoom:150%;" />

**删除例题**

![img](https://upload-images.jianshu.io/upload_images/2392382-b037e4c29cbffc4d.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

### 各种树的复杂度

**二叉树：**各种操作时间复杂度：最好：![O(lgn)](https://math.jianshu.com/math?formula=O(lgn)),最差![O(n)](https://math.jianshu.com/math?formula=O(n))。

最差情况是所有的数据全部在一端时。

**二叉搜索树（二叉排序树、二叉查找树）：**各种操作时间复杂度：最好：![O(lgn)](https://math.jianshu.com/math?formula=O(lgn)),最差![O(n)](https://math.jianshu.com/math?formula=O(n))。

最差情况是所有的数据全部在一端时。

**平衡二叉树：**
查找时间复杂度：![O(lgn)](https://math.jianshu.com/math?formula=O(lgn))

**红黑树：**时间复杂度都是O(logn)

红黑树确保没有一条路径会比其它路径长出两倍。它是一种弱平衡二叉树(由于是弱平衡,可以推出,相同的节点情况下,AVL树的高度低于红黑树),相对于要求严格的AVL树来说,红黑树的旋转次数变少,所以对于搜索,插入,删除操作多的情况下,我们就用红黑树。

**所以简单说，搜索的次数远远大于插入和删除，那么选择AVL树，如果搜索，插入删除次数几乎差不多，应该选择RB树。**

#### B树（平衡多路搜索树）

**非叶子节点既存储数据又存储索引，叶子节点存储数据**

![img](https://p-blog.csdn.net/images/p_blog_csdn_net/manesking/4.JPG)

#### B+树

**只有最底层叶子节点保存数据，前面保存索引（文件夹与文件）**

![img](https://p-blog.csdn.net/images/p_blog_csdn_net/manesking/5.JPG)



### HashMap

**1.7** **扩容时机** 先扩容再put

插入值时 判断 **当前容量大小>=阀值 且 发生hash冲突时** 扩容

**1.8 扩容时机** 先put再扩容

先插入 再判断 **当前容量大小>=阀值** 直接扩容

**并发问题**

**1.7** 环形链表和数据丢失

**1.8** 数据覆盖

如果线程A和线程B同时进行put操作，刚好这两条不同的数据hash值一样，并且该位置数据为null，所以这线程A、B都会进入第6行代码中。假设一种情况，线程A进入后还未进行数据插入时挂起，而线程B正常执行，从而正常插入数据，然后线程A获取CPU时间片，此时线程A不用再进行hash判断了，问题出现：线程A会把线程B插入的数据给**覆盖**，发生线程不安全。

#### 解决哈希冲突方法

##### 开放地址法

**1.线性探测法**

遇到哈希冲突时，线性往后探测直到有空位置。

T[d]->T[d+1]->T[d+2]依次探查

**需要不断处理冲突，效率低下**

**2.二次探查法**

T[d]->T[d+di] di=1^2 ,-1^2 ,2^2, -2^2...

**无法探查整个散列空间**

**3.双哈希函数探测法**



##### 拉链法



##### 再哈希法

发生冲突时，使用第二个，第三个哈希函数计算地址，直到无冲突。

##### 公共溢出区

令设立存储表来存储溢出的数据。

### ConcurrentHashMap

唯一的区别就是其中的核心数据如 value ，以及链表都是 volatile 修饰的，保证了获取时的可见性。

#### 1.7

**ReentrantLock+Segment+HashEntry+链表**

![img](https://upload-images.jianshu.io/upload_images/2184951-af57d9d50ae9f547.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/767/format/webp)

**定位**

两次定位。

segment[]位置：key **hash值的高位**  & **segment数组大小-1**

hashEntry[]位置：key **hash值的高位**  & **entry数组大小-1**

**put**

当执行`put`方法插入数据的时候，根据key的hash值，在`Segment`数组中找到对应的位置

如果当前位置没有值，则通过**CAS**进行赋值，接着执行`Segment`的`put`方法通过加锁机制插入数据

假如有线程AB同时执行相同`Segment`的`put`方法

> 线程A 执行`tryLock`方法成功获取锁，然后把`HashEntry`对象插入到相应位置
>
> 线程B 尝试获取锁失败，则执行`scanAndLockForPut()`方法自旋，通过重复执行`tryLock()`方法尝试获取锁
>
> 在**多处理器**环境重复**64**次，**单处理器**环境重复**1**次，当执行`tryLock()`方法的次数超过上限时，则执行`lock()`方法挂起线程B
>
> 当线程A执行完插入操作时，会通过`unlock`方法施放锁，接着唤醒线程B继续执行

**size**

统计每个 segment对象中的元素个数，然后累加。

当计算后面的 segment 元素个数时，前面segment 发生增加或删除 则结果不一定准确

**先采用不加锁方式，连续计算两次**

若两次结果相等，则结果准确。

若两次结果不相等，则把所有 segment 加锁 然后再次计算

**resize**

put元素时做扩容，获取到锁之后，在当线程中扩容（和hashmap差不多）

#### 1.8

![img](https://upload-images.jianshu.io/upload_images/2184951-d9933a0302f72d47.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/768/format/webp)

**synchronized+CAS+HashEntry+链表+红黑树**

为每一行数据加锁，即每个数组元素行加锁。

**锁的粒度变小，并且不是每次都要加锁了**

**put**

根据key hash值找到 node数组位置。

若当前位置node未初始化，则通过 **CAS** 插入数据

若当前位置已有值并且hash值！=-1，则对该节点加 synchronized锁，然后从该节点遍历，直到有空的位置。

若当前位置已有值并且hash值==-1，说明其它线程在扩容，参与一起扩容

**resize**

支持并发迁移节点，从old数组的尾部开始，如果该桶被其他线程处理过了，就创建一个 ForwardingNode 放到该桶的首节点，hash值为-1，其他线程判断hash值为-1后就知道该桶被处理过了

**size**

用一个 volatile `baseCount` 变量来记录ConcurrentHashMap 当前 `节点的个数`。

- 1. 先尝试通过CAS 修改 `baseCount`
- 1. 如果多线程竞争激烈，某些线程CAS失败，那就CAS尝试将 `CELLSBUSY` 置1，成功则可以把 `baseCount变化的次数` 暂存到一个数组 `counterCells` 里，后续数组 `counterCells` 的值会加到 `baseCount` 中。
- 1. 如果 `CELLSBUSY` 置1失败又会反复进行CAS`baseCount` 和 CAS`counterCells`数组

```
if(root==null) return 0;
        List<TreeNode> queue=new LinkedList<>();
        queue.add(root);
        int res=0;
        while(!queue.isEmpty()){
            List<TreeNode> temp=new LinkedList<>();
            for(TreeNode node:queue){
                if(node.left!=null) temp.add(node.left);
                if(node.right!=null) temp.add(node.right);
            }
            queue=temp;
            res++;
        }
        return res;
```

 