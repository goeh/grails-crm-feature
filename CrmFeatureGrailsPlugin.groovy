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

import grails.plugins.crm.feature.CrmFeature
import grails.plugins.crm.core.ClosureToMap
import grails.plugins.crm.feature.FeatureParser

class CrmFeatureGrailsPlugin {
    def groupId = "grails.crm"
    def version = "0.9.7"
    def grailsVersion = "2.0 > *"
    def dependsOn = [:]
    def loadAfter = ['controllers']
    def pluginExcludes = ["grails-app/views/error.gsp"]
    def title = "Manage installed Grails CRM features" // Headline display name of the plugin
    def author = "Goran Ehrsson name"
    def authorEmail = "goran@technipelago.se"
    def description = '''\
This plugin manages installed "features" in Grails CRM.
A feature is a unit of functionality that can be enabled or disabled per user account.
'''

    def documentation = "http://grails.org/plugin/crm-feature"
    def license = "APACHE"
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]
    def issueManagement = [system: "github", url: "https://github.com/goeh/grails-crm-feature/issues"]
    def scm = [url: "https://github.com/goeh/grails-crm-feature"]

    def doWithApplicationContext = { applicationContext ->
        def crmFeatureService = applicationContext.crmFeatureService
        for (plugin in manager.allPlugins) {
            if (plugin.instance.hasProperty('features')) {
                crmFeatureService.addApplicationFeatures(plugin.instance.features)
            }
        }
    }

}
