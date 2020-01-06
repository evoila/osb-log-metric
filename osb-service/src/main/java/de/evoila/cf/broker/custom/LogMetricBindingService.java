package de.evoila.cf.broker.custom;

import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean;
import de.evoila.cf.autoscaler.kafka.model.BindingInformation;
import de.evoila.cf.autoscaler.kafka.producer.KafkaJsonProducer;
import de.evoila.cf.broker.cloudfoundry.CfUtils;
import de.evoila.cf.broker.dashboard.DashboardBackendService;
import de.evoila.cf.broker.exception.ServiceBrokerException;
import de.evoila.cf.broker.model.*;
import de.evoila.cf.broker.model.catalog.Catalog;
import de.evoila.cf.broker.model.catalog.ServerAddress;
import de.evoila.cf.broker.model.catalog.plan.Plan;
import de.evoila.cf.broker.repository.*;
import de.evoila.cf.broker.service.AsyncBindingService;
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
@ConditionalOnBean(KafkaPropertiesBean.class)
public class LogMetricBindingService extends BindingServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(LogMetricBindingService.class);

    private static final String BIND_ACTION = "bind";

    private static final String UNBIND_ACTION = "unbind";

    private static final String SOURCE = "logMetric";

    private Catalog catalog;

    private ServiceInstanceRepository serviceInstanceRepository;

    private BindingRepository bindingRepository;

    private KafkaJsonProducer kafkaJsonProducer;

    private KafkaPropertiesBean kafkaPropertiesBean;

    private DashboardBackendService dashboardBackendService;

    private CfUtils cfUtils;

    public LogMetricBindingService(Catalog catalog, ServiceInstanceRepository serviceInstanceRepository, BindingRepository bindingRepository,
                                   ServiceDefinitionRepository serviceDefinitionRepository, RouteBindingRepository routeBindingRepository,
                                   KafkaJsonProducer kafkaJsonProducer, KafkaPropertiesBean kafkaPropertiesBean,
                                   JobRepository jobRepository, AsyncBindingService asyncBindingService, PlatformRepository platformRepository, DashboardBackendService dashboardBackendService, CfUtils cfUtils) {
        super(bindingRepository, serviceDefinitionRepository, serviceInstanceRepository, routeBindingRepository, jobRepository, asyncBindingService, platformRepository);
        this.catalog = catalog;
        this.serviceInstanceRepository = serviceInstanceRepository;
        this.bindingRepository = bindingRepository;
        this.kafkaJsonProducer = kafkaJsonProducer;
        this.kafkaPropertiesBean = kafkaPropertiesBean;
        this.dashboardBackendService = dashboardBackendService;
        this.cfUtils = cfUtils;

        syncBindings();
    }


    @Override
    protected RouteBinding bindRoute(ServiceInstance serviceInstance, String route) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ServiceInstanceBinding bindService(String bindingId, ServiceInstanceBindingRequest serviceInstanceBindingRequest,
                                                 ServiceInstance serviceInstance, Plan plan) throws ServiceBrokerException {

        final String appId = serviceInstanceBindingRequest.getBindResource().getAppGuid();

        AppData appData = cfUtils.createAppData(appId, bindingId, serviceInstance);

        // Kafka or Dashboard must be reverted in the future if one of both fails!!!
        BindingInformation logMetricBinding = new BindingInformation(appId, BIND_ACTION, SOURCE);
        kafkaJsonProducer.produce(kafkaPropertiesBean.getBindingTopic(), logMetricBinding);
        dashboardBackendService.createBinding(bindingId, serviceInstance, appData);

        log.info("Binding successful, serviceInstance = " + serviceInstance.getId() +
                ", bindingId = " + bindingId);

        ServiceInstanceBinding serviceInstanceBinding = new ServiceInstanceBinding(bindingId, serviceInstance.getId(), new HashMap<>());
        serviceInstanceBinding.setAppGuid(appId);
        return serviceInstanceBinding;
    }

    @Override
    protected void unbindService(ServiceInstanceBinding binding, ServiceInstance serviceInstance, Plan plan) throws ServiceBrokerException {

        BindingInformation logMetricBinding = new BindingInformation(binding.getAppGuid(), UNBIND_ACTION, SOURCE);

        // Kafka or Dashboard must be reverted in the future if one of both fails!!!
        kafkaJsonProducer.produce(kafkaPropertiesBean.getBindingTopic(), logMetricBinding);
        dashboardBackendService.deleteBinding(binding, serviceInstance);

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
                    BindingInformation logMetricBinding = new BindingInformation(binding.getAppGuid(), BIND_ACTION, SOURCE);
                    kafkaJsonProducer.produce(kafkaPropertiesBean.getBindingTopic(), logMetricBinding);
                });
            });
        });
    }
}
