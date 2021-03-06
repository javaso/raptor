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
package org.createnet.raptor.common.configuration;

import org.createnet.raptor.common.client.ApiClientService;
import org.createnet.raptor.common.client.InternalApiClientService;
import org.createnet.raptor.models.configuration.AuthConfiguration;
import org.createnet.raptor.models.configuration.RaptorConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@Configuration
public class ApplicationSharedConfiguration {

    @Autowired
    RaptorConfiguration config;

    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public ApiClientService apiClientService() {
        String token = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        return new ApiClientService(config.getUrl(), token);
    }
    
    @Bean
    public InternalApiClientService internalApiClientService() {
        AuthConfiguration.AdminUser user = config.getAuth().getServiceUser();
        InternalApiClientService client = new InternalApiClientService(config.getUrl(), user.getToken());
        return client;
    }

}
