package de.evoila.cf.broker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.evoila.cf.broker.exception.ServiceInstanceDoesNotExistException;
import de.evoila.cf.broker.model.LogMetricEnvironment;
import de.evoila.cf.broker.model.LogMetricRedisObject;
import de.evoila.cf.broker.model.ServiceInstanceBinding;
import de.evoila.cf.broker.redis.RedisClientConnector;
import de.evoila.cf.broker.repository.BindingRepository;
import de.evoila.cf.broker.repository.ServiceInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by reneschollmeyer, evoila on 17.05.18.
 */
@RestController
@RequestMapping(value = "/custom/v2/manage")
public class LogMetricBindingController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private BindingRepository bindingRepository;

    private ServiceInstanceRepository serviceInstanceRepository;

    private RedisClientConnector redisClient;

    private ObjectMapper objectMapper;

    public LogMetricBindingController(BindingRepository bindingRepository, ServiceInstanceRepository serviceInstanceRepository, RedisClientConnector redisClient) {
        this.bindingRepository = bindingRepository;
        this.serviceInstanceRepository = serviceInstanceRepository;
        this.redisClient = redisClient;
        objectMapper = new ObjectMapper();
    }

    @GetMapping(value = "/{instanceId}/service_bindings")
    public ResponseEntity<List<LogMetricEnvironment>> getServiceBindings(@PathVariable("instanceId") String instanceId) throws ServiceInstanceDoesNotExistException {

        log.debug("GET: /v2/manage/{instanceId}/service_bindings"
                + ", getServiceBindings(), serviceInstanceId = " + instanceId);

        if(serviceInstanceRepository.containsServiceInstanceId(instanceId)) {
            List<LogMetricEnvironment> appData = new ArrayList<>();

            for(ServiceInstanceBinding serviceInstanceBinding: bindingRepository.getBindingsForServiceInstance(instanceId)) {
                try {
                    appData.add(new LogMetricEnvironment(serviceInstanceBinding.getAppGuid(), objectMapper.readValue(redisClient.get(serviceInstanceBinding.getAppGuid()), LogMetricRedisObject.class)));
                } catch (IOException e) {
                    log.error("Could not deserialize LogMetricRedisObject from json", e);
                }
            }

            return new ResponseEntity<>(appData, HttpStatus.OK);
        } else {
            throw new ServiceInstanceDoesNotExistException(instanceId);
        }
    }
}
