package com.hearth.app.listener;

import com.hearth.app.cache.Cache;
import com.hearth.app.util.ImplClassScanner;
import java.lang.reflect.Field;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.javalabs.decl.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author schan280
 */
public class CacheOpsEventHandler extends AbstractEventHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheOpsEventHandler.class);
    
    private final Map<String, Cache> cacheMapping = new HashMap<>();
    
    CacheOpsEventHandler() {
        super();
        populate();
    }

    @Override
    public void handle(BroadcastEvent event) {
        // Perform operation
        // The message will have the format like => {cache}::{ops}::[id]::[<attr_name>:<attr_val>]
        if (! (event instanceof CacheEvent)) {
            return;
        }
        CacheEvent cacheEvent = (CacheEvent) event;
        
        Cache cache = cacheMapping.get(cacheEvent.getCacheName());
        if (cache == null) {
            LOGGER.warn("No cache found with name {}", cacheEvent.getCacheName());
            return;
        }
        if (cacheEvent.getOp() == CacheOps.ADD) {
            cache.add(cacheEvent.getId(), cacheEvent.getElement());
        }
        else if (cacheEvent.getOp() == CacheOps.FLUSH) {
            cache.flush();
        }
        else if (cacheEvent.getOp() == CacheOps.DELETE) {
            cache.remove(cacheEvent.getId());
        }
        else if (cacheEvent.getOp() == CacheOps.PATCH) {
            Object cachedObj = cache.get(cacheEvent.getId());
            if (cachedObj == null) {
                LOGGER.warn("No element found in for id {} in theh cache {}", cacheEvent.getId(), cacheEvent.getCacheName());
                return;
            }
            if (cacheEvent.getVal() == null) {
                LOGGER.warn("No attribute provided for patch");
                return;
            }
            try {
                for (Map.Entry<String, Object> me : cacheEvent.getVal().entrySet()) {
                    String fieldName = me.getKey();
                    Object fieldVal = me.getValue();

                    // Check if the target field is an enum.
                    Field field = cachedObj.getClass().getDeclaredField(fieldName);
                    if (field.getType().isEnum()) {
                        fieldVal = Enum.valueOf((Class<? extends Enum>)field.getType(), (String)fieldVal);
                    }
                    else if (Date.class.isAssignableFrom(field.getType())) {
                        fieldVal = new Date((long)fieldVal);
                    }
                    Object prevVal = ReflectionUtil.invokeGetter(cachedObj, fieldName);
                    ReflectionUtil.invokeSetter(cachedObj, fieldName, fieldVal);
                    Object currVal = ReflectionUtil.invokeGetter(cachedObj, fieldName);

                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Patched attribute {} having old value as {} to {} of cached element {} with id {}"
                                , fieldName, prevVal, currVal, cachedObj.getClass().getSimpleName(), cacheEvent.getId());
                    }
                }
            }
            catch (ReflectiveOperationException e) {
                LOGGER.error("Error while patching attribute", e);
            }
        }
    }
    
    private void populate() {
        if (! cacheMapping.isEmpty()) {
            return;
        }
        synchronized (cacheMapping) {
            List<Class<?>> impl = ImplClassScanner.findImplementingClasses(Cache.class, new String[] {"com.hearth.app"});
            if (! impl.isEmpty()) {
                for (Class<?> clazz : impl) {
                    String name = (String)ReflectionUtil.invokeStatic(clazz, "name");
                    Cache cache = (Cache)ReflectionUtil.invokeStatic(clazz, "getCache");
                    cacheMapping.put(name, cache);
                }
            }
        }
    }
}
