package de.evoila.cf.broker.custom;

import de.evoila.cf.autoscaler.kafka.model.LogMetricBinding;
import de.evoila.cf.autoscaler.kafka.producer.JsonProducer;
import de.evoila.cf.broker.bean.RedisBean;
import de.evoila.cf.broker.exception.ServiceBrokerException;
import de.evoila.cf.broker.model.*;
import de.evoila.cf.broker.repository.BindingRepository;
import de.evoila.cf.broker.repository.RouteBindingRepository;
import de.evoila.cf.broker.repository.ServiceDefinitionRepository;
import de.evoila.cf.broker.repository.ServiceInstanceRepository;
import de.evoila.cf.broker.service.HAProxyService;
import de.evoila.cf.broker.service.impl.BindingServiceImpl;
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

    private static final String BINDING_TOPIC = "bindings";

    private static final String BIND_ACTION = "bind";

    private static final String UNBIND_ACTION = "unbind";

    private Catalog catalog;

    private ServiceInstanceRepository serviceInstanceRepository;

    private BindingRepository bindingRepository;

    private JsonProducer jsonProducer;

    public LogMetricBindingService(Catalog catalog, ServiceInstanceRepository serviceInstanceRepository, BindingRepository bindingRepository,
                                   ServiceDefinitionRepository serviceDefinitionRepository, RouteBindingRepository routeBindingRepository,
                                   HAProxyService haProxyService, JsonProducer jsonProducer) {
        super(bindingRepository, serviceDefinitionRepository, serviceInstanceRepository, routeBindingRepository, haProxyService);
        this.catalog = catalog;
        this.serviceInstanceRepository = serviceInstanceRepository;
        this.bindingRepository = bindingRepository;
        this.jsonProducer = jsonProducer;
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

        LogMetricBinding logMetricBinding = new LogMetricBinding(serviceInstanceBindingRequest.getAppGuid(), BIND_ACTION);

        jsonProducer.produceKafkaMessage(BINDING_TOPIC, logMetricBinding);

        log.info("Binding successful, serviceInstance = " + serviceInstance.getId() +
                ", bindingId = " + bindingId);

        ServiceInstanceBinding serviceInstanceBinding = new ServiceInstanceBinding(bindingId, serviceInstance.getId(), null, null);
        serviceInstanceBinding.setAppGuid(serviceInstanceBindingRequest.getAppGuid());
        return serviceInstanceBinding;
    }

    @Override
    protected void unbindService(ServiceInstanceBinding binding, ServiceInstance serviceInstance, Plan plan) throws ServiceBrokerException {

        LogMetricBinding logMetricBinding = new LogMetricBinding(binding.getAppGuid(), UNBIND_ACTION);

        jsonProducer.produceKafkaMessage(BINDING_TOPIC, logMetricBinding);

        log.info("Unbinding successful, serviceInstance = " + serviceInstance.getId() +
                ", bindingId = " + binding.getId());
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
                    LogMetricBinding logMetricBinding = new LogMetricBinding(binding.getAppGuid(), BIND_ACTION);
                    jsonProducer.produceKafkaMessage(BINDING_TOPIC, logMetricBinding);
                });
            });
        });
    }
}