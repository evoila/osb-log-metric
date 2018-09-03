package de.evoila.cf.broker.custom;

import de.evoila.cf.broker.bean.RedisBean;
import de.evoila.cf.broker.connection.CFClientConnector;
import de.evoila.cf.broker.exception.ServiceBrokerException;
import de.evoila.cf.broker.model.*;
import de.evoila.cf.broker.service.impl.BindingServiceImpl;
import groovy.json.JsonBuilder;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by reneschollmeyer, evoila on 26.04.18.
 */
@Service
@ConditionalOnBean(RedisBean.class)
public class LogMetricBindingService extends BindingServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(LogMetricBindingService.class);

    private JedisCluster jedisCluster;

    private JsonBuilder jsonBuilder;

    @Autowired
    private RedisBean redisBean;

    @Autowired
    private CFClientConnector cfClient;

    private JedisCluster redisConnection() {

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

    @Override
    protected RouteBinding bindRoute(ServiceInstance serviceInstance, String route) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServiceInstanceBinding getServiceInstanceBinding(String id) { return null; }

    @Override
    protected ServiceInstanceBinding bindService(String bindingId, ServiceInstanceBindingRequest serviceInstanceBindingRequest,
                                                 ServiceInstance serviceInstance, Plan plan) {

        JedisCluster jedisCluster = redisConnection();

        if(jedisCluster.get(serviceInstanceBindingRequest.getAppGuid()) != null) {
            String redisJson = new JsonBuilder(new LogMetricRedisObject(cfClient.getServiceEnvironment(serviceInstanceBindingRequest.getAppGuid()), true)).toString();

            jedisCluster.set(serviceInstanceBindingRequest.getAppGuid(), redisJson);

            log.info("Binding successful, serviceInstance = " + serviceInstance.getId() +
            ", bindingId = " + bindingId);
        } else {
            log.error("Error updating the subscription status for app = " + serviceInstanceBindingRequest.getAppGuid()
                    + ". Application is not registered.");
        }

        try {
            jedisCluster.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ServiceInstanceBinding serviceInstanceBinding = new ServiceInstanceBinding(bindingId, serviceInstance.getId(), null, null);
        serviceInstanceBinding.setAppGuid(serviceInstanceBindingRequest.getAppGuid());
        return serviceInstanceBinding;
    }

    @Override
    protected void unbindService(ServiceInstanceBinding binding, ServiceInstance serviceInstance, Plan plan) throws ServiceBrokerException {
        JedisCluster jedisCluster = redisConnection();

        if(jedisCluster.get(binding.getAppGuid()) != null) {
            jedisCluster.del(binding.getAppGuid());

            log.info("Unbinding successful, serviceInstance = " + serviceInstance.getId() +
                    ", bindingId = " + binding.getId());
        } else {
            log.error("Error updating the subscription status for app = " + binding.getAppGuid()
                    + ". Application is not registered.");
        }

        try {
            jedisCluster.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Map<String, Object> createCredentials(String bindingId, ServiceInstanceBindingRequest serviceInstanceBindingRequest, ServiceInstance serviceInstance, Plan plan, ServerAddress serverAddress) throws ServiceBrokerException {
        return new HashMap<>();
    }










}
