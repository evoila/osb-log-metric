package de.evoila.cf.broker.dashboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.evoila.cf.broker.bean.CfEndpointConfiguration;
import de.evoila.cf.broker.bean.EndpointConfiguration;
import de.evoila.cf.broker.cloudfoundry.UaaTokenRetriever;
import de.evoila.cf.broker.exception.ServiceBrokerException;
import de.evoila.cf.broker.exception.ServiceInstanceBindingException;
import de.evoila.cf.broker.model.AppData;
import de.evoila.cf.broker.bean.DashboardBackendPropertyBean;
import de.evoila.cf.broker.exception.DashboardBackendRequestException;
import de.evoila.cf.broker.exception.InvalidAppDataException;
import de.evoila.cf.broker.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Date;

@Service
@ConditionalOnBean(DashboardBackendPropertyBean.class)
public class DashboardBackendService {

    private final Logger log = LoggerFactory.getLogger(DashboardBackendService.class);

    private DashboardBackendPropertyBean authenticationProperties;
    private CfEndpointConfiguration cfEndpointConfiguration;
    private UaaTokenRetriever uaaTokenRetriever;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    public DashboardBackendService(DashboardBackendPropertyBean authenticationProperties, UaaTokenRetriever uaaTokenRetriever, CfEndpointConfiguration cfEndpointConfiguration) {
        this.authenticationProperties = authenticationProperties;
        this.cfEndpointConfiguration = cfEndpointConfiguration;
        this.uaaTokenRetriever = uaaTokenRetriever;
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
    }

    public void createBinding(String appId, String bindingId, ServiceInstanceBindingRequest serviceInstanceBindingRequest, ServiceInstance serviceInstance) {
        final String uriDashboardBackend = authenticationProperties.getHost() + ":" + authenticationProperties.getPort() + "/manage/serviceinstance/:instanceId/bindings/:bindingId"
                .replace(":instanceId", serviceInstance.getId())
                .replace(":bindingId", bindingId);

        final String uriCloudFoundry = cfEndpointConfiguration.getDefault() + ("/v3/apps/:guid?include=space.organization"
                .replace(":guid", appId));

        HttpEntity<String> httpEntity = new HttpEntity<>(getHeadersBearer(uaaTokenRetriever.getoAuthToken()));

        ResponseEntity<String> cloudFoundryResponse = restTemplate.exchange(
                uriCloudFoundry,
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        String responseBody = cloudFoundryResponse.getBody();
        AppData appDataObj = new AppData();
        appDataObj.setBindingId(bindingId);
        appDataObj.setInstanceId(serviceInstance.getId());

        try {

            JsonNode appData = objectMapper.readTree(responseBody);
            JsonNode appDataSpaces = appData.at("/included/spaces");
            JsonNode appDataOrganizations = appData.at("/included/organizations");

            if (appData == null || appData.isNull() || appDataSpaces == null || appDataSpaces.isNull() || appDataOrganizations == null || appDataOrganizations.isNull()) {
                throw new InvalidAppDataException();
            }

            appDataObj.setAppId(appData.get("guid").asText());
            appDataObj.setAppName(appData.get("name").asText());

            if (!appDataSpaces.isArray() || !appDataOrganizations.isArray()) {
                throw new InvalidAppDataException();
            }

            JsonNode appDataSpacesSpace = appDataSpaces.iterator().next();
            JsonNode appDataOrganizationsOrganization = appDataOrganizations.iterator().next();

            if (appDataSpacesSpace == null || appDataSpacesSpace.isNull() || appDataOrganizationsOrganization == null || appDataOrganizationsOrganization.isNull()) {
                throw new InvalidAppDataException();
            }

            appDataObj.setSpace(appDataSpacesSpace.get("name").asText());
            appDataObj.setOrganization(appDataOrganizationsOrganization.get("name").asText());
            appDataObj.setOrganizationGuid(appDataOrganizationsOrganization.get("guid").asText());

            ResponseEntity<String> dashboardBackendResponse = restTemplate.exchange(
                    uriDashboardBackend,
                    HttpMethod.POST,
                    new HttpEntity<>(appDataObj, getHeadersBasicAuth()),
                    String.class
            );

            if (!dashboardBackendResponse.getStatusCode().is2xxSuccessful()) {
                throw new DashboardBackendRequestException("DashboardBackendRequestException: Error while requesting resource from a Dashboard Backend Endpoint.",
                        dashboardBackendResponse.getStatusCode(), new Date().getTime());
            }

        } catch (Exception e) {
            log.error("Could not deserialize AppData from json", e);
        }

    }

    public void deleteBinding(ServiceInstanceBinding binding, ServiceInstance serviceInstance) {
        final String uri = authenticationProperties.getHost() + ":" + authenticationProperties.getPort() + "/manage/serviceinstance/:instanceId/bindings/:bindingId"
                .replace(":instanceId", serviceInstance.getId())
                .replace(":bindingId", binding.getId());

        ResponseEntity<String> exchange = restTemplate.exchange(
                uri,
                HttpMethod.DELETE,
                new HttpEntity<>(getHeadersBasicAuth()),
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
                new HttpEntity<>(getHeadersBasicAuth()),
                String.class
        );

        if (!exchange.getStatusCode().is2xxSuccessful()) {
            throw new DashboardBackendRequestException("DashboardBackendRequestException: Error while requesting resource from a Dashboard Backend Endpoint.",
                    exchange.getStatusCode(), new Date().getTime());
        }

    }

    private HttpHeaders getHeadersBasicAuth() {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.setBasicAuth(authenticationProperties.getUsername(), authenticationProperties.getPassword());
        return header;
    }

    private HttpHeaders getHeadersBearer(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setBearerAuth(token);
        return httpHeaders;
    }

}
