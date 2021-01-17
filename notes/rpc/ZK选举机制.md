# ZooKeeper选举机制

## 基本概念

**myid** 集群节点id，编号越大在选举中的权重越大

**zxid** 事务id，值越大说明数据越新，在选举中权重越大

**外部投票** 其它节点发来的投票信息

**内部投票** 自身节点的投票信息

**PK** 内部投票和外部投票对比确认是否需要变更内部投票

## 服务器状态

- **LOOKING** 竞选状态。该状态下的服务器认为当前集群中没有Leader，会发起Leader选举
- **FOLLOWING** 跟随者状态。表明当前服务器角色是Follower，并且它知道Leader是谁
- **LEADING** 领导者状态。表明当前服务器角色是Leader，它会维护与Follower间的心跳
- **OBSERVING** 观察者状态。表明当前服务器角色是Observer，与Folower唯一的不同在于不参与选举，也不参与集群写操作时的投票



**observer的作用  **

**能动态扩展zookeeper集群又不会降低写性能。**

如果我们添加多的Follower时，写入性能也会随着下降。这是因为写操作需要（通常）需要集群中至少一半的节点投票达成一致，因此随着更多投票者的加入，投票的成本会显著增加。

观察者不参与投票，只听取投票结果。除了这个简单的区别，Observers的功能与Followers完全相同 - 客户端可以连接到它们并向它们发送读写请求。Observer会像follower一样将消息转发给leader，但是Observer只会听取投票结果，不参与投票。由于这点，我们可以增加任意数量的Observer，同时不会影响我们集群的性能。

Observer还有其它优点。因为他们不投票，所以他们不是ZooKeeper集群的重要组成部分。 因此，它们可以失败，或者与集群断开连接，而不会损害ZooKeeper服务的可用性。

## 选票数据结构

服务器发现票的结果包含以下信息

- **electionEpoch** 逻辑时钟，每个节点会维护一个自增的整数，用于判断多个投票是否在同一论选举中
- **state** 当前节点的状态
- **self_id** 当前节点的myid
- **self_zxid** 当前节点上所保存的最大zxid
- **vote_id** 被推举的节点的myid
- **vote_zxid** 被推举的节点上所保存的数据的最大zxid

## 选举管理

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9pbWcyMDE4LmNuYmxvZ3MuY29tL2Jsb2cvMTcwODk4Ny8yMDE5MDYvMTcwODk4Ny0yMDE5MDYwODIzMTAxNTI2My0xNzk2NDQ5NjUxLnBuZw?x-oss-process=image/format,png)

**sendqueue**：选票发送队列，用于保存待发送的选票。

**recvqueue**：选票接收队列，用于保存接收到的外部投票。

**WorkerReceiver**：选票接收器。其会不断地从QuorumCnxManager中获取其他服务器发来的选举消息，并将其转换成一个选票，然后保存到recvqueue中。

**WorkerSender**：选票发送器，不断地从sendqueue中获取待发送的选票，并将其传递到底层QuorumCnxManager中。

**QuorumCnxManager**：每个节点启动时，会启动一个QuorumCnxManager负责各台服务器之间的网络通信

**注意 以上结构每个节点分别维护一份 以下结构对多个节点维护多个队列**

**recvQueue**：消息接收队列，用于存放那些从其他服务器接收到的消息。

**queueSendMap**：消息发送队列，用于保存那些待发送的消息，按照SID进行分组。

**senderWorkerMap**：发送器集合，每个SenderWorker消息发送器，都对应一台远程Zookeeper服务器，负责消息的发送，也按照SID进行分组。

**lastMessageSent**：最近发送过的消息，为每个SID保留最近发送过的一个消息。

## 连接建立

QuorumCnxManager在启动时会创建一个ServerSocket监听其它节点的连接请求。为了避免两台机器之间重复地创建TCP连接，Zookeeper只允许SID大的服务器主动和其他机器建立连接，否则断开连接。

在接收到连接请求后，服务器通过对比自己和远程服务器的SID值来判断是否接收连接请求，如果自己比远程服务器SID值大，那么会断开当前连接，然后自己主动和远程服务器建立连接。一旦连接建立，就会根据远程服务器的SID来创建相应的消息发送器SendWorker和消息接收器RecvWorker，并启动。

## 消息发送与接收

**消息接收**：由消息接收器RecvWorker负责，由于Zookeeper为每个远程服务器都分配一个**单独的RecvWorker**，因此，每个RecvWorker只需要不断地从这个TCP连接中读取消息，并将其保存到recvQueue队列中。

**消息发送**：由于Zookeeper为每个远程服务器都分配一个**单独的SendWorker**，因此，每个SendWorker只需要不断地从对应的消息发送队列中获取出一个消息发送即可，同时将这个消息放入lastMessageSent中。在SendWorker中，一旦Zookeeper发现针对当前服务器的消息发送队列为空，那么此时需要从lastMessageSent中取出一个最近发送过的消息来进行再次发送，这是为了解决接收方在消息接收前或者接收到消息后服务器挂了，导致消息尚未被正确处理。同时，Zookeeper能够保证接收方在处理消息时，会对重复消息进行正确的处理。

## 选举算法

默认使用基于TCP的FastLeaderElection算法，**投票数**大于一半数则胜出，注意是投票数不是投票获得数。

### 初始化选票

在服务器初始化时，每个服务器节点都是投票给自己，并且把投票信息广播出去直到选出Leader

### 发送投票信息

每次投票自己后，都会进行广播信息，这部可以理解为发送投票信息给其它节点

### 接收外部投票

服务器会尝试从其它服务器获取投票，并记入自己的投票箱内。如果无法获取任何外部投票，则会确认自己是否与集群中其它服务器保持着有效连接。如果是，则再次发送自己的投票信息；如果否，则马上与之建立连接。

### 判断选举轮次

收到外部投票后，根据electionEpoch判断

- 外部投票的electionEpoch大于自己的electionEpoch，立即清空自己的投票箱并将自己的electionEpoch更新为收到的electionEpoch，然后进行选票**PK**，最终再次将自己的投票信息广播出去。
- 外部投票的electionEpoch小于自己的electionEpoch，当前服务器直接忽略该投票，继续处理下一个投票。
- 外部投票的electionEpoch与自己的相等，进行选票PK。

### 选票PK

PK基于electionEpoch、zxid、myid

- 外部投票的electionEpoch大于自己的electionEpoch，则将自己的electionEpoch及自己的选票的electionEpoch变更为收到的electionEpoch
- 若electionEpoch一致，则对比二者的vote_zxid，若外部投票的vote_zxid比较大，则将自己的票中的vote_zxid与vote_myid更新为收到的票中的vote_zxid与vote_myid并广播出去，另外将收到的票及自己更新后的票放入自己的票箱。如果票箱内已存在(self_myid, self_zxid)相同的选票，则直接覆盖
- 若二者vote_zxid一致，则比较二者的vote_myid，若外部投票的vote_myid比较大，则将自己的票中的vote_myid更新为收到的票中的vote_myid并广播出去，另外将收到的票及自己更新后的票放入自己的票箱

### 统计选票

完成投票放入票箱后，进行统计，若有过半节点投给自己，则终止投票，否则继续接收其它节点的投票

### 更新状态

结束统计选票后，更新状态，若过半节点投票给自己，则将自己更新为Leader否则为Follower

## 进入选举情况

- 节点刚刚启动，会进入Looking状态
- Follower监测超时，会进入Looking状态（Follower未收到Leader心跳，Follower节点进入Looking）
- Leader未收到过半节点的心跳，会进入Looking状态（可能是Leader节点与Follower节点产生网络分区）

## 选举场景

### 服务器启动初始化

每个服务器在启动时都会选择自己做为领导，然后将投票信息发送出去，循环一直到选举出领导为止。

> 目前有5台服务器，每台服务器均没有数据，它们的编号分别是1,2,3,4,5,按编号依次启动，它们的选择举过程如下：

1. 服务器1启动，给自己投票，然后发投票信息，由于其它机器还没有启动所以它收不到反馈信息，服务器1的状态一直属于Looking。
2. 服务器2启动，给自己投票，同时与之前启动的服务器1交换结果，由于服务器2的编号大所以服务器2胜出，但此时投票数没有大于半数，所以两个服务器的状态依然是LOOKING。
3. 服务器3启动，给自己投票，同时与之前启动的服务器1,2交换信息，由于服务器3的编号最大所以服务器3胜出，此时投票数正好大于半数，所以服务器3成为领导者，服务器1,2成为小弟。
4. 服务器4启动，给自己投票，同时与之前启动的服务器1,2,3交换信息，尽管服务器4的编号大，但之前服务器3已经胜出，所以服务器4只能成为小弟。
5. 服务器5启动，后面的逻辑同服务器4成为小弟。

### Leader宕机

假设现在有三个节点 1、2是Follower 3是Leader

**1.** Leader（服务器3）宕机后，Follower（服务器1和2）发现Leader不工作了，因此进入LOOKING状态并发起新的一轮投票，并且都将票投给自己。

**2.** 服务器1和2根据外部投票确定是否要更新自身的选票。这里有两种情况

- 服务器1和2的zxid相同。例如在服务器3宕机前服务器1与2完全与之同步。此时选票的更新主要取决于myid的大小
- 服务器1和2的zxid不同。在旧Leader宕机之前，其所主导的写操作，只需过半服务器确认即可，而不需所有服务器确认。换句话说，服务器1和2可能一个与旧Leader同步（即zxid与之相同）另一个不同步（即zxid比之小）。此时选票的更新主要取决于谁的zxid较大

**3.** 上一步票选后，投票出新leader。

**4.** 当旧Leader恢复后，进入Looking状态并发起票选，与2和3同步票选信息后，了解到有Leader后自己进入Follower状态