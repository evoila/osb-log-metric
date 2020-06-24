package de.evoila.cf.broker.dashboard.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.evoila.cf.broker.model.AppData;

import javax.validation.constraints.NotEmpty;

/**
 * @author latzinger
 */
public class CfAuthScope {

    @NotEmpty
    @JsonSerialize
    @JsonProperty("type")
    public String type = "cf";

    @NotEmpty
    @JsonSerialize
    @JsonProperty("orgId")
    public String orgId;

    @NotEmpty
    @JsonSerialize
    @JsonProperty("spaceId")
    public String spaceId;

    @NotEmpty
    @JsonSerialize
    @JsonProperty("serviceInstanceId")
    public String serviceInstanceId;

    public CfAuthScope(AppData appData){
        this.orgId = appData.organizationGuid;
        this.spaceId = appData.spaceId;
        this.serviceInstanceId = appData.instanceId;
    }

    public CfAuthScope(@NotEmpty String orgId, @NotEmpty String spaceId, @NotEmpty String serviceInstanceId) {
        this.orgId = orgId;
        this.spaceId = spaceId;
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    @Override
    public String toString() {
        return "CfAuthScope{" +
                "type='" + type + '\'' +
                ", orgId='" + orgId + '\'' +
                ", spaceId='" + spaceId + '\'' +
                ", serviceInstanceId='" + serviceInstanceId + '\'' +
                '}';
    }
}
