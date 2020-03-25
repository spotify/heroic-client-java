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

import java.util.concurrent.TimeUnit

data class MetricRequest(
    val range: DateRange,
    val filter: Filter,
    val aggregators: MutableList<Aggregation>,
    val features: MutableList<String>,
    val clientContext: MutableMap<String, String>
) {
    fun newBuilder(): Builder {
        val builder = Builder()
        builder.filter = filter
        builder.range = range
        builder.aggregation = aggregators
        builder.features = features
        builder.clientContext = clientContext
        return builder
    }

    class Builder {
        var filter: Filter = TrueFilter()
        var range: DateRange = DateRange.Relative(TimeUnit.HOURS, 1L)
        var aggregation: MutableList<Aggregation> = ArrayList()
        var features: MutableList<String> = ArrayList()
        var clientContext: MutableMap<String, String> = HashMap()

        fun withFilter(filter: Filter): Builder {
            this.filter = filter
            return this
        }


        fun withRange(range: DateRange): Builder {
            this.range = range
            return this
        }

        fun withAggregation(aggregation: Aggregation): Builder {
            this.aggregation.add(aggregation)
            return this
        }

        fun withAggregations(aggregations: List<Aggregation>): Builder {
            aggregation.addAll(aggregations)
            return this
        }

        fun withFeature(feature: String): Builder {
            features.add(feature)
            return this
        }

        fun withFeatures(features: List<String>): Builder {
            this.features.addAll(features)
            return this
        }

        fun withClientContext(key: String, value: String): Builder {
            clientContext.put(key, value)
            return this
        }

        fun withClientContexts(clientContexts: Map<String, String>): Builder {
            clientContext.putAll(clientContexts)
            return this
        }

        fun build(): MetricRequest {
            return MetricRequest(
                range,
                filter,
                aggregation,
                features,
                clientContext
            )
        }
    }
}
