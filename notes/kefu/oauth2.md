# OAuth2

OAuth在"客户端"与"服务提供商"之间，设置了一个授权层（authorization layer）。"客户端"不能直接登录"服务提供商"，只能登录授权层，以此将用户与客户端区分开来。"客户端"登录授权层所用的令牌（token），与用户的密码不同。用户可以在登录的时候，指定授权层令牌的权限范围和有效期。

"客户端"登录授权层以后，"服务提供商"根据令牌的权限范围和有效期，向"客户端"开放用户储存的资料。

## 角色

（1） **Third-party application**：第三方应用程序，本文中又称"客户端"（client）

（2）**HTTP service**：HTTP服务提供商，本文中简称"服务提供商"，即上一节例子中的Google。

（3）**Resource Owner**：资源所有者，本文中又称"用户"（user），授予权限让其他人访问资源。

（4）**User Agent**：用户代理，本文中就是指浏览器。

（5）**Authorization server**：认证服务器，即服务提供商专门用来处理认证的服务器。

（6）**Resource server**：资源服务器，即服务提供商存放用户生成的资源的服务器。它与认证服务器，可以是同一台服务器，也可以是不同的服务器。

## 运行流程

![OAuth运行流程](https://www.ruanyifeng.com/blogimg/asset/2014/bg2014051203.png)

（A）用户打开客户端以后，客户端要求用户给予授权。（账号密码登录）

（B）用户同意给予客户端授权。（登录成功）

（C）客户端使用上一步获得的授权，向认证服务器申请令牌。（申请token）

（D）认证服务器对客户端进行认证以后，确认无误，同意发放令牌。(获得token)

（E）客户端使用令牌，向资源服务器申请获取资源。(无需再次登录，使用token验证通过)

（F）资源服务器确认令牌无误，同意向客户端开放资源。(获取资源)

## 授权模式

#### 授权码模式（authorization code）

#### 简化模式（implicit）

#### 密码模式（resource owner password credentials）

#### 客户端模式（client credentials）



授权模式

https://www.ruanyifeng.com/blog/2014/05/oauth_2_0.html

https://www.ruanyifeng.com/blog/2019/04/oauth-grant-types.html