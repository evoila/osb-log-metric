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

import javax.annotation.PostConstruct;
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

    @PostConstruct
    private void init() {
        jedis = new Jedis(redisBean.getHost(), redisBean.getPort());
        jedis.connect();
        jedis.auth(redisBean.getPassword());
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
        if(!jedis.get(serviceInstanceBindingRequest.getAppGuid()).isEmpty()) {
            jedis.set(serviceInstanceBindingRequest.getAppGuid(), "true");

            log.info("Binding successful, serviceInstance = " + serviceInstance.getId() +
            ", bindingId = " + bindingId);
        } else {
            log.error("Error updating the subscription status for app = " + serviceInstanceBindingRequest.getAppGuid()
                    + ". Application is not registered.");
        }

        ServiceInstanceBinding serviceInstanceBinding = new ServiceInstanceBinding(bindingId, serviceInstance.getId(), null, null);
        serviceInstanceBinding.setAppGuid(serviceInstanceBindingRequest.getAppGuid());
        return serviceInstanceBinding;
    }

    @Override
    protected void deleteBinding(ServiceInstanceBinding binding, ServiceInstance serviceInstance, Plan plan) throws ServiceBrokerException {
        if(!jedis.get(binding.getAppGuid()).isEmpty()) {
            jedis.set(binding.getAppGuid(), "false");

            log.info("Unbinding successful, serviceInstance = " + serviceInstance.getId() +
                    ", bindingId = " + binding.getId());
        } else {
            log.error("Error updating the subscription status for app = " + binding.getAppGuid()
                    + ". Application is not registered.");
        }
    }

    @Override
    protected Map<String, Object> createCredentials(String bindingId, ServiceInstanceBindingRequest serviceInstanceBindingRequest, ServiceInstance serviceInstance, Plan plan, ServerAddress serverAddress) throws ServiceBrokerException {
        return new HashMap<>();
    }










}
