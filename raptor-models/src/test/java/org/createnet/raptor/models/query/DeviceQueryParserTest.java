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
package org.createnet.raptor.models.query;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.createnet.raptor.utils.TestUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class DeviceQueryParserTest extends TestUtils {

    public DeviceQueryParserTest() {

    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws IOException {
    }

    @After
    public void tearDown() {

    }

    @Test
    public void parseDeviceQuery() throws IOException {

        JsonNode json = loadData("devicequery1");
        DeviceQuery q = mapper.readValue(json.toString(), DeviceQuery.class);

        Assert.assertTrue(q.properties.getHas().getOrDefault("userId", null) != null);
        Assert.assertEquals(json.get("name").get("equals").asText(), q.name.getEquals());
    }

    @Test
    public void parseDeviceQuery2() throws IOException {

        JsonNode json = loadData("devicequery2");
        DeviceQuery q = mapper.readValue(json.toString(), DeviceQuery.class);

        Assert.assertEquals(json.get("id").size(), q.id.getIn().size());
        Assert.assertEquals(json.get("name").asText(), q.name.getEquals());
    }

}
