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
package org.createnet.raptor.auth.service.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import org.createnet.raptor.auth.entity.LoginRequest;
import org.createnet.raptor.auth.entity.LoginResponse;
import org.createnet.raptor.auth.service.objects.JsonErrorResponse;
import org.createnet.raptor.auth.service.services.TokenService;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@RestController
@Api(tags = {"User", "Authentication"})
@ApiResponses(value = {
    @ApiResponse(
            code = 200,
            message = "Ok"
    )
    ,
    @ApiResponse(
            code = 401,
            message = "Not authorized"
    )
    ,
    @ApiResponse(
            code = 403,
            message = "Forbidden"
    )
    ,
    @ApiResponse(
            code = 500,
            message = "Internal error"
    )
})
public class AuthenticationController {

    final private static Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Value("${raptor.auth.header}")
    private String tokenHeader;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private TokenService tokenService;

    @RequestMapping(value = "${raptor.auth.route.authentication.path}", method = RequestMethod.POST)
    @ApiOperation(
            value = "Login an user with the provided credentials",
            notes = "",
            response = LoginResponse.class,
            nickname = "login"
    )
    public ResponseEntity<?> login(@RequestBody LoginRequest authenticationRequest) throws AuthenticationException {
        try {

            final Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.username, authenticationRequest.password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Reload password post-security so we can generate token
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.username);
            final Token token = tokenService.createLoginToken((User) userDetails);

            // Return the token
            return ResponseEntity.ok(new LoginResponse((User) userDetails, token.getToken()));

        } catch (AuthenticationException ex) {
            logger.error("Authentication exception: {}", ex.getMessage());
            return JsonErrorResponse.entity(HttpStatus.UNAUTHORIZED, "Authentication failed");
        }
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "${raptor.auth.route.authentication.path}", method = RequestMethod.DELETE)
    @ApiOperation(
            value = "Logout an user invalidating the token used for login",
            notes = "",
            nickname = "logout"
    )
    public ResponseEntity<?> logout(
            HttpServletRequest request,
            Principal principal
    ) {

        String reqToken = request.getHeader(tokenHeader).replace("Bearer ", "");
        Token token = tokenService.read(reqToken);

        if (token == null) {
            return JsonErrorResponse.entity(HttpStatus.NO_CONTENT);
        }

        if (token.getType() != Token.Type.LOGIN) {
            return JsonErrorResponse.entity(HttpStatus.BAD_REQUEST);
        }

        tokenService.delete(token);

        return ResponseEntity.ok(null);

    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "${raptor.auth.route.authentication.refresh}", method = RequestMethod.GET)
    @ApiOperation(
            value = "Refresh a login token",
            notes = "The authentication token, provided via `Authorization` header must still be valid.",
            response = LoginResponse.class,
            nickname = "refreshToken"
    )
    public ResponseEntity<?> refreshToken(
            @RequestHeader("${raptor.auth.header}") String reqToken,
            HttpServletRequest request,
            @AuthenticationPrincipal User currentUser
    ) {

        String bearer = "Bearer ";
        if (reqToken.substring(0, bearer.length()).equals(bearer)) {
            reqToken = reqToken.replace(bearer, "");
        }

        Token token = tokenService.read(reqToken);
        if (token == null) {
            return JsonErrorResponse.entity(HttpStatus.BAD_REQUEST);
        }

        logger.debug("Refreshing token id:{} for user {}", token.getId(), currentUser.getUuid());

        Token refreshedToken = tokenService.generateToken(token);

        logger.debug("Refreshed token id:{}", refreshedToken.getId());
        return ResponseEntity.ok(new LoginResponse(currentUser, refreshedToken.getToken()));
    }

}
