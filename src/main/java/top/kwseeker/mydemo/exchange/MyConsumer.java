package top.kwseeker.mydemo.exchange;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.kwseeker.mydemo.Constant.Constants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MyConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MyConsumer.class);

    public static void main(String[] args) throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        //connectionFactory.setHost("localhost");
        connectionFactory.setPort(5672);
        //connectionFactory.setVirtualHost("/");

        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        /**
         * 默认 exchange + mydemo_queue_1
         */
//        channel.queueDeclare(Constants.QUEUE_TEST1_NAME, false, false, false, null);
        /**
         * 自定义 direct exchange + mydemo_queue_2
         */
//        channel.queueDeclare(Constants.QUEUE_TEST2_NAME, false,false, false, null);
//        channel.exchangeDeclare(Constants.EXCHANGE_DIRECT_NAME, "direct", false, false, null);
//        channel.queueBind(Constants.QUEUE_TEST2_NAME, Constants.EXCHANGE_DIRECT_NAME, Constants.ROUTING_KEY_TEST_DIRECT);
        /**
         * 自定义 fanout exchange + mydemo_queue_31/mydemo_queue_32
         */
//        channel.queueDeclare(Constants.QUEUE_TEST31_NAME, false, false, false, null);
//        channel.queueDeclare(Constants.QUEUE_TEST32_NAME, false, false, false, null);
//        channel.exchangeDeclare(Constants.EXCHANGE_FANOUT_NAME, "fanout", false, false, false, null);
//        channel.queueBind(Constants.QUEUE_TEST31_NAME, Constants.EXCHANGE_FANOUT_NAME, ""); //不用设置路由键但是也不能为null，设为空字符串
//        channel.queueBind(Constants.QUEUE_TEST32_NAME, Constants.EXCHANGE_FANOUT_NAME, "");
        /**
         * 自定义 topic exchange + mydemo_queue_41/mydemo_queue_42
         */
//        channel.queueDeclare(Constants.QUEUE_TEST41_NAME, false, false, false, null);
//        channel.queueDeclare(Constants.QUEUE_TEST42_NAME, false, false, false, null);
//        channel.queueBind(Constants.QUEUE_TEST41_NAME, Constants.EXCHANGE_TOPIC_NAME, Constants.ROUTING_KEY_TEST_TOPIC);
//        channel.queueBind(Constants.QUEUE_TEST42_NAME, Constants.EXCHANGE_TOPIC_NAME, Constants.ROUTING_KEY_TEST_TOPIC3);

        //1 声明一个消费者(新版本好像只有一个 MyConsumer 的实现类 DefaultConsumer)
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                logger.info("来源：Exchange: {}, RoutingKey: {} ", envelope.getExchange(), envelope.getRoutingKey());
                logger.info("  Message: {}", message);
            }
        };

        channel.basicConsume(Constants.QUEUE_TEST1_NAME, true, consumer);
        channel.basicConsume(Constants.QUEUE_TEST2_NAME, true, consumer);
        channel.basicConsume(Constants.QUEUE_TEST31_NAME, true, consumer);
        channel.basicConsume(Constants.QUEUE_TEST32_NAME, true, consumer);
        channel.basicConsume(Constants.QUEUE_TEST41_NAME, true, consumer);
        channel.basicConsume(Constants.QUEUE_TEST42_NAME, true, consumer);
        channel.basicConsume(Constants.QUEUE_TEST43_NAME, true, consumer);
        channel.basicConsume(Constants.QUEUE_TEST44_NAME, true, consumer);
    }
}
