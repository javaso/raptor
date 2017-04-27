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
package org.createnet.raptor.service.tools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Singleton;
import org.createnet.raptor.events.Emitter;
import org.createnet.raptor.events.Event;
import org.createnet.raptor.events.Event.EventType;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
@Singleton
public class EventEmitterService extends Emitter {

  final ExecutorService executor = Executors.newCachedThreadPool();  
  
  public void on(EventType name, Callback cb) {
    on(name.toString(), cb);
  }

  public void off(EventType name, Callback cb) {
    off(name.toString(), cb);
  }

  public void off(EventType name) {
    off(name.toString());
  }

  public void trigger(EventType name, Event event) {

    Runnable task = () -> {

      event.setParentEvent(name.toString());

      // trigger to all listener
      trigger(EventType.all.toString(), event);

      // group event call
      switch (name) {

        case create:
        case update:
        case delete:
          trigger(EventType.object.toString(), event);
          break;

        case push:
        case pull:
          trigger(EventType.data.toString(), event);
          break;

      }

      trigger(name.toString(), event);
    };

    executor.submit(task);
    
  }

}
