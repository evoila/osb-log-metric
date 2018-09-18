package de.evoila.cf.broker.connection;

import de.evoila.cf.broker.bean.CFClientBean;
import de.evoila.cf.broker.model.LogMetricEnvironment;
import org.cloudfoundry.client.v2.applications.SummaryApplicationRequest;
import org.cloudfoundry.client.v2.applications.SummaryApplicationResponse;
import org.cloudfoundry.client.v2.organizations.GetOrganizationRequest;
import org.cloudfoundry.client.v2.organizations.GetOrganizationResponse;
import org.cloudfoundry.client.v2.spaces.GetSpaceRequest;
import org.cloudfoundry.client.v2.spaces.GetSpaceResponse;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.springframework.stereotype.Service;

/**
 * Created by reneschollmeyer, evoila on 30.05.18.
 */
@Service
public class CFClientConnector {

    private ReactorCloudFoundryClient cfClient;

    private CFClientBean cfClientBean;

    public CFClientConnector(CFClientBean cfClientBean) {
        this.cfClientBean = cfClientBean;
        cfClient = client();
    }

    private DefaultConnectionContext connectionContext() {
        return DefaultConnectionContext.builder()
                .apiHost(cfClientBean.getApiHost())
                .build();
    }

    private PasswordGrantTokenProvider tokenProvider() {
        return PasswordGrantTokenProvider.builder()
                .password(cfClientBean.getPassword())
                .username(cfClientBean.getUsername())
                .build();
    }

    private ReactorCloudFoundryClient client() {
        return ReactorCloudFoundryClient.builder()
                .connectionContext(connectionContext())
                .tokenProvider(tokenProvider())
                .build();
    }

    public LogMetricEnvironment getServiceEnvironment(String appId) {

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

        return new LogMetricEnvironment(applicationResponse.getName(), appId, spaceResponse.getEntity().getName(),
                organizationResponse.getEntity().getName(), spaceResponse.getEntity().getOrganizationId());
    }
}
