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
package org.createnet.raptor.http.filter;

import java.io.IOException;
import java.security.Principal;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class LoggerResponseFilter implements ContainerResponseFilter {

    final private Logger logger = LoggerFactory.getLogger(LoggerResponseFilter.class);

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {

        Principal p = request.getSecurityContext().getUserPrincipal();
        String authName = p == null ? "-" : p.getName();

        if (response.getStatus() >= 400) {
            logger.warn("API request error {} {}: [{}] {} /{}",
                    response.getStatus(),
                    response.getStatusInfo().getReasonPhrase(),
                    authName,
                    request.getMethod(),
                    request.getUriInfo().getPath()
            );
        } else {
            logger.info("API request ({}) [{}] {} /{}",
                    response.getStatus(),
                    authName,
                    request.getMethod(),
                    request.getUriInfo().getPath()
            );

        }

    }
}
