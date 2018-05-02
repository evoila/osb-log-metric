package de.evoila.cf.broker.custom;

import de.evoila.cf.broker.bean.RedisBean;
import de.evoila.cf.broker.exception.ServiceBrokerException;
import de.evoila.cf.broker.model.*;
import de.evoila.cf.broker.service.impl.BindingServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by reneschollmeyer, evoila on 26.04.18.
 */
@Service
@ConditionalOnBean(RedisBean.class)
public class LogMetricBindingService extends BindingServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(LogMetricBindingService.class);

    private Jedis jedis;

    @Autowired
    private RedisBean redisBean;

    private Jedis redisConnection() {
        jedis = new Jedis(redisBean.getHost(), redisBean.getPort());
        jedis.connect();
        jedis.auth(redisBean.getPassword());

        return jedis;
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

        Jedis jedis = redisConnection();

        if(jedis.get(serviceInstanceBindingRequest.getAppGuid()) != null) {
            jedis.set(serviceInstanceBindingRequest.getAppGuid(), "true");

            log.info("Binding successful, serviceInstance = " + serviceInstance.getId() +
            ", bindingId = " + bindingId);
        } else {
            log.error("Error updating the subscription status for app = " + serviceInstanceBindingRequest.getAppGuid()
                    + ". Application is not registered.");
        }

        jedis.close();

        ServiceInstanceBinding serviceInstanceBinding = new ServiceInstanceBinding(bindingId, serviceInstance.getId(), null, null);
        serviceInstanceBinding.setAppGuid(serviceInstanceBindingRequest.getAppGuid());
        return serviceInstanceBinding;
    }

    @Override
    protected void deleteBinding(ServiceInstanceBinding binding, ServiceInstance serviceInstance, Plan plan) throws ServiceBrokerException {
        Jedis jedis = redisConnection();

        if(jedis.get(binding.getAppGuid()) != null) {
            jedis.del(binding.getAppGuid());

            log.info("Unbinding successful, serviceInstance = " + serviceInstance.getId() +
                    ", bindingId = " + binding.getId());
        } else {
            log.error("Error updating the subscription status for app = " + binding.getAppGuid()
                    + ". Application is not registered.");
        }

        jedis.close();
    }

    @Override
    protected Map<String, Object> createCredentials(String bindingId, ServiceInstanceBindingRequest serviceInstanceBindingRequest, ServiceInstance serviceInstance, Plan plan, ServerAddress serverAddress) throws ServiceBrokerException {
        return new HashMap<>();
    }










}
