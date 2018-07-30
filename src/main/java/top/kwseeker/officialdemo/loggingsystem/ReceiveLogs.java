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

        //TODO: 显示consumer所在的线程是相同的。NIO？具体怎么实现看DefaultConsumer的源码
        // DefaultConsumer有两个成员变量, 答案应该在 Channel 上
        //    private final Channel _channel;
        //    private volatile String _consumerTag;
        new Thread(() -> {
            MyDefaultConsumer consumer = new MyDefaultConsumer(channel);
            try {
                System.out.println("线程1中声明一个队列和一个消费者");
                String queueName1 = channel.queueDeclare().getQueue();               //create a non-durable, exclusive, autodelete queue with a generated name
                channel.queueBind(queueName1, EXCHANGE_NAME, "");
                channel.basicConsume(queueName1, true, consumer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        },"线程1"
        ).start();

        //一个线程只能定义一个consumer
        System.out.println("main线程中声明一个队列和一个消费者");
        MyDefaultConsumer consumer1 = new MyDefaultConsumer(channel);
        channel.basicConsume(queueName, true, consumer1);
    }
}

class MyDefaultConsumer extends DefaultConsumer {

    MyDefaultConsumer(Channel channel) {
        super(channel);         //DefaultConsumer中没有提供默认的无参构造函数，所以需要手动调用DefaultConsumer的构造函数
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope,
                               AMQP.BasicProperties properties, byte[] body) throws IOException {
        String message = new String(body, "UTF-8");
        System.out.println(" [x] Received '" + message + "'");
        System.out.println("Current thread: " + Thread.currentThread().getName());
    }
}
