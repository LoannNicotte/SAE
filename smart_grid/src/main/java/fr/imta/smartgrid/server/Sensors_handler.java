package fr.imta.smartgrid.server;

import java.util.List;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class Sensors_handler implements Handler<RoutingContext> {
    EntityManager db;

    public Sensors_handler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {

        String kind = event.pathParam("kind");
        List<Integer> sensorIds = db
                    .createNativeQuery(
                        "SELECT id " +
                        " FROM sensor " +
                        " WHERE dtype = ?")                
                    .setParameter(1, kind)     
                    .getResultList();
        event.json(sensorIds);
    }
}
