package fr.imta.smartgrid.model;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "sensor")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Sensor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "grid")
    private Grid grid;

    @ManyToMany(mappedBy = "sensors")
    private List<Person> owners = new ArrayList<>();

    @OneToMany(mappedBy = "sensor")
    private List<Measurement> measurements = new ArrayList<>();

    private String dType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Grid getGrid() {
        return grid;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    public String getdType(){
        return dType;
    }

    public void setdType(String dType){
        this.dType = dType;
    }

    public List<Person> getOwners() {
        return owners;
    }

    public void setOwners(List<Person> owners) {
        this.owners = owners;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement> measurements) {
        this.measurements = measurements;
    }

    public JsonObject toJSON() {
        JsonObject res = new JsonObject();
        
        res.put("id", getId());
        res.put("name", getName());
        res.put("description", getDescription());
        res.put("kind", getdType());
        res.put("grid", getGrid().getId());

        JsonArray measuresArray = new JsonArray();
        for (Measurement m : getMeasurements()) {
            measuresArray.add(m.getId());
        }
        res.put("available_measurements", measuresArray);

        // Sérialisation des propriétaires
        JsonArray ownersArray = new JsonArray();
        for (Person p : getOwners()) {
            ownersArray.add(p.getId());
        }
        res.put("owners", ownersArray);
        
        return res;
    }
   
}
