`services.msc` 打开服务列表

## **xshell**

**隧道**

`连接` 先通过远程ip连接到远程终端

`隧道` 源主机 localhost(即本机) 目标主机 远程ip

则可以通过访问本地ip:port转发请求到远程终端

## 部署

![部署演进](https://d33wubrfki0l68.cloudfront.net/26a177ede4d7b032362289c6fccd448fc4a91174/eb693/images/docs/container_evolution.svg)

**传统部署时代：**

早期，组织在物理服务器上运行应用程序。无法为物理服务器中的应用程序定义资源边界，这会导致资源分配问题。 例如，如果在物理服务器上运行多个应用程序，则可能会出现一个应用程序占用大部分资源的情况， 结果可能导致其他应用程序的性能下降。 一种解决方案是在不同的物理服务器上运行每个应用程序，但是由于资源利用不足而无法扩展， 并且组织维护许多物理服务器的成本很高。

**虚拟化部署时代：**

作为解决方案，引入了虚拟化。虚拟化技术允许你在单个物理服务器的 CPU 上运行多个虚拟机（VM）。 虚拟化允许应用程序在 VM 之间隔离，并提供一定程度的安全，因为一个应用程序的信息 不能被另一应用程序随意访问。

虚拟化技术能够更好地利用物理服务器上的资源，并且因为可轻松地添加或更新应用程序 而可以实现更好的可伸缩性，降低硬件成本等等。

每个 VM 是一台完整的计算机，在虚拟化硬件之上运行所有组件，包括其自己的操作系统。

**容器部署时代：**

容器类似于 VM，但是它们具有被放宽的隔离属性，可以在应用程序之间共享操作系统（OS）。 因此，容器被认为是轻量级的。容器与 VM 类似，具有自己的文件系统、CPU、内存、进程空间等。 由于它们与基础架构分离，因此可以跨云和 OS 发行版本进行移植。

容器因具有许多优势而变得流行起来。下面列出的是容器的一些好处：

- 敏捷应用程序的创建和部署：与使用 VM 镜像相比，提高了容器镜像创建的简便性和效率。
- 持续开发、集成和部署：通过快速简单的回滚（由于镜像不可变性），支持可靠且频繁的 容器镜像构建和部署。
- 关注开发与运维的分离：在构建/发布时而不是在部署时创建应用程序容器镜像， 从而将应用程序与基础架构分离。
- 可观察性不仅可以显示操作系统级别的信息和指标，还可以显示应用程序的运行状况和其他指标信号。
- 跨开发、测试和生产的环境一致性：在便携式计算机上与在云中相同地运行。
- 跨云和操作系统发行版本的可移植性：可在 Ubuntu、RHEL、CoreOS、本地、 Google Kubernetes Engine 和其他任何地方运行。
- 以应用程序为中心的管理：提高抽象级别，从在虚拟硬件上运行 OS 到使用逻辑资源在 OS 上运行应用程序。
- 松散耦合、分布式、弹性、解放的微服务：应用程序被分解成较小的独立部分， 并且可以动态部署和管理 - 而不是在一台大型单机上整体运行。
- 资源隔离：可预测的应用程序性能。
- 资源利用：高效率和高密度。





## kubectl

kubectl port-forward 通过端口转发映射本地端口到指定的应用端口，从而访问集群中的应用程序，然后添加 `隧道 `

`cces-test`是个`namespace` `eric-cces-common-entity` 是个`deployment`

```
kubectl port-forward -n cces-test --address 0.0.0.0 svc/eric-cces-common-entity 8083:8083
kubectl port-forward -n cces-test --address 0.0.0.0 svc/eric-apigm-api-admin 8085:8085									
```

![img](https://img-blog.csdn.net/20180817184719684?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3Vjc2hlZXA=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

namespace   --deployment						

​						--deployment --node

​												 --node --pod

​															  --pod --容器1--app应用

​																		 --容器2																							

​												

**deploy控制RS（ReplicaSet），RS控制Pod，这一整套，向外提供稳定可靠的Service**

**一个node包括多个pod**

pod中的容器共享同一个IP和端口以及其他信息，pod工作在**node工作节点**，node是k8s中参与计算的机器可以是虚拟机/物理计算机



![img](https://d33wubrfki0l68.cloudfront.net/fe03f68d8ede9815184852ca2a4fd30325e5d15a/98064/docs/tutorials/kubernetes-basics/public/images/module_03_pods.svg)

<img src="https://d33wubrfki0l68.cloudfront.net/5cb72d407cbe2755e581b6de757e0d81760d5b86/a9df9/docs/tutorials/kubernetes-basics/public/images/module_03_nodes.svg" alt="img" style="zoom: 33%;" />

**deployment:**管理 `pod` 的生命周期、调度问题，`pod`会被调度到不同的机器上，导致一个app应用ip发送变化

**pod deployment service概念**

https://mp.weixin.qq.com/s/-CsK00RkXepZQXOxbNumEA





```
//列出所有namespace
kubectl get namespaces
//列出所有namespaces下的pods
kubectl get pods --all-namespaces
//列出所有namespaces下的deployment
kubectl get deployment --all-namespaces
//列出所有namespaces下的services
kubectl get services --all-namespaces


//创建deployment
kubectl apply -f https://k8s.io/examples/application/guestbook/redis-master-deployment.yaml
//创建成功
--deployment.apps/redis-master created

//创建service
kubectl apply -f https://k8s.io/examples/application/guestbook/redis-master-service.yaml
//创建成功
service/redis-master created

//查看cces下的services
kubectl get services --namespace cces
//查看cces-test下的pod
kubectl get pods --namespace cces-test
//查找具体的pod pod要全称
kebuctl get pods eric-tm-ingress-controller-cr-envoy-tbrzm --namespace=cces-test
//查找具体的deployment
kubectl get deployment eric-cces-common-entity --namespace=cces-test


//查看pod结构信息
kubectl describe pods eric-cces-common-entity-97bdb7ff4-zr4mc --namespace=cces-test
//查看service结构信息
kubectl describe service 
```



### linux管理命令

```
&	加在一个命令的最后，可以把这个命令放到后台执行 ,如gftp &

ctrl + z	可以将一个正在前台执行的命令放到后台，并且处于暂停状态，不可执行
但是此任务并没有结束,他仍然在进程中，只是放到后台并维持挂起的状态。如需其在后台继续运行，需用“bg 进程号”使其继续运行；再用"fg 进程号"可将后台进程前台化。

ctrl+c	强行中断当前程序的执行。
ctrl+\	表示退出。
jobs	显示的是当前shell环境中所起的后台正在运行或者被挂起的任务信息；
jobs -l选项可显示所有任务的PID，jobs的状态可以是running, stopped, Terminated,但是如果任务被终止了（kill），shell 从当前的shell环境已知的列表中删除任务的进程标识；
fg	将后台中的命令调至前台继续运行
bg	将一个在后台暂停的命令，变成继续执行 （在后台执行）
```

