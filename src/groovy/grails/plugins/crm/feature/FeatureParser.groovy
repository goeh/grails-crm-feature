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

import groovy.transform.CompileStatic
import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.commons.GrailsApplication

/**
 * Feature DSL parser.
 */
class FeatureParser {

    def grailsApplication
    def applicationContext

    Map<String, Feature> features = [:]

    private Feature current

    FeatureParser(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication
        this.applicationContext = grailsApplication.mainContext
    }

    Map<String, Feature> parse(Closure dsl) {
        def save = dsl.delegate
        try {
            dsl.delegate = this
            dsl.resolveStrategy = Closure.DELEGATE_FIRST
            dsl.each {
                "$it"()
            }
        } finally {
            dsl.delegate = save
        }
        features
    }

    def methodMissing(String name, args) {
        if (!args.size()) return
        if (args[0] instanceof Closure) {
            features[name] = current = new Feature(name)
            setDefaults()
            args[0].call()
        }
        return name
    }

    private void setDefaults() {
        required grailsApplication.config.crm.feature[current.name].required.asBoolean()
        hidden grailsApplication.config.crm.feature[current.name].hidden.asBoolean()
    }

    def propertyMissing(String name) {
        name
    }

    @CompileStatic
    def plugin(String arg) {
        current.plugin = arg
    }

    @CompileStatic
    def description(String arg) {
        current.description = arg
    }

    def link(Map args) {
        current.linkParams = args.clone()
    }

    @Deprecated
    def main(Map args) {
        current.linkParams = args.clone()
    }

    def permissions(Closure arg) {
        current.permissions = new ShallowParser(grailsApplication).parse(arg)
    }

    @CompileStatic
    def role(String arg) {
        current.role = arg
    }

    @CompileStatic
    def tenant(Long arg) {
        current.tenant = arg
    }

    @CompileStatic
    def expires(Date arg) {
        current.expires = arg
    }

    @CompileStatic
    def expires(int arg) {
        current.expires = new Date() + arg
    }

    @CompileStatic
    def enabled(boolean arg) {
        if (arg || current.required) { // It's illegal to disable a required feature.
            current.expires = null
        } else {
            current.expires = new Date() // Expire now!
        }
    }

    @CompileStatic
    def hidden(boolean arg) {
        current.hidden = arg
    }

    @CompileStatic
    def required(boolean arg) {
        current.required = arg
        if(arg) {
            enabled true // If a feature is required it must be enabled.
        }
    }

    @CompileStatic
    def statistics(Closure arg) {
        current.statistics = arg
    }

}
