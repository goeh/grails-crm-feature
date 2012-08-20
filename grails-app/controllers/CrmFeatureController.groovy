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

import grails.plugins.crm.core.TenantUtils
import grails.converters.JSON
import grails.converters.XML
import javax.servlet.http.HttpServletResponse

/**
 * Feature administration.
 */
class CrmFeatureController {

    static navigation = [
            [group: 'admin',
                    order: 500,
                    title: 'crmFeature.index.label',
                    action: 'index'
            ]
    ]

    def crmFeatureService
    def crmSecurityService

    def index() {
        def tenantId = params.long('id') ?: TenantUtils.tenant
        def tenant = crmSecurityService.getTenantInfo(tenantId)
        def tenantFeatures = crmFeatureService.getFeatures(tenantId)
        [applicationFeatures: crmFeatureService.applicationFeatures.sort {it.name}, tenantFeatures: tenantFeatures.sort {it.name}, tenant: tenant]
    }

    def statistics() {
        def tenantId = params.long('id') ?: TenantUtils.tenant
        def stats = crmFeatureService.getStatistics(params.name, tenantId) ?: [:]
        withFormat {
            html {
                render template: 'statistics', plugin: 'crm-feature', model: stats
            }
            json {
                render stats as JSON
            }
            xml {
                render stats as XML
            }
        }
    }
}
