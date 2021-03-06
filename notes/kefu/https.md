# HTTPS

https用SSL/TLS协议协商出的密钥加密明文的http数据,保证安全性

## Https是怎么加密的

**加密流程**

1.发起请求：客户端在通过TCP而服务器建立连接之后，默认使用443端口发送一个请求证书消息给服务器，该该请求消息里面包含自己可以实现的算法列表和其他需要的消息。

2.证书返回：服务器端收到消息回应客户端并返回证书，在证书中包含服务器信息，域名申请证书的公司公钥数据加密算法等。

3.验证客户端在收到证书后**判断证书签发机构是否正确**，使用该签发机构的**公钥确认签名是否有效**，客户端还会确保在证书中列出的域名为正在连接的域名。如果客户端确认证书有效，则生成对称秘钥，并使用公钥将对称秘钥加密。

4.密钥交换：户端将加密后的对称密钥发送给服务器服务器在接收到对称密钥后，使用私钥解密。

5.数据传输：经过上述步骤，客户端和服务器就完成了秘钥对的交换。在之后的数据传输过程中，客户端和服务端就可以基于对称加密。将数据加密后在网络上传输，保证网络数据传输的安全性。

## SSL握手阶段

SSL/TLS握手阶段分为五步：

第一步，客户端给出协议版本号、一个客户端生成的随机数（Client random），以及客户端支持的加密方法。
第二步，服务器确认双方使用的加密方法，并给出数字证书、以及一个服务器生成的随机数（Server random）。
第三步，**客户端确认数字证书有效**，然后生成一个新的随机数（Premaster secret），并使用数字证书中的公钥，加密这个随机数，发给服务端。
第四步，服务器使用自己的私钥，获取客户端发来的随机数（即Premaster secret）。
第五步，客户端和服务器根据约定的加密方法，使用前面的三个随机数，生成"对话密钥"（session key），用来加密接下来的整个对话过程。

![img](http://www.ruanyifeng.com/blogimg/asset/2014/bg2014092003.png)

```
（1）生成对话密钥一共需要三个随机数。

（2）握手之后的对话使用"对话密钥"加密（对称加密），服务器的公钥和私钥只用于加密和解密"对话密钥"（非对称加密），无其他作用。

（3）服务器公钥放在服务器的数字证书之中。
```

非对称加密：加密 解密用的不是同一个密匙

对称加密：加密 解密用的是用一个密匙	

**CA证书：证书授权中心** 用于证明自己是哪个服务器，自己不是伪造的，安不安全等

**CA证书在SSL中的作用？**

客户端需要对服务端的证书进行检查，如果证书不是可信机构颁布、或者证书中的域名与实际域名不一致、或者证书已经过期，就会向访问者显示一个警告，由其选择是否还要继续通信。如果证书没有问题，客户端就会从服务器证书中取出服务器的公钥(**公钥从证书中获取,证书的正确性由CA保证**)



**证书的申请过程**

<img src="https://img-blog.csdn.net/20170804162718409?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdXN0Y2N3/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast" alt="证书工作流" style="zoom:50%;" />

**认证流程**

1、服务器生成一对密钥，私钥自己留着，公钥交给数字证书认证机构（CA）
2、CA进行审核，并用**CA自己的私钥**对服务器提供的公钥**进行签名生成数字证书**
3、在https建立连接时，客户端从服务器获取数字证书，用**CA的公钥（根证书）**对数字证书进行验证，比对一致，说明该数字证书确实是CA颁发的（得此结论有一个前提就是：客户端的CA公钥确实是CA的公钥，即该CA的公钥与CA对服务器提供的公钥进行签名的私钥确实是一对。），而CA又作为权威机构保证该公钥的确是服务器端提供的，从而可以确认该证书中的公钥确实是合法服务器端提供的。



## 数字证书CA作用

利用数字证书CA验证公钥是否属于服务器，证书包括了私钥加密后的签名和公钥以及其它信息

**1.**对明文信息进行Hash得到信息摘要

**2.** 利用证书里的公钥对签名进行解密

**3.** 对比两个摘要、验证其它信息（有效时间、是否吊销等）

从而验证公钥是否属于服务器

## 签名的作用

直接将服务端的公钥发送给第三方机构，使用私钥加密不就可以了吗？但是我们要知道，谁都可以去第三方机构申请证书的，而第三方机构都是使用的一对私钥和公钥。假设我们的中间人也去申请了，这个时候中间人可以截取到服务端的返回证书，然后更换为自己的，之后再发给客户端。而由于第三方也是合法的申请证书的人，所以客户端解密成功，利用中间人的公钥进行加密，之后发送到服务端时，中间人截取之后，利用自己的私钥解密获取到信息。

## 为什么数据传输不使用对称加密

**1.** 服务端首先发送密匙给客户端

**2.** 服务端和客户端可以用该密匙对消息进行加解密

**问题 密匙发送过程中被第三方拦截**

之后第三方可以拦截信息并根据密匙进行解密，安全性低。



## 非对称性加密安全在哪

**1.** 客户端和服务端各拥有一把私匙、公匙。公钥加密、私钥解密，私钥加密、共钥解密。

**2.** 非对称性加密传递私钥、对称性加密传输数据。

**3.** 利用数字证书CA验证公钥是否属于服务器

**存在的问题**

这种方式同样存在在传输**公钥**的过程中被中间人获取的风险，但相比对称加密分配不同秘钥给客户端的方式，非对称加密至少可以保证中间人是无法破解客户端通过公钥加密的内容，因为私钥只保存在服务器端，只有私钥可以破解公钥加密的内容。



## 浏览器怎么验证https证书的合法性

1.**验证证书是否过期**

证书中包含有效时间起使和截至时间

**2.验证证书是否已被吊销**

OCSP在线证书状态检查协议，应用按照标准发送一个请求，对某张证书进行查询，之后服务器返回证书状态。

**3.验证证书是否上级CA签发**

浏览器内置了信任的根证书，就是看看web服务器的证书是不是这些信任根发的或者信任根的二级证书机构颁发的。

每一张证书都是由上级CA证书签发的，上级CA证书可能还有上级，最后会找到根证书。根证书即自签证书，自己签自己。
当你验证一张证书是否是由上级CA证书签发的时候，你必须有这张上级CA证书。通常这张证书会内置在浏览器或者是操作系统中，有些场景下应用系统也会保留。

## 为什么握手阶段使用非对称加密而通信时使用对称加密?

因为非对称加密加密解密算法效率较低，不适合客户端和服务器端这样高频率的通信过程，在某些极端情况下，甚至能比非对称加密慢上1000倍。

非对称加密的优势在于它可以很好帮助完成秘钥的交换，所以前期交换秘钥必须使用非对称加密算法。

## 为什么使用HTTPS

HTTP 协议无法加密数据，所有通信数据都在网络中明文“裸奔”，这是导致数据泄露、数据篡改、流量劫持、钓鱼攻击等安全问题的重要原因。而 HTTPS 是用来解决 HTTP 明文协议的缺陷，在 HTTP 的基础上加入 SSL/TLS 协议，依靠 SSL 证书来验证服务器的身份，为客户端和服务器端之间建立“SSL”通道，确保数据运输安全。

最严重的是，运营商劫持HTTP，嵌入广告，或者整个拦截掉，甚至于将页面重定向到其它页面，这样的话是能难解决的，除非通过诉讼，所以使用https才是长久之计。

并且使用https的话是不允许有http资源的，所以想在https中添加广告等的话也必须使用https，所以就可以避免很多内嵌的广告、流量统计之类的东西。

## 那为什么还是大多数使用HTTP？

1. **SSL 证书费用：**不少用户觉得开启 HTTPS 要申购 SSL 证书，每年要在证书上花费不菲的费用。
2. **HTTPS 建立连接服务器端资源占用高**
3. **HTTPS 协议握手费时：**多几次握手，网络耗时变长，用户从 HTTP 跳转到 HTTPS 还要一点时间。

## RSA简介

RSA是一种非对称性加密。通过 **公钥** 和 **私钥** 进行加密解密。

### 加密与签名

**加密** 是为了防止信息被泄露。

RSA的加密过程如下：

（1）A生成一对密钥（公钥和私钥），私钥不公开，A自己保留。公钥为公开的，任何人可以获取。

（2）A传递自己的公钥给B，B用A的公钥对消息进行加密。

（3）A接收到B加密的消息，利用A自己的私钥对消息进行解密。

　　在这个过程中，只有2次传递过程，第一次是A传递公钥给B，第二次是B传递加密消息给A，即使都被敌方截获，也没有危险性，因为只有A的私钥才能对消息进行解密，防止了消息内容的泄露。

**签名** 是为了防止信息被篡改

RSA签名的过程如下：

（1）A生成一对密钥（公钥和私钥），私钥不公开，A自己保留。公钥为公开的，任何人可以获取。

（2）A用自己的私钥对消息加签，形成签名，并将加签的消息和消息本身一起传递给B。

（3）B收到消息后，在获取A的公钥进行验签，如果验签出来的内容与消息本身一致，证明消息是A回复的。

　　在这个过程中，只有2次传递过程，第一次是A传递加签的消息和消息本身给B，第二次是B获取A的公钥，即使都被敌方截获，也没有危险性，因为只有A的私钥才能对消息进行签名，即使知道了消息内容，也无法伪造带签名的回复给B，防止了消息内容的篡改。

**总结 公钥加密、私钥解密、私钥签名、公钥验签。**

**PS: **RSA加密对明文的长度有所限制，规定需加密的明文最大长度=密钥长度-11（单位是字节，即byte），所以在**加密和解密的过程中需要分块进行**。而密钥默认是1024位，即1024位/8位-11=128-11=117字节。所以默认加密前的明文最大长度117字节，解密密文最大长度为128字。那么为啥两者相差11字节呢？是因为RSA加密使用到了填充模式（padding），即内容不足117字节时会自动填满，用到填充模式自然会占用一定的字节，而且这部分字节也是参与加密的。

### 项目实际

<img src="https://pic002.cnblogs.com/images/2012/379997/2012030522491911.png" alt="img" style="zoom:50%;" />

1.用户请求登录页面，服务器生成对应的 **公钥** 和 **私钥**，将 **公钥** 返回给浏览器，**私钥** 用Map/redis或mysql保存。（key 为公钥 value 为私钥）

2.用户 用 **公钥** 对密码进行加密 发给 服务器。

3.服务器用 **私钥** 解密 判断。

### 为什么不用MD5（区别）？

理论上讲，rsa基于大质数分解的复杂度远高于md5+salt。

实际上，md5+salt比rsa更适合。

1、不可能对所有的用户建立一个私钥，主要原因是管理复杂（基于安全的考虑不可能把私钥存在数据库中，那存储密码的复杂度就从保存密码本身变成了安全保存所有私钥）

2、如果只设计一个master私钥的话，所有密码验证共用一个私钥会带来更多的安全问题（第一点安全保存私钥的问题和master泄密的问题）

而使用md5+salt的隐患是彩虹表攻击，但是由于salt是随机生成的，作为明文和md5(passwd+salt)一起存放在数据库中，可以让所有用户密码泄漏的概率从O(N)变成O(N)*用户数（这里O(N)是指彩虹表破解一个用户密码的复杂度）。在绝大部分安全等级没那么高的系统中，这样设计已经足够满足需求了。

