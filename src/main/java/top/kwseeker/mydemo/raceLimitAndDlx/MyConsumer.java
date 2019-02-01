package top.kwseeker.mydemo.raceLimitAndDlx;

import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Qos 工作原理
 * 限制发送给Consumer但还未收到Ack的消息的数量
 */
@Slf4j
public class MyConsumer {

    public static void main(String[] args) throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        //经过此channel给每个consumer一次最多发送1条消息(即直到收到Ack后才继续发送)，消息内容大小不限
        channel.basicQos(0,     //消息的内容大小，0为不限制
                1,              //Broker限制发给Consumer的消息条数，0为不限制
                false);             //true if the settings should be applied to the entire channel rather than each consumer
                                            //这句怎么理解：true：整个channel，每次发送 prefetchCount 条信息， false：channel给单个consumer每次发送prefetchCount 条信息

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                log.info("Received Message: {}", new String(body));
            }
        };
        //channel.basicConsume("mydemo_queue_qos", consumer);   //默认是不会Ack的，要么设置autoAck，要么basicAck()
        //对于队列中没有收到Ack的消息，添加到死信队列，并对死信进行单独处理
        channel.basicConsume("mydemo_queue_qos", true, consumer);
    }
}
