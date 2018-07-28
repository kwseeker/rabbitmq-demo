# RabbitMQ
参考：  
https://www.rabbitmq.com/documentation.html

## 启动RabbitMQ服务

+ Window平台 
https://www.rabbitmq.com/install-windows.html
1. 安装Erlang
    去官网下载安装即可，然后配置环境变量将安装目录添加到Path。  
    测试Erlang是否安装配置成功：
    ```
    > erl   # 正常的话会输出版本号
    ```
2. 安装RabbitMQ Server
    默认安装会将RabbitMQ安装为后台服务，开机自动启动。  
    如果服务正常启动可以看到 任务管理器 > 服务 > RabbitMQ

    进入安装目录的sbin, 可通过命令行执行如下操作：
    ```
    rabbitmq-service start    # 启动服务
    rabbitmq-service stop     # 停止服务
    rabbitmq-service remove   # 移除服务
    rabbitmq-service install  # 安装为服务
    ```

    开启及关闭管理拓展插件
    ```
    rabbitmq-plugins enable rabbitmq_management
    rabbitmq-plugins disable rabbitmq_management
    ```
    新建用户及设置用户权限
    ```
    # rabbitmqctl更多操作：https://www.rabbitmq.com/man/rabbitmqctl.8.html
    rabbitmqctl add_user <username> <password>
    rabbitmqctl set_user_tags <username> <administrator>  # 设置用户为管理员
    ```
3. 自定义RabbitMQ环境变量
    有些时候可能需要自定义（或者说客制化）环境变量，有两种方法：  
    - Start > Settings > Control Panel > System > Advanced > Environment Variables 中创建和编辑环境变量的名称和值；  
    - 创建和编辑 rabbitmq-env-conf.bat 用于定义环境变量，这个文件的路径地址由 RABBITMQ_CONF_ENV_FILE 环境变量指定。   
    注意：环境改变后，需要重新安装服务。

    Rabbit环境变量配置的内容：  
    RABBITMQ_NODE_IP_ADDRESS  
    RABBITMQ_NODE_PORT  
    RABBITMQ_DIST_PORT  
    RABBITMQ_NODENAME  
    RABBITMQ_CONFIG_FILE  
    RABBITMQ_ADVANCED_CONFIG_FILE  
    RABBITMQ_CONF_ENV_FILE  
    RABBITMQ_USE_LONGNAME  
    RABBITMQ_SERVICENAME  
    RABBITMQ_CONSOLE_LOG  
    RABBITMQ_CTL_ERL_ARGS  
    RABBITMQ_SERVER_ERL_ARGS
    RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS
    RABBITMQ_SERVER_START_ARGS  
    ...  

    详细参考：  
    https://www.rabbitmq.com/configure.html#configuration-file  

## RabbitMQ 概念基础

+ 通信协议
    消息队列建立连接并通信的实现协议
    - AMQP 0-9-1
    - 

## RabbitMQ 官方Demo
https://www.rabbitmq.com/getstarted.html
https://github.com/rabbitmq/rabbitmq-tutorials (github上的源码)

+ Hello World
    最简单的RabbitMQ消息队列模型是  
    ![](https://www.rabbitmq.com/img/tutorials/python-one.png)  
    一个生产者发送消息给队列，队列将消息再发给一个消费者。  
    多个生产者可以将消息发送给同一个消息队列，多个消费者也可以从同一个消息队列获取消息。  
    
    此Demo演示一个生产者发送一个消息给队列，一个消费者从队列获取消息并将结果打印出来。
    里面只是涉及RabbitMQ API最简单的使用。  
    
    依赖（amqp-client slf4j-api slf4j-simple）
    ```
    <dependency>
        <groupId>com.rabbitmq</groupId>
        <artifactId>amqp-client</artifactId>
        <version>LATEST</version>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>LATEST</version>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId>
        <version>LATEST</version>
    </dependency>
    ```
    
    实现步骤：
    1) 通过ConnectionFactory创建新的连接Connection，并为新的连接创建Channel;  
        Connection是socket连接的抽象，负责协议版本协商和认证等等；
    2) 声明使用的队列[是幂等的,其任意多次执行所产生的影响均与一次执行的影响]（包括名称、服务重启队列是否保留、是否是创建专属队列、是否自动删除，队列构造参数）
        若此名称队列已存在则不会新创建。
    3) 发布消息
        可以指定交换机exchange,消息传给哪个队列，消息其他的一些属性（如：路由头部等等）    
    4) 消费者端同上1、2步，创建Channel声明关注的队列  
    5) 创建消费者实例，重写handleDelivery方法处理消息  
        handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
        consumerTag: ? 服务器自动生成的消费者的标志
        envelope:   信封很形象，指定一些传输地址信息
        properties: 指定contentHeader（内容类型、编码格式、传输模式、优先级、时间戳、正文大小等等，类似http头）
        body:       正文
    6) 使用创建的消费者实例监听队列
    
    查看创建的消息队列  
    ```
    rabbitmqctl.bat list_queues
    # 结果如下
    # Timeout: 60.0 seconds ...
    # Listing queues for vhost / ...
    # hello   0           # hello是队列名 0表示队列里面的消息条数
    ```
    
+ Work queues

+ Publish/Subscribe

+ Routing

+ Topics

+ RPC

## RabbitMQ 实际应用

## Spring Boot 应用集成 RabbitMQ

