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

/**
 * This service manages available features in a running application instance.
 *
 * Installed CRM plugins provide features available system wide.
 * A single CRM account (tenant) has a subset of those features available for it's users.
 */
class CrmFeatureService {

    static transactional = true

    private static final Map<String, Feature> featureMap = [:]

    def grailsApplication

    /**
     * Remove all installed features.
     */
    protected synchronized void removeAllFeatures() {
        CrmFeature.list()*.delete()
        featureMap.clear()
        log.debug "All features removed from the application!"
    }

    /**
     * Get a list of application features.
     *
     * @return list of features
     */
    List<Feature> getApplicationFeatures() {
        featureMap.values().sort{it.name}
    }

    /**
     * Add application features defined by feature DSL.
     *
     * @param features feature DSL
     */
    void addApplicationFeatures(Closure featureDSL) {
        new FeatureParser().parse(grailsApplication.mainContext, featureDSL).each {name, feature ->
            addApplicationFeature(feature)
        }
    }

    /**
     * Add an application feature.
     *
     * @param f Feature instance
     */
    void addApplicationFeature(Feature f) {
        def name = f.name
        if (featureMap[name]) {
            throw new IllegalArgumentException("The feature [$name] is already installed")
        }
        synchronized (featureMap) {
            if (!featureMap[name]) {
                featureMap[name] = f
                log.debug("Feature [$name] added to the application")
            }
        }
        // If enabled is set the feature is enabled by default.
        if (f.enabled) {
            enableFeature(name, f.role, f.tenant, f.expires)
        }
    }

    /**
     * Remove an application feature from the system.
     * It will not be possible to use this feature unless it's added back again.
     *
     * @param name feature name
     */
    void removeApplicationFeature(String name) {
        disableFeature(name)
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
    Feature getFeature(String name) {
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
    def enableFeature(def feature, String role = null, Long tenant = null, Date expires = null) {
        if (!(feature instanceof Collection)) {
            feature = [feature]
        }
        for (f in feature) {
            if (!getFeature(f)) {
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
                cache true
            }
            if(result) {
                for(r in result) {
                    r.expires = expires
                }
            } else  {
                new CrmFeature(tenantId: tenant, role: role, name: f, expires: expires).save(failOnError: true)
                log.debug("Feature [$f] enabled for role [$role] and tenant [$tenant] expires [${expires ?: 'never'}]")
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
                cache true
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
            cache true
        } > 0
    }

    /**
     * List all enabled features.
     *
     * @param role (optional) role name
     * @param tenant (optional) tenant ID
     * @return List of enabled feature names
     */
    List<String> getFeatures(String role = null, Long tenant = null) {
        CrmFeature.withCriteria {
            projections {
                property('name')
            }
            inList('name', applicationFeatures*.name)
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
            cache true
        }
    }

}
