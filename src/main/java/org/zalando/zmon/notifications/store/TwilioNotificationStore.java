package org.zalando.zmon.notifications.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.zmon.notifications.EscalationConfigSource;
import org.zalando.zmon.notifications.TwilioAlert;
import org.zalando.zmon.notifications.config.EscalationConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jmussler on 11.10.16.
 */
public class TwilioNotificationStore {

    private final Logger log = LoggerFactory.getLogger(TwilioNotificationStore.class);
    private final ObjectMapper mapper;
    private final JedisPool pool;
    private final EscalationConfigSource escalationSource;

    public TwilioNotificationStore(JedisPool pool, EscalationConfigSource escalationConfigSource, ObjectMapper mapper) {
        this.pool = pool;
        this.mapper = mapper;
        this.escalationSource = escalationConfigSource;
    }

    public String getOrSetIncidentId(int alertId) {
        try(Jedis jedis = pool.getResource()) {
            String uuid = UUID.randomUUID().toString();
            jedis.setnx("zmon:notify:incident:" + alertId, uuid);
            return jedis.get("zmon:notify:incident:" + alertId);
        }
        catch(Exception ex) {

        }
        return null;
    }

    public boolean isIncidentOngoing(int alertId, String incidentId) {
        try(Jedis jedis = pool.getResource()) {
            String currentId = jedis.get("zmon:notify:incident:" + alertId);
            return currentId != null && incidentId.equals(currentId);
        }
        catch(Exception ex) {
        }
        return false;
    }

    public String getOrSetIncidentId(int alertId, String entityId) {
        return "";
    }

    public String storeCallData(TwilioCallData data) {
        try(Jedis jedis = pool.getResource()) {
            String uuid = UUID.randomUUID().toString();
            jedis.set("zmon:notify:data:"+uuid, mapper.writeValueAsString(data));
            return uuid;
        }
        catch(Exception ex) {
            return null;
        }
    }

    public TwilioCallData getCallData(String uuid) {
        if (null == uuid || "".equals(uuid)) {
            return null;
        }

        try(Jedis jedis = pool.getResource()) {
            String data = jedis.get("zmon:notify:data:"+uuid);
            if (null == data) {
                return null;
            }
            return mapper.readValue(data, TwilioCallData.class);
        }
        catch(Exception ex) {
            return null;
        }
    }

    public boolean lockAlert(int alertId) {
        try(Jedis jedis = pool.getResource()) {
            final String lockKey = "zmon:notify:lock:" + alertId;
            long l = jedis.setnx(lockKey, System.currentTimeMillis()+"");
            if(l > 0) {
                jedis.expire(lockKey, 120);
                return true;
            }
        }
        catch(Exception ex) {

        }
        return false;
    }

    public boolean ackAlert(int alertId, String incidentId, String user) {
        try(Jedis jedis = pool.getResource()) {
            final String key = "zmon:notify:ack:" + alertId + ":" + incidentId;
            jedis.set(key, user);
            jedis.expire(key, 60 * 60);
        }
        catch(Exception ex) {
            return false;
        }
        return true;
    }

    public String resolveAlert(int alertId) {
        try(Jedis jedis = pool.getResource()) {
            final String key = "zmon:notify:incident:" + alertId;
            String incidentId = jedis.get(key);
            jedis.del(key);
            return incidentId;
        }
        catch(Exception ex) {

        }
        return null;
    }

    public boolean ackAlertEntity(int alertId, String entityId, String incidentId, String user) {
        try(Jedis jedis = pool.getResource()) {
            final String key ="zmon:notify:ack:" + alertId + ":" + incidentId + ":" + entityId;
            jedis.set(key, user);
            jedis.expire(key, 120 * 60);
        }
        catch(Exception ex) {
            return false;
        }
        return true;
    }

    public boolean isAck(int alertId, String incidentId, String entityId) {
        try(Jedis jedis = pool.getResource()) {
            String key ="zmon:notify:ack:" + alertId + ":" + incidentId;
            String value = jedis.get(key);
            if (null != value) {
                return true;
            }

            key ="zmon:notify:ack:" + alertId + ":" + incidentId + ":" + entityId;
            value = jedis.get(key);
            if (null != value) {
                return true;
            }
        }
        catch(Exception ex) {
            return false;
        }
        return false;
    }

    public static List<String> getNumbersFromTeam(EscalationConfig escalation) {
        List<String> numbers = new ArrayList<>();
        Set<String> added = new HashSet<>();
        for(String active : escalation.getOnCall()) {
            List<EscalationConfig.TeamMember> members = escalation.getMembers().stream().filter(x->x.getName().equals(active)).collect(Collectors.toList());
            if (members.size() > 0) {
                numbers.add(members.get(0).getPhone());
            }
            else {
                members = escalation.getPolicy().get(0).stream().filter(x->x.getName().equals(active)).collect(Collectors.toList());
                if (members.size() > 0) {
                    numbers.add(members.get(0).getPhone());
                }
            }
            added.add(active);
        }

        if(escalation.getPolicy().size()>1) {
            for (List<EscalationConfig.TeamMember> escalationList : escalation.getPolicy().subList(1, escalation.getPolicy().size())) {
                for(EscalationConfig.TeamMember member : escalationList) {
                    if(!added.contains(member.getName())) {
                        if(null != member.getPhone() && !"".equals(member.getPhone())) {
                            numbers.add(member.getPhone());
                            added.add(member.getName());
                        }
                        else {
                            List<EscalationConfig.TeamMember> members = escalation.getMembers().stream().filter(x->x.getName().equals(member.getName())).collect(Collectors.toList());
                            if(members.size()>0) {
                                numbers.add(members.get(0).getPhone());
                                added.add(member.getName());
                            }
                        }
                    }
                }
            }
        }

        return numbers;
    }

    public boolean storeEscalations(TwilioAlert alert, String incidentId) {
        EscalationConfig escalationConfig = escalationSource.getEscalationConfig(alert.getEscalationTeam().toLowerCase());
        List<String> numbers = null;
        if(null != escalationConfig) {
            numbers = getNumbersFromTeam(escalationConfig);
        }
        if(alert.getNumbers().size() > 0) {
            numbers = alert.getNumbers();
        }

        try(Jedis jedis = pool.getResource()) {
            long now = System.currentTimeMillis();
            now /= 1000;

            // increase task level, as queue is unique set
            PendingNotification task = new PendingNotification(alert.getAlertId(), 0, incidentId, numbers.get(0), alert.getEntityId(), alert.getMessage(), alert.getVoice());
            jedis.zadd("zmon:notify:queue", now + 0 * 60, mapper.writeValueAsString(task));

            task = new PendingNotification(alert.getAlertId(), 1, incidentId, numbers.get(0), alert.getEntityId(), alert.getMessage(), alert.getVoice());
            jedis.zadd("zmon:notify:queue", now + 2 * 60, mapper.writeValueAsString(task));

            if(alert.getNumbers().size() > 1) {
                for(int i = 1; i < numbers.size(); ++i) {
                    task = new PendingNotification(alert.getAlertId(), i + 1, incidentId, numbers.get(i), alert.getEntityId(), alert.getMessage(), alert.getVoice());

                    // add an additional 5min for every other phone number
                    jedis.zadd("zmon:notify:queue", now + 2 * 60 + i * 5 * 60, mapper.writeValueAsString(task));
                }
            }
        }
        catch(Exception ex) {
            log.error("Writing tasks to redis failed", ex);
            return false;
        }
        return false;
    }

    public PendingNotification mapJson(String data) {
        try {
            return mapper.readValue(data, mapper.getTypeFactory().constructType(PendingNotification.class));
        }
        catch(Exception ex) {
            log.error("Json mapping failed", ex);
            return null;
        }
    }

    public List<PendingNotification> getPendingNotifications() {
        long max = System.currentTimeMillis() / 1000;
        try(Jedis jedis = pool.getResource()) {
            // execute a fetch atomically and issue calls
            long lock = jedis.setnx("zmon:notify:lock", "true");
            if (lock > 0) {
                jedis.expire("zmon:notify:lock", 30); // only one call every 30, need to lock to single instance to improve aggregation
                List<String> result = (List<String>) jedis.eval("local t = redis.call('ZRANGEBYSCORE', 'zmon:notify:queue', '0', '" + max + "'); redis.call('ZREMRANGEBYSCORE', 'zmon:notify:queue', '0', '" + max + "'); return t;", 0);
                List<PendingNotification> l =  result.stream().map(x -> mapJson(x)).filter(x-> null != x).collect(Collectors.toList());
                return l;
            }
            else {
                return new ArrayList<>();
            }
        }
        catch(Exception ex) {
            log.error("Fetching queue items failed", ex);
            return null;
        }
    }
}
