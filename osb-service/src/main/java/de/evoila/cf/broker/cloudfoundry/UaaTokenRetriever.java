package de.evoila.cf.broker.cloudfoundry;

import de.evoila.cf.broker.bean.CloudFoundryPropertiesBean;
import de.evoila.cf.broker.exception.CouldNotRequestTokenException;
import de.evoila.cf.security.uaa.provider.UaaRelyingPartyAuthenticationProvider;
import de.evoila.cf.security.uaa.utils.UaaFilterUtils;
import org.springframework.http.*;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UaaTokenRetriever {

    private String oauthToken;
    private String endpoint = "/oauth/token";
    private CloudFoundryPropertiesBean cloudFoundryPropertiesBean;

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

    private boolean verifyToken() {

        Jwt jwt = JwtHelper.decode(oauthToken);
        Map<String, Object> token = UaaFilterUtils.tryExtractToken(jwt);
        long timestamp = ((long) ((int) token.get(UaaRelyingPartyAuthenticationProvider.Properties.EXP))) * 1000;
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

