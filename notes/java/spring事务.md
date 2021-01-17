# spring事务

## 传播属性

**1) REQUIRED（默认属性）**
如果存在一个事务，则支持当前事务。如果没有事务则开启一个新的事务。 
被设置成这个级别时，会为每一个被调用的方法创建一个逻辑事务域。如果前面的方法已经创建了事务，那么后面的方法支持当前的事务，如果当前没有事务会重新建立事务。 

**2) MANDATORY** 
支持当前事务，如果当前没有事务，就抛出异常。 

**3) NEVER** 
以非事务方式执行，如果当前存在事务，则抛出异常。 

**4) NOT_SUPPORTED** 
以非事务方式执行操作，如果当前存在事务，就把当前事务挂起。 

**5) REQUIRES_NEW** 
新建事务，如果当前存在事务，把当前事务挂起。 

**6) SUPPORTS** 
支持当前事务，如果当前没有事务，就以非事务方式执行。 

**7) NESTED** 
支持当前事务，新增Savepoint点，与当前事务同步提交或回滚。 
嵌套事务一个非常重要的概念就是内层事务依赖于外层事务。外层事务失败时，会回滚内层事务所做的动作。而内层事务操作失败并不会引起外层事务的回滚。 

**PROPAGATION_NESTED 与PROPAGATION_REQUIRES_NEW的区别：**
它们非常类似，都像一个嵌套事务，如果不存在一个活动的事务，都会开启一个新的事务。

使用PROPAGATION_REQUIRES_NEW时，内层事务与外层事务就像两个独立的事务一样，一旦内层事务进行了提交后，外层事务不能对其进行回滚。两个事务互不影响。两个事务不是一个真正的嵌套事务。同时它需要JTA 事务管理器的支持。 
使用PROPAGATION_NESTED时，外层事务的回滚可以引起内层事务的回滚。而内层事务的异常并不会导致外层事务的回滚，它是一个真正的嵌套事务。 

## 隔离级别

**1) DEFAULT （默认）** 
这是一个PlatfromTransactionManager默认的隔离级别，使用数据库默认的事务隔离级别。另外四个与JDBC的隔离级别相对应。

**2) READ_UNCOMMITTED （读未提交）** 
这是事务最低的隔离级别，它允许另外一个事务可以看到这个事务未提交的数据。这种隔离级别会产生脏读，不可重复读和幻像读。 

**3) READ_COMMITTED （读已提交）** 
保证一个事务修改的数据提交后才能被另外一个事务读取，另外一个事务不能读取该事务未提交的数据。这种事务隔离级别可以避免脏读出现，但是可能会出现不可重复读和幻像读。 

**4) REPEATABLE_READ （可重复读）** 
这种事务隔离级别可以防止脏读、不可重复读，但是可能出现幻像读。它除了保证一个事务不能读取另一个事务未提交的数据外，还保证了不可重复读。

**5) SERIALIZABLE（串行化）** 
这是花费最高代价但是最可靠的事务隔离级别，事务被处理为顺序执行。除了防止脏读、不可重复读外，还避免了幻像读。 

## 事务失效原因

 **1、没有被 Spring 管理**

```java
// @Service
public class OrderServiceImpl implements OrderService {   
 @Transactional    
public void updateOrder(Order order) {       
 // update order；  
  }
}
```

如果此时把 `@Service` 注解注释掉，这个类就不会被加载成一个 Bean，那这个类就不会被 Spring 管理了，事务自然就失效了。

**2、方法不是 public的 -- 该异常一般情况都会被编译器帮忙识别**

大概意思就是 `@Transactional` 只能用于 public 的方法上，否则事务不会失效，如果要用在非 public 方法上，可以开启 `AspectJ` 代理模式。

**3、自身调用问题**

```java
@Service
public class OrderServiceImpl implements OrderService {
    public void update(Order order) {
        updateOrder(order);
    }
    @Transactional
    public void updateOrder(Order order) {
        // update order；
    }
}
```

update方法上面没有加 `@Transactional` 注解，调用有 `@Transactional` 注解的 updateOrder 方法，updateOrder 方法上的事务管用吗？

再来看下面这个例子：

```java
@Service
public class OrderServiceImpl implements OrderService {
    @Transactional
    public void update(Order order) {
        updateOrder(order); 
   }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateOrder(Order order) {
        // update order；
    }
}
```

这次在 update 方法上加了 `@Transactional`，updateOrder 加了 `REQUIRES_NEW` 新开启一个事务，那么新开的事务管用么？

这两个例子的答案是：不管用！

因为它们发生了自身调用，就调该类自己的方法，而没有经过 Spring 的代理类，默认只有在外部调用事务才会生效，这也是老生常谈的经典问题了。

这个的解决方案之一就是在的类中注入自己，用注入的对象再调用另外一个方法，这个不太优雅，另外一个可行的方案可以参考《[Spring 如何在一个事务中开启另一个事务？](https://links.jianshu.com/go?to=https%3A%2F%2Fmp.weixin.qq.com%2Fs%3F__biz%3DMzI3ODcxMzQzMw%3D%3D%26mid%3D2247491775%26idx%3D2%26sn%3D142f1d6ab0415f17a413a852efbde54f%26scene%3D21%23wechat_redirect)》这篇文章。

**5、不支持事务**

```java
@Service
public class OrderServiceImpl implements OrderService {
    @Transactional
    public void update(Order order) {
        updateOrder(order);
    }
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void updateOrder(Order order) {
        // update order；
    }
}
```

**Propagation.NOT_SUPPORTED：** 表示不以事务运行，当前若存在事务则挂起，详细的可以参考《[事务隔离级别和传播机制](https://links.jianshu.com/go?to=https%3A%2F%2Fmp.weixin.qq.com%2Fs%3F__biz%3DMzI3ODcxMzQzMw%3D%3D%26mid%3D2247483796%26idx%3D1%26sn%3Da11835fb6cdf4d957b5748ae916e53b7%26scene%3D21%23wechat_redirect)》这篇文章。都主动不支持以事务方式运行了，那事务生效也是白搭！

**6、异常被吃了**

这个也是出现比较多的场景：

```java
@Service
public class OrderServiceImpl implements OrderService {
    @Transactional
    public void updateOrder(Order order) {
        try {
            // update order;
         }catch (Exception e){
            //do something;
        }
    }
}
```

把异常吃了，然后又不抛出来，事务就不生效了

**7、异常类型错误或格式配置错误**

上面的例子再抛出一个异常：

```java
@Service
public class OrderServiceImpl implements OrderService {
 @Transactional
   // @Transactional(rollbackFor = SQLException.class)
    public void updateOrder(Order order) {
        try {            // update order
          }catch (Exception e){
           throw new Exception("更新错误");        
        }    
    }
}
```

这样事务也是不生效的，因为默认回滚的是：RuntimeException，如果你想触发其他异常的回滚，需要在注解上配置一下，如：

```ruby
@Transactional(rollbackFor = Exception.class)
```

这个配置仅限于 `Throwable` 异常类及其子类。

## 同个类一个方法调用另一个方法

**在同一个类中，一个方法调用另外一个有注解（比如@Async，@Transational）的方法，注解是不会生效的**

spring 在扫描bean的时候会扫描方法上是否包含@Transactional注解，如果包含，spring会为这个bean动态地生成一个子类（即代理类，proxy），代理类是继承原来那个bean的。此时，当这个有注解的方法被调用的时候，实际上是由代理类来调用的，代理类在调用之前就会启动transaction。然而，如果这个有注解的方法是被同一个类中的其他方法调用的，那么该方法的调用并没有通过代理类，而是直接通过原来的那个bean，所以就不会启动transaction，我们看到的现象就是@Transactional注解无效。

**在一个Service内部，事务方法之间的嵌套调用，普通方法和事务方法之间的嵌套调用，都不会开启新的事务.**