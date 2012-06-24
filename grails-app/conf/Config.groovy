// configuration for plugin testing - will not be included in the plugin zip

log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log'
}
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"

grails.doc.title = "CRM Feature Management"
grails.doc.version = "0.1"
grails.doc.subtitle = "Grails CRM - Feature Management Plugin"
grails.doc.authors = "Göran Ehrsson, Technipelago AB"
grails.doc.license = "Licensed under the Apache License, Version 2.0"
grails.doc.copyright = "Copyright (c) 2012. Göran Ehrsson,"
grails.doc.footer = "Please contact the author with any corrections or suggestions"
grails.doc.images = new File("src/docs/images")
