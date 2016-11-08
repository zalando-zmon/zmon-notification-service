package org.zalando.zmon.notifications.config;

/**
 * Created by jmussler on 08.11.16.
 */
public class TeamMember {
    public TeamMember() {
    }

    public TeamMember(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String name;
    public String phone;
    public String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        if (null == phone) {
            return "";
        }
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
