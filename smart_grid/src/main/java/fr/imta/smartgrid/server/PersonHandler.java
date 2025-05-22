package fr.imta.smartgrid.server;

import java.util.LinkedList;

import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Sensor;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class PersonHandler implements Handler<RoutingContext> {
    EntityManager db;

    public PersonHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {

        String route_called = event.currentRoute().getName();
        String meth = event.request().method().toString();

        if ("/person".equals(route_called) && "PUT".equals(meth))
        {
            JsonObject body = null;
            try {
                body = event.body().asJsonObject();;
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
                        }
                    }
                }

                db.getTransaction().begin();
                db.persist(person);
                db.getTransaction().commit();

                event.json(new JsonObject().put("id", person.getId()));
            }

        }
        else if ("/person/:id".equals(route_called))
        {
            switch (meth) {
                case "GET" -> {
                    int id = Integer.parseInt(event.pathParam("id"));
                    Person p = (Person) db.find(Person.class, id);
                    JsonObject result = new JsonObject();
                    result.put("id", id);
                    result.put("first_name", p.getFirstName());
                    result.put("last_name", p.getLastName());
                    result.put("grid", p.getGrid().getId());
                    LinkedList<Integer> l = new LinkedList<>();
                    for (Sensor s : p.getSensors()){
                        l.add(s.getId());
                    }
                    result.put("owned_sensors", l);
                    event.json(result);
                }

                case "POST" -> {
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
                case "DELETE" -> {
                    String idStr = event.pathParam("id");
                    int id = Integer.parseInt(idStr);

                    Person person = db.find(Person.class, id);
                    if (person == null) {
                        event.fail(404);
                    }

                    try {
                        db.getTransaction().begin();
                        db.remove(person);
                        db.getTransaction().commit();

                        event.response()
                            .setStatusCode(200)
                            .end();
                    } catch (Exception e) {
                        if (db.getTransaction().isActive()) {
                            db.getTransaction().rollback();
                        }
                        event.fail(500);
                    }

                }
                default -> {
                }
            }

        }
    }
}
