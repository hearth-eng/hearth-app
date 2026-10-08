package com.hearth.app.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author schan280
 */
public class CacheEvent implements BroadcastEvent {
    
    private String eventId;
    private String cacheName;
    private CacheOps op;
    private Object id;
    private Map<String, Object> val = new HashMap<>();
    private Object element;
    private String eventClass;
    
    public CacheEvent() {}

    public CacheEvent(String cacheName, CacheOps op) {
        this.eventId = UUID.randomUUID().toString();
        this.cacheName = cacheName;
        this.op = op;
        this.eventClass = getClass().getName();
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public CacheOps getOp() {
        return op;
    }

    public void setOp(CacheOps op) {
        this.op = op;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Map<String, Object> getVal() {
        return val;
    }

    public void setVal(Map<String, Object> val) {
        this.val = val;
    }

    public Object getElement() {
        return element;
    }

    public void setElement(Object element) {
        this.element = element;
    }

    public String getEventClass() {
        return eventClass;
    }

    public void setEventClass(String eventClass) {
        this.eventClass = eventClass;
    }
    
}
