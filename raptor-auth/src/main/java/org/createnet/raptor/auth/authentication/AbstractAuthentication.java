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
package org.createnet.raptor.auth.authentication;

import org.createnet.raptor.auth.AuthConfiguration;
import org.createnet.raptor.models.objects.Device;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
abstract public class AbstractAuthentication implements Authentication {
  
  protected AuthConfiguration configuration;

  @Override
  public void initialize(AuthConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public void sync(String accessToken, Device obj, SyncOperation op) {
  }

  
}
