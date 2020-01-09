package cn.e3mall.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;

import javax.jms.*;

public class ActiveMqTest {

    /**
     * 点到点形式发送消息
     */
    @Test
    public void testQueueProducer() throws JMSException {
        //1. 创建一个连接工厂𤙸，需要指定服务的IP及端口
        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://192.168.222.131:61616");
        //2. 使用工厂对象创建一个Connection对象
        Connection connection = factory.createConnection();
        //3. 开启连接，调用Connection对象的start方法
        connection.start();
        //4. 创建一个Session会话对象
        //第一个参数：是否开启事务。true:开启事务，第二个参数忽略。
        //第二个参数：当第一个参数衣false时，才有意义。消息的应答模式。1、自动应答 2、手动应答。一般是自动应答
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //5. 使用Session对象创建一个目的地 Destination 对象，两种形式queue\toppic
        Queue queue = session.createQueue("test-queue");
        //6. 使用Session对象创建一个生产者 Producer对象
        MessageProducer producer = session.createProducer(queue);
        //7. 创建一个Message对象，可以使用TextMessage
        //TextMessage textMessage = new ActiveMQTextMessage();
        //textMessage.setText("hello ActiveMq");
        TextMessage textMessage = session.createTextMessage("hello activemq");
        //8. 发送消息
        producer.send(textMessage);
        //9. 关闭资源
        producer.close();
        session.close();
        connection.close();
    }

    /**
     * 点到点接收消息
     *
     * @throws Exception
     */
    @Test
    public void testQueueConsumer() throws Exception {
        //1. 创建一个连接工厂𤙸，需要指定服务的IP及端口
        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://192.168.222.131:61616");
        //2. 使用工厂对象创建一个Connection对象
        Connection connection = factory.createConnection();
        //3. 开启连接，调用Connection对象的start方法
        connection.start();
        //4. 创建一个Session会话对象
        //第一个参数：是否开启事务。true:开启事务，第二个参数忽略。
        //第二个参数：当第一个参数衣false时，才有意义。消息的应答模式。1、自动应答 2、手动应答。一般是自动应答
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //5. 使用Session对象创建一个目的地 Destination 对象，两种形式queue\toppic
        Queue queue = session.createQueue("spring-queue");
        //6. 使用Sesison对象创建一个Consumer对象
        MessageConsumer consumer = session.createConsumer(queue);
        //7. 接收消息
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                TextMessage textMessage = (TextMessage) message;
                String text = null;
                try {
                    //取消息内容
                    text = textMessage.getText();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
                //8. 打印消息
                System.out.println(text);
            }
        });
        //等待键盘输入
        System.in.read();
        //9. 关闭资源
        consumer.close();
        session.close();
        connection.close();
    }

    /**
     * 发布订阅消息
     *
     * @throws Exception
     */
    @Test
    public void testTopicProducer() throws Exception {
        //1. 创建一个连接工厂𤙸，需要指定服务的IP及端口
        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://192.168.222.131:61616");
        //2. 使用工厂对象创建一个Connection对象
        Connection connection = factory.createConnection();
        //3. 开启连接，调用Connection对象的start方法
        connection.start();
        //4. 创建一个Session会话对象
        //第一个参数：是否开启事务。true:开启事务，第二个参数忽略。
        //第二个参数：当第一个参数衣false时，才有意义。消息的应答模式。1、自动应答 2、手动应答。一般是自动应答
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //5. 使用Session对象创建一个目的地 Destination 对象，两种形式queue\toppic
        Topic topic = session.createTopic("test-topic");
        //6. 使用Session对象创建一个生产者 Producer对象
        MessageProducer producer = session.createProducer(topic);
        //7. 创建一个Message对象，可以使用TextMessage
        //TextMessage textMessage = new ActiveMQTextMessage();
        //textMessage.setText("hello ActiveMq");
        TextMessage textMessage = session.createTextMessage("hello topic activemq");
        //8. 发送消息
        producer.send(textMessage);
        //9. 关闭资源
        producer.close();
        session.close();
        connection.close();
    }

    /**
     * 接收订阅消息
     *
     * @throws Exception
     */
    @Test
    public void testTopicConsumer() throws Exception {
        //1. 创建一个连接工厂𤙸，需要指定服务的IP及端口
        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://192.168.222.131:61616");
        //2. 使用工厂对象创建一个Connection对象
        Connection connection = factory.createConnection();
        //3. 开启连接，调用Connection对象的start方法
        connection.start();
        //4. 创建一个Session会话对象
        //第一个参数：是否开启事务。true:开启事务，第二个参数忽略。
        //第二个参数：当第一个参数衣false时，才有意义。消息的应答模式。1、自动应答 2、手动应答。一般是自动应答
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //5. 使用Session对象创建一个目的地 Destination 对象，两种形式queue\toppic
        Topic topic = session.createTopic("test-topic");
        //6. 使用Sesison对象创建一个Consumer对象
        MessageConsumer consumer = session.createConsumer(topic);
        //7. 接收消息
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                TextMessage textMessage = (TextMessage) message;
                String text = null;
                try {
                    //取消息内容
                    text = textMessage.getText();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
                //8. 打印消息
                System.out.println(text);
            }
        });
        System.out.println("topic消费端03");
        //等待键盘输入
        System.in.read();

        //9. 关闭资源
        consumer.close();
        session.close();
        connection.close();
    }
}
