# RabbitMQ
参考：  
https://www.rabbitmq.com/documentation.html

## 启动RabbitMQ服务

#### Window平台 
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


## RabbitMQ 官方Demo
这部分全参考自官网教程   
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
    一个生产者将消息发给一个queue，（如果有多个消息）再将消息以轮回或公平分发的策略发给多个消费者，
    后面的几个场景也是如此，也可以指定多个消费者。
    ![](https://www.rabbitmq.com/img/tutorials/python-two.png)

    涉及内容：  
    - 消息确认(当消息处理异常时，将消息重新传给另一个消费者进行处理，这个功能是默认打开的)  
        消费者接收处理完消息后会返回确认信息，然后消息才会从队列中删除，如果消费者线程处理异常导致connection和channel关闭，
        RabbitMQ会立即将消息传给其他在线的消费者进行处理。
        消息没有超时的概念，即使一个消息处理花费的时间很长很长，只要连接正常就不会转发。
        ```
        channel.basicAck(envelope.getDeliveryTag(), false); //返回确认信息(multiple为false表示只返回delivery tag)
        channel.basicConsume(TASK_QUEUE_NAME, false, consumer);     //send a proper acknowledgment from the worker
        ```
    - 消息持久性（为确保即使RabbitMQ退出或崩溃后消息仍然不会丢失）  
       ```
       channel.queueDeclare("task_queue", durable, false, false, null);  //第二个参数为true,队列为持久化的队列，否则为非持久化队列    
       
       channel.basicPublish("", "task_queue",
                   MessageProperties.PERSISTENT_TEXT_PLAIN,
                   message.getBytes());
       ```
    - 发布确认(相对于上面两种策略更加强大的一种保护措施)
        参考： https://www.rabbitmq.com/confirms.html
    - 轮回分发  
        轮流将队列中的消息分配给几个消费者（如有四个消息A\B\C\D，两个工作线程W1\W2，则A\C分配给W1,B\D分配给W2）
    - 公平分发  
        不会为消费者分发新的消息直到处理完成前一个消息并返回确认信息。  
        这样还是有遗留问题的，如果所有的消费者（工作线程）都是忙状态，队列可能会被填满，这时就需要添加更多工作者线程或者使用其他策略。
        ```
        int prefetchCount = 1;
        channel.basicQos(prefetchCount);
       ```
       
+ Publish/Subscribe  
    发布/订阅 exchange通过广播的方式将一个消息发送给与exchange绑定的queue，然后再发给一个或多个消费者。 
    
    RabbitMQ更通用的消息模型  
    ![](https://www.rabbitmq.com/img/tutorials/python-three-overall.png)  
    生产者并不将消息直接放入工作队列，而是交给exchange(交换机)，exchange决定将消息放在哪个工作队列或哪些工作队列或是否丢弃；
    这些规则取决于exchange的类型。
    
    direct：  
    topic：   
    headers：   
    fanout：将收到的消息广播给所有与其相关联的队列。
    
    channel.basicPublish("", "hello", null, message.getBytes());这种用法其实是使用了默认的无名exchange。
    消息被路由到routingKey指定名称的队列（如果队列存在的话）。
    
    这个实例是一个简单的日志系统，由两个程序组成，一个发送日志消息，一个接收消息并打印。  
    
    ```
    # 查看exchange的命令
    rabbitmqctl.bat list_exchanges
    # 查看exchange和queue的绑定
    rabbitmqctl.bat list_bindings
    ```
    
+ Routing  
    只订阅某些消息，生产者按routing key发送消息，exchange通过direct模式和routing key匹配接收消息的队列，
    将消息发给匹配的队列。
   
    channel.queueBind(queueName, EXCHANGE_NAME, "black");
    队列绑定到交换机可以额外指定一个routingKey参数（如上），对不同的exchange类型有不同的意义。  
    fanout 类型会直接无视此参数，将消息传递给所有绑定的队列；  
    direct 类型只会将与queueBind中routingKey值匹配的消息传递给绑定的队列。
     
    如此demo所示  
    ![](https://www.rabbitmq.com/img/tutorials/python-four.png)  
    上图对应代码：
    ```
    # Productor
    channel.exchangeDeclare(EXCHANGE_NAME, "direct");
    String[] routingKeys = {"info", "error", "warning"}; 
    for(String routingKey : routingKeys) {
        channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes());
    }
    # Customer
    channel.exchangeDeclare(EXCHANGE_NAME, "direct");
    channel.queueBind(queue1Name, EXCHANGE_NAME, "error");
    for(String severity : argv){
        channel.queueBind(queue2Name, EXCHANGE_NAME, severity);
    }
    ```
   
+ Topics  
    生产者不只按固定的routing key发送消息，而是按字符串正则表达式“匹配”发送，接收端同样如此。
    
    ![](https://www.rabbitmq.com/img/tutorials/python-five.png)
    
    
    
+ RPC

## RabbitMQ 实际应用

## Spring Boot 应用集成 RabbitMQ

