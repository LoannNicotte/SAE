package fr.imta.smartgrid.server;

import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Sensor;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import jakarta.persistence.EntityManager;

public class Person_handler implements Handler<RoutingContext> {
    EntityManager db;

    public Person_handler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {

        JsonObject body = null;
        try {
            body = event.body().asJsonObject();
        } catch (Exception e) {
            event.fail(500);
        }
        
        if(body == null){
            event.fail(500);
        }
        else if (!body.containsKey("last_name") || !body.containsKey("grid") ||!body.containsKey("first_name")) {
            event.fail(500);
        }
        else{

            String firstName = body.getString("first_name");
            String lastName = body.getString("last_name");
            int gridId = body.getInteger("grid");

            Grid grid = (Grid)db.find(Grid.class, gridId);
            if (grid == null) {
                event.fail(500);
            }

            Person person = new Person();
            person.setFirstName(firstName);
            person.setLastName(lastName);
            person.setGrid(grid);

            if (body.containsKey("owned_sensors")) {
                JsonArray sensorIds = body.getJsonArray("owned_sensors");
                for (int i = 0; i < sensorIds.size(); i++) {
                    int sensorId = sensorIds.getInteger(i);
                    Sensor sensor = db.find(Sensor.class, sensorId);
                    if (sensor != null) {
                        person.getSensors().add(sensor);
                        sensor.getOwners().add(person);
                    }
                }
            }

            db.getTransaction().begin();
            db.persist(person);
            db.getTransaction().commit();

            event.json(new JsonObject().put("id", person.getId()));
        
        }
    }
}
