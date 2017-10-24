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

import org.createnet.raptor.models.auth.request.SyncRequest;
import org.createnet.raptor.models.acl.RaptorPermission;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.auth.repository.UserRepository;
import org.createnet.raptor.auth.exception.DeviceNotFoundException;
import org.createnet.raptor.auth.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.model.Permission;
import org.springframework.stereotype.Service;
import org.createnet.raptor.auth.repository.AclAppRepository;
import org.createnet.raptor.models.auth.AclApp;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
@CacheConfig(cacheNames = "aclApp")
public class AuthAppService {

    protected static final Logger logger = LoggerFactory.getLogger(AuthAppService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AclAppRepository appRepository;

    @Autowired
    private AclAppService aclAppService;

    @CacheEvict(key = "#app.id")
    public AclApp save(AclApp app) {

        AclApp saved = appRepository.save(app);
        aclAppService.register(app);

        return get(saved.getId());
    }

    public AclApp getByUuid(String uuid) {
        return appRepository.findByUuid(uuid);
    }

    @Cacheable(key = "#id")
    public AclApp get(Long id) {
        return appRepository.findOne(id);
    }

    @CacheEvict(key = "#id")
    public void delete(Long id) {
        appRepository.delete(id);
    }

    public void delete(AclApp app) {
        delete(app.getId());
    }

    public AclApp sync(User user, SyncRequest req) {

        Permission p = RaptorPermission.fromLabel(req.operation);

        AclApp app = null;
        if (req.objectId != null) {
            app = appRepository.findByUuid(req.objectId);
        }

        /**
         * @TODO check user permissions and roles
         */
        if (req.userId == null) {
            req.userId = user.getUuid();
        }

        if (!req.userId.equals(user.getUuid())) {
            if (!user.isSuperAdmin()) {
                if (!aclAppService.isGranted(app, RaptorPermission.ADMINISTRATION)) {
                    throw new AccessDeniedException("Cannot operate on that object");
                }
            }
        }

        // delete app record
        if (p == RaptorPermission.DELETE) {
            if (app == null) {
                throw new DeviceNotFoundException();
            }
            appRepository.delete(app);
            return null;
        }

        // create or update app record
        if (app == null) {
            app = new AclApp();
            app.setUuid(req.objectId);
        }

        if (req.userId == null) {
            app.setOwner(user);
        } else {
            User owner = userRepository.findByUuid(req.userId);
            if (owner == null) {
                throw new UserNotFoundException();
            }
            app.setOwner(owner);
        }

        AclApp dev = save(app);
        return dev;
    }

}
