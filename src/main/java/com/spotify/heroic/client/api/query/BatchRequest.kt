package com.spotify.heroic.client.api.query

data class BatchRequest(val queries: MutableMap<String, MetricRequest>) {

    fun newBuilder(): Builder {
        val builder = Builder()
        builder.queries = queries
        return builder
    }

    class Builder {
        var queries: MutableMap<String, MetricRequest> = HashMap()

        fun withQuery(queryId : String, metricRequest: MetricRequest) : Builder {
            queries[queryId] = metricRequest
            return this
        }

        fun build() : BatchRequest {
            return BatchRequest(queries)
        }
    }
}
