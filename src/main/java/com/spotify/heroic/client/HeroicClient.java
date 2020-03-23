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

package com.spotify.heroic.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.spotify.heroic.client.api.HeroicClientException;
import com.spotify.heroic.client.api.query.BatchRequest;
import com.spotify.heroic.client.api.query.BatchResponse;
import com.spotify.heroic.client.api.query.MetricRequest;
import com.spotify.heroic.client.api.query.MetricResponse;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HeroicClient {

  private HttpUrl baseUrl;
  private OkHttpClient client;
  private Request baseRequest;

  private static final ObjectMapper mapper = new ObjectMapper()
      .registerModule(new KotlinModule())
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  public HeroicClient(String heroicUrl) {
    new HeroicClient(heroicUrl, new Config());
  }

  public HeroicClient(String heroicUrl, Config config) {
    this.baseUrl = HttpUrl.parse(heroicUrl);
    if (this.baseUrl == null) {
      throw new HeroicClientException("A valid heroic url is required");
    }

    this.client = new OkHttpClient().newBuilder()
        .callTimeout(config.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
        .readTimeout(config.getReadTimeoutSeconds(), TimeUnit.SECONDS)
        .build();

    this.baseRequest = new Request.Builder()
        .url(baseUrl)
        .addHeader("Content-Type", "application/json")
        .addHeader("X-Client-Id", config.getClientId())
        .build();
  }


  private <T> Request postRequest(String pathSegments, T request) throws HeroicClientException {
    try {
      return baseRequest.newBuilder()
          .url(baseUrl.newBuilder().addPathSegments(pathSegments).build())
          .post(RequestBody.create(
              MediaType.parse("application/json; charset=utf-8"),
              mapper.writeValueAsString(request)))
          .build();
    } catch (JsonProcessingException e) {
      throw new HeroicClientException("Unable to send POST body: " + e.getMessage());
    }
  }

  public CompletableFuture<MetricResponse> queryMetrics(MetricRequest metricRequest) {
    final Request request = postRequest("query/metrics", metricRequest);
    return bind(request).thenApply(r -> marshallResponse(r, new TypeReference<>() {}));
  }

  public MetricResponse queryMetricsBlocking(MetricRequest metricRequest) throws HeroicClientException {
    final Request request = postRequest("query/metrics", metricRequest);
    return marshallResponse(blockingRequest(request), new TypeReference<>() {});
  }

  public CompletableFuture<BatchResponse> queryBatch(BatchRequest batchRequest) {
    final Request request = postRequest("query/batch", batchRequest);
    return bind(request).thenApply(r -> marshallResponse(r, new TypeReference<>() {}));
  }

  public BatchResponse queryBatchBlocking(BatchRequest batchResponse) {
    final Request request = postRequest("query/batch", batchResponse);
    return marshallResponse(blockingRequest(request), new TypeReference<>() {});
  }

  private <T> T marshallResponse(Response r, TypeReference<T> type) {
    if (!r.isSuccessful()) {
      throw new HeroicClientException("Response returned status code: " + r.code());
    }

    try (ResponseBody body = r.body()) {
      return mapper.readValue(body.byteStream(), type);
    } catch (IOException e) {
      throw new HeroicClientException("Response contained invalid JSON: " + e.getMessage());
    }
  }

  private Response blockingRequest(Request request) {
    try {
      return client.newCall(request).execute();
    } catch (IOException e) {
      throw new HeroicClientException("Error making request to heroic: " + e.getMessage());
    }
  }

  private CompletableFuture<Response> bind(Request request) {
    final CompletableFuture<Response> future = new CompletableFuture<>();

    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        future.completeExceptionally(e);
      }

      @Override
      public void onResponse(Call call, Response response) {
        future.complete(response);
      }
    });

    return future;
  }
}
