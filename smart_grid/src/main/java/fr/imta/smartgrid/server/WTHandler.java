package fr.imta.smartgrid.server;

import fr.imta.smartgrid.model.DataPoint;
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

        Double speedRaw = data.getDouble("speed");
        Double powerRaw = data.getDouble("power");

        WindTurbine wt = db.find(WindTurbine.class, wtId);
        if (wt == null) {
            event.fail(404);
        }
        

        try {
            db.getTransaction().begin();

            // Création du DataPoint
            DataPoint dp = new DataPoint();
            dp.setWindTurbine(wt);
            dp.setTimestamp(timestamp);
            dp.setSpeed(speed);
            dp.setPower(power);
            db.persist(dp);

            // Calculer l'énergie (W * 60s = joules)
            double deltaEnergy = power * 60;
            wt.setTotalEnergyProduced(wt.getTotalEnergyProduced() + deltaEnergy);
            db.merge(wt);

            db.getTransaction().commit();
        } catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            event.response()
                .setStatusCode(500)
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                        .put("error", "Erreur lors de l'enregistrement des données")
                        .encodePrettily());
            return;
        }

        // 7) Répondre succès
        event.response()
            .setStatusCode(200)
            .putHeader("Content-Type", "application/json")
            .end(new JsonObject()
                    .put("status", "success")
                    .encodePrettily());
    }
}
