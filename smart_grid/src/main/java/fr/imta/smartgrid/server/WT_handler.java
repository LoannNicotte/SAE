package fr.imta.smartgrid.server;

import fr.imta.smartgrid.model.DataPoint;
import fr.imta.smartgrid.model.Measurement;
import fr.imta.smartgrid.model.WindTurbine;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class WT_handler implements Handler<RoutingContext> {
    EntityManager db;

    public WT_handler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {

        JsonObject body = event.body().asJsonObject();

        int wtId = body.getInteger("windturbine");

        long timestamp = (long) body.getLong("timestamp");

        JsonObject data = body.getJsonObject("data");

        Double speed = data.getDouble("speed");
        Double power = data.getDouble("power");

        WindTurbine wt = db.find(WindTurbine.class, wtId);
        if (wt == null) {
            event.fail(404);
        }
        else{

            // Speed
            Measurement measureSpeed = findMeasurement(wtId, "speed");
            DataPoint dataSpeed = new DataPoint();
            dataSpeed.setValue(speed);
            dataSpeed.setTimestamp(timestamp);
            dataSpeed.setMeasurement(measureSpeed);

            // Power
            Measurement measurePower = findMeasurement(wtId, "power");
            DataPoint dataPower = new DataPoint();
            dataPower.setValue(power);
            dataPower.setTimestamp(timestamp);
            dataPower.setMeasurement(measurePower);

            // Energy
            Measurement measureEnergy = findMeasurement(wtId, "total_energy_produced");
            Double lastEnergy = (Double) db
                    .createNativeQuery(
                        "SELECT MAX (value) " +
                        " FROM datapoint " +
                        " WHERE measurement = ?")                 
                    .setParameter(1, measureEnergy.getId())    
                    .getSingleResult();

            DataPoint dataEnergy = new DataPoint();
            dataEnergy.setValue(power*60 + lastEnergy);
            dataEnergy.setTimestamp(timestamp);
            dataEnergy.setMeasurement(measureEnergy);

            // Ajout dans la DB
            db.getTransaction().begin();
            db.persist(dataSpeed);
            db.persist(dataPower);
            db.persist(dataEnergy);
            db.getTransaction().commit();

            event.json(new JsonObject().put("status", "success"));
        }
    }

    private Measurement findMeasurement(int sensorId, String name) {
        int measureId = (int) db
            .createNativeQuery(
                "SELECT id" +
                " FROM measurement" +
                " WHERE sensor = ?" +
                " AND name = ?")                
            .setParameter(1, sensorId)   
            .setParameter(2, name)    
            .getSingleResult();
        
        return db.find(Measurement.class, measureId);

    }
}