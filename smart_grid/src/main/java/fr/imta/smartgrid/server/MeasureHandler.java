package fr.imta.smartgrid.server;

import java.util.List;

import fr.imta.smartgrid.model.DataPoint;
import fr.imta.smartgrid.model.Measurement;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class MeasureHandler implements Handler<RoutingContext> {
    EntityManager db;

    public MeasureHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {

        String route_called = event.currentRoute().getName();

        if ("/measurement/:id".equals(route_called))
        {
            int id = Integer.parseInt(event.pathParam("id"));
            Measurement measure = (Measurement) db.find(Measurement.class, id);
            if (measure == null){
                event.fail(404);
            }
            else{
                event.json(measure.toJSON());
            }
        } 
        else if ("/measurement/:id/values".equals(route_called))
        {
            int fromInt;
            int toInt;
            String fromStr = event.request().getParam("from");
            if(fromStr != null){
                fromInt = Integer.parseInt(fromStr);
            }else{
                fromInt = 0;
            }


            String toStr = event.request().getParam("to");
            if(toStr != null){
                toInt = Integer.parseInt(toStr);
            }else{
                toInt = 2147483646;
            }      

            int id = Integer.parseInt(event.pathParam("id"));
            int sensorId = ((Measurement)db.find(Measurement.class, id)).getSensor().getId();

            List<Integer> dataIds = db
                    .createNativeQuery(
                        "SELECT id " +
                        " FROM DataPoint " +
                        " WHERE measurement = ?" +
                        " AND timestamp >= ?" +
                        " AND timestamp <= ?")                
                    .setParameter(1, id)   
                    .setParameter(2, fromInt)   
                    .setParameter(3, toInt)   
                    .getResultList();
            
            JsonObject ret = new JsonObject();
            
            ret.put("sensor_id", sensorId);
            ret.put("measurement_id", id);

            JsonArray values = new JsonArray();

            for(Integer dataId : dataIds){
                DataPoint dp = (DataPoint)db.find(DataPoint.class, dataId);
                values.add(dp.toJson());
            }
            ret.put("values", values);

            event.json(ret);
        }

    }
}
