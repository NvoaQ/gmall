package com.chendong.gmall.payment;

import com.chendong.gmall.util.ActiveMQUtil;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GmallPaymentApplicationTests {
	@Autowired
	ActiveMQUtil activeMQUtil;

	@Test
	public void producer() throws JMSException {
		//ActiveMQ的连接为tcp协议
		ConnectionFactory mqFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
		Connection connection = mqFactory.createConnection();
		connection.start();
		//开启事务，必须commit消息才提交
		Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
		//创建消息队列
		Queue queue = session.createQueue("TestQueue");
		MessageProducer producer = session.createProducer(queue);
		//传递的内容
		ActiveMQTextMessage message = new ActiveMQTextMessage();
		message.setText("这是mq的测试！");
		//消息持久化
		producer.setDeliveryMode(DeliveryMode.PERSISTENT);
		//传递消息
		producer.send(message);
		session.commit();
		session.close();
		connection.close();


	}
	@Test
	public void consumer() throws JMSException {

		ConnectionFactory mqFactory = new ActiveMQConnectionFactory(ActiveMQConnectionFactory.DEFAULT_USER,
				ActiveMQConnectionFactory.DEFAULT_PASSWORD,"tcp://localhost:61616");
		Connection connection = mqFactory.createConnection();
		connection.start();
		//没有开启事务
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = session.createQueue("TestQueue");
		MessageConsumer consumer = session.createConsumer(queue);
		//创建消费者的消息监听器
		consumer.setMessageListener((message)->{
			if(message instanceof TextMessage){
				try {
					String text = ((TextMessage) message).getText();
					System.out.println(text);
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		});


	}
	@Test
	public void contextTest() throws JMSException {
		Connection connection = null;
		Session session = null;
		try {
			connection = activeMQUtil.getConnectionFactory().createConnection();
			connection.start();
			session = connection.createSession(true, Session.SESSION_TRANSACTED);
			Queue queue = session.createQueue("testtesttest");
			ActiveMQTextMessage message = new ActiveMQTextMessage();
			message.setText("hello activemq");

			MessageProducer producer = session.createProducer(queue);
			producer.setDeliveryMode(DeliveryMode.PERSISTENT);
			producer.send(message);
			session.commit();
		}catch (Exception ex){
			ex.printStackTrace();
		}finally {
			session.close();
			connection.close();
		}


	}

}
