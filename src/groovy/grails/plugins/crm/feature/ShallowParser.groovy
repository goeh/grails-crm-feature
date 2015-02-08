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

import org.codehaus.groovy.grails.commons.GrailsApplication

/**
 * A DSL parser that does not evaluate deep.
 */

class ShallowParser {

    def grailsApplication
    def applicationContext

    private Map props = [:]

    ShallowParser(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication
        this.applicationContext = grailsApplication.mainContext
    }

    Map parse(Closure dsl) {
        def save = dsl.delegate
        try {
            dsl.delegate = this
            dsl.resolveStrategy = Closure.DELEGATE_FIRST
            dsl()

        } finally {
            dsl.delegate = save
        }
        return props
    }

    def methodMissing(String name, args) {
        props[name] = (args.size() > 1 ? args.toList() : args[0])
        return name
    }

    def propertyMissing(String name) {
        name
    }
}
