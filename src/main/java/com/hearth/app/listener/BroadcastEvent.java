package com.hearth.app.listener;

import org.javalabs.decl.util.MapperUtil;

/**
 *
 * @author schan280
 */
public interface BroadcastEvent {
    
    String getEventId();
    
    default String serialize() {
        return new String(MapperUtil.encode(this));
    }
}
