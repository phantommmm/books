![img](https://img-blog.csdnimg.cn/20190425140401529.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0Z1dHVyZV9MTA==,size_16,color_FFFFFF,t_70)

### 缓存与数据库一致性

**经典错误：先删除缓存，然后再更新数据库**

两个并发操作，一个是更新操作，另一个是查询操作，更新操作删除缓存后，查询操作没有命中缓存，先把老数据读出来后放到缓存中，然后更新操作更新了数据库。于是，在缓存中的数据还是老的数据，导致缓存中的数据是脏的，而且还一直这样脏下去了。

#### CAP：降低脏数据概率

- **失效**：应用程序先从cache取数据，没有得到，则从数据库中取数据，成功后，放到缓存中。

- **命中**：应用程序从cache中取数据，取到后返回。

- **更新**：先把数据存到数据库中，成功后，再让缓存失效。

  读的时候，先读缓存，缓存没有的话，就读数据库，然后取出数据后放入缓存，同时返回响应。

  更新的时候，先更新数据库，然后再删除缓存。

**仍然可能出现的问题**

比如，一个是读操作，但是没有命中缓存，然后就到数据库中取数据，此时来了一个写操作，写完数据库后，让缓存失效，然后，之前的那个读操作再把老的数据放进去，所以，会造成脏数据。

但，这个case理论上会出现，不过，实际上出现的概率可能非常低，因为这个条件需要发生在**读缓存时缓存失效，而且并发着有一个写操作**。而实际上数据库的写操作会比读操作慢得多，而且还要锁表，而**读操作必需在写操作前进入数据库操作，而又要晚于写操作更新缓存**，所有的这些条件都具备的概率基本并不大。

#### Write Back

在更新数据的时候，只更新缓存，不更新数据库，而我们的缓存会**异步**地批量更新数据库。这个设计的好处就是让数据的I/O操作飞快无比（因为直接操作内存嘛 ），因为异步，write backg还可以合并对同一个数据的多次操作，所以性能的提高是相当可观的。

**问题：**不能保证强一致性，实现逻辑复杂，不知道哪些数据是更新了的，哪些不是。

**lazy write:**只有当这个缓存需要失效的时候，才会被真正的持久化起来。比如，内存不够了，或是进程退出了等情况。