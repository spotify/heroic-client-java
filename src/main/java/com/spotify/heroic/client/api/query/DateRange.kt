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

import com.fasterxml.jackson.annotation.*
import java.util.*
import java.util.concurrent.TimeUnit

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(DateRange.Relative::class),
    JsonSubTypes.Type(DateRange.Absolute::class)
)
interface DateRange {
    /**
     * Convert the date range into the time difference of the specified unit.
     *
     * @param unit Unit to convert to.
     * @return The difference in the given unit.
     */
    fun diff(unit: TimeUnit): Long

    /**
     * Multiply the size of the current date range.
     *
     * @param factor Factory to multiply with.
     * @return A modified date range.
     */
    fun multiply(factor: Long): DateRange

    /**
     * Build a readable diff.
     *
     * <p>Like "10 seconds", or "2 hours".
     */

    fun toReadableDiff(): String

    @JsonTypeName("relative")
    data class Relative(
        val unit: TimeUnit,
        val value: Long
    ) : DateRange {

        companion object {
            @JvmStatic
            fun withTime(unit: TimeUnit, value: Long) : Relative {
                return Relative(unit, value)
            }
        }
        override fun diff(unit: TimeUnit): Long {
            return unit.convert(value, this.unit)
        }

        override fun multiply(factor: Long): DateRange {
            return Relative(unit, this.value * factor)
        }

        override fun toReadableDiff(): String {
            return String.format("%d %s", value, unit.toString().toLowerCase(Locale.ENGLISH))
        }

        @JsonCreator
        constructor(
            @JsonProperty("unit") unit: String,
            @JsonProperty("value") value: Long
        ): this(TimeUnit.valueOf(unit.toUpperCase(Locale.ENGLISH)), value)
    }

    @JsonTypeName("absolute")
    data class Absolute(
        @JsonProperty("start") val start: Long,
        @JsonProperty("end") val end: Long
    ) : DateRange {

        companion object {
            @JvmStatic
            fun withTime(start: Long, end: Long) : Absolute {
                return Absolute(start, end)
            }
        }

        override fun diff(unit: TimeUnit): Long {
            return unit.convert(end - start, TimeUnit.MILLISECONDS)
        }

        override fun multiply(factor: Long): DateRange {
            return Absolute(end - (end - start) * factor, end)
        }

        override fun toReadableDiff(): String {
            return String.format("%d %s", diff(TimeUnit.SECONDS), "seconds");
        }
    }
}
