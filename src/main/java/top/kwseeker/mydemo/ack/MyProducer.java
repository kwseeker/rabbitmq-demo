package top.kwseeker.mydemo.ack;

import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import top.kwseeker.mydemo.Constant.Constants;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试手动发送确认消息
 * Channel.basicPublish()接口的 autoAck 为 true 则自动发送确认消息，为 false 则手动发送确认消息；
 * 发送确认消息：
 *  ACK（收到消息确认信息）：
 *
 *  NACK（未收到消息确认信息）：
 */
@Slf4j
public class MyProducer {

    public static void main(String[] args) throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();

        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        channel.confirmSelect();

        channel.queueDeclare(Constants.QUEUE_TEST41_NAME, false, false, false, null);
        channel.queueBind(Constants.QUEUE_TEST41_NAME, Constants.EXCHANGE_TOPIC_NAME, Constants.ROUTING_KEY_TEST_TOPIC1);

        //监听Broker返回的确认消息（指消息从Producer发送到Broker，并不一定写入到队列，也不是被消费的确认消息）
        //收到Ack说明channel连接是通的
        channel.addConfirmListener(new ConfirmListener() {
            @Override
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                log.info("Ack ...");
            }

            @Override
            public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                log.info("Nack ...");
            }
        });

        //监听被退回的消息(指从Producer发送消息到Broker未收到ACK被返回给Producer)
        channel.addReturnListener(new ReturnListener() {
            @Override
            public void handleReturn(int replyCode, String replyText, String exchange, String routingKey,
                                     AMQP.BasicProperties properties, byte[] body) throws IOException {
                log.info("Returned Message:");
                log.info("replyCode: " + replyCode);
                log.info("replyText: " + replyText);
                log.info("exchange: " + exchange);
                log.info("routingKey: " + routingKey);
                log.info("properties: " + properties);
                log.info("body: " + new String(body));
            }
        });

        for (int i = 0; i < 5; i++) {
            Map<String, Object> headers = new HashMap<>();
            headers.put("num", i);
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .deliveryMode(2)
                    .contentEncoding("UTF-8")
                    .expiration("10000")
                    .headers(headers)
                    .build();

            String msg = "Message for test manual ack " + i;
            channel.basicPublish(Constants.EXCHANGE_TOPIC_NAME, Constants.ROUTING_KEY_TEST_TOPIC1,
                    true,       //mandatory 没有收到确认消息时ACK是否返回给Producer
                    properties, msg.getBytes());
        }
    }
}
