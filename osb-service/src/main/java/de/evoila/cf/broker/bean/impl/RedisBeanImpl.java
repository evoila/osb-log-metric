package de.evoila.cf.broker.bean.impl;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import de.evoila.cf.broker.bean.RedisBean;

import java.util.List;

@Service
@Profile("!pcf")
@ConfigurationProperties(prefix = "redis")
public class RedisBeanImpl implements RedisBean {

    private List<String> hosts;
    private int port;
    private String password;

    @Override
    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    @Override
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
