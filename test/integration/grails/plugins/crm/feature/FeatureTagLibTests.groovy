package grails.plugins.crm.feature

import grails.test.GroovyPagesTestCase

/**
 * Test CrmFeatureTagLib.
 */
class FeatureTagLibTests extends GroovyPagesTestCase {

    def crmFeatureService

    void testFeatureLink() {
        crmFeatureService.addApplicationFeatures {
            test {
                description "Test Feature"
                link controller: "test", action: "index"
            }
            standard {
                description "A standard feature enabled by default"
                link controller: "std", action: "index"
                enabled true
            }
        }
        // No link should be rendered for non-existing features, and it should not give error.
        def template = '<crm:featureLink feature="nonexisting"/>'
        assert crmFeatureService.hasFeature("nonexisting") == false
        assert applyTemplate(template) == ""

        // No link should be rendered for existing but disabled features.
        template = '<crm:featureLink feature="test"/>'
        assert crmFeatureService.hasFeature("test") == false
        assert applyTemplate(template) == ""

        crmFeatureService.enableFeature("test")
        assert crmFeatureService.hasFeature("test")
        assert applyTemplate(template) == '<a href="/test/index">Test Feature</a>'

        template = '<crm:featureLink feature="standard">Standard</crm:featureLink>'
        assert crmFeatureService.hasFeature("standard")
        assert applyTemplate(template) == '<a href="/std/index">Standard</a>'
    }
}
