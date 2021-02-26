## Redis持久化
    Redis的持久化是将内存的数据保存到硬盘，防止redis因进程重启导致的数据丢失，有RDB和AOF两种持久化的方式。 
    Redis 默认是RDB方式 

### RDB

     RBD 持久化是把当前进程数据生成快照保存到硬盘的过程，触发RDB 持久化分为手动触发和自动触发。
#### 手动触发
     1）save 命令：阻塞当前 redis 服务器，直到 RDB 过程完成为止，对于内存比较大的实例会造成长时间阻塞，线上环境不建议使用。
     2）bgsave 命令：Redis 进程执行fork 操作创建子进程，RDB 持久化由子进程负责，完成后自动结束。阻塞只发生在fork 阶段，一般时间很短。
        bgsave 命令是针对 save 阻塞问题做的优化。因此Redis 内部所有的涉及 RDB 的操作都采用了 bgsave 方式，而save 命令已经废弃。
#### 自动触发
      1）使用 save 相关配置，如 save m n 。表示 m 秒内数据存在 n 次修改时，自动触发 save.。     
      2）如果节点执行全量复制操作，主节点自动执行 bgsave 生成 RDB 文件并发送给从节点。
      3）执行 debug reload 命令重新加载 redis 时，也会自动触发 save 操作。
      4）默认情况下执行 shutdown 命令时，如果没有开启 AOF 持久化功能则自动执行 bgsave。
        save 900 1
        save 300 10
        save 60 10000
        上面的配置规则意思如下:
        # after 900 sec (15 min) if at least 1 key changed
        # after 300 sec (5 min) if at least 10 keys changed
        # after 60 sec if at least 10000 keys changed 
        60s内如果有10000个更新 则save 否则 等300s 看是否有10个更新 以此类推

#### 流程
      1）执行 bgsave 命令，Redis 父进程判断当前是否存在执行的子进程，如 RDB/AOF 子进程，如果存在则 bgsave 命令直接返回。
      2）父进程执行 fork 操作创建子进程，fork 操作过程中父进程会阻塞，通过 info stats 命令查看 latest_fork_usec 选项，可以获取最近一个fork 操作的耗时，单位为微秒。
      3）父进程 fork 完成后，bgsave 命令返回 Background saving statred信息并不再阻塞父进程，可以继续响应其他命令。
      4）子进程创建 RDB 文件，根据父进程内存生成临时快照文件，完成后对原有文件进行原子替换。执行 lastsave 命令可以获取最后一次生成 RDB 的时间，对应 info 统计的 rdb_last_save_time 选项。
      5）进程发送信号给父进程表示完成，父进程跟新统计信息，具体见 info Persistence 下的 rdb_* 相关选项。
#### 文件处理

    保存： RBD 文件保存在 dir 配置指定的目录下，文件名通过 dbfilename 配置指定。可以通过执行 config set dir {newdir} 和 config set dbfilename {newFileName}运行期动态执行，当下次运行时 RDB 文件会保存到新目录。
    
    压缩：Redis 默认采用 LZF 算法对生成的 RBD 文件做压缩处理，压缩后的文件远远小于内存大下，默认开启，可以通过参数config set rfbcompression {yes|no} 动态修改。
    
    校验：如果 Redis 加载损坏的 RDB 文件时拒绝启动，并打印如下日志：
    
    Short read or OOM loading DB.Unrecoverable error,aborting now.
    
    这时可以使用 Redis 提供的redis-check-dump 工具检测 RDB 文件并获取对应的错误报告。
#### RDB持久化中写入数据

Copy On Write 机制，备份的是变更前内存中的数据；

1.父进程继续处理client请求，子进程负责将内存内容写入到临时文件。由于os的写时复制机制（copy on write)父子进程会共享相同的物理页面，当父进程处理写请求时os会为父进程要修改的页面创建副本，而不是写共享的页面。所以子进程的地址空间内的数据是fork时刻整个数据库的一个快照。

2.当子进程将快照写入临时文件完毕后，用临时文件替换原来的快照文件，然后子进程退出（fork一个进程入内在也被复制了，即内存会是原来的两倍）。

3.fork()之后，kernel把父进程中所有的内存页的权限都设为read-only，然后子进程的地址空间指向父进程。当父子进程都只读内存时，相安无事。当其中某个进程写内存时，CPU硬件检测到内存页是read-only的，于是触发页异常中断（page-fault），陷入kernel的一个中断例程。中断例程中，kernel就会把触发的异常的页复制一份，于是父子进程各自持有独立的一份。

**好处**

1、减少分配和复制资源时带来的瞬时延迟；
2、减少不必要的资源分配；
CopyOnWrite的缺点：
1、如果父子进程都需要进行大量的写操作，会产生大量的分页错误（页异常中断page-fault）;

#### RDB优缺点

     优点：
     RDB 是一个紧凑压缩的二进制文件，便于传送到另外其他地方，代表 Redis 在某个时间点上的数据快照。
     非常适用于备份，全量复制等场景。比如每 6 小时执行 bgsave 备份，并把 RDB 文件拷贝带远程机器或者文件系统中（如hdfs）,用于灾难恢复。
     RDB 在保存 RDB 文件时父进程唯一需要做的就是 fork 出一个子进程,接下来的工作全部由子进程来做，父进程不需要再做其他 IO 操作，所以 RDB 持久化方式可以最大化 Redis 的性能。
     与AOF相比,在恢复大的数据集的时候，RDB 方式会更快一些。
     
     缺点：
     RDB 方式数据没办法做到实时持久化。因为 bgsave 每次运行都要执行 fork 操作创建子进程，属于重量级操作，频繁执行成本过高。
     RDB 文件使用特定二进制格式保存，Redis 版本演进过程中有多个格式的 RDB 版本，存在老版本 Redis 服务无法兼容新版 RDB格式的问题。
### AOF

    AOF持久化：以独立日志的方式每次记录写命令，重启时在重新执行AOF 文件中的命令达到恢复数据的目的。
    AOF 的主要作用是解决了数据持久化的实时性，目前已经是Redis 持久化的主流方式。
#### 使用AOF
    开启 AOF 功能需要设置配置：appendonly yes，默认不开启。
    AOF 文件名通过 appendfilename 配置设置，默认文件名是appendonly.aof 。保存路径同 RDB 持久化方式一致，通过 dir 配置指定。
    AOF 的工作流程操作：命令写入（append）、文件同步（sync）、文件重写（rewrite）、重启加载（load）。

 工作流程：
    所有的写入命令会追加到 aof_buf （缓冲区）中。
    AOF 缓冲区根据对应的策略向硬盘做同步操作。
    随着 AOF 文件越来越大，需要定期对 AOF 文件进行重写，达到压缩的目的。
    当 Redis 服务器重启时，可以加载 AOF 文件进行数据恢复。

 命令写入：

    AOF 命令写入的内容直接是文本协议格式。例如 set hello world 命令，在 AOF 缓冲区会会追加如下文本：
    
    3\r\n$3\r\nest\r\n$5\nhello\r\n$5\r\nworld\r\n
 问题：

    1.AOF为什么直接采用文本协议格式？
    
    文本协议具有很好的兼容性。
    开启 AOF 后，所有写入命令都包含追加操作，直接采用协议格式，避免了二次处理的开销。
    文本协议具有可读性，方便直接修改和处理。
    
    2.AOF 为什么把命令追加到 aof_buf （缓冲区）中?
    Redis 使用单线程响应命令，如果每次AOF 文件命令都直接追加到硬盘，那么性能完全取决于当前硬盘负载（要等完成同步后才能执行其他操作）
    先写入缓冲区中，还有另外一个好处，Redis 可以提供多种缓冲区同步硬盘的策略，在性能和安全性方面做出平衡。
#### 文件同步策略

    appendfsync always   （write写入缓存区，fsync操作同步硬盘）# 每次有数据修改发生时都会写入AOF文件（安全但是费时）。
    appendfsync everysec  （write写入缓存区，每秒一次fsync操作同步硬盘） # 每秒钟同步一次，该策略为AOF的缺省策略。
    appendfsync no  （write写入缓存区）# 从不同步。高效但是数据不会被持久化。

write：写入缓存区

    write 操作会触发 延迟写（delay write）机制。 
    Linux 在内核提供页缓冲区用来提高硬盘I/O 性能。
    write 操作在写入系统缓冲区后直接返回。
    同步硬盘操作依赖于系统调度机制，例如：缓冲区页空间写满或达到热特定时间周期。
    同步文件之前，如果此时系统故障宕机，缓冲区内数据将丢失。
fsync ：同步硬盘

    fsync 针对单个文件操作（比如 AOF 文件），做强制硬盘同步，fsync 将阻塞直到硬盘写入后返回，保证了数据的持久化。
    配置为 always 时，每次写入都要同步AOF 文件，在一般的 SATA 硬盘上，Redis 只能支持大约几百 TPS 写入，显然跟Redis 高性能特性背道而驰，不建议配置。
    配置为 no ，由于操作系统每次同步AOF 文件的周期不可控，而且会加大每次同步硬盘的数据，虽然提高了性能，但数据安全性无法保证。
    配置为 everysec ，是建议的同步策略，也是默认配置，做到兼顾性能和数据安全性。理论上只有在系统突然宕机的情况下丢失1 秒的数据。
#### AOF追加阻塞

##### 使用everysec做刷盘策略的流程

阻塞流程分析：

1. 主线程负载写入 AOF 缓冲区。
2. AOF 线程负责每秒执行一次同步磁盘操作，并记录最近一次同步时间。
3. 主线程负责对比上次 AOF 同步时间：
   如果距上次同步成功时间在2秒内，主线程直接返回。
   如果距上次同步成功时间超过2秒，主线程将会阻塞，直到同步操作完成。

![在这里插入图片描述](https://img-blog.csdn.net/20181008162059204?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3p4NzExMTY2/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

##### AOF阻塞引发的两个问题

1. everysec 配置最多可能丢失2秒数据，不是1秒。
2. 如果系统 fsync 缓慢，将会导致 Redis 主线程阻塞影响效率。



#### 重写机制

    Redis服务器fork创建一个新的AOF（子进程）文件来替代现有的AOF文件，新旧两个文件所保存的数据库状态是相同的
    AOF重写并不需要对原有AOF文件进行任何的读取，写入，分析等操作，这个功能是通过读取服务器当前的数据库状态来实现的。

产生问题   ： 子进程在进行AOF重写期间，服务器进程还要继续处理命令请求，而新的命令可能对现有的数据进行修改，这会让当前数据库的数据和重写后的AOF文件中的数据不一致。

解决 ：Redis增加了一个AOF重写缓存，这个缓存在fork出子进程之后开始启用，Redis服务器主进程在执行完写命令之后，会同时将这个写命令追加到AOF缓冲区和AOF重写缓冲区
    
    因此子进程进行AOF重写时，主进程工作：
    
    1.执行client发来的命令请求；
    2.将写命令追加到现有的AOF文件中；
    3.将写命令追加到AOF重写缓存中。
效果 ：

    1.AOF缓冲区的内容会定期被写入和同步到AOF文件中，对现有的AOF文件的处理工作会正常进行
    
    2.从创建子进程开始，服务器执行的所有写操作都会被记录到AOF重写缓冲区中；
完成重写之后（主线程接受到完成信号后）：

    1.将AOF重写缓存中的内容全部写入到新的AOF文件中；这个时候新的AOF文件所保存的数据库状态和服务器当前的数据库状态一致；
    2.对新的AOF文件进行改名，原子的覆盖原有的AOF文件；完成新旧两个AOF文件的替换。
#### 重写后为什么AOF文件变小？
    1.进程内已经超时的数据不在写入文件
    2.旧的 AOF 文件含有无效命令，如的del key1、serm keys、set a 111 等。重写使用进程内数据直接生成，这样新的 AOF 文件只保留最终数据的写入命令。
    3.多条写命令可以合并为一个，如： lpush list a、 lpush list b、 lpush list c 可以转化为 lpush list a b c 。为了防止单条命令过大造成客户端缓冲区溢出，对于 list、set、hash、zset 等类型操作，以64 个元素为界拆分为多条。
    4.AOF 重写，降低了文件占用空间。除此之外，另一个目的是：更小的 AOF 文件可以更快地被 Redis 加载。

重写方式：

    手动触发：直接调用 bgrewriteaof 命令。
    自动触发：根据 auto-aof-rewrite-size 和 auto-aof-rewrite-percetenage 参数确定自动触发时机。
    auto-aof-rewrite-size ：表示运行 AOF 重写时文件最小体积，默认为64 MB。
    auto-aof-rewrite-percetenage ：代表当前 AOF 重写时文件空间（aof_current_size）和上一次重写后AOF 文件空间（aof_base_size）的比值。
#### 重写执行过程

    1.执行 AOF 重写请求。
    2.如果当前进程正在执行 AOF重写，请求不执行并返回如下响应。
    ERROR Background append only file rewriting already in progress
    
    3.如果当前进程正在执行 bgsave 操作，重写命令演出到 bgsave 完成之后再执行，返回如下响应
    Background append only file rewriting scheduled
    
    4.父进程执行fork 创建子进程，开销等同于 bgsave 过程。
    
    5.主进程 fork 操作完成后，继续响应其他命令。所有修改命令依然写入 AOF 缓冲区并根根据 appendfsync 策略同步到硬盘，保证原有 AOF 机制正确性。
    
    6.由于 fork 操作运行写时复制技术，子进程只能共享 fork 操作时的内存数据。由于父进程依然响应命令，Redis 使用“AOF 重写缓冲区”，保存这部分新数据，防止新 AOF 文件生成期间丢失这部分数据。
    
    7.子进程根据内存快照，按照命令合并规则写入到新的 AOF 文件。每次批量写入硬盘数据量由配置 aof-reerite-incremental-fsync 控制，默认为 32MB，防止单词刷盘数据过多造成硬盘阻塞。
    
    8.新 AOF 文件写入完成后，子进程发送信号给父进程，父进程更新统计信息。
    
    9.父进程把 AOF 重写缓冲区的数据写入到新的 AOF 文件。
    
    10.使用新 AOF 文件替换老文件，完成 AOF 重写。
#### 文件校验

    加载损坏的 AOF 文件时会拒绝启动，并打印如下日志：
    
    Bad file format reading the append only file:make a backup of your AOF file , then yse ./redis-check-aof --fix
    
    AOF 文件可能存在结尾不完整的请款，比如机器突然掉电导致 AOF 文件尾部命令写入不全。Redis 为我们提供了 aof-load-truncated 配置来兼容这种情况，默认开启。加载 AOF 时，
    
    !!! waring : short read while loading the AOF fie !!!
    
    !!! Truncating the AOF at offset 397856725 !!!
    
    AOF loaded anyway beacuse aof-load-truncated is enabled
#### 缓存穿透
     描述：访问缓存和数据库中一定不存在的数据，如ID为-1，进而对数据库造成压力，压垮DB
     
     解决：
     1.接口层增加校验，如用户鉴权校验，id做基础校验，id<=0的直接拦截；
     2.从缓存取不到的数据，在数据库中也没有取到，这时将他放入缓存为key-null，缓存有效时间可以设置短点，如30秒（设置太长会导致正常情况也没法使用）。这样可以防止攻击用户反复用同一个id暴力攻击
     3.使用布隆过滤器将所有可能存在的数据哈希到一个足够大的bitmap中，一个一定不存在的数据会被 这个bitmap拦截掉，从而避免了对底层存储系统的查询压力
#### 缓存击穿
     描述：是指一个key非常热点，在不停的扛着大并发，大并发集中对这一个点进行访问，当这个key在失效的瞬间，持续的大并发就穿破缓存，直接请求数据库，就像在一个屏障上凿开了一个洞。 
     
     解决：
     1.设置热点key永不过期
     2.加互斥锁
**设置热点key永不过期**

1.直接对key设置永不过期时间

2.每次get key时检查过期时间，当快要过期的时候，通过后台异步线程重新构造key

```

String get(final String key) {  
        V v = redis.get(key);  
        String value = v.getValue();  
        long timeout = v.getTimeout();  
        if (v.timeout <= System.currentTimeMillis()) {  
            // 异步更新后台异常执行  
            threadPool.execute(new Runnable() {  
                public void run() {  
                    String keyMutex = "mutex:" + key;  
                    if (redis.setnx(keyMutex, "1")) {  
                        // 3 min timeout to avoid mutex holder crash  
                        redis.expire(keyMutex, 3 * 60);  
                        String dbValue = db.get(key);  
                        redis.set(key, dbValue);  
                        redis.delete(keyMutex);  
                    }  
                }  
            });  
        }  
        return value;  
}
```

**加互斥锁**

```
public String get(key) {
      String value = redis.get(key);
      if (value == null) { //代表缓存值过期
          //设置3min的超时，防止del操作失败的时候，下次缓存过期一直不能load db
		  if (redis.setnx(key_mutex, 1, 3 * 60) == 1) {  //代表设置成功
               value = db.get(key);
                      redis.set(key, value, expire_secs);
                      redis.del(key_mutex);
              } else {  //这个时候代表同时候的其他线程已经load db并回设到缓存了，这时候重试获取缓存值即可
                      sleep(50);
                      get(key);  //重试 一般重试一定次数后仍然失败则返回
              }
          } else {
              return value;      
          }
 }
```



#### 缓存雪崩

     描述：缓存在同一时间内大量键过期（失效），接着来的一大波请求瞬间都落在了数据库中导致连接异常。
     
     解决：
     缓存数据的过期时间设置随机，防止同一时间大量数据过期现象发生。
     如果缓存数据库是分布式部署，将热点数据均匀分布在不同搞得缓存数据库中。
     设置热点数据永远不过期。
     像解决缓存穿透一样加锁排队。
     建立备份缓存，缓存A和缓存B，A设置超时时间，B不设值超时时间，先从A读缓存，A没有再读B，并且更新A缓存和B缓存;
#### 缓存预热
     缓存预热就是系统上线后，提前将相关的缓存数据直接加载到缓存系统。
     避免在用户请求的时候，先查询数据库，然后再将数据缓存的问题！
     用户直接查询事先被预热的缓存数据！
#### 面试题

**连续写10万数据，一直写用什么aof还是rdb**

rdb,rdb保存的是数据 aof保存的是写操作，大量写操作会重新执行。

**加载一年的aof是不是一直加载停不下来**

不是，重写机制。

**fork子进程开销是平常的两倍吗**

理论上，fork的子进程和平常进程**使用**同样大小的内存，最坏情况下，子进程内存最大是普通进程的1倍。但实际上不一定。因为 **写时复制**

由于**写时复制**策略，bgsave过程中，写请求只在父进程中进行，子进程不处理(只需把当时 fork 得到的数据存盘就好)。简单来说就是子进程并不会真正copy  父进程的内存数据，只是在新对象的内存映射表中保存旧数据的指针，只有旧数据有更改，才会把这部分数据copy到新的内存空间，即当父进程处理写请求时会把要修改的页创建副本。

bgsave 时启动的 fork 进程的主要内存开销是**拷贝页表、fd 列表等进程数据结构**，当父进程**同时在处理写请求从而影响到某些内存页变动才对这些页进行拷贝增加内存占用**。

![进程1修改页面C前后](http://c.biancheng.net/uploads/allimg/181108/2-1Q10Q45045312.gif)

在 master 上 做 bgsave 容易导致 oom，一般会选择在 slave 上做 bgsave	

