/*
 * Copyright (c) 2012 Goran Ehrsson.
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
 * under the License.
 */

package grails.plugins.crm.feature

import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.annotation.Propagation
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.CacheEvict

/**
 * This service manages available features in a running application instance.
 *
 * Installed CRM plugins provide features available system wide.
 * A single CRM account (tenant) has a subset of those features available for it's users.
 */
class CrmFeatureService {

    static transactional = false

    private static final Map featureMap = [:]

    /**
     * Remove all installed features.
     */
    protected synchronized void removeAllFeatures() {
        featureMap.clear()
        log.debug "All features removed from the application!"
    }

    /**
     * Get a list of application features (names).
     *
     * @return list of feature names
     */
    List getApplicationFeatures() {
        featureMap.keySet().sort()
    }

    /**
     * Add an application feature.
     *
     * Metadata properties:
     * name: short name of feature
     * description: String that describes the feature
     * (see user guide for more metadata attributes)
     *
     * @param metadata metadata describing the feature
     */
    void addApplicationFeature(Map metadata) {
        String name = metadata.name
        if (!name) {
            throw new IllegalArgumentException("Feature metadata is missing required attribute [name]")
        }
        if (featureMap[name]) {
            throw new IllegalArgumentException("The feature [$name] is already installed")
        }
        synchronized (featureMap) {
            if (!featureMap[name]) {
                featureMap[name] = metadata.clone().asImmutable()
                log.debug("Feature [$name] added to the application")
            }
        }
        // If enabled is set the feature is enabled by default.
        if (metadata.enabled) {
            enableFeature(name, metadata.role, metadata.tenant, metadata.expires)
        }
    }

    /**
     * Remove an application feature from the system.
     * It will not be possible to use this feature unless it's added back again.
     *
     * @param name feature name
     */
    void removeApplicationFeature(String name) {
        synchronized (featureMap) {
            featureMap.remove(name)
        }
    }

    /**
     * Get metadata for a feature.
     *
     * @param name name of feature
     * @return a Map with feature metadata
     */
    @Cacheable("featureCache")
    Map getFeature(String name) {
        featureMap[name]
    }

    /**
     * Enable feature.
     *
     * @param feature name of feature or List of feature names to enable
     * @param role (option) enable only for a specific user role
     * @param tenant (optional) tenant ID to enable feature for a specific tenant
     * @param expires (optional) expiration date for the feature
     * @return
     */
    @Transactional
    @CacheEvict("featureCache")
    def enableFeature(def feature, String role = null, Long tenant = null, Date expires = null) {
        if (!(feature instanceof Collection)) {
            feature = [feature]
        }
        for (f in feature) {
            if (!getApplicationFeatures().contains(f)) {
                throw new IllegalArgumentException("Feature [$f] is not available in this application")
            }
            def result = CrmFeature.createCriteria().list() {
                eq('name', f)
                if (role != null) {
                    eq('role', role)
                }
                if (tenant != null) {
                    eq('tenantId', tenant)
                }
            }
            if(result) {
                for(r in result) {
                    r.expires = expires
                }
            } else  {
                new CrmFeature(tenantId: tenant, role: role, name: f, expires: expires).save(failOnError: true)
                log.debug("Feature [$f] enabled for role [$role] and tenant [$tenant] expires [$expires ?: 'never']")
            }
        }
    }

    /**
     * Disable feature.
     *
     * @param feature name of feature or List of feature names to disable
     * @param role (option) enable only for a specific user role
     * @param tenant (optional) tenant ID to disable feature for a specific tenant
     * @return
     */
    @Transactional
    @CacheEvict("featureCache")
    def disableFeature(def feature, String role = null, Long tenant = null) {
        if (!(feature instanceof Collection)) {
            feature = [feature]
        }
        for (f in feature) {
            def result = CrmFeature.withCriteria {
                eq('name', f)
                if (role != null) {
                    eq('role', role)
                }
                if (tenant != null) {
                    eq('tenantId', tenant)
                }
            }
            if (result) {
                // If no role or tenant was specified, delete this feature for ALL roles and tenants!
                result*.delete()
                log.debug("Feature [$f] disabled for role [$role] and tenant [$tenant]")
            }
        }
    }

    /**
     * Check if a feature is enabled.
     *
     * @param feature name of feature
     * @params role (optional) role name to check feature for specific role
     * @param tenant (optional) tenant ID to check feature for a specific tenant
     * @return true if the feature is enabled
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    boolean hasFeature(String feature, String role = null, Long tenant = null) {
        CrmFeature.createCriteria().count {
            eq('name', feature)
            if (role != null) {
                or {
                    eq('role', role)
                    isNull('role')
                }
            } else {
                isNull('role')
            }
            if (tenant != null) {
                eq('tenantId', tenant)
            }
            or {
                isNull('expires')
                gt('expires', new Date())
            }
        } > 0
    }

    /**
     * List all enabled features.
     *
     * @param role (optional) role name
     * @param tenant (optional) tenant ID
     * @return List of enabled feature names
     */
    @Cacheable("featureCache")
    @Transactional(propagation = Propagation.SUPPORTS)
    List<String> getFeatures(String role = null, Long tenant = null) {
        CrmFeature.withCriteria {
            projections {
                property('name')
            }
            inList('name', getApplicationFeatures())
            if (role != null) {
                or {
                    isNull('role')
                    eq('role', role)
                }
            } else {
                isNull('role')
            }
            if (tenant != null) {
                eq('tenantId', tenant)
            }
            or {
                isNull('expires')
                gt('expires', new Date())
            }
        }
    }

}
