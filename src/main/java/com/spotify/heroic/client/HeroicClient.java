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
import com.spotify.heroic.client.api.HeroicServerException;
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

  private final HttpUrl baseUrl;
  private final OkHttpClient client;
  private final Request baseRequest;

  private static final ObjectMapper mapper = new ObjectMapper()
      .registerModule(new KotlinModule())
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private HeroicClient(String heroicUrl, Config config) {
    this.baseUrl = HttpUrl.parse(heroicUrl);
    if (this.baseUrl == null) {
      throw new HeroicClientException("A valid heroic url is required");
    }

    this.client = new OkHttpClient().newBuilder()
        .connectTimeout(config.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
        .readTimeout(config.getReadTimeoutSeconds(), TimeUnit.SECONDS)
        .build();

    this.baseRequest = new Request.Builder()
        .url(baseUrl)
        .addHeader("Content-Type", "application/json")
        .addHeader("X-Client-Id", config.getClientId())
        .build();
  }

  public static HeroicClient create(String heroicUrl) {
    return new HeroicClient(heroicUrl, new Config.Builder().build());
  }

  public static HeroicClient createWithConfig(String heroicUrl, Config config) {
    return new HeroicClient(heroicUrl, config);
  }

  private <T> Request postRequest(String pathSegments, T request) {
    try {
      return baseRequest.newBuilder()
          .url(baseUrl.newBuilder().addPathSegments(pathSegments).build())
          .post(RequestBody.create(
              MediaType.parse("application/json; charset=utf-8"),
              mapper.writeValueAsString(request)))
          .build();
    } catch (JsonProcessingException e) {
      throw new HeroicClientException(e);
    }
  }

  public CompletableFuture<MetricResponse> queryMetrics(MetricRequest metricRequest) {
    final Request request = postRequest("query/metrics", metricRequest);
    return bind(request).thenApply(r -> marshallResponse(r, new TypeReference<>() {}));
  }

  public MetricResponse queryMetricsBlocking(MetricRequest metricRequest)
      throws HeroicServerException {
    final Request request = postRequest("query/metrics", metricRequest);
    return marshallResponse(blockingRequest(request), new TypeReference<>() {});
  }

  public CompletableFuture<BatchResponse> queryBatch(BatchRequest batchRequest) {
    final Request request = postRequest("query/batch", batchRequest);
    return bind(request).thenApply(r -> marshallResponse(r, new TypeReference<>() {}));
  }

  public BatchResponse queryBatchBlocking(BatchRequest batchResponse)
      throws HeroicServerException {
    final Request request = postRequest("query/batch", batchResponse);
    return marshallResponse(blockingRequest(request), new TypeReference<>() {});
  }

  private <T> T marshallResponse(Response r, TypeReference<T> type) {
    try (ResponseBody body = r.body()) {
      return mapper.readValue(body.byteStream(), type);
    } catch (IOException e) {
      throw new HeroicClientException(e.getMessage());
    }
  }

  private Response blockingRequest(Request request) throws HeroicServerException {
    final Response response;
    try {
      response = client.newCall(request).execute();
    } catch (IOException e) {
      throw new HeroicServerException(e.getMessage());
    }

    if (!response.isSuccessful()) {
      try {
      throw new HeroicServerException(
          "status code: " + response.code() + " error: " + response.body().string());
      } catch (IOException e) {
        throw new HeroicServerException(e);
      }
    }
    return response;
  }

  private CompletableFuture<Response> bind(Request request) {
    final CompletableFuture<Response> future = new CompletableFuture<>();

    client
        .newCall(request)
        .enqueue(
            new Callback() {
              @Override
              public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
              }

              @Override
              public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                  future.complete(response);
                } else {
                  future.completeExceptionally(new HeroicServerException(response.body().string()));
                }
              }
            });

    return future;
  }
}
