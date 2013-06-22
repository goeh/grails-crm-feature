grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.repos.default = "crm"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn"
    repositories {
        grailsHome()
        mavenRepo "http://labs.technipelago.se/repo/crm-releases-local/"
        grailsCentral()
        //mavenRepo "http://labs.technipelago.se/repo/plugin-releases-local/"
    }
    dependencies {
        //test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
    }

    plugins {
        build(":tomcat:$grailsVersion",
                ":release:2.2.1") {
            export = false
        }
        runtime(":hibernate:$grailsVersion")

        test(":spock:0.7") {
            export = false
            //exclude "spock-grails-support"
        }
        test(":codenarc:0.18.1") { export = false }
        test(":code-coverage:1.2.6") { export = false }

        compile(":platform-core:1.0.RC5") { excludes 'resources' }

        compile "grails.crm:crm-core:latest.integration"
    }
}
