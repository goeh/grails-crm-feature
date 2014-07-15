/*
 * Copyright (c) 2014 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.crm.feature

import grails.plugins.crm.core.TenantUtils
import groovy.transform.CompileStatic
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager

/**
 * This service manages available features in a running application instance.
 *
 * Installed CRM plugins provide features available system wide.
 * A single CRM account (tenant) has a subset of those features available for it's users.
 */
class CrmFeatureService {

    public static final String CRM_FEATURE_CACHE = "features"

    static Map<String, Feature> featureMap = [:]

    def grailsApplication
    CacheManager grailsCacheManager

    /**
     * Remove all installed features.
     */
    protected synchronized void removeAllFeatures() {
        CrmFeature.list()*.delete()
        featureMap.clear()
        clearCache()
        log.debug "All features removed from the application!"
    }

    /**
     * Get a list of application features.
     *
     * @return list of features
     */
    @CompileStatic
    List<Feature> getApplicationFeatures() {
        featureMap.values().toList()
    }

    /**
     * Add application features defined by feature DSL.
     *
     * @param features feature DSL
     */
    void addApplicationFeatures(final Closure featureDSL) {
        new FeatureParser(grailsApplication).parse(featureDSL).each {String name, Feature feature ->
            addApplicationFeature(feature)
        }
    }

    /**
     * Add an application feature.
     *
     * @param f Feature instance
     */
    void addApplicationFeature(final Feature f) {
        final String name = f.name
        if (featureMap[name]) {
            throw new IllegalArgumentException("The feature [$name] is already installed")
        }
        synchronized (featureMap) {
            if (!featureMap[name]) {
                featureMap[name] = f
                log.debug("Feature [$name] added to the application")
            }
        }
        event(for: "crm", topic: "addFeature", data: name)
    }

    /**
     * Remove an application feature from the system.
     * It will not be possible to use this feature unless it's added back again.
     *
     * @param name feature name
     */
    void removeApplicationFeature(final String name) {
        final Feature f = featureMap[name]
        if(f) {
            // Tell system we are removing this feature and wait for listeners to complete.
            event(for: "crm", topic: "removeFeature", data: name, fork: false)
        }
        // Remove all enable/disable records.
        CrmFeature.findAllByName(name)*.delete()
        // Remove the feature from system.
        synchronized (featureMap) {
            featureMap.remove(name)
        }
    }

    /**
     * Get metadata for a feature.
     *
     * @param name name of feature
     * @return a Feature instance
     */
    @CompileStatic
    Feature getApplicationFeature(final String name) {
        featureMap[name]
    }

    /**
     * Get metadata for a feature.
     *
     * @param name name of feature
     * @return a Feature instance
     */
    @CompileStatic
    Feature getFeature(final String name, Long tenantId = TenantUtils.tenant, String role = null) {
        featureMap[name]
    }

    /**
     * Convert a persisted CrmFeature instance to a Feature instance.
     * @param crmFeature persisted feature configuration
     * @return Feature instance
     */
    @CompileStatic
    private Feature createFeature(final CrmFeature crmFeature) {
        final Feature a = getApplicationFeature(crmFeature.name)
        if (!a) {
            throw new IllegalArgumentException("Feature [${crmFeature.name}] is not available in this application")
        }
        final Feature f = new Feature(crmFeature.name)
        f.role = crmFeature.role
        f.tenant = crmFeature.tenantId
        f.description = a.description
        f.linkParams = a.linkParams
        f.expires = crmFeature.expires
        f.permissions = a.permissions
        f.hidden = a.hidden
        f.required = a.required
        return f
    }

    /**
     * Enable feature.
     *
     * @param feature name of feature or List of feature names to enable
     * @param tenant (optional) tenant ID to enable feature for a specific tenant
     * @param role (option) enable only for a specific user role
     * @param expires (optional) expiration date for the feature
     */
    void enableFeature(def features, Long tenant = null, String role = null, Date expires = null) {
        if (!(features instanceof Collection)) {
            features = [features]
        }
        for (f in features) {
            Feature feature = getApplicationFeature(f)
            if(! feature) {
                throw new IllegalArgumentException("Feature [$f] is not available in this application")
            }
            List result = CrmFeature.createCriteria().list() {
                eq('name', f)
                if (role != null) {
                    or {
                        eq('role', role)
                        isNull('role')
                    }
                } else {
                    isNull('role')
                }
                if (tenant != null) {
                    or {
                        isNull('tenantId')
                        eq('tenantId', tenant)
                    }
                } else {
                    isNull('tenantId')
                }
                cache true
            }
            if (result) {
                for (r in result) {
                    r.expires = expires
                }
            } else {
                new CrmFeature(tenantId: tenant, role: role, name: f, expires: expires).save(failOnError: true)
                log.debug("Feature [$f] enabled for role [$role] and tenant [$tenant] expires [${expires ?: 'never'}]")
            }
            event(for: f, topic: 'enableFeature', data: [feature: f, tenant: tenant, role:role, expires:expires], fork:false)
        }
        clearCache()
    }

    /**
     * Disable feature.
     *
     * @param feature name of feature or List of feature names to disable
     * @param tenant (optional) tenant ID to disable feature for a specific tenant
     * @param role (option) disable only for a specific user role
     */
    void disableFeature(def features, Long tenant = null, String role = null) {
        if (!(features instanceof Collection)) {
            features = [features]
        }
        for (f in features) {
            Feature feature = getApplicationFeature(f)
            if(! feature) {
                log.warn("Disabling unavailable feature [$f] for tenant [$tenant]")
            }
            List result = CrmFeature.withCriteria {
                eq('name', f)
                if (role != null) {
                    or {
                        eq('role', role)
                        isNull('role')
                    }
                } else {
                    isNull('role')
                }
                if (tenant != null) {
                    or {
                        isNull('tenantId')
                        eq('tenantId', tenant)
                    }
                } else {
                    isNull('tenantId')
                }
                cache true
            }
            Date expired = new Date()
            if (result) {
                for (r in result) {
                    r.expires = expired
                }
            } else {
                new CrmFeature(tenantId: tenant, role: role, name: f, expires: expired).save(failOnError: true)
            }
            log.debug("Feature [$f] disabled for role [$role] and tenant [$tenant]")
            event(for: f, topic: 'disableFeature', data: [feature: f, tenant: tenant, role:role], fork:false)
        }
        clearCache()
    }

    /**
     * Check if a feature is enabled.
     *
     * @param feature name of feature
     * @param tenant (optional) tenant ID to check feature for a specific tenant
     * @params role (optional) role name to check feature for specific role
     * @return true if the feature is enabled
     */
    @CompileStatic
    boolean hasFeature(final String feature, Long tenant = null, String role = null) {
        final Cache cache = grailsCacheManager.getCache(CRM_FEATURE_CACHE)
        final String key = "$feature#${tenant ?: 0}/$role".toString()
        Boolean has = cache.get(key)?.get()
        if(has == null) {
            println "Cache miss for feature $feature"
            has = (getFeatures(tenant, role).find {Feature f -> f.name == feature} != null)
            cache.put(key, has)
        }
        return has
    }

    @CompileStatic
    private List<Feature> union(final Collection<Feature> features, final Collection<Feature> otherFeatures) {
        Collection<Feature> result = features.findAll {Feature f -> f.enabled}
        for (Feature other in otherFeatures) {
            if (other.enabled == false) {
                result.remove(other)
            } else if(! result.find{Feature f -> f.name == other.name}) {
                result.add(other)
            }
        }
        return result
    }

    /**
     * List all enabled features.
     *
     * @param tenant (optional) tenant ID
     * @param role (optional) role name
     * @return List of enabled features
     */
    List<Feature> getFeatures(Long tenant = null, String role = null) {
        List<Feature> all = getApplicationFeatures()
        List<Feature> standard = all.findAll { Feature f->
            if (tenant != f.tenant && f.tenant != null) return false
            if (role != f.role && f.role != null) return false
            return true
        }
        List result = (all ? CrmFeature.withCriteria {
            inList('name', all*.name)
            if (role != null) {
                or {
                    isNull('role')
                    eq('role', role)
                }
            } else {
                isNull('role')
            }
            if (tenant != null) {
                or {
                    isNull('tenantId')
                    eq('tenantId', tenant)
                }
            } else {
                isNull('tenantId')
            }
            cache true
        } : []).collect {createFeature(it)}

        union(standard, result)
    }

    Map<String, Object> getStatistics(final String feature, Long tenant = TenantUtils.tenant) {
        final Feature f = getApplicationFeature(feature)
        if (!f) {
            throw new IllegalArgumentException("Feature [$feature] is not available in this application")
        }
        f.statistics?.call(tenant) ?: null
    }

    @CompileStatic
    void clearCache() {
        grailsCacheManager.getCache(CRM_FEATURE_CACHE).clear()
    }
}
