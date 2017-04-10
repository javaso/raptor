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
package org.createnet.raptor.http.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.models.objects.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Path("/{id}/tree")
@Produces(MediaType.APPLICATION_JSON)
@ApiResponses(value = {
    @ApiResponse(
            code = 200, 
            message = "Ok"
    ),
    @ApiResponse(
            code = 401,
            message = "Not authorized"
    ),
    @ApiResponse(
            code = 403, 
            message = "Forbidden"
    ),
    @ApiResponse(
            code = 500, 
            message = "Internal error"
    )
})
@Api(tags = { "Inventory" })
public class ObjectTreeApi extends AbstractApi {

    final private Logger logger = LoggerFactory.getLogger(ObjectTreeApi.class);

    @GET
    @ApiOperation(
            value = "Return the device children if any", 
            notes = "",
            response = Device.class,
            responseContainer = "List",
            nickname = "getChildren"
    )
    @ApiResponses(value = {})
    public List<Device> children(@PathParam("id") String id) {

        Device obj = objectManager.load(id);

        if (!auth.isAllowed(obj, Authorization.Permission.Read)) {
            throw new ForbiddenException("Cannot read object");
        }

        List<Device> list = objectManager.tree(obj);
        return list;
    }

    @POST
    @ApiOperation(
            value = "Set a list of devices as children",
            notes = "",
            response = Device.class,
            responseContainer = "List",
            nickname = "setChildren"
    )
    @ApiResponses(value = {})    
    public List<Device> setChildren(@PathParam("id") String id, final List<String> childrenIds) {

        Device parentObject = objectManager.load(id);
        
        if (!auth.isAllowed(parentObject, Authorization.Permission.Read)) {
            throw new ForbiddenException("Cannot load parent object");
        }
        
        List<Device> childrenObjects = childrenIds.stream().map((String cid) -> {

            Device child = objectManager.load(cid);

            if (!auth.isAllowed(child, Authorization.Permission.Update)) {
                throw new ForbiddenException("Cannot update object");
            }

            // set parent in memory
            child.parentId = parentObject.id;

            return child;
        }).collect(Collectors.toList());

        for (Device childObject : childrenObjects) {
            boolean sync = syncObject(childObject, Authentication.SyncOperation.UPDATE);
            if (!sync) {
                throw new InternalServerErrorException("Failed to sync to auth system object " + childObject.id);
            }
        }

        List<Device> list = objectManager.setChildren(parentObject, childrenObjects);
        return list;
    }

    @PUT
    @Path("{childrenId}")
    @ApiOperation(
            value = "Add a device as children",
            notes = "",
            response = Device.class,
            responseContainer = "List",
            nickname = "addChildren"
    )
    @ApiResponses(value = {})    
    public List<Device> addChildren(@PathParam("id") String id, @PathParam("childrenId") String childrenId) {

        Device parentObject = objectManager.load(id);
        Device childObject = objectManager.load(childrenId);

        if (!auth.isAllowed(parentObject, Authorization.Permission.Update)) {
            throw new ForbiddenException("Cannot update object");
        }

        if (!auth.isAllowed(childObject, Authorization.Permission.Update)) {
            throw new ForbiddenException("Cannot update object");
        }

        childObject.parentId = parentObject.id;
        
        boolean sync = syncObject(childObject, Authentication.SyncOperation.UPDATE);
        if (!sync) {
            throw new InternalServerErrorException("Failed to sync to auth system object " + childObject.id);
        }
        
        List<Device> list = objectManager.addChildren(parentObject, Arrays.asList(childObject));

        return list;
    }

    @DELETE
    @Path("{childrenId}")
    @ApiOperation(
            value = "Remove a device from children list",
            notes = "",
            response = Device.class,
            responseContainer = "List",
            nickname = "removeChildren"
    )
    public List<Device> removeChildren(@PathParam("id") String id, @PathParam("childrenId") String childrenId) {

        Device parentObject = objectManager.load(id);
        Device childObject = objectManager.load(childrenId);

        if (!auth.isAllowed(parentObject, Authorization.Permission.Update)) {
            throw new ForbiddenException("Cannot update parent object");
        }

        if (!auth.isAllowed(childObject, Authorization.Permission.Update)) {
            throw new ForbiddenException("Cannot update child object");
        }

        childObject.parentId = null;
        
        boolean sync = syncObject(childObject, Authentication.SyncOperation.UPDATE);
        if (!sync) {
            throw new InternalServerErrorException("Failed to sync to auth system object " + childObject.id);
        }
        
        List<Device> list = objectManager.removeChildren(parentObject, Arrays.asList(childObject));
        
        return list;

    }

}
