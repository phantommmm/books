# Nginx

<img src="C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200122151842346.png" alt="image-20200122151842346" style="zoom:67%;" />

**TCP层：**进程与进程间的通讯

**IP层：**机器与机器直接如何找到的关系

### 组成

**Nginx二进制可执行文件：**由各模块源码编译出的一个文件

**Nginx.conf:**控制nginx的行为

**access.log访问日记：**记录每一条http请求信息

**error.log:**错误日记 定位问题

### nginx服务的进程

nginx是多进程结构，是为了保证nginx的高可用可靠。

**master进程：**父进程，负责管理worker进程

**worker进程：**子进程，worker进程用来处理具体的请求的，worker进程一般配置成与服务器的CPU核数相同。

**cache进程：**子进程，包括cache manager和cache loader进程，主要是反向代理时做缓存使用。

### 命令

nginx -s start

nginx -s reload：修改配置文件后 平滑启动  不会停止服务 等于向master发送信号 kill -HUP 端口号

nginx -s stop 立即停止服务

nginx -s quit 有序的停止服务

nginx -s reopen重新开始记录日记文件

<img src="C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200122145950560.png" alt="image-20200122145950560" style="zoom:50%;" />

<img src="C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200122150348684.png" alt="image-20200122150348684" style="zoom:67%;" />

master开启新的进程，旧的master就没了。新旧worker进程会共存直到旧worker结束。

<img src="C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200122151124790.png" alt="image-20200122151124790" style="zoom: 67%;" />

### 热部署升级

<img src="C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200122151209277.png" alt="image-20200122151209277" style="zoom:67%;" />

<img src="C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200122151241327.png" alt="image-20200122151241327" style="zoom:67%;" />

**概念：**nginx升级（即升级二进制文件），如果不热部署，nginx需要重新启动，这样会使正在访问nginx的连接断开，实现在线升级。

**原理：**替换新的master进程，保持还有连接的老worker进程，等到它所有连接完成，再kill掉。新的连接请求由新的worker进程完成，即新旧版本可以同时存在。

nginx -s reload:使修改的配置生效，配置生效是平滑的，不会对访问产生任何影响
reload后会启动新的进程接受新请求，对于未处理完的请求还是用老的配置，直到请求处理完毕，老的进行会退出。启动新的worker进程

**步骤：**

1.备份二进制文件 进入nginx目录下sbin目录下执行命令

cp nginx nginx.old

2.把编译好的新版nginx的二进制文件覆盖旧版本（nginx nginx.old需要在同一目录下）

​	cp -r nginx /usr/local/src/nginx/sbin/ -f

3.查看正在运行的老版本nginx进程号

ps -ef|grep nginx

4.通知运行中的老进程将任务过渡给新的nginx进程

kill -USR2 53713(旧的nginx进程号)

```
1. 该命令执行后会首先会重新调用原命令创建一个新的Nginx进程,但因为我们已经更换了sbin目录下的Nginx文件,所以其实质上使用的是新版本的Nginx
2. 新的Nginx进程创建后,老的Nginx进程和子进程不会结束,但已经不再监控相关端口,实质上已经停止了相关工作,新的工作由新的Nginx进程接管
```

5.老版本死去 关闭worker进程

kill -WINCH 53713

```
1. 老的Nginx任务工作进程(worker)关闭,但主进程(master)并不会关闭,其目的是方便回滚,可以使用reload重启老的进程
2. 新的任务由新的进程完成
```

### 更新失败后版本回滚

kill -HUP 53713（旧的进程号）//平滑升级，重新加载配置文件 拉起旧版本的worker进程

kill -USR2 15151//让新版本的master进程不接受请求

kill -WINCH 15151（新版本进程号）//关闭新版本的worker进程

ps -ef|grep nginx查看nginx进程号

### 日记切割

因为access.log日记会保存所有客户端连接信息，普通网址每天请求量很大，所以每天都要切割

du -sh access.log//查看日记大小

nginx -s reopen//重新生成一个access.log日记

### 缓存静态资源

<img src="http://wangxiaokai.vip/images/2018-05-09-nginx-cache/1.png" alt="nginx缓存图示" style="zoom: 50%;" />

蓝：第一次获取  nginx将静态资源缓存到nginx服务器本地

红：第二次获取 nginx直接从本地返回静态资源

![img](https://img-blog.csdnimg.cn/20190305162840747.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQxMjAxODE2,size_16,color_FFFFFF,t_70)

**http层设置**

```
http{	
    proxy_connect_timeout 10;//服务器连接的超时时间
        proxy_read_timeout 180;//连接成功后，等待后端服务器响应时间
        proxy_send_timeout 5;//后端服务器数据回传时间
        proxy_buffer_size 16k;//缓存区大小
        proxy_buffers 4 32k;//每个连接设置缓冲区的数量 每块缓冲区的大小
        proxy_busy_buffers_size 96k;
        //开启缓冲响应的功能以后，在没有读到全部响应的情况下，写缓冲到达一定大小时，nginx一定会向客户端发送响应，直到缓冲小于此值。
        proxy_temp_file_write_size 96k;//设置nginx每次写数据到临时文件的size(大小)限制
        proxy_temp_path /tmp/temp_dir;//从后端服务器接收的临时文件的存放路径
        proxy_cache_path /tmp/cache levels=1:2 keys_zone=cache_one:100m inactive=1d max_size=10g;//设置缓存的路径和其他参数。被缓存的数据如果在inactive参数（当前为1天）指定的时间内未被访问，就会被从缓存中移除
```

**server层设置**

```
server {
        listen       80 default_server;
        server_name  localhost;
        root /mnt/blog/;

        location / {

        }

        #要缓存文件的后缀，可以在以下设置。
        location ~ .*\.(gif|jpg|png|css|js)(.*) {
                proxy_pass http://ip地址:90;//nginx缓存里拿不到资源，向该地址转发请求，获取资源并缓存
                proxy_redirect off;//设置后端服务器“Location”响应头和“Refresh”响应头的替换文本
                proxy_set_header Host $host;//允许重新定义或者添加发往后端服务器的请求头
                proxy_cache cache_one;//指定用于页面缓存的共享内存，对应http层设置的keys_zone
                proxy_cache_valid 200 302 24h;//为不同的响应状态码设置不同的缓存时间
                proxy_cache_valid 301 30d;
                proxy_cache_valid any 5m;
                expires 90d;//缓存时间
                add_header wall  "hey!guys!give me a star.";//在header里设置自定义消息
                //如果返回的是缓存 则有该消息头
        }
    }
```

### 代理

<img src="https://images2018.cnblogs.com/blog/1120165/201809/1120165-20180905232339438-913760288.png" alt="img" style="zoom:67%;" />

**正向代理代理客户端（服务端不知道客户端真正ip），反向代理代理服务器（客户端不知道服务端真正ip）**

#### 正向代理

<img src="https://images2018.cnblogs.com/blog/1120165/201807/1120165-20180730224449157-560730759.png" alt="img" style="zoom:67%;" />

#### 反向代理

<img src="https://images2018.cnblogs.com/blog/1120165/201807/1120165-20180730224512924-952923331.png" alt="img" style="zoom:67%;" />

```
server {
        listen       80;
        server_name  www.123.com; 

        location / {
            proxy_pass http://127.0.0.1:8080;
            index  index.html index.htm index.jsp;
        }
    }
    通过反向代理，原本要通过127.0.0.1:8080访问 现在只需通过 www.123.com:80即可
    输入 www123.com:80 会跳到 127.0.0.1:8080
```

### 负载均衡

<img src="https://images2018.cnblogs.com/blog/1120165/201809/1120165-20180908131700302-1529457842.png" alt="img" style="zoom:67%;" />

#### 普通轮询算法

通过访问 localhost:80 nginx会轮询访问 127.0.0.1：8080/8081

```
upstream OrdinaryPolling {
    server 127.0.0.1:8080;
    server 127.0.0.1:8081;
    }
    server {
        listen       80;
        server_name  localhost;
    }
```

#### 基于比例加权轮询

和上面差不多，主要是8080访问权重为5/7 8081为2/7

```
upstream OrdinaryPolling {
    server 127.0.0.1:8080 weight=5;
    server 127.0.0.1:8081 weight=2;
    }
    server {
        listen       80;
        server_name  localhost;
    }
```

#### 最少连接

```
upstream  dalaoyang-server {
       least_conn;
       server    localhost:10001 weight=1;
       server    localhost:10002 weight=2;
}
```



#### 基于IP路由负载

**集群环境下的session共享问题？**

用户第一次进入一个系统是需要进行登录身份验证的，首先将请求跳转到Tomcat1服务器进行处理，登录信息是保存在Tomcat1 上的，这时候需要进行别的操作，那么可能会将请求轮询到第二个Tomcat2上，那么由于Tomcat2 没有保存会话信息，会以为该用户没有登录，然后继续登录一次，如果有多个服务器，每次第一次访问都要进行登录，这显然是很影响用户体验的。

**方法：**

　1、第一种方法是选择一个中间件，将登录信息保存在一个中间件上，这个中间件可以为 Redis 这样的数据库。那么第一次登录，我们将session 信息保存在 Redis 中，跳转到第二个服务器时，我们可以先去Redis上查询是否有登录信息，如果有，就能直接进行登录之后的操作了，而不用进行重复登录。

　2、第二种方法是根据客户端的IP地址划分，每次都将同一个 IP 地址发送的请求都分发到同一个 Tomcat 服务器，那么也不会存在 session 共享的问题。（nginx ip路由负载）

```
upstream OrdinaryPolling {
    ip_hash;//IP路由负载：同一个ip分配到同一个服务器
    server 127.0.0.1:8080 weight=5;
    server 127.0.0.1:8081 weight=2;
    }
    server {
        listen       80;
        server_name  localhost;
    }
```

#### 基于服务器响应时间负载分配

根据服务器处理请求的时间来进行负载，处理请求越快，也就是响应时间越短的优先分配。

```
upstream OrdinaryPolling {
    server 127.0.0.1:8080 weight=5;
    server 127.0.0.1:8081 weight=2;
    fair;//响应时间负载分配
    }
    server {
        listen       80;
        server_name  localhost;
    }
```

#### 对不同域名实现负载均衡

通过配合location 指令块我们还可以实现对不同域名实现负载均衡。

```
upstream wordbackend {
    server 127.0.0.1:8080;
    server 127.0.0.1:8081;
    }
//上游 即最终的访问服务器
    upstream pptbackend {
    server 127.0.0.1:8082;
    server 127.0.0.1:8083;
    }

    server {
        listen       80;
        server_name  localhost;//代理服务器

        location /word/ {
            proxy_pass http://wordbackend;
            index  index.html index.htm index.jsp;
         }
       
    location /ppt/ {
            proxy_pass http://pptbackend;
            index  index.html index.htm index.jsp;
        }
    }
```

### 请求切换

![image-20200122222743405](C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200122222743405.png)

http请求在服务器处理中一般分为3段：第一段header 第二段body 第三段响应

服务器先处理header 查看content-length有无body

接着处理body

最后返回响应

在通常的服务器如apache等是采用的阻塞机制，即一个进程同时只处理一个请求。容易造成进程间切换的耗时。（用户态到内核态切换耗时）

nginx直接在同一进程里执行，就算失败就直接处理其它请求，即无需从用户态到内核态进行切换，降低了性能损耗

如图，如果process1 处理 header请求网络事件不满足（即请求失败了，不接着往下执行）,则切换到process2 去执行绿色请求（其它请求 不同于蓝色请求了）,如果绿色请求依然不满足,就会切换到process3 去执行橙色请求. 

每个进程之间切换需要消耗5ms时间,当请求并发增加时,消耗时间成指数性增加,进程之间切换的消耗就不可忽视.容易导致阻塞.

nginx 在时间片时间内(在5ms-800ms之间),当请求网络事件不满足时,nginx直接在当前process内切换请求,直到分配的时间片消耗完毕. 所以一般我们会在nginx配置时,把worker的优先级调到最高-19,(优先级数字在-20~20之间,-20为最高).从而使worker 分配的时间片尽可能多, 从而尽可能少的切换process,让cpu少做无用功.

### 四层负载均衡（NAT）

![img](https://img2018.cnblogs.com/blog/1479216/201810/1479216-20181008222406971-647294688.png)

四层工作在OSI模型第四层，传输层(TCP)。

基于修改 IP+端口 对报文进行转发，并记录下该TCP/UDP由哪台服务器处理的。后续这个连接所有流量都同样的转发到同一台服务器。

无法修改或判断请求资源的具体类型。

### 七层负载均衡

七层工作在OSI模型第七层，应用层(URL、Cookie)。

在四层基础上，增加条件，除了对IP+端口进行处理转发，还可以根据**浏览器类别、语言**判断负载均衡。例如WEB服务器分为中文和英文，则七层则根据用户访问时自动辨别用户语言分发给对于语言服务器处理。

基于请求的应用层信息进行转发，读取并解析http请求内容。

可以根据请求的资源类型分配到不同的服务器。

**特点**

根据流经的数据类型（图像文件、压缩文件、多媒体文件格式等），把数据转发到对应的服务器处理。

根据连接请求的类型（普通文本、图像等静态资源还是动态资源），把数据转发到对应的服务器处理。

### 其它负载均衡

**二层负载均衡：**通过一个虚拟MAC地址接受请求，在转发到真实的MAC地址。

**三层负载均衡：**通过一个虚拟IP地址接受请求，分发到真实的IP地址。

**四层：**IP+端口

**七层：**URL或主机名

### KeepAlive

![img](https://img-blog.csdn.net/20180827173301248?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTAwMjAwOTk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

用于nginx实现故障转移。

在 Keepalived服务正常工作时，主 Master节点会不断地向备节点发送（多播的方式）心跳消息，用以告诉备Backup节点自己还活着，当主 Master节点发生故障时，就无法发送心跳消息，备节点也就因此无法继续检测到来自主 Master节点的心跳了，于是调用自身的接管程序，接管主Master节点的 IP资源及服务。而当主 Master节点恢复时，备Backup节点又会释放主节点故障时自身接管的IP资源及服务，恢复到原来的备用角色。

**启动keepalived节点会自动启动对应的Nginx,无需手动启动nginx**