package org.infinispan.demo;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Simulates network of temperature sensors. Randomly choose a place (form list
 * of European capitals) and generates random temperature. Pair (place,
 * temperature) is stored into ISPN in default ISPN cache. This is periodically
 * repeated for specified amount of time.
 * 
 * @author vjuranek
 *
 */
public class TemperatureSensor {
	public static final String ISPN_IP = "127.0.0.1";
	private static final Random RANDOM = new Random();
	private static final int GENERATE_INTERVAL = 10; // for how long the date
														// should be genearted,
														// in min
	private static final int GENERATE_PERIOD = 100; // how often data should be
													// generated, in ms

	public static void main(String[] args) throws InterruptedException {
		// Configure remote cache
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.addServer().host(ISPN_IP).port(ConfigurationProperties.DEFAULT_HOTROD_PORT);
		RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());
		RemoteCache<String, Double> cache = cacheManager.getCache();

		// Insert some temperature data into the cache
		TimerTask randTemp = new TemperatureGenerator(cache);
		Timer timer = new Timer(true);
		timer.schedule(randTemp, 0, GENERATE_PERIOD); // start generating of
														// random temperatures
		System.out.println("Inserting some temperature data into the cache ...");

		// Generate temperatures for specified interval
		Thread.sleep(GENERATE_INTERVAL * 60 * 1000);
		randTemp.cancel();
		cacheManager.stop();
		System.out.println("DONE! No more date for you.");
		System.exit(0);
	}

	private static class TemperatureGenerator extends TimerTask {

		private static final int TEMP_MAX = 40; // maximum temperature
		private static final String[] places = { "Amsterdam", "Athens", "Belgrade", "Berlin", "Bern", "Bratislava",
				"Brussels", "Bucharest", "Budapest", "Chişinău", "Copenhagen", "Dublin", "Helsinki", "Kiev", "Lisbon",
				"Ljubljana", "London", "Luxembourg", "Madrid", "Minsk", "Monaco", "Moscow", "Oslo", "Paris",
				"Podgorica", "Prague", "Pristina", "Reykjavík", "Riga", "Rome", "San Marino", "Sarajevo", "Skopje",
				"Sofia", "Stockholm", "Tallinn", "Tirana", "Vaduz", "Valletta", "Vatican City", "Vienna", "Vilnius",
				"Warsaw", "Zagreb" };
		private final RemoteCache<String, Double> cache;

		public TemperatureGenerator(RemoteCache<String, Double> cache) {
			this.cache = cache;
		}

		@Override
		public void run() {
			String place = places[RANDOM.nextInt(places.length)];
			double temp = RANDOM.nextDouble() * TEMP_MAX;
			cache.put(place, temp);
			System.out.printf("Inserted %s -> %4.2f%n", place, temp);
		}
	}

}
