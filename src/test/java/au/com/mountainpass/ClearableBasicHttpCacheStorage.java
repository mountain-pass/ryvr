/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package au.com.mountainpass;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.client.cache.HttpCacheEntry;
import org.apache.http.client.cache.HttpCacheStorage;
import org.apache.http.client.cache.HttpCacheUpdateCallback;
import org.apache.http.impl.client.cache.BasicHttpCacheStorage;
import org.apache.http.impl.client.cache.CacheConfig;

/**
 * Copied from {@link BasicHttpCacheStorage}. Add's flushing capability.
 *
 */
@Contract(threading = ThreadingBehavior.SAFE)
public class ClearableBasicHttpCacheStorage implements HttpCacheStorage {

  private static class CacheMap extends LinkedHashMap<String, HttpCacheEntry> {
    private static final long serialVersionUID = -7750025207539768511L;
    private final int maxEntries;

    private CacheMap(CacheConfig config) {
      super(20, 0.75f, true);
      maxEntries = config.getMaxCacheEntries();
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry<String, HttpCacheEntry> eldest) {
      return size() > this.maxEntries;
    }
  }

  private final CacheMap entries;

  public ClearableBasicHttpCacheStorage(final CacheConfig config) {
    super();
    this.entries = new CacheMap(config);
  }

  /**
   * Places a HttpCacheEntry in the cache
   *
   * @param url
   *          Url to use as the cache key
   * @param entry
   *          HttpCacheEntry to place in the cache
   */
  @Override
  public synchronized void putEntry(final String url, final HttpCacheEntry entry)
      throws IOException {
    entries.put(url, entry);
  }

  /**
   * Gets an entry from the cache, if it exists
   *
   * @param url
   *          Url that is the cache key
   * @return HttpCacheEntry if one exists, or null for cache miss
   */
  @Override
  public synchronized HttpCacheEntry getEntry(final String url) throws IOException {
    return entries.get(url);
  }

  /**
   * Removes a HttpCacheEntry from the cache
   *
   * @param url
   *          Url that is the cache key
   */
  @Override
  public synchronized void removeEntry(final String url) throws IOException {
    entries.remove(url);
  }

  @Override
  public synchronized void updateEntry(final String url, final HttpCacheUpdateCallback callback)
      throws IOException {
    final HttpCacheEntry existingEntry = entries.get(url);
    entries.put(url, callback.update(existingEntry));
  }

  public synchronized void clear() {
    entries.clear();
  }

}
