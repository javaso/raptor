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
package org.createnet.raptor.auth.service.repository;

import java.util.List;
import org.createnet.raptor.models.auth.Token;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Repository
public interface TokenRepository extends CrudRepository<Token, Long> {

    Token findByToken(String token);

    List<Token> findByUserId(String userId);
    List<Token> findByUserUuid(String userUuid);
    List<Token> findByType(Token.Type type);

    @Override
    Token findOne(Long id);

    @Transactional
    @Override
    public void delete(Token token);

    @Transactional
    @Override
    public void delete(Long id);

    @Transactional
    @Override
    public <S extends Token> S save(S token);

}
