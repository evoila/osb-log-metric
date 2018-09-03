package de.evoila.cf.broker.bean;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by reneschollmeyer, evoila on 26.04.18.
 */
@Service
@ConfigurationProperties(prefix = "redis")
public class RedisBean {

    private List<String> hosts;
    private int port;
    private String password;

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
