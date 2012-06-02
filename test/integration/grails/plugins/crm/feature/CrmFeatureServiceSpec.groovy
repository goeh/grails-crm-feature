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

import grails.plugins.crm.core.ClosureToMap

/**
 * Tests for CrmFeatureService
 */
class CrmFeatureServiceSpec extends grails.plugin.spock.IntegrationSpec {

    def crmFeatureService

    def setup() {
        crmFeatureService.removeAllFeatures()
    }

    def "feature declaration"() {
        // Add features the same way CrmFeatureGrailsPlugin does it.
        given:
        def closure = {
            test {
                description "Integration Test"
                main controller: "test", action: "index"
                permissions {
                    read "test:index,list,show"
                    update "test:index,list,show,create,edit,delete"
                    admin "test:*", "admin:*"
                }
            }
        }
        def features = ClosureToMap.convert(closure)
        features.each {name, metadata ->
            if (!metadata.name) {
                metadata.name = name
            }
            crmFeatureService.addApplicationFeature(metadata)
        }

        when:
        def testFeature = crmFeatureService.getFeature("test")

        then:
        testFeature?.name == "test"
        testFeature?.permissions?.read != null
    }

    /**
     * Since we don't have any CRM plugins other than crm-core and crm-feature
     * the list of application features is empty.
     * If default features are added in the future this test must be changed.
     */
    def "No application feature should be installed by default"() {
        expect:
        crmFeatureService.applicationFeatures.isEmpty()
    }

    def "Install a feature without a name should throw an exception"() {
        when:
        crmFeatureService.addApplicationFeature([description: "Feature without a name"])
        then:
        thrown(IllegalArgumentException)
        crmFeatureService.applicationFeatures.isEmpty()
    }

    def "Install a feature and make sure it got installed, but not enabled"() {
        when:
        crmFeatureService.addApplicationFeature([name: "test", description: "Test Feature"])
        then:
        !crmFeatureService.applicationFeatures.isEmpty()
        // features are disabled by default
        !crmFeatureService.hasFeature("test")
    }

    def "Install a feature and make it enabled by default"() {
        when:
        crmFeatureService.addApplicationFeature([name: "test1", description: "Test Feature 1"])
        crmFeatureService.addApplicationFeature([name: "test2", description: "Test Feature 2", enabled: false])
        crmFeatureService.addApplicationFeature([name: "standard", description: "A standard feature enabled by default", enabled: true])
        then:
        crmFeatureService.getApplicationFeatures().size() == 3
        !crmFeatureService.hasFeature("test1")
        !crmFeatureService.hasFeature("test2")
        crmFeatureService.hasFeature("standard")
    }

    def "Install same feature twice should throw an exception"() {
        given:
        crmFeatureService.addApplicationFeature([name: "test", description: "Test Feature 1"])
        when:
        crmFeatureService.addApplicationFeature([name: "test", description: "Test Feature 2"])
        then:
        thrown(IllegalArgumentException)
        crmFeatureService.applicationFeatures.size() == 1
    }

    def "Make sure feature metadata works ok"() {
        when:
        crmFeatureService.addApplicationFeature([name: name, description: description])
        then:
        crmFeatureService.getFeature(name).description == description
        where:
        name      | description
        "test"    | "Test Feature"
        "awesome" | "Awesome Feature"
    }

    def "disable a feature"() {
        // Add two application features.
        when:
        crmFeatureService.addApplicationFeature([name: "test", description: "Test Feature"])
        crmFeatureService.addApplicationFeature([name: "awesome", description: "Awesome Feature"])
        // No feature should be enabled by default.
        then:
        crmFeatureService.hasFeature("test") == false
        crmFeatureService.hasFeature("awesome") == false
        // Enable both features.
        when:
        crmFeatureService.enableFeature(["test", "awesome"])
        // make sure they got enabled.
        then:
        crmFeatureService.hasFeature("test") == true
        crmFeatureService.hasFeature("awesome") == true
        // Now disable only the test feature.
        when:
        crmFeatureService.disableFeature("test")
        // Only the test feature should be disabled.
        then:
        crmFeatureService.hasFeature("test") == false
        crmFeatureService.hasFeature("awesome") == true
        crmFeatureService.getFeatures() == ["awesome"]
    }

    def "enable feature for specific role"() {
        when:
        crmFeatureService.addApplicationFeature([name: "test", description: "Test Feature"])
        crmFeatureService.addApplicationFeature([name: "awesome", description: "Awesome Feature"])
        crmFeatureService.addApplicationFeature([name: "admin", description: "Admin Feature"])
        crmFeatureService.enableFeature(["test", "awesome"])
        crmFeatureService.enableFeature("admin", "admin")
        then:
        crmFeatureService.hasFeature("test") == true
        crmFeatureService.hasFeature("awesome") == true
        crmFeatureService.hasFeature("admin") == false
        crmFeatureService.hasFeature("admin", "admin") == true
    }

    def "feature expiration"() {

        when:
        crmFeatureService.addApplicationFeature([name: "basic", description: "Basic Feature", enabled: true, expires: new Date() + 30])
        crmFeatureService.addApplicationFeature([name: "advanced", description: "Advanced Feature", enabled: true, expires: new Date() + 1])
        crmFeatureService.addApplicationFeature([name: "legacy", description: "Legacy Feature", enabled: true, expires: new Date() - 1])

        then:
        crmFeatureService.hasFeature("basic")
        crmFeatureService.hasFeature("advanced")
        !crmFeatureService.hasFeature("legacy") // Expired yesterday

        when:
        crmFeatureService.enableFeature("legacy", null, null, new Date() + 7)

        then:
        crmFeatureService.hasFeature("legacy") // We gave it one more week above
    }
}
