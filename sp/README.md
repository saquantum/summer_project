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

# -------------------------

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
