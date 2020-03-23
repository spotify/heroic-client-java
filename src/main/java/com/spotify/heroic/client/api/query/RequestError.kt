/*
 * Copyright 2020 Spotify AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.spotify.heroic.client.api.query

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(RequestError.NodeError::class),
    JsonSubTypes.Type(RequestError.ShardError::class),
    JsonSubTypes.Type(RequestError.QueryError::class)
)
interface RequestError {

    @JsonTypeName("node")
    data class NodeError  (
        @JsonProperty("nodeId") val nodeId: UUID,
        @JsonProperty("nodeUri") val node: String,
        @JsonProperty("tags") val tags: Any,
        @JsonProperty("error") val error: String,
        @JsonProperty("internal") val internal: Boolean) : RequestError

    @JsonTypeName("shard")
    class ShardError (
        @JsonProperty("nodes") val nodes: List<String>,
        @JsonProperty("shard") val shard: Map<String, String>,
        @JsonProperty("error") val error: String) : RequestError

    @JsonTypeName("query")
    class QueryError(
        @JsonProperty("error") error: String
    ) : RequestError
}
