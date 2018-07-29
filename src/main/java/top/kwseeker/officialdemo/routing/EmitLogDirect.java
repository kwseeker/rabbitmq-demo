package top.kwseeker.officialdemo.routing;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Arrays;

/**
 * ReceiveLogsDirect启动后启动此应用
 * 传参，第一个参数默认为info, 只有一个队列可以收到，传error 两个队列都可以收到
 */
public class EmitLogDirect {

    private static final String EXCHANGE_NAME = "direct_logs";

    public static void main(String[] argv) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);         //声明名为direct_logs类型为direct的交换机

            String severity = getSeverity(argv);
            String message = getMessage(argv);

            channel.basicPublish(EXCHANGE_NAME, severity, null, message.getBytes("UTF-8")); //发布routingKey为severity的消息
            System.out.println(" [x] Sent '" + severity + "':'" + message + "'");
        }
    }

    private static String getSeverity(String[] strings) {   //默认为info级别
        if (strings.length < 1)
            return "info";
        return strings[0];
    }

    private static String getMessage(String[] strings) {    //默认内容为Hello World
        if (strings.length < 2)
            return "Hello World!";
        return String.join(" ",Arrays.copyOfRange(strings, 1, strings.length));
    }
}
