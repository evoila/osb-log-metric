package de.evoila.cf.broker.dashboard;

import de.evoila.cf.broker.bean.DashboardBackendPropertyBean;
import de.evoila.cf.broker.exception.DashboardBackendRequestException;
import de.evoila.cf.broker.model.AppData;
import de.evoila.cf.broker.model.ServiceInstance;
import de.evoila.cf.broker.model.ServiceInstanceBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

@Service
@ConditionalOnBean(DashboardBackendPropertyBean.class)
public class DashboardBackendService {

    private final RestTemplate restTemplate;
    private DashboardBackendPropertyBean authenticationProperties;

    public DashboardBackendService(DashboardBackendPropertyBean authenticationProperties, RestTemplateBuilder templateBuilder) {
        this.authenticationProperties = authenticationProperties;
        restTemplate = templateBuilder.errorHandler(new DashboardBackendResponseErrorHandler()).build();
    }

    public void createBinding(String bindingId, ServiceInstance serviceInstance, AppData appDataObj) {
        final String uriDashboardBackend = authenticationProperties.getHost() + ":" + authenticationProperties.getPort() + "/manage/serviceinstance/:instanceId/bindings/:bindingId"
                .replace(":instanceId", serviceInstance.getId())
                .replace(":bindingId", bindingId);

        ResponseEntity<String> dashboardBackendResponse = restTemplate.exchange(
                uriDashboardBackend,
                HttpMethod.POST,
                new HttpEntity<>(appDataObj, getHeadersBasicAuth(authenticationProperties.getUsername(), authenticationProperties.getPassword())),
                String.class
        );

        if (!dashboardBackendResponse.getStatusCode().is2xxSuccessful()) {
            throw new DashboardBackendRequestException("DashboardBackendRequestException: Error while requesting resource from a Dashboard Backend Endpoint.",
                    dashboardBackendResponse.getStatusCode(), new Date().getTime());
        }

    }

    public void deleteBinding(ServiceInstanceBinding binding, ServiceInstance serviceInstance) {
        final String uri = authenticationProperties.getHost() + ":" + authenticationProperties.getPort() + "/manage/serviceinstance/:instanceId/bindings/:bindingId"
                .replace(":instanceId", serviceInstance.getId())
                .replace(":bindingId", binding.getId());

        ResponseEntity<String> response = requestDeleteOf(uri);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new DashboardBackendRequestException("DashboardBackendRequestException: Error while requesting resource from a Dashboard Backend Endpoint.",
                    response.getStatusCode(), new Date().getTime());
        }

    }

    public void deleteServiceInstance(ServiceInstance serviceInstance) {
        final String uri = authenticationProperties.getHost() + ":" + authenticationProperties.getPort() + "/manage/serviceinstance/:instanceId"
                .replace(":instanceId", serviceInstance.getId());

        ResponseEntity<String> response = requestDeleteOf(uri);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new DashboardBackendRequestException("DashboardBackendRequestException: Error while requesting resource from a Dashboard Backend Endpoint.",
                    response.getStatusCode(), new Date().getTime());
        }

    }

    private ResponseEntity<String> requestDeleteOf(String uri) {
        ResponseEntity<String> exchange = restTemplate.exchange(
                uri,
                HttpMethod.DELETE,
                new HttpEntity<>(getHeadersBasicAuth(authenticationProperties.getUsername(), authenticationProperties.getPassword())),
                String.class
        );

        return exchange;
    }

    public HttpHeaders getHeadersBasicAuth(String username, String password) {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.setBasicAuth(username, password);
        return header;
    }

}
