package org.zalando.zmon.notifications.config;

/**
 * Created by jmussler on 07.11.16.
 */
public class ConfigPayload<T> {
    private T data;
    private String id;
    private String team;
    private String type;

    public ConfigPayload() {
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
