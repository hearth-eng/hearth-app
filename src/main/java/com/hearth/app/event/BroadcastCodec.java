package com.hearth.app.event;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;

/**
 *
 * @author schan280
 */
public class BroadcastCodec implements MessageCodec<BroadcastEventWrapper, BroadcastEventWrapper> {

    @Override
    public void encodeToWire(Buffer buffer, BroadcastEventWrapper s) {
        Buffer tmp = Json.encodeToBuffer(s);
        buffer.appendBuffer(tmp);
    }

    @Override
    public BroadcastEventWrapper decodeFromWire(int pos, Buffer buffer) {
        return Json.decodeValue(buffer, BroadcastEventWrapper.class);
    }

    @Override
    public BroadcastEventWrapper transform(BroadcastEventWrapper s) {
        return s;
    }

    @Override
    public String name() {
        return "broadcast::codec";
    }

    @Override
    public byte systemCodecID() {
        return (byte)-1;            // Non-system codec
    }
    
}
