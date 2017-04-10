/*
 * Copyright 2017 FBK/CREATE-NET.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.createnet.raptor.service.tools;

import org.createnet.raptor.service.AbstractRaptorService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.Device;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author l
 */
@Service
public class CacheService extends AbstractRaptorService {

    private final static Logger logger = LoggerFactory.getLogger(CacheService.class);

    CacheManager cacheManager;

    final private Map<String, Cache<String, String>> instances = new ConcurrentHashMap();

    /**
     * @return the EHCache CacheManager instance
     */
    public CacheManager getManager() {
        if (cacheManager == null) {
            cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
        }
        return cacheManager;
    }

    @Override
    public void shutdown() {
        instances.clear();
        if (cacheManager != null) {
            cacheManager.close();
            cacheManager = null;
        }
    }

    /**
     * @return the EHCache Cache instance for objects
     */
    synchronized public Cache<String, String> getObjectCache() {
        return getCache("objects");
    }

    /**
     * @return the EHCache Cache instance for an object tree
     */
    synchronized public Cache<String, String> getTreeCache() {
        return getCache("tree");
    }

    /**
     * @return the EHCache Cache by name, initializing it if not found
     */
    synchronized public Cache<String, String> getCache(String name) {

        if (instances.containsKey(name)) {
            return instances.get(name);
        } else {

            Cache cache = getManager().getCache(name, String.class, String.class);
            if (cache != null) {
                instances.put(name, cache);
                return cache;
            }

            cache = getManager().createCache(
                    name,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(
                            String.class,
                            String.class,
                            ResourcePoolsBuilder.newResourcePoolsBuilder()
                                    .heap(500, EntryUnit.ENTRIES)
                                    .offheap(100, MemoryUnit.MB))
                            .withExpiry(Expirations.timeToLiveExpiration(Duration.of(30, TimeUnit.SECONDS)))
            );

            instances.put(name, cache);
            return cache;
        }
    }

    @Override
    public void initialize() {
        getObjectCache();
        getTreeCache();
    }
    
    @Override
    public void reset() {
        clearAll();
    }

    /**
     * Set the cache for an object definition by ID
     * @param obj
     */
    public void setObject(Device obj) {
        logger.debug("Store cache for {}", obj.id);
        getObjectCache().put(obj.getId(), obj.toJSON());
    }

    /**
     * Get an object definition by ID
     * @param id
     * @return the service object definition
     */
    public Device getObject(String id) {
        if (getObjectCache().containsKey(id)) {
            logger.debug("Load cache for {}", id);
            String cached = getObjectCache().get(id);
            try {
                return Device.fromJSON(cached);
            } catch (RaptorComponent.ParserException ex) {
                logger.warn("Cannot parse cached json {}: {}", id, cached);
                getObjectCache().remove(id);
            }
        }
        return null;
    }

    /**
     * Clear all objects cache
     */
    public void clearObjects() {
        getObjectCache().clear();
    }

    /**
     * Clear an object cache by ID
     * @param id
     */
    public void clearObject(String id) {
        if (getObjectCache().containsKey(id)) {
            logger.debug("Remove cache for {}", id);
            getObjectCache().remove(id);
        }
    }

    /**
     * Get a list of direct children for an object
     * @param parentId
     * @return 
     */
    public String getChildren(String parentId) {
        logger.debug("Load cache children for {}", parentId);
        return getTreeCache().get(parentId);
    }

    /**
     * Set a list of direct children for an object
     * @param parentId
     * @param list
     */
    public void setChildren(String parentId, String list) {
        logger.debug("Store cache children for {}", parentId);
        getTreeCache().put(parentId, list);
    }

    /**
     * Clear objects cache
     */
    public void clearChildren(String parentId) {
        logger.debug("Remove cache children for {}", parentId);
        getTreeCache().remove(parentId);
    }

    /**
     * Clear objects tree cache
     */
    public void clearChildren() {
        getTreeCache().clear();
    }
    
    /**
     * Clear all cache
     */
    public void clearAll() {
        clearChildren();
        clearObjects();
    }
    

}
