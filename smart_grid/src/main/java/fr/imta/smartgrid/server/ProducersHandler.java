package fr.imta.smartgrid.server;


import java.util.List;

import fr.imta.smartgrid.model.Sensor;
import fr.imta.smartgrid.model.SolarPanel;
import fr.imta.smartgrid.model.WindTurbine;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class ProducersHandler implements Handler<RoutingContext> {
    EntityManager db;

    public ProducersHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {
        List<Integer> sensorIds = db
                    .createNativeQuery(
                        "SELECT id " +
                        " FROM sensor " +
                        " WHERE dtype = ? OR dtype = ?")                
                    .setParameter(1, "WindTurbine")  
                    .setParameter(2, "SolarPanel")    
                    .getResultList();

        JsonArray producers = new JsonArray();
        
        for(Integer sensorId : sensorIds){
            Sensor sensor = (Sensor) db.find(Sensor.class, sensorId);
            if (sensor == null){
                event.fail(404);
            }
            else{
                String dType = sensor.getdType();

                if(dType == null){
                    event.fail(404);
                }
                else switch (dType) {
                    case "WindTurbine" -> {
                        WindTurbine windT = (WindTurbine) db.find(WindTurbine.class, sensorId);
                        producers.add(windT.toJSON());
                    }
                    case "SolarPanel" -> {
                        SolarPanel solarP = (SolarPanel) db.find(SolarPanel.class, sensorId);
                        producers.add(solarP.toJSON());
                    }
                    default -> {
                        event.fail(404);
                    }
                }
            }
        }
        event.json(producers);

    }
}
