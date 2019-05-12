package at.ac.tuwien.processing;

import at.ac.tuwien.processing.influxdb.InfluxDbClient;
import at.ac.tuwien.processing.mqtt.MqttReader;

public class Main {

    public static void main(String[] args) {
        InfluxDbClient influxDbClient = new InfluxDbClient(args[2], args[3], args[4], args[5]);
        MqttReader reader = new MqttReader(args[0], args[1], influxDbClient);
        reader.collectData();
    }
}
