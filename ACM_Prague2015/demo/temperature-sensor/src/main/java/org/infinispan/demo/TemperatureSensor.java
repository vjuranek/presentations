package org.infinispan.demo;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;

import java.util.*;

public class TemperatureSensor {
    public static final String ISPN_IP = "127.0.0.1";
    private static final Random RANDOM = new Random();

    public static void main(String[] args) throws InterruptedException {
        // Configure remote cache
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer().host(ISPN_IP).port(ConfigurationProperties.DEFAULT_HOTROD_PORT);
        RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());
        RemoteCache<String, Double> cache = cacheManager.getCache();

        // Insert some temperature data into the cache
        TimerTask randTemp = new TemperatureGenerator(cache);
        Timer timer = new Timer(true);
        timer.schedule(randTemp, 0, 1000); // generate random temperature every second
        System.out.println("Inserting some temperature data into the cache ...");

        // Generate temperatures for 2 minutes
        Thread.sleep(10 * 60 * 1000);
        randTemp.cancel();
        cacheManager.stop();
        System.out.println("DONE! No more date for you.");
        System.exit(0);
    }

    private static class TemperatureGenerator extends TimerTask {

        private static final int UPPER_TEMP = 40;
        private static final String[] places = {"Amsterdam", "Athens", "Belgrade", "Berlin", "Bern", "Bratislava", "Brussels",
                "Bucharest", "Budapest", "Chişinău", "Copenhagen", "Dublin", "Helsinki", "Kiev", "Lisbon", "Ljubljana",
                "London", "Luxembourg", "Madrid", "Minsk", "Monaco", "Moscow", "Oslo", "Paris", "Podgorica", "Prague",
                "Pristina", "Reykjavík", "Riga", "Rome", "San Marino", "Sarajevo", "Skopje", "Sofia", "Stockholm", "Tallinn",
                "Tirana", "Vaduz", "Valletta", "Vatican City", "Vienna", "Vilnius", "Warsaw", "Zagreb"};
        private final RemoteCache cache;

        public TemperatureGenerator(RemoteCache cache) {
            this.cache = cache;
        }

        @Override
        public void run() {
            String place = places[RANDOM.nextInt(places.length)];
            double temp = RANDOM.nextDouble() * UPPER_TEMP;
            cache.put(place, temp);
            System.out.printf("Inserted %s -> %4.2f%n", place, temp);
        }
    }

}
