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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.io.IOException

/**
 * Represents a single data point.
 */
@JsonDeserialize(using = DataPointSerializer::class)
data class DataPoint(val timestamp: Long, val value: Double)

class DataPointSerializer : JsonDeserializer<DataPoint>() {
    @Throws(IOException::class)
    override fun deserialize(p: JsonParser, c: DeserializationContext): DataPoint {
        if (p.currentToken != JsonToken.START_ARRAY) {
            throw c.mappingException("Expected start of array")
        }
        var timestamp: Long
        run {
            if (p.nextToken() != JsonToken.VALUE_NUMBER_INT) {
                throw c.mappingException("Expected number (timestamp)")
            }
            timestamp = p.readValueAs(Long::class.java)
        }
        val value: Double = when (p.nextToken()) {
            JsonToken.VALUE_NUMBER_FLOAT -> p.readValueAs(Double::class.java)
            JsonToken.VALUE_NUMBER_INT -> p.readValueAs(Long::class.java).toDouble()
            else -> throw c.mappingException("Expected float (value)")
        }
        if (p.nextToken() != JsonToken.END_ARRAY) {
            throw c.mappingException("Expected end of array")
        }
        return DataPoint(timestamp, value)
    }
}
