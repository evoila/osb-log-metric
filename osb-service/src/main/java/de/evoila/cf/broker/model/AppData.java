package de.evoila.cf.broker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.validation.constraints.NotEmpty;

public class AppData {

    @NotEmpty
    @JsonSerialize
    @JsonProperty("bindingId")
    public String bindingId;

    @NotEmpty
    @JsonSerialize
    @JsonProperty("instanceId")
    public String instanceId;

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
    @JsonProperty("organization_guid")
    public String organizationGuid;

    public AppData() {
    }

    public AppData(@NotEmpty String bindingId, @NotEmpty String instanceId, @NotEmpty String appId, @NotEmpty String appName, @NotEmpty String organization, @NotEmpty String space, @NotEmpty String organizationGuid) {
        this.bindingId = bindingId;
        this.instanceId = instanceId;
        this.appId = appId;
        this.appName = appName;
        this.organization = organization;
        this.space = space;
        this.organizationGuid = organizationGuid;
    }

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
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

    public String getOrganizationGuid() {
        return organizationGuid;
    }

    public void setOrganizationGuid(String organizationGuid) {
        this.organizationGuid = organizationGuid;
    }

    @Override
    public String toString() {
        return "AppData{" +
                "bindingId='" + bindingId + '\'' +
                ", instanceId='" + instanceId + '\'' +
                ", appId='" + appId + '\'' +
                ", appName='" + appName + '\'' +
                ", organization='" + organization + '\'' +
                ", space='" + space + '\'' +
                ", organization_guid='" + organizationGuid + '\'' +
                '}';
    }
}