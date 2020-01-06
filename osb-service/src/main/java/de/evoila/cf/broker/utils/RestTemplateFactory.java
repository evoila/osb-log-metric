package de.evoila.cf.broker.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class RestTemplateFactory {

    private static RestTemplate restTemplate;

    private RestTemplateFactory() {
    }

    public static RestTemplate getInstance() {
        if (restTemplate == null)
            restTemplate = new RestTemplate();
        return restTemplate;
    }

    public static HttpHeaders getHeadersBasicAuth(String username, String password) {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.setBasicAuth(username, password);
        return header;
    }

    public static HttpHeaders getHeadersBearer(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setBearerAuth(token);
        return httpHeaders;
    }

}
