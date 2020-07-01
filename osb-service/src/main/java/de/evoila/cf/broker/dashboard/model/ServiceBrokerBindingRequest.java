package de.evoila.cf.broker.dashboard.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.evoila.cf.broker.model.AppData;

import javax.validation.constraints.NotEmpty;

public class ServiceBrokerBindingRequest {

    @NotEmpty
    @JsonSerialize
    @JsonProperty("bindingId")
    public String bindingId;

    @NotEmpty
    @JsonSerialize
    @JsonProperty("authScope")
    public CfAuthScope authScope;

    @NotEmpty
    @JsonSerialize
    @JsonProperty("appId")
    public String appId;

    @NotEmpty
    @JsonSerialize
    @JsonProperty("appName")
    public String appName;

    @NotEmpty
    @JsonSerialize
    @JsonProperty("organization")
    public String organization;

    @NotEmpty
    @JsonSerialize
    @JsonProperty("space")
    public String space;

    @NotEmpty
    @JsonSerialize
    @JsonProperty("type")
    public String type = "servicebroker";

    public ServiceBrokerBindingRequest(AppData appData, CfAuthScope authScope){
        this.bindingId = appData.bindingId;
        this.authScope = authScope;
        this.appId = appData.appId;
        this.appName = appData.appName;
        this.organization = appData.organization;
        this.space = appData.space;
    }

    public ServiceBrokerBindingRequest(@NotEmpty String bindingId, @NotEmpty CfAuthScope authScope, @NotEmpty String appId, @NotEmpty String appName, @NotEmpty String organization, @NotEmpty String space) {
        this.bindingId = bindingId;
        this.authScope = authScope;
        this.appId = appId;
        this.appName = appName;
        this.organization = organization;
        this.space = space;
    }

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    public CfAuthScope getAuthScope() {
        return authScope;
    }

    public void setAuthScope(CfAuthScope authScope) {
        this.authScope = authScope;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ServiceBrokerBindingRequest{" +
                "bindingId='" + bindingId + '\'' +
                ", authScope=" + authScope +
                ", appId='" + appId + '\'' +
                ", appName='" + appName + '\'' +
                ", organization='" + organization + '\'' +
                ", space='" + space + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
