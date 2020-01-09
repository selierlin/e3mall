package cn.e3mall.cart.publish;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class CartPublish {
    @Test
    public void publishServer() throws IOException {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-*.xml");
        //while (true) {
        //    Thread.sleep(1000);
        //}
        System.out.println("服务已经启动");
        System.in.read();
        System.out.println("服务已经关闭");
    }
}
