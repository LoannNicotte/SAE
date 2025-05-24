package fr.imta.smartgrid.server;

import java.util.List;
import java.util.ArrayList;

import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Sensor;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import jakarta.persistence.EntityManager;

public class Person_id_get_handler implements Handler<RoutingContext> {
    EntityManager db;

    public Person_id_get_handler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {
       
        int id = Integer.parseInt(event.pathParam("id"));
        Person p = (Person) db.find(Person.class, id);
        JsonObject result = new JsonObject();
        result.put("id", id);
        result.put("first_name", p.getFirstName());
        result.put("last_name", p.getLastName());
        result.put("grid", p.getGrid().getId());
        List<Integer> sensorListId = new ArrayList<>();
        for (Sensor s : p.getSensors()){
            sensorListId.add(s.getId());
        }
        result.put("owned_sensors", sensorListId);
        event.json(result);
                
    }
}
