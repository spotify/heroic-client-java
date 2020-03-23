package com.spotify.heroic.client.api.query

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.spotify.heroic.client.api.query.MetricResponse

@JsonIgnoreProperties(ignoreUnknown = true)
data class BatchResponse (
    val results: Map<String, MetricResponse>
)
