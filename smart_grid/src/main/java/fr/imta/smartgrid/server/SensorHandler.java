package fr.imta.smartgrid.server;

import fr.imta.smartgrid.model.EVCharger;
import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Producer;
import fr.imta.smartgrid.model.Sensor;
import fr.imta.smartgrid.model.SolarPanel;
import fr.imta.smartgrid.model.WindTurbine;
import fr.imta.smartgrid.model.Consumer;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
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


        }else if ("POST".equals(meth)){

            int id = Integer.parseInt(event.pathParam("id"));

            Sensor sensor = db.find(Sensor.class, id);
            if (sensor == null) {
                event.fail(404);  
            }

            JsonObject body = null;
            try {
                body = event.body().asJsonObject();
            } catch (Exception e) {
                event.fail(500);
            }
            System.out.println(body.toString());

            Object nameRaw = body.getValue("name");
            if (nameRaw != null) {
                sensor.setName((String) nameRaw);
            }

            Object descRaw = body.getValue("description");
            if (descRaw != null) {
                sensor.setDescription((String) descRaw);
            }


            Object ownersRaw = body.getValue("owners");
            if (ownersRaw != null) {
                JsonArray arr = (JsonArray) ownersRaw;
                sensor.getOwners().clear();
                for (Object o : arr) {
                    int personId = (Integer) o;
                    Person p = db.find(Person.class, personId);
                    if (p != null) {
                        sensor.getOwners().add(p);
                    }
                }
            }

            if (sensor instanceof Producer) {
                Producer prod = (Producer) sensor;
                Object psRaw = body.getValue("power_source");
                if (psRaw != null) {
                    prod.setPowerSource((String) psRaw);
                }
                
                if (prod instanceof SolarPanel) {
                    SolarPanel sp = (SolarPanel) prod;
                    Object effRaw = body.getValue("efficiency");
                    if (effRaw != null) {
                        sp.setEfficiency(((Double) effRaw).floatValue());
                    }
                }
                
                if (prod instanceof WindTurbine) {
                    WindTurbine wt = (WindTurbine) prod;
                    Object hRaw = body.getValue("height");
                    if (hRaw != null) {
                        wt.setHeight((Double) hRaw);
                    }
                    Object blRaw = body.getValue("blade_length");
                    if (blRaw != null) {
                        wt.setBladeLength((Double) blRaw);
                    }
                }
            }

            if (sensor instanceof Consumer) {
                Consumer cons = (Consumer) sensor;
                Object mpRaw = body.getValue("max_power");
                if (mpRaw != null) {
                    cons.setMaxPower((Double) mpRaw);
                }
                
                if (cons instanceof EVCharger) {
                    EVCharger ev = (EVCharger) cons;
                    Object typeRaw = body.getValue("type");
                    if (typeRaw != null) {
                        ev.setType((String) typeRaw);
                    }
                    Object voltRaw = body.getValue("voltage");
                    if (voltRaw != null) {
                        ev.setVoltage((int) voltRaw);
                    }
                    Object ampRaw = body.getValue("maxAmp");
                    if (ampRaw != null) {
                        ev.setMaxAmp((int) ampRaw);
                    }
                }
            }

            try {
                db.getTransaction().begin();
                db.merge(sensor);
                db.getTransaction().commit();
            } catch (Exception e) {
                if (db.getTransaction().isActive()) {
                    db.getTransaction().rollback();
                }
                event.fail(500);
            }

            event.response()
                .setStatusCode(200)
                .end();
        }  

    }
}
