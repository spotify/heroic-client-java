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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = ResultGroup.Points::class)
@JsonSubTypes(JsonSubTypes.Type(ResultGroup.Points::class))
interface ResultGroup {
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Points(
        val key: String?,
        val shard: Map<String, String> = Collections.emptyMap(),
        val tags: Map<String, String>,
        val values: List<DataPoint>,
        val resource: Map<String, String>) : ResultGroup
}
