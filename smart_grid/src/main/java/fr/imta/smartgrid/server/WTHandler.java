package fr.imta.smartgrid.server;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class WTHandler implements Handler<RoutingContext> {
    EntityManager db;

    public WTHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {

        String route_called = event.currentRoute().getName();
        String meth = event.request().method().toString();
        event.end(route_called + " " + meth);
    }
}
