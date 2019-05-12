package at.ac.tuwien.processing.influxdb;

import at.ac.tuwien.processing.dto.Data;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class InfluxDbClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxDbClient.class);

    private InfluxDB influxDB;

    private String host;
    private String database;
    private String user;
    private String password;

    public InfluxDbClient(String host, String database, String username, String password) {
        this.host = host;
        this.database = database;
        this.user = username;
        this.password = password;
    }

    public void writeMeasurement(String measurement, Data data) {
        LOGGER.info("Writing to measurement {}", measurement);
        InfluxDB influxDB = getInfluxDb();
        Point point = Point.measurement(getSensor(measurement))
                .tag("host", getHost(measurement))
                .time(data.getTimestamp(), TimeUnit.SECONDS)
                .addField("value", data.getValue())
                .build();
        try {
            influxDB.write(point);
            LOGGER.info("Successfully written");
        } catch (Exception e) {
            LOGGER.error("error on writing to influxdb", e);
        }
    }

    private synchronized InfluxDB getInfluxDb() {
        if (influxDB == null) {
            influxDB = InfluxDBFactory.connect(host, user, password);
            influxDB.query(new Query(String.format("CREATE DATABASE \"%s\"", database), database));
            influxDB.setDatabase(database);
        }
        return influxDB;
    }

    private String getHost(String s){
        String[] h = s.split("/");
        return h.length > 0 ? h[1] : s;
    }

    private String getSensor(String s){
        String[] h = s.split("/");
        return h.length > 3 ? h[3] : s;
    }
}
