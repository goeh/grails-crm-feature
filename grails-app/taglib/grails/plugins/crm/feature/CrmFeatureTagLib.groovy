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
 */

package grails.plugins.crm.feature

import grails.plugins.crm.core.TenantUtils

class CrmFeatureTagLib {

    static namespace = "crm"

    def crmFeatureService

    def featureLink = {attrs, body ->
        def f = attrs.feature
        if (!f) {
            throwTagError("Tag [featureLink] is missing required attribute [feature]")
        }
        def feature = crmFeatureService.getApplicationFeature(f)
        if(feature) {
            def linkParams = feature.linkParams
            def bodyText = body().toString().trim()
            if (!bodyText) {
                bodyText = feature.description ?: g.message(code: feature.name + '.label', default: feature.name)
            }
            def role = attrs.role ?: null
            def tenant = attrs.tenant ?: TenantUtils.tenant
            def hasFeature = crmFeatureService.hasFeature(f, tenant, role)
            def enabled = (attrs.enabled?.asBoolean() == true) || hasFeature
            if (enabled) {
                if (linkParams) {
                    out << g.link(linkParams, bodyText)
                } else if (attrs.nolink?.asBoolean()) {
                    out << bodyText
                }
            }
        } else {
            log.debug("Tag [featureLink] refer to no-existing feature [$f]")
        }
    }

    def hasFeature = {attrs, body->
        def f = attrs.feature
        if (!f) {
            throwTagError("Tag [hasFeature] is missing required attribute [feature]")
        }
        def role = attrs.role ?: null
        def tenant = attrs.tenant ?: TenantUtils.tenant
        if(crmFeatureService.hasFeature(f, tenant, role)) {
            out << body()
        }
    }

    def eachFeature = {attrs, body->
        def t = attrs.tenant ?: TenantUtils.tenant
        def r = attrs.role ?: null
        def features = crmFeatureService.getFeatures(t, r).sort{it.name}
        int i = 0
        for (f in features) {
            def map = [(attrs.var ?: 'it'): f]
            if (attrs.status) {
                map[attrs.status] = i++
            }
            out << body(map)
        }
    }
}
