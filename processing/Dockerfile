FROM java:8
WORKDIR /
ADD target/processing-stage-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
ENV INFLUXDB_HOST "http://localhost:8086"
ENV INFLUXDB_DB "sensors-db"
ENV INFLUXDB_USER "root"
ENV INFLUXDB_PASSWORD "root"
ENV MQTT_HOST tcp://localhost:1883
ENV MQTT_TOPICS "host/+/sensor/#"

CMD java -jar app.jar $MQTT_HOST $MQTT_TOPICS $INFLUXDB_HOST $INFLUXDB_DB $INFLUXDB_USER $INFLUXDB_PASSWORD
