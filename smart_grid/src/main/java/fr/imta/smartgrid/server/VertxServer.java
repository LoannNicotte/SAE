package fr.imta.smartgrid.server;

import java.util.HashMap;
import java.util.Map;

import static org.eclipse.persistence.config.PersistenceUnitProperties.CONNECTION_POOL_MIN;
import static org.eclipse.persistence.config.PersistenceUnitProperties.LOGGING_LEVEL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.TARGET_SERVER;
import org.eclipse.persistence.config.TargetServer;

import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramSocketOptions;
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
        
        // WT_handler
        router.post("/ingress/windturbine").handler(new WT_handler(this.db));
        
        // Grids_handler
        router.get("/grids").handler(new Grids_handler(this.db));

        // Grid_id_handler
        router.get("/grid/:id").handler(new Grid_id_handler(this.db));

        // Grid_id_production_handler
        router.get("/grid/:id/production").handler(new Grid_id_production_handler(this.db));

        // Grid_id_consumption_handler
        router.get("/grid/:id/consumption").handler(new Grid_id_consumption_handler(this.db));

        // Persons_handler
        router.get("/persons").handler(new Persons_handler(this.db));

        // Person_id_delete_handler
        router.put("/person").handler(new Person_id_delete_handler(this.db));

        //Person_id_post_handler
        router.put("/person").handler(new Person_id_post_handler(this.db));

        // Person_id_get_handler

        router.put("/person").handler(new Person_id_get_handler(this.db));

        // Person_handler
        router.put("/person").handler(new Person_handler(this.db));

        // Sensor_id_get_handler
        router.get("/sensor/:id").handler(new Sensor_id_get_handler(this.db));

        // Sensor_id_post_handler
        router.post("/sensor/:id").handler(new Sensor_id_post_handler(this.db));

        // Sensors_handler
        router.get("/sensors/:kind").handler(new Sensors_handler(this.db));

        // Consumers_handler
        router.get("/consumers").handler(new Consumers_handler(this.db));

        // Producers_handler
        router.get("/producers").handler(new Producers_handler(this.db));

        // Measure_id_handler
        router.get("/measurement/:id").handler(new Measure_id_handler(this.db));

        // Measure_id_value_handler
        router.get("/measurement/:id").handler(new Measure_id_value_handler(this.db));

        // UDP
        vertx.createDatagramSocket(new DatagramSocketOptions())
             .handler(new Solar_handler(db))
             .listen(12345, "0.0.0.0");

        
        // start the server
        vertx.createHttpServer().requestHandler(router).listen(8080);
    }

    public static void main(String[] args) {
        new VertxServer().start();
    }
}
