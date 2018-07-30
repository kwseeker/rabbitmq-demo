package top.kwseeker.officialdemo.rpc;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class RPCClient implements AutoCloseable {

    private Connection connection;
    private Channel channel;
    private String requestQueueName = "rpc_queue";
    private String replyQueueName;  //返回消息的队列的名称自动生成

    //创建connection和channel，以及声明返回消息queue
    public RPCClient() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        channel = connection.createChannel();

        replyQueueName = channel.queueDeclare().getQueue();
    }

    //通过channel发布消息给请求queue, 并监听返回消息的queue
    public String call(String message) throws IOException, InterruptedException {
        final String corrId = UUID.randomUUID().toString();

        AMQP.BasicProperties props = new AMQP.BasicProperties   //这个参数用于传输控制
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)   //消息返回给哪个队列
                .build();

        channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));

        //String类型的阻塞队列用于存储接收到的返回消息
        final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);

        channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                if (properties.getCorrelationId().equals(corrId)) {
                    response.offer(new String(body, "UTF-8"));  //将返回消息插入BlockingQueue
                }
            }
        });

        return response.take(); //从BlockingQueue取出一个消息
    }

    public void close() throws IOException {
        connection.close();
    }

    public static void main(String[] argv) {
        try (RPCClient fibonacciRpc = new RPCClient()) {
            System.out.println(" [x] Requesting fib(30)");

            final String response = fibonacciRpc.call("30");
            System.out.println(" [.] Got '" + response + "'");

        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
