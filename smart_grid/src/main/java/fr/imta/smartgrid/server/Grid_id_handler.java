package fr.imta.smartgrid.server;

import java.util.LinkedList;

import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Sensor;
import fr.imta.smartgrid.model.Person;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import jakarta.persistence.EntityManager;

public class Grid_id_handler implements Handler<RoutingContext> {
    EntityManager db;

    public Grid_id_handler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {

        int id = Integer.parseInt(event.pathParam("id"));
        Grid grid = (Grid) db.find(Grid.class, id);

        JsonObject gridJson = new JsonObject();

        gridJson.put("id", id);
        gridJson.put("name", grid.getName());
        gridJson.put("description", grid.getDescription());

        LinkedList<Integer> users = new LinkedList<>();
        for (Person s : grid.getPersons()){
            users.add(s.getId());
        }   
        gridJson.put("users", users);

        LinkedList<Integer> sensors = new LinkedList<>();
        for (Sensor s : grid.getSensors()){
            sensors.add(s.getId());
        }
        gridJson.put("sensors", sensors);

        event.json(gridJson);

    }
}
