package top.kwseeker.officialdemo.loggingsystem;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class EmitLog {

    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT); //声明FANOUT类型交换机

            String message = getMessage(argv);

            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));   //发布消息给所有队列
            System.out.println(" [x] Sent '" + message + "'");
        }
    }

    private static String getMessage(String[] strings) {
        if (strings.length < 1)
            return "info: Hello World!";
        return String.join(" ", strings);
    }
}
