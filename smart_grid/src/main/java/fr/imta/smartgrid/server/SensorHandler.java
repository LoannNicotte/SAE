package fr.imta.smartgrid.server;

import fr.imta.smartgrid.model.EVCharger;
import fr.imta.smartgrid.model.Sensor;
import fr.imta.smartgrid.model.SolarPanel;
import fr.imta.smartgrid.model.WindTurbine;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

public class SensorHandler implements Handler<RoutingContext> {
    EntityManager db;

    public SensorHandler(EntityManager db) {
        this.db = db;
    }

    @Override
    public void handle(RoutingContext event) {

        String meth = event.request().method().toString();

        if ("GET".equals(meth))
        {
            
            int id = Integer.parseInt(event.pathParam("id"));
            Sensor sensor = (Sensor) db.find(Sensor.class, id);
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
                        WindTurbine windT = (WindTurbine) db.find(WindTurbine.class, id);
                        event.json(windT.toJSON());
                    }
                    case "SolarPanel" -> {
                        SolarPanel solarP = (SolarPanel) db.find(SolarPanel.class, id);
                        event.json(solarP.toJSON());
                    }
                    case "EVCharger" -> {
                        EVCharger EVC = (EVCharger) db.find(EVCharger.class, id);
                        event.json(EVC.toJSON());
                    }
                    default -> {
                        event.fail(404);
                    }
                }
            }


        }else if ("Post".equals(meth)){

        }  

    }
}
