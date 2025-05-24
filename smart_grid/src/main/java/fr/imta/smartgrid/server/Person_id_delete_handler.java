package fr.imta.smartgrid.server;

import java.util.List;

import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Sensor;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import jakarta.persistence.EntityManager;

public class Person_id_delete_handler implements Handler<RoutingContext> {
    EntityManager db;

    public Person_id_delete_handler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {

        String idStr = event.pathParam("id");
        int id = Integer.parseInt(idStr);

        Person person = db.find(Person.class, id);
        if (person == null) {
            event.fail(404);
        }

        List<Integer> sensorIds = db
            .createNativeQuery(
                "SELECT id" +
                " FROM Sensor")                
            .getResultList();
        
        for(Integer sensorId : sensorIds){
            Sensor sensor = (Sensor) db.find(Sensor.class, sensorId);
            sensor.getOwners().remove(person);
        }

        db.getTransaction().begin();
        db.remove(person);
        db.getTransaction().commit();

        event.response()
            .setStatusCode(200)
            .end();

    }
}
