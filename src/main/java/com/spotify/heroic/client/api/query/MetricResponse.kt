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
import com.spotify.heroic.client.api.query.DateRange.Absolute

@JsonIgnoreProperties(ignoreUnknown = true)
data class MetricResponse(
    val range: Range,
    val errors: List<RequestError>,
    private val result: List<ResultGroup>,
    val limits: List<String>,
    val commonTags: Map<String, List<String>>,
    val commonResource: Map<String, List<String>>) {

    fun toAbsoluteRange(): Absolute {
        return Absolute(range.start, range.end)
    }

    fun hasErrors(): Boolean {
        return errors.isNotEmpty()
    }

    fun hitLimits(): Boolean {
        return limits.isNotEmpty()
    }

    @Suppress("UNCHECKED_CAST")
    fun getPoints(): List<ResultGroup.Points> {
        return result as List<ResultGroup.Points>
    }

    data class Range(val start: Long, val end: Long)
}
