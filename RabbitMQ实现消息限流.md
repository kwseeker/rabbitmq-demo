# RabbitMQ实现消息限流

### 从队列取消息限流

RabbitMQ提供了一种QOS(服务质量保证)功能，即在非自动确认消息（consume接口中，autoAck=false）的前提下，如果一定数据的消息
（通过基于consume或者channel设置QOS值）未被确认前，不进行消费新的消息。

```
void BasicQos(uint prefechSize, 
              ushort prefetchCount,     //设置预取数量
              bool global);
```

### 消息入队列限流

