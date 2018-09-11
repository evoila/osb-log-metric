package de.evoila.cf.broker.custom;

import de.evoila.cf.broker.bean.RedisBean;
import de.evoila.cf.broker.connection.CFClientConnector;
import de.evoila.cf.broker.exception.ServiceBrokerException;
import de.evoila.cf.broker.model.*;
import de.evoila.cf.broker.redis.RedisClientConnector;
import de.evoila.cf.broker.repository.BindingRepository;
import de.evoila.cf.broker.repository.ServiceInstanceRepository;
import de.evoila.cf.broker.service.impl.BindingServiceImpl;
import groovy.json.JsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by reneschollmeyer, evoila on 26.04.18.
 */
@Service
@ConditionalOnBean(RedisBean.class)
public class LogMetricBindingService extends BindingServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(LogMetricBindingService.class);

    private CFClientConnector cfClient;

    private RedisClientConnector redisClient;

    private Catalog catalog;

    private ServiceInstanceRepository serviceInstanceRepository;

    private BindingRepository bindingRepository;

    public LogMetricBindingService(CFClientConnector cfClient, RedisClientConnector redisClient, Catalog catalog,
                                   ServiceInstanceRepository serviceInstanceRepository, BindingRepository bindingRepository) {
        this.cfClient = cfClient;
        this.redisClient = redisClient;
        this.catalog = catalog;
        this.serviceInstanceRepository = serviceInstanceRepository;
        this.bindingRepository = bindingRepository;
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

        if(redisClient.get(serviceInstanceBindingRequest.getAppGuid()) != null) {
            String redisJson = new JsonBuilder(new LogMetricRedisObject(cfClient.getServiceEnvironment(serviceInstanceBindingRequest.getAppGuid()), true)).toString();

            redisClient.set(serviceInstanceBindingRequest.getAppGuid(), redisJson);

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
    protected void unbindService(ServiceInstanceBinding binding, ServiceInstance serviceInstance, Plan plan) throws ServiceBrokerException {

        if(redisClient.get(binding.getAppGuid()) != null) {
            redisClient.del(binding.getAppGuid());

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

    public void syncBindings() {
        log.info("Synchronizing Service Bindings with Redis.");

        catalog.getServices().forEach(serviceDefinition -> {
            serviceInstanceRepository.getServiceInstancesByServiceDefinitionId(serviceDefinition.getId()).forEach(serviceInstance -> {
                bindingRepository.getBindingsForServiceInstance(serviceInstance.getId()).forEach(binding -> {
                    log.info("Found binding with bindingId = " + binding.getId() + ", synchronizing with Redis...");
                    String redisJson = new JsonBuilder(new LogMetricRedisObject(cfClient.getServiceEnvironment(binding.getAppGuid()), true)).toString();
                    redisClient.set(binding.getAppGuid(), redisJson);
                });
            });
        });
    }
}