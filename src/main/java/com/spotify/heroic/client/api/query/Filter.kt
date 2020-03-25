package com.spotify.heroic.client.api.query

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue


interface Filter

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


//TODO: add tag in, tag not in, and regex
enum class Operator {
    @JsonProperty("=")
    MATCH,

    @JsonProperty("!=")
    NOT_MATCH,

    @JsonProperty("+")
    EXIST,

    @JsonProperty("!+")
    NOT_EXIST,

    @JsonProperty("^")
    PREFIX,

    @JsonProperty("!^")
    NOT_PREFIX
}
