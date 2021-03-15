```
//查看文件大小

du -h filepath

//将会删除/var/log/httpd/access目录以及其下所有文件、文件夹

rm -rf /var/log/httpd/access

//删除文件

rm file

//查看隐藏文件

ls -al

//下载文件

wget  file

安装：yum install <package_name>
  卸载：yum remove <package_name>
  更新：yum update <package_name>

//查看 ssh进程
rpm -qa |grep ssh
//查看redis进程
 ps -ef | grep redis | grep -v grep

启动redis集群
./redis-cli --cluster create 127.0.0.1:6379 127.0.0.1:6383 127.0.0.1:6380 127.0.0.1:6384 127.0.0.1:6381 127.0.0.1:6382 --cluster-replicas 1
 
//查看redis安装路径
rpm -ql redis-3.2.10-2.el7.x86_64

//启动节点
./redis-server /usr/local/redis/6383/redis.conf


//查看当前所有tcp端口

netstat -ntlp

//关闭防火墙

service iptables stop
service iptables start
service iptables restart
service iptables status
chkconfig iptables off//永久关闭防火墙
				   on//永久关闭后 的重启

//压缩文件 到相应目录

tar -zcvf  java .tar.gz  -c/usr/java

//解压文件

tar -zxvf java .tar.gz

Permission denied
$ sudo chmod -R 777 某一目录

如需要将/home/wwwroot/sinozzz123/music/目录下的1.mp3文件剪切到/home/wwwroot/sinozzz123/abc目录下，执行下面的命令即可：

# mv /home/wwwroot/sinozzz123/music/1.mp3 /home/wwwroot/sinozzz123/abc

2.使用mv命令剪切文件夹。

把/home/wwwroot/sinozzz123/soft文件夹剪切到/home/wwwroot/sinozzz123/abc目录下

# mv /home/wwwroot/sinozzz123/soft /home/wwwroot/sinozzz123/abc

//复制文件夹

cp  -r /home/downloads/phpcms_v9_UTF8/install_package/ /opt/lampp/htdocs/

//搜索关键字 命令模式下
/关键字 n往下查找 N往上查找
```



![image-20200211211727029](C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200211211727029.png)

```
nginx 启动目录再 /usr/local/nginx 
配置文件在 /usr/local/nginx/conf/nginx.conf

启动：进入 /usr/local/nginx/sbin
输入 ./nginx
```

```
实时查看 进程、cpu信息、内存使用
top
简单查看内存使用情况
free

查看进程占用磁盘io信息
iotop 
-o 只显示有io操作的进程
-b 批量显示
-p PID

列出目前所有的正在内存当中的进程
ps axu //有进程占用CPU百分比%CPU 内存占用百分比%MEM
ps -ef //进程占用CPU百分比C
//检查 java进程
ps -ef | grep java

查看端口占用情况
(list of file)
lsof -i 列出所有网络连接信息
lsof -i tcp/udp 列出tcp/udp网络连接信息
lsof -i :端口号 
lsof -i :22查看22端口运行情况


查看tcp udp端口和进程情况
netstat -tunlp | grep 端口号
netstat -ntlp //查看当前所有tcp端口
netstat -ntulp | grep 80 //查看所有80端口
```

![image-20200223224210901](C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200223224210901.png)

![image-20200223224422744](C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200223224422744.png)



```
//输出CPU和磁盘IO信息
iostat
//分析io瓶颈
iostat -x
//查看cpu状态
iostat -c
```

![image-20200223231317877](C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200223231317877.png)

![image-20200223231419746](C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200223231419746.png)

![image-20200223231433964](C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200223231433964.png)

1. 若 %iowait 的值过高，表示硬盘存在I/O瓶颈 

2. 若 %idle 的值高但系统响应慢时，有可能是CPU等待分配内存，此时应加大内存容量 

3. 若 %idle 的值持续低于1，则系统的CPU处理能力相对较低，表明系统中最需要解决的资源是 CPU



```
//监控网卡的实时流量（可以指定网段）、反向解析IP、显示端口信息
iftop
//检测特定网卡
iftop -i eth1
-F 显示特定网段的进出流量
iftop -F 10.10.1.0/24或# iftop -F 10.10.1.0/255.255.255.0
```

![image-20200223232442596](C:\Users\15521\AppData\Roaming\Typora\typora-user-images\image-20200223232442596.png)

1. 使用iftop工具查出来是哪些个IP地址在请求主机的带宽资源，找出耗带宽的元凶

2. 找出耗带宽的IP地址或者段，分析是out方向还是in方向，使用iptables规则来进行控制

中间的< = =>这两个左右箭头，表示的是流量的方向。

TX：发送流量
RX：接收流量
TOTAL：总流量
Cumm：运行iftop到目前时间的总流量
peak：流量峰值
rates：分别表示过去 2s 10s 40s 的平均流量



### 查询日记

```
-n表示带行号
-f表示动态刷新
tail -n 100 mysql.log 查询日记尾部100行内容
tail -n +100 mysql.log 查询日记100行后的内容
tail -nf 100 mysql.log 循环实时查看最后100行日记
配合 grep使用 tail -fn 100 catalina.out | grep   -- '关键字'

head -n 100 mysql.log 查询日记头100行内容
head -n -100 mysql.log 查询日记除了最后100行的内容

由第一行到最后一行连续显示在屏幕上
cat -n test.log |grep "debug"  查找debug内容输出到屏幕上
tac -n test.log 反过来，从最后一行到第一行输出到屏幕上
cat -n test1.log > test2.log 将test1的内容输出到test2里

//根据日期查询 日期必须是日记中打印出来的日记
sed -n '/2014-12-17 16:17:20/,/2014-12-17 16:17:36/p'  test.log
//查看demo.log 300-500行内容
sed -n '300,500p' demo.log
```



### kill

**kill 进程号(默认 kill -15)命令**，系统发送一个SIGTERM信号给对应的程序。

当程序收到signal信号后，可能发生以下事情：

1.程序立刻停止

2.当程序释放相应资源后再停止

3.程序可能仍然继续运行

大部分程序收到SIGTERM信号后，会释放自己的资源，然后再停止。

但是也有程序可能接收信号后，做一些其他的事情（如果程序正在等待IO，可能就不会立马做出响应，我在使用wkhtmltopdf转pdf的项目中遇到这现象）。

即 **SIGTERM** 可以被阻塞 忽略 让程序友好的退出



**kill -9** 进程号 发送一个 SIGKILL命令，SIGKILL强制关闭程序，不会被阻塞。

**小结：** 应该先使用 kill -15 给进程一个清理善后工作的机会，否则可能会留下一些不完整的文件或状态，从而影响服务的再次启动。

kill -15失败后 再使用 kill -9



**修改dns文件**

/etc/resolv.conf

/etc/sysconfig/network-scripts/ifcfg-eth0

**磁盘空间满了，删除日记文件，但是磁盘空间还是满的？**

在Linux或者Unix系统中，通过rm或者文件管理器删除文件将会从文件系统的目录结构上解除链接(unlink).然而如果文件是被打开的（有一个进程正在使用），那么进程将仍然可以读取该文件，磁盘空间也一直被占用。而我删除的是nginx的log文件删除的时候文件应该正在被使用

echo "" > a.txt 字符串替换a文件内容 不写内容表示清空

echo "">>a.txt字符串追加a文件内容

**解决：** 重启服务 or 清空日记  echo >access.log  清空文件至大小为0 truncate -s 0 access.log  