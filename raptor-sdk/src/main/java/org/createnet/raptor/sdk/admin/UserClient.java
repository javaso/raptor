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
package org.createnet.raptor.sdk.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.createnet.raptor.models.acl.Permissions;
import org.createnet.raptor.models.auth.Role;
import org.createnet.raptor.sdk.AbstractClient;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.api.HttpClient;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.auth.request.AuthorizationRequest;
import org.createnet.raptor.models.auth.request.AuthorizationResponse;
import org.createnet.raptor.models.objects.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods to interact with Raptor API
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class UserClient extends AbstractClient {

    private class JsonUser extends User {

        public JsonUser(User user) {
            super(user);
        }

        public JsonUser() {
        }

        @JsonProperty(access = JsonProperty.Access.READ_WRITE)
        protected String password;

    }

    protected PreferencesClient Preferences;

    public PreferencesClient Preferences() {
        if (Preferences == null) {
            Preferences = new PreferencesClient(getContainer());
        }
        return Preferences;
    }

    public UserClient(Raptor container) {
        super(container);
        Preferences = new PreferencesClient(container);
    }

    final static Logger logger = LoggerFactory.getLogger(UserClient.class);

    /**
     * Check if an user is authorized to operate on a device
     *
     * @param device
     * @param user
     * @param permission
     * @return
     */
    public AuthorizationResponse isAuthorized(Device device, User user, Permissions permission) {
        return isAuthorized(device.getId(), user.getUuid(), permission);
    }

    /**
     * Check if the current user is authorized to operate on a device
     *
     * @param device
     * @param permission
     * @return
     */
    public AuthorizationResponse isAuthorized(Device device, Permissions permission) {
        return isAuthorized(device.getId(), getContainer().Auth().getUser().getUuid(), permission);
    }

    /**
     * Check if an user is authorized to operate on a device
     *
     * @param deviceId
     * @param userId
     * @param permission
     * @return
     */
    public AuthorizationResponse isAuthorized(String deviceId, String userId, Permissions permission) {

        AuthorizationRequest auth = new AuthorizationRequest();
        auth.objectId = deviceId;
        auth.operation = AuthorizationRequest.Operation.Permission.name();
        auth.permission = permission.name();
        auth.userId = userId;

        JsonNode node = getClient().post(HttpClient.Routes.PERMISSION_CHECK, toJsonNode(auth));
        return getMapper().convertValue(node, AuthorizationResponse.class);
    }

    /**
     * Get an user
     *
     * @param userUuid
     * @return
     */
    public User get(String userUuid) {
        JsonNode node = getClient().get(String.format(HttpClient.Routes.USER_GET, userUuid));
        return getMapper().convertValue(node, User.class);
    }

    /**
     * Get user by token
     *
     * @return
     */
    public User get() {
        JsonNode node = getClient().get(HttpClient.Routes.USER_GET_ME);
        return getMapper().convertValue(node, User.class);
    }

    /**
     * Create a new user
     *
     * @param user
     * @return
     */
    public User create(User user) {
        JsonUser jsonUser = new JsonUser(user);
        JsonNode node = getClient().post(HttpClient.Routes.USER_CREATE, toJsonNode(jsonUser));
        return getMapper().convertValue(node, User.class);
    }

    /**
     * Update a user
     *
     * @param user
     * @return
     */
    public User update(User user) {
        assert user.getUuid() != null;
        JsonUser jsonUser = new JsonUser(user);
        JsonNode node = getClient().put(String.format(HttpClient.Routes.USER_UPDATE, user.getUuid()), toJsonNode(jsonUser));
        return getMapper().convertValue(node, User.class);
    }

    /**
     * Delete a user
     *
     * @param user
     * @return
     */
    public void delete(User user) {
        assert user.getUuid() != null;
        delete(user.getUuid());
    }

    /**
     * Delete a user
     *
     * @param userUuid
     */
    public void delete(String userUuid) {
        getClient().delete(String.format(HttpClient.Routes.USER_DELETE, userUuid));
    }

    /**
     * Create a new user setting the minimum required parameters
     *
     * @param username
     * @param password
     * @param email
     * @return
     */
    public User create(String username, String password, String email) {
        return create(username, password, email, new HashSet(Arrays.asList(new Role(Role.Roles.admin))));
    }

    /**
     * Create a new user setting the minimum required parameters
     *
     * @param username
     * @param password
     * @param email
     * @param roles
     * @return
     */
    public User create(String username, String password, String email, Set<Role> roles) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setRoles(roles);
        return create(user);
    }

}
