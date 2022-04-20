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
import com.spotify.heroic.client.api.query.Filter;
import com.spotify.heroic.client.api.query.FilterFromPubsub;
import com.spotify.heroic.client.api.query.Key;
import com.spotify.heroic.client.api.query.KeyTagFilter;
import com.spotify.heroic.client.api.query.MetricRequest;
import com.spotify.heroic.client.api.query.Operator;
import com.spotify.heroic.client.api.query.Tag;
import com.spotify.heroic.client.api.query.TrueFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class FilterTest {

  private ObjectMapper mapper =  new ObjectMapper()
      .registerModule(new KotlinModule())
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Test
  public void testFilters() throws JsonProcessingException {
    assertEquals("true", mapper.writeValueAsString(new TrueFilter()));

    assertEquals(
        "[\"and\",[\"key\",\"system\"]]",
        mapper.writeValueAsString(KeyTagFilter.of(Key.of("system"))));

    assertEquals(
        "[\"and\",[\"=\",\"what\",\"heartbeat\"]]",
        mapper.writeValueAsString(
            KeyTagFilter.of(List.of(Tag.and(Operator.MATCH, "what", "heartbeat")))));

    assertEquals(
        "[\"and\",[\"key\",\"system\"],[\"=\",\"what\",\"heartbeat\"]]",
        mapper.writeValueAsString(
            KeyTagFilter.of(
                Key.of("system"), List.of(Tag.and(Operator.MATCH, "what", "heartbeat")))));

    assertEquals(
        "[\"and\",[\"key\",\"system\"],[\"=\",\"what\",\"heartbeat\"],[\"not\",[\"=\",\"env\",\"staging\"]]]",
        mapper.writeValueAsString(
            KeyTagFilter.of(
                Key.of("system"),
                List.of(
                    Tag.and(Operator.MATCH, "what", "heartbeat"),
                    Tag.not(Operator.MATCH, "env", "staging")))));

    assertEquals(
        "[\"and\",[\"key\",\"system\"],[\"=\",\"what\",\"heartbeat\"],[\"=\",\"env\",\"staging\"]]",
        mapper.writeValueAsString(
            KeyTagFilter.of(
                Key.of("system"),
                List.of(
                    Tag.and(Operator.MATCH, "what", "heartbeat"),
                    Tag.and(Operator.MATCH, "env", "staging")))));
  }

  @Test
  public void testTags() throws JsonProcessingException {
    assertEquals(
        "[\"=\",\"what\",\"heartbeat\"]",
        mapper.writeValueAsString(Tag.and(Operator.MATCH, "what", "heartbeat")));

    assertEquals(
        "[\"+\",\"what\"]",
        mapper.writeValueAsString(Tag.and(Operator.EXIST, "what")));

    assertEquals(
        "[\"^\",\"what\",\"heart\"]",
        mapper.writeValueAsString(Tag.and(Operator.PREFIX, "what", "heart")));
  }

  @Test
  public void testNotTags() throws JsonProcessingException {
    assertEquals(
        "[\"not\",[\"=\",\"what\",\"heartbeat\"]]",
        mapper.writeValueAsString(Tag.not(Operator.MATCH, "what", "heartbeat")));
  }

  @Test
  public void testCustomTag() throws JsonProcessingException {
    assertEquals(
        "[\"q\",\"role in [heroicsuggestes\"]",
        mapper.writeValueAsString(Tag.and(Operator.CUSTOM, "role in [heroicsuggestes")));
  }

  @Test
  public void testFilterDeserialization() throws IOException {
    InputStream stream = getClass().getResourceAsStream("/filter-from-pubsub.json");
    Filter actualFilter = mapper.readValue(stream, Filter.class);
    Filter expectedFilter = new FilterFromPubsub(Arrays.asList("and", Arrays.asList("key", "kube-state-metrics"), Arrays.asList("=", "what", "kube_hpa_status_current_usage"), Arrays.asList("=", "hpa", "alexa-proxy")));

    assertEquals(expectedFilter, actualFilter);
  }

  @Test
  public void testMetricRequestDeserialization() throws IOException {
    InputStream stream = getClass().getResourceAsStream("/metric-request-from-pubsub.json");
    MetricRequest metricRequest = mapper.readValue(stream, MetricRequest.class);
    Filter expectedFilter = new FilterFromPubsub(Arrays.asList("and", Arrays.asList("key", "kube-state-metrics"), Arrays.asList("=", "what", "kube_hpa_status_current_usage"), Arrays.asList("=", "hpa", "alexa-proxy")));
    assertEquals(Arrays.asList(), metricRequest.getAggregators());
    assertEquals(expectedFilter, metricRequest.getFilter());

  }
}
