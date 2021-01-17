# Mysql

### 表设计注意点

**1.设置主键**

最好使用自增主键或者一定范围内随机生成的主键，防止产生大量的页分裂。

**2.添加 create_time 和 update_time**

记录时间戳，方便回溯

**3.枚举字段使用 tinyint 类型**

不使用enum而原因如下

1.order by操作效率低，需要额外操作

2.容易出错

```
CREATE TABLE test (foobar ENUM('0', '1', '2'));
mysql> INSERT INTO test VALUES (1);
结果会插入值 '0'
得改成如下才是插入 ‘1’
mysql> INSERT INTO test VALUES (`1`);
```

原因为当插入的值不加' ' 则会插入位置为1 即第一个 ‘0’

**4.金额字段使用decimal**

不能使用float或double，因为这两种是以二进制存储的，会有精度误差

**5.时间字段选择根据业务定**

`varchar` 直观简单，进行时间比较时，需要额外转换

`timestamp` 4字节整数 范围为1970-2038 当时区发生改变时，值会随之改变

`datetime` 8字节 范围为1000-9999 值不会随时区变化而变化

`bigint` 时间戳 得自己维护

**6.字段定义NOT NULL**

NULL字段仍然可以使用索引，但MYSQL优化器处理复杂，建议用默认 0 或 “”代替

### 分表

随着数据量的不断激增，某张表的记录数也飞速增长，这就给数据表操作造成性能影响，因此需要进行分表

#### 原理

![img](https://img-blog.csdn.net/20160523190156712?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQv/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

如上图所示，其实单表的分割比较简单，就是将当前的一张大数据表中的数据，按照约定的分割规则，将数据均摊到多张小的数据表，目的只是为日后表的CURD操作IO压力更小所设计的

#### 策略

**取模**

![img](https://img-blog.csdn.net/20160523190200573?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQv/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

**时间**

![img](https://img-blog.csdn.net/20160523190204401?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQv/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

date range代表条件日期的范围，比如：201503～201504

**哈希**

![img](https://img-blog.csdn.net/20160523190209510?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQv/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

hash(n)代表获得根据ID生成的hash值的n位字符串，使用它来作为表名的一部分

**区域**

![img](https://img-blog.csdn.net/20160523190214370?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQv/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

id range代表当前记录ID的大小范围，比如：0~9999

#### 问题

涉及到范围查询时则可能引起多表查询，因为在查询前我们不确定要查询的数据的ID号，所以我们必须关联多张分表

**知道多条记录的ID**

分别查询这些分表，最后把所有结果UNION

**不知道多条记录的ID**

通过建立一张**字典表**，该表主要记录了**查询条件关键字与所属分表的对应关系**，这样当输入关键字查询时，先通过关键字从该表中检索出涉及的分表，然后再针对这些分表进行查询，并返货UNION结果即可

### Buffer Pool

包含 free链表（未被使用的缓冲区） flush链表（已经被修改过的缓冲区） lru链表（按照最近最少使用淘汰缓冲区）

### **三大范式**

**第一** 列的原子性，列不能够再分成其它几列

**第二** 表必须有一个主键，其它列必须完全依赖于主键，而不能只依赖于主键一部分（联合主键）

**第三** 每一列数据都和主键直接相关，而不能间接相关（列A依赖于B,B依赖于主键）

### **反范式**

1.**字段重复**

空间换时间，减少表连接操作。

文章列表会显示文章标题、作者等，此时作者信息需要通过**uid连接用户表**获取用户名

因为用户名很少修改，所以实际应用中，在**文章表中添加用户名列**。

字段冗杂适用于不经常变动、不允许修改，但又经常需要通过表连接而获得的字段，可以将该字段直接冗杂到对应的表里。

2.**分表存储**

**水平分割**

当表的数据量很大时，进行水平分割成多张表，并增加一张索引表。

**垂直分割**

当表的字段非常多的时候，进行垂直分割。

例如  Article(aid,uid,username,title,content....)字段非常多

将表中常用的字段 和 不常用的字段分割成多张表，避免持续扫描到少用的字段。

**特别：** 当表中某一列内容很大时，可以单独分割成一张表。



#### 锁

1.快照读(snapshot read)

简单的select操作(不包括 select ... lock in share mode, select ... for update)

2.当前读(current read)

select ... lock in share mode

select ... for update

insert、update、delete



update insert delete默认加排他锁

select,,for update加排他锁

select,,,lock in share mode加共享锁

select不加锁

**共享锁 ** 一个事务加了共享锁，其它事务能够加共享锁，不能够加排他锁。即其它事务能够select和select...lock in share mode

**排他锁** 一个事务加了排他锁，其它事务不能加共享锁和排他锁。即其它事务只能select 基于MVCC

1，Record Lock：单个行记录上的锁。

2，Gap Lock：间隙锁，锁定一个范围，但不包括记录本身。GAP锁的目的，是为了防止同一事务的两次当前读，出现幻读的情况。

3，Next-Key Lock：1+2，锁定一个范围，并且锁定记录本身。对于行的查询，都是采用该方法，主要目的是解决幻读的问题。

若名字唯一索引或主键索引时，降级为行锁。



### 慢查询

开启慢查询日志，可以让MySQL记录下查询超过指定时间（默认10s）的语句，通过定位分析性能的瓶颈，才能更好的优化数据库系统的性能。

默认不启动。将语句运行时间超过指定时间的sql记录到日记中。

**如果不是调优需要的话，一般不建议启动该参数**，因为开启慢查询日志会或多或少带来一定的性能影响。

### 日记

#### 错误日记

用来记录 MySQL 服务器运行过程中的错误信息,默认开启无法关闭.
复制环境下，从服务器进程的信息也会被记录进错误日志

#### 查询日记

**查询日志里面记录了数据库执行的所有命令，不管语句是否正确，都会被记录**，具体原因如下:

insert 查询为了避免数据冲突，如果此前插入过数据，当前插入的数据如果跟主键或唯一键的数据重复那肯定会报错；
update 时也会查询因为更新的时候很可能会更新某一块数据；
delete 查询，只删除符合条件的数据；
因此都会产生日志，在并发操作非常多的场景下，查询信息会非常多，那么如果都记录下来会导致 IO 非常大，影响 MySQL 性能，**因此如果不是在调试环境下，是不建议开启查询日志功能的。**默认是关闭的。

#### redo log重做日记

**物理日记，记录的是数据页的物理修改，而不是某一行或某几行修改成怎样怎样，它用来恢复提交后的物理数据页(恢复数据页，且只能恢复到最后一次提交的位置)。**

记录对数据库的操作，即使后来系统崩溃，在重启后也能把这种修改通过日记恢复出来。

包含缓冲区和硬盘持久化文件。

不管事务是否提交都会记录下来。

#### undo log回滚日记

用于存储日志被修改前的值，从而保证如果修改出现异常，可以使用 undo log 日志来实现回滚操作，保证事务的原子性。

undo log 和 redo log 记录物理日志不一样，它是逻辑日志，可以认为当 delete 一条记录时，undo log 中会记录一条对应的 insert 记录，反之亦然，当 update 一条记录时，它记录一条对应相反的 update 记录，当执行 rollback 时，就可以从 undo log 中的逻辑记录读取到相应的内容并进行回滚。undo log 默认存放在共享表空间中，在 ySQL 5.6 中，undo log 的存放位置还可以通过变量 innodbundodirectory 来自定义存放目录，默认值为“.”表示 datadir 目录。

#### bin log二进制日记

是一个二进制文件，主要记录所有数据库表结构变更
bin log 中记录了对 MySQL 数据库**执行更改的所有操作**，并且记录了语句发生时间、执行时长、操作数据等其它额外信息，但是它不记录 SELECT、SHOW 等那些不修改数据的 SQL 语句。

**作用**

### 为什么索引用B+树

哈希表虽然查找快但是不支持范围查询，所以要优化，avl树，avl树因为是2叉的，范围解决了但是树的高度还是太高。所以进一步优化成B树。B树减少了一次IO。但是B树进行范围查询还可以进一步优化。B+树他只有叶子节点存放data而且串成链表。这样范围查询就可以遍历链表，而不是去树上中序遍历。

### explain

explain + select语句看查询计划，主要包括以下几个属性

id、select_type、table、type、possible_keys、key、key_len、ref、rows、Extra几项信息。

**id**

表明查询的顺序，在嵌套查询中，id越大的语句先执行

**select type**

区别 普通查询 子查询 联合查询等复杂查询

```
1、simple：表示简单子查询，不包含子查询和union；

2、primary：表示复杂查询中最外层的 select；

3、subquery：一般子查询中的子查询被标记为subquery，也就是位于select列表中的查询；

4、derived：包含在 from 子句中的子查询。MySQL会将结果存放在一个临时表中，也称为派生表；

5、union：位于union中第二个及其以后的子查询被标记为union，第一个就被标记为primary如果是union位于from中则标记为derived；
```

**table **

查询的表

**type**

性能从左到右依次下降

system > const > eq_ref > ref > range > index > ALL 

```
　1、system：只有一条数据的系统表，或派生表只有一条数据的子查询；

　　2、const：当确定最多只会有一行匹配的时候，MySQL优化器会在查询前读取它而且只读取一次，因此非常快。当主键放入where子句时，mysql把这个查询转为一个常量（高效）；  主键/唯一键 单表

　　3、eq_ref：唯一性索引，对于每个键的查询，最多只返回一条符合条件的记录。使用唯一性索引或主键查找时会发生 （高效）； 按联表的主键或唯一键联合查询 多表查询

　　4、ref：非唯一性索引，对于每个索引键的查询，返回匹配所有行（0，多）；

　　5、range：检索指定范围的行，key 列显示使用了哪个索引，where后面是一个范围查询（between，>，<，>=，in有时候会失效，从而转为无索引ALL）；

　　6、index：另一种形式的全表扫描。扫描表的时候按照索引顺序进行而不是行，然后进行回表操作。主要优点就是避免了排序, 但是开销仍然非常大。如在Extra列看到Using index，说明正在使用覆盖索引，只扫描索引的数据，它比按索引次序全表扫描的开销要小很多；

　　7、all:最坏的情况，全表扫描
```

**possible key**

可能用到的索引

**key**

实际用到的索引 如果没有索引 则为NULL

**ref**

只有type为ref时，该属性才有值，表明哪一列被使用到了

![img](https://upload-images.jianshu.io/upload_images/4582242-3a2f9e2d2b619aca.png?imageMogr2/auto-orient/strip|imageView2/2/w/1147/format/webp)

上图说明 shared库的t2表的col字段被用到了索引，const表明用到了常量

**rows**

需要查找的行数

**extra**

```
　　1、Using filesort：说明mysql会对数据使用一个外部的索引排序，而不是按照表内的索引顺序进行读取。MySQL中无法利用索引完成的排序操作称为“文件排序；

　　2、Using temporary：用临时表保存中间结果，常用于GROUP BY 和 ORDER BY操作中，一般看到它说明查询需要优化了，就算避免不了临时表的使用也要尽量避免硬盘临时表的使用;

　　3、Using index: 说明查询是覆盖索引的，不需要读取数据文件，从索引树（索引文件）中即可获得信息。如果同时出现using where，表明索引被用来执行索引键值的查找，没有using where，表明索引用来读取数据而非执行查找动作。

　　6、Using where: 表明使用了where 

　　10、Distinct：优化distinct操作，在找到第一匹配的行后它将停止找更多的行；　　
```



### 主键Id怎么选择？（分布式id）

美团分布式id

https://tech.meituan.com/2017/04/21/mt-leaf.html

#### 自增id

**优点：** 简单，性能高，id排序对于易于排序和分页

**缺点：**

分表分库时，自增id重复，无法唯一。

在单个数据库或读写分离或一主多从的情况下，只有一个主库可以生成。有单点故障的风险。

多个数据库数据合并或迁移很麻烦

对于数据敏感的场景，容易被推测出来。

**优化：** 多个集群 主键设置起始位置不一样，步长一样(集群个数)

A 1,4,7,10 B 2,5,8,11 C 3,6,9,12

#### UUID

本地生成的字符串id

**优点：**适用于分布式环境，数据迁移或数据合并情况

​	       性能非常高：本地生成，没有网络消耗。

**缺点：**

UUID不具有有序性，影响插入效率。

会导致 B+ 树索引在写的时候有过多的随机写操作（连续的 ID 可以产生部分顺序写），还有，由于在写的时候不能产生有顺序的 append 操作，而需要进行 insert 操作，将会读取整个 B+ 树节点到内存，在插入这条记录后会将整个节点写回磁盘，这种操作在记录占用空间比较大的情况下，性能下降明显。

使用字符串存储，效率低，存储空间大。

**适用于 文件名、编号名**

#### 雪花算法

long型id

使用41bit作为毫秒数，10bit作为机器的ID（5个bit是数据中心，5个bit的机器ID）可以自定义设置，12bit作为毫秒内的流水号（意味着每个节点在每毫秒可以产生 4096 个 ID），最后还有一个符号位，永远是0。

**优点：**

不依赖于数据库、灵活方便

ID按照时间在单机上是递增的。

**缺点：**

在单机上是递增的，但分布式环境下，每台机器上的时钟不可能完全同步

强依赖机器时钟，如果机器上时钟回拨，会导致发号重复或者服务会处于不可用状态。



### 主从复制/备份

**好处**

①避免数据库单点故障：主服务器实时、异步复制数据到从服务器，当主数据库宕机时，可在从数据库中选择一个升级为主服务器，从而防止数据库单点故障。

②提高査询效率：根据系统数据库访问特点，可以使用主数据库进行数据的插入、删除及更新等写操作，而从数据库则专门用来进行数据査询操作，从而将査询操作分担到不同的从服务器以提高数据库访问效率。

**流程**

(1) Master 将数据改变记录到二进制日志(binary log)中

(2) Slave 通过 I/O 线程读取 Master 中的 binary log events 并写入到它的中继日志(relay log)； 

(3) Slave 重做中继日志中的事件，把中继日志中的事件信息一条一条的在本地执行一次，完成数据在本地的存储，从而实现将改变反映到它自己的数据(数据重放)。


<img src="https://img-blog.csdn.net/20160425105401063?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQv/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center" alt="img" style="zoom:50%;" />

**MySQL 对于二进制日志 (binlog)的复制类型** 

(1) 基于语句的复制：在 Master 上执行的 SQL 语句，在 Slave 上执行同样的语句。MySQL 默 认采用基于语句的复制，效率比较高。一旦发现没法精确复制时，会自动选着基于行的复制。 

(2) 基于行的复制：把改变的内容复制到 Slave，而不是把命令在 Slave 上执行一遍。从MySQL5.0 开始支持。

 (3) 混合类型的复制：默认采用基于语句的复制，一旦发现基于语句的无法精确的复制时，就会采用基于行的复制。


#### 主从不同步情况

1. 网络的延迟
   由于mysql主从复制是基于binlog的一种异步复制
   通过网络传送binlog文件，理所当然网络延迟是主从不同步的绝大多数的原因，特别是跨机房的数据同步出现这种几率非常的大，所以做读写分离，注意从业务层进行前期设计。

2. 主从两台机器的负载不一致
   由于mysql主从复制是主数据库上面启动1个io线程，而从上面启动1个sql线程和1个io线程，当中任何一台机器的负载很高，忙不过来，导致其中的任何一个线程出现资源不足，都将出现主从不一致的情况。

3. 版本不一致

   特别是高版本是主，低版本为从的情况下，主数据库上面支持的功能，从数据库上面不支持该功能



**查看主从不同步情况**

```
mysql> show slave status\G                                                

Slave_IO_Running: Yes
Slave_SQL_Running: No
//NO表明主从不同步
```

**解决方法**

**1.忽略错误，继续同步**

```
stop slave;
//跳过错误 重新同步
set global sql_slave_skip_counter=1;
start slave;
```

**2.重新做主从，完全同步**

```
1.进入主库，锁表，防止数据写入
flush tables with read lock;//锁定为只读状态
2.数据备份到mysql.bak.sql文件
[root@server01 mysql]#mysqldump -uroot -p -hlocalhost > mysql.bak.sql
3.把mysql备份文件传到从库机器，进行数据恢复 使用scp命令
[root@server01 mysql]# scp mysql.bak.sql root@192.168.1.206:/tmp/
4.查看master状态 记录Position和File

mysql> show master status;
+——————-+———-+————–+——————————-+
| File | Position | Binlog_Do_DB | Binlog_Ignore_DB |
+——————-+———-+————–+——————————-+
| mysqld-bin.000001 | 3260 | | mysql,test,information_schema |
+——————-+———-+————–+——————————-+
1 row in set (0.00 sec)

5.停止从库状态
stop slave;
6.执行mysql命令，导入数据备份
mysql> source /tmp/mysql.bak.sql
7.设置主从同步，同步点为File和Position
change master to master_host = ‘192.168.1.206’, master_user = ‘rsync’, master_port=3306, master_password=”, master_log_file = ‘mysqld-bin.000001’, master_log_pos=3260;
8.重新开始从同步
start slave;
```



#### binlog格式导致的问题

<img src="C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200621165116005.png" alt="image-20200621165116005" style="zoom:150%;" />

binlog日记格式

- statement:记录的是修改SQL语句
- row：记录的是每行实际数据的变更   
- mixed：statement和row模式的混合  

Mysql在5.0以前，binlog只支持`STATEMENT`这种格式！而这种格式在**读已提交(Read Commited)**这个隔离级别下主从复制是有bug的，因此Mysql将**可重复读(Repeatable Read)**作为默认的隔离级别！



相关链接

https://mp.weixin.qq.com/s/Xdsw8TzZEBti-ocQmUNJfg

https://blog.csdn.net/qq_36827957/article/details/89145966



binlog为`STATEMENT`格式，**读已提交(Read Commited)**时，有什么bug呢？

假设在RC隔离级别下支持STATEMENT格式的binlog，并且binlog是打开的。

binlog的记录顺序是按照事务commit顺序为序的。

而正常流程是按照时间顺序的。

即master顺序为先删后增 而binlog记录的是先增后删 导致主从不一致。

   

**解决？**

(1)隔离级别设为**可重复读(Repeatable Read)**,在该隔离级别下引入间隙锁。当`Session 1`执行delete语句时，会锁住间隙。那么，`Ssession 2`执行插入语句就会阻塞住！

(2)将binglog的格式修改为row格式，此时是基于行的复制，自然就不会出现sql执行顺序不一样的问题！奈何这个格式在mysql5.1版本开始才引入。因此由于历史原因，mysql将默认的隔离级别设为**可重复读(Repeatable Read)**，保证主从复制不出问题！



### Left Join连接查询

#### Simple Nested-Loop Join 

**简单嵌套循环连接** 不命中索引字段

当左表有n条记录，右表有m条数据时，总匹配次数为 n * m ，当数据库表数据量比较大，由于数据库数据是保存在磁盘中，也就相当于要做很多次的I/O操作。

#### Index Nested-Loop Join

**索引嵌套循环连接** 查询条件必须命中右表索引。

减少左右表总的匹配次数，减少数据库I/O操作，从而降低查询消耗，提高查询效率

#### Block Nested-Loop Join

**缓存块嵌套循环连接** 不命中索引字段

Block Nested-Loop Join通过一次缓存多条数据，将**参与查询的列**添加到join buffer 中，然后使用join buffer中的数据与右表数据进行匹配，从而减少外层的循环次数，虽然查询次数和普通连接查询一样，但是因为是内存操作，性能更好。

当block不够存放大量数据时，分块存放。

![img](https://pic4.zhimg.com/80/v2-13b7fef8c675be086026bf806561e31b_1440w.jpg)

**总结**

可以使用Index Join 但尽量避免使用Block Join，避免扫描行数过多。

使用数据量小的小表做驱动表，减少扫描行数。

### 子查询

子查询就是在一条查询语句中还有其它的查询语句，主查询得到的结果依赖于子查询的结果。

```
SELECT
    SQL_NO_CACHE mm.username
FROM
    mm_member mm
WHERE
    mm.id IN(SELECT ml.member_id FROM mm_log ml WHERE ml.access_time LIKE '%2017-02-06%' GROUP BY ml.member_id);
```

子查询效率太差，执行子查询时，MYSQL需要创建临时表，查询完毕后再删除这些临时表，所以，子查询的速度会受到一定的影响，这里多了一个创建和销毁临时表的过程。

**建议：**既不是用Join也不使用子查询，强烈推荐**分别根据索引单表取数据**，然后在**程序里面做join，merge数据。**



**Cross Join笛卡尔积**

**笛卡尔积就是将A表的每一条记录与B表的每一条记录强行拼在一起**

A n条记录 B m条记录 结果集 n*m条记录

**union union all**

将两个表的结果合并在一起显示出来。

union all直接把结果合并

union 将结果去一次distinct取出重复的记录后的结果

