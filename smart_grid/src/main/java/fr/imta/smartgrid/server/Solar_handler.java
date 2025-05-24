package fr.imta.smartgrid.server;

import io.vertx.core.Handler;
import io.vertx.core.datagram.DatagramPacket;
import java.nio.charset.StandardCharsets;
import jakarta.persistence.EntityManager;
import fr.imta.smartgrid.model.Measurement;
import fr.imta.smartgrid.model.DataPoint;

public class Solar_handler implements Handler<DatagramPacket> {
  private final EntityManager db;

  public Solar_handler(EntityManager db) {
    this.db = db;
  }

  @Override
  public void handle(DatagramPacket packet) {
    String msg = packet.data()
                       .toString(StandardCharsets.UTF_8)
                       .trim();

    System.out.println("\n" + msg + "\n");


    String[] parts = msg.split(":");

    int spId       = Integer.parseInt(parts[0]);
    double temp    = Double.parseDouble(parts[1]);
    double power   = Double.parseDouble(parts[2]);
    long timestamp = Long.parseLong(parts[3]);

    // Temperature
    Measurement measureTemp = findMeasurement(spId, "temperature");
    DataPoint dataTemp = new DataPoint();
    dataTemp.setMeasurement(measureTemp);
    dataTemp.setTimestamp(timestamp);
    dataTemp.setValue(temp);
    
    Measurement measurePower = findMeasurement(spId, "power");
    DataPoint dataPower = new DataPoint();
    dataPower.setMeasurement(measurePower);
    dataPower.setTimestamp(timestamp);
    dataPower.setValue(power);

    Measurement measureEnergy = findMeasurement(spId, "total_energy_produced");
    Double lastEnergy = (Double) db
            .createNativeQuery(
                "SELECT MAX(value) "  +
                "FROM datapoint " + 
                "WHERE measurement = ?")
            .setParameter(1, measureEnergy.getId())
            .getSingleResult();

    DataPoint dataEnergy = new DataPoint();
    dataEnergy.setMeasurement(measureEnergy);
    dataEnergy.setTimestamp(timestamp);
    dataEnergy.setValue(lastEnergy + power * 60);

    db.getTransaction().begin();
    db.persist(dataTemp);
    db.persist(dataPower);
    db.persist(dataEnergy);
    db.getTransaction().commit();
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