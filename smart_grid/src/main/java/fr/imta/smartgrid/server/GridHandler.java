package fr.imta.smartgrid.server;

import java.util.LinkedList;
import java.util.List;

import fr.imta.smartgrid.model.DataPoint;
import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Measurement;
import fr.imta.smartgrid.model.Sensor;
import fr.imta.smartgrid.model.Person;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class GridHandler implements Handler<RoutingContext> {
    EntityManager db;

    public GridHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {

        String route_called = event.currentRoute().getName();
        String meth = event.request().method().toString();

        if ("/grid/:id/consumption".equals(route_called) && "GET".equals(meth))
        {
            int id = Integer.parseInt(event.pathParam("id"));
            List<Integer> sensorIds = db
                    .createNativeQuery(
                        "SELECT id " +
                        "  FROM sensor " +
                        " WHERE grid   = ? " +
                        " AND dtype = ?"
                    )
                    .setParameter(1, id)                  
                    .setParameter(2, "EVCharger")     
                    .getResultList();

            double totalEnergyConsumption = 0;
            for (Integer sensorId : sensorIds) {
                Sensor sensor = (Sensor) db.find(Sensor.class, sensorId);
                List<Measurement> measures = sensor.getMeasurements();
                for (Measurement measure : measures) {
                    if("power".equals(measure.getName())){
                        List<Integer> DataIds  = db
                            .createNativeQuery("SELECT id FROM datapoint " +
                                               "WHERE measurement = ? ")
                            .setParameter(1, measure.getId())
                            .getResultList();

                        for(Integer DataId : DataIds) {
                            DataPoint Data = (DataPoint) db.find(DataPoint.class, DataId);
                            totalEnergyConsumption += Data.getValue() * 60;
                        }
                    }
                }
            }
            event.end(Double.toString(totalEnergyConsumption));
        }

        else if ("/grid/:id/production".equals(route_called) && "GET".equals(meth))
        {
            int id = Integer.parseInt(event.pathParam("id"));
            List<Integer> sensorIds = db
                    .createNativeQuery(
                        "SELECT id " +
                        "  FROM sensor " +
                        " WHERE grid   = ? " +
                        "   AND (dtype = ? OR dtype = ?)"
                    )
                    .setParameter(1, id)                  
                    .setParameter(2, "WindTurbine")  
                    .setParameter(3, "SolarPanel")    
                    .getResultList();

            double totalEnergyProduced = 0;
            for (Integer sensorId : sensorIds) {
                Sensor sensor = (Sensor) db.find(Sensor.class, sensorId);
                List<Measurement> measures = sensor.getMeasurements();
                for (Measurement measure : measures) {
                    if("power".equals(measure.getName())){
                        List<Integer> DataIds  = db
                            .createNativeQuery("SELECT id FROM datapoint " +
                                               "WHERE measurement = ? ")
                            .setParameter(1, measure.getId())
                            .getResultList();

                        for(Integer DataId : DataIds) {
                            DataPoint Data = (DataPoint) db.find(DataPoint.class, DataId);
                            totalEnergyProduced += Data.getValue() * 60;
                        }
                    }
                }
            }
            event.end(Double.toString(totalEnergyProduced));
            

        }else if ("/grid/:id".equals(route_called))
        {
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
}
