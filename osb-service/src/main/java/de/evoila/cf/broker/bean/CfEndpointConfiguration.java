package de.evoila.cf.broker.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cfendpoints")
public class CfEndpointConfiguration {

    @JsonProperty(value = "identity_provider")
    private String identityProvider;

    @JsonProperty(value = "default")
    private String defaultEndpoint;


    public String getDefault() {
        return defaultEndpoint;
    }

    public void setDefault(String defaultEndpoint) {
        this.defaultEndpoint = defaultEndpoint;
    }

    public String getIdentityProvider() {
        return identityProvider;
    }

    public void setIdentityProvider(String identityProvider) {
        this.identityProvider = identityProvider;
    }

}