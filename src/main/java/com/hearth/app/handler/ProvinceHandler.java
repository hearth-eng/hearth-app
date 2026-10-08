package com.hearth.app.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import org.javalabs.decl.util.MapperUtil;
import org.javalabs.decl.vertx.config.model.ServerMessage;
import com.hearth.app.bo.ProvinceBO;
import com.hearth.app.cache.impl.ProvinceCache;
import com.hearth.app.event.BroadcastEventWrapper;
import com.hearth.app.listener.CacheEvent;
import com.hearth.app.listener.CacheOps;
import com.hearth.app.model.Province;
import com.hearth.app.model.ItemList;
import com.hearth.app.util.Constants;
import com.hearth.app.util.QueryParams;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Example REST handler.
 * 
 * <p>
 * This handler class is designed to handle asynchronous events, such as incoming network requests,
 * database responses, or other events within your application, allowing you to process data and respond
 * accordingly without blocking the main event loop, making your application highly scalable and reactive.
 * 
 * <p>
 * Refer to the <code>routing-config.xml</code> to understand the url mapping.
 */
public class ProvinceHandler extends AbstractHandler {
    
    private final ProvinceBO provinceBO;
    
    public ProvinceHandler(Vertx vertx) {
        super(vertx);
        this.provinceBO = new ProvinceBO();
    }
    
    /**
     * Create a new resource element in the system.
     * 
     * <p>
     * The newly created resource is stored in the memory. If you intend to use a database, the 
     * {@link Vertx#executeBlocking(java.util.concurrent.Callable, io.vertx.core.Handler) } will ensure the
     * request is processed in a non-blocking fashion.
     * 
     * <p>
     * The <code>COUNTER</code> will create a unique id to identify the element.
     * 
     * @param ctx   Vertx {@link RoutingContext} object.
     */
    public void create(RoutingContext ctx) {
        // If you use a remote store, this method will safely execute the blocking code.
        vertx().executeBlocking(() -> {
            Province province = MapperUtil.decode(ctx.body().buffer().getBytes(), Province.class);
            province = provinceBO.create(user(ctx), province);
            
            return province;
            
        }).onComplete(result -> {
            if (result.succeeded()) {
                CacheEvent event = new CacheEvent(ProvinceCache.name(), CacheOps.ADD);
                event.setId(result.result().getProvinceId());
                event.setElement(result.result());
                
                vertx().eventBus().send(Constants.BROADCAST_ADDRESS, new BroadcastEventWrapper(event));
                sendResponse(ctx, HttpURLConnection.HTTP_CREATED, result.result());
            }
            else {
                ctx.fail(result.cause());
            }
        });
    }

    public void batchCreate(RoutingContext ctx) {
        // If you use a remote store, this method will safely execute the blocking code.
        vertx().executeBlocking(() -> {
            List<Province> list = MapperUtil.mapper().readValue(ctx.body().buffer().getBytes(), new TypeReference<List<Province>>() {});
            provinceBO.create(user(ctx), list);
            
            ServerMessage msg = new ServerMessage();
            msg.setCode(HttpURLConnection.HTTP_CREATED);
            msg.setMessage("Inserted " + list.size() + " record(s)");
            
            return msg;
        }).onComplete(result -> {
            if (result.succeeded()) {
                sendResponse(ctx, HttpURLConnection.HTTP_CREATED, result.result());
            }
            else {
                ctx.fail(result.cause());
            }
        });
    }
    
    /**
     * Modify an existing resource by it's id (PUT request).
     * 
     * <p>
     * If no corresponding resource is found then this method will throw {@link NoSuchElementException}
     * resulting a <code>404</code> response.
     * 
     * <p>
     * For a PUT request, the server expects you to include all the information for the resource, even if
     * you only want to update a small part of it. If you leave something out, that part of the resource
     * will be erased or set to default.
     * 
     * @param ctx   Vertx {@link RoutingContext} object.
     */
    public void modify(RoutingContext ctx) {
        final String id = ctx.pathParam("id");
        
        // If you use a remote store, this method will safely execute the blocking code.
        vertx().executeBlocking(() -> {
            Province province = MapperUtil.decode(ctx.body().buffer().getBytes(), Province.class);
            province.setProvinceId(Integer.valueOf(id));


            // First fetch the entry, to see if this already exists.
            Province rs = provinceBO.modify(user(ctx), province);

            ServerMessage msg = new ServerMessage();
            msg.setCode(HttpURLConnection.HTTP_OK);
            msg.setMessage("Province modified successfully");

            return msg;
            
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
     * Partially modify an existing resource by it's id (PUT request).
     * 
     * <p>
     * If no corresponding resource is found then this method will throw {@link NoSuchElementException}
     * resulting a <code>404</code> response.
     * 
     * <p>
     * For a PUT request, the server expects you to include all the information for the resource, even if
     * you only want to update a small part of it. If you leave something out, that part of the resource
     * will be erased or set to default.
     * 
     * @param ctx   Vertx {@link RoutingContext} object.
     */
    public void patchUp(RoutingContext ctx) {
        final String id = ctx.pathParam("id");
        
        // If you use a remote store, this method will safely execute the blocking code.
        vertx().executeBlocking(() -> {
            Province province = MapperUtil.decode(ctx.body().buffer().getBytes(), Province.class);
            province.setProvinceId(Integer.valueOf(id));

            // First fetch the entry, to see if this already exists.
            Province rs = provinceBO.mmodifyPartial(user(ctx), province);

            ServerMessage msg = new ServerMessage();
            msg.setCode(HttpURLConnection.HTTP_OK);
            msg.setMessage("Province modified successfully");

            return msg;
            
        }).onComplete(result -> {
            if (result.succeeded()) {
                CacheEvent event = new CacheEvent(ProvinceCache.name(), CacheOps.PATCH);
                event.setId(Integer.valueOf(id));
                event.setVal(MapperUtil.decode(ctx.body().buffer().getBytes(), Map.class));
                
                vertx().eventBus().send(Constants.BROADCAST_ADDRESS, new BroadcastEventWrapper(event));
                sendResponse(ctx, HttpURLConnection.HTTP_OK, result.result());
            }
            else {
                ctx.fail(result.cause());
            }
        });
    }
    
    /**
     * View a specific resource by it's id.
     * 
     * <p>
     * If no corresponding resource is found then this method will throw {@link NoSuchElementException}
     * resulting a <code>404</code> response.
     * 
     * @param ctx   Vertx {@link RoutingContext} object.
     */
    public void view(RoutingContext ctx) {
        final String id = ctx.pathParam("id");
        
        // If you use a remote store, this method will safely execute the blocking code.
        vertx().executeBlocking(() -> {
            Province province = provinceBO.view(user(ctx), Integer.valueOf(id));

            return province;
            
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
     * View all the elements from the store.
     * 
     * @param ctx   Vertx {@link RoutingContext} object.
     */
    public void viewAll(RoutingContext ctx) {
        final QueryParams params = params(ctx);

        vertx().executeBlocking(() -> {
            List<Province> provinces = provinceBO.viewAll(user(ctx), params);
            List<Object> rows = (List)provinces;

            ItemList itemList = build(ctx.normalizedPath(), params, rows);
            return itemList;
            
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
     * Remove the element for the given id.
     * 
     * <p>
     * The element will be evicted from the in-memory store.
     * If no corresponding resource is found then this method will throw {@link NoSuchElementException}
     * resulting a <code>404</code> response.
     * 
     * @param ctx   Vertx {@link RoutingContext} object.
     */
    public void remove(RoutingContext ctx) {
        final String id = ctx.pathParam("id");
        
        // If you use a remote store, this method will safely execute the blocking code.
        vertx().executeBlocking(() -> {
            Province province = provinceBO.remove(user(ctx), Integer.valueOf(id));

            ServerMessage msg = new ServerMessage();
            msg.setCode(HttpURLConnection.HTTP_NO_CONTENT);
            msg.setMessage("Province deleted successfully");

            return msg;
            
        }).onComplete(result -> {
            if (result.succeeded()) {
                CacheEvent event = new CacheEvent(ProvinceCache.name(), CacheOps.DELETE);
                event.setId(Integer.valueOf(id));
                
                vertx().eventBus().send(Constants.BROADCAST_ADDRESS, new BroadcastEventWrapper(event));
                sendResponse(ctx, HttpURLConnection.HTTP_NO_CONTENT, result.result());
            }
            else {
                ctx.fail(result.cause());
            }
        });
    }
}
