package grails.plugins.crm.feature

/**
 * Feature description.
 */
class Feature {
    String name
    String description
    Map<String, Object> linkParams
    String role
    Long tenant
    boolean enabled
    Date expires
    Map<String, List<String>> permissions
    Map<String, Closure> dashboard

    Feature() {}

    Feature(String name) {
        this.name = name
    }

    String toString() {
        name.toString()
    }

    String dump() {
        def s = new StringBuilder()
        s << this.toString()
        s << " description=\"$description\"".toString()
        s << " linkParams=$linkParams".toString()
        s << " role[$role]".toString()
        s << " tenant[$tenant]".toString()
        s << " enabled=$enabled".toString()
        s << " expires=$expires".toString()
        s << " permissions=$permissions".toString()
        s << " dashboard=${dashboard?.keySet()}".toString()
        s.toString()
    }
}
