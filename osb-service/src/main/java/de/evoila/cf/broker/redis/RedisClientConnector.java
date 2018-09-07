package de.evoila.cf.broker.redis;

import de.evoila.cf.broker.bean.RedisBean;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by reneschollmeyer, evoila on 06.09.18.
 */
@Service
@ConditionalOnBean(RedisBean.class)
public class RedisClientConnector {

    private static String isCluster = "cluster_enabled:1";

    private Jedis jedis;

    private JedisCluster jedisCluster;

    @Autowired
    private RedisBean redisBean;

    private Jedis redisSingleNodeConnection() {
        Jedis jedis = new Jedis(redisBean.getHosts().get(0), redisBean.getPort());
        jedis.connect();
        jedis.auth(redisBean.getPassword());

        return jedis;
    }

    private JedisCluster redisClusterConnection() {

        Set<HostAndPort> jedisClusterNodes = new HashSet<>();

        for(String host : redisBean.getHosts()) {
            jedisClusterNodes.add(new HostAndPort(host, redisBean.getPort()));
        }

        GenericObjectPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(20);
        poolConfig.setMaxIdle(3);
        poolConfig.setMaxWaitMillis(3000);
        jedisCluster = new JedisCluster(jedisClusterNodes, 100, 3, 100, redisBean.getPassword(), poolConfig);

        return jedisCluster;
    }

    public void set(String key, String value) {
        jedis = redisSingleNodeConnection();

        if(jedis.info("Cluster").contains(isCluster)) {
            jedis.close();
            clusterSet(key, value);
        } else {
            jedis.set(key, value);
            jedis.close();
        }
    }

    private void clusterSet(String key, String value) {
        jedisCluster = redisClusterConnection();
        jedisCluster.set(key, value);

        try {
            jedisCluster.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String get(String key) {
        jedis = redisSingleNodeConnection();

        if(jedis.info("Cluster").contains(isCluster)) {
            jedis.close();
            return clusterGet(key);
        } else {
            String value = jedis.get(key);
            jedis.close();
            return value;
        }
    }

    private String clusterGet(String key) {
        jedisCluster = redisClusterConnection();
        String value = jedisCluster.get(key);

        try {
            jedisCluster.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return value;
    }

    public void del(String key) {
        jedis = redisSingleNodeConnection();

        if(jedis.info("Cluster").contains(isCluster)) {
            jedis.close();
            clusterDel(key);
        } else {
            jedis.del(key);
            jedis.close();
        }
    }

    private void clusterDel(String key) {
        jedisCluster = redisClusterConnection();
        jedisCluster.del(key);

        try {
            jedisCluster.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
