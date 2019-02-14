package de.evoila.cf.broker.model;

/**
 * Created by reneschollmeyer, evoila on 01.06.18.
 */
public class LogMetricRedisObject {

    private String applicationName;
    private String space;
    private String organization;
    private String organization_guid;
    private boolean subscribed;
    private boolean logMetric;
    private boolean autoscaler;

    public LogMetricRedisObject() {

    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
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

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    public boolean isLogMetric() {
        return logMetric;
    }

    public void setLogMetric(boolean logMetric) {
        this.logMetric = logMetric;
    }

    public boolean isAutoscaler() {
        return autoscaler;
    }

    public void setAutoscaler(boolean autoscaler) {
        this.autoscaler = autoscaler;
    }
}