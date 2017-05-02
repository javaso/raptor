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
package org.createnet.raptor.models.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.createnet.raptor.models.exception.RecordsetException;
import org.createnet.raptor.models.objects.Channel;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.models.objects.serializer.RecordSetSerializer;
import org.createnet.raptor.models.objects.deserializer.RecordSetDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@JsonSerialize(using = RecordSetSerializer.class)
@JsonDeserialize(using = RecordSetDeserializer.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document
public class RecordSet {

    @Id
    public String id;
    
    @Indexed
    public Date timestamp;

    @Indexed
    final public Map<String, Object> channels = new HashMap();

    @Indexed
    public String userId;

    @Indexed
    public String streamId;

    @Indexed
    public String objectId;

    @GeoSpatialIndexed
    public Point location;

    @JsonIgnore
    @Transient
    private final Logger logger = LoggerFactory.getLogger(RecordSet.class);

    @JsonIgnore
    @Transient
    private Stream stream;

    public RecordSet() {
        this.timestamp = new Date();
    }

    public RecordSet(Stream stream) {
        this();
        this.stream(stream);
    }

    public RecordSet(Stream stream, JsonNode row) {
        this(stream);
        parseJson(stream, row);
    }

    public RecordSet(Stream stream, String body) {
        this(stream);
        ObjectMapper mapper = Device.getMapper();
        try {
            parseJson(stream, mapper.readTree(body));
        } catch (IOException ex) {
            throw new RecordsetException(ex);
        }
    }

    public RecordSet(Map<String, Object> records) {
        this();
        this.channels.putAll(records);
    }

    public RecordSet(Map<String, Object> records, Date date) {
        this.channels.putAll(records);
        this.timestamp = date;
    }

    public RecordSet(Map<String, Object> records, Date date, String userId) {
        this(records, date);
        this.userId = userId;
    }

    public ObjectNode toJsonNode() {
        try {
            return Device.getMapper().convertValue(this, ObjectNode.class);
        } catch (IllegalArgumentException ex) {
            throw new RaptorComponent.ParserException(ex);
        }
    }

    public String toJson() {
        return toJsonNode().toString();
    }

    @Override
    public String toString() {
        try {
            return toJson();
        } catch (RaptorComponent.ParserException ex) {
            logger.error("Cannot serialize RecordSet: {}", ex.getMessage());
        }
        return "{}";
    }

    /**
     * Get the records timestamp
     *
     * @return
     */
    public Date getTimestamp() {
        if (timestamp == null) {
            setTimestamp(new Date());
        }
        return timestamp;
    }

    /**
     * Get the records timestamp as UNIX epoch
     *
     * @return
     */
    public Long getTimestampTime() {
        return getTimestamp().toInstant().getEpochSecond();
    }

    /**
     * Set the records timestamp
     *
     * @param timestamp
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get the stored records
     *
     * @return
     */
    public Map<String, Object> getRecords() {
        return channels;
    }

    public static Object parseType(JsonNode channelNode) {
        return parseType(channelNode, null);
    }

    public static Object parseType(JsonNode channelNode, Channel channel) {

        Object channelValue = null;
        String channelType = null;

        if (channelNode.isBoolean()) {
            channelType = "boolean";
            channelValue = channelNode.asBoolean();
        }

        if (channelNode.isNumber()) {
            channelType = "number";
            if (channelNode.isFloat() || channelNode.isDouble()) {
                channelValue = channelNode.asDouble();
            } else {
                channelValue = channelNode.asLong();
            }
        }

        if (channelNode.isTextual()) {
            channelType = "string";
            channelValue = channelNode.asText();
        }

        if (channel != null && channelType != null) {
            channel.type = channelType;
        }

        return channelValue;
    }

    private void parseJson(Stream stream, JsonNode row) {

        JsonNode channels = row;
        if (row.has("channels")) {

            if (row.has("timestamp")) {
                Date date = Date.from(Instant.ofEpochSecond(row.get("timestamp").asLong()));
                this.setTimestamp(date);
            } else if (row.has("lastUpdate")) {
                Date date = Date.from(Instant.ofEpochSecond(row.get("lastUpdate").asLong()));
                this.setTimestamp(date);
            }

            channels = row.get("channels");
        }

        for (Iterator<Map.Entry<String, JsonNode>> iterator = channels.fields(); iterator.hasNext();) {

            Map.Entry<String, JsonNode> item = iterator.next();

            String channelName = item.getKey();
            JsonNode nodeValue = item.getValue();

            // allow short-hand without [current-]value
            if (nodeValue.isObject()) {
                if (nodeValue.has("value")) {
                    nodeValue = nodeValue.get("value");
                } else if (nodeValue.has("current-value")) {
                    nodeValue = nodeValue.get("current-value");
                }
            }

            Object channelValue = parseType(nodeValue);

            if (channelValue != null) {
                this.channel(channelName, channelValue);
            }

        }

    }

    /**
     * @param channelName
     * @return IRecord
     */
    public Object getByChannelName(String channelName) {
        return channels.get(channelName);
    }

    /**
     * @param channel
     * @return IRecord
     */
    public Object getByChannel(Channel channel) {
        return getByChannelName(channel.name);
    }

    public static RecordSet fromJSON(String raw) {
        try {
            return Device.getMapper().readValue(raw, RecordSet.class);
        } catch (IOException ex) {
            throw new RaptorComponent.ParserException(ex);
        }
    }

    public static RecordSet fromJSON(JsonNode raw) {
        return Device.getMapper().convertValue(raw, RecordSet.class);
    }

    public void setStream(Stream stream) {

        this.stream = stream;

        if (stream != null) {

            this.streamId = stream.name;

            if (stream.getDevice() != null) {

                this.objectId = stream.getDevice().getId();

                if (this.userId == null) {
                    this.userId = stream.getDevice().getUserId();
                }

            }

        }

    }

    public Stream getStream() {
        return this.stream;
    }

    public void validate() {

        if (getStream() != null) {
            for (String channelName : channels.keySet()) {
                
                Channel channel = getStream().channels.getOrDefault(channelName, null);
                if (channel == null) {
                    throw new RaptorComponent.ValidationException("Objet model does not define this channel: " + channelName);
                }
                
                Object value = this.channels.get(channelName);
                
                if(channel.type.equals("boolean")) {
                    if(!(value instanceof Boolean)) {
                        throw new RaptorComponent.ValidationException("Channel " + channelName + " should be a boolean");
                    }
                }
                if(channel.type.equals("number")) {
                    if(
                        !(value instanceof Float) && 
                        !(value instanceof Double) && 
                        !(value instanceof Integer) && 
                        !(value instanceof Long) && 
                        !(value instanceof Short)
                    ) {
                        throw new RaptorComponent.ValidationException("Channel " + channelName + " should be a number");
                    }                    
                }
                if(channel.type.equals("string")) {
                    if(!(value instanceof String)) {
                        throw new RaptorComponent.ValidationException("Channel " + channelName + " should be a string");
                    }                                        
                }
            }
            
        }

    }

    public RecordSet userId(String userId) {
        this.userId = userId;
        return this;
    }

    public RecordSet streamId(String streamId) {
        this.streamId = streamId;
        return this;
    }

    public RecordSet deviceId(String deviceId) {
        this.objectId = deviceId;
        return this;
    }

    public RecordSet stream(Stream stream) {
        setStream(stream);
        return this;
    }

    public RecordSet setObjectId(String objectId) {
        this.objectId = objectId;
        return this;
    }

    public RecordSet timestamp(int time) {
        setTimestamp(new Date(time));
        return this;
    }

    public RecordSet timestamp(long time) {
        setTimestamp(new Date(time));
        return this;
    }

    public RecordSet timestamp(Date time) {
        setTimestamp(time);
        return this;
    }

    public RecordSet location(Point location) {
        this.location = location;
        return this;
    }

    public RecordSet channel(String name, Object record) {
        this.channels.put(name, record);
        return this;
    }

    public RecordSet channel(String name, String value) {
        channel(name, (Object) value);
        return this;
    }

    public RecordSet channel(String name, int value) {
        channel(name, (Object) value);
        return this;
    }

    public RecordSet channel(String name, Integer value) {
        channel(name, (Object) value);
        return this;
    }

    public RecordSet channel(String name, long value) {
        channel(name, (Object) value);
        return this;
    }

    public RecordSet channel(String name, Long value) {
        channel(name, (Object) value);
        return this;
    }

    public RecordSet channel(String name, double value) {
        channel(name, (Object) value);
        return this;
    }

    public RecordSet channel(String name, Double value) {
        channel(name, (Object) value);
        return this;
    }

    public RecordSet channel(String name, boolean value) {
        channel(name, (Object) value);
        return this;
    }

    public RecordSet channel(String name, Boolean value) {
        channel(name, (Object) value);
        return this;
    }

}
