package de.evoila.cf.broker.cloudfoundry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.evoila.cf.broker.bean.CloudFoundryPropertiesBean;
import de.evoila.cf.broker.exception.CouldNotRequestTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UaaTokenRetriever {

    private String oauthToken;
    private String endpoint = "/oauth/token";
    private CloudFoundryPropertiesBean cloudFoundryPropertiesBean;

    private static final Logger log = LoggerFactory.getLogger(UaaTokenRetriever.class);
    public static ObjectMapper objectMapper = new ObjectMapper();

    private static class Properties {

        // These claims are always present (regardless of scope)
        public static final String EXP = "exp";
        public static final String CLIENT = "client";
        public static final String ORIGIN = "origin";
        public static final String SCOPE = "scope";
        public static final String SUB = "sub";
        public static final String USER_NAME = "user_name";

    }

    public UaaTokenRetriever(CloudFoundryPropertiesBean cloudFoundryPropertiesBean) {
        this.cloudFoundryPropertiesBean = cloudFoundryPropertiesBean;
    }

    public String getoAuthToken() {

        if (oauthToken != null && verifyToken()) {
            return oauthToken;
        }

        HttpHeaders httpHeaders = getHeaders();
        RestTemplate restTemplate = new RestTemplate();
        String uri = cloudFoundryPropertiesBean.getHost() + endpoint;

        HttpEntity<LinkedMultiValueMap<String, String>> request = new HttpEntity<>(getBody(), httpHeaders);

        ResponseEntity<HashMap> result = restTemplate.exchange(uri, HttpMethod.POST, request, HashMap.class);

        if (result.getStatusCode().is2xxSuccessful()) {
            return (oauthToken = (String) result.getBody().get("access_token"));
        } else {
            throw new CouldNotRequestTokenException(result.getBody().toString(), result.getStatusCode(), new Date().getTime());
        }

    }

    private HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.setBasicAuth(cloudFoundryPropertiesBean.getUsername(), cloudFoundryPropertiesBean.getPassword());

        return httpHeaders;
    }

    public static Map<String, Object> tryExtractToken(Jwt jwt) {
        if (jwt.getClaims() == null)
            return null;

        try {
            return objectMapper.readValue(jwt.getClaims(), new TypeReference<HashMap<String, Object>>() {});
        } catch (IOException e) {
            log.error("Error parsing claims from JWT", e);
        }

        return null;
    }

    private boolean verifyToken() {

        Jwt jwt = JwtHelper.decode(oauthToken);
        Map<String, Object> token = tryExtractToken(jwt);
        long timestamp = ((long) ((int) token.get(Properties.EXP))) * 1000;
        Date now = new Date();
        Date expirationTime = new Date(timestamp);
        if (!now.before(expirationTime)) {
            return false;
        }
        return true;
    }

    private LinkedMultiValueMap<String, String> getBody() {
        LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        map.add("client_id", cloudFoundryPropertiesBean.getClientid());
        return map;
    }

}

