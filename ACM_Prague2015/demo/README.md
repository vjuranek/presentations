export SPARK_MASTER_IP=127.0.0.1
./start-master.sh --webui-port 9080
./start-slave.sh spark://127.0.0.1:7077 --webui-port 9081
./spark-submit --master spark://127.0.0.1:7077 --class org.infinispan.demo.TemperatureHistogram --packages org.infinispan:infinispan-spark_2.10:0.1 ~/ws-idea/ispn-demo/spak-app/target/sparkapp-1.0-SNAPSHOT.jar 