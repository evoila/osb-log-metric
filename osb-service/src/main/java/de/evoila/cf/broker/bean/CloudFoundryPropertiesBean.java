package de.evoila.cf.broker.bean;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cloudfoundry")
@ConditionalOnProperty(prefix = "cloudfoundry", name = {"host", "username", "password", "clientid"})
public class CloudFoundryPropertiesBean {
    private String host;
    private String username;
    private String password;
    private String clientid;

    public String getHost() {
        return host;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getClientid() {
        return clientid;
    }
}
