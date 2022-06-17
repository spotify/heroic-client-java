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

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.spotify.heroic.client.api.query.PointsAbove;
import com.spotify.heroic.client.api.query.PointsBelow;
import com.spotify.heroic.client.api.query.Sampling;
import com.spotify.heroic.client.api.query.TDigest;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class AggregationTest {

  private final ObjectMapper mapper =
      new ObjectMapper()
          .registerModule(new KotlinModule())
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Test
  void pointsAbove() throws JsonProcessingException {
    assertEquals(
        "{\"type\":\"pointsabove\",\"threshold\":10.123}",
        mapper.writeValueAsString(new PointsAbove(10.123)));
  }

  @Test
  void pointsBelow() throws JsonProcessingException {
    assertEquals(
        "{\"type\":\"pointsbelow\",\"threshold\":12.345}",
        mapper.writeValueAsString(new PointsBelow(12.345)));
  }

  @Test
  void tDigest() throws JsonProcessingException {
    assertEquals(
        "{\"type\":\"tdigest\",\"sampling\":{\"unit\":\"SECONDS\",\"value\":60}}",
        mapper.writeValueAsString(new TDigest(new Sampling(TimeUnit.SECONDS, 60))));
  }
}
