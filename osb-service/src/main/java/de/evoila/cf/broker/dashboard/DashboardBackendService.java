package de.evoila.cf.broker.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.evoila.cf.broker.dashboard.request.DashboardBackendBindingRequest;
import de.evoila.cf.broker.bean.DashboardBackendPropertyBean;
import de.evoila.cf.broker.exception.DashboardBackendRequestException;
import de.evoila.cf.broker.exception.InvalidRedisObjectException;
import de.evoila.cf.broker.model.*;
import de.evoila.cf.broker.redis.RedisClientConnector;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Date;

@Service
@ConditionalOnBean(DashboardBackendPropertyBean.class)
public class DashboardBackendService {

    private final Logger log = LoggerFactory.getLogger(DashboardBackendService.class);

    private DashboardBackendPropertyBean authenticationProperties;
    private RedisClientConnector redisClient;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    public DashboardBackendService(DashboardBackendPropertyBean authenticationProperties, RedisClientConnector redisClient) {
        this.authenticationProperties = authenticationProperties;
        this.redisClient = redisClient;
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
    }

    public void createBinding(String bindingId, ServiceInstanceBindingRequest serviceInstanceBindingRequest, ServiceInstance serviceInstance) {
        final String uri = authenticationProperties.getHost() + ":" + authenticationProperties.getPort() + "/manage/serviceinstance/:instanceId/bindings/:bindingId"
                .replace(":instanceId", serviceInstance.getId())
                .replace(":bindingId", bindingId);

        try {
            final String appId = serviceInstanceBindingRequest.getBindResource().getAppGuid();
            LogMetricRedisObject logMetricRedisObject = objectMapper.readValue(redisClient.get(appId), LogMetricRedisObject.class);

            if (logMetricRedisObject.getApplicationName() == null || logMetricRedisObject.getSpace() == null ||
                    logMetricRedisObject.getOrganization() == null || logMetricRedisObject.getOrganization_guid() == null)
                throw new InvalidRedisObjectException();

            DashboardBackendBindingRequest dashboardBackendBindingRequest = new DashboardBackendBindingRequest(bindingId, serviceInstance.getId(), appId, logMetricRedisObject);

            final HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(dashboardBackendBindingRequest), getHeaders());

            ResponseEntity<String> exchange = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );

            if (!exchange.getStatusCode().is2xxSuccessful()) {
                throw new DashboardBackendRequestException("DashboardBackendRequestException: Error while requesting resource from a Dashboard Backend Endpoint.",
                        exchange.getStatusCode(), new Date().getTime());
            }

        } catch (IOException e) {
            log.error("Could not deserialize LogMetricRedisObject from json", e);
        }

    }

    public void deleteBinding(ServiceInstanceBinding binding, ServiceInstance serviceInstance) {
        final String uri = authenticationProperties.getHost() + ":" + authenticationProperties.getPort() + "/manage/serviceinstance/:instanceId/bindings/:bindingId"
                .replace(":instanceId", serviceInstance.getId())
                .replace(":bindingId", binding.getId());

        ResponseEntity<String> exchange = restTemplate.exchange(
                uri,
                HttpMethod.DELETE,
                new HttpEntity<>(getHeaders()),
                String.class
        );

        if (!exchange.getStatusCode().is2xxSuccessful()) {
            throw new DashboardBackendRequestException("DashboardBackendRequestException: Error while requesting resource from a Dashboard Backend Endpoint.",
                    exchange.getStatusCode(), new Date().getTime());
        }

    }

    public void deleteServiceInstance(ServiceInstance serviceInstance) {
        final String uri = authenticationProperties.getHost() + ":" + authenticationProperties.getPort() + "/manage/serviceinstance/:instanceId"
                .replace(":instanceId", serviceInstance.getId());

        ResponseEntity<String> exchange = restTemplate.exchange(
                uri,
                HttpMethod.DELETE,
                new HttpEntity<>(getHeaders()),
                String.class
        );

        if (!exchange.getStatusCode().is2xxSuccessful()) {
            throw new DashboardBackendRequestException("DashboardBackendRequestException: Error while requesting resource from a Dashboard Backend Endpoint.",
                    exchange.getStatusCode(), new Date().getTime());
        }

    }

    private HttpHeaders getHeaders() {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.setBasicAuth(authenticationProperties.getUsername(), authenticationProperties.getPassword());
        return header;
    }

}
