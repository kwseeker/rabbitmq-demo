package top.kwseeker.officialdemo.loggingsystem;

import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * 启动两个线程，
 */
public class ReceiveLogs {

    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] argv) throws Exception {
        //创建Connection和Channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //声明交换机和队列，并将其绑定，队列就可以接收交换机的消息了
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        String queueName = channel.queueDeclare().getQueue();               //create a non-durable, exclusive, autodelete queue with a generated name
        channel.queueBind(queueName, EXCHANGE_NAME, "");        //将新建的队列绑定到交换机

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
            }
        };
        channel.basicConsume(queueName, true, consumer);
    }
}
