package org.zalando.zmon.notifications;

import java.util.List;

/**
 * Created by jmussler on 10.06.16.
 */
public interface EventType {

    int getId();

    String getName();

    List<String> getFieldNames();

}