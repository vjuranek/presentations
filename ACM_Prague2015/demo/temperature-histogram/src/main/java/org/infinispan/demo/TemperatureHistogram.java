package org.infinispan.demo;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryModified;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.event.ClientCacheEntryCreatedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryModifiedEvent;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;

public class TemperatureHistogram {
    public static final String ISPN_IP = "127.0.0.1";

    public static void main(String[] args) throws InterruptedException {
        // Configure remote cache
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer().host(ISPN_IP).port(ConfigurationProperties.DEFAULT_HOTROD_PORT);
        RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());

        // Register cache listener
        RemoteCache<Double, Long> histogramCache = cacheManager.getCache("histogram");
        HistogramListener listener = new HistogramListener(histogramCache);
        histogramCache.addClientListener(listener);

        // Wait for some histogram updates for a while
        System.out.println("Waiting for some histograms for a while");
        Thread.sleep(2 * 60 * 1000);
        System.out.println("DONE");
        histogramCache.removeClientListener(listener);
        cacheManager.stop();
        System.exit(0);
    }

    @ClientListener
    public static class HistogramListener {

        private final RemoteCache histCache;

        public HistogramListener(RemoteCache histCache) {
            this.histCache = histCache;
        }

        @ClientCacheEntryCreated
        public void entryCreated(ClientCacheEntryCreatedEvent<Double> event) {
            System.out.printf("[Created] Bin/count: %s -> %s%n ", event.getKey(), histCache.get(event.getKey()));
        }

        @ClientCacheEntryModified
        public void entryModified(ClientCacheEntryModifiedEvent<Double> event) {
            System.out.printf("[Updated] Bin/count: %s -> %s%n ", event.getKey(), histCache.get(event.getKey()));
        }

    }
}
