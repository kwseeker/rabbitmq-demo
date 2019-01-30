package top.kwseeker.mydemo.message;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import top.kwseeker.mydemo.Constant.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 为消息添加自定义属性
 */
public class MyProducer {

    private static final String EXCHANGE_NAME = "exchange_self_prop";
    private static final String QUEUE_NAME = "queue_self_prop";

    public static void main(String[] args) throws Exception {
        //1 创建一个ConnectionFactory
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        connectionFactory.setPort(5672);
        connectionFactory.setVirtualHost("/");
        try (//2 创建连接
            Connection connection = connectionFactory.newConnection();
            //3 创建Channel
            Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, Constants.ROUTING_KEY_TEST_DIRECT);
            /**
             * 为消息属性添加headers信息
             * 并设置消息属性（传输模式，编码方式，过期时间，header消息）
             */
            Map<String, Object> headers = new HashMap<>();
            headers.put("my1", "110");
            headers.put("my2", "119");
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .deliveryMode(2)        //持久化
                    .contentEncoding("UTF-8")
                    .expiration("10000")    //超时10s未消费会被自动剔除
                    .headers(headers)
                    .build();

            //4 通过Channel发送数据
            for (int i = 0; i < 5; i++) {
                String msg = "Hello RabbitMQ " + i;
                channel.basicPublish(EXCHANGE_NAME, Constants.ROUTING_KEY_TEST_DIRECT, properties, msg.getBytes());
            }
        }
    }
}
