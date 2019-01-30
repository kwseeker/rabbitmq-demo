package top.kwseeker.mydemo.exchange;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.kwseeker.mydemo.Constant.Constants;

/**
 * 讲述常用类型 exchange 的使用方法
 *      direct：此模式可以使用RabbitMQ自带的Exchange,不需要绑定操作，直接通过RouteKey完全匹配队列传递；
 *      topic：此模式可以进行模糊匹配（#匹配一或多个词，*匹配一个词）
 *      fanout：不通过路由键过滤消息(性能最好)，直接将队列绑定到交换机上，只要有消息发送到交换机都会转发到与改交换机绑定的队列；
 *      headers：经过消息头路由，不常用。
 *
 */
public class MyProducer {

    private static final Logger logger = LoggerFactory.getLogger(MyProducer.class);

    public static void main(String[] args) throws Exception {
        //1 创建ConnectionFactory（配置连接和Socket）
        ConnectionFactory connectionFactory = new ConnectionFactory();
        //1.1 ConnectionFactory属性有很多（一般都有默认值），如：用户名、密码、虚拟主机、最大Channel数、最大Frame数、host、port、
        //      TCP超时时间、握手超时时间、关闭超时时间、Channel RPC超时时间、网络自动恢复间隔、工作池超时时间、
        //      一些线程池操作接口
        //connectionFactory.setHost("localhost");     //设置host，即rabbitmq-server所在的主机, 这句写不写都一样默认就是localhost
        connectionFactory.setPort(5672);
        //connectionFactory.setVirtualHost("/");

        //2 创建Connection
        //      executor
        //      addressResolver
        //      clientProvidedName：多增加一个连接属性 connection_name
        try (Connection connection = connectionFactory.newConnection();  //newConnection() -> AutorecoveringConnection(params, fhFactory, addressResolver, metricsCollector)
             Channel channel = connection.createChannel()                //TODO: channelNumber ?
            ){

            /**
             * 测试默认的 exchange
             * 连接队列 mydemo_queue_1
             */
            //3 声明一个队列
            AMQP.Queue.DeclareOk declareOk = channel.queueDeclare(Constants.QUEUE_TEST1_NAME, false, false, false, null);
            logger.info("声明队列：" + declareOk.getQueue());
            //4 通过Channel发送消息
            //  TODO: basicPublish() 原理， RoutingKey 和 Exchange、Queue到底什么关系
            for (int i = 0; i < 5; i++) {
                String msg = "Hello RabbitMQ " + i;
                //TODO：为什么使用默认 Exchange, RoutingKey 是队列名, 直接通过RoutingKey查找队列么？ AMQCommand.transmit()
                channel.basicPublish("", Constants.QUEUE_TEST1_NAME, null, msg.getBytes());    //使用默认的 Exchange （Direct方式）
            }

            /**
             * 测试自定义的 Direct exchange
             * 连接队列 mydemo_queue_2
             */
            channel.exchangeDeclare(Constants.EXCHANGE_DIRECT_NAME, "direct", false, false, false, null);
            channel.queueDeclare(Constants.QUEUE_TEST2_NAME, false, false, false, null);
            channel.queueBind(Constants.QUEUE_TEST2_NAME, Constants.EXCHANGE_DIRECT_NAME, Constants.ROUTING_KEY_TEST_DIRECT);
            channel.basicPublish(Constants.EXCHANGE_DIRECT_NAME, Constants.ROUTING_KEY_TEST_DIRECT, null, "Message for test direct exchange".getBytes());

            /**
             * 测试自定义的 Fanout exchange
             * 连接队列 mydemo_queue_31/mydemo_queue_32
             */
            channel.queueDeclare(Constants.QUEUE_TEST31_NAME, false, false, false, null);
            channel.queueDeclare(Constants.QUEUE_TEST32_NAME, false, false, false, null);
            channel.exchangeDeclare(Constants.EXCHANGE_FANOUT_NAME, "fanout", false, false, false, null);
            channel.queueBind(Constants.QUEUE_TEST31_NAME, Constants.EXCHANGE_FANOUT_NAME, ""); //不用设置路由键但是也不能为null，设为空字符串
            channel.queueBind(Constants.QUEUE_TEST32_NAME, Constants.EXCHANGE_FANOUT_NAME, "");
            channel.basicPublish(Constants.EXCHANGE_FANOUT_NAME, "", null, "Message for test fanout exchange".getBytes());

            /**
             * 测试自定义的 Topic exchange
             * 连接队列 mydemo_queue_4/41/42/4*
             * topic 发送时必须严格匹配，取出的时候
             * RoutingKey
             *      "mydemo.test.topic.#"     -> 41/42
             *      "mydemo.test.topic.1.1"   -> 41
             *      "mydemo.test.topic.2"   -> 42
             *      "mydemo.test.topic.*"   -> 42
             */
            channel.exchangeDeclare(Constants.EXCHANGE_TOPIC_NAME, "topic", false, false, false, null);
            channel.queueDeclare(Constants.QUEUE_TEST41_NAME, false, false, false, null);
            channel.queueBind(Constants.QUEUE_TEST41_NAME, Constants.EXCHANGE_TOPIC_NAME, Constants.ROUTING_KEY_TEST_TOPIC1);
            channel.queueDeclare(Constants.QUEUE_TEST42_NAME, false, false, false, null);
            channel.queueBind(Constants.QUEUE_TEST42_NAME, Constants.EXCHANGE_TOPIC_NAME, Constants.ROUTING_KEY_TEST_TOPIC2);
            channel.queueDeclare(Constants.QUEUE_TEST43_NAME, false, false, false, null);
            channel.queueBind(Constants.QUEUE_TEST43_NAME, Constants.EXCHANGE_TOPIC_NAME, Constants.ROUTING_KEY_TEST_TOPIC);
            channel.queueDeclare(Constants.QUEUE_TEST44_NAME, false, false, false, null);
            channel.queueBind(Constants.QUEUE_TEST44_NAME, Constants.EXCHANGE_TOPIC_NAME, Constants.ROUTING_KEY_TEST_TOPIC3);
            channel.basicPublish(Constants.EXCHANGE_TOPIC_NAME, Constants.ROUTING_KEY_TEST_TOPIC1, null,
                    "Message 1 for test topic exchange".getBytes());    //发送到41/43
            channel.basicPublish(Constants.EXCHANGE_TOPIC_NAME, Constants.ROUTING_KEY_TEST_TOPIC2, null,
                    "Message 2 for test topic exchange".getBytes());    //发送到42/43/44

        }

    }
}
