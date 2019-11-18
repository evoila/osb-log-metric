package de.evoila.cf.broker.custom;

import de.evoila.cf.broker.backend.BackendEndpointService;
import de.evoila.cf.broker.bean.ExistingEndpointBean;
import de.evoila.cf.broker.exception.PlatformException;
import de.evoila.cf.broker.model.ServiceInstance;
import de.evoila.cf.broker.model.catalog.plan.Plan;
import de.evoila.cf.broker.repository.PlatformRepository;
import de.evoila.cf.broker.service.availability.ServicePortAvailabilityVerifier;
import de.evoila.cf.cpi.existing.ExistingServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by reneschollmeyer, evoila on 26.04.18.
 */
@Service
@EnableConfigurationProperties
public class LogMetricPlatformService extends ExistingServiceFactory {

    private final Logger log = LoggerFactory.getLogger(LogMetricPlatformService.class);

    @Autowired
    private BackendEndpointService backendEndpointService;

    public LogMetricPlatformService(PlatformRepository platformRepository, ServicePortAvailabilityVerifier portAvailabilityVerifier, ExistingEndpointBean existingEndpointBean) {
        super(platformRepository, portAvailabilityVerifier, existingEndpointBean);
    }

    @Override
    public ServiceInstance createInstance(ServiceInstance serviceInstance, Plan plan, Map<String, Object> customParameters) throws PlatformException {
        serviceInstance = new ServiceInstance(serviceInstance, serviceInstance.getDashboardUrl(), serviceInstance.getId());

        return serviceInstance;
    }

    @Override
    public ServiceInstance postCreateInstance(ServiceInstance serviceInstance, Plan plan) throws PlatformException {
        return serviceInstance;
    }

    @Override
    public void deleteInstance(ServiceInstance serviceInstance, Plan plan) throws PlatformException {
        backendEndpointService.deleteServiceInstance(serviceInstance);
    }

    @Override
    public ServiceInstance getInstance(ServiceInstance serviceInstance, Plan plan) throws PlatformException {
        return null;
    }
}