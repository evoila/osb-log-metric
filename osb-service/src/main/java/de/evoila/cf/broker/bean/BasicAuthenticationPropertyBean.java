package de.evoila.cf.broker.bean;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.basicauth")
@ConditionalOnProperty(prefix = "spring.basicauth", name = {"host", "port", "username", "password"})
public class BasicAuthenticationPropertyBean {
    public String host;
    public int port;
    public String username;
    public String password;
}
