package com.hearth.app.util;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.net.HostAndPort;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import java.util.Set;

/**
 *
 * @author schan280
 */
public class TestHttpServerResponse implements HttpServerResponse {
    
    private int statusCode;
    private String statusMessage;
    private volatile Buffer buff;
    private final MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    
    private Boolean chunked = Boolean.FALSE;
    private Handler<Throwable> exHandler;
    
    public TestHttpServerResponse(Object payload) {
        this(200, payload);
    }
    
    public TestHttpServerResponse(int statusCode, Object payload) {
        this.statusCode = statusCode;
        if (payload == null) {
            this.buff = Buffer.buffer();
        }
        else if (payload instanceof Buffer) {
            this.buff = (Buffer)payload;
        }
        else {
            this.buff = Json.encodeToBuffer(payload);
        }
    }
    
    public Buffer result() {
        return buff;
    }

    @Override
    public HttpServerResponse exceptionHandler(Handler<Throwable> handler) {
        this.exHandler = handler;
        return this;
    }

    @Override
    public HttpServerResponse setWriteQueueMaxSize(int maxSize) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpServerResponse drainHandler(Handler<Void> handler) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public HttpServerResponse setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    @Override
    public String getStatusMessage() {
        return this.statusMessage;
    }

    @Override
    public HttpServerResponse setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        return this;
    }

    @Override
    public HttpServerResponse setChunked(boolean chunked) {
        this.chunked = chunked;
        return this;
    }

    @Override
    public boolean isChunked() {
        return this.chunked;
    }

    @Override
    public MultiMap headers() {
        return headers;
    }

    @Override
    public HttpServerResponse putHeader(String name, String value) {
        headers.add(name, value);
        return this;
    }

    @Override
    public HttpServerResponse putHeader(CharSequence name, CharSequence value) {
        headers.add(name, value);
        return this;
    }

    @Override
    public HttpServerResponse putHeader(String name, Iterable<String> values) {
        headers.add(name, values);
        return this;
    }

    @Override
    public HttpServerResponse putHeader(CharSequence name, Iterable<CharSequence> values) {
        headers.add(name, values);
        return this;
    }

    @Override
    public MultiMap trailers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpServerResponse putTrailer(String name, String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpServerResponse putTrailer(CharSequence name, CharSequence value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpServerResponse putTrailer(String name, Iterable<String> values) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpServerResponse putTrailer(CharSequence name, Iterable<CharSequence> value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpServerResponse closeHandler(Handler<Void> handler) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpServerResponse endHandler(Handler<Void> handler) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> write(String chunk, String enc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> write(String chunk) {
        buff = Buffer.buffer(chunk);
        return null;
    }

    @Override
    public Future<Void> writeEarlyHints(MultiMap headers) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> end(String chunk) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> end(String chunk, String enc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> end(Buffer chunk) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> end() {
        return null;
    }

    @Override
    public Future<Void> sendFile(String filename, long offset, long length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean ended() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean closed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean headWritten() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpServerResponse headersEndHandler(Handler<Void> handler) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpServerResponse bodyEndHandler(Handler<Void> handler) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long bytesWritten() {
        return 0L;
    }

    @Override
    public Future<HttpServerResponse> push(HttpMethod method, HostAndPort authority, String path, MultiMap headers) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpServerResponse addCookie(Cookie cookie) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Cookie removeCookie(String name, boolean invalidate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Cookie> removeCookies(String name, boolean invalidate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Cookie removeCookie(String name, String domain, String path, boolean invalidate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> write(Buffer data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean writeQueueFull() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> writeHead() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> sendFile(FileChannel channel, long offset, long length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> sendFile(RandomAccessFile file, long offset, long length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> writeContinue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> reset(long code) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> writeCustomFrame(int type, int flags, Buffer payload) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Boolean> cancel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> writeAltSvc(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long streamId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
