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

/**
 * Feature description.
 */
@CompileStatic
class Feature {
    String plugin
    String name
    String description
    Map<String, Object> linkParams
    String role
    Long tenant
    Date expires
    boolean required
    boolean hidden
    Map<String, List<String>> permissions
    Closure statistics

    Feature() {}

    Feature(String name) {
        this.name = name
        this.expires = new Date() // Features are disabled by default
    }

    boolean isEnabled() {
        expires == null || (expires > new Date())
    }

    @Override
    String toString() {
        name.toString()
    }

    String dump() {
        final StringBuilder s = new StringBuilder()
        s << this.toString()
        if(plugin) {
            s << " plugin=$plugin"
        }
        s << " description=\"$description\"".toString()
        s << " linkParams=$linkParams".toString()
        s << " role[$role]".toString()
        s << " tenant[$tenant]".toString()
        s << " required=$required".toString()
        s << " enabled=${isEnabled()}".toString()
        s << " expires=$expires".toString()
        s << " hidden=$hidden".toString()
        s << " permissions=$permissions".toString()
        s.toString()
    }

    @Override
    boolean equals(o) {
        if (this.is(o)) return true;
        if (getClass() != o.class) return false;

        Feature feature = (Feature) o;

        if (name != feature.name) return false;
        if (role != feature.role) return false;
        if (tenant != feature.tenant) return false;

        return true;
    }

    @Override
    int hashCode() {
        int result;
        result = (name != null ? name.hashCode() : 0);
        result = 31 * result + (role != null ? role.hashCode() : 0);
        result = 31 * result + (tenant != null ? tenant.hashCode() : 0);
        return result;
    }
}
