package com.hearth.app.listener;

import com.hearth.app.util.Constants;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import java.io.IOException;
import java.util.Map;
import org.javalabs.decl.util.MapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author schan280
 */
public class ChannelEventListener extends RedisPubSubAdapter<String, String> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelEventListener.class);
    
    private final Map<String, AbstractEventHandler> handlers = Map.of(
            Constants.CACHE_CHANNEL, new CacheOpsEventHandler()
    );

    @Override
    public void message(String channel, String message) {
        try {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Received message {} from channel {}", message, channel);
            }
            AbstractEventHandler handler = handlers.get(channel);
            if (handler != null) {
                String className = MapperUtil.mapper().readTree(message).get("eventClass").asText();
                if (className == null) {
                    LOGGER.warn("No className attribute found. Cannot de-serialize the broadcast event");
                    return;
                }
                Class<? extends BroadcastEvent> clazz = (Class<? extends BroadcastEvent>) Class.forName(className);
                
                BroadcastEvent event = MapperUtil.decode(message.getBytes(), clazz);
                handler.handle(event);
            }
        }
        catch (IOException  | ClassNotFoundException e) {
            LOGGER.error("Error de-serializing broadcasting event", e);
        }
    }
}
