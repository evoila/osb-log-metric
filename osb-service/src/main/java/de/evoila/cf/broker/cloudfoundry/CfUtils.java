package de.evoila.cf.broker.cloudfoundry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.evoila.cf.broker.bean.CfEndpointConfiguration;
import de.evoila.cf.broker.exception.InvalidAppDataException;
import de.evoila.cf.broker.model.AppData;
import de.evoila.cf.broker.model.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Component
public class CfUtils {

    private final Logger log = LoggerFactory.getLogger(CfUtils.class);

    private final RestTemplate restTemplate;

    private CfEndpointConfiguration cfEndpointConfiguration;
    private UaaTokenRetriever uaaTokenRetriever;
    private ObjectMapper objectMapper;

    public CfUtils(CfEndpointConfiguration cfEndpointConfiguration, UaaTokenRetriever uaaTokenRetriever, ObjectMapper objectMapper, RestTemplateBuilder templateBuilder) {
        this.cfEndpointConfiguration = cfEndpointConfiguration;
        this.uaaTokenRetriever = uaaTokenRetriever;
        this.objectMapper = objectMapper;
        restTemplate = templateBuilder.errorHandler(new DefaultResponseErrorHandler()).build();
    }

    public AppData createAppData(String appId, String bindingId, ServiceInstance serviceInstance) {
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
            appDataObj.setSpaceId(appDataSpacesSpace.get("guid").asText());
            appDataObj.setOrganization(appDataOrganizationsOrganization.get("name").asText());
            appDataObj.setOrganizationGuid(appDataOrganizationsOrganization.get("guid").asText());

        } catch (Exception e) {
            log.error("Could not deserialize AppData from json", e);
        }

        return appDataObj;
    }

    public HttpHeaders getHeadersBearer(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setBearerAuth(token);
        return httpHeaders;
    }

}
