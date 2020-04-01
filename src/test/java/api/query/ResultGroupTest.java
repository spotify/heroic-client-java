/*-
 * -\-\-
 * heroic-client
 * --
 * Copyright (C) 2016 - 2020 Spotify AB
 * --
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
 * -/-/-
 */

package api.query;

import static org.junit.Assert.assertNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.spotify.heroic.client.api.query.ResultGroup;
import org.junit.jupiter.api.Test;

public class ResultGroupTest {

  private ObjectMapper mapper = new ObjectMapper().registerModule(new KotlinModule());

  @Test
  public void nullKey() throws JsonProcessingException {
    String json = "{\"type\": \"points\",\"hash\": \"2362f9de\", \"shard\": {},"
                  + "\"cadence\": 0,\"values\": [],\"key\": null,\"tags\": {},"
                  + "\"tagCounts\": {},\"resource\": {},\"resourceCounts\": {}}";
    final ResultGroup.Points points = mapper.readValue(json, ResultGroup.Points.class);
    assertNull(points.getKey());
  }

}
