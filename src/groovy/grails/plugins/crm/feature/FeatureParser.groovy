package grails.plugins.crm.feature

import org.springframework.context.ApplicationContext

/**
 * Feature DSL parser.
 */
class FeatureParser {

    def features = [:]

    private Feature current

    def parse(ApplicationContext applicationContext, Closure dsl) {
        def save = dsl.delegate
        try {
            dsl.delegate = this
            dsl.resolveStrategy = Closure.DELEGATE_FIRST
            dsl.each {
                "$it"(applicationContext)
            }
        } finally {
            dsl.delegate = save
        }
        return features
    }

    def methodMissing(String name, args) {
        if (!args.size()) return
        if (args[0] instanceof Closure) {
            features[name] = current = new Feature(name)
            args[0].call()
        }
        return name
    }

    def propertyMissing(String name) {
        name
    }

    def description(String arg) {
        current.description = arg
    }

    def enabled(boolean arg) {
        current.enabled = arg
    }

    def link(Map args) {
        current.linkParams = args.clone()
    }

    @Deprecated
    def main(Map args) {
        current.linkParams = args.clone()
    }

    def permissions(Closure arg) {
        current.permissions = new ShallowParser().parse(arg)
    }

    def role(String arg) {
        current.role = arg
    }

    def tenant(Long arg) {
        current.tenant = arg
    }

    def expires(Date arg) {
        current.expires = arg
    }

    def expires(int arg) {
        current.expires = new Date() + arg
    }

    def dashboard(Closure arg) {
        current.dashboard = new ShallowParser().parse(arg)
    }
}

class ShallowParser {
    private Map props = [:]

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
