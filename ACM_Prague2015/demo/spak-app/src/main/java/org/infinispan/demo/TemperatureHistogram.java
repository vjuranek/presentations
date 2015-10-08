package org.infinispan.demo;

import org.apache.commons.lang.ArrayUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.infinispan.spark.rdd.InfinispanJavaRDD;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Properties;

public class TemperatureHistogram {
    public static final String ISPN_ADDRESS = "127.0.0.1";

    public static void main(String[] args) throws UnknownHostException {
        // Create java spark context
        SparkConf conf = new SparkConf().setAppName("infinispan-spark-simple-job");
        JavaSparkContext jsc = new JavaSparkContext(conf);

        // Create InfinispanRDD
        Properties properties = new Properties();
        properties.put("infinispan.client.hotrod.server_list", ISPN_ADDRESS);
        JavaPairRDD<String, Object> infinispanRDD = InfinispanJavaRDD.createInfinispanRDD(jsc, properties);

        // Convert RDD to RDD of doubles
        JavaDoubleRDD javaDoubleRDD = JavaDoubleRDD.fromRDD(infinispanRDD.values().rdd());

        // Calculate histogram of temperatures
        double[] buckets = {0d, 10d, 20d, 30d, 40d};
        long[] histogram = javaDoubleRDD.histogram(buckets);
        System.out.printf("SPARK TEMPERATURE HISTOGRAM: processed %d data items%n", javaDoubleRDD.rdd().count());

        // Write histogram back to InfinispanRDD
        JavaRDD<Double> bucketsRdd = jsc.parallelize(Arrays.asList(ArrayUtils.toObject(Arrays.copyOf(buckets, buckets.length-1))));
        JavaRDD<Long> histogramRdd = jsc.parallelize(Arrays.asList(ArrayUtils.toObject(histogram)));
        properties.put("infinispan.rdd.cacheName", "histogram");
        InfinispanJavaRDD.write(bucketsRdd.zip(histogramRdd), properties);
    }
}
