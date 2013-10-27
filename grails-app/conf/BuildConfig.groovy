grails.project.work.dir = "target"
grails.project.target.level = 1.6

grails.project.repos.default = "crm"

grails.project.dependency.resolution = {
    inherits("global") {}
    log "warn"
    legacyResolve false
    repositories {
        mavenRepo "http://labs.technipelago.se/repo/crm-releases-local/"
        //mavenRepo "http://labs.technipelago.se/repo/plugin-releases-local/"
        grailsCentral()
        mavenCentral()
    }
    dependencies {
        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
    }

    plugins {
        build(":tomcat:$grailsVersion",
                ":release:2.2.1",
                ":rest-client-builder:1.0.3") {
            export = false
        }
        runtime(":hibernate:$grailsVersion")

        test(":spock:0.7") {
            export = false
            exclude "spock-grails-support"
        }
        test(":codenarc:0.18.1") { export = false }
        test(":code-coverage:1.2.6") { export = false }

        compile(":platform-core:1.0.RC5") { excludes 'resources' }
        compile ":cache:1.1.1"
        compile "grails.crm:crm-core:latest.integration"
    }
}
