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

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Tests for CrmFeatureService
 */
class CrmFeatureServiceSpec extends grails.test.spock.IntegrationSpec {

    def crmFeatureService
    def grailsApplication
    def grailsEventsRegistry

    def setup() {
        crmFeatureService.removeAllFeatures()
    }

    def "feature declaration"() {
        // Add features the same way CrmFeatureGrailsPlugin does it.
        given:

        crmFeatureService.addApplicationFeatures {
            test {
                description "Test Feature"
                link controller: "test", action: "index"
                permissions {
                    read "test:index,list,show"
                    update "test:index,list:show,create,update"
                    admin "test:*"
                }
                tenant 1L
                role "admin"
                expires 30
            }
        }

        when:
        def testFeature = crmFeatureService.getApplicationFeature("test")

        then:
        testFeature?.name == "test"
        testFeature?.description == "Test Feature"
        testFeature?.linkParams?.controller == "test"
        testFeature?.linkParams?.action == "index"
        testFeature?.permissions?.read != null
        testFeature?.permissions?.update != null
        testFeature?.permissions?.admin != null
        testFeature?.tenant == 1L
        testFeature?.role == "admin"
        testFeature?.enabled == true
        def now = new Date() + 30
        testFeature?.expires.year == now.year
        testFeature?.expires.month == now.month
        testFeature?.expires.date == now.date
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

    def "Install a feature and make sure it got installed, but not enabled"() {
        when:
        crmFeatureService.addApplicationFeatures {test { description "Test Feature" } }
        then:
        crmFeatureService.applicationFeatures.size() == 1
        // features are disabled by default
        crmFeatureService.hasFeature("test") == false
        crmFeatureService.getFeature("test").hidden == false
        crmFeatureService.getFeature("test").required == false
    }

    def "Install a feature and make it enabled by default"() {
        when:
        crmFeatureService.addApplicationFeatures {test1 { description "Test Feature 1" } }
        crmFeatureService.addApplicationFeatures { test2 { description "Test Feature 2"; enabled false} }
        crmFeatureService.addApplicationFeatures { standard { description "A standard feature enabled by default"; enabled true} }
        then:
        crmFeatureService.getApplicationFeatures().size() == 3
        !crmFeatureService.hasFeature("test1")
        !crmFeatureService.hasFeature("test2")
        crmFeatureService.hasFeature("standard")
    }

    def "Install same feature twice should throw an exception"() {
        given:
        crmFeatureService.addApplicationFeatures {test { description "Test Feature 1" } }
        when:
        crmFeatureService.addApplicationFeatures {test { description "Test Feature 2" } }
        then:
        thrown(IllegalArgumentException)
        crmFeatureService.applicationFeatures.size() == 1
    }

    def "access grailApplication from feature DSL"() {
        given:
        grailsApplication.config.crmFeature.foo = "Foo Feature"
        grailsApplication.config.crmFeature.permission.read = "test:index"

        when:
        crmFeatureService.addApplicationFeatures {
            foo {
                description grailsApplication.config.crmFeature.foo
                permissions {
                    read grailsApplication.config.crmFeature.permission.read
                }
            }
        }

        then:
        crmFeatureService.getApplicationFeature("foo").description == "Foo Feature"
        crmFeatureService.getApplicationFeature("foo").permissions.read == "test:index"
    }

    def "Make sure feature metadata works ok"() {
        when:
        def f = new Feature(name)
        f.description = description
        crmFeatureService.addApplicationFeature(f)
        then:
        crmFeatureService.getApplicationFeature(name).description == description
        where:
        name      | description
        "test"    | "Test Feature"
        "awesome" | "Awesome Feature"
    }

    def "disable a feature"() {
        // Add two application features.
        when:
        crmFeatureService.addApplicationFeature(new Feature("test"))
        crmFeatureService.addApplicationFeature(new Feature("awesome"))
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
        crmFeatureService.getFeatures()*.name == ["awesome"]
    }

    def "enable feature for specific role"() {
        when:
        crmFeatureService.addApplicationFeature(new Feature("test"))
        crmFeatureService.addApplicationFeature(new Feature("awesome"))
        crmFeatureService.addApplicationFeature(new Feature("admin"))
        crmFeatureService.enableFeature(["test", "awesome"])
        crmFeatureService.enableFeature("admin", null, "admin")
        then:
        crmFeatureService.hasFeature("test") == true
        crmFeatureService.hasFeature("awesome") == true
        crmFeatureService.isEnabled("awesome") == true
        crmFeatureService.hasFeature("admin") == false
        crmFeatureService.hasFeature("admin", null, "admin") == true
        crmFeatureService.isEnabled("admin", null, "admin") == true
    }

    def "feature expiration"() {

        when:
        crmFeatureService.addApplicationFeatures {basic { description "Basic Feature"; expires new Date() + 30} }
        crmFeatureService.addApplicationFeatures {advanced { description "Advanced Feature"; expires new Date() + 1} }
        crmFeatureService.addApplicationFeatures {legacy { description "Legacy Feature"; expires new Date() - 1} }

        then:
        crmFeatureService.hasFeature("basic")
        crmFeatureService.hasFeature("advanced")
        !crmFeatureService.hasFeature("legacy") // Expired yesterday

        when:
        crmFeatureService.enableFeature("legacy", null, null, new Date() + 7)

        then:
        crmFeatureService.hasFeature("legacy") // We gave it one more week above
    }

    def "hidden features"() {
        when:
        crmFeatureService.addApplicationFeatures {secret { description "Secret hidden feature"; hidden true } }

        then:
        crmFeatureService.getFeature("secret").hidden
    }

    def "required features"() {
        when:
        crmFeatureService.addApplicationFeatures {mandatory { description "Mandatory feature"; required true } }

        then:
        crmFeatureService.getFeature("mandatory").required
        crmFeatureService.hasFeature("mandatory")
    }

    def "listen to enableFeature events"() {
        given: "setup event listener"
        def result = []
        def latch = new CountDownLatch(1)
        grailsEventsRegistry.on("myFeature", "enableFeature") {event ->
            result << event.feature
            latch.countDown()
        }

        when: "feature is added to application"
        crmFeatureService.addApplicationFeatures {
            myFeature {
                description "A feature that needs setup/initialization"
            }
        }

        then: "result is empty since no event has been sent yet"
        result.isEmpty()

        when: "enabling a feature triggers the 'enableFeature' event on topic <feature name>"
        // 'enableFeature' event is synchronous now, but may change to async in the future.
        crmFeatureService.enableFeature("myFeature")
        latch.await(10L, TimeUnit.SECONDS)

        then: "feature name added to result list"
        !result.isEmpty()
        result[0] == 'myFeature'
    }

    def "feature statistics"() {

        when:
        crmFeatureService.addApplicationFeatures {
            popular {
                description "A popular feature"
                expires new Date() + 365
            }
            statistics {tenant ->
                [usage: 'high', objects: 1234567]
            }
        }

        then:
        crmFeatureService.getStatistics("popular").usage == 'high'
        crmFeatureService.getStatistics("popular").objects == 1234567
    }

    def "theme feature"() {
        // Add features the same way CrmFeatureGrailsPlugin does it.
        given:

        crmFeatureService.addApplicationFeatures {
            sunny {
                description "A feature only available in tenants with the 'sunny' theme"
                link controller: "sunny", action: "index"
                permissions {
                    read "sunny:index,list,show"
                    update "sunny:index,list:show,create,update"
                    admin "sunny:*"
                }
                theme "sunny"
            }
        }

        when:
        def sunnyFeature = crmFeatureService.getApplicationFeature("sunny")

        then:
        sunnyFeature.theme == 'sunny'
    }
}
