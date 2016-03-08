# Temperature average demo #
This simple application show how to use Infinispan with Spark streaming. Application simulates temperature sensor network, which sends temperature measurements from different places into Insfinspan. Once data
are stored, Spark streaming process the data, updates average temperature in given place and stores results back into Infinispan. Finally, last piece of the demo simulates user application, which receives average
temperature updates for selected places.

### Prerequisites ###
 * JDK 8+
 * Maven 3+
 * Infinispan 8.1+
 * Spark 1.6+

### Starting Infinispan server ###
 * Donwload latest Infinispan server from [Infinispan download page](http://infinispan.org/download/)
 * Unpack Infinispan server (will be reffered as `$ISPN_HOME`)
 * Copy `standalone.xml` from [conf](https://github.com/vjuranek/presentations/blob/master/DevConf_Brno2016/demo/conf) directory into `ISPN_HOME/standalone/configuration`
 * Start Infinispan server by `$ISPN_HOME/bin/standalone.sh`

### Starting Spark ###
Download and unpack latest Spark (will be reffered as `SPARK_HOME`)
```
export SPARK_MASTER_IP=127.0.0.1
$SPARK_HOME/sbin/start-master.sh --webui-port 9080
$SPARK_HOME/sbin/start-slave.sh spark://127.0.0.1:7077 --webui-port 9081
```

### Build the project ###
Clobe git repo in your local workspace (will be reffered as `$WS`) and run
```
mvn clean package
```


### Start temperature sensor simulation ###
Direcotry `WS/temperature-sensor` contains application simulating temperature sensor network.
This application randomly select one of the European capitals and randomly generate temperature. Afterthat it stores `(place, temperature)` pair into Infinispan.
It generates such pair every 100 ms.
To start application, run
```
java -jar $WS/temperature-sensor/target/temperature-sensor-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Start client application ###
This application simulates user application which is notified about average temperatures changes in selected places and is contained in `$WS/temperature-client` directory.
To start application run
```
java -jar target/temperature-client-1.0-SNAPSHOT-jar-with-dependencies.jar Prague Vienna
```
Last argument is space separated list of capitals for which application wants to obtain updates, in this case it's Prague and Vienna. At least one place is required.
Application will listen for updates for 5 minutes.

### Submit Spark job ###
Finally, start Spark streaming analysis job, which recomputes average temperature for each `(place, temperature)` pair arrived, by running
```
$SPARK_HOME/bin/spark-submit --master spark://127.0.0.1:7077 --class org.infinispan.demo.TemperatureAnalysis --packages org.infinispan:infinispan-spark_2.10:0.2 $WS/spark-temperature-analysis/target/spark-temperature-analysis-1.0-SNAPSHOT.jar
```