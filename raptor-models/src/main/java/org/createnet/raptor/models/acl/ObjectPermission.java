/*
 * Copyright 2017 FBK/CREATE-NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.createnet.raptor.models.acl;

import org.createnet.raptor.models.acl.permission.PermissionUtil;
import org.springframework.security.acls.model.Permission;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class ObjectPermission {

    public static ObjectPermission fromLabel(String label) {
        return PermissionUtil.parseObjectPermission(label);
    }

    final protected Class objectType;
    final protected Permission permission;
    final protected boolean own;

    public ObjectPermission(Class objectType, Permission permission) {
        this(objectType, permission, false);
    }

    public ObjectPermission(Class objectType, Permission permission, boolean own) {
        this.objectType = objectType;
        this.permission = permission;
        this.own = own;
    }

    public Class getObjectType() {
        return objectType;
    }

    public Permission getPermission() {
        return permission;
    }

    public boolean isOwn() {
        return own;
    }
    
}
