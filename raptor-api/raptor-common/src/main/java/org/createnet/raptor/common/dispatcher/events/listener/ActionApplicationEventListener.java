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
package org.createnet.raptor.common.dispatcher.events.listener;

import org.createnet.raptor.common.dispatcher.DispatcherService;
import org.createnet.raptor.common.dispatcher.events.ActionApplicationEvent;
import org.createnet.raptor.events.type.ActionEvent;
import org.createnet.raptor.models.acl.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Component
public class ActionApplicationEventListener implements ApplicationListener<ActionApplicationEvent> {

    @Autowired
    DispatcherService dispatcher;

    @Override
    public void onApplicationEvent(ActionApplicationEvent event) {
        ActionEvent actionEvent = event.getActionEvent();
        dispatcher.notifyActionEvent(Operation.valueOf(actionEvent.getParentEvent()), actionEvent.getAction(), actionEvent.getActionStatus().status());
    }

}
