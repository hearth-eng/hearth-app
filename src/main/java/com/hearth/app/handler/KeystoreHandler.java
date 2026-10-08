package com.hearth.app.handler;

import com.hearth.app.bo.KeystoreBO;
import com.hearth.app.model.KeyCertInfo;
import com.hearth.app.util.QueryParams;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * This handler is responsible for executing any sql query and returning the result.
 *
 * @author schan280
 */
public class KeystoreHandler extends AbstractHandler {

    private final KeystoreBO keystoreBO;

    public KeystoreHandler(Vertx vertx) {
        super(vertx);
        this.keystoreBO = new KeystoreBO();
    }

    /**
     * Return the aliases present in the keystore.
     *
     * @param ctx
     */
    public void getAliases(RoutingContext ctx) {
        vertx().executeBlocking(() -> {
            Map<String, Object> result = keystoreBO.viewAliases(user(ctx));
            return result;
            
        }).onComplete(result -> {
            if (result.succeeded()) {
                sendResponse(ctx, HttpURLConnection.HTTP_OK, result.result());
            }
            else {
                ctx.fail(result.cause());
            }
        });
    }

    /**
     * Return the metadata of the keystore.
     *
     * @param ctx
     */
    public void getStorageInfo(RoutingContext ctx) {
        final QueryParams params = params(ctx);
        
        vertx().executeBlocking(() -> {
            List<KeyCertInfo> result = keystoreBO.viewStorageInfo(user(ctx), params);
            return result;
            
        }).onComplete(result -> {
            if (result.succeeded()) {
                sendResponse(ctx, HttpURLConnection.HTTP_OK, result.result());
            }
            else {
                ctx.fail(result.cause());
            }
        });
    }
}
