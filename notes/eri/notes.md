# Notes

## Springboot

### Springboot第一次访问接口很慢，之后正常

`懒加载`     `缓存`

**思路:**查看是客户端的开销还是服务端的开销，即是Chrome/Postman的时间开销还是服务端的开销。

**原因**

1.springmvc问题，这个是第一次 DispatcherServlet 初始化时，把那些 HandleMapping 加载进来所以慢，DispatcherServlet是懒加载

2.第一次请求需要获取数据库连接，数据库连接是懒加载

3.tomcat session生成策略为真随机数生成器，噪声收集要很久

**解决：**手动加个springboot初始化类，在项目启动时主动触发访问接口/数据库（健康检查预热接口）

​			生成策略改成伪随机数 [Springboot重启后第一次访问速度慢 - 简书 (jianshu.com)](https://www.jianshu.com/p/5fe0e6fde941)

​			



## LC

搜索二叉树中序遍历后为升序数列

## Java

### ThreadLocal

ThreadLocal可以用来优雅的解决**线程间隔离的对象**，必须主动创建的问题，借助于ThreadLocal无需在线程中显式的创建对象

每个线程维护一个 `ThreadLocalMap` , `ThreadLocalMap` 维护一个 `Entry[]` 数组，`Entry(ThreadLocal<?> k,Object v)` Entry是一个键值对,因为ThreadLocalMap线程隔离，所以只能在当前线程访问，不能在其他线程访问。

```
ThreadLocal<Integer> integerThreadLocal=new ThreadLocal<>();
ThreadLocal<String> stringThreadLocal=new ThreadLocal<>();
integerThreadLocal.set(1);
integerThreadLocal.set(2);
stringThreadLocal.set("str");
```

线程定义几个变量，Entry[] 就有几个key

--ThreadLocalMap

----Entry<integerThreadLocal,1>

----Entry<integerThreadLocal,2>

----Entry<stringThreadLocal,"str">

**`set`**

```
public void set(T value) {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null)
        map.set(this, value);
    else
        createMap(t, value);
}
```

1.首先获取当前线程（调用set方法的线程）

2.获取线程的`ThreadLocalMap`

3.如果Map不为NULL，则设置值，否则创建Map并设置值

**设置值 set(ThreadLocal<?> key, Object value)**

1.根据key 获取哈希值 并且 与Entry长度取余得出该插入的位置

2.从该坐标往后遍历，若key相等则覆盖value,若key==null，则用新key、value覆盖同时清楚key==null的赃数据

3.若超过阈值，清理一遍旧数据（Entry不为null而key为null），若容量>=3/4阈值则进行扩容，把老数据重新哈希进table

**`get`**

```
public T get() {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            ThreadLocalMap.Entry e = map.getEntry(this);
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T)e.value;
                return result;
            }
        }
        return setInitialValue();
    }
```

获取调用线程Thread-->ThreadLocalMap-->hash(key)&len-->table[i]

**`remove`**

```
public void remove() {
         ThreadLocalMap m = getMap(Thread.currentThread());
         if (m != null)
             m.remove(this);
     }
```

当Thread.exit 线程退出时，ThreadLocalMap会被GC回收，即ThreadLocalMap和Thread生命周期一致。

**内存泄漏**

`ThreadLocalMap`使用`ThreadLocal`的弱引用作为`key`，如果一个`ThreadLocal`没有外部强引用来引用它，那么系统 GC 的时候，这个`ThreadLocal`势必会被回收，这样一来，`ThreadLocalMap`中就会出现`key`为`null`的`Entry`，就没有办法访问这些`key`为`null`的`Entry`的`value`，当线程存活时，这些`key`为`null`的`Entry`的`value`就会造成内存泄漏，一直存在一条强引用链：`Thread Ref -> Thread -> ThreaLocalMap -> Entry -> value`永远无法回收，造成内存泄漏。

**注意** `get set remove` 遇到key==null的脏数据都会进行清除（置Entry=null）防止内存泄漏

**为什么使用弱引用**

- **key 使用强引用**：引用的`ThreadLocal`的对象被回收（被置null）了，但是`ThreadLocalMap`还持有`ThreadLocal`的强引用，如果没有手动删除，`ThreadLocal`不会被回收，导致`Entry`内存泄漏。
- **key 使用弱引用**：引用的`ThreadLocal`的对象被回收了，由于`ThreadLocalMap`持有`ThreadLocal`的弱引用，即使没有手动删除，`ThreadLocal`也会被回收。`value`在下一次`ThreadLocalMap`调用`set`,`get`，`remove`的时候会被清除。

比较两种情况，我们可以发现：由于`ThreadLocalMap`的生命周期跟`Thread`一样长，如果都没有手动删除对应`key`，都会导致内存泄漏，但是使用弱引用可以多一层保障：**弱引用`ThreadLocal`不会内存泄漏，对应的`value`在下一次`ThreadLocalMap`调用`set`,`get`,`remove`的时候会被清除**。

因此，`ThreadLocal`内存泄漏的根源是：由于`ThreadLocalMap`的生命周期跟`Thread`一样长，如果没有手动删除对应`key`就会导致内存泄漏，而不是因为弱引用。

**推荐** 每次使用完`ThreadLocal` 后都调用 `remove`进行清除

在使用线程池的情况下，没有及时清理`ThreadLocal`，不仅是内存泄漏的问题，更严重的是可能导致业务逻辑出现问题。所以，使用`ThreadLocal`就跟加锁完要解锁一样，用完就清理。

**为什么使用static修饰ThreadLocal**

为了保证一个ThreadLocal即为一个类对象而不是实例对象，防止同一个线程访问到同一个类的不同实例对象造成错误/浪费。

**为什么使用final修饰ThreadLocal**

TheadLocal作为ThreadLocalMap的key,必须设置为final保证值不变，不然ThreadLocal可变的话，后续再get则得不到对应的值

```
ThreadLocal<String> stringThreadLocal=new ThreadLocal<>();
stringThreadLocal.set("str");
stringThreadLocal=new ThreadLocal<>();
System.out.println(stringThreadLocal.get());//null
```

**常用场景**

ThreadLocal常用用来解决**数据库连接、Session管理**等

### Spring ThreadLocal

**aop拦截，使用threadlocal hold住当前连接，业务处理拿到连接去处理，最后提交事务/回滚事务**

**Spring怎么保证事务在同一个连接中操作**

spring单例模式下，同个事务中多个数据库操作保证使用同一个数据库连接connection  ---》**`ThreadLocal`**

```
//事务流程
dbc = new DataBaseConnection();
Connection con = dbc.getConnection();//获取连接
con.setAutoCommit(false);
con.executeUpdate(...);
con.commit();//提交事务
```

`getConnection()`的时候从数据库连接池中获取一个connection， 然后将其与ThreadLocal绑定， 事务完成后解除绑定。这样就保证了事务在同一连接下完成。

**`bindResource`事务开始**

```
private static final ThreadLocal<Map<Object, Object>> resources =
			new NamedThreadLocal<>("Transactional resources");
```

ThreadLocal存储的为`DataSource数据源`生成的actualKey为key值和`ConnectionHolder数据库连接`作为value值封装成的Map

因为一个系统可以使用多个`DateSource数据源`，所以使用`DateSource`作为Key,`Connection` 作为连接

**同一个线程的同一个DataSource一定会取到同一个连接**

```
public static void bindResource(Object key, Object value) throws IllegalStateException {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		Assert.notNull(value, "Value must not be null");
		//不同线程获取到的map不一样
		Map<Object, Object> map = resources.get();
		// set ThreadLocal Map if none found
		if (map == null) {
			map = new HashMap<>();
			resources.set(map);
		}
		Object oldValue = map.put(actualKey, value);
```

**`uninbindResource事务结束`**

```
@Nullable
	private static Object doUnbindResource(Object actualKey) {
		//获取ThreadLocalMap 不同线程获取到的map不一样
		Map<Object, Object> map = resources.get();
		if (map == null) {
			return null;
		}
		//删除Entry
		Object value = map.remove(actualKey);
		// Remove entire ThreadLocal if empty...
		if (map.isEmpty()) {
			resources.remove();
		}
```

### RMI（remote method invocation）

远程方法调用，一个JVM中的代码可以通过网络实现远程调用另一个JVM的某个方法

![img](https://pic1.zhimg.com/80/v2-16572d8cea59bce49fa0c9c3da27e740_1440w.jpg)

**调用步骤**

1. 客户调用客户端辅助对象stub上的方法
2. 客户端辅助对象stub打包调用信息（变量，方法名），通过网络发送给服务端辅助对象skeleton
3. 服务端辅助对象skeleton将客户端辅助对象发送来的信息解包，找出真正被调用的方法以及该方法所在对象
4. 调用真正服务对象上的真正方法，并将结果返回给服务端辅助对象skeleton
5. 服务端辅助对象将结果打包，发送给客户端辅助对象stub
6. 客户端辅助对象将返回值解包，返回给调用者
7. 客户获得返回值



**RMI与RPC区别**

**1.调用方式不同**

- RMI，客户端和服务端共享同一个接口，客户端通过调用接口方法（实际上客户端没实现，服务端有实现）,对应客户端会生成一个代理类`stub`,服务端会生成一个网络服务类`skeleton`,该类接收请求并真正调用服务端接口，获取结果然后返回。
- RPC中是通过网络服务协议向远程主机发送请求，请求包含了一个参数集和一个文本值，通常形成“classname.methodname(参数集)”的形式。RPC远程主机就去搜索与之相匹配的类和方法，找到后就执行方法并把结果编码，通过网络协议发回。
- RPC仅仅是调用**远程接口**，RMI可以拥有对**远程对象**的引用并调用其方法，并且能够在多JVM实例中传递对象的引用
- RMI=RPC+面向对象

**2.语言范围不同**

- RMI只用于Java；
- RPC是网络服务协议，与操作系统和语言无关。



### Collections.sort/Arrays.sort

底层都是使用 `arrays.sort` 因为在创建`list`对象时，使用`Arrays.asList`，而最后调用的是`TimSort.sort`

**1.7之前是归并排序，后面才是TimSort**

`TimSort` 混合使用 **归并排序**  和 **插入排序**

![Timsort Sorting Algorithm - Infopulse - 624512](https://www.infopulse.com/files/2015/08/timsort-algorythm-1st-screenshot.png)

**思想：**待排序数组存在已排序的子数组

**原理**

1.扫描数组,将数组分成多个段run,将降序段反转

2.定义minrun,**短于**此的run通过插入排序合并到长度**高于**minrun的run，通过插入排序将run长度尽量靠近minrun

3.反复归并一些相邻run(长度相近的run)，过程中需要**避免归并长度相差很大的run**，直至整个排序完成

![Timsort Sorting Algorithm - Infopulse - 609657](https://www.infopulse.com/files/2015/08/timsort-algorythm-2nd-screenshot.png)

`minrun` run数组长度 不应太长（256）/太短（8），如果N/minrun是2的幂最佳(或者小一点)，原因是归并排序在长度相同的数组上效率最高

**如何避免归并长度相差很大的run**

**同时满足一下条件才合并结束**

`X>Y+Z`

`Y>Z`

依次将run压入栈中，若栈顶run X，run Y，run Z 的长度违反了**X>Y+Z 或 Y>Z** 则Y run与较小长度的run合并，并再次放入栈中。 依据这个法则，能够尽量使得大小相同的run合并，以提高性能。注意Timsort是稳定排序故只有相邻的run才能归并

![Timsort Sorting Algorithm - Infopulse - 274783](https://www.infopulse.com/files/2015/08/timsort-algorythm-3rd-screenshot.png)

## Mysql

### 怎么定位慢sql

#### sql执行完了

**1.** 打开慢查询日记 ，设置慢查询时间，默认是10s

```
mysql> set global slow_query_log=on;
```

**2.**查看慢查询,会显示慢sql数量，具体sql在log file中

```
mysql> show status like ‘slow_queries’;
```

log会记录 sql执行的时间点，用户及主机，查询耗费时间 **Lock_time** 锁表时间**Rows_sent** 发送给请求方的记录条数 **Rows_examined**语句扫描的记录条数，具体的执行语句

#### sql未执行完

**通过 show processlist定位**

```
show processlist：查看哪些线程在运行
show open tables：查看哪些表在使用
```

![img](https://upload-images.jianshu.io/upload_images/5818752-819b47770321d8fe.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

`Id` 进程唯一标识 用于kill语句

`User` 用户

`Host` 语句是从哪个IP:PORT发出的

`db` 连接的数据库

`Time` 当前状态持续的时间（s）

`Info` 显示sql语句，长度有限，所以长的sql显示不全

`State` sql语句状态，最重要的列，如果某个线程保持同一状态很久，则需要检查，状态很多

```
Closing tables：正在将表中修改的数据刷新到磁盘中，同时正在关闭已经用完的表。这是一个很快的操作，如果不是这样的话，就应该确认磁盘空间是否已经满了或者磁盘是否正处于重负中。
deleting from main table：服务器正在执行多表删除中的第一部分，刚删除第一个表。
```

### text、blob建立索引

直接在text上建立索引不行，需要指定字段前多少位做前缀索引 (索引最大长度为255)，否则使用全文索引 `fulltext index`

```
mysql>create table test(text_col TEXT,INDEX(text_col(10)))
```

全文索引比 like "abc%" 效率快，适合于数据量大，但精度不高

### 5.7 json

mysql5.7支持json类型，不能设置长度，可以为Null，不能有默认值，可以是对象形式也可以是数组形式

```
mysql> CREATE TABLE lnmp (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `category` JSON,
    `tags` JSON,
    PRIMARY KEY (`id`)
);
```

```
mysql> INSERT INTO `lnmp` (category, tags) VALUES ('{"id": 1, "name": "lnmp.cn"}', '[1, 2, 3]');
```

```
mysql> SELECT * FROM lnmp;
+----+------------------------------+-----------+
| id | category                     | tags      |
+----+------------------------------+-----------+
|  1 | {"id": 1, "name": "lnmp.cn"} | [1, 2, 3] |
|  2 | {"id": 2, "name": "php.net"} | [1, 3, 5] |
+----+------------------------------+-----------+

//查询lnmp表中category属性中的id值
select JSON_EXTRACT(lnmp.category,'$.id') from lnmp
//查询lnmp表中tags属性索引为0的值
select JSON_EXTRACT(lnmp.tags,'$[0]') from lnmp
```

