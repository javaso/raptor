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
package org.createnet.raptor.auth.service.acl;

import java.io.Serializable;

import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public interface AclManager {
	
	/**
	 * Add a permission for the given object
	 * 
	 * @param clazz Domain class
	 * @param identifier Id from the given domain
	 * @param sid Security Identifier, could be a {@link PrincipalSid} or a {@link GrantedAuthoritySid}
	 * @param permission The permission based on {@link BasePermission}
	 */
	public <T> void addPermission(Class<T> clazz, Serializable identifier, Sid sid, Permission permission);
	
	/**
	 * Remove a permission from the given object
	 * 
	 * @param clazz Domain class
	 * @param identifier Id from the given domain
	 * @param sid Security Identifier, could be a {@link PrincipalSid} or a {@link GrantedAuthoritySid}
	 * @param permission The permission based on {@link BasePermission}
	 */
	public <T> void removePermission(Class<T> clazz, Serializable identifier, Sid sid, Permission permission);
	
	/**
	 * Check whether the given object has permission
	 * 
	 * @param clazz Domain class
	 * @param identifier Id from the given domain
	 * @param sid Security Identifier, could be a {@link PrincipalSid} or a {@link GrantedAuthoritySid}
	 * @param permission The permission based on {@link BasePermission}
	 * @return true or false
	 */
	public <T> boolean isPermissionGranted(Class<T> clazz, Serializable identifier, Sid sid, Permission permission);

	void deleteAllGrantedAcl();
}
