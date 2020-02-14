package de.evoila.cf.broker.dashboard;

import de.evoila.cf.broker.bean.DashboardBackendPropertyBean;
import de.evoila.cf.broker.bean.DashboardBackendResponseErrorHandler;
import de.evoila.cf.broker.model.AppData;
import de.evoila.cf.broker.model.ServiceInstance;
import de.evoila.cf.broker.model.ServiceInstanceBinding;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@ConditionalOnBean(DashboardBackendPropertyBean.class)
public class DashboardBackendService {

    private final RestTemplate restTemplate;
    private DashboardBackendPropertyBean authenticationProperties;

    public DashboardBackendService(DashboardBackendPropertyBean authenticationProperties, RestTemplateBuilder templateBuilder) {
        this.authenticationProperties = authenticationProperties;
        //restTemplate =
          //      templateBuilder.errorHandler(new DashboardBackendResponseErrorHandler()).build();
        this.restTemplate = new RestTemplate();
    }

    public void createBinding(String bindingId, ServiceInstance serviceInstance, AppData appDataObj) {
        final String uriDashboardBackend = authenticationProperties.getHost() + ":" + authenticationProperties.getPort() + "/manage/serviceinstance/:instanceId/bindings/:bindingId"
                .replace(":instanceId", serviceInstance.getId())
                .replace(":bindingId", bindingId);

        restTemplate.exchange(
                uriDashboardBackend,
                HttpMethod.POST,
                new HttpEntity<>(appDataObj, getHeadersBasicAuth(authenticationProperties.getUsername(), authenticationProperties.getPassword())),
                String.class
        );

    }

    public void deleteBinding(ServiceInstanceBinding binding, ServiceInstance serviceInstance) {
        final String uri = authenticationProperties.getHost() + ":" + authenticationProperties.getPort() + "/manage/serviceinstance/:instanceId/bindings/:bindingId"
                .replace(":instanceId", serviceInstance.getId())
                .replace(":bindingId", binding.getId());

        requestDeleteOf(uri);
    }

    public void deleteServiceInstance(ServiceInstance serviceInstance) {
        final String uri = authenticationProperties.getHost() + ":" + authenticationProperties.getPort() + "/manage/serviceinstance/:instanceId"
                .replace(":instanceId", serviceInstance.getId());

        requestDeleteOf(uri);
    }

    private ResponseEntity<String> requestDeleteOf(String uri) {
        return restTemplate.exchange(
                uri,
                HttpMethod.DELETE,
                new HttpEntity<>(getHeadersBasicAuth(authenticationProperties.getUsername(), authenticationProperties.getPassword())),
                String.class
        );

    }

    public HttpHeaders getHeadersBasicAuth(String username, String password) {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.setBasicAuth(username, password);
        return header;
    }

}
