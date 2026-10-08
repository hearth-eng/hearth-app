package com.hearth.app.listener;

import com.hearth.app.config.ApplicationConfiguration;
import com.hearth.app.util.Constants;
import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author schan280
 */
public class DistributedCache {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DistributedCache.class);

    private static final DistributedCache INSTANCE = new DistributedCache();
    
    private RedisClient redisClient;
    
    private DistributedCache() {}
    
    public static DistributedCache get() {
        return INSTANCE;
    }
    
    public void init() {
        Map<String, Object> props = ApplicationConfiguration.getInstance().get("redis.config");
        redisClient = RedisClient.create((String)props.get("url"));
        
        StatefulRedisPubSubConnection<String, String> pubSubConnection = redisClient.connectPubSub();

        // Add a listener to process asynchronous broadcasted messages
        pubSubConnection.addListener(new ChannelEventListener());

        // Trigger the actual channel subscription
        RedisPubSubCommands<String, String> syncCommands = pubSubConnection.sync();
        syncCommands.subscribe(Constants.CACHE_CHANNEL);
        
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Registered redis pub-sub listener to channel: {}", Constants.CACHE_CHANNEL);
        }
    }
    
    public void broadcast(String channel, BroadcastEvent event) {
        try (StatefulRedisPubSubConnection<String, String> pubSubConnection = redisClient.connectPubSub()) {
            RedisPubSubCommands<String, String> syncCommands = pubSubConnection.sync();
            syncCommands.publish(channel, event.serialize());
            
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Sent redis pub-sub event {} to channel: {}", event.getEventId(), channel);
            }
        }
    }
}
