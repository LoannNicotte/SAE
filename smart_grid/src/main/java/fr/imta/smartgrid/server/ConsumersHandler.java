package fr.imta.smartgrid.server;

import java.util.List;

import fr.imta.smartgrid.model.EVCharger;
import fr.imta.smartgrid.model.Sensor;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class ConsumersHandler implements Handler<RoutingContext> {
    EntityManager db;

    public ConsumersHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {

        List<Integer> sensorIds = db
                    .createNativeQuery(
                        "SELECT id " +
                        " FROM sensor " +
                        " WHERE dtype = ?")                
                    .setParameter(1, "EVCharger")   
                    .getResultList();

        JsonArray consumers = new JsonArray();
        
        for(Integer sensorId : sensorIds){
            Sensor sensor = (Sensor) db.find(Sensor.class, sensorId);
            if (sensor == null){
                event.fail(404);
            }
            else{
                String dType = sensor.getdType();

                if(null == dType){
                    event.fail(404);
                }
                else switch (dType) {
                    case "EVCharger" -> {
                        EVCharger EVC = (EVCharger) db.find(EVCharger.class, sensorId);
                        consumers.add(EVC.toJSON());
                    }
                    default -> event.fail(404);
                }
            }
        }
        event.json(consumers);

    }
}
