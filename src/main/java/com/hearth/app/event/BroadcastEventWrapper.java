package com.hearth.app.event;

import com.hearth.app.listener.BroadcastEvent;

/**
 *
 * @author schan280
 */
public class BroadcastEventWrapper {
    
    private final BroadcastEvent event;

    public BroadcastEventWrapper(BroadcastEvent event) {
        this.event = event;
    }

    public BroadcastEvent getEvent() {
        return event;
    }
}
