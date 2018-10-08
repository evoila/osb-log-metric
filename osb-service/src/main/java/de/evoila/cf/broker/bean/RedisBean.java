package de.evoila.cf.broker.bean;


import java.util.List;

public interface RedisBean {

    List<String> getHosts();
    int getPort();
    String getPassword();

}
