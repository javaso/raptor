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
package org.createnet.raptor.auth.services;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.createnet.raptor.auth.acl.RaptorPermission;
import org.createnet.raptor.auth.acl.UserSid;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;

/**
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
public class AclTokenService implements AclServiceInterface<Token> {

    private final Logger logger = LoggerFactory.getLogger(AclTokenService.class);

    @Autowired
    private AclManagerService aclManagerService;

    public void add(Token token, User user, Permission permission) {
        aclManagerService.addPermission(Token.class, token.getId(), new UserSid(user), permission);
    }

    @Override
    public void add(Token token, User user, List<Permission> permissions) {
        aclManagerService.addPermissions(Token.class, token.getId(), new UserSid(user), permissions);
    }

    @Override
    public void set(Token token, User user, List<Permission> permissions) {
        Long pid = null;
        aclManagerService.setPermissions(Token.class, token.getId(), new UserSid(user), permissions, pid);
    }

    @Override
    public List<Permission> list(Token token, User user) {
        ObjectIdentity oitoken = new ObjectIdentityImpl(token.getClass(), token.getId());
        return aclManagerService.getPermissionList(user, oitoken);
    }

    @Override
    public void remove(Token token, User user, Permission permission) {
        aclManagerService.removePermission(Token.class, token.getId(), new UserSid(user), permission);
    }

    @Override
    public boolean isGranted(Token token, User user, Permission permission) {
        return aclManagerService.isPermissionGranted(Token.class, token.getId(), new UserSid(user), permission);
    }

    @Override
    public void register(Token token) {
    }

    @Override
    public boolean check(Token token, User user, Permission permission) {

        if (user == null) {
            return false;
        }
        if (token == null) {
            return false;
        }
        if (permission == null) {
            return false;
        }

        if (user.isSuperAdmin()) {
            return true;
        }

        if (!user.isEnabled()) {
            return false;
        }

        // check if user has ADMINISTRATION permission on device 
        if (isGranted(token, user, RaptorPermission.ADMINISTRATION)) {
            return true;
        }

        // check device specific permission first
        return isGranted(token, user, permission);
    }

}