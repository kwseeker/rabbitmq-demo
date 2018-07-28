package top.kwseeker.officialdemo.helloworld;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Send {

    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");                               //"localhost"表示连接本机的broker, 连接其他机器可以指定IP,如： "192.168.1.100"
        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {     //try块退出时，()内部的资源会自动释放, 相当于做了 channel.close();connection.close();

            AMQP.Queue.DeclareOk declareOk = channel.queueDeclare(QUEUE_NAME, false, false, false, null);    //声明队列[是幂等的]（包括名称、服务重启队列是否保留、是否是创建专属队列、是否自动删除，队列构造参数）
            System.out.println(declareOk.getQueue());

            String message = "Hello World!";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));  //发布消息(消息生产者和队列之间还可指定交换机，消息传给哪个队列，消息其他的一些属性（如：路由头部等等）)
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}
