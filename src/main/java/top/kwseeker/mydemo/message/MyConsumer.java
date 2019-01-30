package top.kwseeker.mydemo.message;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MyConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MyConsumer.class);

    private static final String EXCHANGE_NAME = "exchange_self_prop";
    private static final String QUEUE_NAME = "queue_self_prop";

    public static void main(String[] args) throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                logger.info("来源：Exchange:{}, RoutingKey:{}", envelope.getExchange(), envelope.getRoutingKey());
                logger.info("   Properties:{}, Message: {}",properties.getHeaders().toString(), new String(body));
            }
        };
        channel.basicConsume(QUEUE_NAME, true, consumer);
    }
}

