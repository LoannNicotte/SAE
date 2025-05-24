package fr.imta.smartgrid.server;

import fr.imta.smartgrid.model.Measurement;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import jakarta.persistence.EntityManager;

public class Measure_id_handler implements Handler<RoutingContext> {
    EntityManager db;

    public Measure_id_handler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {

        int id = Integer.parseInt(event.pathParam("id"));
        Measurement measure = (Measurement) db.find(Measurement.class, id);
        if (measure == null){
            event.fail(404);
        }
        else{
            event.json(measure.toJSON());
        }
    }
}
