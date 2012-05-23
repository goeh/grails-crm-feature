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

import grails.plugins.crm.core.TenantUtils

class CrmFeatureTagLib {

    static namespace = "crm"

    def crmFeatureService

    def featureLink = {attrs, body ->
        def f = attrs.feature
        if (!f) {
            throwTagError("Tag [featureLink] is missing required attribute [feature]")
        }
        def metadata = crmFeatureService.getFeature(f)
        def linkParams = metadata?.main
        def bodyText = body().toString().trim()
        if (!bodyText) {
            bodyText = metadata.description ?: g.message(code: f + '.label', default: f)
        }
        def hasFeature = crmFeatureService.hasFeature(f)
        def enabled = (attrs.enabled?.asBoolean() == true) || hasFeature
        if (linkParams && enabled) {
            out << g.link(linkParams, bodyText)
        } else {
            out << bodyText
        }
    }

    def hasFeature = {attrs, body->
        def f = attrs.feature
        if (!f) {
            throwTagError("Tag [hasFeature] is missing required attribute [feature]")
        }
        def role = attrs.role ?: null
        def tenant = attrs.tenant ?: TenantUtils.tenant
        if(crmFeatureService.hasFeature(f, role, tenant)) {
            out << body()
        }
    }
}
