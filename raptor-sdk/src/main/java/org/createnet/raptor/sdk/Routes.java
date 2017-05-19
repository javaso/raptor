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
package org.createnet.raptor.sdk;

/**
 * List of base path for Raptor API
 */
final public class Routes {

    public static final String LIST = "/";
    public static final String SEARCH = "/search";
    public static final String CREATE = LIST;
    public static final String UPDATE = "/%s";
    public static final String LOAD = UPDATE;
    public static final String DELETE = UPDATE;
    
    public static final String STREAM = "/stream/%s/%s";
    public static final String PUSH = STREAM;
    
    public static final String LAST_UPDATE = PUSH + "/lastUpdate";
    public static final String PULL = PUSH;
    public static final String SEARCH_DATA = PUSH;
    
    public static final String INVOKE = "/action/%s/%s";
    public static final String ACTION_STATUS = INVOKE;
    
    public static final String SUBSCRIBE_ACTION = "%s/actions/%s";
    public static final String SUBSCRIBE_STREAM = "%s/streams/%s";
    
    public static final String SUBSCRIBE_DEVICE_EVENTS = "%s/events";
    public static final String SUBSCRIBE_USER_EVENTS = SUBSCRIBE_DEVICE_EVENTS;
    
    public static final String PERMISSION_CHECK = "/auth/check";
    public static final String LOGIN = "/auth/login";
    public static final String LOGOUT = LOGIN;
    public static final String REFRESH_TOKEN = "/auth/refresh";
    
    public static final String USER_CREATE = "/auth/user";
    public static final String USER_GET = USER_CREATE + "/%s";
    public static final String USER_UPDATE = USER_GET;
    public static final String USER_DELETE = USER_GET;
    public static final String USER_GET_ME = "/auth/me";
    public static final String USER_UPDATE_ME = USER_GET_ME;
    
    public static final String TOKEN_CREATE = "/auth/token";
    public static final String TOKEN_UPDATE = TOKEN_CREATE + "/%s";
    public static final String TOKEN_DELETE = TOKEN_UPDATE;
    public static final String TOKEN_GET = TOKEN_CREATE + "?uuid=%s";
    
    public static final String PROFILE_GET_ALL = "/profile/%s";
    public static final String PROFILE_GET = PROFILE_GET_ALL + "/%s";
    public static final String PROFILE_SET = PROFILE_GET;
    public static final String PREFERENCES_DELETE = PROFILE_GET;
    
    public static final String TOKEN_PERMISSION_GET = "/auth/permission/token/%s";
    public static final String TOKEN_PERMISSION_BY_USER = TOKEN_PERMISSION_GET + "/%s";
    public static final String TOKEN_PERMISSION_SET = TOKEN_PERMISSION_GET;
    
    public static final String INVENTORY_LIST = "/inventory/";
    public static final String INVENTORY_SEARCH = INVENTORY_LIST + "search";
    public static final String INVENTORY_CREATE = INVENTORY_LIST;
    public static final String INVENTORY_UPDATE = INVENTORY_LIST + "%s";
    public static final String INVENTORY_LOAD = INVENTORY_UPDATE;
    public static final String INVENTORY_DELETE = INVENTORY_UPDATE;
    
    public static final String TREE_LIST = "/tree/";
    public static final String TREE_CREATE = TREE_LIST;
    public static final String TREE_GET = TREE_LIST + "/%s";
    public static final String TREE_CHILDREN = TREE_GET + "/children";
    public static final String TREE_ADD = TREE_GET;
    public static final String TREE_REMOVE = TREE_GET;
}
