package de.evoila.cf.broker.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.evoila.cf.broker.backend.request.BindingRequest;
import de.evoila.cf.broker.bean.BasicAuthenticationPropertyBean;
import de.evoila.cf.broker.model.*;
import de.evoila.cf.broker.redis.RedisClientConnector;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class BackendEndpointService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private BasicAuthenticationPropertyBean authenticationProperties;

    @Autowired
    private RedisClientConnector redisClient;

    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();

    public void createBinding(String bindingId, ServiceInstanceBindingRequest serviceInstanceBindingRequest, ServiceInstance serviceInstance) {
        final String uri = authenticationProperties.host + ":" + authenticationProperties.port + "/serviceinstance/:instanceId/bindings/:bindingId"
                .replace(":instanceId", serviceInstance.getId())
                .replace(":bindingId", bindingId);

        try {
            final String appId = serviceInstanceBindingRequest.getAppGuid();
            LogMetricRedisObject logMetricRedisObject = objectMapper.readValue(redisClient.get(appId), LogMetricRedisObject.class);

            BindingRequest bindingRequest = new BindingRequest(bindingId, serviceInstance.getId(), appId, logMetricRedisObject);

            final HttpEntity<String> httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(bindingRequest), getHeaders());

            ResponseEntity responseEntity = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    httpEntity,
                    ResponseEntity.class
            );

            Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        } catch (IOException e) {
            log.error("Could not deserialize LogMetricRedisObject from json", e);
        }

    }

    /*
    public void createMultipleBindings(ServiceInstance serviceInstance) {
        final String uri = authenticationProperties.host + ":" + authenticationProperties.port + "/serviceinstance/:instanceId/bindings"
                .replace(":instanceId", serviceInstance.getId());

        final HttpEntity<String> httpEntity = new HttpEntity<>(getHeaders());
    }
     */

    public void deleteBinding(ServiceInstanceBinding binding, ServiceInstance serviceInstance) {
        final String uri = authenticationProperties.host + ":" + authenticationProperties.port + "/serviceinstance/:instanceId/bindings/:bindingId"
                .replace(":instanceId", serviceInstance.getId())
                .replace(":bindingId", binding.getId());

        final HttpEntity<String> httpEntity = new HttpEntity<>(getHeaders());

        ResponseEntity responseEntity = restTemplate.exchange(
                uri,
                HttpMethod.DELETE,
                httpEntity,
                ResponseEntity.class
        );

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    public void deleteServiceInstance(ServiceInstance serviceInstance) {
        final String uri = authenticationProperties.host + ":" + authenticationProperties.port + "/serviceinstance/:instanceId"
                .replace(":instanceId", serviceInstance.getId());

        final HttpEntity<String> httpEntity = new HttpEntity<>(getHeaders());

        ResponseEntity responseEntity = restTemplate.exchange(
                uri,
                HttpMethod.DELETE,
                httpEntity,
                ResponseEntity.class
        );

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    private HttpHeaders getHeaders() {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.setBasicAuth(authenticationProperties.username, authenticationProperties.password);
        return header;
    }

}
