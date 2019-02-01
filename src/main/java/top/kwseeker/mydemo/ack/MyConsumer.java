package top.kwseeker.mydemo.ack;

import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import top.kwseeker.mydemo.Constant.Constants;

import java.io.IOException;
import java.util.Date;

@Slf4j
public class MyConsumer {

    public static void main(String[] args) throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();

        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                log.info("Current time: {}, Current Thread: {}", new Date(System.currentTimeMillis()), Thread.currentThread());
                log.info("Message Received: {}", new String(body));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //消费端ACK与重回队列（说的是Broker到Consumer部分）
                if((Integer) properties.getHeaders().get("num") == 0) {
                    //手动发送Nack
                    channel.basicNack(envelope.getDeliveryTag(),
                            false,      //
                            true);      //重新入队列，实际使用中很少使用
                } else {
                    //手动发送Ack
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };

        channel.basicConsume(Constants.QUEUE_TEST41_NAME, false, consumer);
        channel.basicConsume(Constants.QUEUE_TEST43_NAME, false, consumer);

        /*
        有个有意思的现象，第一条信息发送没有收到返回确认消息，就换了另一个线程接收其他消息，正常的话就一直用这个线程；
        一旦没有接收到返回确认就换一个线程处理剩余的消息；
        [pool-1-thread-4] INFO top.kwseeker.mydemo.ack.MyConsumer - Current time: Thu Jan 31 18:09:11 CST 2019, Current Thread: Thread[pool-1-thread-4,5,main]
        [pool-1-thread-4] INFO top.kwseeker.mydemo.ack.MyConsumer - Message Received: Message for test manual ack0
        [pool-1-thread-5] INFO top.kwseeker.mydemo.ack.MyConsumer - Current time: Thu Jan 31 18:09:13 CST 2019, Current Thread: Thread[pool-1-thread-5,5,main]
        [pool-1-thread-5] INFO top.kwseeker.mydemo.ack.MyConsumer - Message Received: Message for test manual ack1
        [pool-1-thread-5] INFO top.kwseeker.mydemo.ack.MyConsumer - Current time: Thu Jan 31 18:09:15 CST 2019, Current Thread: Thread[pool-1-thread-5,5,main]
        [pool-1-thread-5] INFO top.kwseeker.mydemo.ack.MyConsumer - Message Received: Message for test manual ack2
        [pool-1-thread-5] INFO top.kwseeker.mydemo.ack.MyConsumer - Current time: Thu Jan 31 18:09:17 CST 2019, Current Thread: Thread[pool-1-thread-5,5,main]
        [pool-1-thread-5] INFO top.kwseeker.mydemo.ack.MyConsumer - Message Received: Message for test manual ack3
        [pool-1-thread-5] INFO top.kwseeker.mydemo.ack.MyConsumer - Current time: Thu Jan 31 18:09:19 CST 2019, Current Thread: Thread[pool-1-thread-5,5,main]
        [pool-1-thread-5] INFO top.kwseeker.mydemo.ack.MyConsumer - Message Received: Message for test manual ack4
        [pool-1-thread-6] INFO top.kwseeker.mydemo.ack.MyConsumer - Current time: Thu Jan 31 18:09:21 CST 2019, Current Thread: Thread[pool-1-thread-6,5,main]
        [pool-1-thread-6] INFO top.kwseeker.mydemo.ack.MyConsumer - Message Received: Message for test manual ack0
        [pool-1-thread-7] INFO top.kwseeker.mydemo.ack.MyConsumer - Current time: Thu Jan 31 18:09:23 CST 2019, Current Thread: Thread[pool-1-thread-7,5,main]
        [pool-1-thread-7] INFO top.kwseeker.mydemo.ack.MyConsumer - Message Received: Message for test manual ack0
        [pool-1-thread-8] INFO top.kwseeker.mydemo.ack.MyConsumer - Current time: Thu Jan 31 18:09:25 CST 2019, Current Thread: Thread[pool-1-thread-8,5,main]
        [pool-1-thread-8] INFO top.kwseeker.mydemo.ack.MyConsumer - Message Received: Message for test manual ack0
        [pool-1-thread-9] INFO top.kwseeker.mydemo.ack.MyConsumer - Current time: Thu Jan 31 18:09:27 CST 2019, Current Thread: Thread[pool-1-thread-9,5,main]
        [pool-1-thread-9] INFO top.kwseeker.mydemo.ack.MyConsumer - Message Received: Message for test manual ack0
        [pool-1-thread-10] INFO top.kwseeker.mydemo.ack.MyConsumer - Current time: Thu Jan 31 18:09:29 CST 2019, Current Thread: Thread[pool-1-thread-10,5,main]
        [pool-1-thread-10] INFO top.kwseeker.mydemo.ack.MyConsumer - Message Received: Message for test manual ack0
        * */
    }
}
