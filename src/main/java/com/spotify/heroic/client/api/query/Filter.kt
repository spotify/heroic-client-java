package com.spotify.heroic.client.api.query

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

@JsonDeserialize(using = FilterDeserializer::class)
interface Filter

class FilterDeserializer(vc: Class<*>? = null) : StdDeserializer<Filter>(vc) {
    @JsonValue
    private var filterTags = mutableListOf<Any>()

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Filter {
        val mapper = ObjectMapper()
            .registerModule(KotlinModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        filterTags = mapper.readValue(jp);
        if(filterTags[0] == "and"){
            filterTags.removeAt(0)
        }
        return KeyTagFilter(key = null, tags = filterTags)
    }
}

class TrueFilter: Filter {
    @JsonValue
    private var filter = true
}

class KeyTagFilter(key : Key?, tags: List<Any>) : Filter {
    @JsonValue
    private var filter = mutableListOf<Any>()

    init {
        filter.add("and")
        key?.let { filter.add(it) }
        filter.addAll(tags)
    }
    companion object {
        @JvmStatic
        fun of(key : Key?) : Filter {
            return KeyTagFilter(key, listOf())
        }

        @JvmStatic
        fun of(tags: List<Tag>) : Filter {
            return KeyTagFilter(null, tags)
        }

        @JvmStatic
        fun of(key : Key?, tags: List<Any>) : Filter {
            return KeyTagFilter(key, tags)
        }
    }
}

class Key constructor(key: String) {
    @JsonValue
    private var filter = listOf("key", key)

    companion object {
        @JvmStatic
        fun of(key: String) : Key {
            return Key(key)
        }
    }
}

class Tag(operator: Operator, key: String, value: String?) {
    @JsonValue
    private val tag = listOfNotNull(operator, key, value)

    companion object {
        @JvmStatic
        fun and(operator: Operator, key: String, value: String?) : Tag {
            return Tag(operator, key, value)
        }

        @JvmStatic
        fun and(operator: Operator, key: String) : Tag {
            return Tag(operator, key, null)
        }

        @JvmStatic
        fun not(operator: Operator, key: String, value: String?) : List<Any> {
            val tag = Tag(operator, key, value)
            val notTag = mutableListOf<Any>()
            notTag.add("not")
            notTag.add(tag.tag)
            return notTag
        }
    }
}


enum class Operator {
    @JsonProperty("=")
    MATCH,

    @JsonProperty("+")
    EXIST,

    @JsonProperty("^")
    PREFIX,

    @JsonProperty("q")
    CUSTOM
}
