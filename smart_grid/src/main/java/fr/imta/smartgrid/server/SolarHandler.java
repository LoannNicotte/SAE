package fr.imta.smartgrid.server;

import io.vertx.core.Handler;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.json.JsonObject;
import java.nio.charset.StandardCharsets;
import jakarta.persistence.EntityManager;
import fr.imta.smartgrid.model.SolarPanel;
import fr.imta.smartgrid.model.Measurement;
import fr.imta.smartgrid.model.DataPoint;

public class SolarHandler implements Handler<DatagramPacket> {
  private final EntityManager db;

  public SolarHandler(EntityManager db) {
    this.db = db;
  }

  @Override
  public void handle(DatagramPacket packet) {
    String msg = packet.data()
                       .toString(StandardCharsets.UTF_8)
                       .trim();
    // Format attendu : "id:temperature:power:timestamp"
    String[] parts = msg.split(":");
    if (parts.length != 4) {
      System.err.println("Invalid SolarPanel packet: " + msg);
      return;
    }

    try {
      int spId       = Integer.parseInt(parts[0]);
      double temp    = Double.parseDouble(parts[1]);
      double power   = Double.parseDouble(parts[2]);
      long timestamp = Long.parseLong(parts[3]);

      SolarPanel sp = db.find(SolarPanel.class, spId);
      if (sp == null) {
        System.err.println("SolarPanel id=" + spId + " not found");
        return;
      }

      db.getTransaction().begin();

      // Température
      Measurement mTemp = findMeasurement(spId, "temperature");
      DataPoint dpTemp = new DataPoint();
      dpTemp.setMeasurement(mTemp);
      dpTemp.setTimestamp(timestamp);
      dpTemp.setValue(temp);
      db.persist(dpTemp);

      // Puissance
      Measurement mPower = findMeasurement(spId, "power");
      DataPoint dpPower = new DataPoint();
      dpPower.setMeasurement(mPower);
      dpPower.setTimestamp(timestamp);
      dpPower.setValue(power);
      db.persist(dpPower);

      // Énergie totale (J = W × 60s)
      Measurement mEnergy = findMeasurement(spId, "total_energy_produced");
      Double lastEnergy = (Double) db.createNativeQuery(
          "SELECT MAX(value) FROM datapoint WHERE measurement = ?")
        .setParameter(1, mEnergy.getId())
        .getSingleResult();
      if (lastEnergy == null) lastEnergy = 0.0;

      DataPoint dpEnergy = new DataPoint();
      dpEnergy.setMeasurement(mEnergy);
      dpEnergy.setTimestamp(timestamp);
      dpEnergy.setValue(lastEnergy + power * 60);
      db.persist(dpEnergy);

      db.getTransaction().commit();
    } catch (Exception e) {
      if (db.getTransaction().isActive()) db.getTransaction().rollback();
      System.err.println("Error processing SolarPanel packet: " + e.getMessage());
    }
  }

  private Measurement findMeasurement(int sensorId, String name) {
    return db.createQuery(
        "SELECT m FROM Measurement m WHERE m.sensor.id = ? AND m.name = ?",
        Measurement.class)
      .setParameter(1, sensorId)
      .setParameter(2, name)
      .getSingleResult();
  }
}