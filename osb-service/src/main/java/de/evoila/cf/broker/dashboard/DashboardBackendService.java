package de.evoila.cf.broker.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.evoila.cf.broker.bean.CloudFoundryPropertiesBean;
import de.evoila.cf.broker.bean.EndpointConfiguration;
import de.evoila.cf.broker.cloudfoundry.UaaTokenRetriever;
import de.evoila.cf.broker.dashboard.request.DashboardBackendBindingRequest;
import de.evoila.cf.broker.bean.DashboardBackendPropertyBean;
import de.evoila.cf.broker.exception.DashboardBackendRequestException;
import de.evoila.cf.broker.exception.InvalidRedisObjectException;
import de.evoila.cf.broker.model.*;
import de.evoila.cf.broker.redis.RedisClientConnector;
import de.evoila.cf.security.uaa.provider.UaaRelyingPartyAuthenticationProvider;
import de.evoila.cf.security.uaa.token.UaaRelyingPartyToken;
import de.evoila.cf.security.uaa.utils.UaaFilterUtils;
import de.evoila.config.web.UaaSecurityConfiguration;
import org.apache.kafka.common.protocol.types.Field;
import org.cloudfoundry.client.v3.organizations.Organization;
import org.cloudfoundry.uaa.UaaClient;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@ConditionalOnBean(DashboardBackendPropertyBean.class)
public class DashboardBackendService {

    private final Logger log = LoggerFactory.getLogger(DashboardBackendService.class);

    private DashboardBackendPropertyBean authenticationProperties;
    private CloudFoundryPropertiesBean cloudFoundryPropertiesBean;
    private UaaTokenRetriever uaaTokenRetriever;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    public DashboardBackendService(DashboardBackendPropertyBean authenticationProperties, CloudFoundryPropertiesBean cloudFoundryPropertiesBean, UaaTokenRetriever uaaTokenRetriever) {
        this.authenticationProperties = authenticationProperties;
        this.cloudFoundryPropertiesBean = cloudFoundryPropertiesBean;
        this.uaaTokenRetriever = uaaTokenRetriever;
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
    }

    public void createBinding(String bindingId, ServiceInstanceBindingRequest serviceInstanceBindingRequest, ServiceInstance serviceInstance) {
        final String uriDashboardBackend = authenticationProperties.getHost() + ":" + authenticationProperties.getPort() + "/manage/serviceinstance/:instanceId/bindings/:bindingId"
                .replace(":instanceId", serviceInstance.getId())
                .replace(":bindingId", bindingId);

        try {

            final String uriCloudFoundry = cloudFoundryPropertiesBean.getHost() + "/v3/apps/:guid?include=space.organization"
                    .replace(":gui", serviceInstanceBindingRequest.getBindResource().getAppGuid());

            HttpEntity<String> httpEntity = new HttpEntity<>(getHeaders(uaaTokenRetriever.getoAuthToken()));

            ResponseEntity<HashMap<String, Object>> cloudFoundryResponse = restTemplate.exchange(
                    uriCloudFoundry,
                    HttpMethod.GET,
                    httpEntity,
                    new ParameterizedTypeReference<>() {
                    }
            );

            HashMap<String, Object> responseBody = cloudFoundryResponse.getBody();
            String appId, appName, organization, space, organizationGuid;

            appId = (String) responseBody.get("guid");
            appName = (String) responseBody.get("name");

            HashMap<String, Object> included = (HashMap<String, Object>) responseBody.get("included");

            HashMap<String, Object> spaces = (HashMap<String, Object>) included.get("spaces");
            space = (String) spaces.get("name");

            HashMap<String, Object> organizations = (HashMap<String, Object>) responseBody.get("organizations");
            organizationGuid = (String) organizations.get("guid");
            organization = (String) organizations.get("name");

            DashboardBackendBindingRequest dashboardBackendBindingRequest = new DashboardBackendBindingRequest(bindingId, serviceInstance.getId(), appId, appName, organization, space, organizationGuid);

            httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(dashboardBackendBindingRequest), getHeaders());

            ResponseEntity<String> dashboardBackendResponse = restTemplate.exchange(
                    uriDashboardBackend,
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );

            if (!dashboardBackendResponse.getStatusCode().is2xxSuccessful()) {
                throw new DashboardBackendRequestException("DashboardBackendRequestException: Error while requesting resource from a Dashboard Backend Endpoint.",
                        dashboardBackendResponse.getStatusCode(), new Date().getTime());
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

    private HttpHeaders getHeaders(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setBearerAuth(token);
        return httpHeaders;
    }

}
