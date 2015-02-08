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
import grails.converters.JSON
import grails.converters.XML
import javax.servlet.http.HttpServletResponse
import grails.plugins.crm.core.DateUtils

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

    def grailsApplication
    def crmFeatureService
    def crmSecurityService

    def index() {
        def tenantId = params.long('id') ?: TenantUtils.tenant
        def tenant = crmSecurityService.getTenantInfo(tenantId)
        def tenantFeatures = crmFeatureService.getFeatures(tenantId)
        [applicationFeatures: crmFeatureService.applicationFeatures.sort {it.name}, tenantFeatures: tenantFeatures.sort {it.name}, tenant: tenant]
    }

    def info(Long id) {
        if(! id) {
            id = TenantUtils.tenant
        }
        def tenant = crmSecurityService.getTenantInfo(id)
        def feature = crmFeatureService.getApplicationFeature(params.name)
        if (feature) {
            render template: "/" + feature.name + "/readme", plugin: (feature.plugin != null ? feature.plugin : feature.name), model: [feature: feature, tenant:tenant]
        } else {
            log.error("Feature [$name] not found")
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
    }

    def install() {
        def tenant = params.long('id') ?: TenantUtils.tenant
        def feature = crmFeatureService.getApplicationFeature(params.name)
        if (feature) {
            switch (request.method) {
                case 'GET':
                    return [feature: feature, tenant: tenant, referer: params.referer]
                case 'POST':
                    def trialDays = grailsApplication.config.crm.tenant.trialDays ?: 30
                    def expires = DateUtils.endOfWeek(trialDays)
                    crmFeatureService.enableFeature(feature.name, tenant, null, expires)
                    def label = message(code: 'feature.' + feature.name + '.label', default: feature.name)
                    flash.success = message(code: "crmFeature.installed.message", default: "Feature {0} installed", args: [label])
                    if (params.referer) {
                        redirect(uri: params.referer - request.contextPath)
                    } else {
                        redirect(mapping: 'start')
                    }
                    break
            }
        } else {
            log.error("Feature [$id] not found")
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
    }

    def uninstall() {
        def tenant = params.long('id') ?: TenantUtils.tenant
        def feature = crmFeatureService.getApplicationFeature(params.name)
        if (feature) {
            switch (request.method) {
                case 'GET':
                    return [feature: feature, tenant: tenant, referer: params.referer]
                case 'POST':
                    crmFeatureService.disableFeature(feature.name, tenant)
                    def label = message(code: 'feature.' + feature.name + '.label', default: feature.name)
                    flash.warning = message(code: "crmFeature.uninstalled.message", default: "Feature {0} uninstalled", args: [label])
                    if (params.referer) {
                        redirect(uri: params.referer - request.contextPath)
                    } else {
                        redirect(mapping: 'start')
                    }
                    break
            }
        } else {
            log.error("Feature [$id] not found")
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
    }

    def statistics() {
        def tenantId = params.long('id') ?: TenantUtils.tenant
        def stats = crmFeatureService.getStatistics(params.name, tenantId) ?: [usage: null]
        withFormat {
            html {
                render template: (params.template ?: 'statistics'), plugin: 'crm-feature', model: stats
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
