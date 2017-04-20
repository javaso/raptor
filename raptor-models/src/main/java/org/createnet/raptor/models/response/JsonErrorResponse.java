/*
 * Copyright 2016 CREATE-NET
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
package org.createnet.raptor.auth.service.objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author Luca Capra <luca.capra@create-net.org>
 */
public class JsonErrorResponse {
    
    public final int code;
    public final String message;

    public JsonErrorResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public static ResponseEntity<?> entity(HttpStatus code) {
        return entity(code, null);
    }
    public static ResponseEntity<?> entity(HttpStatus code, String message) {
        if(message == null || message.isEmpty()) {
            message = code.getReasonPhrase();
        }
        return ResponseEntity.status(code).body(new JsonErrorResponse(code.value(), message));
    }
    
}
