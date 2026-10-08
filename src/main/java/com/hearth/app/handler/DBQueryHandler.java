package com.hearth.app.handler;

import com.hearth.app.bo.DBQueryBO;
import com.hearth.app.util.QueryParams;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

import java.net.HttpURLConnection;
import java.util.List;

/**
 * This handler is responsible for executing any sql query and returning the result.
 *
 * @author schan280
 */
public class DBQueryHandler extends AbstractHandler {

    private final DBQueryBO queryBO;

    public DBQueryHandler(Vertx vertx) {
        super(vertx);
        this.queryBO = new DBQueryBO();
    }

    /**
     * Execute the raw sql and return the response.
     *
     * @param ctx
     */
    public void executeSql(RoutingContext ctx) {
        final QueryParams params = params(ctx);
        
        vertx().executeBlocking(() -> {
            List<Object[]> result = queryBO.executeSql(user(ctx), ctx.body().asString(), params);
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
