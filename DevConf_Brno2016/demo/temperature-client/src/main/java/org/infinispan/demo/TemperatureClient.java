package org.infinispan.demo;

import java.util.HashSet;
import java.util.Set;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryModified;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.event.ClientCacheEntryCreatedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryModifiedEvent;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;

/**
 * <p>
 * Simulates client application, which is interested in most up-t-date average
 * temperature in specified places. It uses Infisnipan {@link ClientListener}
 * for obtaining notifications that avg. temperature in place of interested was
 * changed.
 * </p>
 * <p>
 * Application needs at least one argument - place where we are interested in
 * avg. temperature changes. In can be also space separated list of places.
 * </p>
 * 
 * @author vjuranek
 *
 */
public class TemperatureClient {

	public static final String ISPN_IP = "127.0.0.1";
	public static final String CACHE_NAME = "avg-temperatures";
	public static final int LISTEN_TIME = 5; // how long the client should
												// listen to changes, in minutes

	public static void main(String[] args) throws Exception {
		// check provided arguments - at least one place of interest needs to be
		// specified
		if (args.length < 1) {
			System.err.println("You have to provide list of places to watch, at least one!");
			System.exit(1);
		}
		Set<String> placesToWatch = new HashSet<>(args.length);
		for (String place : args) {
			placesToWatch.add(place);
		}

		// Configure remote cache
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.addServer().host(ISPN_IP).port(ConfigurationProperties.DEFAULT_HOTROD_PORT);
		RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());
		RemoteCache<String, Double> cache = cacheManager.getCache(CACHE_NAME);

		// Add cache listener and wait for specified amount of time
		AvgTemperatureListener avgTempListener = new AvgTemperatureListener(cache, placesToWatch);
		cache.addClientListener(avgTempListener);
		System.out.printf("Client will be listening to avg. temperature updates for %d minutes%n", LISTEN_TIME);
		Thread.sleep(LISTEN_TIME * 60 * 1000);

		System.out.println("Stopping client");
		cache.removeClientListener(avgTempListener);
		cacheManager.stop();
		System.exit(0);
	}

	/**
	 * Listens for updates in avg. temperature cache and takes action (printing
	 * to std. out) when avg. temperature in watched place has changed.
	 * 
	 * @author vjuranek
	 *
	 */
	@ClientListener
	public static class AvgTemperatureListener {
		private final RemoteCache<String, Double> cache;
		private final Set<String> watchedPlaces;

		public AvgTemperatureListener(RemoteCache<String, Double> cache, Set<String> watchedPlaces) {
			this.cache = cache;
			this.watchedPlaces = watchedPlaces;
		}

		@ClientCacheEntryCreated
		public void entryCreated(ClientCacheEntryCreatedEvent<String> event) {
			if (watchedPlaces.contains(event.getKey()))
				updateAction(event.getKey());
		}

		@ClientCacheEntryModified
		public void entryModified(ClientCacheEntryModifiedEvent<String> event) {
			if (watchedPlaces.contains(event.getKey()))
				updateAction(event.getKey());
		}

		private void updateAction(String key) {
			System.out.printf("[%s] avg. temperature is now %.1f \u00B0C%n", key, cache.get(key));
		}
	}
}
