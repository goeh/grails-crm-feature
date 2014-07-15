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

class CrmFeature {

    String name
    String role
    Long tenantId
    Date dateCreated
    Date expires

    static constraints = {
        name(maxSize:80, blank:false, unique:['tenantId', 'role'])
        role(maxSize:80, nullable:true, unique:['tenantId', 'name'])
        tenantId(nullable:true)
        expires(nullable:true)
    }

    static mapping = {
        cache "read-write"
        tenantId index: 'crm_feature_idx'
        name index: 'crm_feature_idx'
        role index: 'crm_feature_idx'
    }

    String toString() {
        name
    }
}
