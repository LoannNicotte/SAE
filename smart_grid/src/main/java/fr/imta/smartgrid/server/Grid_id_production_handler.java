package fr.imta.smartgrid.server;

import java.util.List;

import fr.imta.smartgrid.model.DataPoint;
import fr.imta.smartgrid.model.Measurement;
import fr.imta.smartgrid.model.Sensor;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import jakarta.persistence.EntityManager;

public class Grid_id_production_handler implements Handler<RoutingContext> {
    EntityManager db;

    public Grid_id_production_handler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {

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
            event.json(Double.toString(totalEnergyProduced));

    }
}