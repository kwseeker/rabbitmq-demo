package top.kwseeker.mydemo.raceLimitAndDlx;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;

public class MyProducer {

    public static void main(String[] args) throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        //死信队列（用于接收 mydemo_queue_qos 中的死信）
        channel.exchangeDeclare("mydemo_exchange_dlx", "topic", false, false, null);
        channel.queueDeclare("mydemo_queue_dlx", false, false, false, null);
        channel.queueBind("mydemo_queue_dlx", "mydemo_exchange_dlx", "#");  //接收所有routingKey消息

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "mydemo_exchange_dlx");
        channel.exchangeDeclare("mydemo_exchange_qos", "topic", false, false, null);
        channel.queueDeclare("mydemo_queue_qos", false, false, false, arguments);
        channel.queueBind("mydemo_queue_qos", "mydemo_exchange_qos", "mydemo.test.qos");

        //发送一条带超时时间的消息
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                .deliveryMode(2)
                .expiration("10000")    //为何不起作用，超时后没有自动删除并转发到DLX ？因为Consumer端设置了Qos 一次一条阻塞了，需要把这条信息放在最前面发送
                .build();
        channel.basicPublish("mydemo_exchange_qos", "mydemo.test.qos", true, properties,
                "Message for test DLX".getBytes());

        for (int i = 0; i < 5; i++) {
            channel.basicPublish("mydemo_exchange_qos", "mydemo.test.qos", true, null,
                    "Message for test Qos".getBytes());
        }
    }
}
