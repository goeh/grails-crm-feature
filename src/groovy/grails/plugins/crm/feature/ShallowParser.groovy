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
