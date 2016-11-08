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
