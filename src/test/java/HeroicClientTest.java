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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.spotify.heroic.client.HeroicClient;
import com.spotify.heroic.client.api.HeroicClientException;
import com.spotify.heroic.client.api.query.BatchRequest;
import com.spotify.heroic.client.api.query.BatchResponse;
import com.spotify.heroic.client.api.query.DateRange.Relative;
import com.spotify.heroic.client.api.query.GroupingAggregation;
import com.spotify.heroic.client.api.query.Key;
import com.spotify.heroic.client.api.query.KeyTagFilter;
import com.spotify.heroic.client.api.query.Maximum;
import com.spotify.heroic.client.api.query.MetricRequest;
import com.spotify.heroic.client.api.query.MetricResponse;
import com.spotify.heroic.client.api.query.Operator;
import com.spotify.heroic.client.api.query.Sampling;
import com.spotify.heroic.client.api.query.Sum;
import com.spotify.heroic.client.api.query.Tag;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Rule;
import org.junit.jupiter.api.Test;

public class HeroicClientTest {

  @Rule
  public MockWebServer server = new MockWebServer();

  private final Key METRIC_KEY = Key.of("system");
  private final Tag WHAT_TAG = Tag.of("what", "heartbeat", Operator.MATCH);

  final MetricRequest METRIC_REQUEST = new MetricRequest.Builder()
      .withRange(Relative.withTime(TimeUnit.HOURS, 1L))
      .withFilter(KeyTagFilter.of(METRIC_KEY, List.of(WHAT_TAG)))
      .withAggregation(
          GroupingAggregation.forEach(new Maximum(Sampling.withTime(TimeUnit.SECONDS, 120))))
      .withAggregation(GroupingAggregation
          .groupBy(List.of("site"), new Sum(Sampling.withTime(TimeUnit.SECONDS, 120))))
      .build();


  @Test
  void queryMetricsResponse() throws IOException, InterruptedException {
    server.enqueue(new MockResponse().setResponseCode(200).setBody(new String(
        getClass().getResourceAsStream("/heroic-metrics-response.json").readAllBytes())));

    final HeroicClient heroicClient = HeroicClient.create(server.url("").toString());
    final MetricResponse metricResponse = heroicClient.queryMetricsBlocking(METRIC_REQUEST);

    assertEquals(3, metricResponse.getCommonTags().size());
    assertEquals(3, metricResponse.getDataPoints().size());
    assertEquals(123.0, metricResponse.getDataPoints().get(0).getValues().get(0).getValue());

    RecordedRequest serverRequest = server.takeRequest();
    assertEquals("/query/metrics", serverRequest.getPath());
  }

  @Test
  void queryBatchResponse() throws IOException, InterruptedException {
    server.enqueue(new MockResponse().setResponseCode(200).setBody(new String(
        getClass().getResourceAsStream("/heroic-batch-response.json").readAllBytes())));

    final HeroicClient heroicClient = HeroicClient.create(server.url("").toString());

    final BatchRequest batchRequest =
        new BatchRequest.Builder().withQuery("A", METRIC_REQUEST).build();

    final BatchResponse batchResponse = heroicClient.queryBatchBlocking(batchRequest);

    assertEquals(3, batchResponse.getResults().get("A").getCommonTags().size());
    assertEquals(3, batchResponse.getResults().get("A").getDataPoints().size());
    assertEquals(123.0, batchResponse.getResults().get("A").getDataPoints()
        .get(0).getValues().get(0).getValue());

    RecordedRequest serverRequest = server.takeRequest();
    assertEquals("/query/batch", serverRequest.getPath());
  }

  @Test
  void heroicServerErrorResponse() {
    server.enqueue(new MockResponse().setResponseCode(500));
    final HeroicClient heroicClient = HeroicClient.create(server.url("").toString());
    assertThrows(
        HeroicClientException.class, () -> heroicClient.queryMetricsBlocking(METRIC_REQUEST));
  }

}
