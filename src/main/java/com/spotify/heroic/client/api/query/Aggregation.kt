package com.spotify.heroic.client.api.query

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.concurrent.TimeUnit

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(Minimum::class, name = "min"),
    JsonSubTypes.Type(Maximum::class, name = "max"),
    JsonSubTypes.Type(Average::class, name = "average"),
    JsonSubTypes.Type(Sum::class, name = "sum"),
    JsonSubTypes.Type(Chain::class, name = "chain"),
    JsonSubTypes.Type(GroupingAggregation::class, name = "group"),
    JsonSubTypes.Type(TopK::class, name = "topk"),
    JsonSubTypes.Type(BottomK::class, name = "bottomk"),
    JsonSubTypes.Type(AboveK::class, name = "abovek"),
    JsonSubTypes.Type(BelowK::class, name = "belowk"),
    JsonSubTypes.Type(Delta::class, name = "delta"),
    JsonSubTypes.Type(DeltaPerSecond::class, name = "deltaPerSecond"),
    JsonSubTypes.Type(RatePerSecond::class, name = "ratePerSecond"),
    JsonSubTypes.Type(Count::class, name = "count"),
    JsonSubTypes.Type(NotNegative::class, name = "notNegative"),
    JsonSubTypes.Type(StdDev::class, name = "stddev"),
    JsonSubTypes.Type(Sum2::class, name = "sum2")
)
interface Aggregation

data class Sampling(
    val unit: TimeUnit,
    val value: Long
) {
    companion object {
        @JvmStatic
        fun withTime(unit: TimeUnit, value: Long) : Sampling {
            return Sampling(unit, value)
        }
    }
}

data class AboveK(val k: Double): Aggregation

data class Average(val sampling: Sampling): Aggregation

data class BelowK(val k: Double): Aggregation

data class BottomK(val k: Long): Aggregation

data class Chain(val chain: List<Aggregation>): Aggregation

data class Count(val sampling: Sampling): Aggregation

class Delta: Aggregation

class DeltaPerSecond: Aggregation

data class RatePerSecond(val sampling: Sampling): Aggregation

data class GroupingAggregation(
    val of: List<String>?,
    val each: List<Aggregation>
): Aggregation {
    companion object {
        @JvmStatic
        fun forEach(each: Aggregation) : GroupingAggregation {
            return GroupingAggregation(null, listOf(each))
        }

        @JvmStatic
        fun collapse(each: Aggregation) : GroupingAggregation{
            return GroupingAggregation(emptyList(), listOf(each))
        }

        @JvmStatic
        fun groupBy(of: List<String>, each: Aggregation) : GroupingAggregation {
            return GroupingAggregation(of, listOf(each))
        }
    }
}


data class Maximum(val sampling: Sampling): Aggregation

data class Minimum(val sampling: Sampling): Aggregation

class NotNegative: Aggregation

class StdDev: Aggregation

data class Sum(val sampling: Sampling): Aggregation

data class Sum2(val sampling: Sampling): Aggregation

data class TopK(val k: Long): Aggregation
