```
git branch -a //查看所有分支
git branch//查看当前分支
git checkout 分支名

git remote -v//查看远程仓库地址
```

**fork clone区别**

fork：在github页面，点击fork按钮。将别人的仓库A复制一份到自己的仓库B,然后再进行clone到本地idea中
clone：将github中的仓库克隆到自己本地电脑中

直接clone到本地后，commit push无法直接到A，除非是有权限

commite push到自己仓库B,pull request，A主人会收到请求并决定要不要你的代码或者申请为A的contributor则可以直接push

**fork pull仓库流程**

1.老大创建一个主仓库

2.每个成员`fork` 一份到自己仓库

3.成员开发需求完成后，先`push`到自己的仓库，然后发起`merge request`，等待大佬 `code review` 并且合并代码

4.如果主仓库有更新，先`fetch` 然后合并到自己仓库  https://gaohaoyang.github.io/2015/04/12/Syncing-a-fork/

![img](https://img-blog.csdn.net/20180614163016948?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L21hdHJpeF9nb29nbGU=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

```
git fetch upstream
git	merge upstream/master
```

**merge request**

**1.** 本地代码push到自己仓库

**2.** 去到自己仓库发起 `merge request` 源仓库即为自己仓库 target仓库为主仓库

设置code reviewer

**3.** 等待审核



**sourceTree查看远程仓库**

打开终端，输入 `cat .git/config`



**git pull commit顺序**

commit --> pull --> push

先commit表示把自己写的代码commit起来，然后再pull其他人的代码，再push。

这样每个commit信息才清晰