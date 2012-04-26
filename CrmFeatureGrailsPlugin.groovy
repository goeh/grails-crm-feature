import grails.plugins.crm.feature.CrmFeature

class CrmFeatureGrailsPlugin {
    def groupId = "grails.crm"
    def version = "0.9.0"
    def grailsVersion = "2.0 > *"
    def dependsOn = [:]
    def loadAfter = ['controllers']
    def pluginExcludes = ["grails-app/views/error.gsp"]
    def title = "Manage installed Grails CRM features" // Headline display name of the plugin
    def author = "Goran Ehrsson name"
    def authorEmail = "goran@technipelago.se"
    def description = '''\
This plugin manages installed "features" in Grails CRM.
A feature is a unit of functionality that can be enabled or disabled per user account.
'''

    def documentation = "http://grails.org/plugin/crm-feature"
    def license = "APACHE"
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]
    def issueManagement = [system: "GITHUB", url: "https://github.com/goeh/grails-crm-feature/issues"]
    def scm = [url: "https://github.com/goeh/grails-crm-feature"]

    def doWithWebDescriptor = { xml ->
    }

    def doWithSpring = {
    }

    def doWithDynamicMethods = { ctx ->
    }

    def doWithApplicationContext = { applicationContext ->

        def crmFeatureService = applicationContext.crmFeatureService

        for (plugin in manager.allPlugins) {
            if (plugin.instance.hasProperty('features')) {
                CrmFeature.withTransaction {tx ->
                    def f = plugin.instance.features
                    if (f instanceof Map) {
                        crmFeatureService.addApplicationFeature(f)
                    } else if (f instanceof Closure) {
                        def features = new ClosureToMap(f).props
                        features.each {name, metadata ->
                            if (!metadata.name) {
                                metadata.name = name
                            }
                            crmFeatureService.addApplicationFeature(metadata)
                        }
                    }
                }
            }
        }

    }

    def onChange = { event ->
    }

    def onConfigChange = { event ->
    }

    def onShutdown = { event ->
    }

}

private class ClosureToMap {
    Map props = [:]
    String subKey

    ClosureToMap(Closure c) {
        iterate(c)
    }

    def iterate(Closure c) {
        c.delegate = this
        c.each {"$it"()}
    }

    def methodMissing(String name, args) {
        if (!args.size()) return

        // nested closure, recurse
        if (args[0] in Closure) {
            subKey = name; iterate(args[0]); subKey = null
        }
        else {
            // add nested closure to properties map
            if (subKey) {
                Map map = props[subKey], val = [(name): args[0]]
                props[subKey] = map ? map + val : val
            }
            else props[name] = args[0]
        }
    }

    def propertyMissing(String name) {
        name
    }
}