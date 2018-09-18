package de.evoila.cf.broker.model;

/**
 * Created by reneschollmeyer, evoila on 24.05.18.
 */
public class LogMetricEnvironment {

    private String appName;
    private String appId;
    private String space;
    private String organization;
    private String organization_guid;

    public LogMetricEnvironment(String appName, String appId, String space, String organization, String organization_guid) {
        this.appName = appName;
        this.appId = appId;
        this.space = space;
        this.organization = organization;
        this.organization_guid = organization_guid;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getOrganization_guid() {
        return organization_guid;
    }

    public void setOrganization_guid(String organization_guid) {
        this.organization_guid = organization_guid;
    }
}
