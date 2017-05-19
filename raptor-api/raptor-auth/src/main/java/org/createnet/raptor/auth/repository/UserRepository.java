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
package org.createnet.raptor.auth.repository;

import org.createnet.raptor.models.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Transactional
public interface UserRepository extends CrudRepository<User, Long>, JpaRepository<User, Long> {

    User findByUsername(String username);

    User findByEmail(String email);

    User findByUuid(String uuid);

    @Override
    public void delete(User entity);

    @Override
    public void delete(Long id);

    @Override
    public boolean exists(Long id);

    @Override
    public User findOne(Long id);

    @Override
    public <S extends User> S save(S entity);

}
