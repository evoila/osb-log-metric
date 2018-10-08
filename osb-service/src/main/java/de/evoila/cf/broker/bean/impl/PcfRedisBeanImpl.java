package de.evoila.cf.broker.bean.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import de.evoila.cf.broker.util.ObjectMapperUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import de.evoila.cf.broker.bean.RedisBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Profile("pcf")
@ConfigurationProperties(prefix = "redis")
public class PcfRedisBeanImpl implements RedisBean {

    private List<String> hosts;
    private int port;
    private String password;
    private String pcfHosts;
    
  

   public void setPcfHosts(String pcfHosts) throws IOException {
        List<String> pcfHostList = ObjectMapperUtils.getObjectMapper().readValue(pcfHosts, new TypeReference<List<String>>(){});
        this.hosts = new ArrayList<>();
        for (String host : pcfHostList) {
            this.hosts.add(host);
        }

        this.pcfHosts = pcfHosts;
    }

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
