# ZAB协议

ZAB是ZK使用的一致性协议，支持 **奔溃恢复** 的 **原子广播协议**

## 主备模式

基于该协议，ZK实现 **主备模式** 保证集群中个节点 **数据一致性**

![img](https://upload-images.jianshu.io/upload_images/11474088-1b674989b9cd8d7e.png?imageMogr2/auto-orient/strip|imageView2/2/w/768/format/webp)

主备模式即只有一个主进程Leader负责处理外部写请求，然后数据同步到其它Follower节点，从而保证数据一致性。

**注意 只有写请求需要同步，读请求直接从当前节点获取即可**

## 原子广播

整个原子广播过程类型 **二阶段提交过程** ，对于客户端发送的 **写请求** ，全部由 Leader 接收，Leader 将请求封装成一个 **事务 Proposal** ，将其发送给所有 Follwer ，然后，根据所有 Follwer 的反馈，如果超过半数成功响应，则执行  **commit 操作**（先提交自己，再发送 commit 给所有 Follwer）。



主要分为三个步骤，保证集群节点数据一致性。

**1.数据同步到Follower节点**

![img](https://upload-images.jianshu.io/upload_images/11474088-d3d1eb094e3cdc55.png?imageMogr2/auto-orient/strip|imageView2/2/w/711/format/webp)

**2.等待Follower节点回应ACK，至少超过半数则成功**

![img](https://upload-images.jianshu.io/upload_images/11474088-56b7b9cd5c535ad8.png?imageMogr2/auto-orient/strip|imageView2/2/w/720/format/webp)

**3.当超过半数成功回应，则执行commit操作**

![img](https://upload-images.jianshu.io/upload_images/11474088-b1b8c94ba2a1e525.png?imageMogr2/auto-orient/strip|imageView2/2/w/727/format/webp)

**细节**

**1.** 每个封装的事务拥有一个全局递增的ID（ZXID）,ZAB协议需要保证事务的顺序，因此每个事务按照ZXID先后处理。

**2.** 在Leader和每个Follower之间维护一个消息队列进行收发消息，Leader和Follower只需往队列中发消息即可，做到异步解耦。

**3.** 即时是Follower节点接收到写请求，也会转发到Leader后进行处理。

**4.收到过半ACK即可是最小条件，即其它节点仍然在同步数据 除非节点宕机，而节点宕机的话 重新加入时也会进行数据同步**

**5.数据同步 应该是同步所有数据包括之前未同步的数据**

## 奔溃恢复

**消息广播过程中，Leader奔溃怎么办？**

当Leader崩溃时，进入崩溃恢复阶段

### 原则

ZAB定义两个前提原则

1. ZAB 协议确保那些**已经**在 Leader 提交的事务最终会被所有服务器提交。 
2. ZAB 协议确保丢弃那些只在 Leader 提出/复制，但没有提交的事务。

通过上面两个原则 **确保已经被Leader提交的事务，并且丢弃未被提交的事务。**

### 选举算法

为了保证上面两个原则，ZAB设计如下选举算法

**将集群节点中拥有ZXID最大的节点作为新Leader**

这样 **可以省去 Leader 服务器检查事务的提交和丢弃工作的这一步操作。**

### ZXID

![img](https://upload-images.jianshu.io/upload_images/11474088-5fc4a803803f95fe.png?imageMogr2/auto-orient/strip|imageView2/2/w/523/format/webp)

ZXID前32位看作是LeaderID,后32位看作简单自增的事务ID

每次Leader变更，LeaderID+1，事务ID归0，因此当旧的Leader奔溃后，Follower就不会听它的了，因为Follower只服从LeaderID最高的Leader。

这样做的目的是 **有效避免不同Leader使用相同ZXID编号造成异常情况**

### 数据同步

当崩溃恢复之后，需要在正式工作之前（接收客户端请求），完成数据同步，只有集群中过半节点同步完成，Leader才成功真正的Leader，具体过程如下

**注意 下面的epoch 理解为上面的 LeaderID**

**1**. 所有Follower向准Leader发送自己最后接收的事务的epoch

**2**. 当Leader收到过半epoch后，准Leader选取最大的epoch，加1得到e1，将e1 发送给Follower

**3.** Follower收到准Leader发送的epoch 值之后，与自己的epoch 值作比较，若小于，则将自己的epoch 更新为e1，并向准leader 发送反馈ACK信息（epoch 信息、历史事务集合）（注意 只有小于才会发送ACK表明节点未同步）

**4.** 准Leader接收到ACK 消息之后，会在所有历史事务集合中选择其中一个事务集合ZXID最大作为新的事务集合

**5.**  Leader服务器会为每一个Follower服务器准备一个队列，并将最新的事务集合以Proposal的形式并且附上commit消息（表明事务已提交）发送给**未同步**的Follower进行同步。

**6.** Follower收到后，初始化事务，返回ACK

**7.** Leader收到过半ACK后，发送commit消息

**8.** Follower收到commit消息后，提交事务。

#### 同步方式

在完成Leader选举后进行数据同步，在开始数据同步之前，Leader服务器会进行数据同步初始化，取出提交队列 `proposals`，同时完成对以下三个ZXID值的初始化。

- **peerLastZxid：该 `Learner` 服务器最后处理的ZXID。**
- **minCommittedLog：Leader服务器提议缓存队列committedLog中的最小ZXID。**
- **maxCommittedLog：Leader服务器提议缓存队列committedLog中的最大ZXID。**

Leader和Learner服务器之间的数据差异情况来决定最终的数据同步方式。

**差异化同步（DIFF）**

`peerLastZxid介于minCommittedLog和maxCommittedLog之间`

Leader服务器会首先向这个Learner发送一个DIFF指令，用于通知Learner“进入差异化数据同步阶段，然后将差异的数据使用二阶段提交与Learner进行同步。

**回滚化同步（TRUNC）**

`peerLastZxid大于maxCommittedLog`

这种场景其实就是上述先回滚再差异化同步的简化模式，Leader会要求Learner回滚到ZXID值为maxCommitedLog对应的事务操作。

**先回滚再差异化同步（TRUNC+DIFF）	**

当Leader发现某个Learner包含了一条自己没有的事务记录，那么就需要该Learner进行事务回滚，回滚到Leader服务器上存在的，同时也是最接近于peerLastZxid的ZXID。

**全量同步(SNAP)**

- **场景1：peerLastZxid小于minCommittedLog。**
- **场景2：Leader服务器上没有提议缓存队列，peerLastZxid不等于lastProcessedZxid（Leader服务器数据恢复后得到的最大ZXID）。**

Leader无法直接使用提议缓存队列和Learner进行同步，因此只能进行全量同步。Leader将本机的全量内存数据同步给Learner。Leader首先向Learner发送一个SNAP指令，通知Learner即将进行全量同步，随后，Leader会从内存数据库中获取到全量的数据节点和会话超时时间记录器，将他们序列化后传输给Learner。Learner接收到该全量数据后，会对其反序列化后载入到内存数据库中。

## 一致性

**顺序一致性**

ZK文档中明确表明它是 **顺序一致性** 例子如下

ZK针对同一个Follower A提交的写请求R1，R2，某些Follower虽然可能不能在请求提交后立即看到（强一致性），但经过自身与Leader同步后，这些Follower看到这两个请求顺序一定是 R1,R2，即顺序一致性。

**线性一致性**

写操作看起来是瞬时的，一旦写入以后（”以后“依据为时钟），读操作应当返回该写入的值或以后再次写入的值（可以简单理解为volatile）

ZK提供 `sync` 同步命令，即同步节点数据，当使用该命令后便变成 **线性一致性**

## 问题

**为什么最好使用奇数台服务器构成 ZooKeeper 集群？**

我们知道在Zookeeper中 Leader 选举算法采用了Zab协议。Zab核心思想是当多数 Server 写成功，则任务数据写成功。

①如果有3个Server，则最多允许1个Server 挂掉。

②如果有4个Server，则同样最多允许1个Server挂掉。

既然3个或者4个Server，同样最多允许1个Server挂掉，那么它们的可靠性是一样的，所以选择奇数个ZooKeeper Server即可，这里选择3个Server。