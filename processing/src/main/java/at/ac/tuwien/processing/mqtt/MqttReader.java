package at.ac.tuwien.processing.mqtt;

import at.ac.tuwien.processing.dto.Data;
import at.ac.tuwien.processing.influxdb.InfluxDbClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttReader implements MqttCallbackExtended {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttReader.class);

    private String topic;
    private String mqttHost;
    private Gson gson;

    private MqttClient client;
    private InfluxDbClient influxDbClient;

    public MqttReader(String mqttHost, String topic, InfluxDbClient influxDbClient) {
        this.mqttHost = mqttHost;
        this.topic = topic;
        this.gson = new GsonBuilder().create();
        this.influxDbClient = influxDbClient;
    }

    public void collectData() {

        while (client == null) {
            LOGGER.info("Trying to connect to mqtt host {} on topic '{}'", mqttHost, topic);
            try {
                client = new MqttClient(mqttHost, MqttClient.generateClientId());

                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(true);
                options.setAutomaticReconnect(true);
                client.connect(options);
                client.setCallback(this);
                client.subscribe(topic);
                LOGGER.info("Successfully subscribed to topic '{}'", topic);
            } catch (MqttException e) {
                LOGGER.error("Error connecting to Mqtt Broker: ", e);
                client = null;
            }
        }
    }

    public void connectionLost(Throwable throwable) {
        try {
            LOGGER.debug("Trying to reconnect");
            client.reconnect();
            LOGGER.debug("Successfully reconnected");
        } catch (MqttException e) {
            LOGGER.error("Error on reconnection: ", e);
        }
    }

    public void messageArrived(String s, MqttMessage mqttMessage) {
        Data data = gson.fromJson(new String(mqttMessage.getPayload()), Data.class);
        LOGGER.info("Collected data: {}", data);
        influxDbClient.writeMeasurement(s, data);
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    public void connectComplete(boolean b, String s) {
        LOGGER.info("Connect complete: {}, {}", b, s);
        try {
            client.subscribe(topic);
        } catch (MqttException e) {
            LOGGER.error("Error re-subscribing to Mqtt Broker: ", e);
        }
    }
}
