# How to run

## Starting without kubernetes
To run the implementation locally without kubernetes simply run `docker-compose up --build`. 
Grafana should be available at `localhost:3000` after startup. Without kubernetes no failover mechanism exists.

## Starting locally on minikube
1. Install minikube and kubectl first (e.g. see https://kubernetes.io/docs/tasks/tools/install-minikube/)
2. Start minikube
    ```
    minikube start
    ```
3. Create minikube pods, from within the kubernetes folder of the project
    ```
    kubectl create -f kubernetes/deployment.yml
    ```
4. Wait for pods to be started, get status using
    ```
    kubectl get pods
    ```
6. Expose the internal ports of minikube
    ```
    kubectl expose pod <pod-name> --type=NodePort
    ```

Exposed services can be shown with, which should contain the service sensors  
```
kubectl get svc
```

### Configurations in minikube
The output shows the ports to be used when testing locally. All internal ports are exposed on external ports, e.g. the grafana port 3000 is exposed to 30681 or some similar port (output format: 3000:30681) Those parts are not static but may be different on every exposure. 
All ports stated here might not be useable in a concrete situations.
The internal/original ports are: Grafana: `3000`, Influx: `8086`, MQTT: `1883`

The IP of minikube can be found with 
```
minikube ip
``` 
Using this IP in combination with the exposed ports the application can be opened e.g. 192.168.99.100:30681 for grafana.  
Since the default ports can not be used anymore the port and the IP of the influx db have to be changed in grafana.
This can be changed by selecting the already present data source called "InfluxDB" and changing the URL to http://192.168.99.100:32766 where 32766 is the exposed port for the InfluxDB. More detailed descriptions about changing data source of grafana as well as importing dashboards can be found below.

**Note:** The minikube IP also has to be used for starting the sensors when testing locally with minikube.

### Failover in minikube
To test the failover service, all parts of the cluster can be killed from within minikube using docker. This can be done with 
```
minikube ssh
docker container ls
docker container kill <container-id>
```

The status of the containers can be shown on the minikube dashboard. Open with command
```
minikube dashboard
```

## Starting in gcloud kubernetes

`kubectl` and `gcloud` have to be installed and configured to be callable in a shell.
The Application can be deployed on google cloud using:
```
gcloud config set project <project-name>
gcloud config set <compute-zone>
gcloud container clusters get-credentials <clusted-name>
kubectl create -f kubernetes/deployment.yml
```

Afterwards the ports were exposed using

`kubectl expose pods sensors --type=LoadBalancer`

In our case at least two vCPUs were necessary to use in Google Cloud, otherwise starting the pod would fail due to too little
resources.

# Sensors

## Setup the virtual environment 

This has to be done on the raspberry pi (on the provided one everything is already configured) or on the local machine to start
the test sensors.

If not installed run:
```
sudo apt-get install python3 python3-dev python-virtualenv libatlas-base-dev
```

Run the following command to install the virtual environment:
```
make venv
```

To activate the virtual environment run:
```
source venv/bin/activate
```

## Running the scripts

Run the sensor scripts in the activated virtual environment:
```
python *_sensor.py
```

The following options are available to configure the sensor data writing:
```
Available for actual sensors:
-i / --interval <interval> => the time interval between adjacent samples, default=1, type=int
-a / --analog-port <port> => used analog port of the connected sensor, default=0, type=int
-d / --digital-port <port> => used digital port of the connected sensor, default=4, type=int
-b / --broker <broker-ip> => host address of the mqtt broker, default="localhost"
-p / --broker-port <broker-port> => port of the mqtt broker, default=1883, type=int


Available for test sensors:
-n / --hostname <hostname> => the the host name of the simulated sensor, default="test_host"
-s / --sensors <senorname> => the sensor name of the simulated sensor, default="test_sensor"
-min / --min <minimum> => min value which can get sent by the sensor, default=0, type=int
-max / --max <maximum> => max value which can get sent by the sensor, default=100, type=int
-i / --interval <interval> => the time interval between adjacent samples, default=1, type=int
-b / --broker <broker-ip> => host address of the mqtt broker, default="localhost"
-p / --broker-port <broker-port> => port of the mqtt broker, default=1883, type=int
```
 When running locally with minikube use the IP of minikube and the exposed port for MQTT. 

## Deployment
This has to be done on the raspberry pis. This is already configured on the provided raspberry pis, but since we will run
out of money provided in the cloud, the ip of the broker will probably no longer be viable and will have to be changed under
`sensors/init/<sensorname>_sensor.service` files and the files will have to be copied to the correct locations as described below.

Setup the virtual environment and then copy ``` /sensors ``` into ``` /home/pi/ ```

Adapt the start command in the ``sensors/init/*_sensor.service`` files if necessary and 
copy the respective init files for the sensors to ``` /etc/systemd/system ``` by replacing `*` with the wanted sensor type with:

```
sudo cp <sensorname>_sensor.service /etc/systemd/system/<sensorname>_sensor.service
```

Probably only the ip has to be changed in the `sensors/init/<sensorname>_sensor.service`, by simply changing the value after `-b` in these files. In addition the port can be changed using
the `-p` option like stated before. Also if the sensors are inserted into a different analog port, the `-a` option can be used to 
specify the correct one. The default values for the analog ports of the sensors are as followed, by connecting the sensors to these ports no changes have to be made.


```
light -> analog port 0
dht -> digital port 4
sound -> analog port 1
```

The services can be started with:

```
sudo systemctl start <sensorname>_sensor.service
```

Stop the service with:

```
sudo systemctl stop <sensorname>_sensor.service
```

Enable the services to automatically start the sensors at boot (reboot is required):

```
sudo systemctl enable <sensorname>_sensor.service
```

The mqtt broker has to run in the beginning, otherwise the program stops right away. When the broker fails afterwards, 
all data that can not be written because of this, will be discarded until the broker runs again.

# Grafana

## Login

The default user in grafana is `admin` with password `admin`. Since the users could not be stored in the docker image, 
the default user has to be used and a new password can be set afterwards.
Then you will be forwarded to the overview page of grafana.

## Dashboards
In the provided docker image, 2 Dashboards should already be provided. These are not shown on the overview page. 
For this you have to select under `Dashboards` (the icon with four squares on the left),
the option `Manage`. Now you should be provided with 2 available Dashboards `Hosts` and `Sensors`. Under `Hosts` 
you can select on of the hosts of the data providers and select the sensors
you want to see from this specific host. 

On the `Sensors` dashboard you are able to see the data from the same sensors on different hosts. The data will then 
be visualized in one graph for every type of sensor, showing datalines for each
selected host. 

Under `Host` we understand the hostname of the raspberry pi. For this see the description of how to 
run the sensors. The `sensor` is the type of sensor, e.g. Lightsensor, Soundsensor, etc. 

When creating a new dashboard you can select different types of Panels. We mostly only used the Graph panel. When 
creating a new one, you can add a query to influxdb. Grafana provides a lot of predefined selections to create the query
and is really simple to use.

## Import Dashboards
If for some reason the dashboards are not shown under `Dashboards -> Manage`, you can also import the dashboards easily 
by click `+Import` on this site. Then you only have to copy paste the content of `grafana/dashboards/hosts.json` or
`grafana/dashboards/sensors.json` and clicking load or by uploading the json files. Since our dashboards are included in the
docker image this normally should not be necessary and is just mentioned as a fallback option.

## Configuring the datasource
Grafana needs to know where the influxdb is running. This datasource can be configured under `Configuration -> Datasources` 
(Under the gear icon on the left). Here you can select the influxdb datasource that is already provided and simply change the IP
and port if necessary. In Addtion by selectin `Acesss -> Server` some issues might be resolved when accessing outside of localhost.
For our preconfigured influxdb image the username, database name and password should not be changed.

# Influxdb
Influxdb is running on port `8086`. Username and password can be configured beforehand. The name of the preconfigured database is sensors-db. Here nothing should have to be done.

# Mosquitto
For our MQTT broker we used `Eclipse Mosquitto`. It is running on port `1883`. Here nothing should have to be done.

# Processing stage
Our processing stage is a simple java container that reads from the MQTT Broker and pushes the data into the influxdb.
If the mqtt broker is not available, it simply tries to reconnect until it is available again. If the influxdb is not available,
the current data is simply ignored and with the next received dataset a new push is tried. 

# Triggering Failover Mechanism
We used the failover mechanism of kubernetes to restart our containers on failure. This is also why there exists no failover
mechanism using only the docker-compose command without kubernetes. 