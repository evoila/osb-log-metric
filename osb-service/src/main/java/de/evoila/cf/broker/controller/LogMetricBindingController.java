package de.evoila.cf.broker.controller;

import de.evoila.cf.broker.connection.CFClientConnector;
import de.evoila.cf.broker.exception.ServiceInstanceDoesNotExistException;
import de.evoila.cf.broker.model.LogMetricBindingObject;
import de.evoila.cf.broker.model.ServiceInstanceBinding;
import de.evoila.cf.broker.repository.BindingRepository;
import de.evoila.cf.broker.repository.ServiceInstanceRepository;
import org.cloudfoundry.client.v2.applications.SummaryApplicationRequest;
import org.cloudfoundry.client.v2.applications.SummaryApplicationResponse;
import org.cloudfoundry.client.v2.organizations.GetOrganizationRequest;
import org.cloudfoundry.client.v2.organizations.GetOrganizationResponse;
import org.cloudfoundry.client.v2.spaces.GetSpaceRequest;
import org.cloudfoundry.client.v2.spaces.GetSpaceResponse;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by reneschollmeyer, evoila on 17.05.18.
 */
@RestController
@RequestMapping(value = "/v2/service_instances")
public class LogMetricBindingController {

    private static final String mapKey = "service_bindings";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ReactorCloudFoundryClient cfClient;

    @Autowired
    private BindingRepository bindingRepository;

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository;

    @Autowired
    private CFClientConnector cfClientConnector;

    @PostConstruct
    private void init() {
        cfClient = cfClientConnector.client();
    }

    @GetMapping(value = "/{instanceId}/service_bindings")
    public ResponseEntity<Map<String, List<LogMetricBindingObject>>> getServiceBindings(@PathVariable("instanceId") String instanceId) throws ServiceInstanceDoesNotExistException {

        log.debug("GET: /v2/service_instances/{instanceId}/service_bindings"
                + ", getServiceBindings(), serviceInstanceId = " + instanceId);

        if(serviceInstanceRepository.containsServiceInstanceId(instanceId)) {
            Map<String, List<LogMetricBindingObject>> appData = new HashMap<>();

            appData.put(mapKey, new ArrayList<>());

            for(ServiceInstanceBinding serviceInstanceBinding: bindingRepository.getBindingsForServiceInstance(instanceId)) {
                appData.get(mapKey).add(getNameAndSpace(serviceInstanceBinding.getAppGuid()));
            }

            return new ResponseEntity<>(appData, HttpStatus.OK);
        } else {
            throw new ServiceInstanceDoesNotExistException(instanceId);
        }
    }

    private LogMetricBindingObject getNameAndSpace(String appId) {

        SummaryApplicationResponse applicationResponse = cfClient.applicationsV2()
            .summary(SummaryApplicationRequest.builder()
                .applicationId(appId)
                .build())
            .block();

        GetSpaceResponse spaceResponse = cfClient.spaces()
                .get(GetSpaceRequest.builder()
                    .spaceId(applicationResponse.getSpaceId())
                    .build())
                .block();

        GetOrganizationResponse organizationResponse = cfClient.organizations()
                .get(GetOrganizationRequest.builder()
                    .organizationId(spaceResponse.getEntity().getOrganizationId())
                    .build())
                .block();

        return new LogMetricBindingObject(applicationResponse.getName(), appId, spaceResponse.getEntity().getName(), organizationResponse.getEntity().getName());
    }
}