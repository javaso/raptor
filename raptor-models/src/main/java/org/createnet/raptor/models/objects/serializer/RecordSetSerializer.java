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
package org.createnet.raptor.models.objects.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Map;
import org.createnet.raptor.models.data.IRecord;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.Stream;

/**
 * Serialize a RecordSet object to JSON
 * 
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class RecordSetSerializer extends JsonSerializer<RecordSet> {

    @Override
    public void serialize(RecordSet r, JsonGenerator jg, SerializerProvider sp) throws IOException {

        final Stream stream = r.getStream();
        Device obj = null;

        if (stream != null) {
            obj = stream.getDevice();
        }

        jg.writeStartObject();

        jg.writeObjectFieldStart("channels");

        for (Map.Entry<String, IRecord> item : r.channels.entrySet()) {

            String channelName = item.getKey();
            IRecord channel = item.getValue();

            // enforce stream schema if available
            if (r.getStream() != null) {
                if (!r.getStream().channels.containsKey(channelName)) {
                    // skip unmanaged field
                    return;
                }
            }

            if (channel == null) {
                continue;
            }
            if (channel.getValue() == null) {
                continue;
            }

            jg.writeObjectField(channelName, channel.getValue());

//            jg.writeObjectFieldStart(channelName);
//            jg.writeObjectField("current-value", channel.getValue());
//            jg.writeEndObject();

        }

        jg.writeEndObject();

        jg.writeNumberField("timestamp", r.getTimestampTime());
        
        
        // try to get a value
        if(r.userId == null && obj != null) {
            r.userId = obj.userId;
        }
        
        if (r.userId != null) {
            jg.writeStringField("userId", r.userId);
        }


        // try to get a value
        if(r.objectId == null && obj != null) {
            r.objectId = obj.id;
        }
        
        if (r.objectId != null) {
            jg.writeStringField("objectId", r.objectId);
        }

        
        // try to get a value
        if(r.streamId == null && stream != null) {
            r.streamId = stream.name;
        }
        
        if (r.streamId != null) {
            jg.writeStringField("streamId", r.streamId);
        }
        
        jg.writeEndObject();

    }

}
