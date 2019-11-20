package de.evoila.cf.broker.backend.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.evoila.cf.broker.model.LogMetricRedisObject;

import javax.validation.constraints.NotEmpty;

public class BindingRequest {

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
    public String organization_guid;

    public BindingRequest() {
    }

    public BindingRequest(String bindingId, String instanceId, String appId, LogMetricRedisObject logMetricRedisObject) {
        this.bindingId = bindingId;
        this.instanceId = instanceId;
        this.appId = appId;
        space = logMetricRedisObject.getSpace();
        appName = logMetricRedisObject.getApplicationName();
        organization = logMetricRedisObject.getOrganization();
        organization_guid = logMetricRedisObject.getOrganization_guid();
    }

    public BindingRequest(@NotEmpty String bindingId, @NotEmpty String instanceId, @NotEmpty String appId, @NotEmpty String appName, @NotEmpty String organization, @NotEmpty String space, @NotEmpty String organization_guid) {
        this.bindingId = bindingId;
        this.instanceId = instanceId;
        this.appId = appId;
        this.appName = appName;
        this.organization = organization;
        this.space = space;
        this.organization_guid = organization_guid;
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

    public String getOrganization_guid() {
        return organization_guid;
    }

    public void setOrganization_guid(String organization_guid) {
        this.organization_guid = organization_guid;
    }

}