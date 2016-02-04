package org.infinispan.demo

import java.util.Properties

import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.State
import org.apache.spark.streaming.StateSpec
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream.toPairDStreamFunctions
import org.infinispan.spark.stream.InfinispanDStream
import org.infinispan.spark.stream.InfinispanInputDStream

/**
 * Computes average temperature in each place from incoming stream of measurements.
 * The measurement stream is read from Infinispan and produces stream of avg. temperature 
 * updates is written back to Infinispan (to a different cache).
 */
object TemperatureAnalysis {

  val ispnSocket = "127.0.0.1:11222"
  val ispnInCahce = "default"
  val ispnOutCahce = "avg-temperatures"
  
  /**
   * <p>
   * Computes latest average temperature for given place and updates the state.
   * </p>
   * <p>
   * As we want to update average temperature every time new measurement arrives,
   * we need to keep all previous measurement somewhere. We can take advantage of Spark
   * {@link State} and store there sum of temperatures and number of measurements for each place.
   * ONce new measurement arrives, we update the sums, store them in state and compute new 
   * average temperature.
   * </p> 
   */
  val mapFunc = (key: String, temps: Option[Iterable[Double]], state: State[Map[String, (Double, Long)]]) => {
    // get map of places and sums of measured temperatures
    val sumMap: Map[String, (Double, Long)] = state.getOption().getOrElse(Map())
    // get sum of temperatures and number of measurements for given place
    val sums: (Double, Long) = sumMap.getOrElse(key, (0f, 0L))
    // update sums with current measurement
    val curTemps: Iterable[Double] = temps.getOrElse(List())
    val sumUp = sums._1 + curTemps.sum
    val countUp = sums._2 + curTemps.size
    // update state
    state.update(sumMap.updated(key, (sumUp, countUp)))
    // computre and return new average temperature
    (key, sumUp / countUp)
  }

  def main(args: Array[String]) {

    // Initialize Spark streaming context
    val sparkConf = new SparkConf().setAppName("TemperatureAnalysis")
    val ssc = new StreamingContext(sparkConf, Seconds(1))
    ssc.checkpoint("/tmp/spark-temperature") // as we need to store some state information, we need to set up checkpointing

    // configure ISPN connection and create incoming DStream from ISPN
    val configIn = new Properties
    configIn.put("infinispan.rdd.cacheName", ispnInCahce)
    configIn.put("infinispan.client.hotrod.server_list", ispnSocket)
    val ispnStream = new InfinispanInputDStream[String, Double](ssc, StorageLevel.MEMORY_ONLY, configIn)

    // extract (place, temperature) pair from incomming stream
    val measurementStream = ispnStream.map[Tuple2[String, Double]](rdd => (rdd._1, rdd._2))
    // as measurements are batched, it can contain several measurements from the same place - groups them together
    val measurementGrouped = measurementStream.groupByKey()
    // update state and produce new item in avg. temperature stream
    val avgTemperatures = measurementGrouped.mapWithState(StateSpec.function(mapFunc))

    // just print number of items in each RDD to std. out
    avgTemperatures.foreachRDD(rdd => {
      printf("# items in DStream: %d\n", rdd.count())
      rdd.foreach(item => println("Averages:" + item._1 + " -> " + item._2))
    })

    // write avg. temperature stream back to ISPN
    val configOut = new Properties
    configOut.put("infinispan.rdd.cacheName", ispnOutCahce)
    configOut.put("infinispan.client.hotrod.server_list", ispnSocket)
    InfinispanDStream[String, Double](avgTemperatures).writeToInfinispan(configOut)

    ssc.start()
    ssc.awaitTermination()
  }
}