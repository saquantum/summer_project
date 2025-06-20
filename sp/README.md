### All the documentation and functionality specifications can be found separately in each package.

### How to run this project:

Direct to the folder where `mvnw` or `mvnw.cmd` exists, open console here type

```
$ ./mvnw clean package
```

and if it compiles there should be a `sp-xx-.jar` file in the `/target` folder. Open console there and type

```
$ java -jar sp-xx-.jar
```

then visit `http://localhost:8080` with browser.

For Windows, use `mvnw.cmd`:
```
> mvnw clean package
```


#### Front-End
Vue + Leaflet

#### Back-End
SpringBoot + MyBatis + PostgreSQL

# --------------------------------------------------

### 在idea上测试程序 (不需要Docker)

1. 通过官网下载PostgreSQL和图形化工具pgAdmin, 记住安装时填写的root用户的密码.
2. 打开pgAdmin, 创建一个名为`rainwaterDB`的数据库, 右键点击它打开查询工具/Query Tool.
3. 在`sp/src/main/resources/`下找到`init.sql`, 复制其中的所有代码, 粘贴到查询工具中, 点击执行/Execute.
4. 在`sp/src/main/resources/`下找到`application-local.yml`, 把`spring: datasource: password`中的密码改成自己的root用户的密码.
5. 在idea中运行`SpApplication`.
6. 在浏览器中访问`http://localhost:8080`.

常见问题:
1. 我忘了root用户的密码. -> google一下如何找回密码, 或者把postgre卸载了重装.
2. 执行SQL代码出错. -> 删掉rainwaterDB数据库, 重新创建一次.
3. 运行SpApplication出错. -> 检查代码的版本是否为github上的最新版, 或者看报错信息中是否指出8080端口已经被占用. 如果是被占用, 重启电脑试试.

# --------------------------------------------------

### 在Docker上配置运行环境

一. 安装Docker: 

如果你的linux系统的安装工具是`apt`:

```
$ sudo apt update -y
$ sudo apt install -y ca-certificates curl gnupg
$ sudo install -m 0755 -d /etc/apt/keyrings
$ sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg | \
    gpg --dearmor -o /etc/apt/keyrings/docker.gpg
$ sudo chmod a+r /etc/apt/keyrings/docker.gpg
$ sudo echo \
  "deb [arch="$(dpkg --print-architecture)" \
  signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  tee /etc/apt/sources.list.d/docker.list > /dev/null
$ sudo apt update -y
$ sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
$ sudo systemctl start docker
$ sudo systemctl enable docker
```

如果安装工具是`yum`:

```
$ sudo yum update -y
$ sudo yum install -y yum-utils
$ sudo yum-config-manager \
    --add-repo \
    https://download.docker.com/linux/centos/docker-ce.repo
$ sudo yum install -y docker-ce docker-ce-cli containerd.io
$ sudo yum install -y docker-compose-plugin
$ sudo systemctl start docker
$ sudo systemctl enable docker
```

二. 复制本机文件到linux虚拟机中:

首先在虚拟机中查看虚拟机的ip:

```
[root@localhost ~]# hostname -I
192.168.48.131 172.17.0.1 172.18.0.1 
```

我的虚拟机地址是`192.168.48.131`.

执行Maven的`Package`生命周期,找到springboot项目下的`/target`文件夹, 将其中的`sp-xx.jar`文件重命名为`app.jar`, 然后在此处打开cmd, 复制jar包到虚拟机中:

```
E:\Users\Desktop\summer_project\sp\target> scp app.jar root@192.168.48.131:~/app
```

找到springboot项目下的`/src/main/resources`文件夹, 将其中的`docker-compose.yml`, `Dockerfile`和`init.sql`复制到虚拟机`~/app`中.

三. 启动Docker容器

确保虚拟机中`~/app`下有需要的文件.

```
[root@localhost app]# cd
[root@localhost ~]# cd app
[root@localhost app]# ls
Dockerfile  app.jar  docker-compose.yml  init.sql
```

在`~/app`文件夹中依次运行以下指令.

```
$ docker compose down -v
$ docker volume prune -f
$ docker compose up --build -d
$ docker ps -a
```

四. 验证服务器可访问

用浏览器访问`http://192.168.48.131:8080/`.



### windows运行docker

新建文件夹test, 放入如下文件

frontend目录下为前端源码，全部复制过来

```
test/
├── backend/
│   ├── app.jar
│   └── Dockerfile
└── frontend/
│   └── Dockerfile
│   .
│   .
│   └── nginx.conf
│
│
├── docker-compose.yml
└── init.sql
```

在test目录下，运行如下命令

```
$ docker compose up --build
```

访问localhost

