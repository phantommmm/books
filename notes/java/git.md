## hugo

```
//hugo目录下 git
1.创建站点hugo new site augest
//hugo/augest/themes 下 git
2.下载博客主题:git clone git@github.com:shankar/hugo-grapes.git
//hugo/augest 下 git
3.新建博客文章:hugo new first.md
//hugo/augest 下 git
4.启动hugo:hugo server --theme=jane --buildDrafts

//hugo/augest
1.将本地内容上传到自己博客中,会生成一个public文件
hugo --theme=jane --buildDrafts --baseUrl="https://phantommmm.github.io/"
//hugo/augest/public
2.进入到public文件中,将public转换为仓库
git init
//hugo/augest/public
3.将所有内容选中
git add .
//hugo/augest/public
4.提交到站点中
git commit -m "八月的个人博客"
//hugo/augest/public 
5将提交内容与站点关联起来 执行一次就够
$ git remote add origin https://github.com/phantommmm/phantommmm.github.io
//hugo/augest/public
6.将本地内容刷新至站点中
$ git push https://github.com/phantommmm/phantommmm.github.io master
//将本地仓库替换远程仓库
git push --force --set-upstream origin master
```

## Git

```
1. git init //初始化仓库

2. git add .(文件name) //添加文件到本地仓库
//.表示添加所有文件 *表示更新文件

3. git commit -m "first commit" //添加文件描述信息

//绑定远程仓库 https://github.com/phantommmm/phantommmm.github.io master
4. git remote add origin + 远程仓库地址 //链接远程仓库，创建主分支

5. git pull origin master // 把本地仓库的变化连接到远程仓库主分支

6. git push  origin master //把本地仓库的文件推送到远程仓库

origin表示远程仓库的标签 可以之间用下面的代替
https://github.com/phantommmm/phantommmm.github.io 
```

![/GitAddRemote](https://imgconvert.csdnimg.cn/aHR0cHM6Ly93d3cudGllbGVtYW8uY29tL3dwLWNvbnRlbnQvdXBsb2Fkcy8yMDE4LzA2L0dpdEFkZFJlbW90ZS5qcGc)

```
Updating 7c9e086..936acac
error: The following untracked working tree files would be overwritten by merge:
Common/HFHttpRequest/HFHttpRequestParameters.h
Common/HFHttpRequest/HFHttpRequestParameters.m

Please move or remove them before you can merge.
Aborting

git clean  -d  -fx ""
其中 
x  -----删除忽略文件已经对git来说不识别的文件
d  -----删除未被添加到git的路径中的文件 即与远程仓库一样 忽略本地
f  -----强制运行
```

