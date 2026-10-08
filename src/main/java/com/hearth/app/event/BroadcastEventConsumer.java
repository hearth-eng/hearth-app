package com.hearth.app.event;

import com.hearth.app.listener.DistributedCache;
import com.hearth.app.util.Constants;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author schan280
 */
public class BroadcastEventConsumer implements Handler<Message<BroadcastEventWrapper>> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(BroadcastEventConsumer.class);
    
    public BroadcastEventConsumer() {}

    @Override
    public void handle(Message<BroadcastEventWrapper> event) {
        try {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Received broadcasting event. Event: {}", event.body());
            }
            DistributedCache.get().broadcast(Constants.CACHE_CHANNEL, event.body().getEvent());
        }
        catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
}
