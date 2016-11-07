package org.zalando.zmon.notifications.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jmussler on 07.11.16.
 */
public class EscalationConfig {

    public List<String> getOnCall() {
        return onCall;
    }

    public void setOnCall(List<String> onCall) {
        this.onCall = onCall;
    }

    public static class TeamMember {
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

    private List<TeamMember> members = new ArrayList<>();
    private List<String> onCall = new ArrayList<>();
    private List<List<TeamMember>> policy = new ArrayList<>();

    public List<TeamMember> getMembers() {
        return members;
    }

    public void setMembers(List<TeamMember> members) {
        this.members = members;
    }

    public List<List<TeamMember>> getPolicy() {
        return policy;
    }

    public void setPolicy(List<List<TeamMember>> policy) {
        this.policy = policy;
    }
}
