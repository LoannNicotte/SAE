package fr.imta.smartgrid.server;

import fr.imta.smartgrid.model.DataPoint;
import fr.imta.smartgrid.model.Measurement;
import fr.imta.smartgrid.model.WindTurbine;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class WTHandler implements Handler<RoutingContext> {
    EntityManager db;

    public WTHandler(EntityManager db) {
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

            Measurement measureSpeed = (Measurement) db
                    .createNativeQuery(
                        "SELECT id " +
                        " FROM measurement " +
                        " WHERE sensor = ?" +
                        " AND name = ?", 
                        Measurement.class)                
                    .setParameter(1, wtId)   
                    .setParameter(2, "speed")    
                    .getSingleResult();

            DataPoint dataSpeed = new DataPoint();
            dataSpeed.setValue(speed);
            dataSpeed.setTimestamp(timestamp);
            dataSpeed.setMeasurement(measureSpeed);

            db.getTransaction().begin();
            db.persist(dataSpeed);
            db.getTransaction().commit();

            Measurement measurePower = (Measurement) db
                    .createNativeQuery(
                        "SELECT id " +
                        " FROM measurement " +
                        " WHERE sensor = ?" +
                        " AND name = ?", 
                        Measurement.class)                
                    .setParameter(1, wtId)   
                    .setParameter(2, "power")    
                    .getSingleResult();

            DataPoint dataPower = new DataPoint();
            dataPower.setValue(power);
            dataPower.setTimestamp(timestamp);
            dataPower.setMeasurement(measurePower);

            db.getTransaction().begin();
            db.persist(dataPower);
            db.getTransaction().commit();

            Measurement measureEnergy = (Measurement) db
                    .createNativeQuery(
                        "SELECT id " +
                        " FROM measurement " +
                        " WHERE sensor = ?" +
                        " AND name = ?", 
                        Measurement.class)                
                    .setParameter(1, wtId)   
                    .setParameter(2, "total_energy_produced")    
                    .getSingleResult();

            System.out.println("l'id de la meusure est:");
            System.out.print(measureEnergy.getId());

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

            db.getTransaction().begin();
            db.persist(dataEnergy);
            db.getTransaction().commit();

            event.json(new JsonObject().put("status", "success"));
        }
    }
}
