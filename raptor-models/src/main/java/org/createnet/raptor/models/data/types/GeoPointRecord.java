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
package org.createnet.raptor.models.data.types;

import com.fasterxml.jackson.databind.JsonNode;
import org.createnet.raptor.models.data.Record;
import org.createnet.raptor.models.data.types.instances.GeoPoint;
import org.createnet.raptor.models.objects.RaptorComponent;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class GeoPointRecord extends Record<GeoPoint> {

    public static class Range {

        public static int LATITUDE_MAX = 180;
        public static int LATITUDE_MIN = -180;

        public static int LONGITUDE_MAX = 90;
        public static int LONGITUDE_MIN = -90;

    }

    protected GeoPoint value;

    @Override
    public GeoPoint getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = parseValue(value);
    }

    @Override
    public GeoPoint parseValue(Object raw) {
        try {

            String val;

            if (raw instanceof GeoPoint) {
                GeoPoint point = (GeoPoint) raw;
                if (validateCoords(point)) {
                    return point;
                }
                throw new Exception("Lat or Lon coordinates invalid");
            }

            if (raw instanceof JsonNode) {

                JsonNode node = (JsonNode) raw;

                if (node.isArray() && node.get(0).isNumber() && node.get(1).isNumber()) {

                    GeoPoint point = new GeoPoint(node.get(0).asDouble(), node.get(1).asDouble());

                    if (validateCoords(point)) {
                        return point;
                    }
                }

                if (node.isObject() && node.has(GeoPoint.LATITUDE) && node.has(GeoPoint.LONGITUDE)) {

                    GeoPoint point = new GeoPoint(node.get(GeoPoint.LATITUDE).asDouble(), node.get(GeoPoint.LONGITUDE).asDouble());

                    if (validateCoords(point)) {
                        return point;
                    }
                }

            }

            // Avoid to parse plain text as ther may be false matches with commons string
            throw new Exception("Cannot parse value: " + raw);

        } catch (Exception e) {
            throw new RaptorComponent.ParserException(e);
        }

    }

    protected static boolean validateCoords(GeoPoint point) {

        if (point.getLat() > Range.LATITUDE_MAX || point.getLat() < Range.LATITUDE_MIN) {
            return false;
        }

        if (point.getLon() > Range.LONGITUDE_MAX || point.getLon() < Range.LONGITUDE_MIN) {
            return false;
        }

        return true;
    }

    @Override
    public String getType() {
        return "geo_point";
    }

    @Override
    public Class<GeoPoint> getClassType() {
        return GeoPoint.class;
    }

}
