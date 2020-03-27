#  G-mall

Spring Boot电商学习项目，教程视屏：https://www.bilibili.com/video/av55643074

### P6 idea和git的配置

**注意**：项目开发协作时，git上不能提交[.idea]文件夹下的个人文件，否则会被队友打死

**Git上只应当提交：**1.源代码	2.pom依赖	3.配置文件



### P12 通用Mapper的加入

1.在pom文件中引入通用Mapper依赖，此依赖可与已有MyBatis并存

```xml
<dependency>
    <groupId>tk.mybatis</groupId>
    <artifactId>mapper-spring-boot-starter</artifactId>
    <version>2.1.0</version>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

2.在Mapper类中继承通用Mapper：tk.mybatis.mapper.common.Mapper

```java
import tk.mybatis.mapper.common.Mapper;

public interface UserMapper extends Mapper<UmsMember> {
}
```

​	然后在ServiceImpl中就可调用通用mapper方法

```java
 @Autowired
    UserMapper userMapper;

    @Override
    public List<UmsMember> getAllUser() {
        List<UmsMember> umsMemberList=userMapper.selectAll();
        return umsMemberList;
    }
```

3.配置通用Mapper的主键及主键返回策略

```java
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
```

4.配置启动类，使用tk.mybatis.spring.annotation.MapperScan扫描器



### p13

**IDEA的Debug模式中：**F8——逐步执行，F9——跳过断点

通过bean中某一字段值查询的方法

1.用Bean对象查询

```java
public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {
    UmsMemberReceiveAddress umsMemberReceiveAddress=new UmsMemberReceiveAddress();
    umsMemberReceiveAddress.setMemberId(memberId);

    List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = userReceiveAddressMapper.select(umsMemberReceiveAddress);
    return umsMemberReceiveAddresses;
}
```

2.用Example对象查询

```java
public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {
    Example example=new Example(UmsMemberReceiveAddress.class);
    example.createCriteria().andEqualTo("memberId",memberId);
    List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = userReceiveAddressMapper.selectByExample(example);
    return umsMemberReceiveAddresses;
}
```



### P15 项目架构的介绍

1. 工程结构

   以maven为基础，对项目的分层架构

2. 项目架构

   分布式（SOA,Service Oriented Architecture）

   ##### 最终结构图：

   ![1575097286710](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/1575097286710.png)

   

![1575097118810](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/1575097118810.png)

##### 抽取parent模块：

创建Maven Model，命名为gmall-parent，其中放置通用的pom依赖：

```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.10.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.atguigu.gmall</groupId>
    <artifactId>gmall-parent</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>pom</packaging>
   <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <java.version>1.8</java.version>
        <fastjson.version>1.2.46</fastjson.version>
        <dubbo-starter.version>1.0.10</dubbo-starter.version>
        <dubbo.version>2.6.0</dubbo.version>
        ······
    </properties>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.alibaba</groupId>
                <artifactId>fastjson</artifactId>
                <version>${fastjson.version}</version>
            </dependency>
            <dependency>
                <groupId>com.alibaba</groupId>
                <artifactId>dubbo</artifactId>
                <version>${dubbo.version}</version>
            </dependency>
            ······
        </dependencies>
    </dependencyManagement>

```

在其他模块中引入parent模块：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.atguigu.gmall</groupId>
        <artifactId>gmall-parent</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <groupId>com.atguigu.gmall</groupId>
    <artifactId>gmall-user</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>gmall-user</name>
    <description>Demo project for Spring Boot</description>
```

##### 抽取API模块：

api模块：放置接口，bean文件，全都要用

（bean）

![1575104420668](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/1575104420668.png)

![1575104601010](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/1575104601010.png)

（service）

##### 抽取Utils模块：

1. Utils模块：项目中的通用框架，是所有应用工程需要引入的包（CommonUtil）

> springboot、common-langs、common-beanutils……

2.基于SOA的架构理念，项目分为web前端：controller（WebUtil）

> JSP、thymeleaf、cookie工具类……

3.基于SOA的架构理念，项目分为web后端：service（ServiceUtil）

> MyBatis、mysql、redis……

![1575117966350](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/1575117966350.png)

controller = parent + api +webUtil

servive = parent + api + serviceUtil

![](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/1575122478336.png)

### P22 SOA面向服务（以Dubbo为基础）

Dubbo通信时使用非Http协议（自定义协议），利用注册中心的客户端，通过Dubbo来访问服务。注册中心客户端负责实时同步注册中心的服务信息，Dubbo框架负责把服务封装成dubbo协议互相之间访问

1.Dubbo的工作原理和spring cloud类似

2.Dubbo和Spring Cloud的区别在于dubbo由自己的dubbo协议通信，而springcloud是由Http协议（Rest风格）

3.Dubbo有一个注册中心的客户端在实时同步注册中心的服务信息

4.Dubbo有一个javaweb的监控中心，负责监控服务的注册信息，甚至可以配置负载均衡

### P23 启动 dubbo

##### 启动监控中心：

1. 将dubbo监控中心和tomcat上传到linux服务器
2. 用unzip命令解压dubbo-admin.war
3. 配置tomcat的server.xml，在末尾添加<Context / >：

```xml
<Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
       prefix="localhost_access_log" suffix=".txt"
       pattern="%h %l %u %t &quot;%r&quot; %s %b" />

<Context path="/dubbo" docBase="/opt/dubbo" debug="0" priviledged="true" />

</Host>
```

4. 启动tomcat，打开监控中心。(本机浏览器访问linux ip地址时注意，须关掉linux防火墙)

##### 安装和配置zookeeper：

1. 解压zookeeper，将zookeeper文件夹命名为zookeeper

2. 修改zookeeper的配置文件（conf/zoo.cfg）

   建立一个新的数据目录/opt/zookeeper/data

   编辑conf/zoo.cfg,修改数据目录

   ![1575259083061](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/1575259083061.png)

3. 启动zookeeper

   ```sh
   cd /opt/zookeeper/bin
   ```

   ```shell
   [root@localhost zookeeper]# cd bin
   [root@localhost bin]# ./zkServer.sh start		主线程
   ZooKeeper JMX enabled by default
   Using config: /opt/zookeeper/bin/../conf/zoo.cfg
   Starting zookeeper ... STARTED
   [root@localhost bin]# ./zkServer.sh status		另外一线程
   ZooKeeper JMX enabled by default
   Using config: /opt/zookeeper/bin/../conf/zoo.cfg
   Mode: standalone
   
   ```

4. 设置监控中心和zookeeper的开机自启动：
   监控中心：

   ```sh
   cd /etc/init.d
   ```

   ```sh
   vim dubbo-admin
   ```

   ```sh
   #!/bin/bash
   #chkconfig:2345 20 90
   #description:dubbo-admin
   #processname:dubbo-admin
   CATALANA_HOME=/opt/tomcat
   export JAVA_HOME=/usr/local/java/jdk1.8
   case $1 in
   start)  
       echo "Starting Tomcat..."  
       $CATALANA_HOME/bin/startup.sh  
       ;;  
     
   stop)  
       echo "Stopping Tomcat..."  
       $CATALANA_HOME/bin/shutdown.sh  
       ;;  
     
   restart)  
       echo "Stopping Tomcat..."  
       $CATALANA_HOME/bin/shutdown.sh  
       sleep 2  
       echo  
       echo "Starting Tomcat..."  
       $CATALANA_HOME/bin/startup.sh  
       ;;  
   *)  
       echo "Usage: tomcat {start|stop|restart}"  
       ;; esac
   ```

   ```sh
   chkconfig --add dubbo-admin
   ```

   ```sh
   chmod 777 dubbo-admin
   ```

   ```sh
   service dubbo-admin start
   ```

   ​	zookeeper：

   ```sh
   cd /etc/init.d
   ```

   ```sh
   vim zookeeper
   ```

   ```sh
   #!/bin/bash
   #chkconfig:2345 20 90
   #description:zookeeper
   #processname:zookeeper
   ZK_PATH=/opt/zookeeper
   export JAVA_HOME=/usr/local/java/jdk1.8
   case $1 in
            start) sh  $ZK_PATH/bin/zkServer.sh start;;
            stop)  sh  $ZK_PATH/bin/zkServer.sh stop;;
            status) sh  $ZK_PATH/bin/zkServer.sh status;;
            restart) sh $ZK_PATH/bin/zkServer.sh restart;;
            *)  echo "require start|stop|status|restart"  ;;
   esac
   ```

   ```sh
   chkconfig --add dubbo-admin
   ```

   ```sh
   chmod 777 dubbo-admin
   ```

   ```sh
   service dubbo-admin start
   ```

### P27 将项目改造为Dubbo的分布式架构

   1. 将user项目拆分为user-servie和user-web

   2. 引入dubbo框架：

      service层和web层都要用dubbo进行通信，故将dubbo引入到common-util中

      注意到之前在gmall-parent的pom文件中已经定义好了依赖版本：

      ```xml
          <properties>
              ......
              <dubbo-starter.version>1.0.10</dubbo-starter.version>
              <dubbo.version>2.6.0</dubbo.version>
              <zkclient.version>0.10</zkclient.version>
              ......
          </properties>
      
          <dependencyManagement>
              <dependencies>
                  ......
                  <dependency>
                      <groupId>com.alibaba</groupId>
                      <artifactId>dubbo</artifactId>
                      <version>${dubbo.version}</version>
                  </dependency>
      
                  <dependency>
                      <groupId>com.101tec</groupId>
                      <artifactId>zkclient</artifactId>
                      <version>${zkclient.version}</version>
                  </dependency>
      
                  <dependency>
                      <groupId>com.gitee.reger</groupId>
                      <artifactId>spring-boot-starter-dubbo</artifactId>
                      <version>${dubbo-starter.version}</version>
                  </dependency>
                  ......
              </dependencies>
          </dependencyManagement>
      ```

      然后在common-util中引入依赖:

      ```xml
      <dependency>
          <groupId>com.alibaba</groupId>
          <artifactId>dubbo</artifactId>
      </dependency>
      
      <dependency>
          <groupId>com.101tec</groupId>
          <artifactId>zkclient</artifactId>
          <exclusions>
              <exclusion>
                  <groupId>org.slf4j</groupId>
                  <artifactId>slf4j-log4j12</artifactId>
              </exclusion>
          </exclusions>
      </dependency>
      
      <dependency>
          <groupId>com.gitee.reger</groupId>
          <artifactId>spring-boot-starter-dubbo</artifactId>
      </dependency>
      ```

      3. 刷新maven依赖
      
         
      
##### dubbo服务Provider的启动

1. 将原项目的配置文件、serviceImpl与Mapper包复制到user-service模块中
   
2. 修改user-service中的配置文件：
   
```properties
# 服务端口
server.port=8070
# jdbc配置
spring.datasource.username=root
spring.datasource.password=asdasdasd
spring.datasource.url=jdbc:mysql://localhost:3306/gmall?characterEncoding=UTF-8
# mybatis配置
mybatis.mapper-locations=classpath:mapper/*Mapper.xml
mybatis.configuration.map-underscore-to-camel-case=true

# dubbo的配置

# dubbo中的服务名称
spring.dubbo.application=user-service
# dubbo的通讯协议的名称
spring.dubbo.protocol.name=dubbo
# zookeeper注册中心的地址
spring.dubbo.registry.address=192.168.226.129:2181
# zookeeper的通讯协议的名称
spring.dubbo.registry.protocol=zookeeper
# dubbo的服务的扫描路径
spring.dubbo.base-package=com.atguigu.gmall
```

3. 将Impl中的@Service所导入的包改为com.alibaba.dubbo.config.annotation.Service

##### dubbo服务consumer的启动

1. 将原项目的配置文件、serviceImpl与Mapper包复制到user-web模块中

2. 修改user-web中的配置文件：

```properties
# 服务端口
server.port=8080

# dubbo的配置

# dubbo中的服务名称
spring.dubbo.application=user-service
# dubbo的通讯协议的名称
spring.dubbo.protocol.name=dubbo
# zookeeper注册中心的地址
spring.dubbo.registry.address=192.168.226.129:2181
# zookeeper的通讯协议的名称
spring.dubbo.registry.protocol=zookeeper
# dubbo的服务的扫描路径
spring.dubbo.base-package=com.atguigu.gmall
# 设置超时时间(毫秒)
spring.dubbo.consumer.timeout=2000
```

3. 将controller中的@Autowired改为@Reference

##### dubbo配置的注意事项：

1. spring的@service改为dubbo的@Service
2. 将@Autowired改为@Reference
3. dubbo在进行dubbo协议通讯时，要实现序列化接口（封装的数据对象）

### P30商品概念介绍

1. 系统名称：Gmall-Manager

2. 数据结构：Pms、sku+spu

   SKU：stock keeping unit 库存存储单元，一般指一个具体的库存商品，单位为台、部、件等

   （比如某品牌鞋子的：款式+颜色+尺码就是这里说的SKU，是我们识别产品所必须的，也是商场进出存的最小单元*）

   ​	是库存进出计量的基本单元，是对大型连锁超市物流管理的一个必要方法。现在已经被引申为产品统一编号的建成。每一产品均只有对应的唯一的SKU号
   
   SPU：standard product unit 标准的商品单元，一般一个商品（该商品又可分为XX系列），就是一个SPU。（比如IPhoneX就为一个SPU。由不同的颜色和内存大小，又可得到不同的SKU，如IPhoneX 64G 黑色）
   
   ​	是商品信息聚合的最小单元，是一组可复用、易检索的标准化信息的集合，该集合描述了一个产品的特性。
   
   
   
   关系：
   
   ​	范围上来说Spu包含Sku
   
   ​	SPU表与SKU表是一对多的关系

3. 平台属性：

![image-20191204161810546](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191204161810546.png)

​	涉及的两张表pms_base_attr_info、pms_base_attr_value



##### pms商品的数据结构的划分    

1. sku的结构：pms_sku_
2. spu的结构：pms_spu_
3. 类目的结构：pms_catalog
4. 属性的结构：pms_attr_

##### 前端

![image-20191203194941620](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191203194941620.png)

模板技术：把页面中的静态数据替换成从后台数据库中的数据。这种操作用jsp就可以实现。但是Springboot 的架构不推荐使用Jsp，而且支持也不好，所以如果你是用springboot的话，一般使用Freemarker或者Thymeleaf。

前后端比较：

| JVM     | Spring | Maven | IDEA   |
| ------- | ------ | ----- | ------ |
| Node.js | Vue    | npm   | vscode |

##### 解压前端项目gmall-admin

解压后进入Conf文件夹，配置前端服务的IP 和 前端访问后端的数据服务服务的ip地址

dev.env.js 前端访问后端的数据服务的地址：

![image-20191203210301402](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191203210301402.png)

index.js 前端的服务器的端口：

![image-20191203210542784](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191203210542784.png)

##### 用npm命令编译和启动前端的项目

在gmall-admin目录下执行命令：

```bash
npm run dev
```

然后打开127.0.0.1:8888就可看到前端项目

![image-20191203214955227](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191203214955227.png)

##### 前后端请求格式

一般前端会用post向后端发送请求（把参数封装到json中）

后端对应的**请求格式：@RequestBody，返回格式：@ResponseBody**

### P35 商城商品录入功能（Manage）

1. 三级分类的查询

2. 商品的品台属性的增删改查

3. 商品spu的添加

   spu列表查询

   spu的销售属性、属性值、Fastdfs图片上传

4. 商品sku的添加

   sku信息、sku关联的销售属性、sku关联的品台的属性、sku图片

##### 实现manage-web模块

商品分类功能查询：

1. 新建manage-web项目
2. 配置pom
3. 写一个getCatalog1()给前端项目调用
4. 返回一个catalog1列表集合（json）
5. 新建一个catalog服务接口

![image-20191203231211803](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191203231211803.png)

6. 实现该服务

   新建Mapper

   ![image-20191203233024909](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191203233024909.png)

##### 前后端的跨域问题

前端127.0.0.1:8888

后端127.0.0.1:8081

![image-20191204004733339](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191204004733339.png)

前端和后端因为来自不同的网域，所以在http的安全协议策略下，不信任

![image-20191204004306304](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191204004306304.png)

![image-20191204004940481](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191204004940481.png)

请求头、响应头都缺少：Access-Control-Allow-Origin

**解决方案** ：

1. 前端加入请求头
2. 后端Controller上加入@CrossOrigin跨域访问的注解

成功：

![image-20191204005916131](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191204005916131.png)

#### 商品平台属性的管理功能（增删改查）：

![image-20191204161810546](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191204161810546.png)

新建AttrController，AttrService，AttrServiceImpl及相应的Mapper，无太高技术含量，只记录一些要点。

Mapper.insert()与insertSelective()的区别：

| insert               | insertSelective    |
| -------------------- | ------------------ |
| 会将null插入到数据库 | null值不插入数据库 |

**修改操作：**

先修改平台属性，再改属性值

进入修改页面时，注意到前端调用请求http://127.0.0.1:8081/getAttrValueList?attrId=43

![image-20191204210143068](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191204210143068.png)

故实现getAttrValueList方法，为修改页面查询该平台属性的属性值值集合

![image-20191204210611056](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191204210611056.png)

成功请求，可发现修改度逻辑为：查询+保存（该保存与新增操作区别在于属性值已有id）

```java
String id=pmsBaseAttrInfo.getId();

if (StringUtils.isBlank(id)){
    //id为空时
    //保存属性
    pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);
    //保存属性值
    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
        pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
        pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
    }
}else {
    //id不为空时，修改

    //属性修改
    Example example=new Example(PmsBaseAttrInfo.class);
    example.createCriteria().andEqualTo("id",id);
    pmsBaseAttrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo,example);

    //属性值修改
    //按照属性ID删除所有属性值
    PmsBaseAttrValue pmsBaseAttrValueDel=new PmsBaseAttrValue();
    pmsBaseAttrValueDel.setAttrId(id);
    pmsBaseAttrValueMapper.delete(pmsBaseAttrValueDel);
	//删除后将新的属性值插入,若无新的属性值(全部删除)则将属性删除
    List<PmsBaseAttrValue> attrValueList=pmsBaseAttrInfo.getAttrValueList();
    if (attrValueList.isEmpty()){
        pmsBaseAttrInfoMapper.deleteByExample(example);
    }else {
        for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
            pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
        }
    }
}
```

### P43 商品SKU和SPU的概念、设计

商品模型设计：

1. 根据SKU、SPU电商模型设计
2. 根据电商用户检索和过滤的需求

![image-20191204223134635](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191204223134635.png)

##### SPU的查询功能

前台请求：

![image-20191204232402869](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191204232402869.png)

 仿照前面查询功能，返回PmsProductInfo列表即可。

##### SPU添加功能

1. SPU信息：名称、描述

2. SPU图片信息：图片的对象数据保存在分布式的文件存储服务器上（fastdfs）、图片元数据保存在数据库

- 用户在选择完图片后，将图片在用户提交的时候和其他商品spu信息一起提交到后台

- 用户选择图片时就将图片上传到服务器（目前主流，可减轻服务器压力）

3. SPU销售属性：

​		对应的表：pms_product_sale_attr、pms_product_sale_attr_value

| 商品平台属性     | 商品销售属性   |
| ---------------- | -------------- |
| 电商网站后台管理 | 由商家自行管理 |

4. 销售属性字典表

   商家在添加spu商品信息时，需要添加销售属性（自定义）

   在添加spu页面，商家先选择销售属性（平台后台自定义的销售属性字典表），然后自定义当前的销售商品属性值。

   ![image-20191205010430860](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191205010430860.png)





**处理前台请求1，获取基本销售属性列表：**

![image-20191205010831635](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191205010831635.png)

实现baseSaleAttrList查询，返回返回基本销售属性列表：

![image-20191205012056878](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191205012056878.png)



**处理前台请求2，保存PmsProductInfo：**

![image-20191205120525487](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191205120525487.png)

![image-20191205120628755](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191205120628755.png)

所提供的的材料中,前后端对接参数名不一致,以下冲突采用前端参数命名：

| 前端                 | 后端                        |
| -------------------- | --------------------------- |
| spuSaleAttrList      | pmsProductSaleAttrList      |
| spuSaleAttrValueList | pmsProductSaleAttrValueList |
| spuImageList         | pmsProductImageList         |



**处理前台请求3:，保存上传图片(fileUpload)：**

![image-20191205122952487](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191205122952487.png)

![image-20191205123214211](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191205123214211.png)

用户点击上传图片后，后台将图片传到服务器上，然后返回图片的访问路径给前端。

用户点击保存时，将SPU基本信息、销售属性列表、图片地址列表等元数据保存到后台



#### Fastdfs分布式文件存储

![image-20191206001728209](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191206001728209.png)

![image-20191207000126862](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191207000126862.png)

Fastdfs：阿里，开源免费

本项目采用：Nginx+FastDFS

##### 安装：

0. 安装前的准备：

   yum install gcc-c++ -y

   yum -y install zlib zlib-devel pcre pcre-devel gcc gcc-c++ openssl openssl-devel libevent libevent-devel perl unzip net-tools wget

##### P53 开始安装

将fastdfs所有压缩包上载到服务器/opt目录

```sh
chmod 777 *
```

![image-20191205170643455](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191205170643455.png)

1. fdfs依赖库：

   Libfastcommon

   a）解压安装压缩包

   ```bash
   [root@localhost opt]# tar -zxvf libfastcommonV1.0.7.tar.gz
   ```

   b）/make.sh

   ```bash
   [root@localhost opt]# cd libfastcommon-1.0.7/
   [root@localhost libfastcommon-1.0.7]# ./make.sh
   ```

   c）/make.sh install

   ```shell
   [root@localhost libfastcommon-1.0.7]# ./make.sh install
   ```

   d）用cp /usr/lib64/libfastcommon.so /usr/lib/ 将类库拷贝到/usr/lib目录

   ```shell
   [root@localhost libfastcommon-1.0.7]# cp /usr/lib64/libfastcommon.so /usr/lib/
   ```

2. fastdfs软件（tracker、storage）

   作为练习项目，只安装在一台机器上

   配置tracker

   配置storage

   (依赖于GCC、libevent、perl)

   a）新建目录

   ```shell
   mkdir /opt/fastdfs
   ```

   b）解压FastDFS压缩包到/usr/local

   ```sh
   
   ```

   c）进入解压目录

   ```sh
   cd fastdfs
   ```

   d）/make.sh

   e）/make.sh install

   f）进入conf配置文件目录将文件都拷贝到/etc/fdfs下

   ```sh
   cp * /etc/fdfs/
   ```

   g）进入/etc/fdfs/，配置tracker.conf，设置软件的数据和目录

   ```sh
   vim /etc/fdfs/tracker.conf
   ```

   ![image-20191205173802270](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191205173802270.png)

   h）storage的配置（不用安装，因为安装tracker时已经同时安装了）

   ```sh
   vim /etc/fdfs/storage.conf
   ```

   软件目录

   ![image-20191205174112279](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191205174112279.png)

   Storage存储文件的目录

   ```sh
   mkdir /opt/fastdfs/fdfs_storage 
   ```

   ![image-20191205174240918](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191205174240918.png)

   Storage的tracker的ip：改成本linux机的ip

   ![image-20191205174300669](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191205174300669.png)

   ![image-20191206151335305](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191206151335305.png)

   

3. 配置tracker和storage的启动服务

   **tracker**：

   新建目录并将安装目录中的启动脚本复制到其中

   ```sh
   [root@localhost local]# cd /opt/FastDFS
   [root@localhost FastDFS]# ls
   client  COPYING-3_0.txt  init.d   php_client  stop.sh  tracker
   common  fastdfs.spec     INSTALL  README.md   storage
   conf    HISTORY          make.sh  restart.sh  test
   [root@localhost FastDFS]# mkdir /usr/local/fdfs
   [root@localhost FastDFS]# cp restart.sh /usr/local/fdfs/
   [root@localhost FastDFS]# cp stop.sh /usr/local/fdfs/
   ```

   修改启动脚本

   ```sh
   vim /etc/init.d/fdfs_trackerd
   ```

   ![image-20191205224341265](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191205224341265.png)

   ![image-20191205224401802](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191205224401802.png)

   **storage**:

   配置storage启动服务，由于restart个stop脚本已经复制到/usr/local/fdfs，所以storage只需要配置/etc/init.d/fdfs_storage脚本即可

   ```sh
   vim /etc/init.d/fdfs_storaged
   ```

   修改处与fdfs_tracked相同

   **注册服务**

   ```sh
   [root@localhost opt]# cd /etc/init.d
   [root@localhost init.d]# chkconfig --add fdfs_trackerd 
   [root@localhost init.d]# chkconfig --add fdfs_storaged
   ```

   启动：

   ```sh
   [root@localhost init.d]# service fdfs_trackerd 
   用法：/etc/init.d/fdfs_trackerd {start|stop|status|restart|condrestart}
   [root@localhost init.d]# service fdfs_trackerd start
   Starting fdfs_trackerd (via systemctl):                    [  确定  ]
   [root@localhost init.d]# service fdfs_storaged start
   Starting fdfs_storaged (via systemctl):                    [  确定  ]
   ```

   显示成功启动：

   ![image-20191205234514840](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191205234514840.png)

   ##### 测试图片：

   修改/etc/fdfs/client.conf

   ```sh
   vim /etc/fdfs/client.conf
   ```

   ![image-20191205234900214](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191205234900214.png)

   然后我们先在根目录放一张测试图片：

   ![image-20191205235625676](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191205235625676.png)

   上传：

   ```sh
   /usr/bin/fdfs_test  /etc/fdfs/client.conf  upload  /root/test_pic.jpg
   ```

   上传成功后打印一堆日志信息，并有图片url

   ![image-20191206000025284](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191206000025284.png)

   图片路径：/opt/fastdfs/fdfs_storage/data/00/00

   但此时无法访问改地址，由于没配置nginx

4. FastDFS-nginx-module

   Fdfs整合nginx插件

   **解压插件压缩包**

   切换到/opt目录(压缩包所在目录)下

   ```sh
   tar -zxvf fastdfs-nginx-module_v1.16.tar.gz 
   ```

   **修改插件读取fdfs的目录(插件自身的配置文件)**

   ```sh
   vim fastdfs-nginx-module/src/config
   ```

   ​	将下图所示红框处原先路径改为图中路径(/usr/local/include -- usr/include)

   ![image-20191206005346033](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191206005346033.png)

   **将FastDFS-nginx-module插件整合fdfs的配置文件复制到fdfs配置目录下(整合fdfs的配置文件)**

   ```sh
   cp mod_fastdfs.conf /etc/fdfs/
   ```

   ```sh
   vim /etc/fdfs/mod_fastdfs.conf
   ```

   ![image-20191206010002903](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191206010002903.png)

   ![image-20191206010018674](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191206010018674.png)

   ![image-20191206010032556](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191206010032556.png)

   ![image-20191206010044410](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191206010044410.png)

   

5. nginx

   nginx作为web服务器，提供http请求服务	

   依赖：pcre-devel、zlib-devel

   **解压安装压缩包**

   cd /opt

   ```sh
   tar -zxvf nginx-1.12.2.tar.gz
   ```

   ```sh
   cd nginx-1.12.2
   ```

   然后将下面命令复制到命令行执行：

   ```sh
   ./configure \
   --prefix=/usr/local/nginx \
   --pid-path=/var/run/nginx/nginx.pid \
   --lock-path=/var/lock/nginx.lock \
   --error-log-path=/var/log/nginx/error.log \
   --http-log-path=/var/log/nginx/access.log \
   --with-http_gzip_static_module \
   --http-client-body-temp-path=/var/temp/nginx/client \
   --http-proxy-temp-path=/var/temp/nginx/proxy \
   --http-fastcgi-temp-path=/var/temp/nginx/fastcgi \
   --http-uwsgi-temp-path=/var/temp/nginx/uwsgi \
   --http-scgi-temp-path=/var/temp/nginx/scgi \
   --add-module=/opt/fastdfs-nginx-module/src
   ```

   成功后将显示：

   ![image-20191206011841082](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191206011841082.png)

   编译：

   ```sh
   make
   ```

   安装：

   ```sh
   make install
   ```

   编辑nginx.conf

   ```sh
   vim /usr/local/nginx/conf/nginx.conf
   ```

   ![image-20191206012856282](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191206012856282.png)

   别忘了创建临时目录：/var/temp/nginx/client

   ```sh
   mkdir -p /var/temp/nginx/client
   ```

   启动Nginx：

   ```sh
   cd /usr/local/nginx/sbin
   ```

   ```sh
   ./nginx
   ```

   

功能文件目录：

> Opt/fastdfs 软件数据存储目录
>
> Usr/local/fdfs 启动文件目录
>
> Etc/fdfs 配置文件目录
>
> Usr/bin/fdfs_trackerd 启动配置
>
> Etc/init.d/fdfs_trackerd 启动服务脚本

##### Fdfs和SpringBoot整合

1. 从git上克隆fastdfs-client-java

```bash
 git clone https://github.com/happyfish100/fastdfs-client-java
```

2. 将fdfs-client打包到本地maven仓库中

   将克隆到本地的文件夹复制到项目目录下，单击选中，然后点击左上角"file"=>"new"=>"Module from ..."

   ![image-20191207003125526](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191207003125526.png)

   ![image-20191207003032131](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191207003032131.png)

   然后一直下一步，将其设置为maven项目

3. maven install

   ![image-20191207003530648](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191207003530648.png)

   ![image-20191207003727962](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191207003727962.png)

4. 在common-util中引入已经打包好的maven依赖：

   ```xml
   <!--引入FastDFS-->
   <dependency>
       <groupId>org.csource</groupId>
       <artifactId>fastdfs-client-java</artifactId>
       <version>1.27-SNAPSHOT</version>
   </dependency>
   ```

5. manage-web模块下添加配置文件tracker.conf

   ![image-20191207005651291](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191207005651291.png)

   ```conf
   tracker_server=192.168.226.129:22122
   
   # 连接超时时间，针对socket套接字函数connect，默认为30秒
   connect_timeout=30000
   
   # 网络通讯超时时间，默认是60秒
   network_timeout=60000
   ```

6. 运行如下测试代码:

   ```java
   @Test
   public void textFileUpload() throws IOException, MyException {
       //配置fdfs的全局链接地址
       String file = this.getClass().getResource("/tracker.conf").getFile();
       ClientGlobal.init(file);
       TrackerClient trackerClient=new TrackerClient();
       //获得一个trackerServer实例
       TrackerServer trackerServer=trackerClient.getConnection();
       //通过tracker获得一个storage的链接客户端
       StorageClient storageClient=new StorageClient(trackerServer,null);
       //上传图片
       String orginalFilename="C:\\Users\\Apollos\\Desktop\\文史研究\\timg.jpg";
       String[] upload_file = storageClient.upload_file(orginalFilename, "jpg", null);
       //打印返回信息
       for (int i = 0; i < upload_file.length; i++) {
           String s = upload_file[i];
           System.out.println("s = " + s);
       }
   }
   ```

   成功：

   ![image-20191207011343367](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191207011343367.png)

### P67 SKU管理



##### SKU表间的关联关系

- pms_sku_info

![image-20191222223807029](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191222223807029.png)

- pms_sku_attr_value（attr_id：对应的平台属性id，value_id：对应的平台属性值的id）

![image-20191222224613436](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191222224613436.png)

- `pms_base_attr_info`平台属性表

![image-20191222224712039](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191222224712039.png)

- pms_base_attr_value平台属性值表

![image-20191222224805720](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191222224805720.png)

pms_sku_img(从已添加的spu图片中选取，当前sku涉及的图片：比如，IPhoneX spu图片30张 ，其中包含 IPhoneX黑色 sku图片7张)

##### SKU的保存

![](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191222231154470.png)

点击进入添加sku页面后，我么发现：

1. 已有attrInfoList方法，但返回的BaseAttrInfo中没有pmsBaseAttrValueList

   在其serviceImpl中完善一下:

   ![image-20191222231444755](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191222231444755.png)

   ```java
   @Override
   public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {
       PmsBaseAttrInfo pmsBaseAttrInfo=new PmsBaseAttrInfo();
       pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
       List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);
       for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfos) {
   
           List<PmsBaseAttrValue> pmsBaseAttrValues;
           PmsBaseAttrValue pmsBaseAttrValue=new PmsBaseAttrValue();
           pmsBaseAttrValue.setAttrId(baseAttrInfo.getId());
           pmsBaseAttrValues=pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
   
           baseAttrInfo.setAttrValueList(pmsBaseAttrValues);
       }
       return pmsBaseAttrInfos;
   }
   ```

   然后可以看见，平台属性被添加上来了:

   ![image-20191223114310890](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191223114310890.png)

2. 未实现方法:

   ![](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191222231444755.png)

   > 请求销售属性列表：http://127.0.0.1:8081/spuSaleAttrList?spuId=24
   >
   > 请求spu图片列表:http://127.0.0.1:8081/spuImageList?spuId=24

   依次实现，较为简单不做赘述

   

   保存：

   ![](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20191223003155098.png)

> 保存sku:http://127.0.0.1:8081/spuImageList?spuId=24

​	为sku添加controller及service

​	注意：此项目前后端部分字段对不上，我们改后端，折中处理

​	bean:

```java
public class PmsSkuInfo implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column
    String id;

    @Column
    String productId;

    @Transient
    String spuId;	//临时字段
```

​	controller:

```java
@RequestMapping("saveSkuInfo")
@ResponseBody
public String saveSkuInfo(@RequestBody PmsSkuInfo pmsSkuInfo){
    //前后端属性名对不上，故将前端spuId赋值给productId
    pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());
    //前端没有默认图片校验，我们加上验证
    String skuDefaultImg=pmsSkuInfo.getSkuDefaultImg();
    if (StringUtils.isBlank(skuDefaultImg)){
        pmsSkuInfo.setSkuDefaultImg(pmsSkuInfo.getPmsSkuImageList().get(0).getImgUrl());
    }

    skuService.saveSkuInfo(pmsSkuInfo);
    return "success";
}
```

service:

```java
@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;
    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;
    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;

    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        //插入skuInfo
        int i = pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String skuId=pmsSkuInfo.getId();

        // 插入平台属性关联
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }

        // 插入销售属性关联
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }

        // 插入图片信息
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }
    }
}
```





### P71 电商前台系统

![image-20200122191846858](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200122191846858.png)

##### 1. Item商品详情 介绍

电商前台页面是给消费者用户看的，前台系统压力更大，多用缓存、负载均衡，注意数据的一致性	   
后台页面是给管理人员或商家看的。压力小，直连数据库，注意数据的一致性   
前台后台联系：数据结构是同一套，后台系统的一些设置可以控制前台系统的运行方式

##### 2. 前台系统中包含的业务功能

首页（静态化）   
检索页（搜索引擎）   
详情页（缓存、切换、推荐）   
购物车页（cookie、redis）   
结算页（一致性校验、安全）   
支付页

##### 3.Item功能的创建

创建一个item-web的商品详情工程   
配置item-web，pom，properties   
引入thymeleaf依赖   

```html
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:th="http://www.thymeleaf.org">
```

**设置热部署，松校验**

gmall-web-util模块下的pom.xml:

```xml
<!--thymeleaf支持包，可以设置松校验、热部署-->
<dependency>
    <groupId>net.sourceforge.nekohtml</groupId>
    <artifactId>nekohtml</artifactId>
</dependency>

<dependency>
    <groupId>xml-apis</groupId>
    <artifactId>xml-apis</artifactId>
</dependency>

<dependency>
    <groupId>org.apache.xmlgraphics</groupId>
    <artifactId>batik-ext</artifactId>
</dependency>
```

gmall-item-web模块下的properties文件:

```properties
# 关闭thymeleaf的缓存(热部署)
spring.thymeleaf.cache=false
# 松校验
spring.thymeleaf.mode=LEGACYHTML5
```

#####  4.前台sku数据展示

![image-20200205045124458](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200205045124458.png)

引入商品详情前端静态资源。

查看item.html中需要后端传入的值，做好前后端对接：

![image-20200205045316903](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200205045316903.png)

包括：skuInfo、skuImage、spuSaleAttr、saleAttrValue等



**sku根据销售属性动态切换**

![image-20200206042503842](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200206042503842.png)

1. 数据模型（根据销售属性切换一个sku的其他兄弟姐妹）

   pms_sku_info、pms_sku_image   
   pms_sku_sale_attr_value   
   pms_sale_attr   
   pms_spu_sale_attr_value

2. 如何实现这些数据模型对应的业务功能

   A）页面根据销售属性列表(当前sku对应的spu的id)    

   ​	pms_spu_sale_attr   
   ​	pms_product_sale_attr   

   ```mysql
   select * from
   	pms_product_sale_attr sa,
   	pms_product_sale_attr_value sav
   where
   	sa.product_id=sav.product_id
   and sa.sale_attr_id=sav.sale_attr_id
   and sa.product_id=?
   ```

   B）页面根据销售属性的选择的组合，定位到关联的sku

   ​	通过页面被选中属性值的id查询中间表：pms_sku_sale_attr_value得到skuId

   ```sql
   SELECT sku_id FROM
   	pms_sku_sale_attr_value ssav
   WHERE 
   	ssav.sale_attr_id IN (?,?)      # 销售属性id1，销售属性id2
   AND ssav.sale_attr_value_id IN (?,?)
   ```

   C）根据skuId查询到sku对象，返回到页面

   ABC:这样做的坏处是一共要向后台发送两次查询请求，极度低效

   

   D）功能优化：

   在用户进入某一个spu领域后，将该spu所包含的sku们和这些对应的销售属性值生成一个键值对（key为销售属性值的组合，value为skuid）hash表格，放到页面上

   ![image-20200206053013430](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200206053013430.png)

   在用户选择完销售属性后，根据hash组合找到对应的skuId
   
   升级后的查询SQL:
   
   ```sql
   SELECT
   	sa.*,sav.*,if(ssav.sku_id,1,0) as isChecked
   FROM
   	pms_product_sale_attr sa
   	INNER JOIN pms_product_sale_attr_value sav ON sa.product_id = sav.product_id 
   	AND sa.sale_attr_id = sav.sale_attr_id 
   	AND sa.product_id = ?
   	LEFT JOIN pms_sku_sale_attr_value ssav ON sav.id = ssav.sale_attr_value_id 
   	AND ssav.sku_id = ?
   ```
   
   ![image-20200210080732579](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200210080732579.png)
   
   ![image-20200210080808639](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200210080808639.png)
   
   **实现：**
   
   ItemController:
   
   ```java
   @RequestMapping("{skuId}.html")
   public String item(@PathVariable String skuId, ModelMap map){
       //sku对象
       PmsSkuInfo skuInfo=skuService.getSkuById(skuId);
       map.put("skuInfo",skuInfo);
       //销售属性列表
       List<PmsProductSaleAttr> pmsProductSaleAttrs=spuService.spuSaleAttrListCheckBySku(skuInfo.getProductId(),skuId);
       map.put("spuSaleAttrListCheckBySku",pmsProductSaleAttrs);
       return "item";
   }
   ```
   
   SpuService:
   
   ```java
   List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId,String skuId);
   ```
   
   SpuServiceImpl:
   
   ```java
   @Override
   public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId,String skuId) {
       List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductSaleAttrMapper.selectSpuSaleAttrListCheckBySku(productId,skuId);
       return pmsProductSaleAttrs;
   }
   ```
   
   PmsProductSaleAttrMapper:
   
   ```java
   public interface PmsProductSaleAttrMapper extends Mapper<PmsProductSaleAttr> {
       List<PmsProductSaleAttr> selectSpuSaleAttrListCheckBySku(@Param("productId") String productId, @Param("skuId") String skuId);
   }
   ```
   
   gmall-manage-service的resources下的mapper文件夹中，PmsProductSaleAttrMapper.xml:
   
   ```xml
   <?xml version="1.0" encoding="UTF-8" ?>
   <!DOCTYPE mapper
           PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
           "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
   <mapper namespace="com.atguigu.gmall.manage.mapper.PmsProductSaleAttrMapper">
       <select id="selectSpuSaleAttrListCheckBySku" resultMap="selectSpuSaleAttrListCheckBySkuMap">
           SELECT
               sa.id as sa_id,sav.id as sav_id,sa.*,sav.*,if(ssav.sku_id,1,0) as isChecked
           FROM
               pms_product_sale_attr sa
               INNER JOIN pms_product_sale_attr_value sav ON sa.product_id = sav.product_id
               AND sa.sale_attr_id = sav.sale_attr_id
               AND sa.product_id = #{productId}
               LEFT JOIN pms_sku_sale_attr_value ssav ON sav.id = ssav.sale_attr_value_id
               AND ssav.sku_id = #{skuId}
       </select>
       
       <resultMap id="selectSpuSaleAttrListCheckBySkuMap" type="com.atguigu.gmall.bean.PmsProductSaleAttr" autoMapping="true">
           <result column="sa_id" property="id"/>
           <collection property="spuSaleAttrValueList" ofType="com.atguigu.gmall.bean.PmsProductSaleAttrValue" autoMapping="true">
               <result column="sav_id" property="id"/>
           </collection>
       </resultMap>
   </mapper>
   ```
   
   

##### 5.将后台查出的同属一个spu的sku与其sku销售属性的列表组合成一个静态的hash表

1. sql查询出sku和关联的销售属性

```sql
SELECT
	si.id,
	ssav.sale_attr_value_id 
FROM
	pms_sku_info si,
	pms_sku_sale_attr_value ssav 
WHERE
	si.product_id = 74 
	AND si.id = ssav.sku_id;
```

2. 实现：

   ItemControlelr:

   ```java
   //查询当前sku所属spu的其他sku集合的hash表
   Map<String,String> skuSaleAttrHash=new HashMap<>();
   List<PmsSkuInfo> pmsSkuInfos=skuService.getSkuSaleAttrValueListBySpu(skuInfo.getProductId());
   
   for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
       String k="";
       String v=pmsSkuInfo.getId();
       List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
       for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
           k+=pmsSkuSaleAttrValue.getSaleAttrValueId()+"|";
       }
       skuSaleAttrHash.put(k,v);
   }
   //将sku销售属性hash放到页面
   String skuSaleAttrHashJsonStr = JSON.toJSONString(skuSaleAttrHash);
   map.put("skuSaleAttrHashJsonStr",skuSaleAttrHashJsonStr);
   ```

   SkuServiceImpl:

   ```java
   @Override
   public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {
       List<PmsSkuInfo> skuInfos = pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);
       return skuInfos;
   }
   ```

   PmsSkuInfoMapper.xml:

   ```xml
   <?xml version="1.0" encoding="UTF-8" ?>
   <!DOCTYPE mapper
           PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
           "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
   <mapper namespace="com.atguigu.gmall.manage.mapper.PmsSkuInfoMapper">
       <select id="selectSkuSaleAttrValueListBySpu" resultMap="selectSkuSaleAttrValueListBySpuMap">
           SELECT
               si.*,ssav.*,
               si.id as si_id,
               ssav.id as ssav_id
           FROM
               pms_sku_info si,
               pms_sku_sale_attr_value ssav
           WHERE
               si.product_id = #{productId}
               AND si.id = ssav.sku_id;
       </select>
       
       <resultMap id="selectSkuSaleAttrValueListBySpuMap" type="com.atguigu.gmall.bean.PmsSkuInfo" autoMapping="true">
           <result column="si_id" property="id"/>
           <collection property="skuSaleAttrValueList" ofType="com.atguigu.gmall.bean.PmsSkuSaleAttrValue" autoMapping="true">
               <result column="ssav_id" property="id"/>
           </collection>
       </resultMap>
   </mapper>
   ```



##### 6.使用缓存redis解决页面并发问题

1. 缓存使用的简单方式

   连接缓存、查询缓存、如果缓存中没有则查询mysql、mysql查询结果存入redis

2. redis的整合步骤

   A）将redis整合到项目中（redis+spring）

   B）设计数据存储策略（核心就是如何设计key）

   ​	企业中的存储策略：   
   ​	数据对象名：数据对象id：对象属性   
   ​	eg：User:123:password，User:123:username，sku:108:info

   C

##### 7.redis的整合过程

1. 引入pom依赖（本工程所有redis统一放入service-util中）

   ```xml
   <!--redis-->
   <dependency>
       <groupId>redis.clients</groupId>
       <artifactId>jedis</artifactId>
   </dependency>
   ```

2. 写一个redis的工具类（将redis池初始化到spring容器中）

   ```java
   public class RedisUtil {
       private  JedisPool jedisPool;
       public void initPool(String host,int port ,int database){
           JedisPoolConfig poolConfig = new JedisPoolConfig();
           poolConfig.setMaxTotal(200);				//最大连接数200
           poolConfig.setMaxIdle(30);		
           poolConfig.setBlockWhenExhausted(true);
           poolConfig.setMaxWaitMillis(10*1000);		//延迟时间10s
           poolConfig.setTestOnBorrow(true);
           jedisPool=new JedisPool(poolConfig,host,port,20*1000);
       }
       public Jedis getJedis(){
           Jedis jedis = jedisPool.getResource();
           return jedis;
       }
   }
   ```

3. 写一个Spring整合redis的配置类

   将redis的连接池创建到spring容器中

   ```java
   @Configuration
   public class RedisConfig {
       //读取配置文件中的redis的ip地址
       @Value("${spring.redis.host:disabled}")
       private String host;
   
       @Value("${spring.redis.port:0}")
       private int port;
   
       @Value("${spring.redis.database:0}")
       private int database;
       
       @Bean
       public RedisUtil getRedisUtil(){
           if(host.equals("disabled")){
               return null;
           }
           RedisUtil redisUtil=new RedisUtil();
           redisUtil.initPool(host,port,database);
           return redisUtil;
       }
   }
   ```

4. 注意：每个应用工程引入service-util后，单独配置自己的redis文件，service-util模块中的配置文件没有作用

5. 注意：主应用要放在项目结构最外面（至少跟config和util平级）才能访问到config和util中的内容：

   ![image-20200211104743527](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200211104743527.png)

   ![image-20200211104829919](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200211104829919.png)

##### 8.redis常见问题&注意事项

1. 报错：cont get a connection from the pool

2. 命令：

   启动redis：

   ```sh
   redis-server
   ```

   查看redis是否启动

   ```sh
   redis-cli （或：redis-cli -h 本机IP -p 6379）
   ```

3. **缓存在高并发和安全压力下的一些问题：**

   **缓存击穿：**

   ​	对于一些设置了过期时间的key，如果这些key可能会在某些时间点被超高并发访问，则称他们为一些非常”热点“的数据。需考虑到：如果这个key在大量请求同时进来之前正好失效，那么所有对这个key的查询都落在db上，这种现象称之为缓存击穿。

   **缓存穿透**：

   ​	指 **利用redis和mysql的机制（redis缓存一旦不存在就访问mysql）去查询一个不存在的数据**，直接绕过缓存访问mysql制造db请求压力，由于缓存不命中，将去查询数据库，但是数据库中也无此记录，并且处于容错考虑，我们没有将这次查询的null写入缓存，这将导致这个不存在的数据每次请求都要到存储层查询，失去了缓存的意义。**流量大时，可能DB就挂掉了**。漏洞所在：有人利用不存在的key频繁地攻击我们的应用。

   ​	解决：一般在代码中防止该现象——空结果进行缓存，但它的过期时间很短，最长不超5min

   **缓存雪崩：**

   ​	指 在我们设置缓存时采用了相同的过期时间，导致缓存在某一时刻同时失效，请求全部转发到DB上，DB瞬时压力过重。（多个热点key同时失效）

   ​	解决：原有的失效时间基础之上增加一个随机值，比如1-5min随机，这样每一个缓存的过期时间的重复率会降低，很难遇到集体失效的情况

##### 9. 如何解决缓存击穿问题

![image-20200212012428994](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200212012428994.png)

使用redis数据库的分布式锁，解决mysql的访问压力问题

两种分布式锁：

​	![image-20200212105008218](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200212105008218.png)

1. **redis自带分布式锁**：set px nx

   ![image-20200212105924576](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200212105924576.png)

    

   **两种意外情况：**

   问题1：如果在redis中的锁已经过期，然后锁过期的那个请求又执行完毕，回来删锁，删除了其他线程的锁，怎么解决？   
   解决方案：生成UUID字符串 token ，将自己锁的value值设置为token，回来删锁时先确保value值为token再删除

   问题2：如果碰巧在查询redis锁还没删除的时候，正在网络传输，锁过期了，怎么办？

   ![image-20200213004042057](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200213004042057.png)

   解决方案：执行lua脚本来删除锁，能够将获得token值与判断删除合并为一个原子性操作

   ![image-20200213004646769](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200213004646769.png)

   

   整合redis到本项目中：

   ```java
   @Override
   public PmsSkuInfo getSkuById(String skuId) {
       PmsSkuInfo skuInfo=null;
       //连接缓存
       Jedis jedis=redisUtil.getJedis();
   
       //查询缓存
       String skuKey="sku:"+skuId+":info";
       String skuJson=jedis.get(skuKey);
   
       if (StringUtils.isNotBlank(skuJson)){
           skuInfo= JSON.parseObject(skuJson,PmsSkuInfo.class);
       }else {
           //如果缓存中没有，查询Mysql
           //设置分布式锁
           String token= UUID.randomUUID().toString();
           String OK = jedis.set("sku:"+skuId+":lock", token, "nx", "px", 10);//拿到锁的线程有10s的过期时间
           if (StringUtils.isNotBlank(OK)&&OK.equals("OK")){
               //设置成功，有权在10s的过期时间内访问数据库
               skuInfo=getSkuByIdFromDb(skuId);
               //                try {
               //                    Thread.sleep(5000);
               //                } catch (InterruptedException e) {
               //                    e.printStackTrace();
               //                }
               //将mysql查询结果存入redis
               if (skuInfo!=null){
                   jedis.set(skuKey,JSON.toJSONString(skuInfo));
               }else{
                   //数据库中不存在该sku
                   //为了防止缓存穿透，null或空字符串值设置给redis
                   jedis.setex(skuKey,60*3,JSON.toJSONString(""));
               }
               //在访问mysql后，将分布式锁释放
               String lockToken = jedis.get("sku:" + skuId + ":lock");
               if (StringUtils.isNotBlank(lockToken)&&lockToken.equals(token)){
                   jedis.del("sku:"+skuId+":lock");    //用token字段确认删除的是自己sku的锁
               }
           }else {
               //设置失败，自旋（该线程睡眠几秒后重新尝试访问）
               getSkuById(skuId);
           }
       }
   
       jedis.close();
       return skuInfo;
   }
   ```

2. **redisson框架**：一个redis的 juc (java.util.concurrent) 的lock功能的客户端的实现（既有jedis功能，又有juc功能）

   Redisson是一个在Redis的基础上实现的Java驻内存数据网格（In-Memory Data Grid）。它不仅提供了一系列的分布式的Java常用对象，还提供了许多分布式服务。其中包括(BitSet, Set, Multimap, SortedSet, Map, List, Queue, BlockingQueue, Deque, BlockingDeque, Semaphore, Lock, AtomicLong, CountDownLatch, Publish / Subscribe, Bloom filter, Remote service, Spring cache, Executor service, Live Object service, Scheduler service) Redisson提供了使用Redis的最简单和最便捷的方法。Redisson的宗旨是促进使用者对Redis的关注分离（Separation of Concern），从而让使用者能够将精力更集中地放在处理业务逻辑上。

   **引入Maven依赖：**(service-util pom)

   ```java
   <!-- https://mvnrepository.com/artifact/org.redisson/redisson -->
   <dependency>
       <groupId>org.redisson</groupId>
       <artifactId>redisson</artifactId>
       <version>3.10.5</version>
   </dependency>
   ```

   配置

   ```properties
   spring.redis.host=192.168.159.130
   spring.redis.port=6379
   ```

   配置类：

   ```java
   @Configuration
   public class GmallRedissonConfig {
   
       @Value("${spring.redis.host}")
       private String host;
   
       @Value("${spring.redis.port}")
       private String port;
   
       @Bean
       public RedissonClient redissonClient(){
           Config config = new Config();
           config.useSingleServer().setAddress("redis://"+host+":"+port);
           RedissonClient redisson = Redisson.create(config);
           return redisson;
       }
   }
   ```

   Redisson实现了JUC的lock锁，并且可以在分布式redis环境下使用

   **压力测试：**

![image-20200213190221163](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200213190221163.png)

我们创建一个测试项目：gmall-redisson-test，为其 添加Controller：

```java
@Controller
public class RedissonController {
    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redissonClient;

    @RequestMapping("testRedisson")
    @ResponseBody
    public String testRedisson(){
        Jedis jedis=redisUtil.getJedis();
//        RLock lock=redissonClient.getLock("lock");
        String v=jedis.get("k");
        if (StringUtils.isBlank(v)){
            v="1";
        }
        System.out.println(v);
        jedis.set("k",Integer.parseInt(v)+1+"");
        jedis.close();
        return "success";
    }
}
```

**redisson-nginx负载均衡配置**

> Nginx-windows安装：https://www.cnblogs.com/jiangwangxiang/p/8481661.html

nginx.conf:

```sh
upstream redisTest{
    server	127.0.0.1:8080 weight=3;
    server	127.0.0.1:8081 weight=3;
    server	127.0.0.1:8082 weight=3;
}
    
server {
    listen       80;
    location / {
    #root   html;
    proxy_pass http://redisTest;
    index  index.html index.htm;
}
```

在nginx目录下开启cmd窗口，启动Nginx：

```sh
start nginx
```

Nginx关闭命令：

```sh
nginx -s stop 或 nginx -s quit
```

访问localhost/testRedisson即可

**下载Apache测试工具**

> windows下载链接：https://www.apachehaus.com/cgi-bin/download.plx

![image-20200214032954828](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200214032954828.png)

解压后，找到安装目录下的httpd.conf，修改为自己的安装目录

![image-20200214035343079](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200214035343079.png)

![image-20200214043122367](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200214043122367.png)

![image-20200214043625639](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200214043625639.png)

**启动Apache服务：**

查看被占用的端口：

```sh
netstat -ano | findstr "443"(端口号)
```

Apache启动时443端口经常被占用，我们把它改成442

> 启动Apache会占用443端口，而443被其他程序占用了。我们只需将Apache默认端口443改掉就行。网上搜了一下，说是更改Apache24\conf\extra\httpd-ssl.conf 文件中的443端口，我试了下，并没有什么用。
>
> 经过一番研究，在httpd.conf看到ssl_module引用的是httpd-ahssl.conf。然后找到httpd-ahssl.conf，修改文件里的443为442即可。

![image-20200214052537154](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200214052537154.png)

![image-20200214052727160](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200214052727160.png)

**压力测试：**

![image-20200214052916070](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200214052916070.png)

先清除redis中已有的kv缓存：

![image-20200214053251755](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200214053251755.png)

开始测试（不加锁）：

![image-20200214054033272](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200214054033272.png)

加锁：

```java
@RequestMapping("lockTest")
@ResponseBody
public String lockTest(){
    Jedis jedis = redisUtil.getJedis();// redis链接
    RLock lock = redissonClient.getLock("redis-lock");//分布锁
    //加锁
    lock.lock();
    try {
        String v = jedis.get("k");//获取value
        System.err.print("==>"+v);//打印value
        if(StringUtil.isBlank(v)){
            v = "1";
        }
        int inum = Integer.parseInt(v);//获得value的值
        jedis.set("k", inum+1+"");//value增加1
        jedis.close();
    } finally {
        lock.unlock();
    }
    return "success";
}
```

![image-20200214060626176](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200214060626176.png)

补充：**AB命令参数：**

> -n 即requests，用于指定压力测试总共的执行次数
>
> -c 即concurrency，用于指定压力测试的并发数。
>
> -t即timelimit，等待响应的最大时间(单位：秒)。
>
> -b即windowsize，TCP发送/接收的缓冲大小(单位：字节)。
>
> -p即postfile，发送POST请求时需要上传的文件，此外还必须设置-T参数。
>
> -u即putfile，发送PUT请求时需要上传的文件，此外还必须设置-T参数。
>
> -T即content-type，用于设置Content-Type请求头信息，例如：application/x-www-form-urlencoded，默认值为text/plain。
>
> -v即verbosity，指定打印帮助信息的冗余级别。
>
> -w以HTML表格形式打印结果。
>
> -i使用HEAD请求代替GET请求。
>
> -x插入字符串作为table标签的属性。
>
> -y插入字符串作为tr标签的属性。
>
> -z插入字符串作为td标签的属性。
>
> -C添加cookie信息，例如："Apache=1234"(可以重复该参数选项以添加多个)。
>
> -H添加任意的请求头，例如："Accept-Encoding: gzip"，请求头将会添加在现有的多个请求头之后(可以重复该参数选项以添加多个)。
>
> -A添加一个基本的网络认证信息，用户名和密码之间用英文冒号隔开。
>
> -P添加一个基本的代理认证信息，用户名和密码之间用英文冒号隔开。
>
> -X指定使用的代理服务器和端口号，例如:"126.10.10.3:88"。
>
> -V打印版本号并退出。
>
> -k使用HTTP的KeepAlive特性。
>
> -d不显示百分比。
>
> -S不显示预估和警告信息。
>
> -g输出结果信息到gnuplot格式的文件中。
>
> -e输出结果信息到CSV格式的文件中。
>
> -r指定接收到错误信息时不退出程序。
>
> -h显示用法信息，其实就是ab -help。



### P109 商城搜索：ElasticSearch

#### 介绍：

##### 1. 搜索引擎

elasticSearch6（和elasticSearch5的区别在于，root用户权限、一个库是否能建立多个表）

文本搜索（以空间换时间的算法）

与同类产品（solr、Hermes）相比，elasticSearch和solr都是基于Lucene（Apache），默认以集群方式工作

搜索引擎 的工作原理是什么？（以百度和google为例）

	1. 爬取
 	2. 分析
 	3. 查询

ElasticSearch（搜索引擎）的算法

​	倒排索引：在内容上建立索引，用内容去匹配索引      
​	B-Tree、B+ Tree

**2.安装**

​	安装环境：CentOS7+jdk8   
​	配置文件：elasticSearch.yml、jvmOptions.yml

创建目录、上传、解压：

```sh
mkdir -p /opt/es
```

![image-20200215074729424](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200215074729424.png)

```sh
cd es
chmod 777 *
```

```sh
tar -zxvf elasticsearch-6.3.1.tar.gz
```

配置：

​	启动：切换到elasticsearch-6.3.1的bin目录下，输入./elasticsearch

![image-20200215080730786](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200215080730786.png)

​	可见：需要非root用户才能启动，我们为es创建一个用户：

```sh
adduser es
su es
```

​	切换用户后启动也会报错：

![image-20200215081254330](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200215081254330.png)

​	我们需要配置如下两个文件（配置时需要切换回root）：

![image-20200215081456409](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200215081456409.png)

​	**jvm.options**（jvm配置文件）:

![image-20200215081754910](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200215081754910.png)

​	**elasticsearch.yml**（集群配置文件）（elasticsearch无法访问data目录：es的软件和日志数据目录）（如果不用root用户解压，可直接用es用户解压，解压后可避免配置权限）:	   

1. 给用户授权：chown -R es:es /opt/es/elasticserch-6.3.1/
2. 切换用户：su es
3. 通过命令重写启动服务：sh /opt/mysoft/elasticsearch/bin/elasticsearch -d
4. 输入：curl http://localhost:9200 测试能够正常访问

![image-20200215122349067](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200215122349067.png)

5. 修改conf/elasticsearch.yml ，配置host地址（配置成本机地址，允许访问）：

![image-20200215115116549](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200215115116549.png)

6. 重启elasticsearch：

   ps -ef | grep elastic 查找并杀死进程

   切换到bin目录启动：./elasticsearch

7. 启动后将会报错，因为es使用的最大线程数、最大内存数、访问的最大文件数皆不足

![image-20200215121818138](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200215121818138.png)

8. 修改linux的配置（配合es的启动需求）：

   A）修改linux的limits配置文件，设置内存、线程和文件

   nofile - 打开文件的最大数目   
   noproc - 进程的最大数目   
   soft - 指当前系统生效的设置值   
   hard - 表明系统中所能设定的最大值

   当设置为：

   ``` sh
   * hard nofile 655360
   * soft nofile 131072
   * hard nproc 4096
   * soft nproc 2048
   ```

   ```sh
   vim /etc/security/limits.conf 
   ```

   ![image-20200216040504009](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200216040504009.png)

   设置完后，保存并退出，然后输入：

   ```sh
   source  /etc/security/limits.conf 
   ```

   然后修改最大内存：

   ```sh
   vim /etc/sysctl.conf
   ```

   ![image-20200216040932930](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200216040932930.png)

   设置完成后，保存退出，然后输入如下命令使之生效：

   ```sh
   sysctl -p
   ```

   然后切换回es用户，就可正常启动elasticsearch了我们，我们希望日志信息不输出在命令行（后台启动）：

   ```sh
nohup ./elasticsearch &
   ```

   ![image-20200216042018592](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200216042018592.png)
   
   **总结：**
   
   整个es的配置有四处文件需要修改：    
elasticsearch.yml		es启动的host地址    
   jvm.options				  配置es的虚拟机内存   
   limits.conf					 配置linux的线程内存和文件   
   sysctl.conf					 配置系统允许的软件运行内存

##### 2.Search API

1. elasticsearch通过（9200端口）http协议进行交互

2. 基本概念

   **全文搜索：**

   全文检索是指计算机索引程序通过扫描文章中的每一个词，对每一个词建立一个索引，指明该词在文章中出现的次数和位置，当用户查询时，检索程序就根据事先建立的索引进行查找，并将查找的结果反馈给用户的检索方式。

   **倒排索引（Inverted Index）：**

   该索引表中的每一项都包括一个属性值和具有该属性值的各记录的地址。由于不是由记录来确定属性值，而是由属性值来确定记录的位置，因而称为倒排索引(inverted index)。Elasticsearch能够实现快速、高效的搜索功能，正是基于倒排索引原理。

   **节点&集群（Node & Cluster）：**

   Elasticsearch 本质上是一个分布式数据库，允许多台服务器协同工作，每台服务器可以运行多个Elasticsearch实例。单个Elasticsearch实例称为一个节点（Node），一组节点构成一个集群（Cluster）。

   **索引（Index）：**

   Elasticsearch 数据管理的顶层单位就叫做 Index（索引），相当于关系型数据库里的数据库的概念。另外，每个Index的名字必须是小写。

   **文档（Document）：**

     Index里面单条的记录称为 Document（文档）。许多条 Document 构成了一个 Index。Document 使用 JSON 格式表示。同一个 Index 里面的 Document，不要求有相同的结构（scheme），但是最好保持相同，这样有利于提高搜索效率。

   **类型（Type）：**

     Document 可以分组，比如employee这个 Index 里面，可以按部门分组，也可以按职级分组。这种分组就叫做 Type，它是虚拟的逻辑分组，用来过滤 Document，类似关系型数据库中的数据表。
     不同的 Type 应该有相似的结构（Schema），性质完全不同的数据（比如 products 和 logs）应该存成两个 Index，而不是一个 Index 里面的两个 Type（虽然可以做到）。

   **文档元数据（Document metadata）：**

     文档元数据为_index, _type, _id, 这三者可以唯一表示一个文档，_index表示文档在哪存放，_type表示文档的对象类别，_id为文档的唯一标识。

   **字段（Fields）：**

     每个Document都类似一个JSON结构，它包含了许多字段，每个字段都有其对应的值，多个字段组成了一个 Document，可以类比关系型数据库数据表中的字段。
     在 Elasticsearch 中，文档（Document）归属于一种类型（Type），而这些类型存在于索引（Index）中，下图展示了Elasticsearch与传统关系型数据库的类比：

   ![img](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/webp)

   | Index    | 库             |
   | -------- | -------------- |
   | Type     | 表             |
   | Document | 行（一条数据） |
   | Field    | 字段           |

3. 开发工具Kibana

   Kibana 是为 Elasticsearch设计的开源分析和可视化平台。你可以使用 Kibana 来搜索，查看存储在 Elasticsearch 索引中的数据并与之交互。你可以很容易实现高级的数据分析和可视化，以图标的形式展现出来。

   安装：首先解压压缩包

   ![image-20200217031144801](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200217031144801.png)

   然后进入kibana.yml配置kibana的es信息：

   ![image-20200217031527191](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200217031527191.png)

    启动kibana：

   ![image-20200217031657677](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200217031657677.png)

   （停止kibana）：

   ![image-20200217032301229](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200217032301229.png)

   kibana端口号为5601：

   ![image-20200217032453019](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200217032453019.png)

##### 3.ES简单的增删改查

ES采用RestfulAPI进行增删改查

在

**新增PUT**：

PUT /（index，索引，库）/（type，表）/id

```html
PUT /movie_index/movie/1
{ "id":1,
  "name":"operation red sea",
  "doubanScore":8.5,
  "actorList":[  
{"id":1,"name":"zhang yi"},
{"id":2,"name":"hai qing"},
{"id":3,"name":"zhang han yu"}
]
}

PUT /movie_index/movie/2
{
  "id":2,
  "name":"operation meigong river",
  "doubanScore":8.0,
  "actorList":[  
{"id":3,"name":"zhang han yu"}
]
}

PUT /movie_index/movie/3
{
  "id":3,
  "name":"incident red sea",
  "doubanScore":5.0,
  "actorList":[  
{"id":4,"name":"zhang chen"}
]
}
```

**注意**：在elasticsearch6中，一个index下只能由一个type，本例中 movie_index 下已有 movie ，故当再往其中新增 movie_chn 时将报错。elasticsearch5中没有这个规定。

![image-20200218053916092](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200218053916092.png)

**更新PUT:**

将原有记录覆盖

**查找GET**：

```html
GET movie_index/_search
```

![image-20200218050559352](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200218050559352.png)

查找所有字段name中包含“red”的记录：

![image-20200218051221348](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200218051221348.png)

##### 4.ES对中文的处理与解析：

我们先新增中文document：

```html
PUT /movie_chn/movie/1
{ "id":1,
  "name":"红海行动",
  "doubanScore":8.5,
  "actorList":[  
  {"id":1,"name":"张译"},
  {"id":2,"name":"海清"},
  {"id":3,"name":"张涵予"}
 ]
}
PUT /movie_chn/movie/2
{
  "id":2,
  "name":"湄公河行动",
  "doubanScore":8.0,
  "actorList":[  
{"id":3,"name":"张涵予"}
]
}
PUT /movie_chn/movie/3
{
  "id":3,
  "name":"红海事件",
  "doubanScore":5.0,
  "actorList":[  
{"id":4,"name":"张晨"}
]
}

PUT /movie_index/movie/4
{ "id":4,
  "name":"红海行动",
  "doubanScore":8.5,
  "actorList":[  
  {"id":1,"name":"张译"},
  {"id":2,"name":"海清"},
  {"id":3,"name":"张涵予"}
 ]
}
PUT /movie_index/movie/5
{
  "id":5,
  "name":"湄公河行动",
  "doubanScore":8.0,
  "actorList":[  
{"id":3,"name":"张涵予"}
]
}

PUT /movie_index/movie/6
{
  "id":6,
  "name":"红海事件",
  "doubanScore":5.0,
  "actorList":[  
{"id":4,"name":"张晨"}
]
}
```

**对于中文的分词：**

若不做任何处理，所有中文将逐字拆分，我们要加入中文分词器（本项目采用IK分词器）

我们先进入插件目录：

![image-20200218105832822](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200218105832822.png)

在plugins目录下解压一层目录就可以

加入分词器后，就能进行中文搜索了：

![image-20200218114554353](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200218114554353.png)

IK（中英文分词器）有两个模式：   

1. ik_smart（简易分词）
2. ik_max_word（尽最大可能分词）

![image-20200218113732021](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200218113732021.png)

![image-20200218113809918](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200218113809918.png)

商品标题：应当用最大可能分词（标题本身不长）   
商品描述：应当用简易分词

##### 5.ES的集群

首先用VMWare克隆一台虚拟机与原有虚拟机构成集群

然后修改被克隆虚拟机IP：

1. 进入IP配置文件目录，修改配置文件

   ```sh
   cd /etc/sysconfig/network-scripts/
   ```

   ```sh
   vim ifcfg-ens33
   ```

   ![image-20200219093645230](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200219093645230.png)

2. 重启网络

   ```sh
   service network restart
   ```


然后切换到elasticsearch/config/目录下，配置elasticsearch.yml，启动集群:

对于集群中的每一台设备，按如下格式修改elasticsearch.yml:

```yml
cluster.name: aubin-cluster     #必须相同 
# 集群名称（不能重复）
node.name: els1 #（必须不同）
# 节点名称，仅仅是描述名称，用于在日志中区分（自定义）
#指定了该节点可能成为 master 节点，还可以是数据节点
node.master: true
node.data: true
path.data: /opt/data
# 数据的默认存放路径（自定义）
path.logs: /opt/logs 
# 日志的默认存放路径 
network.host: 192.168.0.1 
# 当前节点的IP地址 
http.port: 9200 
# 对外提供服务的端口
transport.tcp.port: 9300
#9300为集群服务的端口 
discovery.zen.ping.unicast.hosts: ["172.18.68.11", "172.18.68.12","172.18.68.13"] 
# 集群个节点IP地址，也可以使用域名，需要各节点能够解析 (除本机外的其他节点)
discovery.zen.minimum_master_nodes: 2 
# 为了避免脑裂，集群节点数最少为 半数+1
```

以root身份在/opt目录下创建data、logs两个目录，然后让其他用户有权访问：chmod 777 data/logs

各个节点切换到es用户，后台启动elasticsearch

##### 6.集群的工作原理

我们可以用cerebro管理集群。

解压cerebro压缩包，进入bin目录下，然后以管理员身份运行cerebro.bat文件

![image-20200220051619155](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200220051619155.png)

![image-20200220051815506](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200220051815506.png)

然后打开localhost:9000

![image-20200220053332733](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200220053332733.png)

|            | 概念、定义                                                   |
| ---------- | ------------------------------------------------------------ |
| 节点       | 一个节点就是一个es服务器，es集群中，主节点负责集群的管理和任务的分发，一般不负责文档的增删改查 |
| 片         | 分片是实际的物理存储单元（一个Lucene实例）                   |
| 索引       | 是es的逻辑单元（相当于数据库中的库），一个索引一般建立在不同机器的多个分片上 |
| 复制片     | 每个机器的分片一般在其他机器上都会有2-3个复制片（目的是提高数据的容错率） |
| 容错       | 一旦集群中的某些机器发生故障，剩余机器会在主节点的管理下，重新分配资源（分片） |
| 分片的路由 | 写操作（新建、删除）只在主分片上进行，然后将结果同步给复制分片<br />Sycn：主分片同步复制成功后，才返回结果给客户端 <br />Async：主分片操作成功后，在同步复制分片的同时将成功结果返回给客户端<br />读操作（查询）可在主分片或复制分片上进行 |

#### 实现：

##### 7.谷粒搜索模块

步骤：

1. 数据结构的准备

   商城中商品的数据结构：商品名称（展示/查询）、商品价格（展示/查询）、商品图片（展示）、平台属性和属性值列表（查询）、商品描述（展示/查询）、热度值（查询）、三级分类id（查询）、商品id、主键

   可用于查询的部分（参数结构）：（商品名称、商品价格、商品描述）（关键字）、平台属性和属性值列表、三级分类id

2. 项目的初始化

3. ES客户端的整合

4. 谷粒搜索代码的开发

   搜索功能

   面包屑功能

##### 8. 通过ES的mapping定义商品的数据结构

ElasticSearch的mapping定义是基于整个库的   
Mysql的数据结构字段定义是基于整个表的

ES的数据类型：

![image-20200220151420106](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200220151420106.png)

商品数据结构：商品名称（展示/查询）、商品价格（展示/查询）、商品图片（展示）、平台属性和属性值列表（查询）、商品描述（展示/查询）、热度值（查询）、三级分类id（查询）、商品id、主键

```json
PUT gmall
{
  "mappings": {
    "PmsSkuInfo":{
      "properties": {
        "id":{
          "type": "keyword",
          "index": true
        },
        "skuName": {
          "type": "text",
          "analyzer":"ik_max_word"
        },
        "skuDesc": {
          "type": "text",
          "analyzer": "ik_smart"
        },
        "catalog3Id": {
          "type": "keyword"
        },
        "price": {
          "type": "double"
        },
        "skuDefaultImg": {
          "type": "keyword",
          "index": false
        },
        "hotScore":{
          "type": "double"
        },
        "productId":{
          "type": "keyword"
        },
        "skuAttrValueList":{
          "properties": {
            "attrId":{
              "type": "keyword"
            },
            "valueId":{
              "type": "keyword"
            }
          }
        }
      }
    }
  }
}
```

**用一个Java程序将数据库中的SKU内容导入到ElasticSearch**

首先新建一个模块：gmall-search-service，除了service模块该有的依赖外，在pom中引入ES依赖：

```xml
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>

<!-- https://mvnrepository.com/artifact/io.searchbox/jest -->
<dependency>
   <groupId>io.searchbox</groupId>
   <artifactId>jest</artifactId>
   <version>5.3.3</version>
</dependency>


<!-- https://mvnrepository.com/artifact/net.java.dev.jna/jna -->
<dependency>
   <groupId>net.java.dev.jna</groupId>
   <artifactId>jna</artifactId>
   <version>4.5.1</version>
</dependency>
```

配置文件参照gmall-manage-service的配置文件，另外加上一行elasticsearch的urls配置：

```properties
# 服务端口
server.port=8074

# jdbc配置
spring.datasource.username=root
spring.datasource.password=asdasdasd
spring.datasource.url=jdbc:mysql://localhost:3306/gmall?characterEncoding=UTF-8&useSSL=false

# mybatis配置
mybatis.mapper-locations=classpath:mapper/*Mapper.xml
mybatis.configuration.map-underscore-to-camel-case=true

# dubbo的配置

# dubbo中的服务名称
spring.dubbo.application=search-service
# dubbo的通讯协议的名称
spring.dubbo.protocol.name=dubbo
# zookeeper注册中心的地址
spring.dubbo.registry.address=192.168.226.129:2181
# zookeeper的通讯协议的名称
spring.dubbo.registry.protocol=zookeeper
# dubbo的服务的扫描路径
spring.dubbo.base-package=com.atguigu.gmall

# 设置超时时间(毫秒)
spring.dubbo.consumer.timeout=8000

# ES
spring.elasticsearch.jest.uris=http://192.168.226.129:9200
```

导入原理如下：Jest能将Java对象转换成DSL语句，通过elasticsearch为Java提供的API，执行这些DSL语句将数据写入elasticsearch中

![image-20200222071128885](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200222071128885.png)

我们在gmall-search-service模块中以单元测试的形式实现这个功能：

GmallSearchServiceApplicationTests.java:

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {
    @Reference
    SkuService skuService;      //查询mysql
    @Autowired
    JestClient jestClient;
    @Test
    public void contextLoads() throws IOException {
        //查询mysql数据
        List<PmsSkuInfo> pmsSkuInfoList;
        pmsSkuInfoList=skuService.getAllSku("61");//通过catalog3Id得到所有三级目录下的商品（“手机”的catalog3Id为61），细节不做赘述，自行实现
        //转化为ES数据结构
        ArrayList<PmsSearchSkuInfo> pmsSearchSkuInfos= new ArrayList<>();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
            BeanUtils.copyProperties(pmsSkuInfo,pmsSearchSkuInfo);
            pmsSearchSkuInfos.add(pmsSearchSkuInfo);
        }
        //存入ES
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            Index build = new Index.Builder(pmsSearchSkuInfo).index("gmall").type("PmsSkuInfo").id(pmsSearchSkuInfo.getId()).build();
            jestClient.execute(build);
        }
    }
}
```

![image-20200222094129837](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200222094129837.png)

然后执行该程序，执行前记得启动gmall-manage-service服务

可以看到数据库中数据已成功存入elasticsearch中：

![image-20200222094316615](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200222094316615.png)

##### 9.如何定义复杂查询

查询符合以下条件的手机：名字中带小米、4寸以下、16G内存（搜索+过滤：推荐先过滤后搜索）

```json
GET gmall/PmsSkuInfo/_search
{
  "query": {
    "bool": {
      "filter": [
        {
          "term": {
            "skuAttrValueList.valueId": "39"
          }
        },
        {
          "term": {
            "skuAttrValueList.valueId": "43"
          }
        }
      ],
      "must": [{
          "match": {
            "skuName": "华为"
          }
      }]
    }
  }
}
```

![image-20200222104012655](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200222104012655.png)

过滤条件 term与term之间为交集，当想要让某个过滤条件能有多个候选值时（并集），可以按如下形式写：

![image-20200222105010177](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200222105010177.png)

Java中的查询API：

```java
Search search = new Search.Builder("DSL的Json语句").addIndex("索引名")
    						.addType("Type名").build();
SearchResult execute = jestClient.execute(search);
```

查询API中DSL语句的封装工具类：

![image-20200222130356246](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200222130356246.png)

Term条件：

![image-20200222151420712](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200222151420712.png)

##### 10.前端静态资源

三级分类文件资源的加载路径：

![image-20200222183155152](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200222183155152.png)

修改url：

![image-20200222183318447](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200222183318447.png)

![image-20200222183351362](C:\Users\Apollos\AppData\Roaming\Typora\typora-user-images\image-20200222183351362.png)

##### 11.业务实现

1. controller参数传递

   增加一个PmsSearchParam参数类，包含字段：keyword、catalog3Id、skuAttrValueList

   ```java
   @Controller
   public class SearchController {
       @Reference
       SearchService searchService;
   
       @RequestMapping("/")
       public String index(){
           return "index";
       }
   
       @RequestMapping("list.html")
       public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap){
           List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.list(pmsSearchParam);
           //调用搜索服务，返回搜索结果
           modelMap.put("skuLsInfoList",pmsSearchSkuInfos);
           return "list";
       }
   }
   ```

2. service业务逻辑编写

   在测试用例中已完成

3. 排序和高亮

   ```json
   GET movie_index/movie/_search
   {
     "query":{
       "match": {"name":"red sea"}
     },
     "sort": [
       {
         "doubanScore": {
           "order": "desc"
         }
       }
     ],
     "highlight": {
     		"fields": {"name":{} }
     }
   }
   ```

   ![image-20200223120503966](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200223120503966.png)

4. 最终Service层代码：

   ```java
   @Service
   @CrossOrigin
   public class SearchServiceImpl implements SearchService {
       
       @Autowired
       JestClient jestClient;
       
       @Override
       public List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam) {
           List<PmsSearchSkuInfo> results=new ArrayList<>();
   
           String DSL=getSearchDSL(pmsSearchParam);
   //        System.err.println(DSL);
           Search search = new Search.Builder(DSL).addIndex("gmall").addType("PmsSkuInfo").build();
   
           SearchResult execute = null;
           try {
               execute = jestClient.execute(search);
           } catch (IOException e) {
               e.printStackTrace();
           }
           List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
           for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
               PmsSearchSkuInfo source = hit.source;
               //替换为搜索高亮字段
               Map<String, List<String>> highlight = hit.highlight;
               if (highlight!=null){
                   String skuName=highlight.get("skuName").get(0);
                   source.setSkuName(skuName);
               }
               //添加到查询结果列表
               results.add(source);
           }
           return results;
       }
   
       /**
        * 由传入搜索参数得到DSL
        * @param pmsSearchParam 传入搜索参数
        * @return DSL
        */
       private String getSearchDSL(PmsSearchParam pmsSearchParam){
           List<PmsSkuAttrValue> skuAttrValueList = pmsSearchParam.getSkuAttrValueList();
           String keyword = pmsSearchParam.getKeyword();
           String catalog3Id = pmsSearchParam.getCatalog3Id();
           //Jest的DSL工具
           SearchSourceBuilder sourceBuilder=new SearchSourceBuilder();
           //bool
           BoolQueryBuilder boolQueryBuilder=new BoolQueryBuilder();
           //filter
           if (StringUtils.isNotBlank(catalog3Id)){
               TermQueryBuilder termQueryBuilder=new TermQueryBuilder("catalog3Id",catalog3Id);
               boolQueryBuilder.filter(termQueryBuilder);
           }
           if (skuAttrValueList!=null){
               for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                   TermQueryBuilder termQueryBuilder=new TermQueryBuilder("skuAttrValueList.valueId",pmsSkuAttrValue.getValueId());
                   boolQueryBuilder.filter(termQueryBuilder);
               }
           }
           //must
           if (StringUtils.isNotBlank(keyword)){
               MatchQueryBuilder matchQueryBuilder=new MatchQueryBuilder("skuName",keyword);
               boolQueryBuilder.must(matchQueryBuilder);
           }
           //query
           sourceBuilder.query(boolQueryBuilder);
           //from
           sourceBuilder.from(0);
           //size
           sourceBuilder.size(100);
           //highlight
           HighlightBuilder highlightBuilder=new HighlightBuilder();
           highlightBuilder.preTags("<span style='color:red;'>");
           highlightBuilder.field("skuName");
           highlightBuilder.postTags("</span>");
           sourceBuilder.highlight(highlightBuilder);
           //sort
           sourceBuilder.sort("productId", SortOrder.DESC);
   
           return sourceBuilder.toString();
       }
   }
   ```

##### 12.搜索页面品台属性列表

平台属性列表是从搜索结果中抽取出来的，不是根据三级分类id查询的所有平台属性的集合

抽取方案：

1. ES中使用aggregation聚合函数（效率低，不推荐）：

   ![image-20200223152309880](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200223152309880.png)

   ![image-20200223152802225](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200223152802225.png)

2. Java代码抽取平台属性：

   ![image-20200223153111880](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200223153111880.png)

   A）根据skuId从mysql中查询平台属性值的ID集合（不推荐）

   ```mysql
   select distinct value_id from pms_sku_attr_value where sku_id in (pmsSearchSkuInfos..)
   ```

   B）直接用Java集合进行处理

   用Set集合去重抽取属性值id

   ```java
   //抽取检索结果所包含的平台属性集合
   Set<String> valueIdSet=new HashSet<>();
   for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
       List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
       for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
           String valueId=pmsSkuAttrValue.getValueId();
           valueIdSet.add(valueId);
       }
   }
   //根据valueId将属性列表查询出来
   List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.
       									getAttrValueListByValueId(valueIdSet);
   modelMap.put("attrList",pmsBaseAttrInfos);
   ```

   调用属性服务attrService根据属性值id将平台属性的集合列表查询出来：

   ```sql
   select * from
   pms_base_attr_info ba , pms_base_attr_value bv
   where ba.id=bv.attr_id and bv.id in (valueIdSet..)
   ```

   AttrServiceImpl:

   ```java
   public List<PmsBaseAttrInfo> getAttrValueListByValueId(Set<String> valueIdSet) {
       String valueIdStr=StringUtils.join(valueIdSet,",");     //eg: "41,45,46"
       List<PmsBaseAttrInfo> pmsBaseAttrInfos=pmsBaseAttrInfoMapper.selectAttrValueListByValueId(valueIdStr);
       return pmsBaseAttrInfos;
   }
   ```

   PmsBaseAttrInfoMapper.java:

   ```java
   public interface PmsBaseAttrInfoMapper extends Mapper<PmsBaseAttrInfo> {
       List<PmsBaseAttrInfo> selectAttrValueListByValueId(@Param("valueIdStr") String valueIdStr);
   }
   ```

   PmsBaseAttrInfoMapper.xml:

   ```xml
   <?xml version="1.0" encoding="UTF-8" ?>
   <!DOCTYPE mapper
           PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
           "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
   <mapper namespace="com.atguigu.gmall.manage.mapper.PmsBaseAttrInfoMapper">
       <select id="selectAttrValueListByValueId" resultMap="selectAttrValueListByValueIdMap">
           SELECT
               ba.*,ba.id as ba_id,bv.*,bv.id as bv_id
           FROM
               pms_base_attr_info ba,
               pms_base_attr_value bv
           WHERE
               ba.id = bv.attr_id
           AND bv.id IN (${valueIdStr})
       </select>
       
       <resultMap id="selectAttrValueListByValueIdMap" type="com.atguigu.gmall.bean.PmsBaseAttrInfo" autoMapping="true">
           <result column="ba_id" property="id"/>
           <collection property="attrValueList" ofType="com.atguigu.gmall.bean.PmsBaseAttrValue" autoMapping="true">
               <result column="bv_id" property="id"/>
           </collection>
       </resultMap>
   </mapper>
   ```

   完成：

   ![image-20200223234320132](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200223234320132.png)

##### 13.属性列表和面包屑的url

![image-20200224094713717](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200224094713717.png)

属性：当前url中所包含的属性值=面包屑中所包含的属性值   
属性列表：是排除了当前面包屑请求后的剩余属性（剩余属性=商品SKU中抽取总属性 - 当前面包屑中包含属性）

**点击属性列表中的某项属性：当前url将拼接上被点击属性构造新的请求url**   

![image-20200224120322854](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200224120322854.png)

当前请求的url的参数就是pmsSearchParam所提交的参数

**点击面包屑：当前url将减去被点击面包屑属性构造新的url**

面包屑：用户所点击过的平台属性（属性列表中已被排除的属性），可从当前请求url中获取

![image-20200224181605608](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200224181605608.png)



**面包屑及属性列表制作代码：**

```java
//剔除已选中属性以构造属性列表，同时将已选中属性制成面包屑
String[] delValueIds=pmsSearchParam.getValueId();       //已被选中的属性值
if (delValueIds!=null){
    //面包屑集
    List<PmsSearchCrumb> pmsSearchCrumbs=new ArrayList<>();
    for (String delValueId : delValueIds) {
        Iterator<PmsBaseAttrInfo> iterator=pmsBaseAttrInfos.iterator();
        PmsSearchCrumb pmsSearchCrumb=new PmsSearchCrumb();//面包屑参数
        pmsSearchCrumb.setValueId(delValueId);             //面包屑的属性值（id）
        pmsSearchCrumb.setUrlParam(getUrlParam(pmsSearchParam,delValueId));//面包屑的跳转链接参数
        while (iterator.hasNext()){
            PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            //从属性全集中剔除已选中的属性
            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                if (pmsBaseAttrValue.getId().equals(delValueId)){
                    pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());//面包屑的属性值名称
                    iterator.remove();
                }
            }
        }
        pmsSearchCrumbs.add(pmsSearchCrumb);
    }
    //将面包屑集返回到前台
    modelMap.put("attrValueSelectedList",pmsSearchCrumbs);
}
String urlParam=getUrlParam(pmsSearchParam);
//将已剔除选中属性的属性列表返回到前台
modelMap.put("urlParam",urlParam);
```

由请求参数构造请求链接：

```java
//由请求参数构造请求链接url
private String getUrlParam(PmsSearchParam pmsSearchParam,String ...delValueId) {
    String keyword=pmsSearchParam.getKeyword();
    String catalog3Id = pmsSearchParam.getCatalog3Id();
    String[] skuAttrValueList = pmsSearchParam.getValueId();
    StringBuilder urlParam= new StringBuilder();

    //keyword和catalog3Id中必有一个不为空
    if (StringUtils.isNotBlank(keyword)){
        if (StringUtils.isNotBlank(urlParam.toString())){
            urlParam.append("&");
        }
        urlParam.append("keyword=").append(keyword);
    }
    if (StringUtils.isNotBlank(catalog3Id)){
        if (StringUtils.isNotBlank(urlParam.toString())){
            urlParam.append("&");
        }
        urlParam.append("catalog3Id").append(catalog3Id);
    }
    if (skuAttrValueList!=null){
        for (String pmsSkuAttrValue : skuAttrValueList) {
            if (delValueId.length>0){
                //面包屑的请求url的参数，该url需剔除被点击面包屑所包含的属性值
                if (!pmsSkuAttrValue.equals(delValueId[0])){
                    urlParam.append("&valueId=").append(pmsSkuAttrValue);
                }
            }else {
                //普通请求url的参数
                urlParam.append("&valueId=").append(pmsSkuAttrValue);
            }
        }
    }
    return urlParam.toString();
}
```

### P153购物车

流程：在用户未登录时也可以使用购物车功能，某些系统（如淘宝）也会要求用户必须登录才能使用购物车。

![image-20200225221954372](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200225221954372.png)

我们配一下本地的host文件，同时对以往html的url做一些更新。

![image-20200226143426431](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200226143426431.png)

#### 谷粒购物车

购物车业务比较简单，我们新建gmall-cart-web和gmall-cart-service两个模块，设置对应配置，引入对应的依赖。然后在gmall-cart-web模块中粘贴好购物车的前端静态资源文件

修改一下商品详情页中的购物车跳转地址：

![image-20200225184816359](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200225184816359.png)

跳转方式：重定向到购物车页面

![image-20200225184332132](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200225184332132.png)

![image-20200225202541206](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200225202541206.png)

##### 1.介绍

1. 购物车在不登录时也能使用

   需要引入对浏览器cookie的操作

2. 购物车在登录时要用mysql和redis来存储数据，

   redis作为购物车的缓存

3. 在缓存情况下，或者用户已经添加购物车后，允许购物车中的数据和原始商品数据不一致

4. 购物车同步问题

   什是时候同步：结算&登录时同步

   同步后是否需要删除cookie数据

5. 用户在不同的客户端同时登录

   如何处理购物车数据 

##### 2.购物车添加商品功能

1. 传递参数：商品skuId，添加数量quantity

2. 根据skuid调用skuService查询商品的详细信息

3. 将商品详细信息封装成购物车信息

4. 判断用户是否登录

5. 根据用户登录状态决定走cookie分支还是db分支，对购物车数据进行写入操作：

   Cookies存取：response.addCookie(cookie)，request.getCookies()   
   Cookies的跨域问题：setDomain()，getDomain()

   Db+cache

   我们在web-util模块下写一个工具类来处理与Cookie相关的操作

   ```java
   import javax.servlet.http.Cookie;
   import javax.servlet.http.HttpServletRequest;
   import javax.servlet.http.HttpServletResponse;
   import java.io.UnsupportedEncodingException;
   import java.net.URLDecoder;
   import java.net.URLEncoder;
   /**
    * @param
    * @return
    */
   public class CookieUtil {
       /***
        * 获得cookie中的值，默认为主ip：www.gmall.com
        */
       public static String getCookieValue(HttpServletRequest request, String cookieName, boolean isDecoder) {
           Cookie[] cookies = request.getCookies();
           if (cookies == null || cookieName == null){
               return null;
           }
           String retValue = null;
           try {
               for (int i = 0; i < cookies.length; i++) {
                   if (cookies[i].getName().equals(cookieName)) {
                       if (isDecoder) {//如果涉及中文
                           retValue = URLDecoder.decode(cookies[i].getValue(), "UTF-8");
                       } else {
                           retValue = cookies[i].getValue();
                       }
                       break;
                   }
               }
           } catch (UnsupportedEncodingException e) {
               e.printStackTrace();
           }
           return retValue;
       }
       /***
        * 设置cookie的值
        */
       public static   void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue, int cookieMaxage, boolean isEncode) {
           try {
               if (cookieValue == null) {
                   cookieValue = "";
               } else if (isEncode) {
                   cookieValue = URLEncoder.encode(cookieValue, "utf-8");
               }
               Cookie cookie = new Cookie(cookieName, cookieValue);
               if (cookieMaxage >= 0)
                   cookie.setMaxAge(cookieMaxage);
               if (null != request)// 设置域名的cookie
                   cookie.setDomain(getDomainName(request));
               // 在域名的根路径下保存
               cookie.setPath("/");
               response.addCookie(cookie);
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
       /***
        * 获得cookie的主域名，本系统为gmall.com，保存时使用
        */
       private static final String getDomainName(HttpServletRequest request) {
           String domainName = null;
           String serverName = request.getRequestURL().toString();
           if (serverName == null || serverName.equals("")) {
               domainName = "";
           } else {
               serverName = serverName.toLowerCase();
               serverName = serverName.substring(7);
               final int end = serverName.indexOf("/");
               serverName = serverName.substring(0, end);
               final String[] domains = serverName.split("\\.");
               int len = domains.length;
               if (len > 3) {
                   // www.xxx.com.cn
                   domainName = domains[len - 3] + "." + domains[len - 2] + "." + domains[len - 1];
               } else if (len <= 3 && len > 1) {
                   // xxx.com or xxx.cn
                   domainName = domains[len - 2] + "." + domains[len - 1];
               } else {
                   domainName = serverName;
               }
           }
           if (domainName != null && domainName.indexOf(":") > 0) {
               String[] ary = domainName.split("\\:");
               domainName = ary[0];
           }
           System.out.println("domainName = " + domainName);
           return domainName;
       }
       /***
        * 将cookie中的内容按照key删除
        */
       public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String cookieName) {
           setCookie(request, response, cookieName, null, 0, false);
       }
   }
   ```

6. 购物车类型：

   DB：cartListDb （有主键和用户id）   
   Cookie：cartListCookie（没有主键和用户id）   
   Redis：cartListCache（有主见和用户id）

##### 3.购物车的缓存结构

1. 存储的是购物车集合

2. 键：用户id

3. 购物车缓存中某一个购物车数据的更新

   ~~用set kv 取出json，转换成集合，从集合中取出对象，修改对象，放回集合，在将集合放回缓存~~（麻烦）

   用hashMap结构来存储购物车：

   ![image-20200226164506909](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200226164506909.png)

   ![image-20200226164719260](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200226164719260.png)

   ![image-20200226171318143](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200226171318143.png)

   实现：

   ```java
   @Override
   public void flushCartCache(String memberId) {
       OmsCartItem omsCartItem=new OmsCartItem();
       omsCartItem.setMemberId(memberId);
       List<OmsCartItem> cartItems = omsCartItemMapper.select(omsCartItem);
       //同步到redis缓存
       Jedis jedis = redisUtil.getJedis();
       Map<String,String> map=new HashMap<>();
       for (OmsCartItem cartItem : cartItems) {
           map.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
       }
       jedis.hmset("user:"+memberId+":cart",map);
       jedis.close();
   }
   ```

##### 4.购物车列表

1. 购物车列表数据从缓存中取

2. 购物车页面展示![image-20200226201751723](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200226201751723.png)

3. 购物车异步修改选中状态

   A）异步请求：返回json，也可返回内嵌页面。我们采用返回内嵌页面的方式刷新购物车列表   
   B）修改数据库中购物车状态   
   C）数据库修改后同步缓存

   ```javascript
   //cartList.html 异步请求
   function checkSku( chkbox){
       var skuId= $(chkbox).attr("value");
       var checked=$(chkbox).prop("checked");
       var isCheckedFlag="0";
       if(checked){
           isCheckedFlag="1";
       }
       var param="isChecked="+isCheckedFlag+"&"+"skuId="+skuId;
       $.post("checkCart",param,function (data) {
           sumSumPrice();
           //服务器会返回一个内嵌页面给ajax，我们用新的页面刷新替换掉原来的老页面
           $("#cartListInner").html(data);
       });
   }
   ```

4. 价格计算：

   ![image-20200227153017658](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200227153017658.png)

   涉及到价格计算时应当用**BigDecimal**做运算、比较，并用字符串初始化

##### 5.代码实现：

CartController：

```java
@Controller
public class CartController {

    @Reference
    SkuService skuService;

    @Reference
    CartService cartService;

    @RequestMapping("checkCart")
    public String checkCart(String isChecked,String skuId,ModelMap modelMap){
        String memberId="1";
        //调用服务，修改状态
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setIsChecked(isChecked);
        omsCartItem.setProductSkuId(skuId);
        cartService.checkCart(omsCartItem);
        //将最新数据从缓存中取出，渲染给内嵌页面
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        //设置购物车单项总价，并计算选中商品总价
        BigDecimal totalAmount=calculatePrice(omsCartItems);
        //购物车商品列表
        modelMap.put("cartList",omsCartItems);
        //购物车总价
        modelMap.put("totalAmount",totalAmount);
        return "cartListInner";
    }

    @RequestMapping("cartList")
    public String cartList(HttpServletRequest request, ModelMap modelMap){
        List<OmsCartItem> omsCartItems = null;
        String userId="1";
        if (StringUtils.isNotBlank(userId)){
            //已经登录,查询db
            omsCartItems=cartService.cartList(userId);
        }else {
            //没有登录查询Cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)){
                omsCartItems=JSON.parseArray(cartListCookie,OmsCartItem.class);
            }
        }
        //设置购物车单项总价，并计算选中商品总价
        assert omsCartItems != null;
        BigDecimal totalAmount=calculatePrice(omsCartItems);
        //购物车商品列表
        modelMap.put("cartList",omsCartItems);
        //购物车总价
        modelMap.put("totalAmount",totalAmount);
        return "cartList";
    }

    @RequestMapping("addToCart")
    public String addToCart(String skuId, int quantity, HttpServletRequest request, HttpServletResponse response){
        //调用商品服务查询商品信息
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId);

        List<OmsCartItem> omsCartItems;

        //将商品封装成购物车信息
        OmsCartItem omsCartItem=new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setModifyDate(omsCartItem.getCreateDate());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setProductId(skuInfo.getProductId());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("11111");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(quantity);
        omsCartItem.setIsChecked("1");

        //判断用户是否登录
        String memberId="1";

        if (StringUtils.isBlank(memberId)){
            //用户未登录
            //取出cookie里原有的购物车数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);

            if (StringUtils.isBlank(cartListCookie)){
                //购物车cookie为空
                omsCartItems=new ArrayList<>();
                omsCartItems.add(omsCartItem);
            }else {
                //购物车cookie不为空
                omsCartItems = JSON.parseArray(cartListCookie,OmsCartItem.class);
                //判断添加到购物车的商品数据在cookie中是否存在
                OmsCartItem exist=if_cart_exist(omsCartItems,omsCartItem);
                if (exist!=null){
                    //之前添加过，更新购物车中该商品的数量
                    exist.setQuantity(exist.getQuantity()+omsCartItem.getQuantity());
                }else {
                    //之前没有添加过，新增该商品到当前购物车
                    omsCartItems.add(omsCartItem);
                }
            }
            //更新cookie
            CookieUtil.setCookie(request,response,"cartListCookie",
                    JSON.toJSONString(omsCartItems),3600*72,true);
        }else {
            //用户已登录
            OmsCartItem cartItemFromDb=cartService.ifItemExistsInUserCart(memberId,skuId);
            if (cartItemFromDb==null){
                //该用户未添加过此商品
                omsCartItem.setMemberId(memberId);
                omsCartItem.setQuantity(quantity);
                omsCartItem.setMemberNickname("测试啊");
                cartService.addCartItem(omsCartItem);
            }else {
                //该用户已添加此商品
                cartItemFromDb.setQuantity(cartItemFromDb.getQuantity()+omsCartItem.getQuantity());
                cartService.updateCart(cartItemFromDb);
            }
            //同步缓存
            cartService.flushCartCache(memberId);
        }

        return "redirect:/success.html";
    }

    /**
     * 判断购物车中是否已存在相同商品，若存在则返回该购物车中商品项，否则返回null
     * @param omsCartItems 购物车中已有商品
     * @param omsCartItem 待判断是否存在于购物车的商品
     * @return 购物车中已存在的相同商品
     */
    private OmsCartItem if_cart_exist(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        for (OmsCartItem cartItem : omsCartItems) {
            if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                return cartItem;
            }
        }
        return null;
    }

    /**
     * 设置购物车中单项商品的总价，并返回购物车内选中商品的总价
     * @param omsCartItems 购物车商品列表
     * @return 购物车商品总价
     */
    private BigDecimal calculatePrice(List<OmsCartItem> omsCartItems){
        BigDecimal totalAmount=new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
            //计算单品总价
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(BigDecimal.valueOf(omsCartItem.getQuantity())));
            //若被选中，累加入购物车选中商品总价
            if (omsCartItem.getIsChecked().equals("1")){
                totalAmount=totalAmount.add(omsCartItem.getTotalPrice());
            }
        }
        return totalAmount;
    }
}
```

CartServiceImpl:

```java
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OmsCartItemMapper omsCartItemMapper;

    @Override
    public OmsCartItem ifItemExistsInUserCart(String memberId, String skuId) {
        OmsCartItem omsCartItem=new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        OmsCartItem selectOne = omsCartItemMapper.selectOne(omsCartItem);
        return selectOne;
    }

    @Override
    public void addCartItem(OmsCartItem omsCartItem) {
        if (StringUtils.isNotBlank(omsCartItem.getMemberId())){
            omsCartItemMapper.insert(omsCartItem);
        }
    }

    @Override
    public void updateCart(OmsCartItem cartItemFromDb) {
        Example example=new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("id",cartItemFromDb.getId());
        omsCartItemMapper.updateByExampleSelective(cartItemFromDb,example);
    }

    @Override
    public void flushCartCache(String memberId) {
        OmsCartItem omsCartItem=new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> cartItems = omsCartItemMapper.select(omsCartItem);
        //同步到redis缓存
        Jedis jedis = redisUtil.getJedis();
        Map<String,String> map=new HashMap<>();
        for (OmsCartItem cartItem : cartItems) {
            map.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
        }
        jedis.del("user:"+memberId+":cart");
        jedis.hmset("user:"+memberId+":cart",map);
        jedis.close();
    }

    @Override
    public List<OmsCartItem> cartList(String userId) {
        Jedis jedis = redisUtil.getJedis();
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        //就自己访问，我觉得应该不用加锁
        List<String> hvals = jedis.hvals("user:" + userId + ":cart");
        for (String hval : hvals) {
            OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
            omsCartItems.add(omsCartItem);
        }
        jedis.close();
        return omsCartItems;
    }

    @Override
    public void checkCart(OmsCartItem omsCartItem) {
        Example example=new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId",omsCartItem.getMemberId())
                .andEqualTo("productSkuId",omsCartItem.getProductSkuId());
        omsCartItemMapper.updateByExampleSelective(omsCartItem,example);
        //同步缓存
        flushCartCache(omsCartItem.getMemberId());
    }
}
```



#### 题外话——Dubbo服务端口绑定报错问题：

```sh
2020-02-27 00:56:21.194 ERROR 24316 --- [           main] com.alibaba.dubbo.qos.server.Server      :  [DUBBO] qos-server can not bind localhost:22222, dubbo version: 2.6.0, current host: 127.0.0.1

java.net.BindException: Address already in use: bind
	at sun.nio.ch.Net.bind0(Native Method) ~[na:1.8.0_161]
	at sun.nio.ch.Net.bind(Net.java:433) ~[na:1.8.0_161]
	at sun.nio.ch.Net.bind(Net.java:425) ~[na:1.8.0_161]
	at sun.nio.ch.ServerSocketChannelImpl.bind(ServerSocketChannelImpl.java:223) ~[na:1.8.0_161]
	at io.netty.channel.socket.nio.NioServerSocketChannel.doBind(NioServerSocketChannel.java:130) ~[netty-transport-4.1.34.Final.jar:4.1.34.Final]
	at io.netty.channel.AbstractChannel$AbstractUnsafe.bind(AbstractChannel.java:563) ~[netty-transport-4.1.34.Final.jar:4.1.34.Final]
	at io.netty.channel.DefaultChannelPipeline$HeadContext.bind(DefaultChannelPipeline.java:1332) ~[netty-transport-4.1.34.Final.jar:4.1.34.Final]
	at io.netty.channel.AbstractChannelHandlerContext.invokeBind(AbstractChannelHandlerContext.java:488) ~[netty-transport-4.1.34.Final.jar:4.1.34.Final]
	at io.netty.channel.AbstractChannelHandlerContext.bind(AbstractChannelHandlerContext.java:473) ~[netty-transport-4.1.34.Final.jar:4.1.34.Final]
	at io.netty.channel.DefaultChannelPipeline.bind(DefaultChannelPipeline.java:984) ~[netty-transport-4.1.34.Final.jar:4.1.34.Final]
	at io.netty.channel.AbstractChannel.bind(AbstractChannel.java:259) ~[netty-transport-4.1.34.Final.jar:4.1.34.Final]
	at io.netty.bootstrap.AbstractBootstrap$2.run(AbstractBootstrap.java:366) ~[netty-transport-4.1.34.Final.jar:4.1.34.Final]
	at io.netty.util.concurrent.AbstractEventExecutor.safeExecute(AbstractEventExecutor.java:163) ~[netty-common-4.1.34.Final.jar:4.1.34.Final]
	at io.netty.util.concurrent.SingleThreadEventExecutor.runAllTasks(SingleThreadEventExecutor.java:404) ~[netty-common-4.1.34.Final.jar:4.1.34.Final]
	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:495) ~[netty-transport-4.1.34.Final.jar:4.1.34.Final]
	at io.netty.util.concurrent.SingleThreadEventExecutor$5.run(SingleThreadEventExecutor.java:905) ~[netty-common-4.1.34.Final.jar:4.1.34.Final]
	at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[netty-common-4.1.34.Final.jar:4.1.34.Final]
	at java.lang.Thread.run(Thread.java:748) ~[na:1.8.0_161]
```

试过，以下设置没用：

```properties
# 手动分配dubbo协议端口号
spring.dubbo.protocol.port=44445
```

### P167 用户认证Passport

##### 1.介绍

1. 在购物车之前的功能，不要求用户登录判断
2. 在购物车之后的功能，必须要求用户登录的判断通过
3. 购物车功能中，必须对用户登录进行判定，判定失败也可继续使用（采用Cookie购物车）

##### 2.用户认证模块的设计：

![image-20200227175419584](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200227175419584.png)

##### 3. 认证中心

	1. 给用户颁发通信证（ttoken）
 	2. 验证其他业务功能接收token（用户访问所携带的）的真伪

**整合：**

 1. 引入静态资源

 2. 在search模块的页面点击登录连接上加上认证中心的url

    ![image-20200228002033022](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200228002033022.png)

3. 增加一个测试用的结算功能页面（真正的结算功能开发在订单模块）

   ![image-20200228002242876](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200228002242876.png)

   ![image-20200228002350740](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200228002350740.png)

4. 加入拦截器（在web-util模块中加入拦截器，让所有请求到web服务的请求都被拦截器拦截）

   ![image-20200228144210465](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200228144210465.png)

   ```java
   @Configuration
   public class WebMvcConfiguration extends WebMvcConfigurerAdapter {
       @Autowired
       AuthInterceptor authInterceptor;
       @Override
       public void addInterceptors(InterceptorRegistry registry){
           registry.addInterceptor(authInterceptor).addPathPatterns("/**");
           super.addInterceptors(registry);
       }
   }
   ```

   ```java
   @Component
   public class AuthInterceptor extends HandlerInterceptorAdapter {
       @Override
       public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
           //拦截代码
           return true;
       }
   }
   ```

5. 决定模块是否被拦截器拦截

   除了可以通过web模块是否扫描拦截器来决定拦截器的使用之外

   还可以通过注解的方式来标识具体的方法是否需要通过拦截器：@LoginRequired

   ![image-20200228145921264](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200228145921264.png)

   ![image-20200228145959115](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200228145959115.png)

   该注解只对方法有效，生效范围是运行时生效

   利用反射机制（通过一个对象获得类的整体信息），用所请求方法的方法名去得到方法信息

   ```java
   @Target(ElementType.METHOD)
   @Retention(RetentionPolicy.RUNTIME)
   public @interface LoginRequired {
       boolean loginSuccessNeeded() default true;
   }
   ```

   我们可以将被请求方法分为三类：

   - 不需要拦截器（没有拦截器注解），直接放行，**不用加上@LoginRequired**
   - 需要拦截器但拦截校验失败（未登录或登录已过期）也可继续访问的方法，这些方法通常要视登录与否决定方法具体的执行逻辑，如购物车中的所有方法，**@LoginRequired(loginSuccessNeeded=false)**
   - 需要拦截，并且拦截校验一定要通过（登录成功）才能访问的方法，**@LoginRequired**

##### 4.单点登录（Single Sign On，SSO）

​	将用户登录认证中心抽离出来，只要在一个模块登录了，在进入其他业务模块（不管是属于顶级域名下的还是跨越了顶级域名）也已经登录了，不需要二次登录。

单点登录服务介绍：

> https://www.bilibili.com/video/av55643074?p=174

1. 早期：单一服务器，用户认证；缺点：单点性能压力，无法扩展

2. Web应用集群，session共享模式

   通过JsessionId判断用户是否已登录

   ![image-20200228162347274](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200228162347274.png)

   解决了单点性能瓶颈   
   问题：   

   - 多业务分布式数据独立管理，不适合统一维护一份session数据
   - 分布式按业务功能切分，用户、认证解耦出来单独统一管理
   - cookie中使用jsessionId容易被篡改、盗取
   - 跨顶级域名无法访问

3. 分布式，SSO模式

   Jwt+userInfo 在认证中心解密校验来判断是否登录，采用Jwt加密算法保障安全，避免了访问Redis或DB

##### 5.用JWT实现用户登录的校验（token）

JWT工具：

JWT（Json Web Token） 是为了在网络应用环境间传递声明而执行的一种基于JSON的开放标准。   
JWT的声明一般被用来在身份提供者和服务提供者间传递被认证的用户身份信息，以便于从资源服务器获取资源。比如用在用户登录上   
JWT 最重要的作用就是对 token信息的防伪作用。

JWT的原理：

一个JWT由三个部分组成：公共部分、私有部分、签名部分。最后由这三者组合进行base64编码得到JWT。

![image-20200228172157695](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200228172157695.png)

公共部分：主要是该JWT的相关配置参数，比如签名的加密算法、格式类型、过期时间等等。   
私有部分：用户自定义的内容，根据实际需要真正要封装的信息。   
签名部分：根据用户信息+盐值+密钥生成的签名。如果想知道JWT是否是真实的只要把JWT的信息取出来，加上盐值和服务器中的密钥就可以验证真伪。所以不管由谁保存JWT，只要没有密钥就无法伪造。   
base64编码，并不是加密，只是把明文信息变成了不可见的字符串。但是其实只要用一些工具就可以吧base64编码解成明文，所以不要在JWT中放入涉及私密的信息，因为实际上JWT并不是加密信息。

![image-20200228173119491](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200228173119491.png)

在web-util引入pom依赖：

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.0</version>
</dependency>
```

制作JWT的工具类：

```java
public class JwtUtil {
    public static String encode(String key,Map<String,Object> param,String salt){
        if(salt!=null){
            key+=salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256,key);

        jwtBuilder = jwtBuilder.setClaims(param);

        String token = jwtBuilder.compact();
        return token;

    }
    public  static Map<String,Object>  decode(String token ,String key,String salt){
        Claims claims=null;
        if (salt!=null){
            key+=salt;
        }
        try {
            claims= Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        } catch ( JwtException e) {
           return null;
        }
        return  claims;
    }
}
```

使用示例：

![image-20200228173541230](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200228173541230.png)

##### 6.passport登录功能

1. 点击登录按钮，在登录页面输入用户名密码，通过用户名密码验证用户是否登录成功，用JWT生成token返回给search.gmall.com/index，在拦截器中将返回的token写入cookie

2. 点击结算按钮，拦截器拦截请求

   该用户没有登录，并且结算请求时必须登录，将用户打回认证中心进行登录

   该用户没有登录，并且请求时没登录也可访问，放行

3. 被拦截登录后，返回原始请求（携带登录成功后颁发的token）

4. 原始请求的拦截器第二次拦截请求，拦截请求后，将返回的token写入cookie

##### 7.从首页点击登录的流程

 1. 首页访问登录页，携带ReturnUrl回跳地址

    ![image-20200228204246925](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200228204246925.png)

	2. 登录页保存回跳地址

    ![image-20200228204432422](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200228204432422.png)

	3. 登录页通过异步方式验证用户名密码

    ```html
    <form id="loginForm" action="./login" method="post">
    ```

    ```javascript
    //异步提交
    function submitLogin() {
        const username = $("#username").val();
        const password = $("#password").val();
        $.post("login",{username:username,password:password},function (token) {
            alert(token);
            //验证token是否为空或者异常
    ...
            window.location.href=$("#ReturnUrl").val()+"?token="+token;
        })
    }
    ```

	4. 验证通过后颁发token给异步ajax

    ```java
    public class PassportController {
        @RequestMapping("index")
        public String index(String ReturnUrl, ModelMap modelMap){
            modelMap.put("ReturnUrl",ReturnUrl);
            return "index";
        }
    
        @RequestMapping("login")
        @ResponseBody
        public String login(UmsMember umsMember){
            //调用用户服务验证用户名和密码
    
            return "token";
        }
    }
    ```

	5. 异步ajax得到token，根据回跳地址ReturnUrl请求原始功能

    ```javascript
    window.location.href=$("#ReturnUrl").val()+"?token="+token;
    ```

    ![image-20200228211310148](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200228211310148.png)

##### 8.拦截器开发

拦截请求的流程：

 1. 被拦截器拦截

 2. 拦截器判断注解

    - 是否需要登录验证（是否有@LoginRequired注解）
    - 是否必须能录才能请求（loginSuccessNeeded==true?）

    拦截器拦截token的四种情况：

    ![image-20200228220821687](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200228220821687.png)

    必须登录但没登录时，会被重定向到认证中心进行登录

    在认证中心登录后，会重新请求原始应用

3. 验证

   通过Httpclient（apache的一个通用工具类）

4. 结合注解的情况验证结果

![image-20200229012606430](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200229012606430.png)

注意：

![image-20200229150212194](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200229150212194.png)

![image-20200229150523914](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200229150523914.png)

##### 9.用户UserService服务的对接

1. 认证中心调用用户服务进行login和信息的查询

2. 用户服务可调用缓存

   User:username:password + user:memberId:info (这种做法麻烦)

   user:password（加密并拼接username等等）:info  

#### 代码实现：

AuthInterceptor.java

```java
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //拦截代码:

        //判断被拦截请求所访问的方法的注解（是否是需要拦截的）
        HandlerMethod hm=(HandlerMethod)handler;
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);
        if (methodAnnotation==null){
            return true;
        }

        String token="";
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        if (StringUtils.isNotBlank(oldToken)){
            token=oldToken;
        }
        String newToekn = request.getParameter("token");
        if (StringUtils.isNotBlank(newToekn)){
            token=newToekn;
        }

        //进入拦截器的拦截方法
        boolean loginSuccessNeeded = methodAnnotation.loginSuccessNeeded(); //该请求是否必须成功登录
        String verify = "";
        Map verificationMap=null;
        //获得发起请求的客户端的ip
        String ip=request.getHeader("x-forwarded-for");     //通过nginx转发的客户端ip
        if (StringUtils.isBlank(ip)){
            ip=request.getRemoteAddr();     //从request中获取ip
            if (StringUtils.isBlank(ip)){
                ip="123.123.123.123";       //异常情况，不做过多拓展
            }
        }
        //调用认证中心进行验证
        if (StringUtils.isNotBlank(token)){
            //请求认证中心进行验证，得到验证结果verificationMap
            String verifyJson=HttpclientUtil
                    .doGet("http://passport.gmall.com:8085/verify?token=" + token+"&currentIp="+ip);
            verificationMap= JSON.parseObject(verifyJson,Map.class);
            assert verificationMap != null;
            verify= (String) verificationMap.get("status");
        }

        if (loginSuccessNeeded){
            //必须登录成功才能放行
            if (!verify.equals("success")){
                //从未登录，踢回认证中心
                response.sendRedirect("http://passport.gmall.com:8085/index?ReturnUrl="
                        +request.getRequestURL()+"&requestIP="+ip);
                return false;
            }else {
                //验证通过，覆盖cookie中的token
                //已登录，需要将token携带的用户信息写入
                request.setAttribute("memberId",verificationMap.get("memberId"));
                request.setAttribute("nicknam",verificationMap.get("nickname"));
                //验证通过，覆盖cookie中的token
                if (StringUtils.isNotBlank(token)){
                    CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
                }
            }
        }else {
            //没登录也能用，但必须验证
            if (verify.equals("success")){
                //已登录，需要将token携带的用户信息写入
                request.setAttribute("memberId",verificationMap.get("memberId"));
                request.setAttribute("nicknam",verificationMap.get("nickname"));
                //验证通过，覆盖cookie中的token
                    CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
            }
        }

        return true;
    }
}
```

PassportController.java

```java
@Controller
public class PassportController {

    @Reference
    UserService userService;

    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap modelMap){
        modelMap.put("ReturnUrl",ReturnUrl);
        return "index";
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token,String currentIp){
        //通过JWT校验token真假

        Map<String,String> map=new HashMap<>();
        Map<String, Object> decode = JwtUtil.decode(token, "2019gmall", currentIp);
        if (decode!=null){
            map.put("status","success");
            map.put("memberId",(String) decode.get("memberId"));
            map.put("nickname",(String) decode.get("nickname"));
        }else {
            map.put("status","fail");
        }
        return JSON.toJSONString(map);
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request){
        String token="";
        //调用用户服务验证用户名和密码
        UmsMember loginUser=userService.login(umsMember);
        if (loginUser!=null){
            //登录成功，用JWT制作token
            Map<String,Object> userMap=new HashMap<>();
            userMap.put("memberId",umsMember.getId());
            userMap.put("nickname",umsMember.getNickname());

            String ip=request.getHeader("x-forwarded-for");     //通过nginx转发的客户端ip
            if (StringUtils.isBlank(ip)){
                ip=request.getRemoteAddr();     //从request中获取ip
                if (StringUtils.isBlank(ip)){
                    ip="123.123.123.123";       //异常情况，不做过多拓展
                }
            }
            token = JwtUtil.encode("2019gmall", userMap, ip);
            //存入一份token到redis
            userService.addUserToken(token,umsMember.getId());
        }else {
            //登录失败
            token+="FAILED";
        }
        return token;
    }
}
```

### P190 社交登录

![image-20200229211819418](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200229211819418.png)

流程：

1. 用户希望通过第三方平台账号登录目的平台。首先重定向到第三方平台登录界面，登录第三方平台（请求第三方平台授权目的平台使用本用户的信息）。第三方平台准许授权，返回code给用户。
2. 用户拿到授权code之后将code给目的平台。
3. 目的平台以用户得到的code为凭证，向第三方平台请求access_token。
4. 目的平台得到access_token，以此向第三方平台的用户服务请求用户数据。



自己的授权ID和授权秘钥

![image-20200301012032849](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200301012032849.png)

修改回调地址（将来需要和授权请求地址保持一致）：

![image-20200301012322431](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200301012322431.png)

#### 四步完成授权操作：

1. 请求授权地址，用户和第三方签订授权协议

   > 授权地址公式：
   >
   > https://api.weibo.com/oauth2/authorize?client_id=YOUR_CLIENT_ID&response_type=code&redirect_uri=YOUR_REGISTERED_REDIRECT_URI

   访问我的授权地址：https://api.weibo.com/oauth2/authorize?client_id=547721421&response_type=code&redirect_uri=http://passport.gmall.com:8085/vlogin

   ![image-20200301030616985](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200301030616985.png)

2. 通过回调地址获得授权码：

   ![image-20200301030520596](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200301030520596.png)

3. 用授权码code交换access_token（必须用post请求）

   > 授权码请求公式：
   >
   > https://api.weibo.com/oauth2/access_token?client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET&grant_type=authorization_code&redirect_uri=YOUR_REGISTERED_REDIRECT_URI&code=CODE

   由我的appKey和secretKey构造访问url，注意：授权码有时限会过期；每生成一次授权码，之前的作废；且授权码只能用一次，第二次再使用无效：

   https://api.weibo.com/oauth2/access_token?client_id=547721421&client_secret=7f2d0d5edb88f273e22b6deac935d312&grant_type=authorization_code&redirect_uri=http://passport.gmall.com:8085/vlogin&code=926cbd9352e5361a5302b8a9099e5f3f

   请求成功后将返回一个JSON字符串，将其转换成Map取得其中access_token、uid等字段

4. 用access_token查询用户信息

   参考渣浪开发文档：[https://open.weibo.com/wiki/%E5%BE%AE%E5%8D%9AAPI](https://open.weibo.com/wiki/微博API)、https://open.weibo.com/wiki/2/users/show

##### 社交登录和项目的整合

1. 用户在passport认证中心准备登录时，可选择第三方登录，跳转到：https://api.weibo.com/oauth2/authorize?client_id=547721421&response_type=code&redirect_uri=http://passport.gmall.com:8085/vlogin，引导用户进入授权界面
2. 用户在授权完成后，第三方网站将授权码写到回调地址请求中，我们通过回调地址接收授权码并写入数据库
3. 通过授权码发送post请求到第三方网站，换取access_token:https://api.weibo.com/oauth2/access_token?client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET&grant_type=authorization_code&redirect_uri=YOUR_REGISTERED_REDIRECT_URI&code=CODE，将换取来的access_token和第三方平台用户相关信息写入到本平台用户数据库
4. 在用户使用的过程中通过access_token用GET请求获取用户数据（第三方平台的用户数据）。https://api.weibo.com/2/users/show.json?access_token=XXXX&uid=XXXXX。通过第三方社交登录的用户在本网站的信息需要补全，该用户在使用本平台的高级别功能时，需要进行信息补全操作甚至实名认证。

#### 代码实现：

PassportController.java

```java
@RequestMapping("vlogin")
    public String vlogin(String code,HttpServletRequest request){
        //授权码换取access_token
        String access_token_url="https://api.weibo.com/oauth2/access_token?";
        Map<String,String> paramMap=new HashMap<>();
        paramMap.put("client_id","547721421");
        paramMap.put("client_secret","7f2d0d5edb88f273e22b6deac935d312");
        paramMap.put("grant_type","authorization_code");
        paramMap.put("redirect_uri","http://passport.gmall.com:8085/vlogin");
        paramMap.put("code",code);
        String access_token_json = HttpclientUtil.doPost(access_token_url, paramMap);
        Map access_token_map=JSON.parseObject(access_token_json,Map.class);
        //access_token换取用户信息
        assert access_token_map != null;
        Long uid = Long.parseLong((String) access_token_map.get("uid"));
        String access_token = (String) access_token_map.get("access_token");
        String query_user_url="https://api.weibo.com/2/users/show.json?access_token="
                +access_token+"&uid="+uid;
        String user_json=HttpclientUtil.doGet(query_user_url);
        Map user_map = JSON.parseObject(user_json, Map.class);
        //将用户信息保存到数据库，用户类型设置为微博用户
        assert user_map!=null;
        UmsMember umsMember=new UmsMember();
        umsMember.setSourceType(2);
        umsMember.setAccessCode(code);
        umsMember.setAccessToken(access_token);
        umsMember.setSourceUid(uid);
        umsMember.setNickname((String) user_map.get("screen_name"));
        umsMember.setCity((String)user_map.get("location"));
        umsMember.setGender(user_map.get("gender").equals("m")?(user_map.get("gender").equals("f")?2:1):0);
        UmsMember login = userService.loginOauthUser(umsMember);

        //生成jwt的token，并且重定向到首页，携带该token
        String token=makeToken(login,request);
        return "redirect:http://search.gmall.com:8083/?token="+token;
    }

    /**
     * 为登录用户制作token
     * @param umsMember 登录用户信息
     * @return token
     */
    private String makeToken(UmsMember umsMember,HttpServletRequest request){
        String token="";
        if (umsMember!=null){
            //登录成功，用JWT制作token
            Map<String,Object> userMap=new HashMap<>();
            userMap.put("memberId",umsMember.getId());
            userMap.put("nickname",umsMember.getNickname());
            String ip=request.getHeader("x-forwarded-for");     //通过nginx转发的客户端ip
            if (StringUtils.isBlank(ip)){
                ip=request.getRemoteAddr();     //从request中获取ip
                if (StringUtils.isBlank(ip)){
                    ip="123.123.123.123";       //异常情况，不做过多拓展
                }
            }
            token = JwtUtil.encode("2019gmall", userMap, ip);
            //存入一份token到redis
            userService.addUserToken(token,umsMember.getId());
        }else {
            //登录失败
            token+="FAILED";
        }
        return token;
    }
```

UserServiceImpl.java:

```java
/**
 * 添加社交登录用户信息
 * @param umsMember 社交登录用户
 */
@Override
public UmsMember loginOauthUser(UmsMember umsMember) {
    UmsMember check=new UmsMember();
    check.setSourceUid(umsMember.getSourceUid());
    check.setSourceType(umsMember.getSourceType());
    UmsMember exist = userMapper.selectOne(check);
    if (exist==null){
        //首次采用第三方平台账号登录
        userMapper.insertSelective(umsMember);
    }else {
        //已有该第三方账号记录
        Example example=new Example(UmsMember.class);
        example.createCriteria().andEqualTo("sourceUid",umsMember.getSourceUid())
            .andEqualTo("sourceType",umsMember.getSourceType());
        userMapper.updateByExampleSelective(umsMember,example);     //更新一下账号信息
    }
    return userMapper.selectOne(umsMember);     //返回完整的登录用户信息
}
```

### P203结算和订单

##### 1.介绍

1. 电商平台支持多个设备登录，一个设备上登录并不会将另一个设备挤下线
2. 生成结算页并没有对后台数据库进行任何变更，原来购物车信息并没发生变化，也没有生成订单数据结构。只是把将要结算的商品数据整合展示出来，让用户确认送货清单和选择收获地址信息的页面
3. 点击提交订单按钮时，后台的购物车数据结构被删除，生成了订单数据结构。

##### 2.提交订单的业务

1. 用户确认了自己的订单信息
2. 用户选择收货地址
3. 确认其他信息（支付方式、发票、优惠券、积分、折扣）

##### 3.订单的安全

1. 如何防止用户通过页面回退的方式重复提交一个订单

   ![image-20200303221300631](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200303221300631.png)

   缓存中根据memberId生成交易码，在提交订单时检查交易码，然后销毁

   user:${memberId}:tradeCode（K）：随机字符串（V）

   **如何防止并发情况下一key多用——使用lua脚本在查询到该key时马上删除**

   ```java
   @Override
   public String checkTradeCode(String memberId,String tradeCode) {
       Jedis jedis=null;
       try{
           jedis=redisUtil.getJedis();
           String tradeKey="user:" + memberId + ":tradeCode";
           //使用lua脚本在发现key的同时删除key，防止订单攻击
           String script="if redis.call('get',KEYS[1]) == ARGV[1] " +
               "then return redis.call('del',KEYS[1]) else return 0 end";
           Long eval=(Long) jedis.eval(script, Collections.singletonList(tradeKey),
                                       Collections.singletonList(tradeCode));
           if (eval!=null&&eval!=0){
               return "success";
           }else return "fail";
       }finally {
           assert jedis != null;
           jedis.close();
       }
   }
   ```

   ##### 4.订单数据的提交

   1. 根据用户id获得要购买的商品列表(购物车),和总价格，为确保结算数据为最新数据不能采用当前页面的数据！

   2. 检验价格、库存（不替用户做决定）

      - 根据用户信息查询当前用户的购物车中的商品数据
      - 循环将购物车中的商品对象封装成订单对象（订单详情）
      - 每次循环一个商品时，校验当前商品的库存和价格是否符合购买需求

   3. 将订单和订单详情写入数据库

   4. 删除购物车对应的商品

   5. 支付对接：

      ![image-20200305025408641](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200305025408641.png)

      1. 用户请求谷粒商城进行支付

      2. 谷粒商城返回用户一个跳转支付宝的连接（带着谷粒商城和支付宝合作的appId）

      3. 用户的浏览器和支付宝交互过程中的安全问题

         非对称密钥加密（rsa）：两个超大质数乘积的因式分解不可逆原理

         通过rsa非对称密钥生成的网络签名，可以用来验证请求发送者的身份信息

      

### P221支付服务

![img](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/wps1.png)

![image-20200305165234145](C:\Users\Apollos\AppData\Roaming\Typora\typora-user-images\image-20200305165234145.png)

#### 支付宝对接步骤

> Alipay文档：https://docs.open.alipay.com/270

##### 和支付宝建立支付协议（企业级账号）

​	具体参考课件文档

##### 下载和整合支付宝sdk

​	支付宝已有maven仓库依赖，引入依赖即可：

```xml
<!-- https://mvnrepository.com/artifact/com.alipay.sdk/alipay-sdk-java -->
<dependency>
    <groupId>com.alipay.sdk</groupId>
    <artifactId>alipay-sdk-java</artifactId>
    <version>3.0.0</version>
</dependency>
```

![image-20200305175614834](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200305175614834.png)

然后设置配置文件和配置类：

首先在resource目录下创建配置文件alipay.properties:

```properties
# 尚硅谷的支付宝服务配置

# 支付宝接口地址
alipay_url=https://openapi.alipay.com/gateway.do
# 企业（尚硅谷）与支付宝间签订合作伙伴协议后得到的app_id
app_id=2018020102122556
# 企业的私钥
app_private_key=MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCdQeknhM2rhiGAH6V0ljxn3rAWIdzduTEQuteTfwjnZtvMhQPuuN1b/88D5yMuaZhZNFeUdWb+SmtP9DAzAWWgnT13T0YhJcxP6txm7JBRrjadCRt+LOFxPiPQk5t9fH7yXjw9i4uMDsNJeTncrVZ/AZYrk0ESC9anJR8XeuBc3HE8T4fqlKKl35jlumIWrPbPNQhKGXaGcOnpiaXO9qYYUSP/tnrjNYXHOso0yBs4YTl+LLX2TJ12p3n/oX6HnL4zQgtN5k4QasHP7CIig1ngcVQGfWsMm4djI9KXNXvGLQPfMQEmyb71mM5OCdl1MtAc6OaIAymhSv2hOLNIuyodAgMBAAECggEAe05/P5mGm4QlKI2n8u8KlneqovASe1kG/BNFjkYB+VBR8OAr4TfbepPvAyRuFap+5xN/yMz14VcBJkRWtufVhEdHNxJV7w/wUIncIGhGEYYFFMVbZWhTrbQH6TiUp6TC9dCmc6vD1CKPRkFj+YGBXT0lPy3LzBa0TYNyCbszyhthrgkpuFYbB0R93IPvvBh5NJFXQytwNb2oVopC9AQWviqnZUZcT0eJ087dQ1WLPa6blBD8DP1PUq0Ldr6pgKfObFxIj8+87DlJznRfdEsbqZlS7jagdw5tLr71WJpctIGPqKpgvajfePP/lj3eY82BKQB+aTw0zmAiB05Yes4LgQKBgQDq3EiQR8J1MEN2rpiLt1WvDYYvKVUgOY7Od//fRPgaMBstbe4TzGBpR8E+z267bHAWLaWtHkfX6muFHn1x68ozEUWk/nZq0smWnuPdcy4E7Itbk36W2FF/rOZB7j5ddlC9byrxDSNgcf9/FA/CU+i5KVQpLYfsk2dvwomvu0aFVQKBgQCraXpxzMmsBx4127LsZDO5bxfxb6nqzyK4NPe0VaGiRg8oaCWczcLz1J5iRqC9QeEwsSt4XU1sYBMTcsFpA0apZpm3prH2HJRx/isNENesaHcihF0mMd0WxU3xyRvWSDeZV5A1Zy1ZEJ+p17DGwb2j+yo2uBrDNXBgBWEzXwiRqQKBgBdXFvsHtqKQzlOQHGbeLGy+KlSrheMy9Sc9s7cLkqB/oWPNZfifugEceW71jGqh5y29EZb3yGoDyPWsxwi4Rxr2H3a7Nyd8lT4bwkdyt+MTYvIR4WW6T7chhqyMsbP2GyYIUzsrdBWUnrCRXNOSJTGpksyY0sZHC+OGcMp/EQ4VAoGBAIISSVL/pm1+/UK7U1ukcced8JpKNLM0uVD1CJ50eHHOHgR4e0owrWYfioxisejLjBlJ6AWvL2g0w2T3qKKKVN2JOM4ulU5/w3l4+KwygqaWowizTogEQJPd5ta52ADTzjTzSD/t6nByd+YHAWLhc4lyt0bMj6pf68VBb8/upm75AoGAGAYz79IVHp9eppykufjNcWu6okkG8tZnzuyaWKW/CuKKBWMaTk0vcyQlfJfxIBccoQrBuYyXBdcpPuZ/ys2C25pNrkACuhIKNgnMc0floJoYEfJzetw/3cIimWu4NJzVQOaojaGA58oo2+fub43Xn25Jq4rvSVe3oLdb5xWkw5Q=
# 支付宝的公钥
alipay_public_key=MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhkZi6W0wn/prX+NIIF9ATb5Z8ReKK4hFYtBrweDfGHD1mNW7YIZY4G5hE7S2Sry8eFXlFgSlBWlJ4fVnDaK9MkVThpwE2H65ooVlK/wLuyPqovIVpMt/utva5Ayuzv7eQOWK45FdLDNDlK8QLoBko6SS+YbnWnf7a+mrf4NAS4UFClpfe8Byqe8XIraO2Cg4Ko5Y5schX39rOAH8GlLdgqQRYVQ2dCnkIQ+L+I4Cy9Mvw3rIkTwt3MBU+AqREXY4r5Bn6cmmX/9MAJbFqrofGiUAqG+qbjTcZAzgNPfuiD0zXgt/YYjMQMzck75BOmwnYOam2ajODUSQn8Xybsa7wQIDAQAB
# 同步回调地址 重定向地址本地浏览器
return_payment_url=http://payment.gmall.com:8087/alipay/callback/return
# 异步通知地址 必须是公网接口(webService)
notify_payment_url=http://60.205.215.91/alipay/callback/notify
return_order_url=http://order.gmall.com:8086/orderList
```

然后在项目的config目录下创建配置类读取配置文件：

```java
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:alipay.properties")
public class AlipayConfig {
    @Value("${alipay_url}")
    private String alipay_url;
    @Value("${app_private_key}")
    private String app_private_key;
    @Value("${app_id}")
    private String app_id;
    public final static String format="json";
    public final static String charset="utf-8";
    public final static String sign_type="RSA2";
    public static String return_payment_url;
    public static  String notify_payment_url;
    public static  String return_order_url;
    public static  String alipay_public_key;
    @Value("${alipay_public_key}")
    public   void setAlipay_public_key(String alipay_public_key) {
        AlipayConfig.alipay_public_key = alipay_public_key;
    }
    @Value("${return_payment_url}")
    public   void setReturn_url(String return_payment_url) {
        AlipayConfig.return_payment_url = return_payment_url;
    }
    @Value("${notify_payment_url}")
    public   void setNotify_url(String notify_payment_url) {
        AlipayConfig.notify_payment_url = notify_payment_url;
    }
    @Value("${return_order_url}")
    public   void setReturn_order_url(String return_order_url) {
        AlipayConfig.return_order_url = return_order_url;
    }
    @Bean
    public AlipayClient alipayClient(){
        AlipayClient alipayClient=new DefaultAlipayClient(alipay_url,app_id,app_private_key,format,charset, alipay_public_key,sign_type );
        return alipayClient;
    }
}
```



##### 开发对接程序

![image-20200305204856552](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200305204856552.png)

具体可参考支付宝开发文档完成本部分

```java
    /**
     * 前往支付宝付款
     * @param outTradeNo 订单号
     * @param totalAmount 订单总额
     * @return 支付页面
     */
    @RequestMapping("alipay/submit")
    @LoginRequired
    @ResponseBody
    public String alipay(String outTradeNo,BigDecimal totalAmount,HttpServletRequest request,ModelMap modelMap){
        //获得一个支付宝请求的客户端（不是链接，而是封装好了http请求的表单请求）
        String form="";
        AlipayTradePagePayRequest alipayRequest=new AlipayTradePagePayRequest();  //创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);    //同步回调地址
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);    //异步通知地址
        Map<String,Object> map=new HashMap<>(); //必选请求参数
        map.put("out_trade_no",outTradeNo);     //订单号
        map.put("product_code","FAST_INSTANT_TRADE_PAY");   //支付宝签约产品码，固定
//            map.put("total_amount",totalAmount);      //订单总额
        map.put("total_amount",0.01);           //模拟订单总额
        map.put("subject","谷粒商城收款中心");     //订单描述
        String param= JSON.toJSONString(map);
        alipayRequest.setBizContent(param);
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();
            System.out.println(form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //生成并保存用户的支付信息
        OmsOrder omsOrder=orderService.getOrderByOutTradeNo(outTradeNo);
        PaymentInfo paymentInfo=new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(outTradeNo);
        paymentInfo.setTotalAmount(totalAmount);
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setSubject("一笔新的订单");
        paymentService.savePaymentInfo(paymentInfo);

        //提交请求到支付宝
        return form;
    }
```



##### 完成对接的回调接口

支付成功后回调函数（谷粒商城被支付宝调用的函数）

> 回调参数：https://docs.open.alipay.com/270/105902/

分析：

![image-20200305234044203](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200305234044203.png)

![image-20200305234259984](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200305234259984.png)

其html如下:

```html
<form name="punchout_form" method="post" action="https://openapi.alipay.com/gateway.do?charset=utf-8&method=alipay.trade.page.pay&sign=WoMSBhBgoYx8fxg71rP6PLngIR%2FpHCz6AQMU2hb6Dj%2F6YNn51KXkFxcUy74bhPd3Ti61%2FnGDt5KNKnfOo25QT%2BLfLR7EybmkHoGzYrl%2FIbwomPFjXzx4jU2Z5WxbS8W5m7uMTLXWLelmYbmgcWD1rvfnul0S7MaqVay22iNpfa5sEkadcFF5l25ZMhmuzZc%2FGOheLR18CwwdDyaHQWXBz%2BV%2BwchYoJZR4h6fVcZnEHapEfCLxLHCRXD6hpEj3%2B%2FUGRTGgUCptWzPw8wmGqih2EPsGstiLnjsOVitf97g%2B0jSEfITi35ltNFGo18uabhie0IQiif1dNcTHZBrKElUSQ%3D%3D&return_url=http%3A%2F%2Fpayment.gmall.com%3A8087%2Falipay%2Fcallback%2Freturn&notify_url=http%3A%2F%2F60.205.215.91%2Falipay%2Fcallback%2Fnotify&version=1.0&app_id=2018020102122556&sign_type=RSA2&timestamp=2020-03-05+23%3A39%3A30&alipay_sdk=alipay-sdk-java-dynamicVersionNo&format=json">
<input type="hidden" name="biz_content" value="{&quot;out_trade_no&quot;:&quot;gmall158339829580220200365165135&quot;,&quot;total_amount&quot;:0.01,&quot;subject&quot;:&quot;谷粒商城收款中心&quot;,&quot;product_code&quot;:&quot;FAST_INSTANT_TRADE_PAY&quot;}">
<input type="submit" value="立即支付" style="display:none" >
</form>
<script>document.forms[0].submit();</script>
```

从post表单中谷粒商城请求支付宝的url中，可看到其中谷粒商城的签名（sign）

```txt
sign=WoMSBhBgoYx8fxg71rP6PLngIR%2FpHCz6AQMU2hb6Dj%2F6YNn51KXkFxcUy74bhPd3Ti61%2FnGDt5KNKnfOo25QT%2BLfLR7EybmkHoGzYrl%2FIbwomPFjXzx4jU2Z5WxbS8W5m7uMTLXWLelmYbmgcWD1rvfnul0S7MaqVay22iNpfa5sEkadcFF5l25ZMhmuzZc%2FGOheLR18CwwdDyaHQWXBz%2BV%2BwchYoJZR4h6fVcZnEHapEfCLxLHCRXD6hpEj3%2B%2FUGRTGgUCptWzPw8wmGqih2EPsGstiLnjsOVitf97g%2B0jSEfITi35ltNFGo18uabhie0IQiif1dNcTHZBrKElUSQ%3D%3D

支付宝将会对谷粒商城的签名进行验签（根据谷粒商城保存在支付宝上的公钥）
```

扫码付款：

![image-20200305234816136](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200305234816136.png)

支付完成后回跳到商户地址：

![image-20200305234957971](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200305234957971.png)

支付宝回调谷粒商城url为：

```txt
https://unitradeprod.alipay.com/acq/cashierReturn.htm?sign=K1iSL1DZJcHKW4c2lsoAM4NPRPpBreqdGPhOp6e4L8MsYMlsLWgMBpvrvjvrsxL7ZpEYyRGW6MnpXeT%252Fg0pnE4ny&outTradeNo=gmall158339829580220200365165135&pid=2088921750292524&type=1
```

可看到其中支付宝的签名（sign）

```txt
K1iSL1DZJcHKW4c2lsoAM4NPRPpBreqdGPhOp6e4L8MsYMlsLWgMBpvrvjvrsxL7ZpEYyRGW6MnpXeT%252Fg0pnE4ny&outTradeNo=gmall158339829580220200365165135

谷粒商城验签（根据支付宝保存在谷粒商城上的公钥）
```

```java
    @RequestMapping("alipay/callback/return")
    @LoginRequired
    @ResponseBody
    public String alipayCallbackReturn(String outTradeNo,BigDecimal totalAmount,HttpServletRequest request,ModelMap modelMap){

        //回调请求中获取支付宝发来的参数
        String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_status = request.getParameter("trade_status");
        String total_amount = request.getParameter("total_amount");
        String subject = request.getParameter("subject");
        String call_back_content = request.getQueryString();

        //通过支付宝的paramsMap进行验证，2.0版本的接口将paramMap参数去掉了，导致同步请求没法验签
        if (StringUtils.isNotBlank(sign)){
            //验签成功
            PaymentInfo paymentInfo=new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setAlipayTradeNo(trade_no); //支付宝交易凭证号
            paymentInfo.setCallbackContent(call_back_content);  //回调请求字符串
            paymentInfo.setCallbackTime(new Date());
            //更新用户的支付状态为已付款
            paymentService.payUp(paymentInfo);
        }

        //支付成功后的系统服务：订单服务、库存服务、物流

        return "finish";
    }
```



##### 如何验签：

​	首先下载验签工具：![image-20200306000226150](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200306000226150.png)

> 文档连接，到该链接下载密钥生成工具：https://docs.open.alipay.com/291/105971/

下载完成，可用：

![image-20200306001347442](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200306001347442.png)

个人开发条件有限，无法完成异步回调功能。若有需求可参考文档



### P233分布式事务

![image-20200306041603200](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200306041603200.png)

![image-20200306034721987](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200306034721987.png)

**分布式事务**：在集群、分布式环境下，如何保持数据的一致性：分布式环境下，有些业务需要并发地去处理，并发的服务不能服务于和自己业务不同的数据结构。不同服务的不同数据结构如何在一个行为操作中同时提交或回滚。

> 微服务架构下的分布式事务解决方案：https://www.cnblogs.com/jiangyu666/p/8522547.html

##### 解决方案介绍：

1. xa协议下的两段式提交

   在xa写一下，提交一个事务需要经过两个阶段：预备+提交   
   弊端：性能太低

   ![image-20200306035725902](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200306035725902.png)

2. xa两段式提交的 进阶版：tcc（try confirm cancel）

   需要在业务层实现Try、Confirm、Cancel接口   
   弊端：入侵性太强（为了实现分布式事务要将业务代码进行大改动）

   ![image-20200306035802327](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200306035802327.png)

3. 基于消息的，采取最终一致性策略的分布式事务（性能效率高，电商网站采用）：消息队列MQ

   在一个事务正在进行的同时，发出消息给其他业务，如果消息发送失败或消息的执行失败，则回滚消息，重复执行    
   反复执行失败后，记录失败信息，后期补充性处理    
   在消息系统中开启事务，消息的事务是指，保障消息被正常消费，否则回滚的一种机制

##### 消息队列中间件

1. ActiveMQ，由Apache开发，基于jms的接口规则
2. RabbitMQ，C开发，基于amqp协议
3. kafka，大数据的消息中间件

##### ActiveMQ的安装和整合

解压对应压缩包，命令行来到bin目录下，输入：

```sh
activemq start
```

activemq的配置文件有点像tomcat，可前往conf目录下的activemq.xml配置

整合：首先在gmall-service-util中引入依赖(为了服务的并发)：

```xml
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-activemq</artifactId>
   <exclusions>
      <exclusion>
         <groupId>org.slf4j</groupId>
         <artifactId>slf4j-log4j12</artifactId>
      </exclusion>
   </exclusions>
</dependency>

<dependency>
   <groupId>org.apache.activemq</groupId>
   <artifactId>activemq-pool</artifactId>
   <version>5.15.2</version>
   <exclusions>
      <exclusion>
         <groupId>org.slf4j</groupId>
         <artifactId>slf4j-log4j12</artifactId>
      </exclusion>
   </exclusions>
</dependency>
```

##### 消息队列的模式

1. 点对点（Point to Point）

   点对点的消息系统中，消息分发给一个单独的使用者。点对点消息往往与队列（javax.jms.Queue）关联，若希望发送的每个消息都会被成功处理的话需要P2P模式

   ![image-20200306173541130](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200306173541130.png)

   每个消息只有一个消费者（Consumer）(即一旦被消费，消息就不再在消息队列中)

   发送者和接收者之间在时间上没有依赖性，也就是说当发送者发送了消息之后，不管接收者有没有正在运行，它不会影响到消息被发送到队列

   接收者在成功接收消息之后需向队列应答成功

2. 发布、订阅模式（Topic）

   发布/订阅消息系统支持一个事件驱动模型，消息生产者和消费者都参与消息的传递。生产者发布事件，而使用者订阅感兴趣的事件，并使用事件。该类型消息一般与特定的主题（javax.jms.Topic）关联。**如果希望发送的消息可以不被做任何处理、或者只被一个消息者处理、或者可以被多个消费者处理的话，那么可以采用Pub/Sub模型。**

   ![image-20200306174039980](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200306174039980.png)

   - 每个消息可以有多个消费者
   - 发布者和订阅者之间有时间上的依赖性。针对某个主题（Topic）的订阅者，它必须创建一个订阅者之后，才能消费发布者的消息。
   - 为了消费消息，订阅者必须保持运行的状态

3. 测试

	我们暂时在gmall-payment里新建test目录，复制如下测试代码
	P2P模式生产者：TestMqProducer.java
	
	```java
public static void main(String[] args) {
   
       ConnectionFactory connect = new ActiveMQConnectionFactory("tcp://localhost:61616");
       try {
           Connection connection = connect.createConnection();
           connection.start();
           //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
           Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue testqueue = session.createQueue("drink");
   
           MessageProducer producer = session.createProducer(testqueue);
           TextMessage textMessage=new ActiveMQTextMessage();
           textMessage.setText("谁帮我卢本伟倒一杯卡布奇诺？");
           producer.setDeliveryMode(DeliveryMode.PERSISTENT);
           producer.send(textMessage);
           session.commit();
           connection.close();
        System.out.println("我卢本伟下线了");
   
       } catch (JMSException e) {
           e.printStackTrace();
    }
   
	}
	```
```
   
P2P模式消费者：TestMqConsumer.java
   
​```java
   public static void main(String[] args) {
       ConnectionFactory connect = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,ActiveMQConnection.DEFAULT_PASSWORD,"tcp://localhost:61616");
       try {
           Connection connection = connect.createConnection();
           connection.start();
           //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
           Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
           Destination testqueue = session.createQueue("drink");
   
           MessageConsumer consumer = session.createConsumer(testqueue);
           consumer.setMessageListener(new MessageListener() {
               @Override
               public void onMessage(Message message) {
                   if(message instanceof TextMessage){
                       try {
                           String text = ((TextMessage) message).getText();
                           System.out.println(text+"\t我来帮你，我是线程:"+Thread.currentThread().getName());
                           //session.rollback();
                       } catch (JMSException e) {
                           e.printStackTrace();
                       }
                   }
               }
           });
       }catch (Exception e){
           e.printStackTrace();;
       }
   }
```

   可看到运行效果：

   ![image-20200306180722248](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200306180722248.png)

   provider发出消息后即可关闭连接，而consumer一直处于监听状态。可在后台查看目前消息队列情况：

   ![image-20200306180859902](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200306180859902.png)

   另外：**点对点模式允许多个消费者**，但一条消息只能被一个消费者消费。多个消费者情况下，**消费者们将以轮询的方式消费消息**

   Topic模式生产者，将createProducer的参数换成Topic即可：

   ```java
   Connection connection = connect.createConnection();
   connection.start();
   //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
   Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
   //话题模式消息
   Topic t=session.createTopic("speaking");
   MessageProducer producer = session.createProducer(t);
   TextMessage textMessage=new ActiveMQTextMessage();
   textMessage.setText("快多点来人帮我扛两百斤麦子！");
   producer.setDeliveryMode(DeliveryMode.PERSISTENT);
   producer.send(textMessage);
   session.commit();
   connection.close();
   ```

   Topic模式消费者，将session.createQueue改为session.createTopic：

   ```java
   Destination topic = session.createTopic("speaking");
   MessageConsumer consumer = session.createConsumer(topic);
   ```

   ![image-20200306201315877](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200306201315877.png)

   先开启三个consumer，再开启一个producer，producer发出的topic将同时被三个consumer消费

   ![image-20200306201507395](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200306201507395.png)

   topic模式消息无法持久化，若发出时没有消费者，就没了。它不像队列消息能记录消息状态。

   队列模式的消息在消息本身记录消息状态，话题模式消息由消费者记录消息状态（想要持久化的话在消费者客户端进行）

4. 事务控制：

   | producer提交时的事务  | 事务开启                                            | 只执行send并不会提交到队列中，只有当执行session.commit()时，消息才被真正的提交到队列中。 |
   | --------------------- | :-------------------------------------------------- | ------------------------------------------------------------ |
   | ↑                     | 事务不开启                                          | 只要执行send，就进入到队列中。                               |
   | consumer 接收时的事务 | 事务开启，签收必须写Session.SESSION_TRANSACTED      | 收到消息后，消息并没有真正的被消费。消息只是**被锁住。一旦出现该线程死掉、抛异常，或者程序执行了session.rollback()那么消息会释放，重新回到队列中被别的消费端再次消费。** |
   | ↑                     | 事务不开启，签收方式选择Session.AUTO_ACKNOWLEDGE    | 只要调用comsumer.receive方法 ，**自动确认**。                |
   | ↑                     | 事务不开启，签收方式选择Session.CLIENT_ACKNOWLEDGE  | 需要客户端执行 message.acknowledge(),否则视为未提交状态，线程结束后，其他线程还可以接收到。 这种方式跟事务模式很像，区别是不能手动回滚,而且可以单独确认某个消息。 |
   | ↑                     | 事务不开启，签收方式选择Session.DUPS_OK_ACKNOWLEDGE | 在Topic模式下做批量签收时用的，可以提高性能。但是某些情况消息可能会被重复提交，使用这种模式的consumer要可以处理重复提交的问题。 |

   **持久化**：通过**producer.setDeliveryMode(DeliveryMode.PERSISTENT)** 进行设置。持久化的好处就是当activemq宕机的话，消息队列中的消息不会丢失。非持久化会丢失。但是会消耗一定的性能。

##### mq整合Spring

1. 编写mq工厂

   ```java
   public class ActiveMQUtil {
       PooledConnectionFactory pooledConnectionFactory=null;
   
       public ConnectionFactory init(String brokerUrl) {
           ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            //加入连接池
           pooledConnectionFactory=new PooledConnectionFactory(factory);
           //出现异常时重新连接
           pooledConnectionFactory.setReconnectOnException(true);
           //
           pooledConnectionFactory.setMaxConnections(5);
           pooledConnectionFactory.setExpiryTimeout(10000);
           return pooledConnectionFactory;
       }
   
       public ConnectionFactory getConnectionFactory(){
           return pooledConnectionFactory;
       }
   }
   ```

2. 将mq工厂初始化到spring容器中:

   ```java
   @Configuration
   public class ActiveMQConfig {
       @Value("${spring.activemq.broker-url:disabled}")
       String brokerURL ;
       @Value("${activemq.listener.enable:disabled}")
       String listenerEnable;
       @Bean
       public    ActiveMQUtil   getActiveMQUtil() throws JMSException {
           if(brokerURL.equals("disabled")){
               return null;
           }
           ActiveMQUtil activeMQUtil=new ActiveMQUtil();
           activeMQUtil.init(brokerURL);
           return  activeMQUtil;
       }
       //定义一个消息监听器连接工厂，这里定义的是点对点模式的监听器连接工厂
       @Bean(name = "jmsQueueListener")
       public DefaultJmsListenerContainerFactory jmsQueueListenerContainerFactory(ActiveMQConnectionFactory activeMQConnectionFactory ) {
           DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
           if(!listenerEnable.equals("true")){
               return null;
           }
   
           factory.setConnectionFactory(activeMQConnectionFactory);
           //设置并发数
           factory.setConcurrency("5");
   
           //重连间隔时间
          factory.setRecoveryInterval(5000L);
          factory.setSessionTransacted(false);
          factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
   
           return factory;
       }
       @Bean
       public ActiveMQConnectionFactory activeMQConnectionFactory ( ){
   /*        if((url==null||url.equals(""))&&!brokerURL.equals("disabled")){
               url=brokerURL;
           }*/
           ActiveMQConnectionFactory activeMQConnectionFactory =
                   new ActiveMQConnectionFactory(  brokerURL);
           return activeMQConnectionFactory;
       }
   }
   ```

   注意application.properties中添加配置：

   ```properties
   # ActiveMQ消息端口，tcp协议
   spring.activemq.broker-url=tcp://localhost:61616
   # 开启监听
   activemq.listener.enable=true
   ```

   测试一下是否整合成功：

   ![image-20200306210322637](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200306210322637.png)

3. 将mq的监听器封装到spring容器中

##### 5.分布式事务的业务模型

![image-20200306223641235](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200306223641235.png)

0. 提交订单的延迟检查（支付服务）：PAYMENT_CHECK_QUEUE

1. 支付完成（支付服务）：PAYMENT_SUCCESS_QUEUE

2. 订单已支付（订单服务）：ORDER_PAY_QUEUE

3. 库存锁定（库存系统）：SKU_DEDUCT_QUEUE

4. 订单已出库（订单服务）：ORDER_SUCCESS_QUEUE

   其他：购物车合并队列、商品管理的同步队列



### P248延迟队列

##### 1.延迟队列解决的问题

**如果支付宝页面支付成功后就立即关闭页面，那么就无法进行回跳到商城提供的支付成功回调地址，进而导致请求回调地址时触发的更新支付状态逻辑无法被执行，商城后台无法更新订单状态。**

定时任务：在提交支付后，向消息队列发送一个延迟执行的消息任务，当该任务被支付服务执行时，在消费任务的程序中去查询当前交易的交易状态，根据交易状态（支付结束）决定解除延迟任务还是继续设置新的延迟任务。

配置消息队列的延迟属性：MQ的conf目录下activemq.xml中，在broker那一行新增：schedulerSupport=true

![image-20200307004548247](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200307004548247.png)

##### 2. 消费延迟队列（支付服务）：

检查当前订单的交易状态，根据交易状态（没有成功支付），设置重新发送延迟检查的时间和队列    
检查当前订单的交易状态，根据交易状态（支付成功），更新支付信息发送订单队列（幂等性检查）

查询接口：

![image-20200307050901873](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200307050901873.png)

在支付服务的m消费端PaymentServiceMqListener 调用阿里的支付查询接口(通过paymentService)   
（特殊可选：必选其一）：

![image-20200307052231829](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200307052231829.png)

1. 需要有延迟检查的限制

   ![image-20200307173247396](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200307173247396.png)

   不能无限循环检查，需要加入检查次数限制，满N结束检查

   

2. 检查支付情况的接口

   调用支付宝订单查询的API，将返回如下支付情况查询结果：

   - 交易未创建

     ![image-20200307174536656](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200307174536656.png)

   - 交易已创建但未支付

     ![image-20200307174323245](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200307174323245.png)

   - 交易成功

     ![image-20200307174737355](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200307174737355.png)

     根据查询结果对消息队列做相应处理：

     PaymentServiceImpl.java:

     ```java
     @Override
     public Map<String, Object> checkAlipayPayment(String out_trade_no) {
         Map<String,Object> resultMap=new HashMap<>();
     
         AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
         Map<String,Object> requestMap=new HashMap<>();
         requestMap.put("out_trade_no",out_trade_no);
         request.setBizContent(JSON.toJSONString(requestMap));
         AlipayTradeQueryResponse response = null;
         try {
             response = alipayClient.execute(request);
         } catch (AlipayApiException e) {
             e.printStackTrace();
         }
         if(response.isSuccess()){
             System.out.println("调用支付宝服务接口查询交易结果，交易可能创建成功");
             resultMap.put("out_trade_no",response.getOutTradeNo());
             resultMap.put("trade_no",response.getTradeNo());
             resultMap.put("trade_status",response.getTradeStatus());
         } else {
             System.out.println("调用支付宝服务接口查询交易结果，交易可能失败");
         }
         return resultMap;
     }
     ```

     PaymentServiceMqListener.java:

     ```java
     @JmsListener(destination = "PAYMENT_CHECK_QUEUE",containerFactory = "jmsQueueListener")
     public void paymentCheckResultConsumer(MapMessage mapMessage) throws JMSException {
         String out_trade_no = mapMessage.getString("out_trade_no");
         int count= mapMessage.getInt("count"); //剩余检查次数
         //调用支付宝检查接口
         Map<String,Object> resultMap=paymentService.checkAlipayPayment(out_trade_no);
     
         if (resultMap!=null&&!resultMap.isEmpty()){
             String trade_status=(String) resultMap.get("trade_status");
             if (StringUtils.isNotBlank(trade_status)&&
                 trade_status.equals("TRADE_SUCCESS")){
                 //paymentService.payUp(null);
                 System.out.println("支付成功，调用支付服务，修改支付信息和发送支付成功的队列");
                 return;
             }
         }
         //继续发送延迟检查任务，计算延迟时间等
         if (count>0){
             //继续发送延迟检查任务，计算延迟时间等
             System.out.println("没支付成功，继续发送延迟检查消息，还剩检查次数："+count);
             count--;
             paymentService.sendDelayPaymentResultCheckQueue(out_trade_no,count);
         }else {
             System.out.println("检查次数用尽，放弃检查");
         }
     }
     ```

##### 3.幂等性问题

​	幂等性：服务器对于相同的一次或多次请求，所返回的状态结果应该是一致的。

##### 4. 库存系统的介绍：

 1. 库存表

    wms_ware_info，库存信息表

2. 库存系统和商品（sku）的对应关系表

   wms_ware_sku，库存中的商品与电商平台商品对应关系表：多对多的关系

3. wms_ware_order_task

   库存根据订单的库存情况，对订单拆分的结果（一个订单的多项商品分别在不同的仓库，故该订单需拆分以便从多个仓库取货）

   ![image-20200307225524566](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200307225524566.png)

4. wms_ware_order_task_detail：拆单所得的子订单中的商品详情

5. 拆单因素：商家、库存、商品类型、价格、物流等其他因素

   ![image-20200307230401041](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200307230401041.png)



### P259基本完成，一些问题的补充

1. 在商品后台管理中，发布商品SKU时应当用消息队列发出商品的同步消息，同步缓存（在SkuService里实现）及搜索引擎（在SearchService里实现）。

2. 搜索热度问题：搜索商品时，热度值字段需要更新。但在ES搜索的同时更新热度值（ES将会更新索引，但ES并不是内存数据库，会做相应的读写IO操作，十分影响性能；修改某一个值在高并发的情况下会有冲突，造成更新的丢失，需要枷锁，而es的乐观锁会恶化性能问题）会给服务器造成非常大的压力。

   我们可以把热度值字段单独存储在redis中。在redis中专门放置一个与es中对应的热度值字段，es搜索后，根据搜索结果商品的id，取出热度值字段，再依次对搜索结果进行排序。

   用es做精确计数器，redis是内存数据库有强大的读写性能，利用redis原子性的自增可解决并发写操作问题。redis每100次技术我们就更新一次es，这样就能将es的写操作稀释100倍，这个倍数可根据业务需求灵活设定。

3. 购物车模块，用户登录时应当合并Cookie和db中的购物车数据，并且同步redis。当时避免服务串行而没实现（这样会让登录和购物车功能的耦合，购物车会影响到登录功能）

   解决：在用户登录时发出用户登录的消息（话题消息topic），让cartService消费，做购物车的合并及同步缓存。

   在访问购物车列表时，如果当前用户已登录，则删除cookie中多余的购物车数据。

4. 库存与订单数据不一致问题：提交订单时，商品可能已经发生库存的变动（被买走，库存被锁定）

   解决：调用库存服务的库存查询接口，作库存的校验

5. 库存削减的队列（SKU_DEDUCT_QUEUE）

   由订单服务消费，订单服务修改订单状态为准备出库

### P260秒杀和限流

服务器容量：一台或多台服务器最多能够承载的当前的连接数，也就是所能装的session总数量。若当前最大连接数10w，连接数已满，为防止下一个人连接我们可以用servlet的sessionListener监听session上限。

服务器流量：某单位时间内的接收或处理请求的数量（吞吐量），

#### 秒杀

基于redis解决

1. 缓存秒杀模型：

   redis是单线程的，所以在redis中所有命令都是原子操作。而当要多条redis命令同时执行而不被打断时，则需要使用redis的事务了。 

   ![image-20200308030042315](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200308030042315.png)

   ![image-20200308031414228](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200308031414228.png)

   

2. 新建秒杀项目Demo：gmall-seckill

   ​	引入依赖：gmall-parent、gmall-api、gmall-web-util、gmall-service-util

   - 基于redis的简易秒杀：

   利用redis的incrBy写数据。先写个简单的请求响应试试：

   ![image-20200308171318881](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200308171318881.png)

   用Apache进行一下简单的压力测试：

   ![image-20200308171945995](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200308171945995.png)

   再用两百条请求模拟一下，这次抢购前先打印剩余库存数：

   ![image-20200308173113839](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200308173113839.png)

   > MULTI命令 ：
   >
   > 用于开启一个事务，它总是返回OK。MULTI执行之后,客户端可以继续向服务器发送任意多条命令， 这些命令不会立即被执行，而是被放到一个队列中，当 EXEC命令被调用时， 所有队列中的命令才会被执行。
   >
   > EXEC命令 ：
   >
   > 负责触发并执行事务中的所有命令。如果客户端成功开启事务后执行EXEC，那么事务中的所有命令都会被执行。 如果客户端在使用MULTI开启了事务后，却因为断线而没有成功执行EXEC,那么事务中的所有命令都不会被执行。    
   > 需要特别注意的是：即使事务中有某条/某些命令执行失败了，事务队列中的其他命令仍然会继续执行——Redis不会停止执行事务中的命令，而不会像我们通常使用的关系型数据库一样进行回滚。
   >
   > DISCARD命令 ：
   >
   > 当执行 DISCARD 命令时， 事务会被放弃， 事务队列会被清空，并且客户端会从事务状态中退出。
   >
   > WATCH 命令 
   >
   > 可以为Redis事务提供 check-and-set （CAS）行为。被WATCH的键会被监视，并会发觉这些键是否被改动过了。 如果有至少一个被监视的键在 EXEC 执行之前被修改了， 那么整个事务都会被取消， EXEC 返回nil-reply来表示事务已经失败。

   ```java
   // 加入watch与multi命令，解决并发问题
   Jedis jedis=redisUtil.getJedis();
   jedis.watch("122");
   int stock=Integer.parseInt(jedis.get("122"));
   if (stock>0){
       Transaction multi = jedis.multi();
       multi.incrBy("122",-1);
       List<Object> exec = multi.exec();
       if (exec!=null&&exec.size()>0){
           System.out.println("当前库存剩余数量："+stock+
                              ",某某用户抢购成功。当前抢购人数："+(1000-stock));
       }else {
           System.out.println("当前库存剩余数量："+stock+
                              ",某某用户抢购失败");
       }
   }
   jedis.close();
   ```

   10k条请求，并发量为10进行测试，结果正常，并发问题基本得以解决

   ![image-20200309014524720](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200309014524720.png)

   watch的不足在于，并发情况下若抢购失败就真的失败了，不能排队继续等待。抢购的成功与否与抢购瞬间并发量有关，与先下手和后下手无关。属于随机拼运气形式的秒杀

   - 基于redission的秒杀

   ```java
   @RequestMapping("/seckill")
   @ResponseBody
   public String seckill(){
       Jedis jedis = redisUtil.getJedis();
       RSemaphore semaphore = redissonClient.getSemaphore("122");
       boolean b = semaphore.tryAcquire();
       if (b){
           int stock = Integer.parseInt(jedis.get("122"));
           System.out.println("当前库存剩余数量："+stock+
                              ",某某用户抢购成功。当前抢购人数："+(1000-stock));
           //消息队列发出订单消息
       }else {
           int stock = Integer.parseInt(jedis.get("122"));
           System.out.println("当前库存剩余数量："+stock+
                              ",某某用户抢购失败");
       }
       jedis.close();
       return "1";
   }
   ```

   ![image-20200309023540790](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200309023540790.png)

   ![image-20200309024139594](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200309024139594.png)

   redission采用了信号量的机制，可做到手快有手慢无的秒杀实现。

#### 限流

服务器容量：一台或多台服务器最多能够承载的当前的连接数，也就是所能装的session总数量。若当前最大连接数10w，连接数已满，为防止下一个人连接我们可以用servlet的sessionListener监听session上限。

服务器流量：某单位时间内的接收或处理请求的数量（吞吐量）

限流一般在Nginx中进行，用lua脚本去写

限流方案：

1. 按秒计算服务器请求是否达到最大值（如1w请求），超过最大值则拒绝请求

   缺点：所计算时间内，第999ms和第1002ms之间突然打来1w条请求，该方法失效

2. 漏桶算法：

   漏桶算法很好的解决了时间边界处理不够***\*平滑\****的问题，在每次请求进桶前都将执行“漏水”的操作，然后再计算当前水量，即不以时间为界限，而以流量为界限进行计算，回避了***\*时间\*******\*边界\****的问题。

   ```java
   /*****伪代码*****/
   long timeStamp = getNowTime(); 
   int capacity = 10000;// 桶的容量，即最大承载值
   int rate = 1;//水漏出的速度，即服务器的处理请求的能力
   int water = 100;//当前水量，即当前的即时请求压力
   
   //当前请求线程进入漏桶方法，true则不被拒绝，false则说明当前服务器负载水量不足，则被拒绝
   public static bool control() {
   long  now = getNowTime();//当前请求时间
   //先执行漏水代码
   //rate是固定的代表服务器的处理能力，所以可以认为“时间间隔*rate”即为漏出的水量
       water = Math.max(0, water - (now - timeStamp) * rate);//请求时间-上次请求时间=时间间隔
       timeStamp = now;//更新时间，为下次请求计算间隔做准备
       if (water < capacity) { 
           // 执行漏水代码后，发现漏桶未满，则可以继续加水，即没有到服务器可以承担的上线
           water ++; 
           return true; 
       } else { 
           return false;//水满，拒绝加水，到服务器可以承担的上线，拒绝请求
      } 
   }
   ```

3. 令牌桶算法

   令牌桶算法的原理是系统会以一个恒定的速度往桶里放入令牌，而如果请求需要被处理，则需要先从桶里获取一个令牌，当桶里没有令牌可取时，则拒绝服务。

   ![image-20200309031713936](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200309031713936.png)

   从桶里拿了门票令牌才能进

   ```java
   /********伪代码********/
   long timeStamp=getNowTime(); 
   int capacity; // 桶的容量 
   int rate ;//令牌放入速度
   int tokens;//当前水量  
   
   bool control() {
      //先执行添加令牌的操作
      long  now = getNowTime();
      tokens = max(capacity, tokens+ (now - timeStamp)*rate); 
      timeStamp = now;  
      if(tokens<1){
        return false; //令牌已用完，拒绝访问
      }else{ 
        tokens--;
        retun true; //还有令牌，领取令牌
      }
    }
   ```

   





### 总结

![image-20200309032519942](https://raw.githubusercontent.com/WinstonSmith1989/mymarkdownpics/master/img/img_谷粒商城/image-20200309032519942.png)