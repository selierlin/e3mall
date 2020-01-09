package cn.e3mall.jedis;

import org.junit.Test;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

import java.util.HashSet;
import java.util.Set;

public class JedisTest {
    /**
     * 连接单个Redis
     */
    @Test
    public void testJedis() {
        //创建一个连接Jedis对象，参数：host、port
        Jedis jedis = new Jedis("192.168.222.131", 6379);
        //直接使用jedis操作redis，所有jedis的命令都对应一个方法
        jedis.set("test123", "my first jedis test");
        String string = jedis.get("test123");
        System.out.println(string);
        //关闭连接
        jedis.close();
    }

    /**
     * 使用连接池连接单个Redis
     */
    @Test
    public void testJedisPool() {
        //创建一个连接池对象，两个参数host、port
        JedisPool jedisPool = new JedisPool("192.168.222.131", 6379);
        //从连接池获得一个连接，也就是一个jedis对象
        Jedis jedis = jedisPool.getResource();
        //使用jedis操作redis
        String string = jedis.get("test123");
        System.out.println(string);
        //关闭连接。每次使用完毕后关闭连接，连接池回收资源。
        jedis.close();
        //关闭连接池。连接池是单例的
        jedisPool.close();
    }

    /**
     * 连接Redis集群
     */
    @Test
    public void testJedisCluster() {
        //创建一个JedisCluster对象，有一个参数nodes是一个set类型，set中包含包含若干个HostAndPort对象
        Set<HostAndPort> nodes = new HashSet<>();
        nodes.add(new HostAndPort("192.168.222.131", 7001));
        nodes.add(new HostAndPort("192.168.222.131", 7002));
        nodes.add(new HostAndPort("192.168.222.131", 7003));
        nodes.add(new HostAndPort("192.168.222.131", 7004));
        nodes.add(new HostAndPort("192.168.222.131", 7005));
        nodes.add(new HostAndPort("192.168.222.131", 7006));
        JedisCluster jedisCluster = new JedisCluster(nodes);
        //直接使用JedisCluster对象操作redis
        jedisCluster.set("testCluster", "123");
        String string = jedisCluster.get("testCluster");
        System.out.println(string);
        //关闭jedisCluster对象
        jedisCluster.close();
    }
}
