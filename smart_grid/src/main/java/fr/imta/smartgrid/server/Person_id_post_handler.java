package fr.imta.smartgrid.server;

import java.util.List;

import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Sensor;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import jakarta.persistence.EntityManager;

public class Person_id_post_handler implements Handler<RoutingContext> {
    EntityManager db;

    public Person_id_post_handler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {

        int id = Integer.parseInt(event.pathParam("id"));

        Person person = db.find(Person.class, id);
        if (person == null) {
            event.fail(404);            
        }
        else{
    
            JsonObject body = null;
            try {
                body = event.body().asJsonObject();
            } catch (Exception e) {
                event.fail(500);
            }
        
            Object firstRaw = body.getValue("first_name");
            if (firstRaw != null) {
                person.setFirstName((String) firstRaw);
            }

            Object lastRaw = body.getValue("last_name");
            if (lastRaw != null) {
                person.setLastName((String) lastRaw);
            }

            Object gridRaw = body.getValue("grid");
            if (gridRaw != null) {
                int gridId = (Integer) gridRaw;
                Grid newGrid = db.find(Grid.class, gridId);
                if (newGrid == null) {
                    event.response().setStatusCode(500);
                }
                person.setGrid(newGrid);
            }

            Object sensorsRaw = body.getValue("owned_sensors");
            if (sensorsRaw != null) {
                JsonArray arr = (JsonArray) sensorsRaw;
                person.getSensors().clear();
                for (Object o : arr) {
                    int sensorId = (Integer) o;
                    Sensor s = db.find(Sensor.class, sensorId);
                    if (s != null) {
                        person.getSensors().add(s);
                    }
                }

                List<Integer> sensorIds = db
                    .createNativeQuery(
                        "SELECT id" +
                        " FROM Sensor")                
                    .getResultList();
                
                for(Integer sensorId : sensorIds){
                    Sensor sensor = (Sensor) db.find(Sensor.class, sensorId);
                    if (person.getSensors().contains(sensor) && !sensor.getOwners().contains(person)){
                        sensor.getOwners().add(person);
                    }else if (!person.getSensors().contains(sensor) && sensor.getOwners().contains(person)){
                        sensor.getOwners().remove(person);
                    }
                    }

            }
        
            try {
                db.getTransaction().begin();
                db.merge(person);
                db.getTransaction().commit();
            } catch (Exception e) {
                if (db.getTransaction().isActive()) {
                    db.getTransaction().rollback();
                }
                event.fail(500, new RuntimeException("Erreur lors de la mise à jour de la personne"));
            }
        
            event.response()
                .setStatusCode(200)
                .end();
        }
               
    }
}
