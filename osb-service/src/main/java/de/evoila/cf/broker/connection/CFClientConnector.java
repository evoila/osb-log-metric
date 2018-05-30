package de.evoila.cf.broker.connection;

import de.evoila.cf.broker.bean.CFClientBean;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by reneschollmeyer, evoila on 30.05.18.
 */
@Service
public class CFClientConnector {

    @Autowired
    private CFClientBean cfClientBean;

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

    public ReactorCloudFoundryClient client() {
        return ReactorCloudFoundryClient.builder()
                .connectionContext(connectionContext())
                .tokenProvider(tokenProvider())
                .build();
    }
}
