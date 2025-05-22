package fr.imta.smartgrid.server;

import java.util.HashMap;
import java.util.Map;

import static org.eclipse.persistence.config.PersistenceUnitProperties.CONNECTION_POOL_MIN;
import static org.eclipse.persistence.config.PersistenceUnitProperties.LOGGING_LEVEL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.TARGET_SERVER;
import org.eclipse.persistence.config.TargetServer;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;

public class VertxServer {
    private final Vertx vertx;
    private final EntityManager db; // database object

    public VertxServer() {
        this.vertx = Vertx.vertx();

        // setup database connexion
        Map<String, String> properties = new HashMap<>();

        properties.put(LOGGING_LEVEL, "FINE");
        properties.put(CONNECTION_POOL_MIN, "1");

        properties.put(TARGET_SERVER, TargetServer.None);

        var emf = Persistence.createEntityManagerFactory("smart-grid", properties);
        db = emf.createEntityManager();
    }

    public void start() {
        Router router = Router.router(vertx);

        // register a handler that process payload of HTTP requests for us
        router.route().handler(BodyHandler.create());
        
        // windturbine
        WTHandler WTHandler = new WTHandler(this.db);
        router.route("/ingress").handler(WTHandler);
        router.post("/ingress/windturbine").handler(WTHandler);
        
        // Grids
        GridsHandler GridsHandler = new GridsHandler(this.db);
        router.get("/grids").handler(GridsHandler);

        // Grid
        GridHandler GridHandler = new GridHandler(this.db);
        router.get("/grid/:id").handler(GridHandler);
        router.get("/grid/:id/production").handler(GridHandler);
        router.get("/grid/:id/consumption").handler(GridHandler);

        //Persons
        PersonsHandler PersonsHandler = new PersonsHandler(this.db);
        router.get("/persons").handler(PersonsHandler);

        //Person
        PersonHandler PersonHandler = new PersonHandler(this.db);
        router.put("/person").handler(PersonHandler);
        router.get("/person/:id").handler(PersonHandler);
        router.post("/person/:id").handler(PersonHandler);
        router.delete("/person/:id").handler(PersonHandler);
        
        //Sensor
        SensorHandler SensorHandler = new SensorHandler(this.db);
        router.get("/sensor/:id").handler(SensorHandler);
        router.post("/sensor/:id").handler(SensorHandler);

        //Sensors
        SensorsHandler SensorsHandler = new SensorsHandler(this.db);
        router.get("/sensors/:kind").handler(SensorsHandler);

        //Consumers
        ConsumersHandler ConsumersHandler = new ConsumersHandler(this.db);
        router.get("/consumers").handler(ConsumersHandler);

        //Producers
        ProducersHandler ProducersHandler = new ProducersHandler(this.db);
        router.get("/producers").handler(ProducersHandler);

        //Measurement
        MeasureHandler MeasureHandler = new MeasureHandler(this.db);
        router.get("/measurement/:id").handler(MeasureHandler);
        router.get("/measurement/:id/values").handler(MeasureHandler);

        
        // start the server
        vertx.createHttpServer().requestHandler(router).listen(8080);
    }

    public static void main(String[] args) {
        new VertxServer().start();
    }
}
